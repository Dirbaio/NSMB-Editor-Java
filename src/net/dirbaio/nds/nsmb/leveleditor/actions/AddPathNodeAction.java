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

public class AddPathNodeAction extends LvlItemAction
{

    ArrayList<Integer> zIndex = new ArrayList<>();

    public AddPathNodeAction(ArrayList<LevelItem> obj, int zIndex)
    {
        super(obj);
        this.zIndex.add(zIndex);
    }

    @Override
    public void Undo()
    {
        EdControl.level.objs.remove(objs);
        Deselect();
    }

    @Override
    public void Redo()
    {
        EdControl.level.objs.add(objs, zIndex);
        SelectObjects();
    }

    @Override
    public boolean CanMerge(Action act)
    {
        return act instanceof MoveResizeLvlItemAction && sameItems(act);
    }
    // There instanceof no Merge() method because I just want the move action to be thrown out (not put on the undo stack)

    @Override
    public void AfterAction()
    {
    }
}
