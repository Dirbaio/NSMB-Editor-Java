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

import net.dirbaio.nds.util.CompressionType;
import net.dirbaio.nds.util.LZ;

public class LZFile extends FileWithLock
{
    private File parentFile;
    private CompressionType comp;

    public LZFile(File parent, CompressionType ct)
    {
        name = parent.name;
        parentFile = parent;
        comp = ct;

        if (comp == CompressionType.None)
            fileSize = parent.fileSize;
        else if (comp == CompressionType.Lz)
            fileSize = LZ.getDecompressedSize(parent.getInterval(0, 4));
        else if (comp == CompressionType.LzWithHeader)
            fileSize = LZ.getDecompressedSizeHeadered(parent.getInterval(0, 8));
        else
            throw new UnsupportedOperationException("Bad LZFile Type: " + comp);
    }

    @Override
    public byte[] getContents()
    {
        if (comp == CompressionType.None)
            return parentFile.getContents();
        else if (comp == CompressionType.Lz)
            return LZ.decompress(parentFile.getContents());
        else if (comp == CompressionType.LzWithHeader)
            return LZ.decompressHeadered(parentFile.getContents());
        else
            throw new UnsupportedOperationException("Bad LZFile Type: " + comp);
    }

    @Override
    public void replace(byte[] newFile, Object editor)
    {
        if (!isAGoodEditor(editor))
            throw new RuntimeException("NOT CORRECT EDITOR " + name);

        if (comp == CompressionType.None)
            parentFile.replace(newFile, this);
        else if (comp == CompressionType.Lz)
            parentFile.replace(LZ.compress(newFile), this);
        if (comp == CompressionType.LzWithHeader)
            parentFile.replace(LZ.compressHeadered(newFile), this);
        else
            throw new UnsupportedOperationException("Bad LZFile Type: " + comp);

        fileSize = newFile.length;
    }

    @Override
    public byte[] getInterval(int start, int end)
    {
        validateInterval(start, end);

        if (comp == CompressionType.None)
            return parentFile.getInterval(start, end);

        byte[] data = parentFile.getContents();
        if (comp == CompressionType.Lz)
            data = LZ.decompress(data);
        else if (comp == CompressionType.LzWithHeader)
            data = LZ.decompressHeadered(data);
        else
            throw new UnsupportedOperationException("Bad LZFile Type: " + comp);

        int len = end - start;
        byte[] thisdata = new byte[len];
        System.arraycopy(data, start, thisdata, 0, len);
        return thisdata;
    }

    @Override
    public void replaceInterval(byte[] newFile, int start)
    {
        validateInterval(start, start + newFile.length);

        if (comp == CompressionType.None)
            parentFile.replaceInterval(newFile, start);
        else
        {
            byte[] data = parentFile.getContents();
            if (comp == CompressionType.Lz)
                data = LZ.decompress(data);
            else if (comp == CompressionType.LzWithHeader)
                data = LZ.decompressHeadered(data);
            else
                throw new UnsupportedOperationException("Bad LZFile Type: " + comp);

            System.arraycopy(newFile, 0, data, start, newFile.length);

            if (comp == CompressionType.Lz)
                parentFile.replace(LZ.compress(data), this);
            else if (comp == CompressionType.LzWithHeader)
                parentFile.replace(LZ.compressHeadered(data), this);
        }
    }

    @Override
    public void editionStarted() throws AlreadyEditingException
    {
        parentFile.beginEdit(this);
    }

    @Override
    public void editionEnded()
    {
        parentFile.endEdit(this);
    }
}
