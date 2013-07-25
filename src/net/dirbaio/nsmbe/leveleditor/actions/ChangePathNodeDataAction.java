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

public class ChangePathNodeDataAction extends LvlItemAction
{

    int PropNum;
    ArrayList<Integer> OrigV;
    short NewV;

    public ChangePathNodeDataAction(ArrayList<LevelItem> objs, int PropNum, int NewV)
    {
        super(objs);
        OrigV = new ArrayList<>();
        this.PropNum = PropNum;
        this.NewV = (short) NewV;
        for (int l = 0; l < this.objs.size(); l++)
            if (this.objs.get(l) instanceof NSMBPathPoint)
                OrigV.add(Read((NSMBPathPoint) this.objs.get(l)));
            else
            {
                this.objs.remove(l);
                l--;
            }
    }

    @Override
    public void Undo()
    {
        for (int l = 0; l < objs.size(); l++)
            Write((NSMBPathPoint) objs.get(l), OrigV.get(l));
    }

    @Override
    public void Redo()
    {
        for (LevelItem obj : objs)
            Write((NSMBPathPoint) obj, NewV);
    }

    private int Read(NSMBPathPoint p)
    {
        switch (PropNum)
        {
            case 0:
                return p.Unknown1;
            case 1:
                return p.Unknown2;
            case 2:
                return p.Unknown3;
            case 3:
                return p.Unknown4;
            case 4:
                return p.Unknown5;
            case 5:
                return p.Unknown6;
        }
        return 0;
    }

    private void Write(NSMBPathPoint p, int value)
    {
        switch (PropNum)
        {
            case 0:
                p.Unknown1 = value;
                break;
            case 1:
                p.Unknown2 = value;
                break;
            case 2:
                p.Unknown3 = value;
                break;
            case 3:
                p.Unknown4 = value;
                break;
            case 4:
                p.Unknown5 = value;
                break;
            case 5:
                p.Unknown6 = value;
                break;
        }
    }

    @Override
    public boolean CanMerge(Action act)
    {
        if (!(act instanceof ChangePathNodeDataAction && sameItems(act)))
            return false;
        return ((ChangePathNodeDataAction) act).PropNum == this.PropNum;
    }

    @Override
    public void Merge(Action act)
    {
        this.NewV = ((ChangePathNodeDataAction) act).NewV;
    }

    /*    @Override
     public String toString()
     {
     return String.Format(LanguageManager.GetArrayList("UndoActions")[24], LanguageManager.Get("PathEditor", PropNum + 7).Replace(":", ""));
     }*/
}