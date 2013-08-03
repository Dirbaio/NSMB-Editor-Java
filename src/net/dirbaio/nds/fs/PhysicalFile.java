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

import java.util.*;

public class PhysicalFile extends FileWithLock
{

    public boolean isSystemFile;
    //File that specifies where the file begins.
    protected File beginFile;
    protected int beginOffset;
    //File that specifies where the file ends OR the file size.
    protected File endFile;
    protected int endOffset;
    protected boolean endIsSize;
    //If true, file begin/size can't change at all.
    //TODO: Make sure these are set properly. I think they aren't.
    public boolean canChangeOffset = true;
    public boolean canChangeSize = true;
    //File begin offset
    public int fileBegin;

    public int getFileBegin()
    {
        return fileBegin;
    }
    public int alignment = 4; // word align by default

    //WARNING: Constructor mess below !!!
    public PhysicalFile(Filesystem parent, Directory parentDir, String name)
    {
        super(parent, parentDir, name, -1);
    }

    public PhysicalFile(Filesystem parent, Directory parentDir, int id, String name, File alFile, int alBeg, int alEnd)
    {
        super(parent, parentDir, name, id);
        this.beginFile = alFile;
        this.endFile = alFile;
        this.beginOffset = alBeg;
        this.endOffset = alEnd;
        refreshOffsets();
    }

    public PhysicalFile(Filesystem parent, Directory parentDir, int id, String name, File alFile, int alBeg, int alEnd, boolean endsize)
    {
        super(parent, parentDir, name, id);
        this.beginFile = alFile;
        this.endFile = alFile;
        this.beginOffset = alBeg;
        this.endOffset = alEnd;
        this.endIsSize = endsize;
        refreshOffsets();
    }

    public PhysicalFile(Filesystem parent, Directory parentDir, int id, String name, int alSize, File alFile, int alBeg)
    {
        super(parent, parentDir, name, id);
        this.beginFile = alFile;
        this.beginOffset = alBeg;
        this.fileSize = alSize;
        refreshOffsets();
    }

    public PhysicalFile(Filesystem parent, Directory parentDir, int id, String name, int alBeg, int alSize)
    {
        super(parent, parentDir, name, id);

        this.fileBegin = alBeg;
        this.fileSize = alSize;
        this.canChangeOffset = false;
        this.canChangeSize = false;
        refreshOffsets();
    }

    private int getFilesystemDataOffset()
    {
        return ((PhysicalFilesystem) fs).getFileDataOffset();
    }

    private File getFilesystemSource()
    {
        return ((PhysicalFilesystem) fs).source;
    }

    private void refreshOffsets()
    {
        if (beginFile != null)
            fileBegin = (int) beginFile.getUintAt(beginOffset) + getFilesystemDataOffset();

        if (endFile != null)
        {
            int end = (int) endFile.getUintAt(endOffset);
            if (endIsSize)
                fileSize = (int) end;
            else
                fileSize = (int) end + -fileBegin;
        }
    }

    void saveOffsets()
    {
        if (beginFile != null)
            beginFile.setUintAt(beginOffset, fileBegin - getFilesystemDataOffset());

        if (endFile != null)
            if (endIsSize)
                endFile.setUintAt(endOffset, fileSize);
            else
                endFile.setUintAt(endOffset, fileBegin + fileSize - getFilesystemDataOffset());
    }

    //Reading and writing!
    @Override
    public byte[] getInterval(int start, int end)
    {
        validateInterval(start, end);
        return getFilesystemSource().getInterval(fileBegin+start, fileBegin+end);
    }

    @Override
    public byte[] getContents()
    {
        return getFilesystemSource().getInterval(fileBegin, fileBegin+fileSize);
    }

    @Override
    public void replaceInterval(byte[] newFile, int start)
    {
        validateInterval(start, start + newFile.length);
        if (!isIntervalEditable(new Interval(start, start + newFile.length)))
            throw new RuntimeException("Incorrect interval: " + name);

        getFilesystemSource().replaceInterval(newFile, fileBegin+start);
    }

    //TODO: Clean up this mess.
    @Override
    public void replace(byte[] newFile, Object editor)
    {
        if (!isAGoodEditor(editor))
            throw new RuntimeException("NOT CORRECT EDITOR " + name);

        if (newFile.length != fileSize && !canChangeSize)
            throw new RuntimeException("TRYING TO RESIZE CONSTANT-SIZE FILE: " + name);

        int newStart = fileBegin;

        //if we insert a bigger file it might not fit in the current place
        if (newFile.length > fileSize)
            if (canChangeOffset && !(fs instanceof NarcFilesystem))
            {
                newStart = ((PhysicalFilesystem) fs).findFreeSpace(newFile.length, alignment);
                if (newStart % alignment != 0)
                    newStart += alignment - newStart % alignment;
            } else
            {
                //TODO: Keep the list always sorted in order to avoid stupid useless sorts.
                Collections.sort(fs.allFiles);
                if (!(fs.allFiles.indexOf(this) == fs.allFiles.size() - 1))
                {
                    PhysicalFile nextFile = (PhysicalFile) fs.allFiles.get(fs.allFiles.indexOf(this) + 1);
                    ((PhysicalFilesystem) fs).moveAllFiles(nextFile, fileBegin + newFile.length);
                }
            }
        //This is for keeping NARC filesystems compact. Sucks.
        else if (fs instanceof NarcFilesystem)
        {
            Collections.sort(fs.allFiles);
            if (!(fs.allFiles.indexOf(this) == fs.allFiles.size() - 1))
            {
                PhysicalFile nextFile = (PhysicalFile) fs.allFiles.get(fs.allFiles.indexOf(this) + 1);
                ((PhysicalFilesystem) fs).moveAllFiles(nextFile, fileBegin + newFile.length);
            }
        }

        //Stupid check.
        if (newStart % alignment != 0)
            System.out.println("Warning: File is not being aligned: " + name + ", at " + newStart);

        //write the file
        File s = getFilesystemSource();
        s.replaceInterval(newFile, newStart);

        //This should be handled in NarcFilesystem instead, in fileMoved (?)
        if (fs instanceof NarcFilesystem)
        {
            //TODO FIXOR
            PhysicalFile lastFile = (PhysicalFile) fs.allFiles.get(fs.allFiles.size() - 1);
            //s.setLength(lastFile.fileBegin + lastFile.fileSize + 16);
        }

        //update ending pos
        fileBegin = newStart;
        fileSize = newFile.length;
        saveOffsets();

        //Updates total used rom size in header, and/or other stuff.
        fs.fileMoved(this);
    }

    public void moveTo(int newOffs)
    {
        if (newOffs % alignment != 0)
            System.out.println("Warning: File is not being aligned: " + name + ", at " + newOffs);

        byte[] data = getContents();
        File s = getFilesystemSource();
        s.replaceInterval(data, newOffs);
        
        fileBegin = newOffs;
        saveOffsets();
    }

    @Override
    public int compareTo(File arg0)
    {
        if (arg0 instanceof PhysicalFile)
            return Integer.compare(fileBegin, ((PhysicalFile) arg0).fileBegin);
        else
            throw new UnsupportedOperationException("WHAT.");
    }
}
