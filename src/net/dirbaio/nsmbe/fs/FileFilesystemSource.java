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

import java.io.IOException;

public class FileFilesystemSource extends FilesystemSource
{

    private File f;
    private int pos = 0;
    private byte[] data;

    public FileFilesystemSource(File f) throws AlreadyEditingException
    {
        this.f = f;
        f.beginEdit(this);
        this.data = f.getContents();
    }

    @Override
    public void seek(int pos)
    {
        if (pos < 0)
            throw new RuntimeException("Seek position out of bounds: " + pos);
        this.pos = pos;
    }

    @Override
    public byte[] read(int len)
    {
        if (pos + len > data.length)
            throw new RuntimeException("Read out of bounds");
        byte[] r = new byte[len];
        System.arraycopy(data, pos, r, 0, len);
        pos += len;
        return r;
    }

    @Override
    public void write(byte[] d)
    {
        if (pos + d.length > data.length)
            setLength(pos + d.length);
        System.arraycopy(data, pos, d, 0, d.length);
        pos += d.length;
    }

    @Override
    public void save()
    {
        f.replace(data, this);
    }

    @Override
    public void close()
    {
        f.endEdit(this);
    }

    @Override
    public String toString()
    {
        return f.getName();
    }

    @Override
    public void setLength(int l)
    {
        byte[] n = new byte[l];
        int c = l;
        if (c > data.length)
            c = data.length;
        System.arraycopy(data, 0, n, 0, c);
        data = n;
    }
}
