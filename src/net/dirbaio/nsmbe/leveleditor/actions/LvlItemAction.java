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

import java.awt.Rectangle;
import java.util.ArrayList;
import net.dirbaio.nsmbe.level.LevelItem;
import net.dirbaio.nsmbe.level.NSMBObject;

public class LvlItemAction extends Action
{

    public ArrayList<LevelItem> objs;

    public LvlItemAction(ArrayList<LevelItem> objs)
    {
        //This is important. We have to clone the list.
        this.objs = new ArrayList<>();
        this.objs.addAll(objs);

        if (objs.isEmpty())
            cancel = true;
    }

    @Override
    public void AfterAction()
    {
        SelectObjects();
    }

    public void SelectObjects()
    {
//        EdControl.SelectObject(objs);
    }

    public void Deselect()
    {
//        EdControl.SelectObject(null);
    }

    protected Rectangle getObjectRectangle()
    {
        boolean found = false;
        Rectangle r = new Rectangle(0, 0, 0, 0);

        for (LevelItem i : objs)
        {
            if(!(i instanceof NSMBObject))
                continue;
                
            NSMBObject o = (NSMBObject) i;

            if (found)
                Rectangle.union(r, o.getBlockRect(), r);
            else
                r = o.getBlockRect();
            found = true;
        }

        return r;
    }

    protected void repaintObjectRectangle()
    {
        Rectangle r = getObjectRectangle();
        EdControl.level.repaintTilemap(r.x, r.y, r.width, r.height);
    }

    protected boolean sameItems(Action act)
    {
        return act instanceof LvlItemAction && equalArrayLists(((LvlItemAction) act).objs, objs);
    }

    private boolean equalArrayLists(ArrayList<LevelItem> a, ArrayList<LevelItem> b)
    {
        if (a.size() != b.size())
            return false;

        for (int i = 0; i < a.size(); i++)
            if (a.get(i) != b.get(i))
                return false;

        return true;
    }
}