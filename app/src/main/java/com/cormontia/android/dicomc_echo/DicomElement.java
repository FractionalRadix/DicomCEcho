package com.cormontia.android.dicomc_echo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DicomElement
{
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

    //TODO!~ Move to a class for generic helper functions
    /**
     * Convert a List&lt;Byte&gt; to an array of byte.
     * PRE: None of the elements in the list is <code>null</code>.
     * @param bytes A list of zero or more Byte instances, to be converted into an array of byte.
     * @return An array of bytes, containing the same bytes as the input list, with the ordering preserved.
     */
    private static byte[] byteListToByteArray( List<Byte> bytes )
    {
        //TODO!+ Handle the NullReferenceException that you should get if one or more elements of the input array are NULL.
        byte[] res = new byte[ bytes.size( ) ];
        int i = 0;
        for ( Byte currentByte : bytes )
        {
            res[ i ] = currentByte;
            i++;
        }
        return res;
    }

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

        return ( ByteArrayHelper.appendByteArrays( groupBytes, elementBytes, lengthBytes, contentBytes ) );
    }



    //TODO!+


}
