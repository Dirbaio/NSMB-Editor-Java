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

public abstract class File implements Comparable<File>
{

    protected Filesystem fs;

    public Filesystem getFilesystem()
    {
        return fs;
    }
    private Directory parentDir;

    public Directory getParentDir()
    {
        return parentDir;
    }
    protected String name;

    public String getName()
    {
        return name;
    }
    private int id;

    public int getId()
    {
        return id;
    }

    public boolean isSystemFile()
    {
        return id < 0;
    }
    protected int fileSize;

    public int getFileSize()
    {
        return fileSize;
    }

    public File()
    {
    }

    public File(Filesystem parent, Directory parentDir, String name, int id)
    {
        this.fs = parent;
        this.parentDir = parentDir;
        this.name = name;
        this.id = id;
    }

    //File functions
    public abstract byte[] getContents();

    public abstract void replace(byte[] newFile, Object editor);

    public abstract byte[] getInterval(int start, int end);

    public abstract void replaceInterval(byte[] newFile, int start);

    //Handy read/write functions.
    public int getUintAt(int offset)
    {
        byte[] data = getInterval(offset, offset + 4);
        return (data[0] & 0xFF)
                | ((data[1] & 0xFF) << 8)
                | ((data[2] & 0xFF) << 16)
                | ((data[3] & 0xFF) << 24);
    }

    public int getUshortAt(int offset)
    {
        byte[] data = getInterval(offset, offset + 2);
        return (data[0] & 0xFF)
                | ((data[1] & 0xFF) << 8);
    }

    public byte getByteAt(int offset)
    {
        byte[] data = getInterval(offset, offset + 1);
        return (byte) (data[0]);
    }

    public void setUintAt(int offset, int val)
    {
        try
        {
            byte[] data =
            {
                (byte) (val), (byte) (val >> 8), (byte) (val >> 16), (byte) (val >> 24)
            };
            beginEditInterval(offset, offset + data.length);
            replaceInterval(data, offset);
            endEditInterval(offset, offset + data.length);
        } catch (AlreadyEditingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public void setUshortAt(int offset, int val)
    {
        try
        {
            byte[] data =
            {
                (byte) (val), (byte) (val >> 8)
            };
            beginEditInterval(offset, offset + data.length);
            replaceInterval(data, offset);
            endEditInterval(offset, offset + data.length);
        } catch (AlreadyEditingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public void setByteAt(int offset, int val)
    {
        try
        {
            byte[] data =
            {
                (byte) (val)
            };
            beginEditInterval(offset, offset + data.length);
            replaceInterval(data, offset);
            endEditInterval(offset, offset + data.length);
        } catch (AlreadyEditingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    //Lock/unlock functions
    public abstract void beginEdit(Object editor) throws AlreadyEditingException;

    public abstract void endEdit(Object editor);

    public abstract void beginEditInterval(int start, int end) throws AlreadyEditingException;

    public abstract void endEditInterval(int start, int end);

    public abstract boolean beingEditedBy(Object editor);

    //Misc functions
    public String getPath()
    {
        return parentDir.getPath() + "/" + name;
    }

    protected void validateInterval(int start, int end)
    {
        if (end < start)
            throw new RuntimeException("Wrong interval: end < start");
//			Console.Out.WriteLine("Checking interval "+start+" - " +end +" on "+name);
        if (start < 0 || start > fileSize)
            throw new RuntimeException("Wrong interval: start out of bounds");
        if (end < 0 || end > fileSize)
            throw new RuntimeException("Wrong interval: end out of bounds");
    }

    @Override
    public int compareTo(File arg0)
    {
        throw new UnsupportedOperationException("FU.");
    }

    @Override
    public String toString()
    {
        return name;
    }
}
