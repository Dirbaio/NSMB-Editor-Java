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
package net.dirbaio.nds.fs;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ExternalFile extends FileWithLock
{

    RandomAccessFile f;
    String fileName;

    public ExternalFile(String fileName) throws IOException
    {
        this.fileName = fileName;
        this.name = fileName;
        this.f = new RandomAccessFile(fileName, "rw");
        fileSize = (int) f.length();
    }

    @Override
    public byte[] getContents()
    {
        try
        {
            f.seek(0);
            byte[] data = new byte[fileSize];
            f.readFully(data);
            return data;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void replace(byte[] newFile, Object editor)
    {
        if (!isAGoodEditor(editor))
            throw new RuntimeException("NOT CORRECT EDITOR " + name);
    }

    @Override
    public byte[] getInterval(int start, int end)
    {
        try
        {
            validateInterval(start, end);
            int len = end-start;
            byte[] data = new byte[len];
            f.seek(start);
            f.readFully(data);
            return data;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void replaceInterval(byte[] newFile, int start)
    {
        try
        {
            validateInterval(start, start + newFile.length);
            if (!isIntervalEditable(new Interval(start, start + newFile.length)))
                throw new RuntimeException("Incorrect interval: " + name);
            f.seek(start);
            f.write(newFile);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
