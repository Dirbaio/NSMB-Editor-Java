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

package net.dirbaio.nds.fsbrowser.actions;

import net.dirbaio.nds.fs.File;
import net.dirbaio.nds.fsbrowser.FileHexEditor;
import net.dirbaio.nds.util.ComponentFrame;
import net.dirbaio.nds.util.FileType;


public class HexEditAction implements FileAction {

    @Override
    public boolean canDoOn(FileType ft)
    {
        return true;
    }

    @Override
    public void doOn(File f)
    {
        new ComponentFrame(new FileHexEditor(f));
    }

    @Override
    public String toString()
    {
        return "Hex edit";
    }

    
}
