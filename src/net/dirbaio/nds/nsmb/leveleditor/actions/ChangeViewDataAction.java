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
import net.dirbaio.nds.nsmb.level.NSMBView;

public class ChangeViewDataAction extends LvlItemAction
{

    int PropNum;
    ArrayList<Integer> OrigV;
    int NewV;

    public ChangeViewDataAction(ArrayList<LevelItem> objs, int PropNum, int NewV)
    {
        super(objs);
        OrigV = new ArrayList<>();
        this.PropNum = PropNum;
        this.NewV = NewV;
        for (int l = 0; l < this.objs.size(); l++)
            if (this.objs.get(l) instanceof NSMBView)
                OrigV.add(Read((NSMBView) this.objs.get(l)));
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
            Write((NSMBView) objs.get(l), OrigV.get(l));
    }

    @Override
    public void Redo()
    {
        for (LevelItem obj : objs)
            Write((NSMBView) obj, NewV);
    }

    private int Read(NSMBView v)
    {
        switch (PropNum)
        {
            case 1:
                return v.Number;
            case 2:
                return v.Music;
            case 3:
                return v.Unknown1;
            case 4:
                return v.Unknown2;
            case 5:
                return v.Unknown3;
            case 6:
                return v.Lighting;
            case 7:
                return v.FlagpoleID;
            case 8:
                return v.CameraTop;
            case 9:
                return v.CameraBottom;
            case 10:
                return v.CameraTopSpin;
            case 11:
                return v.CameraBottomSpin;
            case 12:
                return v.CameraBottomStick;
        }
        return 0;
    }

    private void Write(NSMBView v, int value)
    {
        switch (PropNum)
        {
            case 1:
                v.Number = value;
                break;
            case 2:
                v.Music = value;
                break;
            case 3:
                v.Unknown1 = value;
                break;
            case 4:
                v.Unknown2 = value;
                break;
            case 5:
                v.Unknown3 = value;
                break;
            case 6:
                v.Lighting = value;
                break;
            case 7:
                v.FlagpoleID = value;
                break;
            case 8:
                v.CameraTop = value;
                break;
            case 9:
                v.CameraBottom = value;
                break;
            case 10:
                v.CameraTopSpin = value;
                break;
            case 11:
                v.CameraBottomSpin = value;
                break;
            case 12:
                v.CameraBottomStick = value;
                break;
        }
    }

    @Override
    public boolean CanMerge(Action act)
    {
        if (!(act instanceof ChangeViewDataAction && sameItems(act)))
            return false;
        return ((ChangeViewDataAction) act).PropNum == this.PropNum;
    }

    @Override
    public void Merge(Action act)
    {
        this.NewV = ((ChangeViewDataAction) act).NewV;
    }

    /*
     @Override
     public String toString()
     {
     if (view.isZone)
     return LanguageManager.GetArrayList("UndoActions")[34];
     else
     return String.Format(LanguageManager.GetArrayList("UndoActions")[29], LanguageManager.Get("ViewEditor", PropNum + 6).Replace(":", ""));
     }*/
}