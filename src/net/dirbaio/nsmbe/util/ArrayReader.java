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

import java.security.InvalidParameterException;
import java.util.Stack;

public class ArrayReader
{
    byte[] data;
    private int pos = 0;
    private Stack<Integer> savedPos = new Stack<>();

    public ArrayReader(byte[] data)
    {
        this.data = data;
    }

    /**
     * @return the pos
     */
    public int getPos()
    {
        return pos;
    }

    /**
     * @param pos the pos to set
     */
    public void setPos(int pos)
    {
        this.pos = pos;
    }

    public void savePos()
    {
        savedPos.push(pos);
    }

    public void loadPos()
    {
        pos = savedPos.pop();
    }

    public boolean available(int len)
    {
        return data.length - pos >= len;
    }
    
    public void seek(int pos)
    {
        if (pos < 0 || pos > data.length)
            throw new InvalidParameterException("Seek out of bounds: " + pos);
        this.pos = pos;
    }

    public void skip(int bytes)
    {
        if (pos + bytes < 0 || pos + bytes > data.length)
            throw new InvalidParameterException("Skip out of bounds: " + pos);

        pos += bytes;
    }
    

    public byte readByte()
    {
        return data[pos++];
    }


    public int readShort()
    {
        int res = 0;
        for (int i = 0; i < 2; i++)
            res |= (int) (readByte()&0xFF) << 8 * i;
        return (int) res;
    }
    
    public int readInt()
    {
        int res = 0;
        for (int i = 0; i < 4; i++)
            res |= (int) (readByte()&0xFF) << 8 * i;
        return (int) res;
    }

    public long readLong()
    {
        long res = 0;
        for (int i = 0; i < 8; i++)
            res |= (long) (readByte()&0xFF) << 8 * i;
        return res;
    }
    
    public void read(byte[] dest)
    {
        if(!available(dest.length))
            throw new InvalidParameterException("read() too much data: "+dest.length);
        
        System.arraycopy(data, pos, dest, 0, dest.length);
        pos += dest.length;
    }
    
    public String readString(int len)
    {
        if(!available(len))
            throw new InvalidParameterException("readString() too much data: "+len);

        String res = "";
        for(int i = 0; i < len; i++)
        {
            byte b = readByte();
            if(b != 0)
                res += (char)(b&0xFF);
        }
        
        return res;
    }
    
    public String readString()
    {
        int len = readShort();
        return readString(len);
    }
}
