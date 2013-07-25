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
package net.dirbaio.nsmbe.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.dirbaio.nsmbe.fs.AlreadyEditingException;
import net.dirbaio.nsmbe.fs.File;
import net.dirbaio.nsmbe.util.Util;

public class Image2D extends PixelPalettedImage
{

    private File f;
    protected byte[] data;
    private byte[] rawdata;
    public int width;
    public int tileOffset;
    private boolean is4bppI;

    public Image2D(File f, int width, boolean is4bpp)
    {
        this.f = f;
        this.is4bppI = is4bpp;
        this.width = width;

        reload();
    }

    public void reload()
    {
        rawdata = f.getContents();
        loadImageData();
    }

    public boolean is4bpp()
    {
        return is4bppI;
    }

    public void set4bpp(boolean value)
    {
        if (value == is4bppI)
            return;
        saveImageData();
        is4bppI = value;
        loadImageData();
    }

    @Override
    public void beginEdit() throws AlreadyEditingException
    {
        f.beginEdit(this);
    }

    @Override
    public void endEdit()
    {
        f.endEdit(this);
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        int tileCount = data.length / 64 + tileOffset;

        int tileWidth = (width / 8);
        int rowCount = tileCount / tileWidth;
        if (tileCount % tileWidth != 0)
            rowCount++;

        return rowCount * 8;
    }

    @Override
    public int getPixel(int x, int y)
    {
        int bx = x / 8;
        int by = y / 8;

        int offs = bx + by * (width / 8) - tileOffset;
        if (offs < 0)
            return 0;
        offs *= 64;
        offs += x % 8 + 8 * (y % 8);
        if (offs < 0)
            return 0;
        if (offs >= data.length)
            return 0;
        return data[offs] & 0xFF;
    }

    @Override
    public void setPixel(int x, int y, int c)
    {
        int bx = x / 8;
        int by = y / 8;

        int offs = bx + by * (width / 8) - tileOffset;
        if (offs < 0)
            return;

        offs *= 64;
        offs += x % 8 + 8 * (y % 8);
        if (offs < 0)
            return;
        if (offs >= data.length)
            return;
        data[offs] = (byte) c;
    }

    private void saveImageData()
    {
        if (is4bppI)
        {
            rawdata = new byte[data.length / 2];

            for (int i = 0; i < rawdata.length; i++)
                rawdata[i] = (byte) (data[i * 2] & 0xF
                        | (data[i * 2 + 1] & 0xF) << 4);
        } else
            rawdata = (byte[]) data.clone();
    }

    private void loadImageData()
    {
        if (is4bppI)
        {
            byte[] newdata = new byte[rawdata.length * 2];
            for (int i = 0; i < rawdata.length; i++)
            {
                newdata[i * 2] = (byte) (rawdata[i] & 0xF);
                newdata[i * 2 + 1] = (byte) ((rawdata[i] >> 4) & 0xF);
            }
            data = newdata;
        } else
            data = (byte[]) rawdata.clone();
    }

    @Override
    public void save()
    {
        saveImageData();
        f.replace(rawdata, this);
    }

    @Override
    public byte[] getRawData()
    {
        return data;
    }

    @Override
    public void setRawData(byte[] data)
    {
        this.data = (byte[]) data.clone();
    }

    public static Rectangle getTileRectangle(BufferedImage b, int tileSize, int tilenum)
    {
        int tileCountX = b.getWidth() / tileSize;
        int tileCountY = b.getHeight() / tileSize;
        int tileCount = tileCountX * tileCountY;

        if (tilenum >= tileCount)
            throw new ArrayIndexOutOfBoundsException("Tile number out of bounds!");

        int x = (tilenum % tileCountX) * tileSize;
        int y = (tilenum / tileCountX) * tileSize;

        return new Rectangle(x, y, tileSize, tileSize);
    }

    public static BufferedImage CutImage(BufferedImage im, int width, int blockrows)
    {
        int blocksize = im.getHeight() / blockrows;
        int blockcount = im.getWidth() / blocksize;

        int cols = width / blocksize;
        int rows = blockcount / cols;

        BufferedImage b = new BufferedImage(cols * blocksize, rows * blocksize * blockrows, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = b.createGraphics();
        g.setBackground(Color.GRAY);

        Rectangle SourceRect = new Rectangle(0, 0, cols * blocksize, blocksize);
        Rectangle DestRect = new Rectangle(0, 0, cols * blocksize, blocksize);


        for (int r = 0; r < blockrows; r++)
        {
            SourceRect.y = r * blocksize;
            for (int i = 0; i < rows; i++)
            {
                SourceRect.x = i * cols * blocksize;
                DestRect.y = i * blocksize + r * rows * blocksize;

                Util.drawImage(g, im, DestRect, SourceRect);
            }
        }

        return b;
    }

    @Override
    public String toString()
    {
        return f.getName();
    }
}
