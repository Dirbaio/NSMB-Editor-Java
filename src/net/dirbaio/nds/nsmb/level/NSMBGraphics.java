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
package net.dirbaio.nds.nsmb.level;

import java.util.ArrayList;
import net.dirbaio.nds.nsmb.NSMBRom;
import net.dirbaio.nds.fs.File;

public class NSMBGraphics
{

    NSMBRom rom;
    public NSMBTileset[] Tilesets;

    public NSMBGraphics(NSMBRom rom)
    {
        this.rom = rom;
    }

    public void LoadTilesets(short tilesetId)
    {
        // backup
        LoadTilesets(tilesetId, 8);
    }

    public void LoadTilesets(short tilesetId, int bgId)
    {
        Tilesets = new NSMBTileset[3];

        int JyotyuPalID = rom.getTable(NSMBRom.Table.Table_Jyotyu_NCL).getVal(bgId);

        File JyotyuPalFile;
        if (JyotyuPalID == 1)
            JyotyuPalFile = rom.fs.getFileByName("d_2d_A_J_jyotyu_B_ncl.bin");
        else if (JyotyuPalID == 2)
            JyotyuPalFile = rom.fs.getFileByName("d_2d_A_J_jyotyu_R_ncl.bin");
        else if (JyotyuPalID == 3)
            JyotyuPalFile = rom.fs.getFileByName("d_2d_A_J_jyotyu_W_ncl.bin");
        else
            JyotyuPalFile = rom.fs.getFileByName("d_2d_A_J_jyotyu_ncl.bin");

        Tilesets[0] = new NSMBTileset(
                rom.fs.getFileByName("d_2d_A_J_jyotyu_ncg.bin"),
                JyotyuPalFile,
                rom.fs.getFileByName("d_2d_PA_A_J_jyotyu.bin"),
                rom.fs.getFileByName("A_J_jyotyu.bin"),
                rom.fs.getFileByName("A_J_jyotyu_hd.bin"),
                rom.getEmbeddedFile(NSMBRom.EmbeddedFile.File_Jyotyu_CHK),
                true, 0);

        LoadTileset1(tilesetId);

        Tilesets[2] = new NSMBTileset(
                rom.fs.getFileByName("d_2d_I_S_tikei_nohara_ncg.bin"),
                rom.fs.getFileByName("d_2d_I_S_tikei_nohara_ncl.bin"),
                rom.fs.getFileByName("d_2d_PA_I_S_nohara.bin"),
                rom.fs.getFileByName("I_S_nohara.bin"),
                rom.fs.getFileByName("I_S_nohara_hd.bin"),
                rom.fs.getFileByName("NoHaRaSubUnitChangeData.bin"),
                false, 2);

        // Patch in a bunch of overrides to the normal tileset
        // Now works directly on the map16 data
        Tilesets[0].UseOverrides = true;
        Tilesets[0].Overrides[36] = 135;
        Tilesets[0].Overrides[112] = 26;
        Tilesets[0].Overrides[113] = 27;
        Tilesets[0].Overrides[114] = 53;
        Tilesets[0].Overrides[115] = 55;
        Tilesets[0].Overrides[116] = 28;
        Tilesets[0].Overrides[117] = 57;
        Tilesets[0].Overrides[118] = 0;
        Tilesets[0].Overrides[119] = 1;
        Tilesets[0].Overrides[120] = 4;
        Tilesets[0].Overrides[121] = 5;
        Tilesets[0].Overrides[122] = 30;
        Tilesets[0].Overrides[123] = 31;
        Tilesets[0].Overrides[124] = 8;
        Tilesets[0].Overrides[125] = 9;
        Tilesets[0].Overrides[126] = 20;
        Tilesets[0].Overrides[127] = 21;
        Tilesets[0].Overrides[128] = 24;
        Tilesets[0].Overrides[129] = 25;
        Tilesets[0].Overrides[132] = 29;
        Tilesets[0].Overrides[130] = 54;
        Tilesets[0].Overrides[131] = 56;
        Tilesets[0].Overrides[133] = 58;
        Tilesets[0].Overrides[134] = 2;
        Tilesets[0].Overrides[135] = 3;
        Tilesets[0].Overrides[136] = 6;
        Tilesets[0].Overrides[137] = 7;
        Tilesets[0].Overrides[138] = 12;
        Tilesets[0].Overrides[139] = 13;
        Tilesets[0].Overrides[140] = 10;
        Tilesets[0].Overrides[141] = 11;
        Tilesets[0].Overrides[142] = 22;
        Tilesets[0].Overrides[143] = 23;
        Tilesets[0].Overrides[145] = 32;
        Tilesets[0].Overrides[146] = 19;
        Tilesets[0].Overrides[147] = 17;
        Tilesets[0].Overrides[148] = 18;
        Tilesets[0].Overrides[149] = 14;
        Tilesets[0].Overrides[150] = 22;
        Tilesets[0].Overrides[151] = 23;
        Tilesets[0].Overrides[152] = 71;
        Tilesets[0].Overrides[153] = 72;
        Tilesets[0].Overrides[154] = 15;
        Tilesets[0].Overrides[155] = 16;
        Tilesets[0].Overrides[156] = 20;
        Tilesets[0].Overrides[157] = 21;
        Tilesets[0].Overrides[158] = 28;
        Tilesets[0].Overrides[159] = 29;
        Tilesets[0].Overrides[160] = 71;
        Tilesets[0].Overrides[161] = 72;
        Tilesets[0].Overrides[171] = 53;
        Tilesets[0].Overrides[172] = 57;
        Tilesets[0].Overrides[173] = 55;
        Tilesets[0].Overrides[174] = 26;
        Tilesets[0].Overrides[175] = 27;
        Tilesets[0].Overrides[187] = 54;
        Tilesets[0].Overrides[188] = 58;
        Tilesets[0].Overrides[189] = 56;
        Tilesets[0].Overrides[190] = 24;
        Tilesets[0].Overrides[191] = 25;
        Tilesets[0].Overrides[192] = 59;
        Tilesets[0].Overrides[193] = 63;
        Tilesets[0].Overrides[194] = 61;
        Tilesets[0].Overrides[195] = 65;
        Tilesets[0].Overrides[196] = 69;
        Tilesets[0].Overrides[197] = 67;
        Tilesets[0].Overrides[198] = 33;
        Tilesets[0].Overrides[199] = 34;
        Tilesets[0].Overrides[200] = 41;
        Tilesets[0].Overrides[201] = 42;
        Tilesets[0].Overrides[202] = 37;
        Tilesets[0].Overrides[203] = 38;
        Tilesets[0].Overrides[204] = 47;
        Tilesets[0].Overrides[205] = 48;
        Tilesets[0].Overrides[206] = 51;
        Tilesets[0].Overrides[207] = 52;
        Tilesets[0].Overrides[208] = 60;
        Tilesets[0].Overrides[209] = 64;
        Tilesets[0].Overrides[210] = 62;
        Tilesets[0].Overrides[211] = 66;
        Tilesets[0].Overrides[212] = 70;
        Tilesets[0].Overrides[213] = 68;
        Tilesets[0].Overrides[214] = 35;
        Tilesets[0].Overrides[215] = 36;
        Tilesets[0].Overrides[216] = 43;
        Tilesets[0].Overrides[217] = 44;
        Tilesets[0].Overrides[218] = 39;
        Tilesets[0].Overrides[219] = 40;
        Tilesets[0].Overrides[220] = 49;
        Tilesets[0].Overrides[221] = 50;
        Tilesets[0].Overrides[222] = 45;
        Tilesets[0].Overrides[223] = 46;
        Tilesets[0].Overrides[228] = 41;
        Tilesets[0].Overrides[229] = 42;
        Tilesets[0].Overrides[230] = 45;
        Tilesets[0].Overrides[231] = 65;
        Tilesets[0].Overrides[232] = 69;
        Tilesets[0].Overrides[233] = 67;
        Tilesets[0].Overrides[234] = 47;
        Tilesets[0].Overrides[235] = 48;
        Tilesets[0].Overrides[236] = 52;
        Tilesets[0].Overrides[237] = 59;
        Tilesets[0].Overrides[238] = 63;
        Tilesets[0].Overrides[239] = 61;
        Tilesets[0].Overrides[244] = 43;
        Tilesets[0].Overrides[245] = 44;
        Tilesets[0].Overrides[246] = 46;
        Tilesets[0].Overrides[247] = 66;
        Tilesets[0].Overrides[248] = 70;
        Tilesets[0].Overrides[249] = 68;
        Tilesets[0].Overrides[250] = 49;
        Tilesets[0].Overrides[251] = 50;
        Tilesets[0].Overrides[252] = 51;
        Tilesets[0].Overrides[253] = 60;
        Tilesets[0].Overrides[254] = 64;
        Tilesets[0].Overrides[255] = 62;


        boolean type = true;

        /* Question blocks */
        for (int BlockIdx = 0; BlockIdx < 14; BlockIdx++)
            Tilesets[0].Overrides[BlockIdx + 64] = (short) (BlockIdx + (type ? 89 : 73));

        Tilesets[0].Overrides[48] = (short) (type ? 103 : 87);
        Tilesets[0].Overrides[49] = (short) (type ? 104 : 88);

        /* Brick blocks */
        for (int BlockIdx = 0; BlockIdx < 9; BlockIdx++)
            Tilesets[0].Overrides[BlockIdx + 80] = (short) (BlockIdx + (type ? 114 : 105));

        /* Invisible blocks */
        for (int BlockIdx = 0; BlockIdx < 6; BlockIdx++)
            Tilesets[0].Overrides[BlockIdx + 176] = (short) (BlockIdx + (type ? 129 : 123));

        //TODO FIXME

        /*
         // Enable notes for the normal tileset
         Tilesets[0].UseNotes = true;
         if (Rom.UserInfo.descriptions.containsKey(65535))
         Tilesets[0].ObjNotes = Rom.UserInfo.descriptions[65535].ToArray();
         else
         Tilesets[0].ObjNotes = GetDescriptions(LanguageManager.GetArrayList("ObjNotes"));
         if (Rom.UserInfo.descriptions.containsKey(TilesetID))
         {
         Tilesets[1].ObjNotes = Rom.UserInfo.descriptions[TilesetID].ToArray();;
         Tilesets[1].UseNotes = true;
         }
         if (Rom.UserInfo.descriptions.containsKey(65534))
         {
         Tilesets[2].ObjNotes = Rom.UserInfo.descriptions[65534].ToArray();
         Tilesets[2].UseNotes = true;
         }*/
    }

    public void LoadTileset1(short tilesetId)
    {
        File gfxFile = rom.getFileFromTable(NSMBRom.Table.Table_TS_NCG, tilesetId);
        File palFile = rom.getFileFromTable(NSMBRom.Table.Table_TS_NCL, tilesetId);
        File map16File = rom.getFileFromTable(NSMBRom.Table.Table_TS_PNL, tilesetId);
        File objFile = rom.getFileFromTable(NSMBRom.Table.Table_TS_UNT, tilesetId);
        File objIndexFile = rom.getFileFromTable(NSMBRom.Table.Table_TS_UNT_HD, tilesetId);
        File tileBehaviorFile = rom.getFileFromTable(NSMBRom.Table.Table_TS_CHK, tilesetId);

        Tilesets[1] = new NSMBTileset(gfxFile, palFile, map16File, objFile, objIndexFile, tileBehaviorFile, false, 1);
    }

    public static String[] GetDescriptions(ArrayList<String> contents)
    {
        //TODO FIXME
        /*
         String[] descriptions = new String[256];
         for (int NoteIdx = 0; NoteIdx < contents.size(); NoteIdx++)
         {
         if (contents[NoteIdx] == "")
         continue;

         int equalPos = contents[NoteIdx].IndexOf('=');
         if (equalPos != -1)
         {
         int ObjTarget = int.Parse
         (contents[NoteIdx].SubString(0, equalPos)
         );
         descriptions[ObjTarget] = contents[NoteIdx].SubString(equalPos + 1);
         }
         }
         return descriptions;*/
        return null;
    }
}
