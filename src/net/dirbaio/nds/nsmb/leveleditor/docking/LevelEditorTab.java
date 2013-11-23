/*
 * Copyright (C) 2013 Piranhaplant
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

package net.dirbaio.nds.nsmb.leveleditor.docking;

import net.dirbaio.nds.nsmb.level.*;
import net.dirbaio.nds.nsmb.leveleditor.*;

public class LevelEditorTab extends NSMBeTab {
    
    public LevelEditorComponent levelEditor;

    public LevelEditorTab(NSMBLevel level, LevelEditor editor) {
        super(level.name, null, null);
        levelEditor = new LevelEditorComponent(level, editor);
        this.setComponent(levelEditor);
    }
}
