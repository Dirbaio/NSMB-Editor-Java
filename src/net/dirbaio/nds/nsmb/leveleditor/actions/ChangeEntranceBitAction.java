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
import net.dirbaio.nds.nsmb.level.NSMBEntrance;



public class ChangeEntranceBitAction extends LvlItemAction
{

    int PropNum;
    ArrayList<Boolean> OrigV;
    boolean NewV;

    public ChangeEntranceBitAction(ArrayList<LevelItem> objs, int PropNum, boolean NewV)
    {
        super(objs);
        OrigV = new ArrayList<>();
        this.NewV = NewV;
        this.PropNum = PropNum;
        for (int l = 0; l < this.objs.size(); l++)
            if (this.objs.get(l) instanceof NSMBEntrance)
                OrigV.add(Read((NSMBEntrance) this.objs.get(l)));
            else
                this.objs.remove(l--);
    }

    @Override
    public void Undo()
    {
        for (int l = 0; l < objs.size(); l++)
            Write((NSMBEntrance) objs.get(l), OrigV.get(l));
    }

    @Override
    public void Redo()
    {
        for (LevelItem obj : objs)
            Write((NSMBEntrance) obj, NewV);
    }

    private boolean Read(NSMBEntrance e)
    {
        switch (PropNum)
        {
            case 0:
                return (e.Settings & 128) != 0;
            case 1:
                return (e.Settings & 16) != 0;
            case 2:
                return (e.Settings & 8) != 0;
            case 3:
                return (e.Settings & 1) != 0;
        }
        return false;
    }

    private void Write(NSMBEntrance e, boolean value)
    {
        if (value)
            switch (PropNum)
            {
                case 0:
                    e.Settings |= 128;
                    break;
                case 1:
                    e.Settings |= 16;
                    break;
                case 2:
                    e.Settings |= 8;
                    break;
                case 3:
                    e.Settings |= 1;
                    break;
            }
        else
            switch (PropNum)
            {
                case 0:
                    e.Settings &= 127;
                    break;
                case 1:
                    e.Settings &= 239;
                    break;
                case 2:
                    e.Settings &= 247;
                    break;
                case 3:
                    e.Settings &= 254;
                    break;
            }
    }

    @Override
    public boolean CanMerge(Action act)
    {
        if (!(act instanceof ChangeEntranceBitAction && sameItems(act)))
            return false;
        return ((ChangeEntranceBitAction) act).PropNum == this.PropNum;
    }

    @Override
    public void Merge(Action act)
    {
        this.NewV = ((ChangeEntranceBitAction) act).NewV;
    }
}