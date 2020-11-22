package com.cormontia.android.dicomc_echo;

public class DicomVR
{
    /**
     * Enumeration of DICOM Value Representations (VRs).
     * Taken from the official list of DICOM VR's: http://dicom.nema.org/medical/dicom/current/output/chtml/part05/sect_6.2.html
     * There is no official ordering between VR's, but since the DICOM specification lists them in alphabetical order, we shall do the same here.
     */
    public enum VR
    {
        AE /* Application Entity */,
        AS /* Age String */,
        AT /* Attribute Tag */,
        CS /* Code String */,
        DA /* Date */,
        DS /* Decimal String */,
        DT /* Date Time */,
        FL /* Floating Point Single */,
        FD /* Floating Point Double */,
        IS /* Integer String */,
        LO /* Long String */,
        LT /* Long Text */,
        OB /* Other Byte String */,
        OD /* Other Double */,
        OF /* Other Float String */,
        OL /* Other Long */,
        OV /* Other 64-bit Very long */,
        OW /* Other Word String */,
        PN /* Person Name */,
        SH /* Short String */,
        SL /* Signed Long */,
        SQ /* Sequence of Items */,
        SS /* Signed Short */,
        ST /* Short Text */,
        SV /* Signed 64-bit Very Long */,
        TM /* Time */,
        UC /* Unlimited Characters */,
        UI /* Unique Identifier */,
        UL /* Unsigned Long */,
        UR /* Universal Resource Identifier or Universal Resource Locator */,
        US /* Unsigned Short */,
        UT /* Unlimited Text */,
        UN /* Unknown */
    }
}
