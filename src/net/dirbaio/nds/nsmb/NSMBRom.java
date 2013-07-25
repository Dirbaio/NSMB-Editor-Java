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

import net.dirbaio.nds.Rom;
import net.dirbaio.nds.nsmb.binary.Overlay;
import net.dirbaio.nds.fs.AlreadyEditingException;
import net.dirbaio.nds.fs.File;
import net.dirbaio.nds.fs.Filesystem;
import net.dirbaio.nds.fs.InlineFile;

public class NSMBRom extends Rom
{

    public static final int tilesetCount = 76;
    public static final int bgCount = 76;
    public static final int fgCount = 76;
    public static final int spriteCount = 326;

    public enum Region
    {

        US,
        EU,
        JP,
        KR
    }
    private static final int[] fileIdOffsets =
    {
        131, 135, 131, 131, //File Offset (Overlay Count)
    };

    public enum Table
    {

        Table_TS_NCL,
        Table_TS_NCG,
        Table_TS_UNT_HD,
        Table_TS_UNT,
        Table_TS_CHK,
        Table_TS_ANIM_NCG,
        Table_TS_PNL,
        Table_Jyotyu_NCL,
        Table_FG_NCL,
        Table_FG_NCG,
        Table_FG_NSC,
        Table_BG_NCL,
        Table_BG_NCG,
        Table_BG_NSC,
        Table_Modifiers,
        Table_Sprite_ClassID,
    }
    private static final int[][] tableOffsets =
    {
        {
            0x31494, 0x30CA8, 0x30894, 0x30954
        }, //TS_NCL

        {
            0x30EA4, 0x306B8, 0x302A4, 0x30364
        }, //TS_NCG

        {
            0x2F8E4, 0x2F0F8, 0x2ECE4, 0x2EDA4
        }, //TS_UNT_HD

        {
            0x2FA14, 0x2F228, 0x2EE14, 0x2EED4
        }, //TS_UNT

        {
            0x2FB44, 0x2F358, 0x2EF44, 0x2F004
        }, //TS_CHK

        {
            0x2FC74, 0x2F488, 0x2F074, 0x2F134
        }, //TS_ANIM_NCG

        {
            0x316F4, 0x30F08, 0x30AF4, 0x30BB4
        }, //TS_PNL

        {
            0x30CD8, 0x304EC, 0x300D8, 0x30198
        }, //Jyotyu_NCL

        {
            0x315C4, 0x30DD8, 0x309C4, 0x30A84
        }, //FG_NCL

        {
            0x30FD4, 0x307E8, 0x303D4, 0x30494
        }, //FG_NCG

        {
            0x31104, 0x30918, 0x30504, 0x305C4
        }, //FG_NSC

        {
            0x31364, 0x30B78, 0x30764, 0x30824
        }, //BG_NCL

        {
            0x30D74, 0x30588, 0x30174, 0x30234
        }, //BG_NCG

        {
            0x31234, 0x30A48, 0x30634, 0x306F4
        }, //BG_NSC

        {
            0x2C930, 0x2BDF0, 0x2BD30, 0x2BDF0
        }, //Modifiers

        {
            0x29BD8, 0x00000, 0x00000, 0x00000
        }, //Sprite Class IDs
    };
    private static final int[] tableLengths =
    {
        tilesetCount, //TS_NCL
        tilesetCount, //TS_NCG
        tilesetCount, //TS_UNT_HD
        tilesetCount, //TS_UNT
        tilesetCount, //TS_CHK
        tilesetCount, //TS_ANIM_NCG
        tilesetCount, //TS_PNL
        bgCount, //Jyotyu_NCL

        fgCount, //FG_NCL
        fgCount, //FG_NCG
        fgCount, //FG_NSC

        bgCount, //BG_NCL
        bgCount, //BG_NCG
        bgCount, //BG_NSC

        spriteCount, //Modifiers
        spriteCount, //Sprite Class IDs
    };
    private static final int[] tableEntrySizes =
    {
        4, //TS_NCL
        4, //TS_NCG
        4, //TS_UNT_HD
        4, //TS_UNT
        4, //TS_CHK
        4, //TS_ANIM_NCG
        4, //TS_PNL
        1, //Jyotyu_NCL

        4, //FG_NCL
        4, //FG_NCG
        4, //FG_NSC

        4, //BG_NCL
        4, //BG_NCG
        4, //BG_NSC

        2, //Modifiers
        2, //Sprite Class IDs
    };

    public enum EmbeddedFile
    {

        File_Jyotyu_CHK,
    }
    private static final int[][] embeddedFileOffsets =
    {
        {
            0x2FDA4, 0x2F5B8, 0x2F1A4, 0x2FC74
        }, //Jyotyu_CHK
    };
    private static final int[] embeddedFileLengths =
    {
        0x400, //Jyotyu_CHK
    };
    private Region region;
    private BinaryTable[] tables;
    private InlineFile[] files;

    public NSMBRom(Filesystem fs) throws AlreadyEditingException
    {
        super(fs);

        Overlay ov0 = arm9ovs[0];
        ov0.decompress();
        File ovFile = ov0.getFile();

        switch (gamecode)
        {
            case "A2DE":
                region = Region.US;
                break;
            case "A2DP":
                region = Region.EU;
                break;
            case "A2DJ":
                region = Region.JP;
                break;
            case "A2DK":
                region = Region.KR;
                break;
            default:
                throw new RuntimeException("This is not a NSMB ROM! :(");
        }

        tables = new BinaryTable[Table.values().length];
        for (Table t : Table.values())
        {
            int offs = tableOffsets[t.ordinal()][region.ordinal()];
            int entrySize = tableEntrySizes[t.ordinal()];
            int len = tableLengths[t.ordinal()];
            InlineFile tf = new InlineFile(ovFile, offs, len * entrySize, t.toString());
            tables[t.ordinal()] = new BinaryTable(tf, entrySize);
        }

        files = new InlineFile[EmbeddedFile.values().length];
        for (EmbeddedFile f : EmbeddedFile.values())
        {
            int offs = embeddedFileOffsets[f.ordinal()][region.ordinal()];
            int len = embeddedFileLengths[f.ordinal()];
            files[f.ordinal()] = new InlineFile(ovFile, offs, len, f.toString());
        }
    }

    public BinaryTable getTable(Table t)
    {
        return tables[t.ordinal()];
    }

    public InlineFile getEmbeddedFile(EmbeddedFile f)
    {
        return files[f.ordinal()];
    }

    public int getFileIdOffset()
    {
        return fileIdOffsets[region.ordinal()];
    }

    public File getFileFromTable(Table t, int ind)
    {
        return fs.getFileById((getTable(t).getVal(ind) & 0xFFFF) + getFileIdOffset());
    }
}
