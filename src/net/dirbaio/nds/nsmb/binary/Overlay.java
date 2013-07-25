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
package net.dirbaio.nds.nsmb.binary;

import net.dirbaio.nds.Rom;
import net.dirbaio.nds.fs.AlreadyEditingException;
import net.dirbaio.nds.fs.File;
import net.dirbaio.nds.util.BackLZ;

public class Overlay
{

    private File tableEntry;
    private File f;
    private int id;

    public Overlay(int id, File tableEntry, Rom rom)
    {
        this.id = id;
        this.tableEntry = tableEntry;
        f = rom.fs.getFileById(getFileId());
    }

    public int getId()
    {
        return id;
    }

    public File getFile()
    {
        return f;
    }

    public boolean isCompressed()
    {
        return (getFlags() & 0x1) != 0;
    }

    public void setCompressed(boolean compressed)
    {
        int flags = getFlags() & 0xFE;
        if (compressed)
            flags |= 0x01;
        setFlags((byte) flags);
    }

    public byte[] getDecompressedData()
    {
        if (isCompressed())
            return BackLZ.decompress(f.getContents());
        else
            return f.getContents();
    }

    public void decompress() throws AlreadyEditingException
    {
        if (isCompressed())
        {
            f.beginEdit(this);
            f.replace(BackLZ.decompress(f.getContents()), this);
            f.endEdit(this);
            setCompressed(false);
        }
    }

    public int getOvId()
    {
        return tableEntry.getUintAt(0x00);
    }

    public int getRamAddr()
    {
        return tableEntry.getUintAt(0x04);
    }

    public int getRamSize()
    {
        return tableEntry.getUintAt(0x08);
    }

    public int getBssSize()
    {
        return tableEntry.getUintAt(0x0C);
    }

    public int getStaticInitStart()
    {
        return tableEntry.getUintAt(0x10);
    }

    public int getStaticInitEnd()
    {
        return tableEntry.getUintAt(0x14);
    }

    public int getFileId()
    {
        return tableEntry.getUshortAt(0x18);
    }

    public byte getFlags()
    {
        return tableEntry.getByteAt(0x1F);
    }

    public void setFlags(byte flags)
    {
        tableEntry.setByteAt(0x1F, flags);
    }
}
