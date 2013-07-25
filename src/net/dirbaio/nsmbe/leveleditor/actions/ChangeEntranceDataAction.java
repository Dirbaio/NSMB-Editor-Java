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
import net.dirbaio.nsmbe.level.NSMBEntrance;
import net.dirbaio.nsmbe.util.LanguageManager;

public class ChangeEntranceDataAction extends LvlItemAction
{

    int PropNum;
    ArrayList<Integer> OrigV;
    int NewV;

    public ChangeEntranceDataAction(ArrayList<LevelItem> objs, int PropNum, int NewV)
    {
        super(objs);
        OrigV = new ArrayList<>();
        this.NewV = NewV;
        this.PropNum = PropNum;
        for (int l = 0; l < this.objs.size(); l++)
            if (this.objs.get(l) instanceof NSMBEntrance)
                OrigV.add(Read((NSMBEntrance) this.objs.get(l)));
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
            Write((NSMBEntrance) objs.get(l), OrigV.get(l));
    }

    @Override
    public void Redo()
    {
        for (LevelItem obj : objs)
            Write((NSMBEntrance) obj, NewV);
    }

    private int Read(NSMBEntrance e)
    {
        switch (PropNum)
        {
            case 0:
                return e.CameraX;
            case 1:
                return e.CameraY;
            case 2:
                return e.Number;
            case 3:
                return e.DestArea;
            case 4:
                return e.DestEntrance;
            case 5:
                return e.ConnectedPipeID;
            case 6:
                return e.EntryView;
            case 7:
                return e.Type;
            case 8:
                return e.Settings;
        }
        return 0;
    }

    private void Write(NSMBEntrance e, int value)
    {
        switch (PropNum)
        {
            case 0:
                e.CameraX = value;
                break;
            case 1:
                e.CameraY = value;
                break;
            case 2:
                e.Number = value;
                break;
            case 3:
                e.DestArea = value;
                break;
            case 4:
                e.DestEntrance = value;
                break;
            case 5:
                e.ConnectedPipeID = value;
                break;
            case 6:
                e.EntryView = value;
                break;
            case 7:
                e.Type = value;
                break;
            case 8:
                e.Settings = value;
                break;
        }
    }

    @Override
    public boolean CanMerge(Action act)
    {
        if (!(act instanceof ChangeEntranceDataAction && sameItems(act)))
            return false;
        return ((ChangeEntranceDataAction) act).PropNum == this.PropNum;
    }

    @Override
    public void Merge(Action act)
    {
        this.NewV = ((ChangeEntranceDataAction) act).NewV;
    }

    /*    @Override
     public String toString()
     {
     return String.Format(LanguageManager.GetArrayList("UndoActions")[16], LanguageManager.Get("EntranceEditor", PropNum + 5).Replace(":", ""));
     }*/
}