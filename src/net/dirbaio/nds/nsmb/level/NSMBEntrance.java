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
import java.awt.Rectangle;
import net.dirbaio.nds.nsmb.leveleditor.LevelEditorComponent;
import net.dirbaio.nds.util.ArrayReader;
import net.dirbaio.nds.util.ArrayWriter;
import net.dirbaio.nds.util.LanguageManager;
import net.dirbaio.nds.util.Resources;

public class NSMBEntrance implements LevelItem
{
    //public byte[] Data;

    public int X;
    public int Y;
    public int CameraX;
    public int CameraY;
    public int Number;
    public int DestArea;
    public int ConnectedPipeID;
    public int DestEntrance;
    public int Type;
    public int Settings;
    public int Unknown1;
    public int EntryView;
    public int Unknown2;

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

    public NSMBEntrance()
    {
    }

    public NSMBEntrance(NSMBEntrance e)
    {
        X = e.X;
        Y = e.Y;
        CameraX = e.CameraX;
        CameraY = e.CameraY;
        Number = e.Number;
        DestArea = e.DestArea;
        ConnectedPipeID = e.ConnectedPipeID;
        DestEntrance = e.DestEntrance;
        Type = e.Type;
        Settings = e.Settings;
        Unknown1 = e.Unknown1;
        EntryView = e.EntryView;
        Unknown2 = e.Unknown2;
    }

    @Override
    public void render(Graphics2D g, LevelEditorComponent ed)
    {

        int EntranceArrowColour = 0;
        // connected pipes have the grey blob (or did, it's kind of pointless)
            /*if (((Type >= 3 && Type <= 6) || (Type >= 16 && Type <= 19) || (Type >= 22 && Type <= 25)) && (Settings & 8) != 0) {
         EntranceArrowColour = 2;
         }*/
        // doors and pipes can be exits, so mark them as one if they're not 128
        if (((Type >= 2 && Type <= 6) || (Type >= 16 && Type <= 19) || (Type >= 22 && Type <= 25)) && (Settings & 128) == 0)
            EntranceArrowColour = 1;

        g.drawImage(Resources.get("entrances"), X, Y, X + 16, Y + 16,
                Math.min(Type, 25) * 16, EntranceArrowColour * 16, 16, 16, null);
    }

    @Override
    public String toString()
    {
        return Number + ": " + LanguageManager.GetArrayList("EntranceTypes")[Type] + " (" + X + ", " + Y + ")";
    }

    public static NSMBEntrance read(ArrayReader in)
    {
        NSMBEntrance e = new NSMBEntrance();
        e.X = in.readShort();
        e.Y = in.readShort();
        e.CameraX = in.readShort();
        e.CameraY = in.readShort();
        e.Number = in.readByte() & 0xFF;
        e.DestArea = in.readByte() & 0xFF;
        e.ConnectedPipeID = in.readByte() & 0xFF;
        in.readByte();
        e.DestEntrance = in.readByte() & 0xFF;
        in.readByte();
        e.Type = in.readByte() & 0xFF;
        e.Settings = in.readByte() & 0xFF;
        e.Unknown1 = in.readByte() & 0xFF;
        in.readByte();
        e.EntryView = in.readByte() & 0xFF;
        e.Unknown2 = in.readByte() & 0xFF;
        return e;
    }

    public void write(ArrayWriter out)
    {
        out.writeShort((short) X);
        out.writeShort((short) Y);
        out.writeShort((short) CameraX);
        out.writeShort((short) CameraY);
        out.writeByte(Number);
        out.writeByte(DestArea);
        out.writeByte(ConnectedPipeID);
        out.writeByte(0);
        out.writeByte(DestEntrance);
        out.writeByte(0);
        out.writeByte(Type);
        out.writeByte(Settings);
        out.writeByte(Unknown1);
        out.writeByte(0);
        out.writeByte(EntryView);
        out.writeByte(Unknown2);
    }
}
