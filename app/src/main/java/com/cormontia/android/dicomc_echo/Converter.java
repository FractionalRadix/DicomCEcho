package com.cormontia.android.dicomc_echo;

import java.util.List;

public class Converter {
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

    /**
     * Converting byte values to their hexadecimal representations.
     * We used Integer.toHexString first, but when provided with byte values of 128 or higher, it interpreted them as negative values.
     * @param input A single byte.
     * @return Hexadecimal representation of the byte, unsigned.
     */
    public static String byteToHexString( byte input )
    {
        char[] hexDigits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

        int nibble0 = input & 0x0F;
        int nibble1 = ( input & 0xF0 ) >> 4;
        char ch0 = hexDigits[ nibble0 ];
        char ch1 = hexDigits[ nibble1 ];

        return new String( new char[] { ch1, ch0 } );
    }
}
