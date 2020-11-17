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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

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

        final EditText etUriInput = findViewById(R.id.editText);
        Log.d(TAG, "Found EditText.");

        Button btEchoButton = findViewById(R.id.sendEchoButton);
        Log.d(TAG, "Found Button.");

        btEchoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = etUriInput.getText().toString();
                        try {
                            Log.d(TAG, "ECHO button clicked");
                            URL url = new URL(text);
                            EchoOperatorRunnable runnable = new EchoOperatorRunnable(new EchoTask(), url);
                            viewModel.execute(runnable);
                        }
                        catch (MalformedURLException exc)
                        {
                            Log.e(TAG, "Malformed URL: " + text);
                            //TODO!+ Warn the user that they didn't enter a proper URL
                        }
                    }
                }
        );
    }

    private Handler handler;

    class EchoTask
    {
        //TODO!~ Use this to set the result of sending the request. Whether timeout, server refusal, or responding bytes.
        public void setResult(int n, byte[] result)
        {
            Message msg = handler.obtainMessage(n, result);
            msg.sendToTarget();
        }
    }

    class EchoOperatorRunnable implements Runnable
    {
        private EchoTask echoTask;
        private URL url;

        public EchoOperatorRunnable(EchoTask echoTask, URL url)
        {
            Log.d("EchoOperatorRunnable", "Entered constructor of EchoOperatorRunnable.");

            this.echoTask = echoTask;
            this.url = url;

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
            sendEchoRequest(url);
            Log.d("EchoOperatorRunnable", "sendEchoRequest(...) called.");
            echoTask.setResult(1, new byte[] {(byte) 0xCA, (byte) 0xFE }); //TODO!~ Put the result here! Whether timeout, refusal or received response bytes.
            Log.d("EchoOperatorRunnable", "Leaving the run() method.");
        }
    }

    /**
     * Send a DICOM C-ECHO request to a specified address.
     * @param url URL of the DICOM C-ECHO server.
     */
    void sendEchoRequest(URL url)
    {
        final String tag = "sendEchoRequest";



        Log.d(tag, "Enterend method sendEchoRequest(URL)");
        Log.d(tag, "url=="+url);
        Log.d(tag, "Port=="+url.getPort());
        try {
            //TODO!~ Insert the real address...
            Socket socket = new Socket("example.com/dicomServer", 104);

            java.net.URLConnection con = url.openConnection();
            Log.d(tag, "Created URL Connection");

            // Create the C-ECHO request.
            List<DicomElement> elements = createEchoRequest();
            byte[] echoRequestBytes = binaryRepresentation(elements);

            //TODO!- FOR DEBUGGING
            logBytesAsHexString(echoRequestBytes);

            // Send the C-ECHO request to the specified host.
            con.setDoOutput(true);
            con.setDoInput(true);
            con.connect();

            // Try-with-resources requires API level 19, currently supported minimum is 14.
            OutputStream os = con.getOutputStream();
            os.write(echoRequestBytes, 0, echoRequestBytes.length);

            // Wait (asynchronously, of course) for the C-ECHO Response (if any).
            // When the C-ECHO Response is in, parse it.
            // (Not that there is much to parse in a C-ECHO Response...)
            // If the Response doesn't arrive, or is somehow wrong... we need to report this to the user.
            // If the Response is received properly, we also need to report this to the user.

            InputStream is = con.getInputStream();

            //TODO!~ NAIVE SOLUTION: Just read all.
            //   Need some sort of timeout. Or parse while reading.

            int ch;
            while ((ch = is.read()) != -1) {
                Log.i(tag, byteToHexString((byte) ch));
            }
        }
        catch (UnknownHostException exc) {
            //TODO!~ Here we should inform the user what String was used for the host address.
            // (At the time of writing it's a constant dummy value....)
            Log.e(tag, "Unknown host exception.");
        }
        catch (IOException exc)
        {
            Log.e(tag, "I/O exception in method sendEchoRequest()");
            Log.e(tag, exc.getMessage());
            Log.e(tag, exc.toString());
            //TODO!+
        }
    }

    /**
     * Calculates the binary representation of a list of DICOM elements.
     * In other words, given a list of elements, returns a byte array to represent these elements, in order.
     * @param elements A list of DICOM elements.
     * @return A single byte array, containing the binary (byte array) representation of the subsequent DICOM elements.
     */
    private byte[] binaryRepresentation(List<DicomElement> elements)
    {
        byte[][] byteRepresentations = new byte[elements.size()][];
        for (int i = 0; i < elements.size(); i++)
        {
            byteRepresentations[i] = elements.get(i).littleEndianRepresentation();
        }
        return ( ByteArrayHelper.appendByteArrays(byteRepresentations) );
    }

    private List<DicomElement> createEchoRequest()
    {
        List<DicomElement> res = new ArrayList<>();

        // Command Group Length
        res.add(new DicomElement(
                new DicomTag( (short) 0x0000, (short) 0x0000 ),
                DicomVR.VR.UL,
                //4,
                new byte[] { 56, 0, 0, 0 }
        ));

        // Affected Service Class UID
        res.add(new DicomElement(
                new DicomTag( (short) 0x0000, (short) 0x0002 ),
                DicomVR.VR.UI,
                //18,
                DicomUIDs.byteArrayRepresentation( DicomUIDs.affectedServiceClassUID )
        ));

        // Command Field
        res.add(new DicomElement(
                new DicomTag( (short) 0x0000, (short) 0x0100 ),
                DicomVR.VR.US,
                EndianConverter.littleEndian( (short) 0x0030 )
                //new byte[] { 0x03, 0x00, 0x00, 0x00 }
        ));

        // Message ID
        res.add(new DicomElement(
                new DicomTag( (short) 0x0000, (short) 0x0110 ),
                DicomVR.VR.US,
                new byte[] {(byte) 0xCA, (byte) 0xFE } //TODO!+ Randomize....
        ));

        // Data Set Type
        res.add(new DicomElement(
                new DicomTag( (short) 0x0000, (short) 0x0800 ),
                DicomVR.VR.US,
                new byte[] { 0x01, 0x01 }
        ));

        return res;
    }

    /**
     * Converting byte values to their hexadecimal representations.
     * We used Integer.toHexString first, but when provided with byte values of 128 or higher, it interpreted them as negative values.
     * @param input A single byte.
     * @return Hexadecimal representation of the byte, unsigned.
     */
    private static String byteToHexString( byte input )
    {
        char[] hexDigits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

        int nibble0 = input & 0x0F;
        int nibble1 = ( input & 0xF0 ) >> 4;
        char ch0 = hexDigits[ nibble0 ];
        char ch1 = hexDigits[ nibble1 ];

        return new String( new char[] { ch1, ch0 } );
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
            aux.append(" ").append(byteToHexString(bytes[i]));
            if ( ( i + 1 ) % 16 == 0 )
            {
                aux.append("\r\n");
            }
        }
        Log.d("EchoOperator", aux.toString());
    }

}
