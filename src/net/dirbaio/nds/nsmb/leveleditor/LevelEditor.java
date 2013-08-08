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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import net.dirbaio.nds.nsmb.level.NSMBLevel;


public class LevelEditor extends JPanel
{
    private final JSplitPane splitPane;
    private final LevelEditorComponent editorControl;
    
    private NSMBLevel level;
    public LevelEditor(NSMBLevel level)
    {
        super(new BorderLayout());
        this.level = level;
        splitPane = new JSplitPane();
        
        add(splitPane, BorderLayout.CENTER);
        
        editorControl = new LevelEditorComponent(level, this);
        splitPane.setRightComponent(new JScrollPane(editorControl));
    }
    
    
}
