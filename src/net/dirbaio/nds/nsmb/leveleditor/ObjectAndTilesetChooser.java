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

import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import net.dirbaio.nds.nsmb.level.NSMBGraphics;

public class ObjectAndTilesetChooser extends JPanel
{

    private final NSMBGraphics gfx;
    private final ObjectChooser[] choosers;
    private final ArrayList<ObjectAndTilesetChangeListener> listeners = new ArrayList<>();
    private int selectedObject, selectedTileset;
    private final JTabbedPane tabs;
    
    public ObjectAndTilesetChooser(NSMBGraphics gfx)
    {
        super(new BorderLayout());
        this.gfx = gfx;

        tabs = new JTabbedPane();

        choosers = new ObjectChooser[3];
        for (int i = 0; i < 3; i++)
        {
            choosers[i] = new ObjectChooser(gfx, i);
            tabs.addTab("Tileset " + i, choosers[i]);

            final int j = i;
            choosers[i].addObjectChangeListener(new ObjectChangeListener()
            {
                @Override
                public void objectChanged(int newObject)
                {
                    setSelectedObjectAndTileset(newObject, j);
                    fireChangedEvent();
                }
            });
        }

        add(tabs, BorderLayout.CENTER);
    }

    private void addObjectAndTilesetChangeListener(ObjectAndTilesetChangeListener listener)
    {
        listeners.add(listener);
    }

    private void removeObjectAndTilesetChangeListener(ObjectAndTilesetChangeListener listener)
    {
        listeners.remove(listener);
    }

    public void setSelectedObjectAndTileset(int object, int tileset)
    {
        for (int i = 0; i < 3; i++)
            choosers[i].setSelectedObject(i == tileset ? object : -1);
        
        tabs.setSelectedIndex(tileset);
        
        selectedObject = object;
        selectedTileset = tileset;
    }

    public int getSelectedObject()
    {
        return selectedObject;
    }

    public int getSelectedTileset()
    {
        return selectedTileset;
    }

    private void fireChangedEvent()
    {
        for (ObjectAndTilesetChangeListener l : listeners)
            l.objectAndTilesetChanged(selectedObject, selectedTileset);
    }
}
