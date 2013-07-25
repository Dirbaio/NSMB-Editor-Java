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
package net.dirbaio.nds.nsmb;

import net.dirbaio.nds.fs.File;

public class BinaryTable
{

    private File f;
    private int entrySize;

    public BinaryTable(File f, int entrySize)
    {
        this.f = f;
        this.entrySize = entrySize;
    }

    public int getLength()
    {
        return f.getFileSize() / entrySize;
    }

    public byte[] getData(int ind)
    {
        if (ind >= getLength() || ind < 0)
            throw new ArrayIndexOutOfBoundsException("Out of table");

        return f.getInterval(ind * entrySize, ind * entrySize + entrySize);
    }

    public int getVal(int ind)
    {
        byte[] val = getData(ind);
        int r = 0;
        for (int i = 0; i < entrySize; i++)
            r |= (val[i] & 0xFF) << (i * 8);

        return r;
    }
}
