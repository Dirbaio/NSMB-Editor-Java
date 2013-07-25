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
package net.dirbaio.nds.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import net.dirbaio.nds.fs.AlreadyEditingException;
import net.dirbaio.nds.fs.File;
import net.dirbaio.nds.util.ArrayReader;
import net.dirbaio.nds.util.Util;

public class Image3Dformat5 extends PalettedImage
{

    File f;
    File f5;
    int width, height;

    public Image3Dformat5(File f, File f5, int width, int height)
    {
        this.f = f;
        this.f5 = f5;
//            f.beginEdit(this);
//            f5.beginEdit(this);
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Override
    public BufferedImage render(Palette p)
    {
        int w = getWidth();
        int h = getHeight();

        BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        ArrayReader f5data = new ArrayReader(f5.getContents());
        ArrayReader data = new ArrayReader(f.getContents());

        for (int y = 0; y < h / 4; y++)
            for (int x = 0; x < w / 4; x++)
            {
                int palDat = f5data.readShort();
                short palOffs = (short) ((palDat & 0x3FFF) * 2);
                short mode = (short) ((palDat >> 14) & 3);

                for (int yy = 0; yy < 4; yy++)
                {
                    byte row = data.readByte();
                    for (int xx = 0; xx < 4; xx++)
                    {
                        byte color = (byte) (row >> (byte) (xx * 2));
                        color &= 3;
                        int col;
                        col = p.getColorSafe(palOffs + color);
                        switch (mode)
                        {
                            case 0:
                                if (color == 3)
                                    col = 0;
                                break;
                            case 1:
                                if (color == 2)
                                    col = Util.colorMean(p.getColorSafe(palOffs), p.getColorSafe(palOffs + 1), 1, 1);
                                if (color == 3)
                                    col = 0;
                                break;
                            case 3:
                                if (color == 2)
                                    col = Util.colorMean(p.getColorSafe(palOffs), p.getColorSafe(palOffs + 1), 5, 3);
                                if (color == 3)
                                    col = Util.colorMean(p.getColorSafe(palOffs), p.getColorSafe(palOffs + 1), 3, 5);
                                break;
                        }
                        b.setRGB(x * 4 + xx, y * 4 + yy, col);
                    }
                }
            }
        return b;
    }

    @Override
    public void endEdit()
    {
        f.endEdit(this);
        f5.endEdit(this);
    }

    @Override
    public void beginEdit() throws AlreadyEditingException
    {
        f.beginEdit(this);
        f5.beginEdit(this);
    }

    @Override
    public void replaceWithPal(BufferedImage b, Palette p)
    {
        throw new RuntimeException("Not allowed on these image types!");
    }

    @Override
    public void replaceImgAndPal(BufferedImage b, Palette p)
    {
        ImageTexeler it = new ImageTexeler(b, (int) p.pal.length / 4);
        f.replace(it.texdata, this);
        f5.replace(it.f5data, this);

        p.pal = it.finalPalette;
        p.save();
    }

    @Override
    public void save()
    {
    }

    @Override
    public byte[] getRawData()
    {
        return null;
    }

    @Override
    public void setRawData(byte[] data)
    {
    }

    @Override
    public String toString()
    {
        return name;
    }
}
