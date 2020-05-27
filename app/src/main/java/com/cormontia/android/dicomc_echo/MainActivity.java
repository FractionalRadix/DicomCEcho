package com.cormontia.android.dicomc_echo;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//TODO!+
// (1) Use ONLY List<Byte>.
//    The constant conversion between List<Byte> and byte[] is a waste.
// (2) Add checks to the DicomElement constructor, to verify that the length of the content corresponds to that given by the VR.
//    For this purpose, the VR's with fixed lengths should have their length values added.
//    Note that many VR's (like String, Unknown and Sequence) have variable lengths.
//    Also note however, that SOME of those variable-length VR's still have a MAXIMUM length, that we should check against.

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText etUriInput = (EditText) findViewById(R.id.editText);
        Log.d("Dicom C-ECHO", "Found EditText.");

        Button btEchoButton = (Button) findViewById(R.id.sendEchoButton);
        Log.d("Dicom C-ECHO", "Found Button.");

        btEchoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = etUriInput.getText().toString();
                        try {
                            URL url = new URL(text);
                            new EchoOperator().execute(url);
                        }
                        catch (MalformedURLException exc)
                        {
                            //TODO!+ Warn the user that they didn't enter a proper URL
                        }
                    }
                }
        );
    }

    //TODO!~ Use the Java Concurrency operations, since AsyncTask is deprecated.
    //TODO?~ There are multiple result values, not just True and False. IT can be timeout, but also server error (server may actively refuse)
    class EchoOperator extends AsyncTask<URL,Integer,Boolean>
    {
        protected Boolean doInBackground(URL... urls)
        {
            Log.d("EchoOperator", "Started the doInBackground Task.");
            URL url = urls[0];
            Log.d("EchoOperator", "Using the first parameter as the URL.");
            try {
                java.net.URLConnection con = url.openConnection();
                Log.d("EchoOperator", "Created URL Connection");
                //TODO!+ con.connect();

                //TODO!+ This is where the real work starts.
                // We need to build a C-ECHO Request, and sent it to the specified host.
                // Then we need to wait for the C-ECHO Response (if any), and parse it.
                // (Not that there is much to parse in a C-ECHO Response...)
                // If the Response doesn't arrive, or is somehow wrong... we need to report this to the user.
                // If the Response is received properly, we also need to report this to the user.
                DicomElement commandGroupLength = new DicomElement(
                        new DicomTag( (short) 0x0000, (short) 0x0000 ),
                        DicomVR.VR.UL,
                        //4,
                        new byte[] { 56, 0, 0, 0 }
                );
                DicomElement affectedServiceClassUID = new DicomElement(
                        new DicomTag( (short) 0x0000, (short) 0x0002 ),
                        DicomVR.VR.UI,
                        //18,
                        DicomUIDs.byteArrayRepresentation( DicomUIDs.affectedServiceClassUID )
                );
                DicomElement commandField = new DicomElement(
                        new DicomTag( (short) 0x0000, (short) 0x0100 ),
                        DicomVR.VR.US,
                        EndianConverter.littleEndian( (short) 0x0030 )
                        //new byte[] { 0x03, 0x00, 0x00, 0x00 }
                );
                DicomElement messageID = new DicomElement(
                        new DicomTag( (short) 0x0000, (short) 0x0110 ),
                        DicomVR.VR.US,
                        new byte[] {(byte) 0xCA, (byte) 0xFE } //TODO!+ Randomize....
                );
                DicomElement dataSetType = new DicomElement(
                        new DicomTag( (short) 0x0000, (short) 0x0800 ),
                        DicomVR.VR.US,
                        new byte[] { 0x01, 0x01 }
                );

                Log.d("EchoOperator", "Going to calculate Dicom Element byte sequences...");
                //TODO!~ Instead of asking the byte representations in order, then adding them...
                //   find a way to simply loop over the elements and append the bytes.
                //  It saves a lot of array copying.
                byte[] commandGroupLengthBytes = commandGroupLength.littleEndianRepresentation( );
                byte[] affectedServiceClassUIDBytes = affectedServiceClassUID.littleEndianRepresentation( );
                byte[] commandFieldBytes = commandField.littleEndianRepresentation();
                byte[] messageIDBytes = messageID.littleEndianRepresentation();
                byte[] dataSetTypeBytes = dataSetType.littleEndianRepresentation();

                /*
                //TODO!- FOR DEBUGGING
                Log.d("EchoOperator", "Going to show resulting byte arrays as hex strings...");
                logBytesAsHexString( commandGroupLengthBytes );
                logBytesAsHexString( affectedServiceClassUIDBytes );
                logBytesAsHexString( commandFieldBytes );
                logBytesAsHexString( messageIDBytes );
                logBytesAsHexString( dataSetTypeBytes );
                */

                byte[] echoRequestBytes = ByteArrayHelper.appendByteArrays( commandGroupLengthBytes, affectedServiceClassUIDBytes, commandFieldBytes, messageIDBytes, dataSetTypeBytes );

                //TODO!- FOR DEBUGGING
                logBytesAsHexString( echoRequestBytes );

                con.setDoOutput( true );
                con.setDoInput( true );
                con.connect( );

                // Try-with-resources require API level 19, currently supported minimum is 14.
                OutputStream os = con.getOutputStream();
                os.write( echoRequestBytes, 0, echoRequestBytes.length );


                InputStream is = con.getInputStream();

                //TODO!~ NAIVE SOLUTION: Just read all.
                //   Need some sort of timeout. Or parse while reading.

                int ch;
                while ( ( ch = is.read( ) ) != -1 )
                {
                    Log.i("EchoOperator", byteToHexString( (byte) ch ));
                }


            }
            catch ( IOException exc )
            {
                //TODO!+
            }
            return false;
        }

        //TODO!+ Add a Progress Updater

    }

    /**
     * Converting byte values to their hexadecimal representations.
     * We used Integer.toHexString first, but when provided with byte values of 128 or higher, it interpreted them as negative values.
     * @param input
     * @return
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
        String aux = "\r\n";
        for ( int i = 0; i < bytes.length; i++ )
        {
            aux = aux + " " + byteToHexString( bytes[ i ] );
            if ( ( i + 1 ) % 16 == 0 )
            {
                aux += "\r\n";
            }
        }
        Log.d("EchoOperator", aux );
    }

}
