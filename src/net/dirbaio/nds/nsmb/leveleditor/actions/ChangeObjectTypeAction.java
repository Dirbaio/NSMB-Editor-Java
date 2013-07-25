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
import net.dirbaio.nds.nsmb.level.NSMBObject;
import net.dirbaio.nds.util.LanguageManager;

public class ChangeObjectTypeAction extends LvlItemAction
{

    ArrayList<Integer> OrigTS, OrigNum;
    int NewTS, NewNum;

    public ChangeObjectTypeAction(ArrayList<LevelItem> objs, int NewTS, int NewNum)
    {
        super(objs);
        NSMBObject o;
        OrigTS = new ArrayList<>();
        OrigNum = new ArrayList<>();
        for (int l = 0; l < this.objs.size(); l++)
            if (this.objs.get(l) instanceof NSMBObject)
            {
                o = (NSMBObject) this.objs.get(l);
                OrigTS.add(o.Tileset);
                OrigNum.add(o.ObjNum);
            } else
            {
                this.objs.remove(l);
                l--;
            }
        this.NewTS = NewTS;
        this.NewNum = NewNum;
    }

    @Override
    public void Undo()
    {
        NSMBObject o;
        for (int l = 0; l < objs.size(); l++)
        {
            o = (NSMBObject) objs.get(l);
            o.Tileset = OrigTS.get(l);
            o.ObjNum = OrigNum.get(l);
            o.UpdateObjCache();
        }
        repaintObjectRectangle();
    }

    @Override
    public void Redo()
    {
        NSMBObject o;
        for (LevelItem obj : objs)
        {
            o = (NSMBObject) obj;
            o.Tileset = NewTS;
            o.ObjNum = NewNum;
            o.UpdateObjCache();
        }
        repaintObjectRectangle();
    }

    @Override
    public boolean CanMerge(Action act)
    {
        return act instanceof ChangeObjectTypeAction && sameItems(act);
    }

    @Override
    public void Merge(Action act)
    {
        ChangeObjectTypeAction cota = (ChangeObjectTypeAction) act;
        this.NewTS = cota.NewTS;
        this.NewNum = cota.NewNum;
    }

    @Override
    public String toString()
    {
        return LanguageManager.GetArrayList("UndoActions")[4];
    }
}