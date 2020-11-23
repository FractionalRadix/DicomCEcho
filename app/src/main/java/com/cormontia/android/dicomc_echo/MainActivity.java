package com.cormontia.android.dicomc_echo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

//TODO!+
// (1) Use ONLY "byte[]", not "List<Byte>".
//    The constant conversion between List<Byte> and byte[] is a waste.
//    (Or, maybe use List<Byte> only during construction of things, then turn them into byte[] at the last moment.
//     Note that Java does not have a primitive stream for bytes; i.e. there is no ByteStream like there is IntStream, DoubleStream or LongStream.
//     So the you'll still be stuck with the unboxing, and the fact that a Byte may be NULL.)
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

        final EditText etHostname = findViewById(R.id.etHost);
        final EditText etPortNumber = findViewById(R.id.etPortNumber);
        Button btEchoButton = findViewById(R.id.sendEchoButton);
        final TextView tvResultField = findViewById(R.id.tvEchoResult);

        btEchoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "ECHO button clicked");
                        String hostname = etHostname.getText().toString();
                        int port = 104;
                        String portNrAsString = etPortNumber.getText().toString();
                        try {
                            port = Integer.parseInt(portNrAsString);
                        }
                        catch (NumberFormatException exc) {
                            tvResultField.setText("Bad number for port: " + portNrAsString);
                            tvResultField.append("Defaulting to port " + port);
                        }
                        Log.d(TAG, "Asking ViewModel to send Echo Request.");
                        viewModel.sendEchoRequest(hostname, port);
                    }
                }
        );

        viewModel.getEchoResult().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String str) {
                Log.i(TAG, "Echo observer: str=="+str);
                String messageForUser = viewModel.getEchoResult().getValue();
                tvResultField.setText(messageForUser);
            }
        });
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
}

