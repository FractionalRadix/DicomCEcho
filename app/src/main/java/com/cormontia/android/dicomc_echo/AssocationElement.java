package com.cormontia.android.dicomc_echo;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Base Class for Abstract Syntaxes, Transfer Syntaxes, and Application Contexts.
 */
abstract class AssociationElement {
    protected List<Byte> getBytes(byte itemType, String uid) {
        // This code is based upon the information in Pianykh, p183 and further.
        // It first describes Abstract Syntaxes in DICOM Assocations, on p183.
        // It then explains that what applies to these (regarding length fields and byte ordering), also applies to Transfer Syntaxes and Application Contexts.

        // Note: in this case, the length of the UID does not have to be an even number of bytes. (Pianykh, p183).
        int len = uid.length();

        List<Byte> res = new ArrayList<>();
        res.add(itemType);
        res.add((byte) 0x00); // Reserved
        // "The last (fourth) field contains the L bytes of the Abstract Syntax name" (Pianykh, p183).
        res.add((byte) ((len & 0xFF00) >> 8));
        res.add((byte)  (len & 0x00FF)      );
        for (int i = 0; i < len; i++ ) {
            res.add((byte) uid.charAt(i));
        }
        return res;
    }
}

class AbstractSyntax extends AssociationElement {

    private final String uid;

    AbstractSyntax(@NonNull String uid) {
        this.uid = uid;
    }

    public List<Byte> getBytes( ) {
        return getBytes((byte) 0x30 /* Abstract Syntax */, uid);
    }
}

class TransferSyntax extends AssociationElement {

    private final String uid;

    TransferSyntax(@NonNull String uid) {
        this.uid = uid;
    }

    public List<Byte> getBytes( ) {
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

    public List<Byte> getBytes( ) {
        return getBytes((byte) 0x10 /* Transfer Syntax*/, uid);
    }
}
