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

import java.util.Arrays;
import net.dirbaio.nsmbe.util.LanguageManager;

public class ChangeLevelSettingsAction extends Action
{

    byte[][] oldData, newData;

    public ChangeLevelSettingsAction(byte[][] newData)
    {
        this.newData = newData;
    }

    @Override
    public void Undo()
    {
        EdControl.level.Blocks = Clone(oldData);
        EdControl.level.CalculateSpriteModifiers();
        //EdControl.config.LoadSettings();
        //int oldTileset = newData[0][0xC];
        //int oldBottomBg = newData[0][6];
    }

    @Override
    public void Redo()
    {
        EdControl.level.Blocks = Clone(newData);
        EdControl.level.CalculateSpriteModifiers();
        //EdControl.config.LoadSettings();
    }

    @Override
    public void AfterSetEdControl()
    {
        this.oldData = Clone(EdControl.level.Blocks);
    }

    @Override
    public void AfterAction()
    {
//        if (newData[0][0xC] != oldData[0][0xC] || newData[0][6] != oldData[0][6])
//            EdControl.editor.LevelConfigForm_ReloadTileset();
    }

    @Override
    public String toString()
    {
        return LanguageManager.GetArrayList("UndoActions")[36];
    }

    public static byte[][] Clone(byte[][] data)
    {
        int len = data.length;
        byte[][] newData = new byte[len][];
        for (int l = 0; l < len; l++)
            newData[l] = Arrays.copyOf(data[l], data[l].length);
        return newData;
    }
}