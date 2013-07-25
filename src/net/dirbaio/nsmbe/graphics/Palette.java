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
package net.dirbaio.nsmbe.graphics;

import net.dirbaio.nsmbe.fs.AlreadyEditingException;
import net.dirbaio.nsmbe.util.Util;

public abstract class Palette
{

    public int[] pal;

    public Palette()
    {
    }

    public abstract void beginEdit() throws AlreadyEditingException;

    public abstract void save();

    public abstract void endEdit();

    public int getClosestColor(int c)
    {
        if (c == 0)
            return 0;

        int bestInd = 0;
        float bestDif = Util.colorDiff(pal[0], c);

        for (int i = 0; i < pal.length; i++)
        {
            float d = Util.colorDiff(pal[i], c);
            if (d < bestDif)
            {
                bestDif = d;
                bestInd = i;
            }
        }

        return bestInd;
    }

    public int getColorSafe(int ind)
    {
        if (ind >= pal.length)
            return 0xFFFF00FF; //pink

        return pal[ind];
    }
}
