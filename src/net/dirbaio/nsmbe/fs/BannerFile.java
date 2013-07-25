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

import net.dirbaio.nsmbe.util.*;

public class BannerFile extends PhysicalFile
{

    public BannerFile(Filesystem parent, Directory parentDir, File headerFile)
    {
        super(parent, parentDir, -9, "banner.bin", 0x840, headerFile, 0x68);
    }
    //Hack to prevent stack overflow...
    private boolean updatingCrc = false;

    public void updateCRC16()
    {
        updatingCrc = true;
        byte[] contents = getContents();
        byte[] checksumArea = new byte[0x820];
        System.arraycopy(contents, 0x20, checksumArea, 0, 0x820);
        int checksum = CRC16.calc(checksumArea);
        setUshortAt(2, checksum);
        updatingCrc = false;
    }

    @Override
    public void editionEnded()
    {
        super.editionEnded();
        if (!updatingCrc)
            updateCRC16();
    }
}
