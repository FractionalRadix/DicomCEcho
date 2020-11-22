
package com.cormontia.android.dicomc_echo;

import androidx.annotation.NonNull;

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

class AbstractSyntax {

    private String uid;

    AbstractSyntax(@NonNull String uid) {
        this.uid = uid;
    }

    public byte[] getBytes( ) {
        // Note: in this case, the length of the UID does not have to be an even number of bytes. (Pianykh, p183).
        int len = uid.length();
        byte[] res = new byte[4 + len];
        res[0] = 0x30; // Abstract Syntax
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

class TransferSyntax {
    private String uid;

    TransferSyntax(@NonNull String uid) {
        this.uid = uid;
    }

    public byte[] getBytes( ) {
        // Note: Transfer syntaxes differ from Abstract Syntaxes only in the first byte (0x40 instead of 0x30). (Pianykh, p185).
        // So what's written in Pianykh's book about Abstract Syntaxes on p183, also applies to Transfer Syntaxes.
        // Note: in this case, the length of the UID does not have to be an even number of bytes. (Pianykh, p183).
        int len = uid.length();
        byte[] res = new byte[4 + len];
        res[0] = 0x40; // Transfer Syntax
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

class ApplicationContext {
    private String uid;

    ApplicationContext(String uid) {
        if (uid == null) {
            this.uid = DicomUIDs.dicomApplicationContextName;
        } else {
            this.uid = uid;
        }
    }
}

class PresentationContext {

    final String presentationContextID;
    final AbstractSyntax abstractSyntax;
    final TransferSyntax[] transferSyntaxes;

    PresentationContext(String presentationContextID, AbstractSyntax abstractSyntaxName, TransferSyntax... transferSyntaxNames) {
        this.presentationContextID = presentationContextID;
        this.abstractSyntax = abstractSyntaxName; //TODO?~ deep copy?
        //TODO?~ "Inspect Code" warns about this manual array copy. 
        this.transferSyntaxes = new TransferSyntax[ transferSyntaxNames.length];
        for (int i = 0; i < transferSyntaxNames.length; i++) {
            this.transferSyntaxes[i] = transferSyntaxNames[i]; //TODO?~ deep copy?
        }
    }
}
