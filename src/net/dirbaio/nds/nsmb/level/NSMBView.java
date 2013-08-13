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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.dirbaio.nds.nsmb.leveleditor.LevelEditorComponent;
import net.dirbaio.nds.util.ArrayReader;
import net.dirbaio.nds.util.ArrayWriter;
import net.dirbaio.nds.util.LanguageManager;

public class NSMBView implements LevelItem
{

    public int X;
    public int Y;
    public int Width = 256;
    public int Height = 192;
    public int Number;
//        public int Camera;
    public int Music;
    public int Unknown1;
    public int Unknown2;
    public int Unknown3;
    public int Lighting;
    public int FlagpoleID;
    public int CameraTop, CameraBottom;
    public int CameraTopSpin, CameraBottomSpin;
    public int CameraBottomStick;
    public boolean isZone = false;
    // save two dictionary lookups every repaint
    private String ViewDesc = LanguageManager.Get("NSMBView", "ViewDesc");
    private String ZoneDesc = LanguageManager.Get("NSMBView", "ZoneDesc");

    public NSMBView()
    {
    }

    public NSMBView(boolean isZone)
    {
        this.isZone = isZone;
    }
    
    public NSMBView(NSMBView v)
    {
        X = v.X;
        Y = v.Y;
        Width = v.Width;
        Height = v.Height;
        Number = v.Number;
        Music = v.Music;
        Unknown1 = v.Unknown1;
        Unknown2 = v.Unknown2;
        Unknown3 = v.Unknown3;
        Lighting = v.Lighting;
        FlagpoleID = v.FlagpoleID;
        isZone = v.isZone;
        CameraTop = v.CameraTop;
        CameraTopSpin = v.CameraTopSpin;
        CameraBottom = v.CameraBottom;
        CameraBottomSpin = v.CameraBottomSpin;
        CameraBottomStick = v.CameraBottomStick;
    }

    @Override
    public Rectangle getRect()
    {
        int snap = getSnap();
        return new Rectangle(X * snap, Y * snap, Width * snap, Height * snap);
    }

    @Override
    public void setRect(Rectangle r)
    {
        int snap = getSnap();
        X = r.x / snap;
        Y = r.y / snap;
        Width = r.width / snap;
        Height = r.height / snap;
    }

    @Override
    public Rectangle getRealRect()
    {
        return getRect();
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }

    @Override
    public int getSnap()
    {
        return 1;
    }

    @Override
    public void render(Graphics2D g, LevelEditorComponent ed)
    {
        if (isZone)
            g.setColor(new Color(0x80FF80));
        else
            g.setColor(new Color(0xB0C4DE));

        g.drawRect(X, Y, Width - 1, Height - 1);
        g.drawRect(X + 1, Y + 1, Width - 3, Height - 3);

        Rectangle ViewablePixels = ed.pixels;
        int vx = ViewablePixels.x;
        int vy = ViewablePixels.y;
        if (X + Width > vx && Y + Height > vy)
        {
            int numx = X;
            int numy = Y;

            if (numx < vx)
                numx = vx;
            if (numy < vy)
                numy = vy;
            if (isZone)
                numy += 16;
            g.drawString(GetDisplayString(), numx, numy);
        }
    }

    public String GetDisplayString()
    {
        return "LOL"; //TODO String.Format((isZone ? ZoneDesc : ViewDesc), Number);
    }

    //I think this is unused
    public void renderSelected(Graphics g)
    {
        if (isZone)
            g.setColor(new Color(0x80FF80));
        else
            g.setColor(new Color(0xFFFFFF));

        g.drawRect(X - 1, Y - 1, Width + 1, Height + 1);
        g.drawRect(X + 2, Y + 2, Width - 5, Height - 5);

        for (int x = X + 16 - X % 16; x < X + Width; x += 16)
            g.drawLine(x, Y, x, Y + Height);
        for (int y = Y + 16 - Y % 16; y < Y + Height; y += 16)
            g.drawLine(X, y, X + Width, y);
    }

    public void write(ArrayWriter outp, ArrayWriter cam, int camID)
    {
        outp.writeShort((short) X);
        outp.writeShort((short) Y);
        outp.writeShort((short) Width);
        outp.writeShort((short) Height);
        outp.writeByte((byte) Number);
        outp.writeByte((byte) camID);
        outp.writeByte((byte) Music);
        outp.writeByte((byte) Unknown1);
        outp.writeByte((byte) Unknown2);
        outp.writeByte((byte) Unknown3);
        outp.writeByte((byte) Lighting);
        outp.writeByte((byte) FlagpoleID);

        cam.writeInt(CameraTop);
        cam.writeInt(CameraBottom);
        cam.writeInt(CameraTopSpin);
        cam.writeInt(CameraBottomSpin);
        cam.writeShort((short) camID);
        cam.writeShort((short) CameraBottomStick);
        cam.writeInt(0); //This seems just padding.
    }

    public static NSMBView read(ArrayReader inp, ArrayReader cam)
    {
        NSMBView v = new NSMBView();

        v.X = inp.readShort();
        v.Y = inp.readShort();
        v.Width = inp.readShort();
        v.Height = inp.readShort();
        v.Number = inp.readByte();
        int camID = inp.readByte();
        v.Music = inp.readByte();
        v.Unknown1 = inp.readByte();
        v.Unknown2 = inp.readByte();
        v.Unknown3 = inp.readByte();
        v.Lighting = inp.readByte();
        v.FlagpoleID = inp.readByte();

        cam.seek(0);
        int camCount = (int) cam.available() / 24;
//            Console.Out.WriteLine("CamCount: " + camCount);
        int goodCam = -1;
        for (int i = 0; i < camCount; i++)
        {
            cam.seek(i * 24 + 16);
            int thisCam = cam.readShort();
//                Console.Out.WriteLine("Cam ID: " + thisCam);
            if (thisCam == camID)
            {
                goodCam = i;
                break;
            }
        }

        if (goodCam != -1)
        {
            cam.seek(goodCam * 24);
            v.CameraTop = cam.readInt();
            v.CameraBottom = cam.readInt();
            v.CameraTopSpin = cam.readInt();
            v.CameraBottomSpin = cam.readInt();
            cam.skip(2);
            v.CameraBottomStick = cam.readShort();
        }
        return v;
    }

    public void writeZone(ArrayWriter outp)
    {
        outp.writeShort((short) X);
        outp.writeShort((short) Y);
        outp.writeShort((short) Width);
        outp.writeShort((short) Height);
        outp.writeByte((byte) Number);
        outp.writeByte(0);
        outp.writeByte(0);
        outp.writeByte(0);
    }

    public static NSMBView readZone(ArrayReader inp)
    {
        NSMBView v = new NSMBView();

        v.X = inp.readShort();
        v.Y = inp.readShort();
        v.Width = inp.readShort();
        v.Height = inp.readShort();
        v.Number = inp.readByte();
        v.isZone = true;
        inp.skip(3);

        return v;
    }
}
