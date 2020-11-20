package com.cormontia.android.dicomc_echo;

import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repository {

    //TODO?~ Android documentation suggests we put the ExecutorService in a dependency injection container.
    // For now we're putting it in Repository - there's enough to refactor already. (Famous last words...)
    private static ExecutorService executorService;
    private static final int NTHREADS = 3;

    Repository() {
        if (executorService == null) {
            //TODO?~ Is it wise to use a fixed thread pool here...?
            executorService = Executors.newFixedThreadPool(NTHREADS);
        }
    }

    public void sendEchoRequest(String host, int port) {
        executorService.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        EchoResult result = DicomEchoRequest.sendEchoRequest(host, port);
                    }
                }
        );
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
}

class DicomEchoRequest {
    /**
     * Send a DICOM C-ECHO request to a specified address.
     * @param address Address of the DICOM C-ECHO server (not including port number).
     * @param port Port of the DICOM C-ECHO server.
     */
    static EchoResult sendEchoRequest(String address, int port)
    {
        final String tag = "sendEchoRequest";
        final EchoResult result;

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
                Log.i(tag, Converter.byteToHexString((byte) ch));
                serverResponse.add((byte) ch);
            }
            byte[] responseBytes = Converter.byteListToByteArray(serverResponse);

            return new EchoResult(EchoResult.Status.Success, "C-ECHO-Rsp received succesfully!", responseBytes);
        }
        catch (SocketTimeoutException exc) {
            String timeoutMsg = "Timeout. Please check if the specified host and port are correct, and if the server is available.";
            return new EchoResult(EchoResult.Status.Failure, timeoutMsg, null);
        }
        catch (UnknownHostException exc) {
            Log.e(tag, "Unknown host exception.");
            return new EchoResult(EchoResult.Status.Failure, "Unknown host.", null);
        }
        catch (SecurityException exc) {
            Log.e(tag, "Security exception." + exc.toString());
            return new EchoResult(EchoResult.Status.Failure, "Failed to get response, due to security reasons.", null);
        }
        catch (IllegalArgumentException exc) {
            Log.e(tag, "Illegal Argument Exception." + exc.toString());
            return new EchoResult(EchoResult.Status.Failure, "Failed to get response, wrong arguments (were server and port number specified correctly?)", null);
        }
        catch (IOException exc)
        {
            Log.e(tag, "I/O exception in method sendEchoRequest()");
            Log.e(tag, exc.getMessage());
            Log.e(tag, exc.toString());
            return new EchoResult(EchoResult.Status.Failure, "An I/O error occured while sending/receiving the C-ECHO. Please check your network and try again.", null);
        }
    }

}
