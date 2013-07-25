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
package net.dirbaio.nds.nsmb.level;

import java.awt.Graphics2D;
import java.util.ArrayList;
import net.dirbaio.nds.nsmb.leveleditor.LevelEditorComponent;
import net.dirbaio.nds.util.ArrayReader;
import net.dirbaio.nds.util.ArrayWriter;

public class NSMBPath
{

    public static final int XOffs = 0;
    public static final int YOffs = 0;
    public int id;
    public ArrayList<NSMBPathPoint> points = new ArrayList<>();
    public boolean isProgressPath;

    public NSMBPath()
    {
    }

    public NSMBPath(NSMBPath path)
    {
        this.id = path.id;
        NSMBPathPoint newpt;
        for (NSMBPathPoint pt : path.points)
        {
            newpt = new NSMBPathPoint(pt);
            newpt.parent = this;
            this.points.add(newpt);
        }
    }

    public void write(ArrayWriter outp, ArrayWriter outn)
    {
        outp.writeShort((short) id);
        outp.writeShort((short) (outn.getPos() / 16));
        outp.writeShort((short) (points.size()));
        outp.writeShort((short) 0); //Unused values

        for (NSMBPathPoint p : points)
            p.write(outn);
    }

    public static NSMBPath read(ArrayReader inp, ArrayReader nodes, boolean isProgressPath)
    {
        NSMBPath p = new NSMBPath();
        p.isProgressPath = isProgressPath;

        p.id = inp.readShort();
        int row = inp.readShort();
        int len = inp.readShort();
        inp.skip(2); //unused values

        nodes.seek(row * 16);
        for (int i = 0; i < len; i++)
            p.points.add(NSMBPathPoint.read(nodes, p));
        return p;
    }

    public void render(Graphics2D g, LevelEditorComponent ed)
    {
        if (points.isEmpty())
            return;

        boolean first = true;
        int lx = 0;
        int ly = 0;
        for (NSMBPathPoint p : points)
        {
            if (!first)
                g.drawLine(lx, ly, p.X + 8 + XOffs, p.Y + 8 + YOffs);

            lx = p.X + 8 + XOffs;
            ly = p.Y + 8 + YOffs;
            first = false;
        }

        NSMBPathPoint fp = points.get(0);
        NSMBPathPoint lp = points.get(points.size() - 1);
        int num = 0;
        for (NSMBPathPoint p : points)
            p.render(g, ed);
    }

    public int getMinX()
    {
        int min = Integer.MAX_VALUE;
        for (NSMBPathPoint n : points)
            min = Math.min(min, n.X);
        return min;
    }

    public int getMinY()
    {
        int min = Integer.MAX_VALUE;
        for (NSMBPathPoint n : points)
            min = Math.min(min, n.Y);
        return min;
    }

    @Override
    public String toString()
    {
        return "asdf"; //TODO String.Format(LanguageManager.Get("NSMBPath", "ToString"), id, points.size());
    }
}
