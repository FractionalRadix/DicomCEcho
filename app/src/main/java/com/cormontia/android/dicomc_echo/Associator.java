
package com.cormontia.android.dicomc_echo;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
        return new PresentationContext(presentationContextID, abstractSyntax, transferSyntax1, transferSyntax2);
    }

    static EchoResult openDicomAssociation(String callingAETitle, String calledAETitle, String host, int port, PresentationContext... presentationContexts) {

        //TODO!+ Add a field to the XML where the user can optionally specify a "Called AE" name.
        // ...because some DICOM hosts use a whitelist that only checks for the AE Title...

        // 1. Calculate the bytes for the A-Associate-RQ, and send them to the called AE.
        List<Byte> AAssociateRQ = calculateAAsociateRQBytes(callingAETitle, calledAETitle, presentationContexts);

        byte[] requestBytes = listToArray(AAssociateRQ);
        try {
            Socket socket = new Socket(host, port);
            socket.setSoTimeout(15000); // Timeout in milliseconds.

            // Try-with-resources requires API level 19, currently supported minimum is 14.
            OutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            InputStream bis = socket.getInputStream(); // new BufferedInputStream(socket.getInputStream());
            bos.write(requestBytes, 0, requestBytes.length);

            // Receive the response, hopefully an A-Associate-AC. Note that it can also be A-Associate-RQ or A-Associate-ABORT.
            Log.i(TAG, "Association request sent, awaiting response...");
            List<Byte> serverResponse = new ArrayList<>();
            try {
                //TODO!~ NAIVE SOLUTION: Just read all.

                // For debugging.
                StringBuffer strBytesInHex = new StringBuffer();
                int cnt = 0;

                int ch;
                Log.i(TAG, "Entering while loop...");
                do{
                    ch = bis.read();
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

                bis.close();
                bos.close(); //TODO?+ flush it as well? IIRC close() automatically flushes.
                socket.close();

                return new EchoResult(EchoResult.Status.Success, "C-ECHO-Rsp received successfully!", responseBytes);

            } catch (IOException exc) {
                //TODO!~ more end-user friendly error message
                Log.e(TAG, exc.toString());
                return new EchoResult(EchoResult.Status.Failure, "I/O Exception while trying to receive server response.", null);
            }
        }
        catch (SocketException exc) {
            //TODO!~ more end-user friendly error message
            Log.e(TAG, exc.toString());
            return new EchoResult(EchoResult.Status.Failure, "Socket Exception while trying to receive server response.", null);
        }
        catch (UnknownHostException exc) {
            //TODO!~ more end-user friendly error message
            Log.e(TAG, exc.toString());
            return new EchoResult(EchoResult.Status.Failure, "Unknown host Exception while trying to receive server response.", null);
        }
        catch (IOException exc) {
            //TODO!~ more end-user friendly error message
            Log.e(TAG, exc.toString());
            return new EchoResult(EchoResult.Status.Failure, "I/O Exception while trying to receive server response.", null);
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

        List<Byte> AAssociateRQ = new ArrayList<>();
        AAssociateRQ.add((byte) 0x01);
        AAssociateRQ.add((byte) 0x00);
        AAssociateRQ.add((byte) ((len & 0xFF00) >> 8));
        AAssociateRQ.add((byte) ((len & 0x00FF)     ));
        AAssociateRQ.add((byte) 0x00); // Protocol version, high byte
        AAssociateRQ.add((byte) 0x00); // Protocol version, low byte
        AAssociateRQ.add((byte) 0x00);
        AAssociateRQ.add((byte) 0x00);

        // Note: we may be able to use String.format for a more elegant way of doing the padding:
        //   return String.format("%1$" + length + "s", inputString).replace(' ', '0');
        //   Source: https://www.baeldung.com/java-pad-string

        String modifiedCallingAETitle = callingAETitle;
        if (modifiedCallingAETitle.length() > 16) {
            modifiedCallingAETitle = modifiedCallingAETitle.substring(0,16);
        }
        for (int i = modifiedCallingAETitle.length(); i < 16; i++ ) {
            modifiedCallingAETitle += ' ';
        }
        AAssociateRQ = addString(AAssociateRQ, modifiedCallingAETitle);

        String modifiedCalledAETitle = calledAETitle;
        if (modifiedCalledAETitle.length() > 16) {
            modifiedCalledAETitle = modifiedCalledAETitle.substring(0, 16);
        }
        for (int i = modifiedCalledAETitle.length(); i < 16; i++) {
            modifiedCalledAETitle += ' ';
        }
        AAssociateRQ = addString(AAssociateRQ, modifiedCalledAETitle);

        AAssociateRQ.addAll(applicationContextItemBytes);
        AAssociateRQ.addAll(allPresentationContextsBytes);
        AAssociateRQ.addAll(userInformationBytes);
        return AAssociateRQ;
    }

    /** Primitive conversion of String to List&lt;Byte&gt; .
     * Does not support characters that won't fit in a byte.
     * @param list A List of bytes, to which the characters in the string will be appended.
     * @param str A string, whose individual characters need to be added to the given list.
     * @return The input string, with the bytes for the characters in the string appended at the end.
     */
    private static List<Byte> addString(List<Byte> list, String str) {
        for(char ch : str.toCharArray()) {
            list.add((byte) ch);
        }
        return list;
    }

    /** Primitive conversion of List&lt;Byte&gt; to byte[].
     * It is assumed that none of the Bytes in the list are <code>null</code>.
     * @param input A List of Byte values, where no Byte is <code>null</code>.
     * @return An array where every byte corresponds to the Byte value at the same position in the list.
     */
    private static byte[] listToArray(List<Byte> input) {
        int fullLength = input.size();
        byte[] res = new byte[fullLength];
        for (int i = 0; i < fullLength; i++) {
            res[i] = input.get(i);
        }
        return res;
    }

}

abstract class AssociationElement {
    protected List<Byte> getBytes(byte itemType, String uid) {
        // This code is based upon the information in Pianykh, p183 and further.
        // It first describes Abstract Syntaxes in DICOM Assocations, on p183.
        // It then explains that What applies to these (regarding length fields and byte ordering), also applies to Transfer Syntaxes and Application Contexts.

        // Note: in this case, the length of the UID does not have to be an even number of bytes. (Pianykh, p183).
        int len = uid.length();

        List<Byte> res = new ArrayList<>();
        res.add(itemType);
        res.add((byte) 0x00); // Reserved
        // "The last (fourth) field contains the L bytes of the Abstract Syntax name" (Pianykh, p183).
        res.add((byte) ((len & 0xFF00) >> 8));
        res.add((byte)  (len & 0x00FF)      );
        for (int i = 0; i < len; i++ ){
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

        int len = 8; // The number of bytes that we will need. It's 8 + length of Abstract Syntax + cumulative length of Transfer Syntaxes.

        // Determine the byte representation of the Abstract Syntax.
        // Add it's length to the total number of bytes that we will need.
        List<Byte> abstractSyntaxBytes = abstractSyntax.getBytes();
        len += abstractSyntaxBytes.size();

        // Determine the byte representation of all Transfer Syntaxes.
        // Keep track of their cumulative length.
        List<Byte> allTransferSyntaxBytes = new ArrayList<>();
        for (TransferSyntax transferSyntax : transferSyntaxes) {
            List<Byte> currentTransferSyntaxBytes = transferSyntax.getBytes();
            allTransferSyntaxBytes.addAll(currentTransferSyntaxBytes);
        }
        len += allTransferSyntaxBytes.size();

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

        // Third and last, the byte representation of the Transfer Syntaxes.
        res.addAll(allTransferSyntaxBytes);

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
            res.add((byte) ((versionLen & 0x00000FF))       ); // Item length, low byte.
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
