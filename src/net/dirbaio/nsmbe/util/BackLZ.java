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

public class BackLZ
{

    public static byte[] compress(byte[] data)
    {
        return data;
    }

    public static byte[] decompress(byte[] data)
    {
        int size = data.length;
        int param1, decomp_len;
        param1 = ((data[size - 8] & 0xFF) | ((data[size - 7] & 0xFF) << 8) | ((data[size - 6] & 0xFF) << 16) | ((data[size - 5] & 0xFF) << 24));
        decomp_len = size + ((data[size - 4] & 0xFF) | ((data[size - 3] & 0xFF) << 8) | ((data[size - 2] & 0xFF) << 16) | ((data[size - 1] & 0xFF) << 24));

        int inpos = (int) (size - ((param1 >> 24) & 0xFF) - 1);
        int limit = (int) (size - (param1 & 0x00FFFFFF));
        int outpos = (int) (decomp_len - 1);

        byte[] data2 = new byte[decomp_len];
        System.arraycopy(data, 0, data2, 0, data.length);
        data = data2;

        for (;;)
        {
            if (inpos <= limit)
                break;
            byte blockctl = data[inpos--];
            if (inpos <= limit)
                break;

            boolean done = false;

            for (int i = 0; i < 8; i++)
            {
                if ((blockctl & 0x80) == 0x80)
                {
                    if (inpos <= limit)
                    {
                        done = true;
                        break;
                    }

                    int stuff = ((data[inpos - 1] & 0xFF) | ((data[inpos] & 0xFF) << 8));
                    if (stuff < 0)
                        throw new RuntimeException("DERP");
                    inpos -= 2;
                    int wdisp = (stuff & 0x0FFF) + 2;
                    int wsize = ((stuff >> 12) & 0xF) + 2;

                    for (int j = wsize; j >= 0; j--)
                    {
                        data[outpos] = data[outpos + wdisp + 1];
                        outpos--;
                    }
                } else
                {
                    if (inpos <= limit)
                    {
                        done = true;
                        break;
                    }
                    data[outpos--] = data[inpos--];
                }

                blockctl <<= 1;
            }

            if (done)
                break;
        }

        return data;
    }

//Most beautiful piece of code ever.
//Enjoy.
    public static byte[] decompressLol(byte[] sourcedata)
    {
        int DataVar1, DataVar2;
        DataVar1 = (sourcedata[sourcedata.length - 8] | (sourcedata[sourcedata.length - 7] << 8) | (sourcedata[sourcedata.length - 6] << 16) | (sourcedata[sourcedata.length - 5] << 24));
        DataVar2 = (sourcedata[sourcedata.length - 4] | (sourcedata[sourcedata.length - 3] << 8) | (sourcedata[sourcedata.length - 2] << 16) | (sourcedata[sourcedata.length - 1] << 24));

        byte[] mem = new byte[sourcedata.length + DataVar2];
        System.arraycopy(sourcedata, 0, mem, 0, sourcedata.length);

        int r0, r1, r2, r3, r5 = 0, r6 = 0, r7 = 0xBADC0DE, r12 = 1337;
        boolean N, V;
        r0 = sourcedata.length;

        if (r0 == 0)
            return null;
        r1 = DataVar1;
        r2 = DataVar2;
        r2 = r0 + r2;
        r3 = r0 - (r1 >> 0x18);
        r1 &= 0xFFFFFF;
        r1 = r0 - r1;

        int lol = 0x958;
        while (true)
            switch (lol)
            {
                case 0x958:

                    if (r3 <= r1)
                        return mem;

                    r3 -= 1;
                    r5 = mem[r3];
                    r6 = 8;
                case 0x968:
                {
                    int v1 = r6;
                    int v2 = 1;
                    r6 = v1 - v2;
                    N = (r12 & 0x80000000) != 0;
                    V = ((((v1 & 0x80000000) != 0) && ((v2 & 0x80000000) == 0) && ((r6 & 0x80000000) == 0))
                            || ((v1 & 0x80000000) == 0) && ((v2 & 0x80000000) != 0) && ((r6 & 0x80000000) != 0));

                    if (N != V)
                    {
                        lol = 0x958;
                        break;
                    }
                    if ((r5 & 0x80) != 0)
                    {
                        lol = 0x984;
                        break;
                    }
                    r3 -= 1;
                    r0 = mem[r3];
                    r2 -= 1;
                    mem[r2] = (byte) r0;
                    lol = 0x9AC;
                    break;
                }
                case 0x984:
                    r3 -= 1;
                    r12 = mem[r3];
                    r3 -= 1;
                    r7 = mem[r3];
                    r7 |= (r12 << 8);
                    r7 &= 0xFFF;
                    r7 += 2;
                    r12 += 0x20;

                case 0x99C:
                    r0 = mem[r2 + r7];
                    r2 -= 1;
                    mem[r2] = (byte) r0;
                    int v1 = r12;
                    int v2 = 0x10;
                    r12 = v1 - v2;
                    N = (r12 & 0x80000000) != 0;
                    V = ((((v1 & 0x80000000) != 0) && ((v2 & 0x80000000) == 0) && ((r12 & 0x80000000) == 0))
                            || ((v1 & 0x80000000) == 0) && ((v2 & 0x80000000) != 0) && ((r12 & 0x80000000) != 0));
                    if (N == V)
                    {
                        lol = 0x99C;
                        break;
                    }

                case 0x9AC:
                    r5 <<= 1;
                    if (r3 > r1)
                    {
                        lol = 0x968;
                        break;
                    }
                case 0x9B8:
                    return mem;
            }
    }

    public static int getDecompressedSize(byte[] data)
    {
        return 0;
    }
}
