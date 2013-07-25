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
import net.dirbaio.nsmbe.util.ArrayWriter;
import net.dirbaio.nsmbe.util.Util;

public class ImageTexeler
{

    BufferedImage img;
    int[][] palettes;
    int[] paletteCounts;
    int[][] paletteNumbers;
    float[][] paletteDiffs;
    public byte[] f5data, texdata;
    public int[] finalPalette;

    public ImageTexeler(BufferedImage img, int paletteMaxNum)
    {
        this.img = img;

        int tx = img.getWidth() / 4;
        int ty = img.getHeight() / 4;
        palettes = new int[tx * ty][];
        paletteCounts = new int[tx * ty];
        paletteNumbers = new int[tx][ty];
        paletteDiffs = new float[tx * ty][tx * ty];

        int palNum = 0;
        for (int x = 0; x < tx; x++)
            for (int y = 0; y < ty; y++)
            {
                ImageIndexer ii = new ImageIndexer(img, x * 4, y * 4);
                palettes[palNum] = ii.palettes[0];

                if (palettes[palNum][0] == 0)
                {
                    //TODO SHIFT PALETTE
                }

                paletteNumbers[x][y] = palNum;
                paletteCounts[palNum] = 1;
                int similar = calcPaletteDiffs(palNum);
                /*                    if (similar != -1)
                 {
                 paletteCounts[palNum] = 0;
                 paletteCounts[similar]++;
                 paletteNumbers[x][y] = similar;
                 }
                 */
                palNum++;
            }

        while (countUsedPalettes() > paletteMaxNum)
        {
            int besta = -1;
            int bestb = -1;
            float bestDif = Float.POSITIVE_INFINITY;

            //Find the two most similar palettes
            for (int i = 0; i < palettes.length; i++)
            {
                if (paletteCounts[i] == 0)
                    continue;
                for (int j = 0; j < palettes.length; j++)
                {
                    if (i == j)
                        continue;
                    if (paletteCounts[j] == 0)
                        continue;

                    if (paletteDiffs[i][j] < bestDif)
                    {
                        bestDif = paletteDiffs[i][j];
                        besta = j;
                        bestb = i;
                    }
                }
            }

            //Merge the Palettes!!!
            palettes[besta] = palMerge(palettes[besta], palettes[bestb]);
            calcPaletteDiffs(besta);
            paletteCounts[besta] += paletteCounts[bestb];
            paletteCounts[bestb] = 0;

            for (int x = 0; x < tx; x++)
                for (int y = 0; y < ty; y++)
                    if (paletteNumbers[x][y] == bestb)
                        paletteNumbers[x][y] = besta;
        }



        //CREATE THE FINAL PAL
        int currNum = 0;
        finalPalette = new int[paletteMaxNum * 4];
        int[] newPalNums = new int[palettes.length];
        for (int i = 0; i < palettes.length; i++)
            if (paletteCounts[i] != 0)
            {
                //transparentToTheEnd(palettes[i]);
                newPalNums[i] = currNum;
                System.arraycopy(palettes[i], 0, finalPalette, currNum * 4, 4);
                currNum++;
            }

        ArrayWriter texDat = new ArrayWriter();
        ArrayWriter f5Dat = new ArrayWriter();
        for (int y = 0; y < ty; y++)
            for (int x = 0; x < tx; x++)
            {
                //Find out if texel has transparent.

                boolean hasTransparent = false;
                for (int yy = 0; yy < 4; yy++)
                    for (int xx = 0; xx < 4; xx++)
                    {
                        int coll = img.getRGB(x * 4 + xx, y * 4 + yy);
                        if ((coll & 0x80000000) == 0)
                            hasTransparent = true;
                    }

                //WRITE THE IMAGE DATA
                for (int yy = 0; yy < 4; yy++)
                {
                    byte b = 0;
                    byte pow = 1;
                    for (int xx = 0; xx < 4; xx++)
                    {
                        int coll = img.getRGB(x * 4 + xx, y * 4 + yy);
                        byte col;
                        if ((coll & 0x80000000) == 0)
                            col = 3;
                        else
                        {
                            col = (byte) Util.closestColor(coll, palettes[paletteNumbers[x][y]]);
                            if (col == 3)
                                col = 2;
                        }
                        b |= (byte) (pow * col);
                        pow *= 4;
                    }
                    texDat.writeByte(b);
                }


                //WRITE THE FORMAT-5 SPECIFIC DATA
                short dat = (short) (newPalNums[paletteNumbers[x][y]] * 2);
                if (!hasTransparent)
                    dat |= 2 << 14;
                f5Dat.writeShort(dat);
            }

        f5data = f5Dat.getArray();
        texdata = texDat.getArray();

    }

    /*
     private void transparentToTheEnd(Color[] pal)
     {
     boolean transpFound = false;
     for (int i = 0; i < pal.length; i++)
     {
     if (pal[i] == Color.Transparent)
     {
     pal[i] = pal[pal.length - 1];
     transpFound = true;
     }
     }

     if (transpFound)
     pal[pal.length - 1] = Color.Transparent;
     }
     */
    public int calcPaletteDiffs(int pal)
    {
        int mostSimilar = -1;
        float bestDiff = Float.POSITIVE_INFINITY;
        for (int i = 0; i < palettes.length; i++)
        {
            if (paletteCounts[i] != 0)
                paletteDiffs[pal][i] = paletteDiffs[i][pal] = palDif(palettes[pal], palettes[i]);
            if (paletteDiffs[pal][i] < bestDiff)
            {
                bestDiff = paletteDiffs[pal][i];
                mostSimilar = i;
            }
        }
        return -1;
    }

    public int countUsedPalettes()
    {
        int res = 0;
        for (int i = 0; i < paletteCounts.length; i++)
            if (paletteCounts[i] != 0)
                res++;

        return res;
    }

    public float palDif(int[] a, int[] b)
    {
        return palDifUni(a, b) + palDifUni(b, a);
    }

    public float palDifUni(int[] a, int[] b)
    {
        boolean aTransp = a[3] == 0;
        boolean bTransp = b[3] == 0;

        if (aTransp != bTransp)
            return Float.POSITIVE_INFINITY;

        float dif = 0;
        int len = aTransp ? 3 : 4;

        boolean[] sel = new boolean[len];

        for (int i = 0; i < len; i++)
        {
            int c = a[i];
            float diff = Float.POSITIVE_INFINITY;
            int i2 = -1;
            for (int j = 0; j < len; j++)
            {
                if (sel[j])
                    continue;
                float diff2 = Util.colorDiff(c, b[j]);
                if (diff2 < diff || i2 == -1)
                {
                    i2 = j;
                    diff = diff2;
                }
            }
            sel[i2] = true;
            dif += diff;
        }

        return dif;
    }

    public int[] palMerge(int[] a, int[] b)
    {
        return a; //FIXME!!!!

        /*
         //Very ugly hack here. I put the 8 colors in a bitmap
         //and let ImageIndexer find me a good 4-color palette :P

         BufferedImage bi = new BufferedImage(8, 1);
         for (int i = 0; i < 4; i++)
         {
         bi.SetPixel(i, 0, a[i]);
         bi.SetPixel(i+4, 0, b[i]);
         }

         ImageIndexer ii = new ImageIndexer(bi);
         return ii.palette;*/

        //Haha, it was too slow :)
    }
}
