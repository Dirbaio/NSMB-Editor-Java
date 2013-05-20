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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dirbaio.nsmbe.fs.AlreadyEditingException;
import static net.dirbaio.nsmbe.util.Util.*;

public class Analyzer
{
    ROM rom;
    ArrayList<Section> sections;
    
    public Analyzer(ROM rom) throws AlreadyEditingException
    {
        this.rom = rom;
        sections = new ArrayList<>();

        Arm9 a = new Arm9(rom.arm9binFile);
        int ii = 0;
        for(Arm9.Section ss : a.sections)
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
        

        for(Section sa : sections)
            for(Section sb : sections)
            {
                if(sa == sb) continue;
                if(sa.collidesWith(sb))
                {
                    sa.unique = false;
                    sb.unique = false;
                }
            }
        
        System.out.println("All sections:");
        for(Section s : sections)
        {
            try
            {
                OutputStream out = new FileOutputStream("sections/"+s.name+".bin");
                out.write(s.data);
                out.close();
                System.out.println(s.name+" " + hex(s.ramAddr)+" - "+hex(s.ramAddr+s.data.length));
            }
            catch (IOException ex)
            {
                Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Unique sections:");
        for(Section s : sections)
            if(s.unique)
                System.out.println(s.name+" " + hex(s.ramAddr)+" - "+hex(s.ramAddr+s.data.length));
    }

    class Section
    {
        String name;
        byte[] data;
        int ramAddr;
        boolean unique = true;
        
        boolean collidesWith(Section b)
        {
            return ramAddr < b.ramAddr+b.data.length &&
                   b.ramAddr < ramAddr+data.length;
        }
    }
}
