package com.cormontia.android.dicomc_echo;

import android.util.Log;

import java.util.List;

public class Toolbox {

    private static final String TAG = "Toolbox";

    public static void logBytes(byte[] bytes) {

        Log.i(TAG, "About to log " + bytes.length + " bytes.");

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
            boolean lastByteReached = (i == bytes.length - 1);
            if (newLineRequired || lastByteReached) {
                String padding = "";
                if (lastByteReached) {
                    int paddingBytes = bytes.length % 16;
                    int paddingLength = 3 * paddingBytes;
                    padding = spaces(paddingLength);
                }
                Log.i("HEXBYTES", hexBytes + padding + "  " + asciiBytes);
                hexBytes = new StringBuffer("");
                asciiBytes = new StringBuffer("");
            }

            //TODO!+ Also print if i==length-1 . In that case, add some padding, too.
        }

    }

    /**
     * Create a String of n spaces.
     * @param n The number of spaces in the String.
     * @return A string consisting of precisely n spaces.
     */
    public static String spaces(int n) {
        StringBuffer spaces = new StringBuffer();
        for (int i = 0; i < n; i++) {
            spaces.append(' ');
        }
        return spaces.toString();
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
