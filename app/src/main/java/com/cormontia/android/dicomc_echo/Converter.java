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

    /**
     * Given a List of Byte instances, return the corresponding array of byte instances.
     * @param byteList A List of Byte instances. None of the instances should be <code>null</code> .
     * @return An array of type byte, where every byte is the byte version of the corresponding Byte instance in the input.
     */
    public static byte[] byteListToByteArray(List<Byte> byteList) {
        // You'd think Java 8 could convert a List<T> to t , in a single command... "toArray(T[])" seems to dislike having to unbox Byte.
        int responseLength = byteList.size();
        byte[] byteArray = new byte[responseLength];
        for (int i = 0; i < responseLength; i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }

    /** Primitive conversion of List&lt;Byte&gt; to byte[].
     * It is assumed that none of the Bytes in the list are <code>null</code>.
     * @param input A List of Byte values, where no Byte is <code>null</code>.
     * @return An array where every byte corresponds to the Byte value at the same position in the list.
     */
    public static byte[] listToArray(List<Byte> input) {
        int fullLength = input.size();
        byte[] res = new byte[fullLength];
        for (int i = 0; i < fullLength; i++) {
            res[i] = input.get(i);
        }
        return res;
    }
}
