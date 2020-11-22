package com.cormontia.android.dicomc_echo;

import java.util.ArrayList;
import java.util.List;

public class RequestFactory
{
    /** Generate a DICOM C-ECHO Request.
     * @return An ordered list of Dicom Elements, that together gives all the bytes for a DICOM C-ECHO request (C-ECHO-Req).
     */
    public static List<DicomElement> createEchoRequest()
    {
        List<DicomElement> res = new ArrayList<>();

        // Command Group Length
        res.add(new DicomElement(
                new DicomTag( (short) 0x0000, (short) 0x0000 ),
                DicomVR.VR.UL,
                //4,
                new byte[] { 56, 0, 0, 0 }
        ));

        // Affected Service Class UID
        res.add(new DicomElement(
                new DicomTag( (short) 0x0000, (short) 0x0002 ),
                DicomVR.VR.UI,
                //18,
                DicomUIDs.byteArrayRepresentation( DicomUIDs.verificationSOPClass)
        ));

        // Command Field
        res.add(new DicomElement(
                new DicomTag( (short) 0x0000, (short) 0x0100 ),
                DicomVR.VR.US,
                EndianConverter.littleEndian( (short) 0x0030 )
                //new byte[] { 0x03, 0x00, 0x00, 0x00 }
        ));

        // Message ID
        res.add(new DicomElement(
                new DicomTag( (short) 0x0000, (short) 0x0110 ),
                DicomVR.VR.US,
                new byte[] {(byte) 0xCA, (byte) 0xFE } //TODO!+ Randomize....
        ));

        // Data Set Type
        res.add(new DicomElement(
                new DicomTag( (short) 0x0000, (short) 0x0800 ),
                DicomVR.VR.US,
                new byte[] { 0x01, 0x01 }
        ));

        return res;
    }
}
