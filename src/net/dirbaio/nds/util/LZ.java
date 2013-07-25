/*
 *   This file is part of NSMB Editor 5.
 *
 *   NSMB Editor 5 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   NSMB Editor 5 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with NSMB Editor 5.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.dirbaio.nds.util;

public class LZ
{

    public static byte[] compress(byte[] data)
    {
        ArrayWriter res = new ArrayWriter();

        res.writeInt((data.length << 8) | 0x10);

        byte[] tempBuffer = new byte[16];

        //Current byte to compress.
        int current = 0;

        while (current < data.length)
        {
            int tempBufferCursor = 0;
            byte blockFlags = 0;
            for (int i = 0; i < 8; i++)
            {
                //Not sure if this is needed. The DS probably ignores this data.
                if (current >= data.length)
                {
                    tempBuffer[tempBufferCursor++] = 0;
                    continue;
                }

                int searchPos = 0;
                int searchLen = 0;
                int maxMatchDiff = 4096;
                int maxMatchLen = 18;

                int start = current - maxMatchDiff;
                if (start < 0)
                    start = 0;

                boolean stop = false;
                for (int thisMatch = start; thisMatch < current && !stop; thisMatch++)
                {
                    int thisLength = 0;
                    while (thisLength < maxMatchLen
                            && thisMatch + thisLength < current
                            && current + thisLength < data.length
                            && data[current + thisLength] == data[thisMatch + thisLength])
                        thisLength++;

                    if (thisLength > searchLen)
                    {
                        searchPos = thisMatch;
                        searchLen = thisLength;
                    }

                    //We can't improve the max match length again...
                    if (searchLen == maxMatchLen)
                        stop = true;
                }

                int searchDisp = current - searchPos - 1;
                if (searchLen > 2) //We found a big match, let's write a compressed block.
                {
                    blockFlags |= (byte) (1 << (7 - i));
                    tempBuffer[tempBufferCursor++] = (byte) ((((searchLen - 3) & 0xF) << 4) + ((searchDisp >> 8) & 0xF));
                    tempBuffer[tempBufferCursor++] = (byte) (searchDisp & 0xFF);
                    current += searchLen;
                } else
                    tempBuffer[tempBufferCursor++] = data[current++];
            }

            res.writeByte(blockFlags);
            for (int i = 0; i < tempBufferCursor; i++)
                res.writeByte(tempBuffer[i]);
        }

        return res.getArray();
    }

    public static byte[] decompress(byte[] source)
    {
        int len;
        len = (source[1] & 0xFF) | ((source[2] & 0xFF) << 8) | ((source[3] & 0xFF) << 16);
        byte[] dest = new byte[len];
        int i, j, xin, xout;
        xin = 4;
        xout = 0;
        int length, offset, windowOffset, data;
        byte d;
        while (len > 0)
        {
            d = source[xin++];
            if (d != 0)
                for (i = 0; i < 8; i++)
                {
                    if ((d & 0x80) != 0)
                    {
                        data = (((source[xin] & 0xFF) << 8) | (source[xin + 1] & 0xFF));
                        xin += 2;
                        length = (data >> 12) + 3;
                        offset = data & 0xFFF;
                        windowOffset = xout - offset - 1;
                        for (j = 0; j < length; j++)
                        {
                            dest[xout++] = dest[windowOffset++];
                            len--;
                            if (len == 0)
                                return dest;
                        }
                    } else
                    {
                        dest[xout++] = source[xin++];
                        len--;
                        if (len == 0)
                            return dest;
                    }
                    d <<= 1;
                }
            else
                for (i = 0; i < 8; i++)
                {
                    dest[xout++] = source[xin++];
                    len--;
                    if (len == 0)
                        return dest;
                }
        }
        return dest;
    }

    public static int getDecompressedSize(byte[] source)
    {
        int len;
        len = (source[1] & 0xFF) | ((source[2] & 0xFF) << 8) | ((source[3] & 0xFF) << 16);
        return len;
    }

    public static byte[] compressHeadered(byte[] data)
    {
        byte[] res = compress(data);
        byte[] res2 = new byte[res.length + 4];
        System.arraycopy(res, 0, res2, 4, res.length);
        res2[0] = 0x4C;
        res2[1] = 0x5A;
        res2[2] = 0x37;
        res2[3] = 0x37;
        return res2;
    }

    public static byte[] decompressHeadered(byte[] data)
    {
        byte[] data2 = new byte[data.length - 4];
        System.arraycopy(data, 4, data2, 0, data2.length);
        return decompress(data2);
    }

    public static int getDecompressedSizeHeadered(byte[] source)
    {
        int len;
        len = (source[5] & 0xFF) | ((source[6] & 0xFF) << 8) | ((source[7] & 0xFF) << 16);
        return len;
    }
}
