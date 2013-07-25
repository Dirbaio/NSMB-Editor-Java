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

import java.awt.image.BufferedImage;

public abstract class PixelPalettedImage extends PalettedImage
{

    public PixelPalettedImage()
    {
    }

    @Override
    public void replaceImgAndPal(BufferedImage b, Palette p)
    {
        p.pal = ImageIndexer.createPaletteForImage(b, p.pal.length);
        replaceWithPal(b, p);
    }

    @Override
    public void replaceWithPal(BufferedImage b, Palette p)
    {
        for (int x = 0; x < getWidth(); x++)
            for (int y = 0; y < getHeight(); y++)
            {
                int c = b.getRGB(x, y);
                int i = p.getClosestColor(c);
                setPixel(x, y, i);
            }
    }

    @Override
    public BufferedImage render(Palette p)
    {
        int w = getWidth();
        int h = getHeight();

        BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                b.setRGB(x, y, p.getColorSafe(getPixel(x, y)));

        return b;
    }

    public void setPixelData(byte[][] a, int xx, int yy)
    {
        int tx = getWidth();
        int ty = getHeight();

        for (int x = 0; x < tx; x++)
            for (int y = 0; y < ty; y++)
                setPixel(x, y, a[x + xx][y + yy]);
    }

    public void setPixelData(int[][] a, int xx, int yy)
    {
        int tx = getWidth();
        int ty = getHeight();

        for (int x = 0; x < tx; x++)
            for (int y = 0; y < ty; y++)
                setPixel(x, y, a[x + xx][y + yy]);
    }

    public abstract void setPixel(int x, int y, int c);

    public abstract int getPixel(int x, int y);
}
