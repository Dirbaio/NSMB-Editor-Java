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
package net.dirbaio.nsmbe;

import net.dirbaio.nsmbe.fs.File;
import net.dirbaio.nsmbe.fs.Filesystem;
import net.dirbaio.nsmbe.fs.InlineFile;

public class ROM
{
    public Filesystem fs;
    public File arm9binFile;
    public File arm9ovFile;
    public Overlay[] arm9ovs;
    public File arm7binFile;
    public File arm7ovFile;
    public Overlay[] arm7ovs;
    public File bannerFile;
    public File rsaSigFile;
    public File headerFile;

    public ROM(Filesystem fs)
    {
        this.fs = fs;

        arm9binFile = fs.getFileByName("arm9.bin");
        arm9ovFile = fs.getFileByName("arm9ovt.bin");
        arm9ovs = loadOvTable(arm9ovFile);
        arm7binFile = fs.getFileByName("arm7.bin");
        arm7ovFile = fs.getFileByName("arm7ovt.bin");
        arm7ovs = loadOvTable(arm7ovFile);
        bannerFile = fs.getFileByName("banner.bin");
        rsaSigFile = fs.getFileByName("rsasig.bin");
        headerFile = fs.getFileByName("header.bin");
    }

    private Overlay[] loadOvTable(File table)
    {
        int ct = table.getFileSize() / 32;
        Overlay[] ovs = new Overlay[ct];
        for(int i = 0; i < ct; i++)
        {
            InlineFile f = new InlineFile(table, i*32, 32, null);
            ovs[i] = new Overlay(i, f, this);
        }

        return ovs;
    }
}
