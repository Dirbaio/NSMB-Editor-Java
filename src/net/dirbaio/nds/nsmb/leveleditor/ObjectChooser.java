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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import net.dirbaio.nds.nsmb.level.NSMBGraphics;
import net.dirbaio.nds.nsmb.level.NSMBObject;
import net.dirbaio.nds.nsmb.level.NSMBTileset;
import net.dirbaio.nds.util.Colors;

public class ObjectChooser extends JScrollPane
{

    private final int tileset;
    private final NSMBTileset t;
    private final NSMBGraphics gfx;
    private final ArrayList<NSMBObject> objects;
    private final ArrayList<ObjectChangeListener> listeners = new ArrayList<>();
    private final ObjectChooserControl occ;
    private int tileWidth = -1;
    private int selectedObject = -1;
    private NSMBObject selected = null;

    public ObjectChooser(NSMBGraphics gfx, int tileset)
    {
        this.gfx = gfx;
        this.t = gfx.Tilesets[tileset];
        this.tileset = tileset;
        objects = new ArrayList<>();

        setViewportView(occ = new ObjectChooserControl(gfx, tileset));
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    public void setSelectedObject(int object)
    {
        if(object == -1)
            selected = null;
        else
        {
            selected = objects.get(object);
            Rectangle r = occ.getRectFor(selected);
            getViewport().scrollRectToVisible(r);
            occ.repaint();
        }
        selectedObject = object;
        
    }

    public int getSelectedObject()
    {
        return selectedObject;
    }

    public void addObjectChangeListener(ObjectChangeListener l)
    {
        listeners.add(l);
    }

    public void removeObjectChangeListener(ObjectChangeListener l)
    {
        listeners.remove(l);
    }

    public class ObjectChooserControl extends JComponent implements ComponentListener, Scrollable, MouseListener
    {

        public ObjectChooserControl(NSMBGraphics gfx, int tileset)
        {
            addComponentListener(this);
            addMouseListener(this);
            loadObjects();
        }

        private void loadObjects()
        {
            int nw = (getWidth() - 16) / 16;
            if (nw < 5)
                nw = 5;

            if (tileWidth == nw)
                return;

            selected = null;
            objects.clear();

            tileWidth = nw;
            int x = 0;
            int y = 0;

            int rowheight = 1;
            for (int i = 0; i < 256; i++)
            {
                if (t.Objects[i] == null)
                    continue;
                int ow = t.Objects[i].getWidth();
                int oh = t.Objects[i].getHeight();
                if (ow > tileWidth)
                    ow = tileWidth;
                if (oh > 5)
                    oh = 5;

                if (x + ow > tileWidth)
                {
                    //New line
                    x = 0;
                    y += rowheight + 1;
                    rowheight = 1;
                }

                NSMBObject o = new NSMBObject(i, tileset, x, y, ow, oh, gfx);
                if (i == selectedObject)
                    selected = o;

                x += ow + 1;
                if (oh > rowheight)
                    rowheight = oh;

                objects.add(o);
            }

            if (x != 0)
                y += rowheight + 1;

            int scrollheight = y * 16 + 16;
            setPreferredSize(new Dimension(1, scrollheight));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Colors.objectPickerBackground);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.translate(8, 8);
            for (NSMBObject o : objects)
            {
                g2d.setColor(o == selected ? Colors.objectPickerObjSelected : Colors.objectPickerObjBorder);
                g2d.fillRect(o.X * 16 - 4, o.Y * 16 - 4, o.Width * 16 + 8, o.Height * 16 + 8);
                g2d.setColor(Colors.objectPickerObjBackground);
                g2d.fillRect(o.X * 16, o.Y * 16, o.Width * 16, o.Height * 16);
                o.render(g2d, null);
            }
            g2d.translate(-8, -8);
        }

        @Override
        public void componentResized(ComponentEvent e)
        {
            loadObjects();
        }

        @Override
        public void componentMoved(ComponentEvent e)
        {
        }

        @Override
        public void componentShown(ComponentEvent e)
        {
            loadObjects();
        }

        @Override
        public void componentHidden(ComponentEvent e)
        {
        }

        @Override
        public Dimension getPreferredScrollableViewportSize()
        {
            return new Dimension(300, 500);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
        {
            return 32;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
        {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth()
        {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight()
        {
            return false;
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {

            int x = e.getX();
            int y = e.getY();

            int oldSel = selectedObject;

            for (NSMBObject obj : objects)
            {
                Rectangle or = getRectFor(obj);
                if (or.contains(x, y))
                {
                    selected = obj;
                    selectedObject = obj.ObjNum;
                }
            }

            if (oldSel != selectedObject)
            {
                for (ObjectChangeListener l : listeners)
                    l.objectChanged(selectedObject);
                repaint();
            }
        }

        private Rectangle getRectFor(NSMBObject obj)
        {
            Rectangle or = new Rectangle(obj.X * 16 + 8, obj.Y * 16 + 8, obj.Width * 16, obj.Height * 16);
            or.grow(8, 8);
            return or;
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
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
    }
}