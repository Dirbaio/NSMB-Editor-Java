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
package net.dirbaio.nds.nsmb.leveleditor.actions;

import java.util.ArrayList;
import net.dirbaio.nds.nsmb.level.LevelItem;

public class RaiseLvlItemAction extends LvlItemAction
{

    ArrayList<Integer> zIndex;

    public RaiseLvlItemAction(ArrayList<LevelItem> objs)
    {
        super(objs);
    }

    @Override
    public void Undo()
    {
        EdControl.level.objs.remove(objs);
        EdControl.level.objs.add(objs, zIndex);
        repaintObjectRectangle();
    }

    @Override
    public void Redo()
    {
        if (zIndex == null)
            zIndex = EdControl.level.objs.removeZIndex(objs);
        else
            EdControl.level.objs.remove(objs);
        EdControl.level.objs.add(objs);
        repaintObjectRectangle();
    }
}