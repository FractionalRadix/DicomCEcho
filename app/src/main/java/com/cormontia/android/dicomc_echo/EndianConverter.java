package com.cormontia.android.dicomc_echo;

import java.util.ArrayList;
import java.util.List;

public class EndianConverter
{
    /**
     * Given an even-sized array of bytes, inverse the byte ordering.
     * In other words, if the array was little-endian, it will now be big-endian. And vice versa.
     * @param in An array of bytes; the length of the array is an even number. 0-sized arrays are allowed.
     * @return The permutation of the input array where every byte pair is swapped. Or <code>null</code> if the input array had an odd length.
     */
    public static byte[] swapBytes( byte[] in )
    {
        //PRE: Size of input byte array is even.
        if (in.length % 2 != 0 )
            return null;

        byte[] res = new byte[ in.length ];
        for ( int i = 0; i < in.length; i += 2 )
        {
            res[ i ] = in[ i + 1 ];
            res[ i + 1 ] = in[ i ];
        }
        return res;
    }

    public static byte[] littleEndian( short input )
    {
        byte lsb = (byte) ( input & 0x00FF );
        byte msb = (byte) ( ( input & 0xFF00 ) >> 8 );
        byte[] res = new byte[] { lsb, msb };
        return res;
    }

    public static byte[] bigEndian( short input )
    {
        byte lsb = (byte) ( input & 0x00FF );
        byte msb = (byte) ( ( input & 0xFF00 ) >> 8 );
        byte[] res = new byte[] { msb, lsb };
        return res;
    }

    public static byte[] littleEndian( int input )
    {
        byte byte0 = (byte) ( input & 0x000000FF ); // LSB
        byte byte1 = (byte) ( ( input & 0x0000FF00 ) >>  8 );
        byte byte2 = (byte) ( ( input & 0x00FF0000 ) >> 16 );
        byte byte3 = (byte) ( ( input & 0xFF000000 ) >> 24 ); // MSB

        byte[] res = new byte[] { byte0, byte1, byte2, byte3 };

        return res;
    }

    public static byte[] bigEndian( int input )
    {
        byte byte0 = (byte) ( input & 0x000000FF ); // LSB
        byte byte1 = (byte) ( ( input & 0x0000FF00 ) >>  8 );
        byte byte2 = (byte) ( ( input & 0x00FF0000 ) >> 16 );
        byte byte3 = (byte) ( ( input & 0xFF000000 ) >> 24 ); // MSB

        byte[] res = new byte[] { byte3, byte2, byte1, byte0 };

        return res;
    }
}
