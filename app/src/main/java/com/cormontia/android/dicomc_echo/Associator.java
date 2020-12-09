
package com.cormontia.android.dicomc_echo;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for creating DICOM Associations.
 */
public class Associator {

    private final static String TAG = "DICOM Associator";

    static PresentationContext presentationContextForEcho() {
        //TODO!~ Parameterize with proper defaults. (User's choice is:  which of 3 transfer syntaxes...)
        byte presentationContextID = 0x42; //TODO?~ Arbitrary number.
        AbstractSyntax abstractSyntax = new AbstractSyntax(DicomUIDs.verificationSOPClass);
        TransferSyntax transferSyntax1 = new TransferSyntax(DicomUIDs.implicitVRLittleEndian);
        TransferSyntax transferSyntax2 = new TransferSyntax(DicomUIDs.explicitVRLittleEndian);
        return new PresentationContext(presentationContextID, abstractSyntax, transferSyntax1 /*, transferSyntax2 */);
    }

    static DicomAssociationRequestResult openDicomAssociation(String callingAETitle, String calledAETitle, String host, int port, PresentationContext... presentationContexts) {

        //TODO!+ Add a field to the Android Layout XML where the user can optionally specify a "Called AE" name.
        // ...because some DICOM hosts use a whitelist that only checks for the AE Title...

        // Calculate the bytes for the A-Associate-RQ, and send them to the called AE.
        List<Byte> AAssociateRQ = calculateAAsociateRQBytes(callingAETitle, calledAETitle, presentationContexts);

        // Send the A-Associate-RQ to the server.
        byte[] requestBytes = Converter.listToArray(AAssociateRQ);
        try {
            byte[] responseBytes = Networking.sendAndReceive(host, port, requestBytes);
            return interpretAssociationResponse(responseBytes);
        }
        catch (SocketException exc) {
            //TODO!~ more end-user friendly error message
            Log.e(TAG, exc.toString());
            return new NetworkingFailure(NetworkingFailure.Status.Failure, "Socket Exception while trying to receive server response.", null);
        }
        catch (UnknownHostException exc) {
            //TODO!~ more end-user friendly error message
            Log.e(TAG, exc.toString());
            return new NetworkingFailure(NetworkingFailure.Status.Failure, "Unknown host Exception while trying to receive server response.", null);
        }
        catch (IOException exc) {
            //TODO!~ more end-user friendly error message
            Log.e(TAG, exc.toString());
            return new NetworkingFailure(NetworkingFailure.Status.Failure, "I/O Exception while trying to receive server response.", null);
        }
    }

    private static DicomAssociationRequestResult interpretAssociationResponse(@NonNull byte[] bytes) {

        if (bytes.length == 0) {
            //TODO!+ Error.
            return null;
        }

        byte type = bytes[0];
        switch (type) {
            case 0x02:
                //TODO!+ Process A-Associate-AC.
                // Maybe this section should be in a separate method.
                // Parse the Abstract Syntax and the selected Transfer Syntax.
                //TODO!+ Verify that we have at least 6 bytes in the response.
                int lenHighestByte       = ((int) bytes[2]) << 24;
                int lenSecondHighestByte = ((int) bytes[3]) << 16;
                int lenThirdHighestByte  = ((int) bytes[4]) <<  8;
                int lenLowestByte        = (int) bytes[5];


                Log.i(TAG, "A-Associate-AC. Bytes: "+lenHighestByte + ", " + lenSecondHighestByte + ", " + lenThirdHighestByte + ", " + lenLowestByte);
                int len = lenHighestByte + lenSecondHighestByte + lenThirdHighestByte + lenLowestByte;
                Log.i(TAG, "Total length: " + len);

                //TODO!+ Verify that the bytes.length == 6 + len

                int protocolVersion = (((int) bytes[6]) << 8) + ((int) bytes[7]);

                // Bytes 8 and 9 should be set to 0.
                // Bytes 10-25 are the Called AE Title, but "should not be tested".
                StringBuffer calledAETitle = new StringBuffer("");
                for (int i = 0; i < 16; i++) {
                    calledAETitle.append(bytes[i+10]);
                }
                //TODO!+ Make sure that AE Titles cannot begin with whitespace... if they can, I need to only trim the TRAILING characteres.
                String strCalledAETitle = calledAETitle.toString().trim();

                Log.i(TAG, "Called AE Title: " + calledAETitle);

                // Bytes 26-41
                StringBuffer callingAETitle = new StringBuffer("");
                for (int i = 0; i < 16; i++) {
                    callingAETitle.append(bytes[i+26]);
                }
                Log.i(TAG, "Calling AE Title: " + callingAETitle);
                String strCallingAETitle = callingAETitle.toString().trim();

                //TODO!+ Skip 32 bytes containing only 0x00

                //TODO!+ Parse the Application Context Item
                //TODO!+ Parse the Presentation Context Items.
                //  Note that there can be MULTIPLE Presentation Context Items. But each should contain only ONE Abstract Syntax (well duh) and only ONE Transfer Syntax.
                PresentationContext presentationContext = null; //TODO!+
                //TODO!+ Parse the User Information Item.

                return new DicomAssociation(strCalledAETitle, strCallingAETitle, presentationContext);

                //break;
            case 0x03:
                //TODO!+ Process A-Associate-RJ
                //TODO!+ Test that the length of the message is 10.
                //TODO!+ Test that the bytes 2-5 (inclusive) form "00 00 00 04"
                byte result = bytes[7];
                byte source = bytes[8];
                byte reason = bytes[9];
                return new DicomAssocationRejection(result, source, reason);
                //break;
            case 0x07:
                //TODO!+ Process A-Abort
                return new DicomAssociationAbort();
                //break;
            default:
                //TODO!+ Things went thoroughly wrong here, process that...
                return null; //TODO?~
                //break;
        }
    }

    private static List<Byte> calculateAAsociateRQBytes(String callingAETitle, String calledAETitle, PresentationContext[] presentationContexts) {
        String applicationContextUID = null; //TODO?~
        List<Byte> applicationContextItemBytes = new ApplicationContext(applicationContextUID).getBytes();

        List<Byte> allPresentationContextsBytes = new ArrayList<>();
        for (PresentationContext presentationContext : presentationContexts) {
            allPresentationContextsBytes.addAll(presentationContext.getBytes(true));
        }

        List<Byte> userInformationBytes = new UserInformation().getBytes();

        int len = 2 + 2 + 16 + 16 + 32 + applicationContextItemBytes.size() + allPresentationContextsBytes.size() + userInformationBytes.size();

        // Source: http://dicom.nema.org/medical/dicom/current/output/chtml/part08/sect_9.3.2.html
        List<Byte> AAssociateRQ = new ArrayList<>();
        // Byte 0: PDU type
        AAssociateRQ.add((byte) 0x01);
        // Byte 1: Reserved
        AAssociateRQ.add((byte) 0x00);
        // Bytes 3-6: PDU length
        AAssociateRQ.add((byte) ((len & 0xFF000000) >> 24));
        AAssociateRQ.add((byte) ((len & 0x00FF0000) >> 16));
        AAssociateRQ.add((byte) ((len & 0x0000FF00) >>  8));
        AAssociateRQ.add((byte) ((len & 0x000000FF)      ));
        // Bytes 7-8: Protocol version
        AAssociateRQ.add((byte) 0x00); // Protocol version, high byte
        AAssociateRQ.add((byte) 0x01); // Protocol version, low byte
        // Bytes 9-10: Reserved
        AAssociateRQ.add((byte) 0x00);
        AAssociateRQ.add((byte) 0x00);

        // Note: we may be able to use String.format for a more elegant way of doing the padding:
        //   return String.format("%1$" + length + "s", inputString).replace(' ', '0');
        //   Source: https://www.baeldung.com/java-pad-string

        // Bytes 11-26: Called AE Title
        String modifiedCalledAETitle = calledAETitle;
        if (modifiedCalledAETitle.length() > 16) {
            modifiedCalledAETitle = modifiedCalledAETitle.substring(0, 16);
        }
        for (int i = modifiedCalledAETitle.length(); i < 16; i++) {
            modifiedCalledAETitle += ' ';
        }
        AAssociateRQ = Toolbox.addString(AAssociateRQ, modifiedCalledAETitle);

        // Bytes 27-42: Caling AE Title
        String modifiedCallingAETitle = callingAETitle;
        if (modifiedCallingAETitle.length() > 16) {
            modifiedCallingAETitle = modifiedCallingAETitle.substring(0,16);
        }
        for (int i = modifiedCallingAETitle.length(); i < 16; i++ ) {
            modifiedCallingAETitle += ' ';
        }
        AAssociateRQ = Toolbox.addString(AAssociateRQ, modifiedCallingAETitle);

        // Bytes 43-74: Reserved
        for (int i = 43; i <= 74; i++) {
            AAssociateRQ.add((byte) 0);
        }

        // Bytes 75-xxx: Variable items.
        // "This variable field shall contain the following items: one Application Context Item, one or more Presentation Context Items and one User Information Item."
        AAssociateRQ.addAll(applicationContextItemBytes);
        AAssociateRQ.addAll(allPresentationContextsBytes);
        AAssociateRQ.addAll(userInformationBytes);
        return AAssociateRQ;
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
    public List<Byte> getBytes(boolean isAssociateRQ) {

        int len = 4; // The number of bytes that we will need. It's 4 + length of Abstract Syntax + cumulative length of Transfer Syntaxes.

        Log.i("PR.CTX. Len", "len=="+len);

        // Determine the byte representation of the Abstract Syntax.
        // Add it's length to the total number of bytes that we will need.
        List<Byte> abstractSyntaxBytes = abstractSyntax.getBytes();
        len += abstractSyntaxBytes.size();
        Log.i("PR.CTX. Len",  "Abstract syntax bytes length=="+abstractSyntaxBytes.size());
        Log.i("PR.CTX. Len", "len=="+len);

        // Determine the byte representation of all Transfer Syntaxes.
        // Keep track of their cumulative length.
        List<Byte> allTransferSyntaxBytes = new ArrayList<>();
        for (TransferSyntax transferSyntax : transferSyntaxes) {
            List<Byte> currentTransferSyntaxBytes = transferSyntax.getBytes();
            allTransferSyntaxBytes.addAll(currentTransferSyntaxBytes);
        }
        len += allTransferSyntaxBytes.size();
        Log.i("PR.CTX. Len",  "Transfer syntaxes bytes length=="+allTransferSyntaxBytes.size());
        Log.i("PR.CTX. Len", "len=="+len);

        // With these preparations, we can start building the Presentation Context.
        // First, the 8 bytes that define it as a Presentation Context.
        List<Byte> res = new ArrayList<>();

        res.add((byte) (isAssociateRQ ? 0x20 : 0x21)); // 0x20h must be used when the Presentation Context is part of an A-Associate-RQ, 0x21h must be used if it is part of an A-Associate-AC.
        res.add((byte) 0x00);
        res.add((byte) ((len & 0xFF00) >> 8));
        res.add((byte)  (len & 0x00FF)      );
        res.add(presentationContextID);
        res.add((byte) 0x00);
        res.add((byte) 0x00);
        res.add((byte) 0x00);

        // Second, the byte representation of the Abstract Syntax.
        res.addAll(abstractSyntaxBytes);
        //TODO!- Added for debugging:
        StringBuffer hexBuf = new StringBuffer("");
        for (int i = 0; i < abstractSyntaxBytes.size(); i++) {
            hexBuf.append(String.format("%02x ", abstractSyntaxBytes.get(i)));
        }
        Log.i("ABSTR SYNTAX", hexBuf.toString());

        // Third and last, the byte representation of the Transfer Syntaxes.
        res.addAll(allTransferSyntaxBytes);
        hexBuf = new StringBuffer("");
        for (int i = 0; i < allTransferSyntaxBytes.size(); i++) {
            hexBuf.append(String.format("%02x ", allTransferSyntaxBytes.get(i)));
        }
        Log.i("TRANSF SYNTAXES", hexBuf.toString());
        return res;
    }
}

class UserInformation {
    /** Maximum PDU length
     * Defaults to 0 because that indicates that no maximum length is specified.
     *  "The value of (0) indicates that no maximum length is specified."
     *  Source: http://dicom.nema.org/medical/dicom/current/output/chtml/part08/chapter_D.html
     */
    private int maxPDULength = 0;
    public void setMaxPDULength(int maxPDULength) { this.maxPDULength = maxPDULength; }

    private final String implementationClassUID = DicomUIDs.dicomApplicationContextName; //TODO?~ Is it acceptable to use this one here?
    private final String implementationVersionName = null;

    UserInformation( ) {
        //TODO!+
    }

    List<Byte> getBytes() {

        // Maximum PDU length
        // DICOM definition: http://dicom.nema.org/medical/dicom/current/output/chtml/part08/chapter_D.html
        List<Byte> maxPDULengthBytes = determineMaxPDULengthBytes();

        // Implementation identification
        // DICOM Definition: http://dicom.nema.org/medical/dicom/current/output/chtml/part07/sect_D.3.3.2.html
        List<Byte> implementationIdentificationBytes = determineImplementationIdentificationBytes();

        // Asynchronous operations
        int maxNumberOperationsInvoked = 1; //TODO?~ Parameterize
        int maxNumberOperationsPerformed = 1; //TODO?~ Parameterize
        List<Byte> asynchronousOperationBytes = determineAsynchronousOperationBytes(maxNumberOperationsInvoked, maxNumberOperationsPerformed);

        // SCP/SCU role
        List<Byte> scuScpRoleBytes = determineScuScpRoleBytes();
        //TODO!+

        // Extended negotiation
        List<Byte> extendedNegotiationBytes = determineExtendedNegotiationBytes();

        // And finally, the first four bytes.
        // These are calculated last, because they contain the length of the rest of the item....
        List<Byte> headerBytes = new ArrayList<Byte>();
        int lengthWithoutHeader = maxPDULengthBytes.size()
                + implementationIdentificationBytes.size()
                + asynchronousOperationBytes.size()
                + scuScpRoleBytes.size()
                + extendedNegotiationBytes.size();

        headerBytes.add((byte) 0x50);
        headerBytes.add((byte) 0x00);
        headerBytes.add((byte) ((lengthWithoutHeader & 0xFF00) >> 8));
        headerBytes.add((byte) ((lengthWithoutHeader & 0x00FF)     ));

        // With all the bytes calculated, we can now turn it into an array.
        List<Byte> masterList = new ArrayList<>();
        masterList.addAll(headerBytes);
        masterList.addAll(maxPDULengthBytes);
        masterList.addAll(implementationIdentificationBytes);
        masterList.addAll(asynchronousOperationBytes);
        masterList.addAll(scuScpRoleBytes);
        masterList.addAll(extendedNegotiationBytes);

        return masterList;
    }

    private List<Byte> determineMaxPDULengthBytes( ) {
        // Maximum PDU length
        // DICOM definition: http://dicom.nema.org/medical/dicom/current/output/chtml/part08/chapter_D.html
        List<Byte> res = new ArrayList<>();
        res.add((byte) 0x51);
        res.add((byte) 0x00);
        res.add((byte) 0x00); // Item length, fixed at 4; high byte.
        res.add((byte) 0x04); // Item length, fixed at 4; low byte.
        res.add((byte) ((maxPDULength & 0xFF000000) >> 24));
        res.add((byte) ((maxPDULength & 0x00FF0000) >> 16));
        res.add((byte) ((maxPDULength & 0x0000FF00) >>  8));
        res.add((byte) ((maxPDULength & 0x000000FF))      );
        return res;
    }

    private List<Byte> determineImplementationIdentificationBytes() {
        // Implementation identification
        // DICOM Definition: http://dicom.nema.org/medical/dicom/current/output/chtml/part07/sect_D.3.3.2.html
        List<Byte> res = new ArrayList<>();
        int uidLen = implementationClassUID.length();
        res.add((byte)0x52);
        res.add((byte)0x00);
        res.add((byte) ((uidLen & 0x0000FF00) >>  8)); // Item length, high byte.
        res.add((byte) ((uidLen & 0x00000FF))       );        // Item length, low byte.
        for (int i = 0; i < implementationClassUID.length(); i++) {
            res.add((byte) implementationClassUID.charAt(i));
        }

        // Optional part: version name
        if (this.implementationVersionName != null) {
            int versionLen = implementationVersionName.length();
            res.add((byte) 0x55);
            res.add((byte) 0x00);
            res.add((byte) ((versionLen & 0x0000FF00) >>  8)); // Item length, high byte.
            res.add((byte) ((versionLen & 0x000000FF))      ); // Item length, low byte.
            for (int i = 0; i < implementationVersionName.length(); i++) {
                res.add((byte) implementationVersionName.charAt(i));
            }
        }

        return res;
    }

    private List<Byte> determineAsynchronousOperationBytes(int maxNumberOperationsInvoked, int maxNumberOperationsPerformed) {
        List<Byte> res = new ArrayList<>();

        res.add((byte) 0x53);
        res.add((byte) 0x00);
        res.add((byte) 0x00); // Item length, fixed at 4; high byte.
        res.add((byte) 0x04); // Item length, fixed at 4; low byte.
        res.add((byte) ((maxNumberOperationsInvoked   & 0x0000FF00) >>  8)); // Item length, high byte.
        res.add((byte) ((maxNumberOperationsInvoked   & 0x00000FF))       ); // Item length, low byte.
        res.add((byte) ((maxNumberOperationsPerformed & 0x0000FF00) >>  8)); // Item length, high byte.
        res.add((byte) ((maxNumberOperationsPerformed & 0x00000FF))       ); // Item length, low byte.
        
        return res;
    }


    // "This Sub-Item is optional and if supported, one or more SCP/SCU Role Selection Sub-Items may be present in the User Data Item of the A-ASSOCIATE-RQ."
    // Source: http://dicom.nema.org/medical/dicom/current/output/chtml/part07/sect_D.3.3.4.html
    // So... when we get to implementing this one, we need to do it in a loop.... for now, make it empty.
    // Since this one may be parameterized, we should perhaps let every call create ONE so-called sub-item. And then allow multiple calls.
    private List<Byte> determineScuScpRoleBytes() {
        List<Byte> res = new ArrayList<>();

        //TODO!+

        return res;
    }

    // "The SOP Class Extended Negotiation allows, at Association establishment, peer DICOM AEs to exchange application information defined by specific Service Class specifications.
    //  This is an optional feature that various Service Classes may or may not choose to support."
    // Source: http://dicom.nema.org/medical/dicom/current/output/chtml/part07/sect_D.3.3.5.html
    // So.. it seems that this one, too, is a 0-or-more-iterations thing. Again, for now, make it empty.
    // Since this one may be parameterized, we should perhaps let every call create ONE so-called sub-item. And then allow multiple calls.
    private List<Byte> determineExtendedNegotiationBytes() {
        List<Byte> res = new ArrayList<>();

        //TODO!+

        return res;
    }

}
