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
package net.dirbaio.nsmbe.graphics;

import net.dirbaio.nsmbe.fs.AlreadyEditingException;
import net.dirbaio.nsmbe.fs.File;
import net.dirbaio.nsmbe.util.ArrayReader;
import net.dirbaio.nsmbe.util.ArrayWriter;
import net.dirbaio.nsmbe.util.Util;

public class FilePalette extends Palette
{

    File f;
    String name;

    public FilePalette(File f)
    {
        this(f, f.getName());
    }

    public FilePalette(File f, String name)
    {
        this.f = f;
        this.name = name;

        pal = arrayToPalette(f.getContents());
        if (pal.length != 0)
            pal[0] = 0;
    }

    public static int[] arrayToPalette(byte[] data)
    {
        ArrayReader ii = new ArrayReader(data);
        int[] pal = new int[data.length / 2];
        for (int i = 0; i < pal.length; i++)
            pal[i] = Util.fromRGB15(ii.readShort());
        return pal;
    }

    @Override
    public void beginEdit() throws AlreadyEditingException
    {
        f.beginEdit(this);
    }

    @Override
    public void save()
    {
        ArrayWriter oo = new ArrayWriter();
        for (int i = 0; i < pal.length; i++)
            oo.writeShort((short) Util.toRGB15(pal[i]));

        f.replace(oo.getArray(), this);

    }

    @Override
    public void endEdit()
    {
        f.endEdit(this);
    }

    @Override
    public String toString()
    {
        return name;
    }
}
