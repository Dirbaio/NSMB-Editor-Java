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

import java.util.ArrayList;
import java.util.List;

public class Directory
{

    private boolean isSystemFolderP;

    public boolean isSystemFolder()
    {
        return isSystemFolderP;
    }
    private String name;

    public String getName()
    {
        return name;
    }
    private int id;

    public int getId()
    {
        return id;
    }
    private Directory parentDir;

    public Directory getParentDir()
    {
        return parentDir;
    }
    public List<File> childrenFiles = new ArrayList<File>();
    public List<Directory> childrenDirs = new ArrayList<Directory>();
    private Filesystem parent;

    public Directory(Filesystem parent, Directory parentDir, boolean system, String name, int id)
    {
        this.parent = parent;
        this.parentDir = parentDir;
        this.isSystemFolderP = system;
        this.name = name;
        this.id = id;
    }

    public String getPath()
    {
        if (parentDir == null)
            return "FS";
        else
            return parentDir.getPath() + "/" + name;
    }
}
