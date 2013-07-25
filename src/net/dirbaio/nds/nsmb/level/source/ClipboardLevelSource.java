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
package net.dirbaio.nds.nsmb.level.source;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.xml.bind.DatatypeConverter;
import net.dirbaio.nds.util.LZ;

public class ClipboardLevelSource extends LevelSource implements ClipboardOwner
{

    public static final String backupInfoString = "Clipboard";
    public static final String clipboardHeader = "NSMBeLevel|";
    public static final String clipboardFooter = "|";

    public ClipboardLevelSource()
    {
    }

    @Override
    public LevelData getData() throws LevelIOException
    {
        String leveltxt = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //odd: the Object param of getContents is not currently used
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText =
                (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText)
            try
            {
                leveltxt = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException ex)
            {
                throw new LevelIOException(ex);
            }

        if (!(leveltxt.startsWith(clipboardHeader) && leveltxt.endsWith(clipboardFooter)))
            throw new LevelIOException("Clipboard contents aren't a NSMB level.");
        leveltxt = leveltxt.substring(11, leveltxt.length() - 12);
        byte[] leveldata = DatatypeConverter.parseBase64Binary(leveltxt);
        leveldata = LZ.decompress(leveldata);

        return new LevelData(leveldata);
    }

    @Override
    public void setData(LevelData level) throws LevelIOException
    {
        byte[] data = level.WriteToArray();

        data = LZ.compress(data);
        String str = DatatypeConverter.printBase64Binary(data);

        StringSelection stringSelection = new StringSelection(str);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);

    }

    @Override
    public String getName()
    {
        return "Clipboard level";
    }

    @Override
    public void open()
    {
        // nothing to do here
    }

    @Override
    public void close()
    {
        // nothing to do here
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents)
    {
        //wtf is this
    }
}
