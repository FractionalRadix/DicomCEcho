package com.cormontia.android.dicomc_echo;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Networking {
    private static final String TAG = "Networking";

    public static byte[] sendAndReceiveAssociation(String host, int port, byte[] message) throws IOException {
        //TODO!+ Assert that message != null. (Same about host, actually...)

        Socket socket = new Socket(host, port);
        socket.setSoTimeout(15000); // Timeout in milliseconds.

        // Try-with-resources requires API level 19, currently supported minimum is 14.
        OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
        InputStream inputStream = socket.getInputStream(); // new BufferedInputStream(socket.getInputStream());
        outputStream.write(message, 0, message.length);
        outputStream.flush();

        Log.i(TAG, message.length + " bytes sent, awaiting response...");

        Toolbox.logBytes(message);

        // First byte identifies the type of message; should be 0x02 for A-ASSCOCIATE-AC, 0x03 for A-Associate-RJ, and 0x07 for A-ABORT
        //TODO!+ Verify that neither of these bytes is -1.
        byte pduType = (byte) inputStream.read();
        byte reserved = (byte) inputStream.read();

        // For all possible responses (A-Associate-AC, A-Associate-RJ, and A-Abort), bytes 3-6 are the length.
        //TODO!+ Create a 4-byte array and use inputStream.read(byte[],offset,length).
        //TODO!+ assert that the read result != -1.
        int b3 = inputStream.read();
        int b4 = inputStream.read();
        int b5 = inputStream.read();
        int b6 = inputStream.read();
        int len = (b3 << 24) | (b4 << 16) | (b5 << 8) | b6;

        //TODO!+ In the rest of this app, also use "|" instead of "+" for these combinations.... (search for  "<<" and ">>").

        Log.i(TAG, "len=="+len);

        byte[] res = new byte[6 + len]; //TODO!+ Consider what to do if len is negative...
        res[0] = pduType;
        res[1] = reserved;
        res[2] = (byte) b3;
        res[3] = (byte) b4;
        res[4] = (byte) b5;
        res[5] = (byte) b6;
        for (int i = 0; i < len; i++) {
            //TODO!+ Handle the case that the server returns -1...
            res[i + 6] = (byte) inputStream.read();
        }

        return res;
    }

    public static byte[] OLD_sendAndReceive(String host, int port, byte[] message) throws IOException {
        //TODO!+ Assert that message != null. (Same about host, actually...)

        Socket socket = new Socket(host, port);
        socket.setSoTimeout(15000); // Timeout in milliseconds.

        // Try-with-resources requires API level 19, currently supported minimum is 14.
        OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
        InputStream inputStream = socket.getInputStream(); // new BufferedInputStream(socket.getInputStream());
        outputStream.write(message, 0, message.length);
        outputStream.flush();

        Log.i(TAG, message.length + " bytes sent, awaiting response...");

        Toolbox.logBytes(message);

        // Receive the response, hopefully an A-Associate-AC. Note that it can also be A-Associate-RQ or A-Associate-ABORT.

        List<Byte> serverResponse = new ArrayList<>();

        //TODO!~ NAIVE SOLUTION: Just read all.

        // For debugging.
        StringBuffer strBytesInHex = new StringBuffer();
        StringBuffer strBytesAsChars = new StringBuffer();
        int cnt = 0;

        int ch;
        Log.i(TAG, "Entering while loop...");
        do{
            ch = inputStream.read();
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

        inputStream.close();
        outputStream.close(); //TODO?+ flush it as well? IIRC close() automatically flushes.
        socket.close();

        return responseBytes;
    }
}
