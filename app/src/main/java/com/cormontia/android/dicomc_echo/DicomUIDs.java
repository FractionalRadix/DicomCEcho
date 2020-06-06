package com.cormontia.android.dicomc_echo;

public class DicomUIDs
{
    public static final String affectedServiceClassUID = "1.2.840.10008.1.1";

    public static byte[] byteArrayRepresentation( String input )
    {
        //TODO!~ This might all get a lot more complicated if we have to allow for Unicode.
        // Need to look up how DICOM thinks about character representation.
        byte[] bytes = input.getBytes();
        if (bytes.length % 2 == 0 )
        {
            return bytes;
        }
        else
        {
            //TODO!~ Need to think about how endian-ness affects this.
            // It is VERY naive to just set the last byte to 0. There's a 50% chance it should be the byte BEFORE that.
            byte[] res = new byte[ bytes.length + 1 ];
            System.arraycopy(bytes, 0, res, 0, bytes.length);
            res[ bytes.length ] = 0;
            return res;
        }
    }
}
