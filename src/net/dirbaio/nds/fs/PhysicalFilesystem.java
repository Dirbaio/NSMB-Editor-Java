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

public abstract class PhysicalFilesystem extends Filesystem
{

    protected FilesystemSource source;
    protected File freeSpaceDelimiter;
    protected int fileDataOffset;

    public int getFileDataOffset()
    {
        return fileDataOffset;
    }

    protected PhysicalFilesystem(FilesystemSource fs)
    {
        this.source = fs;
    }

    //Tries to find LEN bytes of continuous unused space AFTER the freeSpaceDelimiter (usually fat or fnt)
    public int findFreeSpace(int len, int align)
    {
        Collections.sort(allFiles);

        PhysicalFile bestSpace = null;
        int bestSpaceLeft = Integer.MAX_VALUE;
        int bestSpaceBegin = -1;

        for (int i = allFiles.indexOf(freeSpaceDelimiter); i < allFiles.size() - 1; i++)
        {
            PhysicalFile a = (PhysicalFile) allFiles.get(i);
            PhysicalFile b = (PhysicalFile) allFiles.get(i + 1);

            int spBegin = a.fileBegin + a.fileSize; //- 1 + 1;
            spBegin = alignUp(spBegin, align);

            int spEnd = b.fileBegin;
            spEnd = alignDown(spEnd, align);

            int spSize = spEnd - spBegin;

            if (spSize >= len)
            {
                int spLeft = spSize - len;
                if (spLeft < bestSpaceLeft)
                {
                    bestSpaceLeft = spLeft;
                    bestSpace = a;
                    bestSpaceBegin = spBegin;
                }
            }
        }

        if (bestSpace != null)
            return bestSpaceBegin;
        else
        {
            PhysicalFile last = (PhysicalFile) allFiles.get(allFiles.size() - 1);
            return alignUp(last.fileBegin + last.fileSize, align);
        }
    }

    public void moveAllFiles(PhysicalFile first, int firstOffs)
    {
        Collections.sort(allFiles);

        int firstStart = first.fileBegin;
        int diff = (int) firstOffs - (int) firstStart;

        //I assume all the aligns are powers of 2, which should be safe.

        int maxAlign = 4;
        for (int i = allFiles.indexOf(first); i < allFiles.size(); i++)
        {
            int align = ((PhysicalFile) allFiles.get(i)).alignment;
            if (align > maxAlign)
                maxAlign = align;
        }

        //To preserve the alignment of all the moved files
        if (diff % maxAlign != 0)
            diff += (int) (maxAlign - diff % maxAlign);


        int fsEnd = getFilesystemEnd();
        int toCopy = (int) fsEnd - (int) firstStart;


        source.seek(firstStart);
        byte[] data = source.read(toCopy);
        source.seek(firstStart + diff);
        source.write(data);

        for (int i = allFiles.indexOf(first); i < allFiles.size(); i++)
            ((PhysicalFile) allFiles.get(i)).fileBegin += diff;
        for (int i = allFiles.indexOf(first); i < allFiles.size(); i++)
            ((PhysicalFile) allFiles.get(i)).saveOffsets();
    }

    @Override
    public void close()
    {
        source.close();
    }

    @Override
    public void save()
    {
        source.save();
    }

    public int getFilesystemEnd()
    {
        Collections.sort(allFiles);
        PhysicalFile lastFile = (PhysicalFile) allFiles.get(allFiles.size() - 1);
        int end = lastFile.fileBegin + lastFile.fileSize;
        return end;
    }
}
