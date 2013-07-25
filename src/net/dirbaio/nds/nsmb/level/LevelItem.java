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
package net.dirbaio.nds.nsmb.level;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.dirbaio.nds.nsmb.leveleditor.LevelEditorComponent;

public interface LevelItem
{

    public Rectangle getRect();

    public void setRect(Rectangle r);

    //This is the "real" object rectangle.
    //It is useful for knowing the real position and size,
    //since the real position shouldn't go out of the level space.
    //Though, the "drawn" position can. For example, see sprite End-of-level Flag.
    public Rectangle getRealRect();

    //If it's not resizable, width.set and height.set just do nothing.
    public boolean isResizable();

    //Objects and sprites have snap 16, the others have snap 1.
    //x, y, width, height should always be multiples of snap.
    //Setting them to something that's not multiple of snap is OK, though.
    public int getSnap();

    //Renders the object itself.
    public void render(Graphics2D g, LevelEditorComponent ed);
}
