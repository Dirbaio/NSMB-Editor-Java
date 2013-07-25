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
package net.dirbaio.nds.fs;

public class NarcFilesystem extends NitroFilesystem
{

    public int fntOffset, fntSize;
    public int fatOffset, fatSize;

    public NarcFilesystem(File f) throws AlreadyEditingException
    {
        super(new FileFilesystemSource(f));
    }

    @Override
    public void load()
    {

        //I have to do some tricky offset calculations here ...
        fatOffset = 0x1C;
        source.seek(0x18); //number of files
        fatSize = source.readUInt() * 8;

        source.seek(fatSize + fatOffset + 4); //size of FNTB
        fntSize = source.readUInt() - 8; //do not include header
        fntOffset = fatSize + fatOffset + 8;

        fileDataOffset = fntSize + fntOffset + 8;
        fntFile = new PhysicalFile(this, mainDir, -2, "fnt.bin", fntOffset, fntSize);
        fatFile = new PhysicalFile(this, mainDir, -3, "fat.bin", fatOffset, fatSize);

        super.load();
        loadNamelessFiles(mainDir);
    }

    //TODO: Find a better method of saving. Maybe on-demand (a button)?
    @Override
    public void fileMoved(File f)
    {
        source.save();
    }
}
