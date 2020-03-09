package com.cormontia.android.dicomc_echo;

import org.junit.Assert;
import org.junit.Test;

public class ByteArrayHelperTests
{
    @Test
    public void testSumOfLengths( )
    {
        byte[] input0 = new byte[] { 3, 4, 2 };
        byte[] input1 = new byte[] { 17, 39 };
        byte[] input2 = new byte[] { 127, -1, 3 };

        int actual = ByteArrayHelper.sumOfLengths( input0, input1, input2 );
        int expected = 8;
        Assert.assertEquals( expected, actual );
    }

    @Test
    public void testAppendByteArrays( )
    {
        byte[] input0 = new byte[] { 3, 4, 2 };
        byte[] input1 = new byte[] { 17, 39 };
        byte[] input2 = new byte[] { 127, -1, 3 };

        byte[] actual = ByteArrayHelper.appendByteArrays( input0, input1, input2 );
        byte[] expected = new byte[] { 3, 4, 2, 17, 39, 127, -1, 3 };
        Assert.assertArrayEquals( expected, actual );
    }
}
