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
package net.dirbaio.nsmbe.leveleditor.actions;

import java.util.ArrayList;
import net.dirbaio.nsmbe.level.LevelItem;
import net.dirbaio.nsmbe.level.NSMBPathPoint;
import net.dirbaio.nsmbe.util.LanguageManager;

public class ChangePathIdAction extends LvlItemAction
{

    int OrigID, NewID;

    public ChangePathIdAction(ArrayList<LevelItem> objs, int NewID)
    {
        super(objs);
        for (int l = 0; l < this.objs.size(); l++)
            if (this.objs.get(l) instanceof NSMBPathPoint)
            {
                OrigID = ((NSMBPathPoint) this.objs.get(l)).parent.id;
                break;
            } else
            {
                this.objs.remove(l);
                l--;
            }
        for (int l = 1; l < this.objs.size(); l++)
            this.objs.remove(l);
        if (this.objs.isEmpty())
            this.cancel = true;
        this.NewID = NewID;
    }

    @Override
    public void Undo()
    {
        ((NSMBPathPoint) objs.get(0)).parent.id = OrigID;
    }

    @Override
    public void Redo()
    {
        ((NSMBPathPoint) objs.get(0)).parent.id = NewID;
    }

    @Override
    public String toString()
    {
        return LanguageManager.GetArrayList("UndoActions")[20];
    }
}
