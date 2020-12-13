package com.cormontia.android.dicomc_echo;

import android.icu.util.Output;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class NetworkConnection {
    private static final String TAG = "Networking";

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    NetworkConnection(String host, int port) throws IOException {
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(15000); // Timeout in milliseconds.
        } catch (SocketException exc) {
            //TODO!+ handle the case that host==null
            Log.e(TAG, "Socket exception while trying to create connection to ("+host+", "+port+").");
            throw exc;
        } catch (UnknownHostException exc) {
            //TODO!+ handle the case that host==null
            Log.e(TAG, "Unknown host: "  + host);
            throw exc;
        } catch (IOException exc) {
            //TODO!+ handle the case that host==null
            Log.e(TAG, "I/O Exception while trying to create connection to ("+host+", "+port+")");
            throw exc;
        }
    }

    public byte[] sendAndReceiveAssociation(byte[] message) throws IOException {
        //TODO!+ Assert that message != null.

        // Try-with-resources requires API level 19, currently supported minimum is 14.
        outputStream = new BufferedOutputStream(socket.getOutputStream());
        inputStream = socket.getInputStream(); // new BufferedInputStream(socket.getInputStream());
        outputStream.write(message, 0, message.length);
        outputStream.flush();

        Log.i(TAG, message.length + " bytes sent, awaiting response...");

        Toolbox.logBytes(message);

        // First byte identifies the type of message; should be 0x02 for A-ASSCOCIATE-AC, 0x03 for A-Associate-RJ, and 0x07 for A-ABORT
        //TODO!+ Verify that neither of these bytes is -1.
        byte pduType = (byte) inputStream.read();
        byte reserved = (byte) inputStream.read();

        // For all possible responses (A-Associate-AC, A-Associate-RJ, and A-Abort), bytes 3-6 are the length.
        //TODO!+ assert that the read result != -1.
        byte[] length = new byte[4];
        int inputResult = inputStream.read(length,0,4);
        // Casting the bytes to int, to prevent them from being interpreted as negative values.
        int b3 = Byte.toUnsignedInt(length[0]);
        int b4 = Byte.toUnsignedInt(length[1]);
        int b5 = Byte.toUnsignedInt(length[2]);
        int b6 = Byte.toUnsignedInt(length[3]);
        //TODO!~ Find out what to do if "len" is big enough to be interpreted as negative.
        // We could cast it to "long", but then we can't allocate an array of that length.
        // The following will cause an error, expected type is int while actual type is long:
        //     long testLength = 20;
        //     byte[] testArray = new byte[testLength];

        int len = ((b3 << 24) | (b4 << 16) | (b5 << 8) | b6);

        //TODO!+ In the rest of this app, also use "|" instead of "+" for these combinations.... (search for  "<<" and ">>").

        Toolbox.logBytes(length);
        Log.i(TAG, "b3<<24=="+(b3<<24));
        Log.i(TAG, "b4<<16=="+(b4<<16));
        Log.i(TAG, "b5<<8=="+(b5<<8));
        Log.i(TAG, "b6=="+(b6));
        Log.i(TAG, "len=="+len);

        byte[] res = new byte[6 + len]; //TODO!+ Consider what to do if len is negative...
        res[0] = pduType;
        res[1] = reserved;
        res[2] = (byte) b3;
        res[3] = (byte) b4;
        res[4] = (byte) b5;
        res[5] = (byte) b6;

        inputResult = inputStream.read(res, 6, len);
        //TOOD!+ Error handling when inputResult == -1.
        Toolbox.logBytes(res);

        return res;
    }

    //TODO!~ Make this code specific for sending and receiving a DICOM C-Echo.
    //  Parameterize it with the Socket to be used, not with host/port.
    //  Also parameterize it with the Transfer Syntax for the connection.
    public byte[] sendEchoRequestBytes(byte[] message) throws IOException {
        //TODO!+ Assert that message != null.

        outputStream.write(message, 0, message.length);
        outputStream.flush();

        Log.i(TAG, message.length + " bytes sent, awaiting response...");
        Toolbox.logBytes(message);

        List<Byte> serverResponse = new ArrayList<>();

        //TODO!~ NAIVE SOLUTION: Just read all.

        // For debugging.
        StringBuffer strBytesInHex = new StringBuffer();
        StringBuffer strBytesAsChars = new StringBuffer();
        int cnt = 0;

        int ch;
        Log.i(TAG, "Entering while loop...");
        do{
            Log.i(TAG, "NetworkConnection.sendEchoRequest(...): about to read from InputStream."); //TODO!- for debugging
            ch = inputStream.read();
            Log.i(TAG, "NetworkConnection.sendEchoRequest(...): have read from InputStream: " + ch); //TODO!- for debugging
            if ( ch == -1)
                break;

            serverResponse.add((byte) ch);

            // For debugging
            strBytesInHex.append(Converter.byteToHexString((byte) ch) + "  ");
            if (ch >= 32 && ch <= 127) {
                strBytesAsChars.append((char) ch);
            } else {
                strBytesAsChars.append(".");
            }

            cnt++;
            if (cnt >= 16) {
                Log.i(TAG, strBytesInHex.toString() + " " + strBytesAsChars);
                cnt = 0;
                strBytesInHex = new StringBuffer("");
                strBytesAsChars = new StringBuffer("");
            }

        } while (ch != -1);
        Log.i(TAG, "Completed while loop.");

        // For debugging
        Log.i(TAG, strBytesInHex.toString() + "  " + strBytesAsChars);

        Log.i(TAG, "Leaving while loop.");
        byte[] responseBytes = Converter.byteListToByteArray(serverResponse);

        //TODO!~ Move these outside of the C-Echo-Rq. This must be done after Assocation Release.
        inputStream.close();
        outputStream.close(); //TODO?+ flush it as well? IIRC close() automatically flushes.
        socket.close();

        return responseBytes;
    }
}
