package com.cormontia.android.dicomc_echo;

import java.util.ArrayList;
import java.util.List;

public class EndianConverter
{
    public static byte[] littleEndian( short input )
    {
        byte lsb = (byte) ( input & 0x00FF );
        byte msb = (byte) ( ( input & 0xFF00 ) >> 8 );
        return ( new byte[] { lsb, msb } ) ;
    }
    
    public static byte[] littleEndian( int input )
    {
        byte byte0 = (byte) ( input & 0x000000FF ); // LSB
        byte byte1 = (byte) ( ( input & 0x0000FF00 ) >>  8 );
        byte byte2 = (byte) ( ( input & 0x00FF0000 ) >> 16 );
        byte byte3 = (byte) ( ( input & 0xFF000000 ) >> 24 ); // MSB

        return ( new byte[] { byte0, byte1, byte2, byte3 } );
    }
}
