package com.cormontia.android.dicomc_echo;

import android.net.Network;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

class DicomEcho {

    private static final String TAG = "DICOM C-ECHO";

    /**
     * Send a DICOM C-ECHO request to a specified address.
     *
     * @param address Address of the DICOM C-ECHO server (not including port number).
     * @param port    Port of the DICOM C-ECHO server.
     */
    static void performEcho(String address, int port, EchoRequestCallback callback) {

        Log.d(TAG, "Entered method sendEchoRequest(URL)");
        Log.d(TAG, "address==" + address);
        Log.d(TAG, "Port==" + port);

        // Send the A-Associate-RQ.
        String callingAETitle = "ECHOSCU"; //TODO!~ Get this from user input.
        String calledAETitle = "ECHOSCP";  //TODO!~ Get this from user input.
        PresentationContext presentationContext = Associator.presentationContextForEcho();

        NetworkConnection networkConnection = null;
        try {
            networkConnection = new NetworkConnection(address, port);
        } catch (IOException exc)
        {}

        Log.i(TAG, "Opening DICOM Association.");
        DicomAssociationRequestResult res = Associator.openDicomAssociation(networkConnection, callingAETitle, calledAETitle, presentationContext);
        Log.i(TAG, "Completed attempt to open DICOM Association.");
        if (res instanceof NetworkingFailure) {
            callback.onComplete((NetworkingFailure) res);
        } else if (res instanceof DicomAssocationRejection) {
            Log.i(TAG, "DICOM Association was rejected.");
            //TODO!+ Inform user that DICOM Association was rejected.
        } else if (res instanceof DicomAssociationAbort) {
            Log.i(TAG, "DICOM ASsociation was aborted.");
            //TODO!+
        } else if (res instanceof DicomAssociation) {
            Log.i(TAG, "DICOM Association successful!");

            List<DicomElement> echoRequest = RequestFactory.createEchoRequest(); //TODO?~ Should this be parameterized with the DICOM Assocation, or at least its Transfer Syntax?
            byte[] echoRequestBytes = Converter.binaryRepresentation(echoRequest);
            try {
                byte[] echoResponseBytes = networkConnection.sendEchoRequestBytes(echoRequestBytes);

                Toolbox.logBytes(echoResponseBytes);
                //TODO!+ Interpret the response bytes...
                //TODO!+ After the Echo Request is processed, RELEASE the DICOM Association.
            } catch (IOException exc) {
                Log.e(TAG, "An error occurred while sending the C-Echo-Rq.");
                //TODO?~ It can be 3 errors really: SocketException, UnknownHostException, and their generic parent IOException. Give user more detail?
                Log.e(TAG, exc.toString());
            }
        } else {
            Log.e(TAG, "Attempted DICOM Association resulted in unhandled case: " + res.getClass().getCanonicalName());
            //TODO!+ Error!!
        }

        return;
    }
}


interface EchoRequestCallback {
    //TODO?~ Should it be "onComplete(DicomAssiciationRequestResult)" ?
    void onComplete(NetworkingFailure result);
}
