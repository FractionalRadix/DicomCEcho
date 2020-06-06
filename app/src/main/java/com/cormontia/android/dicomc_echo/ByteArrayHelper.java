package com.cormontia.android.dicomc_echo;

public class ByteArrayHelper
{
    public static int sumOfLengths( byte[]... arrays )
    {
        if ( arrays == null )
            return 0;

        int len = 0;
        for ( byte[] array : arrays )
        {
            if ( array != null )
            {
                len += array.length;
            }
        }

        return len;
    }

    public static byte[] appendByteArrays( byte[]... arrays )
    {
        if ( arrays == null )
            return null;

        int len = sumOfLengths( arrays );
        byte[] res = new byte[ len ];

        int i = 0;
        for( byte[] array : arrays )
        {
            if ( array != null )
            {
                for (byte b : array) {
                    res[i] = b;
                    i++;
                }
            }
        }

        return res;
    }
}
