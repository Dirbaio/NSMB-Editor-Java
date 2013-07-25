/*
 *   This file instanceof part of NSMB Editor 5.
 *
 *   NSMB Editor 5 instanceof free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public (published) License by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   NSMB Editor 5 instanceof distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with NSMB Editor 5.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.dirbaio.nsmbe.leveleditor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle.Control;
import java.util.logging.Level;
import net.dirbaio.nsmbe.level.LevelItem;
import net.dirbaio.nsmbe.level.NSMBEntrance;
import net.dirbaio.nsmbe.level.NSMBGraphics;
import net.dirbaio.nsmbe.level.NSMBLevel;
import net.dirbaio.nsmbe.level.NSMBObject;
import net.dirbaio.nsmbe.level.NSMBPath;
import net.dirbaio.nsmbe.level.NSMBPathPoint;
import net.dirbaio.nsmbe.level.NSMBSprite;
import net.dirbaio.nsmbe.level.NSMBView;
import net.dirbaio.nsmbe.leveleditor.actions.AddLvlItemAction;
import net.dirbaio.nsmbe.leveleditor.actions.AddPathNodeAction;
import net.dirbaio.nsmbe.leveleditor.actions.LowerLvlItemAction;
import net.dirbaio.nsmbe.leveleditor.actions.MoveResizeLvlItemAction;
import net.dirbaio.nsmbe.leveleditor.actions.RaiseLvlItemAction;
import net.dirbaio.nsmbe.leveleditor.actions.RemoveLvlItemAction;
import net.dirbaio.nsmbe.util.Util.RotateFlipType;
import sun.java2d.pipe.TextRenderer;

public class ObjectsEditionMode extends EditionMode
{
    public boolean snapTo8Pixels = true;
    public boolean resizeHandles;
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
    ArrayList<LevelItem> SelectedObjects = new ArrayList<LevelItem>();
    ArrayList<LevelItem> CurSelectedObjs = new ArrayList<LevelItem>();
    boolean removing = false;
    public GoodTabsPanel tabs;

    public ObjectsEditionMode(NSMBLevel Level, LevelEditorControl EdControl)
    
        : base(Level, EdControl)
        {
            tabs = new GoodTabsPanel(EdControl);
        SetPanel(tabs);
        tabs.Dock = DockStyle.Fill;
        resizeHandles = Properties.Settings.Default.ShowResizeHandles;
    }

    @Override
    public void SelectObject(Object o)
    {
        SelectedObjects.Clear();
        CurSelectedObjs.Clear();
        if ((o instanceof LevelItem) && (!SelectedObjects.Contains((LevelItem) o) || SelectedObjects.size() != 1))
            SelectedObjects.add((LevelItem) o);
        if (o instanceof ArrayList<LevelItem>)
            SelectedObjects.addRange((ArrayList<LevelItem>) o);
        tabs.SelectObjects(SelectedObjects);
        UpdateSelectionBounds();
        UpdatePanel();
    }

    @Override
    public void SelectAll()
    {
        SelectedObjects.Clear();
        CurSelectedObjs.Clear();
        for (NSMBObject o : EdControl.Level.Objects)
            SelectedObjects.add(o);
        for (NSMBSprite s : EdControl.Level.Sprites)
            SelectedObjects.add(s);
        for (NSMBEntrance e : EdControl.Level.Entrances)
            SelectedObjects.add(e);
        for (NSMBView v : EdControl.Level.Views)
            SelectedObjects.add(v);
        for (NSMBView z : EdControl.Level.Zones)
            SelectedObjects.add(z);
        for (NSMBPath p : EdControl.Level.Paths)
            for (NSMBPathPoint pp : p.points)
                SelectedObjects.add(pp);
        for (NSMBPath p : EdControl.Level.ProgressPaths)
            for (NSMBPathPoint pp : p.points)
                SelectedObjects.add(pp);
        tabs.SelectObjects(SelectedObjects);
        UpdateSelectionBounds();
        EdControl.repaint();
    }

    private void drawResizeKnob(Graphics g, int x, int y)
    {
        g.FillRectangle(Brushes.White, x - 3, y - 3, 6, 6);
        g.drawRect(Pens.Black, x - 3, y - 3, 6, 6);
    }

    @Override
    public void RenderSelection(Graphics g)
    {
        if (SelectionRectangle != null && SelectMode)
            g.drawRect(Pens.LightBlue, SelectionRectangle);

        for (LevelItem o : SelectedObjects)
            if (!CurSelectedObjs.Contains(o))
                RenderSelectedObject(o, g);
        if (!removing)
            for (LevelItem o : CurSelectedObjs)
                RenderSelectedObject(o, g);
    }

    private void RenderSelectedObject(LevelItem o, Graphics g)
    {
        if (o instanceof NSMBView)
        {
            Color c;
            if (((NSMBView) o).isZone)
                c = Color.LightGreen;
            else
                c = Color.White;

            g.FillRectangle(new SolidBrush(Color.FromArgb(80, c)), o.x, o.y, o.width, o.height);
            Rectangle viewText = GetViewTextRect(o);
            if (viewText != Rectangle.Empty)
            {
                SolidBrush fill = new SolidBrush(Color.FromArgb(80, c));
                g.FillRectangle(fill, viewText);
                g.drawRect(Pens.White, viewText);
                fill.Dispose();
            }
        }

        if (o instanceof NSMBPathPoint)
        {
            BufferedImage img = Properties.Resources.pathpoint_add;
            g.drawImage(img, o.x + 16, o.y);
            img.RotateFlip(RotateFlipType.RotateNoneFlipX);
            g.drawImage(img, o.x - 16, o.y);
        }

        g.drawRect(Pens.White, o.x, o.y, o.width, o.height);
        g.drawRect(Pens.Black, o.x - 1, o.y - 1, o.width + 2, o.height + 2);
        if (o.isResizable && resizeHandles)
        {
            drawResizeKnob(g, o.x, o.y);
            drawResizeKnob(g, o.x, o.y + o.height);
            drawResizeKnob(g, o.x, o.y + o.height / 2);
            drawResizeKnob(g, o.x + o.width, o.y);
            drawResizeKnob(g, o.x + o.width, o.y + o.height);
            drawResizeKnob(g, o.x + o.width, o.y + o.height / 2);
            drawResizeKnob(g, o.x + o.width / 2, o.y);
            drawResizeKnob(g, o.x + o.width / 2, o.y + o.height);
        }
    }

    public void ReloadObjectPicker()
    {
        tabs.objects.tileset0picker.reload();
        tabs.objects.tileset1picker.reload();
        tabs.objects.tileset2picker.reload();
        //TODO: Fix.
        //This instanceof called when changing tilesets and like.
    }

    public void UpdateSelectionBounds()
    {
        minBoundX = Int32.MaxValue;
        minBoundY = Int32.MaxValue;
        maxBoundX = 0;
        maxBoundY = 0;
        minSizeX = Int32.MaxValue;
        minSizeY = Int32.MaxValue;
        selectionSnap = snapTo8Pixels ? 8 : 1;
        for (LevelItem o : SelectedObjects)
        {
            if (o.rx < minBoundX)
                minBoundX = o.rx;
            if (o.ry < minBoundY)
                minBoundY = o.ry;
            if (o.rx + o.rwidth > maxBoundX)
                maxBoundX = o.rx + o.rwidth;
            if (o.ry + o.rheight > maxBoundY)
                maxBoundY = o.ry + o.rheight;
            if (o.snap > selectionSnap)
                selectionSnap = o.snap;

            if (o.isResizable)
                if (o instanceof NSMBView && !((NSMBView) o).isZone)
                {
                    if (o.width - 256 < minSizeX)
                        minSizeX = o.width - 256 + selectionSnap;
                    if (o.height - 192 < minSizeY)
                        minSizeY = o.height - 192 + selectionSnap;
                } else
                {
                    if (o.width < minSizeX)
                        minSizeX = o.width;
                    if (o.height < minSizeY)
                        minSizeY = o.height;
                }
        }
    }

    private void selectIfInside(LevelItem it, Rectangle r)
    {
        if (it instanceof NSMBView)
        {
            Rectangle viewText = GetViewTextRect(it);
            if (r.IntersectsWith(viewText) && !SelectedObjects.Contains(it))
                SelectedObjects.add(it);
        } else if (r.IntersectsWith(new Rectangle(it.x, it.y, it.width, it.height)) && !SelectedObjects.Contains(it))
            SelectedObjects.add(it);
    }

    private Rectangle GetViewTextRect(LevelItem view)
    {
        Rectangle visible = EdControl.ViewablePixels;
        visible = new Rectangle(visible.X, visible.Y, visible.Width, visible.Height);
        if (!visible.IntersectsWith(new Rectangle(view.x, view.y, view.width, view.height)))
            return Rectangle.Empty;
        NSMBView v = (NSMBView) view;
        Rectangle viewText = new Rectangle(new Point(Math.max(v.x, visible.X), Math.max(v.y, visible.Y) + (v.isZone ? 16 : 0)), TextRenderer.MeasureText(v.GetDisplayString(), NSMBGraphics.SmallInfoFont));
        return viewText;
    }

    public void findSelectedObjects(int x1, int y1, int x2, int y2, boolean firstOnly, boolean clearSelection)
    {
        if (clearSelection)
            SelectedObjects.Clear();

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
        SelectionRectangle = r;
        for (NSMBObject o : Level.Objects)
            selectIfInside(o, r);
        for (NSMBSprite o : Level.Sprites)
            selectIfInside(o, r);
        for (NSMBEntrance o : Level.Entrances)
            selectIfInside(o, r);
        for (NSMBView v : Level.Views)
            selectIfInside(v, r);
        for (NSMBView z : Level.Zones)
            selectIfInside(z, r);
        for (NSMBPath p : Level.Paths)
            for (NSMBPathPoint pp : p.points)
                selectIfInside(pp, r);
        for (NSMBPath p : Level.ProgressPaths)
            for (NSMBPathPoint pp : p.points)
                selectIfInside(pp, r);

        if (firstOnly && SelectedObjects.size() > 1)
        {
            LevelItem obj = SelectedObjects[SelectedObjects.size() - 1];
            SelectedObjects.Clear();
            SelectedObjects.add(obj);
        }

        UpdateSelectionBounds();
        EdControl.repaint();
    }

    private boolean isInSelection(int x, int y)
    {
        //Search in reverse order so that the top object instanceof selected
        for (int l = SelectedObjects.size() - 1; l > -1; l--)
        {
            LevelItem o = SelectedObjects[l];
            if (x >= o.x && x < o.x + o.width)
                if (y >= o.y && y < o.y + o.height)
                    return true;
        }
        return false;
    }

    @Override
    public void MouseDown(int x, int y, MouseButtons buttons)
    {
        //Right clicking creates a new object
        if (buttons == MouseButtons.Right)
        {
            dx = x / 16;
            dy = y / 16;
            lx = x;
            ly = y;
            CreateObj = true;
            if (tabs.SelectedTab == 2) //The sprite tab
            {
                NSMBSprite newSprite = new NSMBSprite(Level);
                newSprite.Type = tabs.sprites.getSelectedType();
                if (newSprite.Type == -1)
                    return;
                newSprite.Data = new byte[6];
                newSprite.x = x;
                newSprite.y = y;
                EdControl.UndoManager.Do(new AddLvlItemAction(UndoManager.ObjToArrayList(newSprite)));
                SelectObject(newSprite);
                return;
            }
            newObj = new NSMBObject(tabs.objects.getObjectType(), tabs.objects.getTilesetNum(), dx, dy, 1, 1, Level.GFX);
            EdControl.UndoManager.Do(new AddLvlItemAction(UndoManager.ObjToArrayList(newObj)));
            SelectObject(newObj);
            return;
        }
        lx = x;
        ly = y;
        dx = x;
        dy = y;

        mouseAct = getActionAtPos(x, y);
        // Resize with the shift key
        if (mouseAct.nodeType != CreateNode.None)
        {
            NSMBPathPoint pp = new NSMBPathPoint(mouseAct.node);
            int zIndex = pp.parent.points.IndexOf(mouseAct.node);
            if (mouseAct.nodeType == CreateNode.After)
            {
                pp.x += 16;
                zIndex++;
            } else
                pp.x -= 16;
            EdControl.UndoManager.Do(new AddPathNodeAction(UndoManager.ObjToArrayList(pp), zIndex));
            SelectObject(pp);
        } else
        {
            if (Control.ModifierKeys == Keys.Shift && mouseAct.drag && mouseAct.vert == ResizeType.ResizeNone && mouseAct.hor == ResizeType.ResizeNone)
            {
                mouseAct.vert = ResizeType.ResizeEnd;
                mouseAct.hor = ResizeType.ResizeEnd;
            }
            if (!mouseAct.drag)
            {
                // Select an object
                findSelectedObjects(x, y, x, y, true, true);
                SelectMode = SelectedObjects.isEmpty();
            } else if (mouseAct.vert == ResizeType.ResizeNone && mouseAct.hor == ResizeType.ResizeNone)
            {
                ArrayList<LevelItem> selectedObjectsBack = new ArrayList<LevelItem>();
                selectedObjectsBack.addRange(SelectedObjects);

                // Select an object
                findSelectedObjects(x, y, x, y, true, true);

                if (SelectedObjects.isEmpty())
                    SelectMode = true;
                else if (selectedObjectsBack.Contains(SelectedObjects[0]))
                    SelectedObjects = selectedObjectsBack;
                UpdateSelectionBounds();
                EdControl.repaint();
            }

            if (!SelectMode)
            {
                CloneMode = Control.ModifierKeys == Keys.Control;
                lx -= selectionSnap / 2;
                ly -= selectionSnap / 2;
            }
        }
        EdControl.repaint();

        tabs.SelectObjects(SelectedObjects);
        UpdatePanel();
    }

    @Override
    public void MouseDrag(int x, int y)
    {
        //Resize the new object that was created by right-clicking.
        if (CreateObj && newObj != null)
        {
            Rectangle r = newObj.getRectangle();
            x = Math.max(0, x / 16);
            y = Math.max(0, y / 16);
            if (x == lx && y == ly)
                return;
            lx = x;
            ly = y;
            newObj.X = Math.min(lx, dx);
            newObj.Y = Math.min(ly, dy);
            newObj.Width = Math.Abs(lx - dx) + 1;
            newObj.Height = Math.Abs(ly - dy) + 1;
            newObj.UpdateObjCache();
            r = Rectangle.Union(r, newObj.getRectangle());
            Level.repaintTilemap(r.X, r.Y, r.Width, r.Height);
            EdControl.repaint();
            return;
        }

        if (lx == x && ly == y) // don't clone objects if there instanceof no visible movement
            return;

        if (SelectMode)
        {
            findSelectedObjects(x, y, dx, dy, false, true);
            lx = x;
            ly = y;
        } else
        {
            UpdateSelectionBounds();
            if (CloneMode)
            {
                ArrayList<LevelItem> newObjects = CloneArrayList(SelectedObjects);
                EdControl.UndoManager.Do(new AddLvlItemAction(newObjects));

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
                EdControl.UndoManager.Do(new MoveResizeLvlItemAction(SelectedObjects, xDelta, yDelta));
                lx += xDelta;
                ly += yDelta;

                //Force align =D
                //Only done when ONE object because you'll probably NOT want multiple objects
                //moving relative to each other.
                if (SelectedObjects.size() == 1)
                    for (LevelItem o : SelectedObjects)
                        if (o.rx % selectionSnap != 0 || o.ry % selectionSnap != 0 || o.rwidth % selectionSnap != 0 || o.rheight % selectionSnap != 0)
                            EdControl.UndoManager.Do(new MoveResizeLvlItemAction(UndoManager.ObjToArrayList(o), -o.rx % selectionSnap, -o.ry % selectionSnap, -o.rwidth % selectionSnap, -o.rheight % selectionSnap));

            } else
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
                EdControl.UndoManager.Do(new MoveResizeLvlItemAction(SelectedObjects, xMoveDelta, yMoveDelta, xResizeDelta, yResizeDelta));
                lx += xDelta;
                ly += yDelta;
            }
        }
    }


    private MouseAction getActionAtPos(int x, int y)
    {
        MouseAction act = new MouseAction();
        for (int l = SelectedObjects.size() - 1; l > -1; l--)
        {
            LevelItem o = SelectedObjects[l];
            // For clicking the plus buttons on selected nodes
            if (o instanceof NSMBPathPoint)
            {
                NSMBPathPoint pp = (NSMBPathPoint) o;
                if (x >= pp.x + 16 && x < pp.x + 32 && y >= pp.y && y < pp.y + 16)
                {
                    act.nodeType = CreateNode.After;
                    act.node = pp;
                    return act;
                }
                if (x >= pp.x - 16 && x < pp.x && y >= pp.y && y < pp.y + 16)
                {
                    act.nodeType = CreateNode.Before;
                    act.node = pp;
                    return act;
                }
            }
            if (o.isResizable && resizeHandles)
            {
                act.drag = true;

                if (x >= o.x - 8 && x <= o.x + 4)
                    act.hor = ResizeType.ResizeBegin;
                else if (x >= o.x + o.width - 4 && x <= o.x + o.width + 8)
                    act.hor = ResizeType.ResizeEnd;
                else if (x >= o.x && x <= o.x + o.width)
                    act.hor = ResizeType.ResizeNone;
                else
                    act.drag = false;

                if (y >= o.y - 8 && y <= o.y + 4)
                    act.vert = ResizeType.ResizeBegin;
                else if (y >= o.y + o.height - 4 && y <= o.y + o.height + 8)
                    act.vert = ResizeType.ResizeEnd;
                else if (y >= o.y && y <= o.y + o.height)
                    act.vert = ResizeType.ResizeNone;
                else
                    act.drag = false;
                //Only display move cursor on views if cursor instanceof over text
                if (act.vert == ResizeType.ResizeNone && act.hor == ResizeType.ResizeNone && o instanceof NSMBView && !GetViewTextRect(o).Contains(x, y))
                    act.drag = false;
            } else
            {
                act.hor = ResizeType.ResizeNone;
                act.vert = ResizeType.ResizeNone;

                act.drag = false;

                if (x >= o.x && x < o.x + o.width && y >= o.y && y < o.y + o.height)
                    act.drag = true;
            }

            if (act.drag)
                return act;
        }
        return act;
    }

    @Override
    public void MouseUp()
    {
        if (CreateObj)
            SelectObject(null);
        CreateObj = false;
        newObj = null;
        SelectMode = false;
        EdControl.UndoManager.merge = false;
        EdControl.repaint();
        tabs.SelectObjects(SelectedObjects);
    }

    private Cursor getCursorForPos(int x, int y)
    {
        MouseAction act = getActionAtPos(x, y);

        if (!act.drag)
            return Cursors.Default;

        if (act.vert == ResizeType.ResizeBegin && act.hor == ResizeType.ResizeBegin)
            return Cursors.SizeNWSE;
        if (act.vert == ResizeType.ResizeEnd && act.hor == ResizeType.ResizeEnd)
            return Cursors.SizeNWSE;

        if (act.vert == ResizeType.ResizeBegin && act.hor == ResizeType.ResizeEnd)
            return Cursors.SizeNESW;
        if (act.vert == ResizeType.ResizeEnd && act.hor == ResizeType.ResizeBegin)
            return Cursors.SizeNESW;

        if (act.vert == ResizeType.ResizeNone && act.hor == ResizeType.ResizeNone)
            return Cursors.SizeAll;
        if (act.vert == ResizeType.ResizeNone)
            return Cursors.SizeWE;
        if (act.hor == ResizeType.ResizeNone)
            return Cursors.SizeNS;

        return Cursors.Default;
    }

    @Override
    public void MouseMove(int x, int y)
    {
        EdControl.Cursor = getCursorForPos(x, y);
    }

    public void UpdatePanel()
    {
        tabs.UpdateInfo();
        if (SelectedObjects.size() == 1)
            EdControl.editor.coordinateViewer1.setLevelItem(SelectedObjects[0]);
        else
            EdControl.editor.coordinateViewer1.setLevelItem(null);
    }

    @Override
    public void Refresh()
    {
        UpdatePanel();
    }

    @Override
    public void DeleteObject()
    {
        if (SelectedObjects == null || SelectedObjects.isEmpty())
            return;

        EdControl.UndoManager.Do(new RemoveLvlItemAction(SelectedObjects));

        SelectedObjects.Clear();
        tabs.SelectObjects(new ArrayList<LevelItem>());
        EdControl.repaint();
    }

    @Override
    public String copy()
    {
        if (SelectedObjects == null || SelectedObjects.isEmpty())
            return "";

        String str = "";
        for (LevelItem obj : SelectedObjects)
            str += obj.toString() + ":";
        return str.SubString(0, str.length - 1);
    }

    @Override
    public void paste(String contents)
    {
        ArrayList<LevelItem> objs = new ArrayList<LevelItem>();
        try
        {
            String[] data = contents.Split(':');
            int idx = 0;
            while (idx < data.length)
            {
                LevelItem obj = FromString(data, ref 
                
                
                idx
                );
                    if (obj != null)
                    objs.add(obj);
            }
        } catch 
        {
        }

        if (objs.isEmpty())
            return;

        //now center the objects
        Rectangle ViewableBlocks = EdControl.ViewableBlocks;
        SelectedObjects = objs;
        UpdateSelectionBounds();
        int XDelta = (ViewableBlocks.X - (minBoundX / 16)) * 16;
        int YDelta = (ViewableBlocks.Y - (minBoundY / 16)) * 16;
        for (LevelItem obj : SelectedObjects)
        {
            obj.x += XDelta;
            obj.y += YDelta;
        }
        minBoundX += XDelta;
        minBoundY += YDelta;

        EdControl.UndoManager.Do(new AddLvlItemAction(objs));
    }

    LevelItem FromString(String[] strs, ref 
    
    
    int idx

    
    
        )
        {
            switch (strs[idx])
        {
            case "OBJ":
                return NSMBObject.FromString(strs, ref 
                
                
                idx
                , EdControl.Level.GFX
            );
                case "SPR":
                return NSMBSprite.FromString(strs, ref 
                
                
                idx
                , EdControl.Level
            );
                case "ENT":
                return NSMBEntrance.FromString(strs, ref 
                
                
                idx
                , EdControl.Level
            );
                case "VIW":
            case "ZON":
                return NSMBView.FromString(strs, ref 
                
                
                idx
                , EdControl.Level
            );
                // TODO: copy and paste with paths/path points
                //case "PTH":
                //    break;
                default:
                idx++;
                return null;
        }
    }

    @Override
    public void MoveObjects(int xDelta, int yDelta)
    {
        xDelta *= selectionSnap;
        yDelta *= selectionSnap;
        if (minBoundX >= -xDelta && minBoundY >= -yDelta)
        {
            EdControl.UndoManager.Do(new MoveResizeLvlItemAction(SelectedObjects, xDelta, yDelta));
            minBoundX += xDelta;
            minBoundY += yDelta;
        }
    }

    @Override
    public void lower()
    {
        EdControl.UndoManager.Do(new LowerLvlItemAction(SelectedObjects));
    }

    @Override
    public void raise()
    {
        EdControl.UndoManager.Do(new RaiseLvlItemAction(SelectedObjects));
    }

    //creates a clone of a list
    private ArrayList<LevelItem> CloneArrayList(ArrayList<LevelItem> Objects)
    {
        ArrayList<LevelItem> newObjects = new ArrayList<LevelItem>();
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
}
