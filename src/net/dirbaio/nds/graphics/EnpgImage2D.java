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
package net.dirbaio.nds.graphics;

import net.dirbaio.nds.fs.File;

public class EnpgImage2D extends Image2D
{

    public EnpgImage2D(File f)
    {
        super(f, 256, false);
    }

    @Override
    public int getPixel(int x, int y)
    {
        int offs = x + y * 256;
        if (offs >= data.length)
            return 0;
        return data[offs] & 0xFF;
    }

    @Override
    public void setPixel(int x, int y, int c)
    {
        int offs = x + y * 256;

        if (offs >= data.length)
            return;
        data[offs] = (byte) c;
    }

    /*
     @Override
     public int getPixel(int x, int y)
     {
     int bx = x / 32;
     int by = y / 32;

     int offs = bx + by * (width / 32);
     if (offs < 0) return 0;
     offs *= 32 * 32;
     offs += x % 32 + 32 * (y % 32);
     if (offs >= data.length) return 0;
     return data[offs];
     }

     @Override
     public void setPixel(int x, int y, int c)
     {
     int bx = x / 32;
     int by = y / 32;

     int offs = bx + by * (width / 32) - tileOffset;
     if (offs < 0) return;

     offs *= 32 * 32;
     offs += x % 32 + 32 * (y % 32);
     if (offs >= data.length) return;
     data[offs] = (byte)c;
     }*/
}
