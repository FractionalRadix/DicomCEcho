package com.cormontia.android.dicomc_echo;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

class DicomEchoRequest {

    private static final String TAG = "DICOM C-ECHO";

    /**
     * Send a DICOM C-ECHO request to a specified address.
     *
     * @param address Address of the DICOM C-ECHO server (not including port number).
     * @param port    Port of the DICOM C-ECHO server.
     */
    static void sendEchoRequest(String address, int port, EchoRequestCallback callback) {

        Log.d(TAG, "Entered method sendEchoRequest(URL)");
        Log.d(TAG, "address==" + address);
        Log.d(TAG, "Port==" + port);

        String callingAETitle = "ECHOSCU"; //TODO!~ Get this from user input.
        String calledAETitle = "ECHOSCP";  //TODO!~ Get this from user input.
        PresentationContext presentationContext = Associator.presentationContextForEcho();
        Associator.sendAAssociateRQ(callingAETitle, calledAETitle, address, port, presentationContext);

        /*
        try {
            Socket socket = new Socket(address, port);
            socket.setSoTimeout(5000); // Timeout in milliseconds.

            // Create the C-ECHO request.
            List<DicomElement> elements = RequestFactory.createEchoRequest();
            byte[] echoRequestBytes = Converter.binaryRepresentation(elements);

            //TODO!- FOR DEBUGGING
            //logBytesAsHexString(echoRequestBytes);

            // Try-with-resources requires API level 19, currently supported minimum is 14.
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            bos.write(echoRequestBytes, 0, echoRequestBytes.length);
            bos.flush();
            bos.close(); //TODO?~ Should this be done here? Also, didn't close() automatically flush() ?

            // Wait for the C-ECHO Response (if any).
            // When the C-ECHO Response is in, parse it.
            // (Not that there is much to parse in a C-ECHO Response...)
            // If the Response doesn't arrive, or is somehow wrong... we need to report this to the user.
            // If the Response is received properly, we also need to report this to the user.

            List<Byte> serverResponse = new ArrayList<>();
            InputStream bis = socket.getInputStream();
            //TODO!~ NAIVE SOLUTION: Just read all.
            //   Need some sort of timeout. Or parse while reading.
            int ch;
            while ((ch = bis.read()) != -1) {
                Log.i(TAG, Converter.byteToHexString((byte) ch));
                serverResponse.add((byte) ch);
            }
            byte[] responseBytes = Converter.byteListToByteArray(serverResponse);

            EchoResult result = new EchoResult(EchoResult.Status.Success, "C-ECHO-Rsp received successfully!", responseBytes);
            callback.onComplete(result);
            return;
        } catch (SocketTimeoutException exc) {
            Log.e(TAG, "Socket timeout exception.");
            String timeoutMsg = "Timeout. Please check if the specified host and port are correct, and if the server is available.";
            EchoResult result = new EchoResult(EchoResult.Status.Failure, timeoutMsg, null);
            callback.onComplete(result);
            return;
        } catch (UnknownHostException exc) {
            Log.e(TAG, "Unknown host exception.");
            EchoResult result = new EchoResult(EchoResult.Status.Failure, "Unknown host.", null);
            callback.onComplete(result);
            return;
        } catch (SecurityException exc) {
            Log.e(TAG, "Security exception." + exc.toString());
            EchoResult result = new EchoResult(EchoResult.Status.Failure, "Failed to get response, due to security reasons.", null);
            callback.onComplete(result);
            return;
        } catch (IllegalArgumentException exc) {
            Log.e(TAG, "Illegal Argument Exception." + exc.toString());
            EchoResult result = new EchoResult(EchoResult.Status.Failure, "Failed to get response, wrong arguments (were server and port number specified correctly?)", null);
            callback.onComplete(result);
            return;
        } catch (IOException exc) {
            Log.e(TAG, "I/O exception in method sendEchoRequest()");
            Log.e(TAG, exc.getMessage());
            Log.e(TAG, exc.toString());
            EchoResult result = new EchoResult(EchoResult.Status.Failure, "An I/O error occurred while sending/receiving the C-ECHO. Please check your network and try again.", null);
            callback.onComplete(result);
            return;
        }
         */
    }
}

class EchoResult {
    public enum Status { Failure, Success };

    private Status status;
    private String messageForUser;
    private byte[] serverResponse;

    EchoResult(Status status, String msg, byte[] serverResponse) {
        this.status = status;
        this.messageForUser = msg;
        this.serverResponse = serverResponse;
    }

    public String getMessage( ) {
        return messageForUser;
    }
}

interface EchoRequestCallback {
    void onComplete(EchoResult result);
}
