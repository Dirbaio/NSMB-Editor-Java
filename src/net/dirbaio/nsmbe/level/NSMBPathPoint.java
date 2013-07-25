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
package net.dirbaio.nsmbe.level;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.dirbaio.nsmbe.leveleditor.LevelEditorComponent;
import net.dirbaio.nsmbe.leveleditor.LevelEditorControl;
import net.dirbaio.nsmbe.util.ArrayReader;
import net.dirbaio.nsmbe.util.ArrayWriter;
import net.dirbaio.nsmbe.util.Resources;

public class NSMBPathPoint implements LevelItem
{

    public int X;
    public int Y;
    public int Unknown1;
    public int Unknown2;
    public int Unknown3;
    public int Unknown4;
    public int Unknown5;
    public int Unknown6;
    public NSMBPath parent;

    //LevelItem implementation.
    @Override
    public Rectangle getRect()
    {
        return new Rectangle(X, Y, 16, 16);
    }

    @Override
    public void setRect(Rectangle r)
    {
        X = r.x;
        Y = r.y;
    }

    @Override
    public Rectangle getRealRect()
    {
        return getRect();
    }

    @Override
    public boolean isResizable()
    {
        return false;
    }

    @Override
    public int getSnap()
    {
        return 1;
    }

    public NSMBPathPoint(NSMBPath p)
    {
        parent = p;
    }

    public NSMBPathPoint(NSMBPathPoint p)
    {
        X = p.X;
        Y = p.Y;
        parent = p.parent;
        Unknown1 = p.Unknown1;
        Unknown2 = p.Unknown2;
        Unknown3 = p.Unknown3;
        Unknown4 = p.Unknown4;
        Unknown5 = p.Unknown5;
        Unknown6 = p.Unknown6;
    }

    @Override
    public void render(Graphics2D g, LevelEditorComponent ed)
    {
        g.drawImage(Resources.get("pathpoint"), X + NSMBPath.XOffs, Y + NSMBPath.YOffs, null);

        boolean lol = true;
        int num = parent.points.indexOf(this);
        if (num == 0)
            g.setColor(Color.GREEN);
        if (num == parent.points.size() - 1)
            g.setColor(Color.RED);
        else
            lol = false;

        if (lol)
        {
            g.drawRect(X, Y, 16, 16);
            g.drawRect(X + 1, Y + 1, 14, 14);
        }

        g.setColor(Color.WHITE);
        g.drawString(num + "", X, Y);
    }

    public static NSMBPathPoint read(ArrayReader inp, NSMBPath parent)
    {
        NSMBPathPoint p = new NSMBPathPoint(parent);
        p.X = inp.readShort();
        p.Y = inp.readShort();
        p.Unknown1 = inp.readShort();
        p.Unknown2 = inp.readShort();
        p.Unknown3 = inp.readShort();
        p.Unknown4 = inp.readShort();
        p.Unknown5 = inp.readShort();
        p.Unknown6 = inp.readShort();
        return p;
    }

    void write(ArrayWriter outn)
    {
        outn.writeShort((short) X);
        outn.writeShort((short) Y);
        outn.writeShort((short) Unknown1);
        outn.writeShort((short) Unknown2);
        outn.writeShort((short) Unknown3);
        outn.writeShort((short) Unknown4);
        outn.writeShort((short) Unknown5);
        outn.writeShort((short) Unknown6);
    }
}
