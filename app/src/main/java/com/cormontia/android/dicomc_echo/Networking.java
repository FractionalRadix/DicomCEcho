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

    public static byte[] sendAndReceive(String host, int port, byte[] message) throws IOException {
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
        int cnt = 0;

        int ch;
        Log.i(TAG, "Entering while loop...");
        do{
            ch = inputStream.read();
            Log.i(TAG, "ch=="+ch);
            if ( ch == -1)
                break;

            Log.i(TAG, Converter.byteToHexString((byte) ch));
            serverResponse.add((byte) ch);

            // For debugging
            strBytesInHex.append(Converter.byteToHexString((byte) ch) + " ");
            cnt++;
            if (cnt > 16) {
                Log.i(TAG, strBytesInHex.toString());
                cnt = 0;
                strBytesInHex = new StringBuffer("");
            }

        } while (ch != -1);

        Log.i(TAG, "Leaving while loop.");
        byte[] responseBytes = Converter.byteListToByteArray(serverResponse);

        inputStream.close();
        outputStream.close(); //TODO?+ flush it as well? IIRC close() automatically flushes.
        socket.close();

        return responseBytes;
    }
}
