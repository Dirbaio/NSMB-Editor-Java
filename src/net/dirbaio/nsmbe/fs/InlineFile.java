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

public class InlineFile extends FileWithLock
{

    private int inlineOffs;
    private int inlineLen;
    private File parentFile;

    public InlineFile(File parent, int offs, int len, String name)
    {
        this.name = name;
        fileSize = len;
        parentFile = parent;
        inlineOffs = offs;
        inlineLen = len;
    }

    @Override
    public byte[] getContents()
    {
        return parentFile.getInterval(inlineOffs, inlineOffs + inlineLen);
    }

    @Override
    public void replace(byte[] newFile, Object editor)
    {
        if (!isAGoodEditor(editor))
            throw new RuntimeException("NOT CORRECT EDITOR " + name);
        if (newFile.length != inlineLen)
            throw new RuntimeException("Trying to resize an InlineFile: " + name);

        parentFile.replaceInterval(newFile, inlineOffs);
    }

    @Override
    public byte[] getInterval(int start, int end)
    {
        validateInterval(start, end);
        return parentFile.getInterval(inlineOffs + start, inlineOffs + end);
    }

    @Override
    public void replaceInterval(byte[] newFile, int start)
    {
        validateInterval(start, start + newFile.length);
        parentFile.replaceInterval(newFile, inlineOffs + start);
    }

    @Override
    public void editionStarted() throws AlreadyEditingException
    {
        parentFile.beginEditInterval(inlineOffs, inlineOffs + inlineLen);
    }

    @Override
    public void editionEnded()
    {
        parentFile.endEditInterval(inlineOffs, inlineOffs + inlineLen);
    }
}
