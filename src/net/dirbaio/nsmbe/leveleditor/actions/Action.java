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

import net.dirbaio.nsmbe.leveleditor.LevelEditorComponent;

public class Action
{

    LevelEditorComponent EdControl;
    public boolean cancel;

    public Action()
    {
    }

    public void Undo()
    {
    }

    public void Redo()
    {
    }

    public void AfterAction()
    {
    }

    public void AfterSetEdControl()
    {
    }

    public boolean CanMerge(Action act)
    {
        return false;
    }

    public void Merge(Action act)
    {
    }

    public void SetEdControl(LevelEditorComponent EdControl)
    {
        this.EdControl = EdControl;
        this.AfterSetEdControl();
    }
}