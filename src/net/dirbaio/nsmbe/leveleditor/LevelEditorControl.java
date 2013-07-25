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
package net.dirbaio.nsmbe.leveleditor;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.io.Console;
import java.util.ArrayList;
import java.util.ResourceBundle.Control;
import javax.swing.JComponent;
import net.dirbaio.nsmbe.level.LevelItem;
import net.dirbaio.nsmbe.level.NSMBEntrance;
import net.dirbaio.nsmbe.level.NSMBGraphics;
import net.dirbaio.nsmbe.level.NSMBLevel;
import net.dirbaio.nsmbe.level.NSMBPath;
import net.dirbaio.nsmbe.level.NSMBSprite;
import net.dirbaio.nsmbe.level.NSMBView;

public class LevelEditorControl extends JComponent
{

    public float zoom = 1;
    private boolean drag = false;
//    public LevelMinimap minimap;
//    public MinimapControl minimapctrl;
    public UndoManager UndoManager;
    public Image bgImage;
    public int bgX, bgY;
    public int dsScreenX = -256, dsScreenY = -256;
    public boolean showDSScreen = false;
    public boolean showGrid = false;
    public boolean ignoreMouse = false;
    public Rectangle ViewablePixels;
    public Rectangle ViewableBlocks;
//    public LevelConfig config;

    public LevelEditorControl()
    {
        Ready = false;
        hScrollBar.Visible = false;
        vScrollBar.Visible = false;
        MouseWheel += new MouseEventHandler(DrawingArea_MouseWheel);
        DrawingArea.MouseWheel += new MouseEventHandler(DrawingArea_MouseWheel);
        this.SetStyle(ControlStyles.Selectable, true);
        //dragTimer.Start();
    }

    public void LoadUndoManager(ToolStripSplitButton Undo, ToolStripSplitButton Redo)
    {
        UndoManager = new UndoManager(Undo, Redo, this);
    }

    public void SetZoom(float nZoom)
    {
        this.zoom = nZoom;
        UpdateScrollbars();
        repaint();
    }
    public LevelEditor editor;

    public void Initialise(NSMBGraphics GFX, NSMBLevel Level, LevelEditor editor)
    {
        this.GFX = GFX;
        this.Level = Level;
        this.editor = editor;
        Ready = true;
        hScrollBar.Visible = true;
        vScrollBar.Visible = true;
        ViewablePixels = new Rectangle();
        ViewableBlocks = new Rectangle();
        UpdateScrollbars();
        remakeTileCache();
        DrawingArea.Invalidate();

    }

    public void SetEditionMode(EditionMode nm)
    {
        if (nm == mode)
            return;

        mode = nm;
        if (mode != null)
            mode.Refresh();

        DrawingArea.Invalidate();
    }

    private void UpdateScrollbars()
    {
        ViewablePixels.Width = (int) Math.Ceiling((float) DrawingArea.Width / zoom);
        ViewablePixels.Height = (int) Math.Ceiling((float) DrawingArea.Height / zoom);

        int xMax = 511 * 16;// - ViewablePixels.Width;
        int yMax = 255 * 16;// - ViewablePixels.Height;
        if (yMax < 0)
            yMax = 0;
        if (xMax < 0)
            xMax = 0;

        vScrollBar.Maximum = yMax;
        hScrollBar.Maximum = xMax;

        ViewablePixels.X = hScrollBar.Value;
        ViewablePixels.Y = vScrollBar.Value;

        ViewableBlocks.X = ViewablePixels.X / 16;
        ViewableBlocks.Y = ViewablePixels.Y / 16;
        ViewableBlocks.Width = (ViewablePixels.Width + 15) / 16 + 1;
        ViewableBlocks.Height = (ViewablePixels.Height + 15) / 16 + 1;
    }

    private void LevelEditorControl_Resize(object sender, EventArgs e)
    {
        UpdateScrollbars();
        DrawingArea.Invalidate();
    }

    private void hScrollBar_ValueChanged(object sender, EventArgs e)
    {
        UpdateScrollbars();
        DrawingArea.Invalidate();
    }

    private void vScrollBar_ValueChanged(object sender, EventArgs e)
    {
        UpdateScrollbars();
        DrawingArea.Invalidate();
    }

    private void DrawingArea_MouseWheel(object sender, MouseEventArgs e)
    {
        vScrollBar.Value = Math.max(vScrollBar.Minimum, Math.min(vScrollBar.Maximum - vScrollBar.LargeChange + 1, vScrollBar.Value - e.Delta / 4));
    }
    public NSMBGraphics GFX;
    public NSMBLevel Level;
    private boolean Ready;

    public enum ObjectType
    {

        Object,
        Sprite,
        Entrance,
        Path
    }
    public EditionMode mode = null;
    private int DragStartX;
    private int DragStartY;

    public void repaint()
    {
        DrawingArea.Invalidate();
    }
    int repa = 0;

    private void DrawingArea_Paint(object sender, PaintEventArgs e)
    {
        if (!Ready)
            return;
        minimap.Invalidate(true);
        minimapctrl.Invalidate(true);

        e.Graphics.InterpolationMode = System.Drawing.Drawing2D.InterpolationMode.NearestNeighbor;

        e.Graphics.ScaleTransform(zoom, zoom);
        e.Graphics.TranslateTransform(-ViewablePixels.X, -ViewablePixels.Y);

        // Render level.
        if (tileCache == null)
            return;

        updateTileCache();

        // RENDER PANNING BLOCKS GRID
        for (int x = ViewableBlocks.X / 16; x <= (ViewableBlocks.Width + ViewableBlocks.X) / 16; x++)
            for (int y = ViewableBlocks.Y / 16; y <= (ViewableBlocks.Height + ViewableBlocks.Y) / 16; y++)
            {
                boolean has = false;
                for (int xx = 0; xx < 16 && !has; xx++)
                    for (int yy = 0; yy < 16 && !has; yy++)
                        if (Level.levelTilemap[x * 16 + xx][y * 16 + yy] != 0)
                            has = true;
                if (!has)
                    e.Graphics.FillRectangle(Brushes.DarkSlateGray, x * 256, y * 256, 256, 256);
                e.Graphics.drawRect(Pens.LightGray, x * 256, y * 256, 256, 256);
            }

        if (bgImage != null)
            e.Graphics.drawImage(bgImage, bgX, bgY);

        if (showGrid)
        {
            for (int x = ViewableBlocks.X; x <= ViewableBlocks.Width + ViewableBlocks.X; x++)
                e.Graphics.drawLine(Pens.DarkGray, x * 16, ViewablePixels.Y, x * 16, ViewablePixels.Y + ViewablePixels.Height);
            for (int y = ViewableBlocks.Y; y <= ViewableBlocks.Height + ViewableBlocks.Y; y++)
                e.Graphics.drawLine(Pens.DarkGray, ViewablePixels.X, y * 16, ViewablePixels.X + ViewablePixels.Width, y * 16);
        }

        // RENDER PANNING BLOCKS GRID
        for (int x = ViewableBlocks.X / 16; x <= (ViewableBlocks.Width + ViewableBlocks.X) / 16; x++)
            for (int y = ViewableBlocks.Y / 16; y <= (ViewableBlocks.Height + ViewableBlocks.Y) / 16; y++)
                e.Graphics.drawRect(Pens.LightGray, x * 256, y * 256, 256, 256);
        {
            int x = ViewableBlocks.X;
            x -= x % ViewableBlocks.Width;
            x *= 16;
            int y = ViewableBlocks.Y;
            y -= y % ViewableBlocks.Height;
            y *= 16;
            int dx = ViewableBlocks.Width * 16;
            int dy = ViewableBlocks.Height * 16;
            e.Graphics.drawImage(tileCache, x, y);
            e.Graphics.drawImage(tileCache, x + dx, y);
            e.Graphics.drawImage(tileCache, x, y + dy);
            e.Graphics.drawImage(tileCache, x + dx, y + dy);
        }

        // And other stuff.
        for (NSMBSprite s : Level.Sprites)
            if (s.AlwaysDraw() || ViewablePixels.IntersectsWith(s.getRect()))
                s.render(e.Graphics, this);

        for (NSMBEntrance n : Level.Entrances)
            if (ViewablePixels.IntersectsWith(new Rectangle(n.x, n.y, n.width, n.height)))
                n.render(e.Graphics, this);

        for (NSMBView v : Level.Views)
            v.render(e.Graphics, this);
        for (NSMBView v : Level.Zones)
            v.render(e.Graphics, this);

        for (NSMBPath p : Level.Paths)
            p.render(e.Graphics, this, false);
        for (NSMBPath p : Level.ProgressPaths)
            p.render(e.Graphics, this, false);

        if (mode != null)
            mode.RenderSelection(e.Graphics);

        // DS Screen preview
        if (showDSScreen)
        {
            e.Graphics.drawRect(Pens.BlueViolet, dsScreenX, dsScreenY, 256, 192);
            e.Graphics.drawLine(Pens.BlueViolet, dsScreenX + 128, dsScreenY, dsScreenX + 128, dsScreenY + 192);
            e.Graphics.drawLine(Pens.BlueViolet, dsScreenX, dsScreenY + 96, dsScreenX + 256, dsScreenY + 96);
        }

        e.Graphics.TranslateTransform(hScrollBar.Value * 16, vScrollBar.Value * 16);
    }

    public boolean ProcessCmdKeyHack(ref Message
    
        msg, Keys keyData)
        {
            return ProcessCmdKey(ref 
        
        
        msg
        , keyData
    
    
    
    );
        }

        

        protected override

    boolean ProcessCmdKey(ref Message
    
        msg, Keys keyData)
        {
            Console.Out.WriteLine(keyData);
        if (keyData == (Keys.Control | Keys.X))
        {
            cut();
            return true;
        }
        if (keyData == (Keys.Control | Keys.C))
        {
            copy();
            return true;
        }
        if (keyData == (Keys.Control | Keys.V))
        {
            paste();
            return true;
        }
        if (keyData == (Keys.Control | Keys.S))
        {
            Level.Save();
            return true;
        }
        if (keyData == (Keys.Control | Keys.Z))
        {
            UndoManager.onUndoLast(null, null);
            return true;
        }
        if (keyData == (Keys.Control | Keys.Y))
        {
            UndoManager.onRedoLast(null, null);
            return true;
        }
        if (keyData == (Keys.Control | Keys.A))
        {
            mode.SelectAll();
            return true;
        }
        if (keyData == (Keys.Delete))
        {
            delete();
            return true;
        }
        if (keyData == (Keys.PageDown))
        {
            mode.lower();
            return true;
        }
        if (keyData == (Keys.PageUp))
        {
            mode.raise();
            return true;
        }
        int xDelta = 0, yDelta = 0;
        if (keyData == Keys.Up)
            yDelta -= 1;
        if (keyData == Keys.Down)
            yDelta += 1;
        if (keyData == Keys.Left)
            xDelta -= 1;
        if (keyData == Keys.Right)
            xDelta += 1;
        if (xDelta != 0 || yDelta != 0)
        {
            mode.MoveObjects(xDelta, yDelta);
            return true;
        }

        int newTab = -1;
        if (keyData == Keys.C)
            newTab = 0;
        if (keyData == Keys.O)
            newTab = 1;
        if (keyData == Keys.S)
            newTab = 2;
        if (keyData == Keys.E)
            newTab = 3;
        if (keyData == Keys.V)
            newTab = 4;
        if (keyData == Keys.Z)
            newTab = 5;
        if (keyData == Keys.P)
            newTab = 6;
        if (keyData == Keys.G)
            newTab = 7;

        if (newTab != -1)
        {
            editor.oem.tabs.SelectedTab = newTab;
            this.Focus(); //For some reason setting a new tab gives it focus
            return true;
        }

        return base.ProcessCmdKey(ref 
        
        
        msg
        , keyData
    
    
    
    );
        }

        boolean scrollingDrag = false;

    private void DrawingArea_MouseDown(object sender, MouseEventArgs e)
    {
        if (Ready)
        {
            if (e.Button == System.Windows.Forms.MouseButtons.Middle || e.Button == MouseButtons.Left && Control.ModifierKeys == Keys.Alt)
            {
                DragStartX = e.X;
                DragStartY = e.Y;
                scrollingDrag = true;
                return;
            }
            if (e.Button == MouseButtons.Left)
                drag = true;

            if (mode != null)
                mode.MouseDown((int) (e.X / zoom) + ViewablePixels.X, (int) (e.Y / zoom) + ViewablePixels.Y, e.Button);

            this.Focus();
        }
    }
    BufferedImage tileCache;
    Rectangle tileCacheRect;
    int[][] tilemapRendered = new int[512][256];

    public void updateTileCache()
    {
        updateTileCache(false);
    }

    public void updateTileCache(boolean repaintAll)
    {
        Rectangle oldCacheRect = tileCacheRect;
        tileCacheRect = ViewableBlocks;
//            BufferedImage oldCache = tileCache;

        if (oldCacheRect.Width != tileCacheRect.Width || oldCacheRect.Height != tileCacheRect.Height)
        {
            tileCache.Dispose();
            tileCache = new BufferedImage(ViewableBlocks.Width * 16, ViewableBlocks.Height * 16, System.Drawing.Imaging.PixelFormat.Format32bppPArgb);
            repaintAll = true;
        }
        Graphics g = Graphics.FromImage(tileCache);
//            if(oldCache != null && oldCacheRect != tileCacheRect)
//                g.drawImage(oldCache, (oldCacheRect.X - tileCacheRect.X) * 16, (oldCacheRect.Y - tileCacheRect.Y) * 16);

        Rectangle srcRect = new Rectangle(0, 0, 16, 16);
        Rectangle destRect = new Rectangle(0, 0, 16, 16);
        for (int xx = ViewableBlocks.X; xx < ViewableBlocks.X + ViewableBlocks.Width; xx++)
            for (int yy = ViewableBlocks.Y; yy < ViewableBlocks.Y + ViewableBlocks.Height; yy++)
            {
                if (xx < 0 || xx >= 512)
                    continue;
                if (yy < 0 || yy >= 256)
                    continue;
                int t = Level.levelTilemap[xx][yy];
                boolean eq = t == tilemapRendered[xx][yy];

                if (oldCacheRect.Contains(xx, yy) && eq && !repaintAll)
                    continue;
                tilemapRendered[xx][yy] = t;

                g.CompositingMode = CompositingMode.SourceCopy;
                destRect.X = (xx % ViewableBlocks.Width) * 16;
                destRect.Y = (yy % ViewableBlocks.Height) * 16;

                if (t == -1)
                {
                    g.FillRectangle(Brushes.Transparent, destRect);
                    continue;
                }

                int tileset = 0;
                if (t >= 256 * 4)
                {
                    t -= 256 * 4;
                    tileset = 2;
                } else if (t >= 256)
                {
                    t -= 256;
                    tileset = 1;
                }

                srcRect.X = (t % 16) * 16;
                srcRect.Y = (t / 16) * 16;
                g.drawImage(GFX.Tilesets[tileset].Map16Buffer, destRect.X, destRect.Y, srcRect, GraphicsUnit.Pixel);

                if (!GFX.Tilesets[tileset].UseOverrides)
                    continue;
                int t2 = GFX.Tilesets[tileset].Overrides[t];
                if (t2 == -1)
                    continue;
                if (t2 == 0)
                    continue;

                srcRect.X = t2 * 16;
                srcRect.Y = 0;

                g.CompositingMode = CompositingMode.SourceOver;
                //Note: overrides are drawn here too.
                g.drawImage(GFX.Tilesets[tileset].OverrideBufferedImage, destRect.X, destRect.Y, srcRect, GraphicsUnit.Pixel);
            }
    }

    private void remakeTileCache()
    {
        if (ViewableBlocks.Width == 0 || ViewableBlocks.Height == 0)
            return;

        tileCache = new BufferedImage(ViewableBlocks.Width * 16, ViewableBlocks.Height * 16);
        Graphics g = Graphics.FromImage(tileCache);

    }

    private void DrawingArea_SizeChanged(object sender, EventArgs e)
    {
        ignoreMouse = true;
        hScrollBar.LargeChange = DrawingArea.Width + 16;
        vScrollBar.LargeChange = DrawingArea.Height + 16;
        hScrollBar.Value = Math.max(0, Math.min(hScrollBar.Value, hScrollBar.Maximum - hScrollBar.LargeChange));
        vScrollBar.Value = Math.max(0, Math.min(vScrollBar.Value, vScrollBar.Maximum - vScrollBar.LargeChange));
    }

    private void DrawingArea_MouseLeave(object sender, EventArgs e)
    {
        dsScreenX = -256;
        dsScreenY = -256;
        repaint();
    }

    private void DrawingArea_MouseMove(object sender, MouseEventArgs e)
    {
        if (ignoreMouse)
        {
            ignoreMouse = false;
            return;
        }
        int DragSpeed = (int) Math.Ceiling(16 * zoom);

        int xx = (int) (e.X / zoom) + ViewablePixels.X;
        int yy = (int) (e.Y / zoom) + ViewablePixels.Y;

        if (scrollingDrag)
        {
            int NewX = e.X;
            int NewY = e.Y;
            int XDelta = (int) ((NewX - DragStartX) / zoom);
            int YDelta = (int) ((NewY - DragStartY) / zoom);
            if (XDelta != 0 || YDelta != 0)
            {
                Point NewPosition = new Point(ViewablePixels.X - XDelta, ViewablePixels.Y - YDelta);
                if (NewPosition.X < 0)
                    NewPosition.X = 0;
                if (NewPosition.X > hScrollBar.Maximum - hScrollBar.LargeChange)
                    NewPosition.X = hScrollBar.Maximum - hScrollBar.LargeChange;
                if (NewPosition.Y < 0)
                    NewPosition.Y = 0;
                if (NewPosition.Y > vScrollBar.Maximum - vScrollBar.LargeChange)
                    NewPosition.Y = vScrollBar.Maximum - vScrollBar.LargeChange;
                DragStartX = NewX;
                DragStartY = NewY;
                ScrollEditorPixel(NewPosition);
            }
        } else if ((e.Button == MouseButtons.Left || e.Button == MouseButtons.Right) && Ready && mode != null)
            mode.MouseDrag(xx, yy);
        else
            mode.MouseMove(xx, yy);

        if (showDSScreen)
        {
            dsScreenX = xx - 128;
            dsScreenY = yy - 96;
            repaint();
        }
    }

    public void EnsureBlockVisible(int X, int Y)
    {
        EnsurePixelVisible(X * 16, Y * 16);
    }

    public void EnsurePixelVisible(int X, int Y)
    {

        Point NewPosition = new Point(ViewablePixels.X, ViewablePixels.Y);
        if (X < ViewablePixels.X)
            NewPosition.X = Math.max(0, X - (ViewablePixels.Width / 2));
        if (X >= ViewablePixels.Right)
            NewPosition.X = Math.min(hScrollBar.Maximum, X - (ViewablePixels.Width / 2));
        if (Y < ViewablePixels.Y)
            NewPosition.Y = Math.max(0, Y - (ViewablePixels.Height / 2));
        if (Y >= ViewablePixels.Bottom)
            NewPosition.Y = Math.min(vScrollBar.Maximum, Y - (ViewablePixels.Height / 2));

        ScrollEditorPixel(NewPosition);
    }

    public void ScrollToObjects(ArrayList<LevelItem> objs)
    {
        for (LevelItem obj : objs)
            if (ViewablePixels.IntersectsWith(new Rectangle(obj.x, obj.y, obj.width, obj.height)))
                return;
        if (objs.size() > 0)
            EnsurePixelVisible(objs[0].x, objs[0].y);
    }

    public void ScrollEditor(Point NewPosition)
    {

        hScrollBar.Value = NewPosition.X * 16;
        vScrollBar.Value = NewPosition.Y * 16;
        UpdateScrollbars();
        DrawingArea.Invalidate();
    }

    public void ScrollEditorPixel(Point NewPosition)
    {
        ViewablePixels.Location = NewPosition;
        hScrollBar.Value = NewPosition.X;
        vScrollBar.Value = NewPosition.Y;
        UpdateScrollbars();
        DrawingArea.Invalidate();
    }

    public void SelectObject(Object no)
    {
        if (mode != null)
            mode.SelectObject(no);
    }

    public void cut()
    {
        copy();
        mode.DeleteObject();
    }
    public const String clipHeader = "NSMBeClip|";
    public const String clipFooter = "|";

    public void copy()
    {
        String str = mode.copy();
        if (str.length > 0)
            Clipboard.SetText(clipHeader + str + clipFooter);
    }

    public void paste()
    {
        String str = Clipboard.GetText().Trim();
        if (str.length > 0 && str.StartsWith(clipHeader) && str.EndsWith(clipFooter))
        {
            mode.paste(str.SubString(10, str.length - 11));
            mode.Refresh();
        }
    }

    public void delete()
    {
        mode.DeleteObject();
    }

    public void lower()
    {
        mode.lower();
    }

    public void raise()
    {
        mode.raise();
    }

    private void DrawingArea_MouseUp(object sender, MouseEventArgs e)
    {
        scrollingDrag = false;
        drag = false;
        mode.MouseUp();
    }

    private void dragTimer_Tick(object sender, EventArgs e)
    {
        Point mousePos = this.PointToClient(MousePosition);
        if ((MouseButtons == MouseButtons.Left) && drag)
        {
            if (mousePos.X < 0 && hScrollBar.Value > 0)
                hScrollBar.Value = Math.max(hScrollBar.Minimum, hScrollBar.Value + mousePos.X);
            if (mousePos.X > DrawingArea.Width && hScrollBar.Value < hScrollBar.Maximum)
                hScrollBar.Value = Math.min(hScrollBar.Maximum - hScrollBar.LargeChange + 1, hScrollBar.Value + mousePos.X - DrawingArea.Width);
            if (mousePos.Y < 0 && vScrollBar.Value > 0)
                vScrollBar.Value = Math.max(vScrollBar.Minimum, vScrollBar.Value + mousePos.Y);
            if (mousePos.Y > DrawingArea.Height && vScrollBar.Value < vScrollBar.Maximum)
                vScrollBar.Value = Math.min(vScrollBar.Maximum - vScrollBar.LargeChange + 1, vScrollBar.Value + mousePos.Y - DrawingArea.Height);

            mode.MouseDrag((int) (mousePos.X / zoom) + hScrollBar.Value, (int) (mousePos.Y / zoom) + vScrollBar.Value);
            UpdateScrollbars();
        }
    }

    public void GiveFocus()
    {
        DrawingArea.Focus();
    }
}
