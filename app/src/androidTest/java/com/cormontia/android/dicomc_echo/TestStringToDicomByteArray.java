package com.cormontia.android.dicomc_echo;

import org.junit.Assert;
import org.junit.Test;

public class TestStringToDicomByteArray
{
    @Test
    public void testStringConversion( )
    {
        String input = DicomUIDs.affectedServiceClassUID;
        byte[] actualResult = DicomUIDs.byteArrayRepresentation( input );
        byte[] expectedResult = new byte[] { '1', '.', '2', '.', '8', '4', '0', '.', '1', '0', '0', '0', '8','.', '1', '.', '1', 0 };
        Assert.assertArrayEquals( expectedResult, actualResult );
    }
}
