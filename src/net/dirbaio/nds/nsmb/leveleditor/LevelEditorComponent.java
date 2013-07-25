/*
 * Copyright (C) 2013 dirbaio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.dirbaio.nds.nsmb.leveleditor;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JComponent;
import net.dirbaio.nds.nsmb.level.LevelItem;
import net.dirbaio.nds.nsmb.level.NSMBEntrance;
import net.dirbaio.nds.nsmb.level.NSMBLevel;
import net.dirbaio.nds.nsmb.level.NSMBObject;
import net.dirbaio.nds.nsmb.level.NSMBPath;
import net.dirbaio.nds.nsmb.level.NSMBPathPoint;
import net.dirbaio.nds.nsmb.level.NSMBSprite;
import net.dirbaio.nds.nsmb.level.NSMBView;
import net.dirbaio.nds.nsmb.leveleditor.actions.AddLvlItemAction;
import net.dirbaio.nds.nsmb.leveleditor.actions.AddPathNodeAction;
import net.dirbaio.nds.nsmb.leveleditor.actions.LowerLvlItemAction;
import net.dirbaio.nds.nsmb.leveleditor.actions.MoveResizeLvlItemAction;
import net.dirbaio.nds.nsmb.leveleditor.actions.RaiseLvlItemAction;
import net.dirbaio.nds.nsmb.leveleditor.actions.RemoveLvlItemAction;
import net.dirbaio.nds.util.Colors;
import net.dirbaio.nds.util.Resources;
import net.dirbaio.nds.util.Util;

public class LevelEditorComponent extends JComponent implements MouseListener, MouseMotionListener
{

    public NSMBLevel level;
    TilemapRenderer renderer;
    boolean showGrid = true;
    public Rectangle pixels, blocks;
    UndoManager undo;
    public boolean snapTo8Pixels = true;
    public boolean resizeHandles = true;
    boolean CloneMode, SelectMode;
    int dx, dy; //MouseDown position
    int lx, ly; //last position
    boolean CreateObj;
    NSMBObject newObj;
    MouseAction mouseAct = new MouseAction();
    int minBoundX, minBoundY; //the top left corner of the selected objects
    int maxBoundX, maxBoundY; //the top left corner of the selected objects
    int minSizeX, minSizeY; //the minimum size of all resizable objects.
    int selectionSnap; //The max snap in the selection :P
    Rectangle SelectionRectangle;
    ArrayList<LevelItem> SelectedObjects = new ArrayList<>();
    ArrayList<LevelItem> CurSelectedObjs = new ArrayList<>();
    boolean removing = false;

    public LevelEditorComponent(NSMBLevel l)
    {
        this.level = l;
        setPreferredSize(new Dimension(512 * 16, 256 * 16));
        setSize(new Dimension(512 * 16, 256 * 16));

        renderer = new TilemapRenderer(l.levelTilemap, l.GFX);
        undo = new UndoManager(this);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics gg)
    {
        Graphics2D g = (Graphics2D) gg;

        pixels = g.getClipBounds();
        blocks = new Rectangle(pixels.x / 16, pixels.y / 16, (pixels.width + 15) / 16, (pixels.height + 15) / 16);
        if (blocks.width == 512 || blocks.height == 256)
            return;


        // RENDER PANNING BLOCKS GRID
        for (int x = blocks.x / 16; x <= (blocks.width + blocks.x) / 16; x++)
            for (int y = blocks.y / 16; y <= (blocks.height + blocks.y) / 16; y++)
            {
                boolean has = false;
                for (int xx = 0; xx < 16 && !has; xx++)
                    for (int yy = 0; yy < 16 && !has; yy++)
                        if (level.levelTilemap[x * 16 + xx][y * 16 + yy] != 0)
                            has = true;
                if (has)
                    g.setColor(Colors.backgroundPannable);
                else
                    g.setColor(Colors.background);
                g.fillRect(x * 256, y * 256, 256, 256);
            }

//        if (bgImage != null)
//            g.drawImage(bgImage, bgX, bgY);

        if (showGrid)
        {
            g.setColor(Colors.smallGrid);
            for (int x = blocks.x; x <= blocks.width + blocks.x; x++)
                g.drawLine(x * 16, pixels.y, x * 16, pixels.y + pixels.height);
            for (int y = blocks.y; y <= blocks.height + blocks.y; y++)
                g.drawLine(pixels.x, y * 16, pixels.x + pixels.width, y * 16);
        }

        // RENDER PANNING BLOCKS GRID
        g.setColor(Colors.bigGrid);
        for (int x = blocks.x / 16; x <= (blocks.width + blocks.x) / 16; x++)
            for (int y = blocks.y / 16; y <= (blocks.height + blocks.y) / 16; y++)
                g.drawRect(x * 256, y * 256, 256, 256);

        //Render level tiles
        renderer.render(g, blocks);

        // And now level stuff
        for (NSMBSprite s : level.Sprites)
            if (s.AlwaysDraw() || pixels.intersects(s.getRect()))
                s.render(g, this);

        for (NSMBEntrance n : level.Entrances)
            if (pixels.intersects(n.getRect()))
                n.render(g, this);

        for (NSMBView v : level.Views)
            v.render(g, this);
        for (NSMBView v : level.Zones)
            v.render(g, this);

        for (NSMBPath p : level.Paths)
            p.render(g, this);
        for (NSMBPath p : level.ProgressPaths)
            p.render(g, this);

        RenderSelection(g);
        /*
         // DS Screen preview
         if (showDSScreen)
         {
         g.drawRect(Pens.BlueViolet, dsScreenX, dsScreenY, 256, 192);
         g.drawLine(Pens.BlueViolet, dsScreenX + 128, dsScreenY, dsScreenX + 128, dsScreenY + 192);
         g.drawLine(Pens.BlueViolet, dsScreenX, dsScreenY + 96, dsScreenX + 256, dsScreenY + 96);
         }*/
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();
        int buttons = e.getButton();
        int modifiers = e.getModifiersEx();

        //Right clicking creates a new object
        if (buttons == MouseEvent.BUTTON3)/*
             dx = x / 16;
             dy = y / 16;
             lx = x;
             ly = y;
             CreateObj = true;
             if (tabs.SelectedTab == 2) //The sprite tab
             {
             NSMBSprite newSprite = new NSMBSprite(level);
             newSprite.Type = tabs.sprites.getSelectedType();
             if (newSprite.Type == -1)
             return;
             newSprite.Data = new byte[6];
             newSprite.x = x;
             newSprite.y = y;
             undo.Do(new AddLvlItemAction(UndoManager.ObjToArrayList(newSprite)));
             SelectObject(newSprite);
             return;
             }
             newObj = new NSMBObject(tabs.objects.getObjectType(), tabs.objects.getTilesetNum(), dx, dy, 1, 1, level.GFX);
             undo.Do(new AddLvlItemAction(UndoManager.ObjToArrayList(newObj)));
             SelectObject(newObj);
             return;*/

            return;
        lx = x;
        ly = y;
        dx = x;
        dy = y;

        mouseAct = getActionAtPos(x, y);
        if (mouseAct.nodeType != CreateNode.None)
        {
            NSMBPathPoint pp = new NSMBPathPoint(mouseAct.node);
            int zIndex = pp.parent.points.indexOf(mouseAct.node);
            if (mouseAct.nodeType == CreateNode.After)
            {
                Rectangle r = pp.getRect();
                r.x += 16;
                pp.setRect(r);
                zIndex++;
            }
            else
            {
                Rectangle r = pp.getRect();
                r.x -= 16;
                pp.setRect(r);
            }
            undo.Do(new AddPathNodeAction(UndoManager.ObjToArrayList(pp), zIndex));
            SelectObject(pp);
        }
        else
        {
            SelectMode = false;
            // Resize with the shift key
            if ((modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0 && mouseAct.drag && mouseAct.vert == ResizeType.ResizeNone && mouseAct.hor == ResizeType.ResizeNone)
            {
                mouseAct.vert = ResizeType.ResizeEnd;
                mouseAct.hor = ResizeType.ResizeEnd;
            }
            if (!mouseAct.drag)
            {
                // Select an object
                findSelectedObjects(x, y, x, y, true, true);
                SelectMode = SelectedObjects.isEmpty();
            }
            else if (mouseAct.vert == ResizeType.ResizeNone && mouseAct.hor == ResizeType.ResizeNone)
            {
                ArrayList<LevelItem> selectedObjectsBack = new ArrayList<>();
                selectedObjectsBack.addAll(SelectedObjects);

                // Select an object
                findSelectedObjects(x, y, x, y, true, true);

                if (SelectedObjects.isEmpty())
                    SelectMode = true;
                else if (selectedObjectsBack.contains(SelectedObjects.get(0)))
                    SelectedObjects = selectedObjectsBack;
                UpdateSelectionBounds();
                repaint();
            }

            if (!SelectMode)
            {
                CloneMode = (modifiers & MouseEvent.CTRL_DOWN_MASK) != 0;
                lx -= selectionSnap / 2;
                ly -= selectionSnap / 2;
            }
        }
        repaint();

//        tabs.SelectObjects(SelectedObjects);
        UpdatePanel();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();

        //Resize the new object that was created by right-clicking.
        if (CreateObj && newObj != null)
        {
            Rectangle r = newObj.getRect();
            x = Math.max(0, x / 16);
            y = Math.max(0, y / 16);
            if (x == lx && y == ly)
                return;
            lx = x;
            ly = y;
            newObj.X = Math.min(lx, dx);
            newObj.Y = Math.min(ly, dy);
            newObj.Width = Math.abs(lx - dx) + 1;
            newObj.Height = Math.abs(ly - dy) + 1;
            newObj.UpdateObjCache();
            Rectangle.union(r, newObj.getRect(), r);
            level.repaintTilemap(r.x, r.y, r.width, r.height);
            repaint();
            return;
        }

        if (lx == x && ly == y) // don't clone objects if there instanceof no visible movement
            return;

        if (SelectMode)
        {
            findSelectedObjects(x, y, dx, dy, false, true);
            lx = x;
            ly = y;
        }
        else
        {
            UpdateSelectionBounds();
            if (CloneMode)
            {
                ArrayList<LevelItem> newObjects = CloneArrayList(SelectedObjects);
                undo.Do(new AddLvlItemAction(newObjects));

                CloneMode = false;
                mouseAct.vert = ResizeType.ResizeNone;
                mouseAct.hor = ResizeType.ResizeNone;

                SelectedObjects = newObjects;
            }

            if (mouseAct.hor == ResizeType.ResizeNone && mouseAct.vert == ResizeType.ResizeNone)
            {
                int xDelta = x - lx;
                int yDelta = y - ly;
                if (xDelta < -minBoundX)
                    xDelta = -minBoundX;
                if (yDelta < -minBoundY)
                    yDelta = -minBoundY;
                xDelta &= ~(selectionSnap - 1);
                yDelta &= ~(selectionSnap - 1);
                if (xDelta == 0 && yDelta == 0)
                    return;
                minBoundX += xDelta;
                minBoundY += yDelta;
                undo.Do(new MoveResizeLvlItemAction(SelectedObjects, xDelta, yDelta));
                lx += xDelta;
                ly += yDelta;

                //Force align =D
                //Only done when ONE object because you'll probably NOT want multiple objects
                //moving relative to each other.
                if (SelectedObjects.size() == 1)
                    for (LevelItem o : SelectedObjects)
                    {
                        Rectangle r = o.getRealRect();
                        if (r.x % selectionSnap != 0 || r.y % selectionSnap != 0 || r.width % selectionSnap != 0 || r.height % selectionSnap != 0)
                            undo.Do(new MoveResizeLvlItemAction(UndoManager.ObjToArrayList(o), -r.x % selectionSnap, -r.y % selectionSnap, -r.width % selectionSnap, -r.height % selectionSnap));
                    }

            }
            else
            {
                int xDelta = x - lx;
                int yDelta = y - ly;

                int xMoveDelta = 0;
                int xResizeDelta = 0;
                int yMoveDelta = 0;
                int yResizeDelta = 0;

                xDelta &= ~(selectionSnap - 1);
                yDelta &= ~(selectionSnap - 1);
                if (xDelta == 0 && yDelta == 0)
                    return;

                if (mouseAct.hor == ResizeType.ResizeBegin)
                {
                    if (-xDelta <= -minSizeX + selectionSnap)
                        xDelta = -(-minSizeX + selectionSnap);
                    if (xDelta < -minBoundX)
                        xDelta = -minBoundX;
                    xMoveDelta = xDelta;
                    xResizeDelta = -xDelta;
                }
                if (mouseAct.vert == ResizeType.ResizeBegin)
                {
                    if (-yDelta <= -minSizeY + selectionSnap)
                        yDelta = -(-minSizeY + selectionSnap);
                    if (yDelta < -minBoundY)
                        yDelta = -minBoundY;
                    yMoveDelta = yDelta;
                    yResizeDelta = -yDelta;
                }
                if (mouseAct.hor == ResizeType.ResizeEnd)
                {
                    if (xDelta <= -minSizeX + selectionSnap)
                        xDelta = -minSizeX + selectionSnap;
                    xResizeDelta = xDelta;
                }
                if (mouseAct.vert == ResizeType.ResizeEnd)
                {
                    if (yDelta <= -minSizeY + selectionSnap)
                        yDelta = -minSizeY + selectionSnap;
                    yResizeDelta = yDelta;
                }
                if (xMoveDelta == 0 && yMoveDelta == 0 && xResizeDelta == 0 && yResizeDelta == 0)
                    return;

                minBoundX += xMoveDelta;
                minBoundY += yMoveDelta;
                minSizeX += xResizeDelta;
                minSizeY += yResizeDelta;
                undo.Do(new MoveResizeLvlItemAction(SelectedObjects, xMoveDelta, yMoveDelta, xResizeDelta, yResizeDelta));
                lx += xDelta;
                ly += yDelta;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        setCursor(Cursor.getPredefinedCursor(getCursorForPos(e.getX(), e.getY())));
    }

    public void SelectObject(LevelItem o)
    {
        SelectedObjects.clear();
        CurSelectedObjs.clear();
        if (!SelectedObjects.contains((LevelItem) o) || SelectedObjects.size() != 1)
            SelectedObjects.add((LevelItem) o);

        UpdateSelectionBounds();
        UpdatePanel();
    }

    public void SelectObjects(ArrayList<LevelItem> o)
    {
        SelectedObjects.clear();
        CurSelectedObjs.clear();
        SelectedObjects.addAll(o);

        UpdateSelectionBounds();
        UpdatePanel();
    }

    public void SelectAll()
    {
        SelectedObjects.clear();
        CurSelectedObjs.clear();
        for (NSMBObject o : level.Objects)
            SelectedObjects.add(o);
        for (NSMBSprite s : level.Sprites)
            SelectedObjects.add(s);
        for (NSMBEntrance e : level.Entrances)
            SelectedObjects.add(e);
        for (NSMBView v : level.Views)
            SelectedObjects.add(v);
        for (NSMBView z : level.Zones)
            SelectedObjects.add(z);
        for (NSMBPath p : level.Paths)
            for (NSMBPathPoint pp : p.points)
                SelectedObjects.add(pp);
        for (NSMBPath p : level.ProgressPaths)
            for (NSMBPathPoint pp : p.points)
                SelectedObjects.add(pp);
        UpdateSelectionBounds();
    }

    private void drawResizeKnob(Graphics2D g, int x, int y)
    {
        g.setColor(Colors.selectedKnob);
        g.fillRect(x - 3, y - 3, 6, 6);
        g.setColor(Colors.selectedKnobOutline);
        g.drawRect(x - 3, y - 3, 6, 6);
    }

    public void RenderSelection(Graphics2D g)
    {
        if (SelectionRectangle != null && SelectMode)
        {
            g.setColor(Colors.selectRectangle);
            g.drawRect(SelectionRectangle.x, SelectionRectangle.y, SelectionRectangle.width, SelectionRectangle.height);
        }

        for (LevelItem o : SelectedObjects)
            if (!CurSelectedObjs.contains(o))
                RenderSelectedObject(o, g);
        if (!removing)
            for (LevelItem o : CurSelectedObjs)
                RenderSelectedObject(o, g);
    }

    private void RenderSelectedObject(LevelItem o, Graphics2D g)
    {
        Rectangle r = o.getRect();

        if (o instanceof NSMBView)
        {
            if (((NSMBView) o).isZone)
                g.setColor(Colors.zoneSelect);
            else
                g.setColor(Colors.viewSelect);

            g.fillRect(r.x, r.y, r.width, r.height);
        }

        if (o instanceof NSMBPathPoint)
        {
            BufferedImage img = Resources.get("pathpoint_add");
            g.drawImage(img, r.x + 16, r.y, null);
            img = Util.rotateFlip(img, Util.RotateFlipType.RotateNoneFlipX);
            g.drawImage(img, r.x - 16, r.y, null);
        }

        g.setColor(Colors.selected);
        g.drawRect(r.x, r.y, r.width, r.height);
        g.setColor(Colors.selectedOutline);
        g.drawRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2);

        if (o.isResizable() && resizeHandles)
        {
            drawResizeKnob(g, r.x, r.y);
            drawResizeKnob(g, r.x, r.y + r.height);
            drawResizeKnob(g, r.x, r.y + r.height / 2);
            drawResizeKnob(g, r.x + r.width, r.y);
            drawResizeKnob(g, r.x + r.width, r.y + r.height);
            drawResizeKnob(g, r.x + r.width, r.y + r.height / 2);
            drawResizeKnob(g, r.x + r.width / 2, r.y);
            drawResizeKnob(g, r.x + r.width / 2, r.y + r.height);
        }
    }

    public void ReloadObjectPicker()
    {/*
         tabs.objects.tileset0picker.reload();
         tabs.objects.tileset1picker.reload();
         tabs.objects.tileset2picker.reload();*/
        //TODO: Fix.
        //This instanceof called when changing tilesets and like.

    }

    public void UpdateSelectionBounds()
    {
        minBoundX = Integer.MAX_VALUE;
        minBoundY = Integer.MAX_VALUE;
        maxBoundX = 0;
        maxBoundY = 0;
        minSizeX = Integer.MAX_VALUE;
        minSizeY = Integer.MAX_VALUE;
        selectionSnap = snapTo8Pixels ? 8 : 1;
        for (LevelItem o : SelectedObjects)
        {
            Rectangle r = o.getRealRect();
            if (r.x < minBoundX)
                minBoundX = r.x;
            if (r.y < minBoundY)
                minBoundY = r.y;
            if (r.x + r.width > maxBoundX)
                maxBoundX = r.x + r.width;
            if (r.y + r.height > maxBoundY)
                maxBoundY = r.y + r.height;
            if (o.getSnap() > selectionSnap)
                selectionSnap = o.getSnap();

            if (o.isResizable())
                if (o instanceof NSMBView && !((NSMBView) o).isZone)
                {
                    if (r.width - 256 < minSizeX)
                        minSizeX = r.width - 256 + selectionSnap;
                    if (r.height - 192 < minSizeY)
                        minSizeY = r.height - 192 + selectionSnap;
                }
                else
                {
                    if (r.width < minSizeX)
                        minSizeX = r.width;
                    if (r.height < minSizeY)
                        minSizeY = r.height;
                }
        }
    }

    private void selectIfInside(LevelItem it, Rectangle r)
    {
        Rectangle itr = it.getRect();
        if (it instanceof NSMBView)
        {
            Rectangle viewText = GetViewTextRect(it);
            if (viewText != null && r.intersects(viewText) && !SelectedObjects.contains(it))
                SelectedObjects.add(it);
        }
        else if (r.intersects(new Rectangle(itr.x, itr.y, itr.width, itr.height)) && !SelectedObjects.contains(it))
            SelectedObjects.add(it);
    }

    private Rectangle GetViewTextRect(LevelItem view)
    {
        Rectangle r = view.getRect();
        Rectangle visible = pixels;
        visible = new Rectangle(visible.x, visible.y, visible.width, visible.height);
        if (!visible.intersects(new Rectangle(r.x, r.y, r.width, r.height)))
            return null;
        NSMBView v = (NSMBView) view;
        Rectangle viewText = new Rectangle(Math.max(r.x, visible.x), Math.max(r.y, visible.y) + (v.isZone ? 16 : 0), 50, 16);
        return viewText;
    }

    public void findSelectedObjects(int x1, int y1, int x2, int y2, boolean firstOnly, boolean clearSelection)
    {
        if (clearSelection)
            SelectedObjects.clear();

        if (x1 > x2)
        {
            int aux = x1;
            x1 = x2;
            x2 = aux;
        }
        if (y1 > y2)
        {
            int aux = y1;
            y1 = y2;
            y2 = aux;
        }

        Rectangle r = new Rectangle(x1, y1, x2 - x1, y2 - y1);
        if(r.width <= 0) r.width = 1;
        if(r.height <= 0) r.height = 1;
        SelectionRectangle = r;
        for (NSMBObject o : level.Objects)
            selectIfInside(o, r);
        for (NSMBSprite o : level.Sprites)
            selectIfInside(o, r);
        for (NSMBEntrance o : level.Entrances)
            selectIfInside(o, r);
        for (NSMBView v : level.Views)
            selectIfInside(v, r);
        for (NSMBView z : level.Zones)
            selectIfInside(z, r);
        for (NSMBPath p : level.Paths)
            for (NSMBPathPoint pp : p.points)
                selectIfInside(pp, r);
        for (NSMBPath p : level.ProgressPaths)
            for (NSMBPathPoint pp : p.points)
                selectIfInside(pp, r);

        if (firstOnly && SelectedObjects.size() > 1)
        {
            LevelItem obj = SelectedObjects.get(SelectedObjects.size() - 1);
            SelectedObjects.clear();
            SelectedObjects.add(obj);
        }

        UpdateSelectionBounds();
        repaint();
    }

    private boolean isInSelection(int x, int y)
    {
        //Search in reverse order so that the top object instanceof selected
        for (int l = SelectedObjects.size() - 1; l > -1; l--)
        {
            LevelItem o = SelectedObjects.get(l);
            Rectangle r = o.getRect();
            if (x >= r.x && x < r.x + r.width)
                if (y >= r.y && y < r.y + r.height)
                    return true;
        }
        return false;
    }

    private MouseAction getActionAtPos(int x, int y)
    {
        MouseAction act = new MouseAction();


        for (int l = SelectedObjects.size() - 1; l > -1; l--)
        {
            LevelItem o = SelectedObjects.get(l);
            Rectangle r = o.getRect();
            // For clicking the plus buttons on selected nodes
            if (o instanceof NSMBPathPoint)
            {
                NSMBPathPoint pp = (NSMBPathPoint) o;
                if (x >= r.x + 16 && x < r.x + 32 && y >= r.y && y < r.y + 16)
                {
                    act.nodeType = CreateNode.After;
                    act.node = pp;
                    return act;
                }
                if (x >= r.x - 16 && x < r.x && y >= r.y && y < r.y + 16)
                {
                    act.nodeType = CreateNode.Before;
                    act.node = pp;
                    return act;
                }
            }
            if (o.isResizable() && resizeHandles)
            {
                act.drag = true;

                if (x >= r.x - 8 && x <= r.x + 4)
                    act.hor = ResizeType.ResizeBegin;
                else if (x >= r.x + r.width - 4 && x <= r.x + r.width + 8)
                    act.hor = ResizeType.ResizeEnd;
                else if (x >= r.x && x <= r.x + r.width)
                    act.hor = ResizeType.ResizeNone;
                else
                    act.drag = false;

                if (y >= r.y - 8 && y <= r.y + 4)
                    act.vert = ResizeType.ResizeBegin;
                else if (y >= r.y + r.height - 4 && y <= r.y + r.height + 8)
                    act.vert = ResizeType.ResizeEnd;
                else if (y >= r.y && y <= r.y + r.height)
                    act.vert = ResizeType.ResizeNone;
                else
                    act.drag = false;
                //Only display move cursor on views if cursor instanceof over text
                if (act.vert == ResizeType.ResizeNone && act.hor == ResizeType.ResizeNone && o instanceof NSMBView && !GetViewTextRect(o).contains(x, y))
                    act.drag = false;
            }
            else
            {
                act.hor = ResizeType.ResizeNone;
                act.vert = ResizeType.ResizeNone;

                act.drag = false;

                if (x >= r.x && x < r.x + r.width && y >= r.y && y < r.y + r.height)
                    act.drag = true;
            }

            if (act.drag)
                return act;
        }
        return act;
    }

    public void MouseUp()
    {
        if (CreateObj)
            SelectObject(null);
        CreateObj = false;
        newObj = null;
        SelectMode = false;
        undo.merge = false;
        repaint();
//        tabs.SelectObjects(SelectedObjects);
    }

    private int getCursorForPos(int x, int y)
    {
        MouseAction act = getActionAtPos(x, y);

        if (!act.drag)
            return Cursor.DEFAULT_CURSOR;

        if (act.vert == ResizeType.ResizeBegin && act.hor == ResizeType.ResizeBegin)
            return Cursor.NW_RESIZE_CURSOR;
        if (act.vert == ResizeType.ResizeEnd && act.hor == ResizeType.ResizeEnd)
            return Cursor.SE_RESIZE_CURSOR;

        if (act.vert == ResizeType.ResizeBegin && act.hor == ResizeType.ResizeEnd)
            return Cursor.NE_RESIZE_CURSOR;
        if (act.vert == ResizeType.ResizeEnd && act.hor == ResizeType.ResizeBegin)
            return Cursor.SW_RESIZE_CURSOR;

        if (act.vert == ResizeType.ResizeNone && act.hor == ResizeType.ResizeBegin)
            return Cursor.W_RESIZE_CURSOR;
        if (act.vert == ResizeType.ResizeNone && act.hor == ResizeType.ResizeEnd)
            return Cursor.E_RESIZE_CURSOR;
        if (act.hor == ResizeType.ResizeNone && act.vert == ResizeType.ResizeBegin)
            return Cursor.N_RESIZE_CURSOR;
        if (act.hor == ResizeType.ResizeNone && act.vert == ResizeType.ResizeEnd)
            return Cursor.S_RESIZE_CURSOR;

        if (act.vert == ResizeType.ResizeNone && act.hor == ResizeType.ResizeNone)
            return Cursor.MOVE_CURSOR;

        return Cursor.DEFAULT_CURSOR;
    }

    public void UpdatePanel()
    {
        /*        tabs.UpdateInfo();
         if (SelectedObjects.size() == 1)
         EdControl.editor.coordinateViewer1.setLevelItem(SelectedObjects[0]);
         else
         EdControl.editor.coordinateViewer1.setLevelItem(null);*/
    }

    public void Refresh()
    {
        UpdatePanel();
    }

    public void DeleteObject()
    {
        if (SelectedObjects == null || SelectedObjects.isEmpty())
            return;

        undo.Do(new RemoveLvlItemAction(SelectedObjects));

        SelectedObjects.clear();
        repaint();
    }

    public void MoveObjects(int xDelta, int yDelta)
    {
        xDelta *= selectionSnap;
        yDelta *= selectionSnap;
        if (minBoundX >= -xDelta && minBoundY >= -yDelta)
        {
            undo.Do(new MoveResizeLvlItemAction(SelectedObjects, xDelta, yDelta));
            minBoundX += xDelta;
            minBoundY += yDelta;
        }
    }

    public void lower()
    {
        undo.Do(new LowerLvlItemAction(SelectedObjects));
    }

    public void raise()
    {
        undo.Do(new RaiseLvlItemAction(SelectedObjects));
    }

    //creates a clone of a list
    private ArrayList<LevelItem> CloneArrayList(ArrayList<LevelItem> Objects)
    {
        ArrayList<LevelItem> newObjects = new ArrayList<>();
        for (LevelItem SelectedObject : Objects)
        {
            if (SelectedObject instanceof NSMBObject)
                newObjects.add(new NSMBObject((NSMBObject) SelectedObject));
            if (SelectedObject instanceof NSMBSprite)
                newObjects.add(new NSMBSprite((NSMBSprite) SelectedObject));
            if (SelectedObject instanceof NSMBEntrance)
                newObjects.add(new NSMBEntrance((NSMBEntrance) SelectedObject));
            if (SelectedObject instanceof NSMBView)
                newObjects.add(new NSMBView((NSMBView) SelectedObject));
            if (SelectedObject instanceof NSMBPathPoint)
                newObjects.add(new NSMBPathPoint((NSMBPathPoint) SelectedObject));
        }

        return newObjects;
    }

    enum ResizeType
    {

        ResizeBegin,
        ResizeNone,
        ResizeEnd
    }

    enum CreateNode
    {

        None,
        Before,
        After
    }

    class MouseAction
    {

        public boolean drag = false;
        public ResizeType vert = ResizeType.ResizeNone;
        public ResizeType hor = ResizeType.ResizeNone;
        public CreateNode nodeType = CreateNode.None;
        public NSMBPathPoint node = null;
    }
}
