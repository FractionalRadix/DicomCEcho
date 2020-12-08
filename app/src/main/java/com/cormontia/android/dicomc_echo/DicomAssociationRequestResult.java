package com.cormontia.android.dicomc_echo;

/**
 * Abstract base class for all possible results of a DICOM Association Request.
 * This includes the standard DICOM responses (A-Associate-AC, A-Associate-RJ, and A-Abort).
 * But it also includes server errors and I/O Exceptions.
 */
public abstract class DicomAssociationRequestResult {
}

/**
 * Holds the result of a successful DICOM Association Request.
 * In other words, an instance of this class contains all the parameters of a DICOM Association between two Application Entities.
 */
class DicomAssociation extends DicomAssociationRequestResult {
    String calledAETitle, callingAETitle;
    PresentationContext presentationContext;

    //TODO!+ Make sure that Presentation Context is not null.
    public DicomAssociation(String calledAETitle, String callingAETitle, PresentationContext presentationContext) {
        this.calledAETitle = calledAETitle;
        this.callingAETitle = callingAETitle;
        this.presentationContext = presentationContext;
    }

    /** Returns the Called Application Entity Title.
     * For a DICOM Echo SCU, this is the name of the Application Entity we're talking to.
     */
    public String getCalledAETitle() { return calledAETitle; }

    /** Returns the Calling Application Entity Title.
     * For a DICOM Echo SCU, this is our own name.
     */
    public String getCallingAETitle() { return callingAETitle; }

    //TODO!+ Methods for retrieving the Abstract Syntax's UID and Endianness...
}

/** Holds the data about the rejection of a DICOM Association request.
 * If we contact another DICOM Application Entity, and we fail to establish an Association, this class contains the data that the Application Party sent.
 * This can help to understand why the Association was Rejected.
 */
class DicomAssocationRejection extends DicomAssociationRequestResult {
    byte result, source, reason;

    public DicomAssocationRejection(byte result, byte source, byte reason) {
        this.result = result;
        this.source = source;
        this.reason = reason;
    }

    String getResult( ) {
        if (result == 1) {
            return "rejected-permanent";
        } else if (result == 2) {
            return "rejected-transient";
        } else {
            return "The byte with the rejection Result did not even contain a valid value.";
        }
    }

    //TODO!+ Add "String getSource()" and "String getReason()".
}

/**
 * If an attempt to establish a DICOM Association isn't just rejected, but outright aborted... this class contains what information the other party gave.
 */
class DicomAssociationAbort extends DicomAssociationRequestResult {
    //TODO!+
    DicomAssociationAbort() {
        //TODO!+
    }
}

/**
 * If an attempt to establish a DICOM Association failed at the network level, this class contains the Exception, as well as possible other information.
 */
class NetworkingFailure extends DicomAssociationRequestResult {
    public enum Status { Failure, Success };

    private NetworkingFailure.Status status;
    private String messageForUser;
    private byte[] serverResponse;

    NetworkingFailure(NetworkingFailure.Status status, String msg, byte[] serverResponse) {
        this.status = status;
        this.messageForUser = msg;
        this.serverResponse = serverResponse;
    }

    public String getMessage( ) {
        return messageForUser;
    }
}