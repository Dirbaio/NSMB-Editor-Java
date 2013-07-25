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

import net.dirbaio.nds.util.ArrayReader;

public class NitroFilesystem extends PhysicalFilesystem
{

    public PhysicalFile fatFile, fntFile;

    public NitroFilesystem(FilesystemSource s)
    {
        super(s);

        mainDir = new Directory(this, null, true, "FILESYSTEM [" + s + "]", -100);
        load();
    }

    public void load()
    {
        addDir(mainDir);

        addFile(fntFile);
        mainDir.childrenFiles.add(fntFile);
        addFile(fatFile);
        mainDir.childrenFiles.add(fatFile);

        freeSpaceDelimiter = fntFile;

        //read the fnt
        ArrayReader fnt = new ArrayReader(fntFile.getContents());

        loadDir(fnt, "root", 0xF000, mainDir);
    }

    private void loadDir(ArrayReader fnt, String dirName, int dirID, Directory parent)
    {
        fnt.savePos();
        fnt.seek(8 * (dirID & 0xFFF));
        int subTableOffs = fnt.readInt();

        int fileID = fnt.readShort();

        //Crappy hack for MKDS course .carc's. 
        //Their main dir starting ID is 2, which is weird...
        //  if (parent == mainDir) fileID = 0; 

        Directory thisDir = new Directory(this, parent, false, dirName, dirID);
        addDir(thisDir);
        parent.childrenDirs.add(thisDir);

        fnt.seek((int) subTableOffs);
        while (true)
        {
            byte data = fnt.readByte();
            int len = data & 0x7F;
            boolean isDir = (data & 0x80) != 0;
            if (len == 0)
                break;
            String name = fnt.readString(len);

            if (isDir)
            {
                int subDirID = fnt.readShort();
                loadDir(fnt, name, subDirID, thisDir);
            } else
            {
                loadFile(name, fileID, thisDir);
                fileID++;
            }
        }
        fnt.loadPos();
    }

    protected void loadNamelessFiles(Directory parent)
    {
        boolean ok = true;
        for (int i = 0; i < fatFile.fileSize / 8; i++)
            if (getFileById(i) == null)
                ok = false;

        if (ok)
            return;

        Directory d = new Directory(this, parent, true, "Unnamed files", -94);
        parent.childrenDirs.add(d);
        allDirs.add(d);

        for (int i = 0; i < fatFile.fileSize / 8; i++)
            if (getFileById(i) == null)
                loadFile("File " + i, i, d);
    }

    protected File loadFile(String fileName, int fileID, Directory parent)
    {
        int beginOffs = fileID * 8;
        int endOffs = fileID * 8 + 4;
        File f = new PhysicalFile(this, parent, fileID, fileName, fatFile, beginOffs, endOffs);
        parent.childrenFiles.add(f);
        addFile(f);
        return f;
    }
}
