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
import net.dirbaio.nsmbe.level.NSMBLevel;
import net.dirbaio.nsmbe.level.source.InternalLevelSource;
import net.dirbaio.nsmbe.leveleditor.LevelEditorComponent;
import net.dirbaio.nsmbe.util.ComponentViewer;

public class NSMBe
{

    public static void main(String[] args)
    {
        try
        {
            NitroROMFilesystem fs = new NitroROMFilesystem("nsmb.nds");
            NSMBRom rom = new NSMBRom(fs);
            /*
             byte[] data = rom.arm9ovs[0].getDecompressedData();
             OutputStream out = new FileOutputStream("ov0.bin");
             out.write(data);
             out.close();
             */

            /*
             System.out.println("ID  FID START        END          BSSEND     FLAGS");
             System.out.println("==================================================");

             for (int i = 0; i < rom.arm9ovs.length; i++)
             {
             Overlay o = rom.arm9ovs[i];
             System.out.println(num(o.id) + " " + num(o.getFileId()) + " " + hex(o.getRamAddr()) + " - " + hex(o.getRamAddr() + o.getRamSize()) + " - " + hex(o.getRamAddr() + o.getRamSize() + o.getBssSize()) + " " + o.getFlags());
             }
             */
//            Analyzer a = new Analyzer(rom);
//            NetFSServer s = new NetFSServer(fs);
//            s.run();

            NSMBLevel l = new NSMBLevel(rom, new InternalLevelSource(rom, "A01_1", "1-1"));
            LevelEditorComponent ed = new LevelEditorComponent(l);
            new ComponentViewer(ed);
            //new ImageViewer(l.GFX.Tilesets[0].Map16Buffer);
            /*
             File f = new LZFile(rom.fs.getFileByName("d_2d_I_M_back_demo_castle_ncl.bin"), LZFile.COMP_LZ);
             FilePalette p1 = new FilePalette(new InlineFile(f, 0, 512, null));
             FilePalette p2 = new FilePalette(new InlineFile(f, 512, 512, null));
             Image2D i = new Image2D(new LZFile(rom.fs.getFileByName("d_2d_I_M_back_demo_castle_ncg.bin"), LZFile.COMP_LZ), 256, false);
             Palette[] pals = {p1, p2};
             Tilemap t = new Tilemap(new LZFile(rom.fs.getFileByName("d_2d_I_M_back_demo_castle_nsc.bin"), LZFile.COMP_LZ), 64, i, pals, 576, 10);
             new ImageViewer(t.render());*/
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
}
