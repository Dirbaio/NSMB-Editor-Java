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

public class BackgroundDragEditionMode extends EditionMode
{

    public BackgroundDragEditionMode(NSMBLevel l, LevelEditorControl edc)
    
    
    : base(l, edc) { }

        int dx;
    int dy;

    @Override
    public void MouseDown(int x, int y, System.Windows.Forms.MouseButtons buttons)
    {
        dx = EdControl.bgX - x;
        dy = EdControl.bgY - y;
    }

    @Override
    public void MouseDrag(int x, int y)
    {
        EdControl.bgX = dx + x;
        EdControl.bgY = dy + y;
        EdControl.repaint();
    }

    @Override
    public void RenderSelection(System.Drawing.Graphics g)
    {
    }

    @Override
    public void SelectObject(object o)
    {
    }

    @Override
    public void Refresh()
    {
    }
}
