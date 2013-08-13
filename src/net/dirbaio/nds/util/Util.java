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
package net.dirbaio.nds.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

public class Util
{

    /**
     * Converts a color from RGB15 to RGB24
     *
     * @param x
     * @return
     */
    public static int fromRGB15(int x)
    {
        int r = x & 0x1F;
        int g = (x >> 5) & 0x1F;
        int b = (x >> 10) & 0x1F;
        r = (r << 3) | (r >> 2);
        g = (g << 3) | (g >> 2);
        b = (b << 3) | (b >> 2);

        int a = 255;
        return (r << 16) | (g << 8) | (b) | (a << 24);
    }

    /**
     * Converts a color from RGB24 to RGB15
     *
     * @param x
     * @return
     */
    public static int toRGB15(int x)
    {
        int r = (x >> 16) & 0xFF;
        int g = (x >> 8) & 0xFF;
        int b = (x) & 0xFF;
        int a = (x >> 24) & 0xFF;
        r = r >> 3;
        g = g >> 3;
        b = b >> 3;

        return r | (g << 5) | (b << 10) | (1 << 15);
    }

    /**
     * Converts an ARGB int to a Color.
     *
     * @param x
     * @return
     */
    public static Color toColor(int x)
    {
        return new Color(x, true);
    }

    /**
     * Calculates the difference between two colors. Returns +INF if different
     * alpha.
     *
     * @param a
     * @param b
     * @return
     */
    public static float colorDiff(int a, int b)
    {
        int ar = a & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = (a >> 16) & 0xFF;
        int aa = (a >> 24) & 0xFF;
        int br = a & 0xFF;
        int bg = (a >> 8) & 0xFF;
        int bb = (a >> 16) & 0xFF;
        int ba = (a >> 24) & 0xFF;

        int dr = ar - br;
        int dg = ag - bg;
        int db = ab - bb;
        int da = aa - ba;

        if (da != 0)
            return Float.POSITIVE_INFINITY;
        else
            return dr * dr + dg * dg + db * db;
    }

    public static int colorDiffAlpha(int a, int b)
    {
        int ar = a & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = (a >> 16) & 0xFF;
        int aa = (a >> 24) & 0xFF;
        int br = a & 0xFF;
        int bg = (a >> 8) & 0xFF;
        int bb = (a >> 16) & 0xFF;
        int ba = (a >> 24) & 0xFF;

        int dr = ar - br;
        int dg = ag - bg;
        int db = ab - bb;
        int da = aa - ba;

        return dr * dr + dg * dg + db * db + da * da;
    }

    public static int colorMean(int a, int b, int wa, int wb)
    {
        int ar = a & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = (a >> 16) & 0xFF;
        int aa = (a >> 24) & 0xFF;
        int br = a & 0xFF;
        int bg = (a >> 8) & 0xFF;
        int bb = (a >> 16) & 0xFF;
        int ba = (a >> 24) & 0xFF;

        int rr = (ar * wa + br * wb) / (wa + wb);
        int rg = (ag * wa + bg * wb) / (wa + wb);
        int rb = (ab * wa + bb * wb) / (wa + wb);

        int ra = 255;
        return rr | (rg << 8) | (rb << 16) | (ra << 24);
    }

    public static int closestColor(int c, int[] palette)
    {
        int best = 0;
        float bestDif = Util.colorDiff(c, palette[0]);
        for (int i = 0; i < palette.length; i++)
        {
            float dif = Util.colorDiff(c, palette[i]);
            if (dif < bestDif)
            {
                bestDif = dif;
                best = i;
            }
        }

        return best;
    }

    public static void drawImage(Graphics g, BufferedImage im, Rectangle dst, Rectangle src)
    {
        g.drawImage(im,
                dst.x, dst.y, dst.x + dst.width, dst.y + dst.height,
                src.x, src.y, src.x + src.width, src.y + src.height, null);
    }

    public static void drawImage(Graphics g, BufferedImage im, int dstx, int dsty, Rectangle src)
    {
        g.drawImage(im,
                dstx, dsty, dstx + src.width, dsty + src.height,
                src.x, src.y, src.x + src.width, src.y + src.height, null);
    }

    public static void drawTransparent(Graphics g, int x, int y, int width, int height)
    {
        Shape clip = g.getClip();
        g.clipRect(x, y, width, height);
        int size = 8;
        for (int xx = 0; xx <= width / size; xx++)
            for (int yy = 0; yy <= height / size; yy++)
            {
                if ((xx + yy) % 2 == 0)
                    g.setColor(Color.LIGHT_GRAY);
                else
                    g.setColor(Color.GRAY);
                g.fillRect(x + xx * size, y + yy * size, size, size);
            }
        g.setClip(clip);
    }

    public enum RotateFlipType
    {

        Rotate180FlipNone,
        Rotate180FlipX,
        Rotate180FlipXY,
        Rotate180FlipY,
        Rotate270FlipNone,
        Rotate270FlipX,
        Rotate270FlipXY,
        Rotate270FlipY,
        Rotate90FlipNone,
        Rotate90FlipX,
        Rotate90FlipXY,
        Rotate90FlipY,
        RotateNoneFlipNone,
        RotateNoneFlipX,
        RotateNoneFlipXY,
        RotateNoneFlipY,
    }

    public static BufferedImage rotateFlip(BufferedImage img, RotateFlipType t)
    {
        //TODO
        return img;
    }

    public static String hex(int n)
    {
        return String.format("0x%08X", n);
    }

    public static String num(int n)
    {
        return String.format("%03d", n);
    }
    
    public static int roundDown(int i, int snap)
    {
        return i - i%snap;
    }
    public static int roundUp(int i, int snap)
    {
        return roundDown(i+snap-1, snap);
    }
}
