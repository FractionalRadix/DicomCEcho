package com.cormontia.android.dicomc_echo;

public class DicomTag
{
    private final short group, element;

    public DicomTag( short group, short element )
    {
        this.group = group;
        this.element = element;
    }

    public short getGroup( ) { return group; }
    public short getElement( ) { return element; }
}
