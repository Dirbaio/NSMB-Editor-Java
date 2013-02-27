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

import java.io.*;

public abstract class FilesystemSource
{
    public abstract void seek(int pos);
    public abstract byte[] read(int len);
    public abstract void write(byte[] data);
    public abstract void save();
    public abstract void close();
    public abstract void setLength(int l);
    @Override
    public abstract String toString();
    
    public int readUInt()
    {
        byte[] d = read(4);
        return d[0] | (d[1]<<8) | (d[2]<<16) | (d[3]<<24);
    }
}
