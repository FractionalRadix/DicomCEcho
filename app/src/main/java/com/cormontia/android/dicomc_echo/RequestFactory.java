package com.cormontia.android.dicomc_echo;

import java.util.ArrayList;
import java.util.List;

public class RequestFactory
{
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
                DicomUIDs.byteArrayRepresentation( DicomUIDs.affectedServiceClassUID )
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

    /**
     * Calculates the binary representation of a list of DICOM elements.
     * In other words, given a list of elements, returns a byte array to represent these elements, in order.
     * @param elements A list of DICOM elements.
     * @return A single byte array, containing the binary (byte array) representation of the subsequent DICOM elements.
     */
    public static byte[] binaryRepresentation(List<DicomElement> elements)
    {
        byte[][] byteRepresentations = new byte[elements.size()][];
        for (int i = 0; i < elements.size(); i++)
        {
            byteRepresentations[i] = elements.get(i).littleEndianRepresentation();
        }
        return ( ByteArrayHelper.appendByteArrays(byteRepresentations) );
    }
}
