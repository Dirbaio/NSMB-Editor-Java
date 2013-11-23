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

package net.dirbaio.nds.util;

import java.util.ArrayList;
import java.util.Iterator;

public class MultiIterator<T> implements Iterator<T>
{
    private ArrayList<Iterator<? extends T>> iterators;
    private int curIter = 0;
    
    public MultiIterator()
    {
        iterators = new ArrayList<>();
    }
    
    public void add(Iterator<? extends T> iterator)
    {
        iterators.add(iterator);
    }

    @Override
    public boolean hasNext() {
        return curIter != iterators.size();
    }

    @Override
    public T next() {
        T ret = iterators.get(curIter).next();
        while (curIter < iterators.size() && !iterators.get(curIter).hasNext()) {
            curIter++;
        }
        return ret;
    }

    @Override
    public void remove() {
        iterators.get(curIter).remove();
        if (!iterators.get(curIter).hasNext())
            curIter++;
    }
}
