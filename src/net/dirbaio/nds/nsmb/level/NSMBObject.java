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

public class NSMBObject implements LevelItem
{

    public int ObjNum;
    public int Tileset;
    public int X;
    public int Y;
    public int Width;
    public int Height;
    private int[][] CachedObj;
    private NSMBGraphics GFX;

    public NSMBObject()
    {
    }

    public NSMBObject(int ObjNum, int Tileset, int X, int Y, int Width, int Height, NSMBGraphics GFX)
    {
        this.ObjNum = ObjNum;
        this.Tileset = Tileset;
        this.GFX = GFX;
        this.X = X;
        this.Y = Y;
        this.Width = Width;
        this.Height = Height;
        CachedObj = new int[Width][Height];
        UpdateObjCache();
    }

    public NSMBObject(NSMBObject o) // clone conclassor: returns an identical object
    {
        this.ObjNum = o.ObjNum;
        this.Tileset = o.Tileset;
        this.GFX = o.GFX;
        this.X = o.X;
        this.Y = o.Y;
        this.Width = o.Width;
        this.Height = o.Height;
        CachedObj = new int[Width][Height];
        UpdateObjCache();
    }

    @Override
    public Rectangle getRect()
    {
        int snap = getSnap();
        return new Rectangle(X * snap, Y * snap, Width * snap, Height * snap);
    }

    public Rectangle getBlockRect()
    {
        int snap = 1;
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
        return 16;
    }

    public final void UpdateObjCache()
    {
        if (GFX == null)
            return;

        try
        {
            CachedObj = GFX.Tilesets[Tileset].RenderObject(ObjNum, Width, Height);
        } catch (ObjectRenderingException e)
        {
            e.printStackTrace();
        }
    }

//        public void Render(Graphics g, int XOffset, int YOffset, Rectangle Clip, float zoom)
    @Override
    public void render(Graphics2D g, LevelEditorComponent ed)
    {
//        //This method is really messy due to the quirky rendering of .NET
//        //I need to do a lot of hacks to get objects rendered correctly with
//        //high and low zoom.
//
//        if (badObject)
//        {
//            g.DrawRectangle(new Pen(Color.Red, 4), new Rectangle(X * 16, Y * 16, Width * 16, Height * 16));
//            g.DrawLine(new Pen(Color.Red, 4), new Point(X * 16, (Y + Height) * 16), new Point((X + Width) * 16, (Y) * 16));
//            g.DrawLine(new Pen(Color.Red, 4), new Point((X + Width) * 16, (Y + Height) * 16), new Point((X) * 16, (Y) * 16));
//            return;
//        }
//
//        if (ed.zoom > 1)
//        {
//            RectangleF srcRect = new RectangleF(0, 0, 16, 16);
//            RectangleF destRect = new RectangleF(X << 4, Y << 4, 16, 16);
//
//            for (int xx = 0; xx < CachedObj.Getlength(0); xx++)
//                for (int yy = 0; yy < CachedObj.Getlength(1); yy++)
//                {
//                    int t = CachedObj[xx][yy];
//                    if (t < 0)
//                        continue;
//
//                    destRect.x = (X + xx) << 4;
//                    destRect.y = (Y + yy) << 4;
//
//                    srcRect.x = (t % 16) * 16 - 0.5f;
//                    srcRect.y = (t / 16) * 16 - 0.5f;
//
//                    g.drawImage(GFX.Tilesets[Tileset].Map16Buffer, destRect.x, destRect.y, srcRect, GraphicsUnit.Pixel);
//
//                    if (!GFX.Tilesets[Tileset].UseOverrides)
//                        continue;
//                    int t2 = GFX.Tilesets[Tileset].Overrides[t];
//                    if (t2 == -1)
//                        continue;
//                    if (t2 == 0)
//                        continue;
//
//                    srcRect.x = t2 * 16 - 0.5f;
//                    srcRect.y = 0 - 0.5f;
//
//                    g.drawImage(GFX.Tilesets[Tileset].OverrideBufferedImage, destRect.x, destRect.y, srcRect, GraphicsUnit.Pixel);
//                    //int overridenum = Array.IndexOf(NSMBTileset.BehaviorOverrides, GFX.Tilesets[Tileset].TileBehaviors[t]);
//                    //if (overridenum > -1)
//                    //    g.drawImage(Resources.get("tileoverrides2"), destRect.x, destRect.y, new Rectangle(overridenum * 16, 0, 16, 16), GraphicsUnit.Pixel);
//                }
//        }
//        else
//        {
//            Rectangle srcRect = new Rectangle(0, 0, 16, 16);
//            Rectangle destRect = new Rectangle(X << 4, Y << 4, 16, 16);
//
//            for (int xx = 0; xx < CachedObj.Getlength(0); xx++)
//                for (int yy = 0; yy < CachedObj.Getlength(1); yy++)
//                {
//                    int t = CachedObj[xx][yy];
//                    if (t < 0)
//                        continue;
//
//                    destRect.x = (X + xx) << 4;
//                    destRect.y = (Y + yy) << 4;
//
//                    srcRect.x = (t % 16) * 16;
//                    srcRect.y = (t / 16) * 16;
//
//                    g.drawImage(GFX.Tilesets[Tileset].Map16Buffer, destRect.x, destRect.y, srcRect, GraphicsUnit.Pixel);
//
//                    if (!GFX.Tilesets[Tileset].UseOverrides)
//                        continue;
//                    int t2 = GFX.Tilesets[Tileset].Overrides[t];
//                    if (t2 <= 0)
//                        continue;
//
//                    srcRect.x = t2 * 16;
//                    srcRect.y = 0;
//
//                    g.drawImage(GFX.Tilesets[Tileset].OverrideBufferedImage, destRect.x, destRect.y, srcRect, GraphicsUnit.Pixel);
//                    //int overridenum = Array.IndexOf(NSMBTileset.BehaviorOverrides, GFX.Tilesets[Tileset].TileBehaviors[t]);
//                    //if (overridenum > -1)
//                    //    g.drawImage(Resources.get("tileoverrides2"), destRect.x, destRect.y, new Rectangle(overridenum * 16, 0, 16, 16), GraphicsUnit.Pixel);
//                }
//        }
    }

    public void renderTilemap(int[][] tilemap, Rectangle bounds)
    {
        int xmin = Math.max(X, bounds.x);
        int ymin = Math.max(Y, bounds.y);
        int xmax = Math.min(X + Width, bounds.x + bounds.width);
        int ymax = Math.min(Y + Height, bounds.y + bounds.height);

        if (ObjNum == 0 && Tileset == 0)
            for (int xx = xmin; xx < xmax; xx++)
                for (int yy = ymin; yy < ymax; yy++)
                    tilemap[xx][yy] = 0;
        else
            for (int xx = xmin; xx < xmax; xx++)
                for (int yy = ymin; yy < ymax; yy++)
                {
                    int t = CachedObj[xx - X][yy - Y];
                    if (t < -2)
                        throw new RuntimeException("wtf");
                    if (t == -2)
                        continue;
                    else if (t == -1)
                        t = 0;
                    else if (Tileset == 1)
                        t += 256;
                    else if (Tileset == 2)
                        t += 256 * 4;

                    tilemap[xx][yy] = t;
                }
    }

    public void RenderPlain(Graphics2D g, int X, int Y)
    {

        Rectangle srcRect = new Rectangle(0, 0, 16, 16);
        Rectangle destRect = new Rectangle(X, Y, 16, 16);

        for (int xx = 0; xx < CachedObj.length; xx++)
            for (int yy = 0; yy < CachedObj[0].length; yy++)
            {
                int t = CachedObj[xx][yy];
                if (t < 0)
                    continue;

                destRect.x = X + xx * 16;
                destRect.y = Y + yy * 16;

                srcRect.x = (t % 16) * 16;
                srcRect.y = (t / 16) * 16;

                g.drawImage(GFX.Tilesets[Tileset].Map16Buffer, destRect.x, destRect.y, destRect.x+16, destRect.y+16, srcRect.x, srcRect.y, srcRect.x+16, srcRect.y+16, null);

                if (!GFX.Tilesets[Tileset].UseOverrides)
                    continue;
                int t2 = GFX.Tilesets[Tileset].Overrides[t];
                if (t2 <= 0)
                    continue;

                srcRect.x = t2 * 16;
                srcRect.y = 0;

                g.drawImage(GFX.Tilesets[Tileset].OverrideBufferedImage, destRect.x, destRect.y, destRect.x+16, destRect.y+16, srcRect.x, srcRect.y, srcRect.x+16, srcRect.y+16, null);
                //int overridenum = Array.IndexOf(NSMBTileset.BehaviorOverrides, GFX.Tilesets[Tileset].TileBehaviors[t]);
                //if (overridenum > -1)
                //    g.drawImage(Resources.get("tileoverrides2"), destRect.x, destRect.y, new Rectangle(overridenum * 16, 0, 16, 16), GraphicsUnit.Pixel);
            }
    }
    /*
     @Override
     public String toString()
     {
     return String.Format("OBJ:{0}:{1}:{2}:{3}:{4}:{5}", X, Y, Width, Height, Tileset, ObjNum);
     }

     public static NSMBObject FromString(String[] strs, int idx, NSMBGraphics gfx)
     {
     NSMBObject o = new NSMBObject(
     int.Parse
        
     (strs[6 + idx]
     ),
     int.Parse
     (strs[5 + idx]
     ),
     int.Parse
     (strs[1 + idx]
     ),
     int.Parse
     (strs[2 + idx]
     ),
     int.Parse
     (strs[3 + idx]
     ),
     int.Parse
     (strs[4 + idx]
     ),
     gfx
     );
     idx += 7;
     return o;
     }*/

    public static NSMBObject read(ArrayReader in, NSMBGraphics gfx)
    {
        NSMBObject o = new NSMBObject();
        int id = in.readShort();
        o.ObjNum = id & 0x0FFF;
        o.Tileset = (id >> 12) & 0xF;
        o.X = in.readShort();
        o.Y = in.readShort();
        o.Width = in.readShort();
        o.Height = in.readShort();
        o.GFX = gfx;

        return o;
    }

    public void write(ArrayWriter out)
    {
        out.writeShort((short) ((ObjNum & 0xFFF) | ((Tileset << 12) & 0xF000)));
        out.writeShort((short) X);
        out.writeShort((short) Y);
        out.writeShort((short) Width);
        out.writeShort((short) Height);
    }
}
