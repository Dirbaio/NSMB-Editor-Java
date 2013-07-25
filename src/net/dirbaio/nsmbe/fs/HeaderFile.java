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

public class HeaderFile extends PhysicalFile
{

    public HeaderFile(Filesystem parent, Directory parentDir)
    {
        super(parent, parentDir, -8, "header.bin", 0, 0x4000);
    }

    public void UpdateCRC16()
    {
        byte[] contents = getContents();
        byte[] header = new byte[0x15E];
        System.arraycopy(contents, 0, header, 0, 0x15E);

        int crc16 = CRC16.calc(header);
        setUshortAt(0x15E, crc16);
    }
    /*
     //This is kind of a hack.
     public override void endEditInline(InlineFile f)
     {
     base.endEditInline(f);
     UpdateCRC16();
     }
     */
}
