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

import java.awt.Rectangle;
import java.util.ArrayList;
import net.dirbaio.nds.nsmb.level.LevelItem;
import net.dirbaio.nds.nsmb.level.NSMBObject;

public class MoveResizeLvlItemAction extends LvlItemAction
{

    int XDelta, YDelta;
    int XSDelta, YSDelta;

    public MoveResizeLvlItemAction(ArrayList<LevelItem> objs, int XDelta, int YDelta, int XSDelta, int YSDelta)
    {
        super(objs);
        this.XDelta = XDelta;
        this.YDelta = YDelta;
        this.XSDelta = XSDelta;
        this.YSDelta = YSDelta;
    }

    public MoveResizeLvlItemAction(ArrayList<LevelItem> objs, int XDelta, int YDelta)
    {
        super(objs);
        this.XDelta = XDelta;
        this.YDelta = YDelta;
        this.XSDelta = 0;
        this.YSDelta = 0;
    }

    @Override
    public void Undo()
    {
        Rectangle obr = getObjectRectangle();

        for (LevelItem obj : objs)
        {
            Rectangle r = obj.getRect();
            r.x -= XDelta;
            r.y -= YDelta;
            r.width -= XSDelta;
            r.height -= YSDelta;
            obj.setRect(r);
            if (obj instanceof NSMBObject && (this.XSDelta != 0 || this.YSDelta != 0))
                ((NSMBObject) obj).UpdateObjCache();
        }
        Rectangle.union(obr, getObjectRectangle(), obr);
        EdControl.level.repaintTilemap(obr.x, obr.y, obr.width, obr.height);
    }

    @Override
    public void Redo()
    {
        Rectangle obr = getObjectRectangle();

        for (LevelItem obj : objs)
        {
            Rectangle r = obj.getRect();
            r.x += XDelta;
            r.y += YDelta;
            r.width += XSDelta;
            r.height += YSDelta;
            obj.setRect(r);
            if (obj instanceof NSMBObject && (this.XSDelta != 0 || this.YSDelta != 0))
                ((NSMBObject) obj).UpdateObjCache();
        }
        Rectangle.union(obr, getObjectRectangle(), obr);
        EdControl.level.repaintTilemap(obr.x, obr.y, obr.width, obr.height);
    }

    @Override
    public boolean CanMerge(Action act)
    {
        return act instanceof MoveResizeLvlItemAction && sameItems(act);
    }

    @Override
    public void Merge(Action act)
    {
        MoveResizeLvlItemAction mlia = (MoveResizeLvlItemAction) act;
        this.XDelta += mlia.XDelta;
        this.YDelta += mlia.YDelta;
        this.XSDelta += mlia.XSDelta;
        this.YSDelta += mlia.YSDelta;
    }
}