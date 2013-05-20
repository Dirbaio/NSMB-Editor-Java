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

import java.util.ArrayList;
import net.dirbaio.nsmbe.fs.AlreadyEditingException;
import net.dirbaio.nsmbe.fs.File;
import net.dirbaio.nsmbe.util.ArrayWriter;
import net.dirbaio.nsmbe.util.BackLZ;

public class Arm9
{
    File f;
    int codeSettingsOffs;
    ArrayList<Section> sections;

    public Arm9(File f) throws AlreadyEditingException
    {
        this.f = f;

        codeSettingsOffs = -1;
        for (int i = 0; i < 0x8000; i += 4)
            if (f.getUintAt(i) == 0xDEC00621 && f.getUintAt(i + 4) == 0x2106C0DE)
            {
                codeSettingsOffs = i - 0x1C;
                break;
            }

        if (codeSettingsOffs == -1)
            throw new RuntimeException("Code settings not found.");
        
        decompress();
        loadSections();
    }

    public void newSection(int ramAddr, int ramLen, int fileOffs, int bssSize)
    {
        // Console.Out.WriteLine(String.Format("SECTION {0:X8} - {1:X8} - {2:X8}", ramAddr, ramAddr + ramLen, ramAddr + ramLen + bssSize));

        byte[] data = new byte[ramLen];
        System.arraycopy(f.getContents(), fileOffs, data, 0, ramLen);
        Section s = new Section(data, ramAddr, bssSize);
        sections.add(s);
    }

    public void loadSections()
    {
        if (isCompressed())
            throw new RuntimeException("Can't load sections of compressed arm9");

        sections = new ArrayList<>();

        int copyTableBegin = (int) (f.getUintAt(codeSettingsOffs + 0x00) - 0x02000000);
        int copyTableEnd = (int) (f.getUintAt(codeSettingsOffs + 0x04) - 0x02000000);
        int dataBegin = (int) (f.getUintAt(codeSettingsOffs + 0x08) - 0x02000000);

        newSection(0x02000000, dataBegin, 0x0, 0);
        sections.get(0).real = false;

        while (copyTableBegin < copyTableEnd)
        {
            int start = (int) f.getUintAt(copyTableBegin);
            copyTableBegin += 4;
            int size = (int) f.getUintAt(copyTableBegin);
            copyTableBegin += 4;
            int bsssize = (int) f.getUintAt(copyTableBegin);
            copyTableBegin += 4;

            newSection(start, size, dataBegin, bsssize);
            dataBegin += size;
        }
    }
    //020985f0 02098620

    public void saveSections() throws AlreadyEditingException
    {
//        Console.Out.WriteLine("Saving sections...");
        f.beginEdit(this);
        ArrayWriter o = new ArrayWriter();
        for (Section s : sections)
        {
            //Console.Out.WriteLine(String.Format("{0:X8} - {1:X8} - {2:X8}: {3:X8}",
            //        s.ramAddr, s.ramAddr + s.len, s.ramAddr + s.len + s.bssSize, o.getPos()));

            o.write(s.data);
            o.align(4);
        }

        int sectionTableAddr = 0x02000E00;
        ArrayWriter o2 = new ArrayWriter();
        for (Section s : sections)
        {
            if (!s.real)
                continue;
            if (s.len == 0)
                continue;
            o2.writeInt((int) s.ramAddr);
            o2.writeInt((int) s.len);
            o2.writeInt((int) s.bssSize);
        }

        //Write BSS sections last
        //because they overwrite huge areas with zeros (?)
        for (Section s : sections)
        {
            if (!s.real)
                continue;
            if (s.len != 0)
                continue;

            o2.writeInt((int) s.ramAddr);
            o2.writeInt((int) s.len);
            o2.writeInt((int) s.bssSize);
        }

        byte[] data = o.getArray();
        byte[] sectionTable = o2.getArray();
        System.arraycopy(sectionTable, 0, data, sectionTableAddr - 0x02000000, sectionTable.length);
        f.replace(data, this);
        f.endEdit(this);

        f.setUintAt(codeSettingsOffs + 0x00, (int) sectionTableAddr);
        //Console.Out.WriteLine(String.Format("{0:X8} {1:X8}", codeSettingsOffs + 0x04, (int) o2.getPos() + sectionTableAddr));
        f.setUintAt(codeSettingsOffs + 0x04, (int) o2.getPos() + sectionTableAddr);
        f.setUintAt(codeSettingsOffs + 0x08, (int) (sections.get(0).len + 0x02000000));

//        Console.Out.WriteLine("DONE");
    }

    int getDecompressionRamAddr()
    {
        return f.getUintAt(codeSettingsOffs + 0x14);
    }

    void setDecompressionRamAddr(int addr)
    {
        f.setUintAt(codeSettingsOffs + 0x14, addr);
    }

    boolean isCompressed()
    {
        return getDecompressionRamAddr() != 0;
    }

    public void decompress() throws AlreadyEditingException
    {
        if (!isCompressed())
            return;

        int decompressionOffs = getDecompressionRamAddr() - 0x02000000;

        int compDatSize = (int) (f.getUintAt(decompressionOffs - 8) & 0xFFFFFF);
        int compDatOffs = decompressionOffs - compDatSize;
        //Console.Out.WriteLine("OFFS: " + compDatOffs.ToString("X"));
        //Console.Out.WriteLine("SIZE: " + compDatSize.ToString("X"));

        byte[] data = f.getContents();
        byte[] compData = new byte[compDatSize];
        System.arraycopy(data, compDatOffs, compData, 0, compDatSize);
        byte[] decompData = BackLZ.decompress(compData);
        byte[] newData = new byte[data.length - compData.length + decompData.length];
        System.arraycopy(data, 0, newData, 0, data.length);
        System.arraycopy(decompData, 0, newData, compDatOffs, decompData.length);

        f.beginEdit(this);
        f.replace(newData, this);
        f.endEdit(this);
        setDecompressionRamAddr(0);
    }

    public class Section
    {
        public byte[] data;
        public int len;
        public int ramAddr;
        public int bssSize;
        public boolean real = true;

        public Section(byte[] data, int ramAddr, int bssSize)
        {
            this.data = data;
            len = data.length;
            this.ramAddr = ramAddr;
            this.bssSize = bssSize;
        }

        public boolean containsRamAddr(int addr)
        {
            return addr >= ramAddr && addr < ramAddr + len;
        }

        public int readFromRamAddr(int addr)
        {
            addr -= ramAddr;

            return (int) ((data[addr] & 0xFF)
                    | (data[addr + 1] & 0xFF) << 8
                    | (data[addr + 2] & 0xFF) << 16
                    | (data[addr + 3] & 0xFF) << 24);
        }

        public void writeToRamAddr(int addr, int val)
        {
            addr -= ramAddr;

            data[addr] = (byte) val;
            data[addr + 1] = (byte) (val >> 8);
            data[addr + 2] = (byte) (val >> 16);
            data[addr + 3] = (byte) (val >> 24);
        }
    }
}
