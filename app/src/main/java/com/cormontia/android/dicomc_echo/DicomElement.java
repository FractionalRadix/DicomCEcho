package com.cormontia.android.dicomc_echo;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

//TODO!+ In general, determine if we store Dicom Elements in Little Endian or Big Endian internally.

public class DicomElement
{
    private static final String TAG = "DICOMElement";

    private DicomTag dicomTag;
    private DicomVR.VR valueRepresentation; //Null for implicit?
    private byte[] contents;
    // Length of the Dicom Element is a derived value.

    DicomElement( DicomTag dicomTag, DicomVR.VR valueRepresentation, byte[] contents )
    {
        //TODO!+ Add a check that the length corresponds to that of the given VR

        this.dicomTag = dicomTag;
        this.valueRepresentation = valueRepresentation;
        this.contents = new byte[ contents.length ];
        System.arraycopy(contents, 0, this.contents, 0, contents.length);
    }

    //TODO?-
    // Assumes the contents is already in the right endian-ness.
    byte[] littleEndianRepresentation( )
    {
        // Length of the resulting Dicom Element:
        //  8 bytes for the tag (4 bytes for group id, 4 bytes for element id)
        //  4 (?) bytes for the length
        //  4 (?) bytes for the Value Representation, unless it's implicitVR //TODO!~

        byte[] groupBytes = EndianConverter.littleEndian( dicomTag.getGroup( ) );
        byte[] elementBytes = EndianConverter.littleEndian( dicomTag.getElement( ) );

        //TODO?~ Content must always be of even length, mustn't it?
        byte[] lengthBytes = EndianConverter.littleEndian( (int) ( contents.length /* + 4 when using Explicit VR ? */ ) ) ;
        byte[] contentBytes = this.contents;

        Log.i(TAG, "Lengths of DICOM Element components: " + groupBytes.length + ", " + elementBytes.length + ", " + lengthBytes.length + ", " + contentBytes.length );
        byte[] res = ByteArrayHelper.appendByteArrays( groupBytes, elementBytes, lengthBytes, contentBytes );
        Log.i(TAG, "Length of DICOM Element components combined: " + res.length);
        return res;
    }

    public String humanReadableForm( ) {

        String tagString = String.format("(%04X,%04X)",dicomTag.getGroup(),dicomTag.getElement());
        //String vrString = DicomVR.VR.toString();
        StringBuffer contentStr = new StringBuffer();
        for (int i = 0; i < contents.length; i++) {
            byte cur = contents[i];
            if (cur >= 32 && cur <= 127) {
                contentStr.append((char) cur);
            } else {
                contentStr.append('.');
            }
        }

        return tagString + " [" + contentStr + "]";
    }
}
