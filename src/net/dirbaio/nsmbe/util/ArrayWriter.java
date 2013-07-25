/*
 * Copyright (C) 2013 dirbaio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    public void writeByte(int b)
    {
        writeByte((byte) b);
    }

    public void writeShort(short u)
    {
        writeByte((byte) u);
        writeByte((byte) (u >> 8));
    }

    public void writeInt(int u)
    {
        writeByte((byte) u);
        writeByte((byte) (u >> 8));
        writeByte((byte) (u >> 16));
        writeByte((byte) (u >> 24));
    }

    public void writeLong(long u)
    {
        writeByte((byte) u);
        writeByte((byte) (u >> 8));
        writeByte((byte) (u >> 16));
        writeByte((byte) (u >> 24));
        writeByte((byte) (u >> 32));
        writeByte((byte) (u >> 40));
        writeByte((byte) (u >> 48));
        writeByte((byte) (u >> 56));
    }

    public void align(int m)
    {
        while (pos % m != 0)
            writeByte((byte) 0);
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

    public void writeString(String s)
    {
        writeShort((short) s.length());
        for (int i = 0; i < s.length(); i++)
            writeByte((byte) s.charAt(i));
    }

    public void writeCSharpString(String s)
    {
        writeByte((byte) s.length());
        for (int i = 0; i < s.length(); i++)
            writeByte((byte) s.charAt(i));
    }

    public void writeString(String s, int len)
    {
        for (int i = 0; i < len; i++)
            if (len < s.length())
                writeByte((byte) s.charAt(i));
            else
                writeByte((byte) 0);
    }

    public void writeByteArray(byte[] data)
    {
        writeInt(data.length);
        write(data);
    }
}
