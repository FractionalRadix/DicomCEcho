package com.cormontia.android.dicomc_echo;

import android.util.Log;

import java.util.List;

public class Toolbox {
    public static void logBytes(byte[] bytes) {

        StringBuffer hexBytes = new StringBuffer();
        StringBuffer asciiBytes = new StringBuffer();

        for (int i = 0; i < bytes.length; i++) {

            byte cur = bytes[i];

            // Get the hexadecimal representation of the current byte.
            String hexByte = String.format("%02x ", cur);
            hexBytes.append(hexByte);

            // Get the ASCII representation of the current byte, or "." if it is not printable.
            String asciiByte = ".";
            if (cur >= 32 && cur <= 126) {
                asciiByte = String.format("%c", cur);
            }
            asciiBytes.append(asciiByte);

            boolean newLineRequired = (i % 16 == 15);
            boolean lastLine = (i + 16 > bytes.length);
            if (newLineRequired || lastLine) {
                //TODO!+ if (lastLine), add some padding.
                Log.i("HEXBYTES", hexBytes + "  " + asciiBytes);
                hexBytes = new StringBuffer("");
                asciiBytes = new StringBuffer("");
            }
            if (lastLine) {
                break;
            }
        }

    }

    /** Primitive tool for adding the bytes of a String to List&lt;Byte&gt; .
     * Does not support characters that won't fit in a byte.
     * @param list A List of bytes, to which the characters in the string will be appended.
     * @param str A string, whose individual characters need to be added to the given list.
     * @return The input string, with the bytes for the characters in the string appended at the end.
     */
    public static List<Byte> addString(List<Byte> list, String str) {
        for(char ch : str.toCharArray()) {
            list.add((byte) ch);
        }
        return list;
    }
}
