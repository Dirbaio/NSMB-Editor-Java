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
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import net.dirbaio.nds.nsmb.level.LevelItem;
import net.dirbaio.nds.nsmb.level.NSMBLevel;

public class LevelEditor extends JPanel
{

    private final JSplitPane splitPane;
    public final LevelEditorComponent editorControl;
    public final NSMBLevel level;
    public final PalettePanel palette;

    public LevelEditor(NSMBLevel level)
    {
        super(new BorderLayout());
        this.level = level;
        splitPane = new JSplitPane();

        add(splitPane, BorderLayout.CENTER);

        editorControl = new LevelEditorComponent(level, this);
        splitPane.setRightComponent(new JScrollPane(editorControl));

        palette = new PalettePanel(this);
        setPanel(new ArrayList<LevelItem>());
    }
    ArrayList<LevelItem> currentItems = null;

    public final void setPanel(ArrayList<LevelItem> items)
    {
        if (currentItems != null && items.containsAll(currentItems) && currentItems.containsAll(items))
            return;

        currentItems = items;
        
        JComponent panel = palette;
        
        if(!items.isEmpty())
        {
            panel = new JTabbedPane();
            
        }            

        int pos = splitPane.getDividerLocation();
        splitPane.setLeftComponent(panel);
        splitPane.setDividerLocation(pos);
    }
}
