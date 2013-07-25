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

import net.dirbaio.nsmbe.NSMBRom;
import net.dirbaio.nsmbe.fs.AlreadyEditingException;
import net.dirbaio.nsmbe.fs.File;

public class InternalLevelSource extends LevelSource
{

    File levelFile;
    File BGDatFile;
    String filename;
    String levelname;

    public InternalLevelSource(NSMBRom rom, String filename, String levelname)
    {
        //TODO LOLOL
        levelFile = rom.fs.getFileByName("A01_1.bin");
        BGDatFile = rom.fs.getFileByName("A01_1_bgdat.bin");

        this.filename = filename;
        this.levelname = levelname;
    }

    @Override
    public LevelData getData()
    {
        return new LevelData(levelFile.getContents(), BGDatFile.getContents());
    }

    @Override
    public void setData(LevelData data)
    {
        levelFile.replace(data.LevelFile, this);
        BGDatFile.replace(data.BGDatFile, this);
    }

    @Override
    public String getName()
    {
        return levelname;
    }

    @Override
    public void open() throws AlreadyEditingException
    {
        levelFile.beginEdit(this);
        try
        {
            BGDatFile.beginEdit(this);
        } catch (AlreadyEditingException ex)
        {
            levelFile.endEdit(this);
            throw ex;
        }
    }

    @Override
    public void close()
    {
        levelFile.endEdit(this);
        BGDatFile.endEdit(this);
    }
}
