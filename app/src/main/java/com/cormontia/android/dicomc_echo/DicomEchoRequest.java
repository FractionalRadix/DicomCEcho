package com.cormontia.android.dicomc_echo;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
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

        // Send the A-Associate-RQ.
        String callingAETitle = "ECHOSCU"; //TODO!~ Get this from user input.
        String calledAETitle = "ECHOSCP";  //TODO!~ Get this from user input.
        PresentationContext presentationContext = Associator.presentationContextForEcho();

        //TODO!+ Interpret the result, then act according to it.
        // 1. The result can be: timeout, A-Associate-AC, A-Associate-RJ, or A-Associate-ABORT.
        //    Interpret the result.
        // 2. Act corresponding to the result.
        //    A-Associate-AC means the Assocation is established and can be used.
        //      Create a class with the parameters for this DICOM association.
        //      Then send a Verification message using these parameters, wait for the response, and interpret it.
        //    The others mean that for, whatever reason, the Assocation is not established. Inform the user.

        // Until we have implemented the above two steps...

        EchoResult res = Associator.openDicomAssociation(callingAETitle, calledAETitle, address, port, presentationContext);
        callback.onComplete(res);
        return;

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
