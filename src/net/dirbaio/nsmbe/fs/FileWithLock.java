/*
*   This file is part of NSMB Editor 5.
*
*   NSMB Editor 5 is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   NSMB Editor 5 is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with NSMB Editor 5.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.dirbaio.nsmbe.fs;

import java.util.ArrayList;
import java.util.List;

public abstract class FileWithLock extends File
{
    public FileWithLock() {}

    public FileWithLock(Filesystem parent, Directory parentDir, String name, int id)
    {
        super(parent, parentDir, name, id);
    }

    // HANDLE EDITIONS

    // Invariants:
    // editedBy == null || editedIntervals.count == 0
    // No two intervals in editedIntervals have intersection

    protected Object editedBy;
    protected List<Interval> editedIntervals = new ArrayList<>();

    protected class Interval {
        public int start, end;
        public Interval(int start, int end)
        {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj instanceof Interval)
                return this.start == ((Interval)obj).start && 
                        this.end == ((Interval)obj).end;
            else
                return false;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 31 * hash + this.start;
            hash = 31 * hash + this.end;
            return hash;
        }
        
    }

    @Override
    public void beginEdit(Object editor) throws AlreadyEditingException
    {
        editionStarted();

        if (editedBy != null || !editedIntervals.isEmpty())
            throw new AlreadyEditingException(this);
        else
            editedBy = editor;
    }

    @Override
    public void endEdit(Object editor)
    {
        if (editor == null || editor != editedBy)
            throw new RuntimeException("Not correct editor: " + name);

        editedBy = null;
        editionEnded();
    }

    @Override
    public void beginEditInterval(int start, int end) throws AlreadyEditingException
    {
        validateInterval(start, end);

        if (editedBy != null)
            throw new AlreadyEditingException(this);

        if(editedIntervals.isEmpty())
            editionStarted();

        for(Interval i : editedIntervals)
            if(i.start < end && start < i.end)
                throw new AlreadyEditingException(this);

        editedIntervals.add(new Interval(start, end));
    }

    @Override
    public void endEditInterval(int start, int end)
    {
        validateInterval(start, end);

        if(!editedIntervals.remove(new Interval(start, end)))
            throw new RuntimeException("Not correct interval: " + name);

        if(editedIntervals.isEmpty())
            editionEnded();
    }

    @Override
    public boolean beingEditedBy(Object ed)
    {
        return ed == editedBy;
    }

    protected boolean isAGoodEditor(Object editor)
    {
        return editor == editedBy;
    }

    public void editionStarted() throws AlreadyEditingException {}
    public void editionEnded() {}
    
}

