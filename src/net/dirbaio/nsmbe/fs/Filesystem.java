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
package net.dirbaio.nsmbe.fs;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Filesystem
{

    public List<File> allFiles = new ArrayList<>();
    public List<Directory> allDirs = new ArrayList<>();
    protected Map<Integer, File> filesById = new HashMap<>();
    protected Map<Integer, Directory> dirsById = new HashMap<>();
    public Directory mainDir;

    public File getFileById(int id)
    {
        return filesById.get(id);
    }

    public File getFileByName(String name)
    {
        for (File f : allFiles)
            if (f.getName().equals(name))
                return f;

        return null;
    }
    /*
     public Directory getDirByPath(string path)
     {
     string[] shit = path.Split(new char[] { '/' });
     Directory dir = mainDir;
     for (int i = 0; i < shit.Length; i++)
     {
     Directory newDir = null;
     foreach(Directory d in dir.childrenDirs)
     if(d.name == shit[i])
     {
     newDir = d;
     break;
     }
     if(newDir == null) return null;

     dir = newDir;
     }
     return dir;
     }*/

    protected void addFile(File f)
    {
        allFiles.add(f);
        if (filesById.containsKey(f.getId()))
            throw new RuntimeException("Duplicate file ID");
        filesById.put(f.getId(), f);
    }

    protected void addDir(Directory d)
    {
        allDirs.add(d);
        if (dirsById.containsKey(d.getId()))
            throw new RuntimeException("Duplicate dir ID");
        dirsById.put(d.getId(), d);
    }

    public void fileMoved(File f)
    {
    }

    //Saving and closing
    public void save()
    {
    }

    public void close()
    {
    }

    //Utility methods that should go elsewhere
    public int alignUp(int what, int align)
    {
        if (what % align != 0)
            what += align - what % align;
        return what;
    }

    public int alignDown(int what, int align)
    {
        what -= what % align;
        return what;
    }
    /*
     public int readUInt()
     {
     uint res = 0;
     for (int i = 0; i < 4; i++)
     {
     res |= (uint)s.ReadByte() << 8 * i;
     }
     return res;
     }*/
}
