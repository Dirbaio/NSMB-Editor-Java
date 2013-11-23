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

package net.dirbaio.nds.nsmb.leveleditor.docking;

import java.awt.Component;
import java.util.List;
import javax.swing.Icon;
import net.dirbaio.nds.nsmb.level.LevelItem;
import net.infonode.docking.*;

public abstract class NSMBeView extends View {
    public NSMBeView(String title, Icon icon, Component component) {
        super(title, icon, component);
    }
    
    public abstract void update(NSMBeTab tab);
    public abstract void selectObjects(List<LevelItem> objects);
}
