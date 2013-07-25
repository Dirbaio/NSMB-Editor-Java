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

//This always overwrites all of the sprite data
import java.util.ArrayList;
import java.util.Arrays;
import net.dirbaio.nsmbe.level.LevelItem;
import net.dirbaio.nsmbe.level.NSMBSprite;
import net.dirbaio.nsmbe.util.LanguageManager;

//not just the property that was changed
public class ChangeSpriteDataAction extends LvlItemAction
{

    byte[][] OrigData;
    byte[] NewData;

    public ChangeSpriteDataAction(ArrayList<LevelItem> objs, byte[] NewData)
    {
        super(objs);
        for (int l = 0; l < this.objs.size(); l++)
            if (!(this.objs.get(l) instanceof NSMBSprite))
                this.objs.remove(l--);
        if (this.objs.isEmpty())
            cancel = true;
        OrigData = new byte[this.objs.size()][];
        for (int l = 0; l < this.objs.size(); l++)
            OrigData[l] = Arrays.copyOf(((NSMBSprite) this.objs.get(l)).Data, 6);
        this.NewData = NewData;
    }

    @Override
    public void Undo()
    {
        for (int l = 0; l < objs.size(); l++)
            ((NSMBSprite) objs.get(l)).Data = Arrays.copyOf(OrigData[l], 6);
    }

    @Override
    public void Redo()
    {
        for (LevelItem obj : objs)
            ((NSMBSprite) obj).Data = Arrays.copyOf(NewData, 6);
    }

    @Override
    public boolean CanMerge(Action act)
    {
        return act instanceof ChangeSpriteDataAction && sameItems(act);
    }

    @Override
    public void Merge(Action act)
    {
        ChangeSpriteDataAction csda = (ChangeSpriteDataAction) act;
        this.NewData = csda.NewData;
    }

    @Override
    public void AfterAction()
    {
//        if (EdControl.mode instanceof ObjectsEditionMode)
//            ((ObjectsEditionMode) EdControl.mode).tabs.UpdateInfo();
    }

    @Override
    public String toString()
    {
        return LanguageManager.GetArrayList("UndoActions")[9];
    }
}