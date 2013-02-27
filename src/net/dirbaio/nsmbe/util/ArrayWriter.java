package net.dirbaio.nsmbe.util;

public class ArrayWriter
{
    //implements an unbonded array to store unlimited data.
    //writes in amortized constant time.

    private byte[] buf = new byte[16];
    private int pos = 0;

    public ArrayWriter()
    {
    }

    public int getPos()
    {
        return pos;
    }

    public byte[] getArray()
    {
        byte[] ret = new byte[pos];
        System.arraycopy(buf, 0, ret, 0, pos);
        return ret;
    }

    public void writeByte(byte b)
    {
        if (buf.length <= pos)
            grow();

        buf[pos] = b;
        pos++;
    }

    public void writeShort(short u)
    {
        writeByte((byte)u);
        writeByte((byte)(u >> 8));
    }

    public void writeInt(int u)
    {
        writeByte((byte)u);
        writeByte((byte)(u >> 8));
        writeByte((byte)(u >> 16));
        writeByte((byte)(u >> 24));
    }

    public void writeLong(long u)
    {
        writeByte((byte)u);
        writeByte((byte)(u >> 8));
        writeByte((byte)(u >> 16));
        writeByte((byte)(u >> 24));
        writeByte((byte)(u >> 32));
        writeByte((byte)(u >> 40));
        writeByte((byte)(u >> 48));
        writeByte((byte)(u >> 56));
    }

    public void align(int m)
    {
        while (pos % m != 0)
            writeByte((byte)0);
    }

    private void grow()
    {
        byte[] nbuf = new byte[buf.length * 2];
        System.arraycopy(buf, 0, nbuf, 0, buf.length);
        buf = nbuf;
    }

    public void write(byte[] ar)
    {
        //TODO This can be optimized!
        for (int i = 0; i < ar.length; i++)
            writeByte(ar[i]);
    }
}
