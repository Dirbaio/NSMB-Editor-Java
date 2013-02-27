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

package net.dirbaio.nsmbe.fs;

import java.io.*;
import java.util.logging.*;

public class ExternalFilesystemSource extends FilesystemSource
{
    public String fileName;
    RandomAccessFile f;
    
    public ExternalFilesystemSource(String n) throws IOException
    {
        this.fileName = n;
        f = new RandomAccessFile(n, "rw");
    }

    @Override
    public void seek(int pos)
    {
        try
        {
            f.seek(pos);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public byte[] read(int len)
    {
        try
        {
            byte[] res = new byte[len];
            f.read(res);
            return res;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void write(byte[] data)
    {
        try
        {
            f.write(data);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void save()
    {
    }

    @Override
    public void close()
    {
        try
        {
            f.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString()
    {
        return fileName;
    }

    @Override
    public void setLength(int l)
    {
        throw new UnsupportedOperationException("Is this really needed?!");
    }

}

