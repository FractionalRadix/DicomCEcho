package com.cormontia.android.dicomc_echo;

public class DicomVR
{
    public enum VR
    {
        CS /* Code String */,
        SH /* Short String */,
        LO /* Long String */,
        ST /* Short Text */,
        LT /* Long Text */,
        UT /* Unlimited Text */,
        AE /* Application Entity */,
        PN /* Person Name */,
        UI /* Unique Identifier */,
        DA /* Date */,
        TM /* Time */,
        DT /* Date Time */,
        AS /* Age String */,
        IS /* Integer String */,
        DS /* Decimal String */,
        SS /* Signed Short */,
        US /* Unsigned Short */,
        SL /* Signed Long */,
        UL /* Unsigned Long */,
        AT /* Attribute Tag */,
        FL /* Floating Point Single */,
        FD /* Floating Point Double */,
        OB /* Other Byte String */,
        OW /* Other Word String */,
        OF /* Other Float String */,
        SQ /* Sequence of Items */,
        UN /* Unknown */
    };
}
