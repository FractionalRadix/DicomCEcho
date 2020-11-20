package com.cormontia.android.dicomc_echo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

//TODO!+
// (1) Use ONLY "byte[]", not "List<Byte>".
//    The constant conversion between List<Byte> and byte[] is a waste.
// (2) Add checks to the DicomElement constructor, to verify that the length of the content corresponds to that given by the VR.
//    For this purpose, the VR's with fixed lengths should have their length values added.
//    Note that many VR's (like String, Unknown and Sequence) have variable lengths.
//    Also note however, that SOME of those variable-length VR's still have a MAXIMUM length, that we should check against.

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DICOM C-ECHO";
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        final EditText etUriInput = findViewById(R.id.etHost);
        Log.d(TAG, "Found EditText.");

        Button btEchoButton = findViewById(R.id.sendEchoButton);
        Log.d(TAG, "Found Button.");

        btEchoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = etUriInput.getText().toString();
                        Log.d(TAG, "ECHO button clicked");
                        String address = text;
                        int port = 104; //TODO!~ Take it from a separate input (that defaults to 104)
                        EchoOperatorRunnable runnable = new EchoOperatorRunnable(new EchoTask(), address, port);
                        viewModel.execute(runnable);
                    }
                }
        );
    }

    private Handler handler;

    class EchoOperatorRunnable implements Runnable
    {
        private EchoTask echoTask;
        private String address;
        private int port;

        public EchoOperatorRunnable(EchoTask echoTask, String address, int port)
        {
            Log.d("EchoOperatorRunnable", "Entered constructor of EchoOperatorRunnable.");

            this.echoTask = echoTask;
            this.address = address;
            this.port = port;

            handler = new Handler(Looper.getMainLooper())
            {
                public void handleMessage(Message msg)
                {
                    //TODO!~ Get server response and display it in a field...
                    Toast.makeText(MainActivity.this, "Result is in", Toast.LENGTH_LONG).show();
                }
            };
            Log.d("EchoOperatorRunnable", "Leaving constructor of EchoOperatorRunnable.");
        }

        public void run( )
        {
            Log.d("EchoOperatorRunnable", "Entered the run() method.");
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND); //TODO?~
            Log.d("EchoOperatorRunnable", "Calling sendEchoRequest(...).");
            sendEchoRequest(address, port);
            Log.d("EchoOperatorRunnable", "sendEchoRequest(...) called.");
            echoTask.setResult(1, new byte[] {(byte) 0xCA, (byte) 0xFE }); //TODO!~ Put the result here! Whether timeout, refusal or received response bytes.
            Log.d("EchoOperatorRunnable", "Leaving the run() method.");
        }
    }

    /**
     * Send a DICOM C-ECHO request to a specified address.
     * @param address Address of the DICOM C-ECHO server (not including port number).
     * @param port Port of the DICOM C-ECHO server.
     */
    void sendEchoRequest(String address, int port)
    {
        final String tag = "sendEchoRequest";

        Log.d(tag, "Entered method sendEchoRequest(URL)");
        Log.d(tag, "address=="+address);
        Log.d(tag, "Port=="+port);
        try {
            Socket socket = new Socket(address, port);
            socket.setSoTimeout(5000); // Teimout in milliseconds.

            // Create the C-ECHO request.
            List<DicomElement> elements = RequestFactory.createEchoRequest();
            byte[] echoRequestBytes = Converter.binaryRepresentation(elements);

            //TODO!- FOR DEBUGGING
            logBytesAsHexString(echoRequestBytes);

            // Try-with-resources requires API level 19, currently supported minimum is 14.
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            bos.write(echoRequestBytes, 0, echoRequestBytes.length);
            bos.flush();
            bos.close(); //TODO?~ Should this be done here? Also, didn't close() automatically flush() ?

            // Wait (asynchronously, of course) for the C-ECHO Response (if any).
            // When the C-ECHO Response is in, parse it.
            // (Not that there is much to parse in a C-ECHO Response...)
            // If the Response doesn't arrive, or is somehow wrong... we need to report this to the user.
            // If the Response is received properly, we also need to report this to the user.

            InputStream bis = socket.getInputStream();

            //TODO!~ NAIVE SOLUTION: Just read all.
            //   Need some sort of timeout. Or parse while reading.
            int ch;
            while ((ch = bis.read()) != -1) {
                Log.i(tag, Converter.byteToHexString((byte) ch));
            }
        }
        catch (SocketTimeoutException exc) {
            String timeoutMsg = "Timeout. Please check if the specified host and port are correct, and if the server is available.";
            Toast.makeText(this, timeoutMsg, Toast.LENGTH_LONG).show();

            //TODO!~ Move this tot the Looper. Only the original thread that created a View hierarchy may touch its views.
            // And this method is called as a background Thread, so cannot touch the Echo Result View.
            //TextView tv = findViewById(R.id.tvEchoResult);
            //tv.setText(timeoutMsg);
        }
        catch (UnknownHostException exc) {
            //TODO!~ Here we should inform the user what String was used for the host address.
            // (At the time of writing it's a constant dummy value....)
            Log.e(tag, "Unknown host exception.");

            //TODO!~ Move this tot the Looper. Only the original thread that created a View hierarchy may touch its views.
            // And this method is called as a background Thread, so cannot touch the Echo Result View.
            //TextView tv = findViewById(R.id.tvEchoResult);
            //tv.setText("Could not connect to the given address: host is unknown. Please check the spelling, and check if the host is available.");
        }
        catch (SecurityException exc) {
            //TODO!+
        }
        catch (IllegalArgumentException exc) {
            //TODO!+
        }
        catch (IOException exc)
        {
            Log.e(tag, "I/O exception in method sendEchoRequest()");
            Log.e(tag, exc.getMessage());
            Log.e(tag, exc.toString());

            //TODO!~ Move this tot the Looper. Only the original thread that created a View hierarchy may touch its views.
            // And this method is called as a background Thread, so cannot touch the Echo Result View.
            //TextView tv = findViewById(R.id.tvEchoResult);
            //tv.setText("An I/O exception occurred while trying to send the C-ECHO request. Please try again.");

        }
    }

    /**
     * Log an array of bytes as a string of hexadecimal numbers.
     * This is used for debugging.
     * @param bytes The bytes whose hex values we want to see in the log.
     */
    private static void logBytesAsHexString( byte[] bytes )
    {
        Log.d("EchoOperator", "About to show " + bytes.length + " bytes...");
        StringBuilder aux = new StringBuilder("\r\n");
        for ( int i = 0; i < bytes.length; i++ )
        {
            aux.append(" ").append(Converter.byteToHexString(bytes[i]));
            if ( ( i + 1 ) % 16 == 0 )
            {
                aux.append("\r\n");
            }
        }
        Log.d("EchoOperator", aux.toString());
    }

    class EchoTask
    {
        //TODO!~ Use this to set the result of sending the request. Whether timeout, server refusal, or responding bytes.
        public void setResult(int n, byte[] result)
        {
            Message msg = handler.obtainMessage(n, result);
            msg.sendToTarget();
        }
    }
}
