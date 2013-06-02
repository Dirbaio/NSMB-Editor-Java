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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dirbaio.nsmbe.fs.AlreadyEditingException;
import net.dirbaio.nsmbe.util.ArrayWriter;
import static net.dirbaio.nsmbe.util.Util.*;

public class Analyzer
{
    ROM rom;
    ArrayList<Section> sections;

    public Analyzer(ROM rom) throws AlreadyEditingException
    {
        this.rom = rom;
        sections = new ArrayList<>();
        loadSections();
        findUniqueSections();
        allocateOverlays();
        printStuff();

        
        //Sections should be sorted by now.
        int addr = sections.get(0).newRamAddr;
        try
        {
            ArrayWriter out = new ArrayWriter();
            for (Section s : sections)
            {
                for(int i = addr; i < s.newRamAddr; i++)
                    out.writeByte((byte)0x77);
                addr = s.newRamAddr+s.data.length;
                out.write(s.data);
            }
            FileOutputStream fout = new FileOutputStream("data.bin");
            fout.write(out.getArray());
            fout.close();
        }
        catch (IOException ex)
        {
            Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Data written to data.bin!");
    }

    private void loadSections() throws AlreadyEditingException
    {

        Arm9 a = new Arm9(rom.arm9binFile);
        int ii = 0;
        for (Arm9.Section ss : a.sections)
        {
            byte[] data = new byte[ss.len + ss.bssSize];
            System.arraycopy(ss.data, 0, data, 0, ss.data.length);

            for (int j = ss.len; j < data.length; j++)
                data[j] = (byte) 0x99;

            Section s = new Section();
            s.name = "main" + (ii++);
            s.data = data;
            s.ramAddr = ss.ramAddr;
            sections.add(s);
        }

        for (int i = 0; i < rom.arm9ovs.length; i++)
        {
            Overlay o = rom.arm9ovs[i];

            byte[] data = new byte[o.getRamSize() + o.getBssSize()];
            byte[] data2 = o.getDecompressedData();
            System.arraycopy(data2, 0, data, 0, data2.length);

            for (int j = o.getRamSize(); j < data.length; j++)
                data[j] = (byte) 0x99;

            Section s = new Section();
            s.name = "ov" + i;
            s.data = data;
            s.ramAddr = o.getRamAddr();
            sections.add(s);
        }
    }

    private void findUniqueSections()
    {
        for (Section sa : sections)
            for (Section sb : sections)
            {
                if (sa == sb)
                    continue;
                if (sa.collidesWith(sb))
                {
                    sa.unique = false;
                    sb.unique = false;
                }
            }

        //Hardcode some overlays so they're left at their original pos
        //They're the most commonly used, I don't want all pointers
        //to them to break.
        getSectionByName("ov0").unique = true;
        getSectionByName("ov10").unique = true;
        getSectionByName("ov11").unique = true;
        getSectionByName("ov54").unique = true;
        
        //This section sux, it moves everything by +8MB
        sections.remove(getSectionByName("main2"));
    }

    private void allocateOverlays()
    {
        int start = 0;
        int end = 0;
        boolean found = false;

        //Unique sections don't get moved.
        for (Section s : sections)
            if (s.unique)
            {
                int sstart = s.ramAddr;
                int send = s.ramAddr + s.data.length;
                if (sstart < start || !found)
                    start = sstart;
                if (send > end || !found)
                    end = send;
                found = true;
                s.newRamAddr = s.ramAddr;
            }

        //Everything else is slapped after it.
        for (Section s : sections)
            if (!s.unique)
            {
                s.newRamAddr = end;
                end += s.data.length;
            }
    }

    private Section getSectionByName(String name)
    {
        for (Section s : sections)
            if (s.name.equals(name))
                return s;

        return null;
    }

    private void printStuff()
    {
        //Sort by new ram addr
        Collections.sort(sections);

        System.out.println("All sections:");
        for (Section s : sections)
            System.out.println(s.name + " " + hex(s.ramAddr) + " " + hex(s.newRamAddr) + " - " + hex(s.ramAddr + s.data.length));

        System.out.println("IDC script:");
        try
        {
            
            PrintStream fout = new PrintStream("data.idc");
            fout.println("#include <idc.idc>\nstatic main(void){");
            
            for (Section s : sections)
            {
                fout.println("SegCreate(" + hex(s.newRamAddr) + ", " + hex(s.newRamAddr + s.data.length) + ", 0, 1, 1, 2);");
                fout.println("SegRename(" + hex(s.newRamAddr) + ", \"" + s.name + "\");");
                fout.println("SegClass(" + hex(s.newRamAddr) + ", \"CODE\");");
                fout.println("SegDefReg(" + hex(s.newRamAddr) + ", \"T\", 0x0);");
                fout.println("SegDefReg(" + hex(s.newRamAddr) + ", \"DS\", 0x2);");
                
            }
            fout.println("}");
        }
        catch (FileNotFoundException fileNotFoundException)
        {
        }
    }

    class Section implements Comparable<Section>
    {
        String name;
        byte[] data;
        int ramAddr;
        int newRamAddr;
        boolean unique = true;

        boolean collidesWith(Section b)
        {
            return ramAddr < b.ramAddr + b.data.length
                    && b.ramAddr < ramAddr + data.length;
        }

        @Override
        public int compareTo(Section o)
        {
            return Integer.compare(newRamAddr, o.newRamAddr);
        }
    }
}
