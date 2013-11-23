/*
 * Copyright (C) 2013 Piranhaplant
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

package net.dirbaio.nds.nsmb.level;

import java.util.ArrayList;
import java.util.Iterator;
import net.dirbaio.nds.util.MultiIterator;

public class LevelObjects implements Iterable<LevelItem> {
    
    public ArrayList<NSMBObject> Objects = new ArrayList<>();
    public ArrayList<NSMBSprite> Sprites = new ArrayList<>();
    public ArrayList<NSMBEntrance> Entrances = new ArrayList<>();
    public ArrayList<NSMBView> Views = new ArrayList<>();
    public ArrayList<NSMBView> Zones = new ArrayList<>();
    public ArrayList<NSMBPath> Paths = new ArrayList<>();
    public ArrayList<NSMBPath> ProgressPaths = new ArrayList<>();
    
    public void remove(ArrayList<LevelItem> objs)
    {
        for (LevelItem obj : objs)
            remove(obj);
    }

    public void remove(LevelItem obj)
    {
        if (obj instanceof NSMBObject)
            Objects.remove((NSMBObject) obj);
        if (obj instanceof NSMBSprite)
            Sprites.remove((NSMBSprite) obj);
        if (obj instanceof NSMBEntrance)
            Entrances.remove((NSMBEntrance) obj);
        if (obj instanceof NSMBView)
        {
            NSMBView v = (NSMBView) obj;
            if (v.isZone)
                Zones.remove(v);
            else
                Views.remove(v);
        }
        if (obj instanceof NSMBPathPoint)
        {
            NSMBPathPoint pp = (NSMBPathPoint) obj;
            pp.parent.points.remove(pp);
            if (pp.parent.points.isEmpty())
                if (pp.parent.isProgressPath)
                    ProgressPaths.remove(pp.parent);
                else
                    Paths.remove(pp.parent);
        }
    }

    public ArrayList<Integer> removeZIndex(ArrayList<LevelItem> objs)
    {
        ArrayList<Integer> zIndex = new ArrayList<>();
        for (LevelItem obj : objs)
            zIndex.add(removeZIndex(obj));
        return zIndex;
    }

    public int removeZIndex(LevelItem obj)
    {
        int idx = -1;
        if (obj instanceof NSMBObject)
        {
            idx = Objects.indexOf(obj);
            Objects.remove((NSMBObject) obj);
        }
        if (obj instanceof NSMBSprite)
        {
            idx = Sprites.indexOf(obj);
            Sprites.remove((NSMBSprite) obj);
        }
        if (obj instanceof NSMBEntrance)
        {
            idx = Entrances.indexOf(obj);
            Entrances.remove((NSMBEntrance) obj);
        }
        if (obj instanceof NSMBView)
        {
            NSMBView v = (NSMBView) obj;
            if (v.isZone)
            {
                idx = Zones.indexOf(v);
                Zones.remove(v);
            } else
            {
                idx = Views.indexOf(v);
                Views.remove(v);
            }
        }
        if (obj instanceof NSMBPathPoint)
        {
            NSMBPathPoint pp = (NSMBPathPoint) obj;
            idx = pp.parent.points.indexOf(pp);
            pp.parent.points.remove(pp);
            if (pp.parent.points.isEmpty())
                if (pp.parent.isProgressPath)
                    ProgressPaths.remove(pp.parent);
                else
                    Paths.remove(pp.parent);
        }
        return idx == -1 ? 0 : idx;
    }

    public void add(ArrayList<LevelItem> objs)
    {
        for (LevelItem obj : objs)
            add(obj);
    }

    public void add(LevelItem obj)
    {
        if (obj instanceof NSMBObject)
            Objects.add((NSMBObject) obj);
        if (obj instanceof NSMBSprite)
            Sprites.add((NSMBSprite) obj);
        if (obj instanceof NSMBEntrance)
            Entrances.add((NSMBEntrance) obj);
        if (obj instanceof NSMBView)
        {
            NSMBView v = (NSMBView) obj;
            if (v.isZone)
                Zones.add(v);
            else
                Views.add(v);
        }
        if (obj instanceof NSMBPathPoint)
        {
            NSMBPathPoint pp = (NSMBPathPoint) obj;
            pp.parent.points.add(pp);
            if (pp.parent.isProgressPath)
            {
                if (!ProgressPaths.contains(pp.parent))
                    ProgressPaths.add(pp.parent);
            } else if (!Paths.contains(pp.parent))
                Paths.add(pp.parent);
        }
    }

    public void add(ArrayList<LevelItem> objs, ArrayList<Integer> zIndex)
    {
        //This needs to iterate in reverse order to preserve the correct z-index

        for (int i = objs.size() - 1; i >= 0; i--)
            add(objs.get(i), zIndex.get(i));
    }

    public void add(LevelItem obj, int zIndex)
    {
        if (obj instanceof NSMBObject)
            Objects.add(zIndex, (NSMBObject) obj);
        if (obj instanceof NSMBSprite)
            Sprites.add(zIndex, (NSMBSprite) obj);
        if (obj instanceof NSMBEntrance)
            Entrances.add(zIndex, (NSMBEntrance) obj);
        if (obj instanceof NSMBView)
        {
            NSMBView v = (NSMBView) obj;
            if (v.isZone)
                Zones.add(zIndex, v);
            else
                Views.add(zIndex, v);
        }
        if (obj instanceof NSMBPathPoint)
        {
            NSMBPathPoint pp = (NSMBPathPoint) obj;
            pp.parent.points.add(zIndex, pp);
            if (pp.parent.isProgressPath)
            {
                if (!ProgressPaths.contains(pp.parent))
                    ProgressPaths.add(pp.parent);
            } else if (!Paths.contains(pp.parent))
                Paths.add(pp.parent);
        }
    }

    @Override
    public Iterator<LevelItem> iterator() {
        MultiIterator<LevelItem> i = new MultiIterator<>();
        i.add(Objects.iterator());
        i.add(Sprites.iterator());
        i.add(Entrances.iterator());
        i.add(Views.iterator());
        i.add(Zones.iterator());
        for (NSMBPath p : Paths)
            i.add(p.points.iterator());
        for (NSMBPath p : ProgressPaths)
            i.add(p.points.iterator());
        return i;
    }
}
