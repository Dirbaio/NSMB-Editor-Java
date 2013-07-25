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
package net.dirbaio.nsmbe.level.source;

import net.dirbaio.nsmbe.fs.AlreadyEditingException;
import net.dirbaio.nsmbe.fs.File;
import net.dirbaio.nsmbe.util.ArrayReader;
import net.dirbaio.nsmbe.util.ArrayWriter;
import net.dirbaio.nsmbe.util.LanguageManager;

public class LevelData
{

    public static final String fileHeader = "NSMBe4 Exported Level";
    public static final short version = 1;
    public byte[] LevelFile;
    public byte[] BGDatFile;

    public LevelData()
    {
    }

    public LevelData(byte[] LevelFile, byte[] BGDatFile)
    {
        this.LevelFile = LevelFile;
        this.BGDatFile = BGDatFile;
    }

    public LevelData(File LevelFile, File BGFile)
    {
        this.LevelFile = LevelFile.getContents();
        this.BGDatFile = BGFile.getContents();
    }

    public LevelData(byte[] data) throws LevelIOException
    {
        ArrayReader br = new ArrayReader(data);

        String Header = br.readString();
        if (!Header.equals(fileHeader))
            throw new LevelIOException(LanguageManager.Get("NSMBLevel", "InvalidFile"));

        int FileVersion = br.readShort();
        if (FileVersion > version)
            throw new LevelIOException(LanguageManager.Get("NSMBLevel", "OldVersion"));

        int LevelFileID = br.readShort();
        int BGDatFileID = br.readShort();
        LevelFile = br.readByteArray();
        BGDatFile = br.readByteArray();
    }

    public void WriteToFiles(File destLevelFile, File destBGFile) throws AlreadyEditingException
    {
        destLevelFile.beginEdit(this);

        try
        {
            destBGFile.beginEdit(this);
        } catch (AlreadyEditingException ex)
        {
            destLevelFile.endEdit(this);
            throw ex;
        }

        destLevelFile.replace(LevelFile, this);
        destLevelFile.endEdit(destBGFile);
        destBGFile.replace(BGDatFile, this);
        destBGFile.endEdit(destLevelFile);
    }

    public byte[] WriteToArray()
    {
        ArrayWriter out = new ArrayWriter();
        out.writeCSharpString(fileHeader);
        out.writeShort(version);
        out.writeShort((short) 0);
        out.writeShort((short) 0);
        out.writeByteArray(LevelFile);
        out.writeByteArray(BGDatFile);

        return out.getArray();
    }
}
