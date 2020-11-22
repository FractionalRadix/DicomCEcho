
package com.cormontia.android.dicomc_echo;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for creating DICOM Associations.
 */
public class Associator {

    static void sendAAssociateRQ(String callingAETitle, String calledAETitle, String host, int port, PresentationContext... presentationContexts) {
        //TODO!+
        // 1. Send the request to the called AE.
        // 2. Await the result.
        // 3. The result can be: timeout, A-Associate-AC, A-Associate-RJ. Maybe also A-Associate-ABORT, need to figure that one out.
        //    Interpret the result.
        // 4. Act corresponding to the result.
        //    A-Associate-AC means the Assocation is established and can be used. Return the assocation.
        //    The others mean that for, whatever reason, the Assocation is not established. Inform the user.
    }
}

abstract class AssociationElement {
    protected byte[] getBytes(byte firstByte, String uid) {
        // This code is based upon the information in Pianykh, p183 and further.
        // It first describes Abstract Syntaxes in DICOM Assocations, on p183.
        // It then explains that What applies to these (regarding length fields and byte ordering), also applies to Transfer Syntaxes and Application Contexts.

        // Note: in this case, the length of the UID does not have to be an even number of bytes. (Pianykh, p183).
        int len = uid.length();
        byte[] res = new byte[4 + len];
        res[0] = firstByte;
        res[1] = 0x00; // Reserved
        // "The last (fourth) field contains the L bytes of the Abstract Syntax name" (Pianykh, p183).
        res[2] = (byte) ((len & 0xFF00) >> 8);
        res[3] = (byte) (len & 0x00FF);
        for (int i = 0; i < len; i++ ){
            res[i + 3] = (byte) uid.charAt(i);
        }
        return res;
    }
}

class AbstractSyntax extends AssociationElement {

    private final String uid;

    AbstractSyntax(@NonNull String uid) {
        this.uid = uid;
    }

    public byte[] getBytes( ) {
        return getBytes((byte) 0x30 /* Abstract Syntax */, uid);
    }
}

class TransferSyntax extends AssociationElement {

    private final String uid;

    TransferSyntax(@NonNull String uid) {
        this.uid = uid;
    }

    public byte[] getBytes( ) {
        return getBytes((byte) 0x40 /* Transfer Syntax */, uid);
    }
}

class ApplicationContext extends AssociationElement {
    private final String uid;

    ApplicationContext(String uid) {
        if (uid == null) {
            this.uid = DicomUIDs.dicomApplicationContextName;
        } else {
            this.uid = uid;
        }
    }

    public byte[] getBytes( ) {
        return getBytes((byte) 0x10 /* Transfer Syntax*/, uid);
    }
}

class PresentationContext {

    private final byte presentationContextID;
    private final AbstractSyntax abstractSyntax;
    private final TransferSyntax[] transferSyntaxes;

    PresentationContext(byte presentationContextID, AbstractSyntax abstractSyntaxName, TransferSyntax... transferSyntaxNames) {
        this.presentationContextID = presentationContextID;
        this.abstractSyntax = abstractSyntaxName; //TODO?~ deep copy?
        //TODO?~ "Inspect Code" warns about this manual array copy.
        this.transferSyntaxes = new TransferSyntax[ transferSyntaxNames.length];
        for (int i = 0; i < transferSyntaxNames.length; i++) {
            this.transferSyntaxes[i] = transferSyntaxNames[i]; //TODO?~ deep copy?
        }
    }

    /**
     * Calculate the byte representation of the Presentation Context.
     * @param isAssociateRQ Set to <code>true</code> if the Presentation Context is to be part of an A-Associate-RQ, to <code>false</code> if it is to be part of an A-Associate-AC.
     * @return The byte representation of the given Presentation Context.
     */
    public byte[] getBytes(boolean isAssociateRQ) {

        int len = 8; // The number of bytes that we will need. It's 8 + length of Abstract Syntax + cumulative length of Transfer Syntaxes.

        // Determine the byte representation of the Abstract Syntax.
        // Add it's length to the total number of bytes that we will need.
        byte[] abstractSyntaxBytes = abstractSyntax.getBytes();
        len += abstractSyntaxBytes.length;

        // Determine the byte representation of all Transfer Syntaxes.
        // Keep track of their cumulative length.
        List<byte[]> allTransferSyntaxBytes = new ArrayList<>();
        for (TransferSyntax transferSyntax : transferSyntaxes) {
            byte[] currentTransferSyntaxBytes = transferSyntax.getBytes();
            allTransferSyntaxBytes.add(currentTransferSyntaxBytes);
            len += currentTransferSyntaxBytes.length;
        }

        // With these preparations, we can start building the Presentation Context.
        // First, the 8 bytes that define it as a Presentation Context.
        byte[] res = new byte[len];

        res[0] = (byte) (isAssociateRQ ? 0x20 : 0x21); // 0x20h must be used when the Presentation Context is part of an A-Associate-RQ, 0x21h must be used if it is part of an A-Associate-AC.
        res[1] = 0x00;
        res[2] = (byte) ((len & 0xFF00) >> 8);
        res[3] = (byte) (len & 0x00FF);
        res[4] = presentationContextID;
        res[5] = 0x00;
        res[6] = 0x00;
        res[7] = 0x00;

        // Second, the byte representation of the Abstract Syntax.
        for (int i = 0; i < abstractSyntaxBytes.length; i++) {
            res[8 + i] = abstractSyntaxBytes[i];
        }

        // Third and last, the byte representation of the Transfer Syntaxes.
        int offset = 8 + abstractSyntaxBytes.length;
        for (byte[] currentTransferSyntaxBytes : allTransferSyntaxBytes) {

            for (int i = 0; i < currentTransferSyntaxBytes.length; i++) {
                res[offset + i] = currentTransferSyntaxBytes[i];
            }
            offset += currentTransferSyntaxBytes.length;
        }

        return res;
    }
}

class UserInformation {
    //TODO!+

    /** Maximum PDU length
     * Defaults to 0 because that indicates that no maximum length is specified.
     *  "The value of (0) indicates that no maximum length is specified."
     *  Source: http://dicom.nema.org/medical/dicom/current/output/chtml/part08/chapter_D.html
     */
    private int maxPDULength = 0; //
    public void setMaxPDULength(int maxPDULength) { this.maxPDULength = maxPDULength; }

    private final String implementationClassUID = DicomUIDs.dicomApplicationContextName; //TODO?~ Is it acceptable to use this one here?
    private final String implementationVersionName = null;

    UserInformation( ) {
        //TODO!+
    }

    byte[] getBytes() {
        int len = 2000; //TODO!~
        byte[] res = new byte[len];

        res[0] = 0x50;
        res[1] = 0x00;
        res[2] = (byte) ((len & 0xFF00) >> 8);
        res[3] = (byte) (len & 0x00FF);

        //TODO!+ Add the sub items

        // Maximum PDU length
        // DICOM definition: http://dicom.nema.org/medical/dicom/current/output/chtml/part08/chapter_D.html
        res[ 4] = 0x51;
        res[ 5] = 0x00;
        res[ 6] = (byte) 0x00; // Item length, fixed at 4; high byte.
        res[ 7] = (byte) 0x04; // Item length, fixed at 4; low byte.
        res[ 8] = (byte) ((maxPDULength & 0xFF000000) >> 24);
        res[ 9] = (byte) ((maxPDULength & 0x00FF0000) >> 16);
        res[10] = (byte) ((maxPDULength & 0x0000FF00) >>  8);
        res[11] = (byte) ((maxPDULength & 0x000000FF));

        // Implementation identification
        // DICOM Definition: http://dicom.nema.org/medical/dicom/current/output/chtml/part07/sect_D.3.3.2.html
        int uidLen = implementationClassUID.length();
        res[12] = 0x52;
        res[13] = 0x00;
        res[14] = (byte) ((uidLen & 0x0000FF00) >>  8); // Item length, high byte.
        res[15] = (byte) ((uidLen & 0x00000FF));        // Item length, low byte.
        for (int i = 0; i < implementationClassUID.length(); i++) {
            res[16 + i] = (byte) implementationClassUID.charAt(i);
        }

        int offset = 16 + uidLen;
        // Optional part: version name
        if (this.implementationVersionName != null) {
            int versionLen = implementationVersionName.length();
            res[offset  ] = 0x55;
            res[offset+1] = 0x00;
            res[offset+2] = (byte) ((versionLen & 0x0000FF00) >>  8); // Item length, high byte.
            res[offset+3] = (byte) ((versionLen & 0x00000FF));        // Item length, low byte.
            offset += 4;
            for (int i = 0; i < implementationVersionName.length(); i++) {
                res[offset + i] = (byte) implementationVersionName.charAt(i);
            }
        }

        // Asynchronous operations
        //TODO!+

        // SCP/SCU role
        //TODO!+

        // Extended negotiation
        //TODO!+

        return res;
    }
}
