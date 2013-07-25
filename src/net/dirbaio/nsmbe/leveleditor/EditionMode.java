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
package net.dirbaio.nsmbe.leveleditor;

import java.awt.Graphics;
import javax.swing.JComponent;
import net.dirbaio.nsmbe.level.NSMBLevel;

public abstract class EditionMode
{

    public LevelEditorComponent EdControl;

    public EditionMode(LevelEditorComponent EdControl)
    {
        this.EdControl = EdControl;
    }

    public abstract void MouseDown(int x, int y, MouseButtons buttons);

    public abstract void MouseDrag(int x, int y);

    public void MouseUp()
    {
    }

    public void MouseMove(int x, int y)
    {
    }

    public abstract void RenderSelection(Graphics g);

    public abstract void SelectObject(Object o);

    public abstract void Refresh();

    public void SelectAll()
    {
    }

    public void DeleteObject()
    {
    }

    public String copy()
    {
        return "";
    }

    public void paste(String contents)
    {
    }

    public void lower()
    {
    }

    public void raise()
    {
    }

    public void MoveObjects(int xDelta, int yDelta)
    {
    }
}
