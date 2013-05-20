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

import net.dirbaio.nsmbe.fs.NitroROMFilesystem;
import static net.dirbaio.nsmbe.util.Util.*;

public class NSMBe
{
    public static void main(String[] args)
    {
        try
        {
            NitroROMFilesystem fs = new NitroROMFilesystem("nsmbe.nds");
            ROM rom = new ROM(fs);
            /*
             byte[] data = rom.arm9ovs[0].getDecompressedData();
             OutputStream out = new FileOutputStream("ov0.bin");
             out.write(data);
             out.close();
             */
            System.out.println("ID  FID START        END          BSSEND     FLAGS");
            System.out.println("==================================================");

            for (int i = 0; i < rom.arm9ovs.length; i++)
            {
                Overlay o = rom.arm9ovs[i];
                System.out.println(num(o.id) + " " + num(o.getFileId()) + " " + hex(o.getRamAddr()) + " - " + hex(o.getRamAddr() + o.getRamSize()) + " - " + hex(o.getRamAddr() + o.getRamSize() + o.getBssSize()) + " " + o.getFlags());
            }

            Analyzer a = new Analyzer(rom);
//            NetFSServer s = new NetFSServer(fs);
//            s.run();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
}
