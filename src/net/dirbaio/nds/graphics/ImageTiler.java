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

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import net.dirbaio.nds.util.Util;

//TODO: Rewrite to handle ANY TILEMAP.
class ImageTiler
{

    private static final int tileCount = 64 * 64;
    Tile[] tiles;
    public int[][] tileMap;
    float[][] tileDiffs;
    public BufferedImage tileBuffer;
    private ArrayList<TileDiff> diffs;

    public ImageTiler(BufferedImage b)
    {
        if (b.getWidth() != 512 || b.getHeight() != 512)
            throw new RuntimeException("Wrong image size");

//        ProgressWindow p = new ProgressWindow(LanguageManager.Get("BgImport", "Importing"));
//        p.Show();

//        p.SetMax(tileCount);
        tileMap = new int[64][64];
        tileDiffs = new float[tileCount][tileCount];
        tiles = new Tile[tileCount];
        diffs = new ArrayList<>();

        //LOAD TILES
//        p.WriteLine("1/5: Loading tiles...");
        int tileNum = 0;
        for (int xt = 0; xt < 64; xt++)
            for (int yt = 0; yt < 64; yt++)
            {
                //                    Console.Out.WriteLine("Tile " + xt + " " + yt + ", " + tileNum);
                tiles[tileNum] = new Tile(b, xt * 8, yt * 8);
                tileMap[xt][yt] = tileNum;
                tileNum++;
//                p.setValue(xt * 64 + yt);
            } //                Console.Out.WriteLine(xt);

//        p.setValue(0);
//        p.SetMax(64 * 64);
//        p.WriteLine("2/5: Computing tile differences...");
        for (int xt = 0; xt < 64 * 64; xt++)
        {
//            p.setValue(xt);
            if (tiles[xt] == null)
                continue;

            for (int yt = 0; yt < xt; yt++)
            {
                if (tiles[yt] == null)
                    continue;
                float diff = tiles[xt].difference(tiles[yt]);
                if (diff < 0.5)
                    mergeTiles(xt, yt);
                else
                {
                    TileDiff td = new TileDiff();
                    td.diff = diff;
                    td.t1 = xt;
                    td.t2 = yt;
                    diffs.add(td);
                }
            }
        }

//            p.WriteLine("Tiles merged in first pass: " + (64 * 64 - countUsedTiles()) + " of " + 64 * 64);
//        p.WriteLine("3/5: Sorting tiles...");
        Collections.sort(diffs);

//        p.WriteLine("4/5: Merging tiles...");
        //REDUCE TILE COUNT
        int used = countUsedTiles();
        int mustRemove = used - 320;
        if (used > 320)
        {
//            p.setValue(0);
//            p.SetMax(mustRemove);
        }

        Iterator<TileDiff> en = diffs.iterator();

        while (used > 320)
        {
            TileDiff td = en.next();
            int t1 = td.t1;
            int t2 = td.t2;
            if (tiles[t1] == null)
                continue;
            if (tiles[t2] == null)
                continue;
            if (t1 == t2)
                throw new RuntimeException("This Should Never Happen");

            mergeTiles(t1, t2);

            used = countUsedTiles();
//            p.setValue(mustRemove - used + 320);
        }

//        p.WriteLine("5/5: Buiding tile map...");
        /*
         //DEBUG, DEBUG...
            
         for (int yt = 0; yt < 64; yt++)
         {
         for (int xt = 0; xt < 64; xt++)
         Console.Out.Write(tileMap[xt][yt].ToString("X2") + " ");
         Console.Out.WriteLine();
         }

         BufferedImage bb = new BufferedImage(512, 512, PixelFormat.Format32bppArgb);
         for (int xt = 0; xt < 64; xt++)
         for (int yt = 0; yt < 64; yt++)
         {
         for (int x = 0; x < 8; x++)
         for (int y = 0; y < 8; y++)
         {
         Color c = tiles[tileMap[xt][yt]].data[x][y];
         //                            Console.Out.WriteLine(c);
         bb.SetPixel(xt * 8 + x, yt * 8 + y, c);
         }
         }

         bb.Save("C:\\image2.png");
         new ImagePreviewer(bb).Show();*/

        //COMPACTIFY TILES AND MAKE THE TILE BUFFER!!!
        tileBuffer = new BufferedImage(countUsedTiles() * 8, 8, BufferedImage.TYPE_INT_ARGB);
        int[] newTileNums = new int[tileCount];
        int nt = 0;
        for (int t = 0; t < tileCount; t++)
            if (tiles[t] != null)
            {
                newTileNums[t] = nt;
                for (int x = 0; x < 8; x++)
                    for (int y = 0; y < 8; y++)
                        tileBuffer.setRGB(x + nt * 8, y, tiles[t].data[x][y]);
                nt++;
            }

//            new ImagePreviewer(tileBuffer).Show();

        for (int xt = 0; xt < 64; xt++)
            for (int yt = 0; yt < 64; yt++)
                tileMap[xt][yt] = newTileNums[tileMap[xt][yt]];

//        p.WriteLine("Done! You can close this window now.");
    }

    private void mergeTiles(int t1, int t2)
    {
//            Console.Out.WriteLine("Used: " + countUsedTiles() + ", replacing " + t2 + " with " + t1);
        tiles[t1].merge(tiles[t2]);
        //                fillDiffs(best1);
        //fusionate them
        for (int xt = 0; xt < 64; xt++)
            for (int yt = 0; yt < 64; yt++)
                if (tileMap[xt][yt] == t2)
                    tileMap[xt][yt] = t1;

        tiles[t2] = null;
    }

    private int countUsedTiles()
    {
        int c = 0;
        for (int i = 0; i < tileCount; i++)
            if (tiles[i] != null)
                c++;

        return c;
    }

    /*
     * Fills the difference table of a tile (row and column)
     * if it finds a very similar tile, returns its number and stops
     * if not, returns -1
     */
    private int fillDiffs(int tile)
    {
        for (int t = 0; t < tileCount; t++)
        {
            if (t == tile)
                continue;
            if (tiles[t] == null)
                continue;
            float diff = tiles[tile].difference(tiles[t]);
            if (diff == 0)
                return t;
            tileDiffs[tile][t] = diff;
            tileDiffs[t][tile] = diff;
            TileDiff td = new TileDiff();
            td.diff = diff;
            td.t1 = t;
            td.t2 = tile;
            diffs.add(td);
//                Console.Out.WriteLine(t+" "+tile+" "+diff);
        }
        return -1;
    }

    private static float colorMatrixDiff(int[][] a, int[][] b)
    {
        float res = 0;
        for (int x = 0; x < a.length; x++)
            for (int y = 0; y < a[x].length; y++)
                res += Util.colorDiff(a[x][y], b[x][y]);

        return res / a.length;
    }

    private static float colorMatrixBorderDiff(int[][] a, int[][] b)
    {
        int l = a.length - 1;
        float res = 0;
        for (int x = 0; x < a.length; x++)
        {
            res += Util.colorDiff(a[x][0], b[x][0]);
            res += Util.colorDiff(a[x][l], b[x][l]);
            res += Util.colorDiff(a[0][x], b[0][x]);
            res += Util.colorDiff(a[l][x], b[l][x]);
        }

        return res / l * 2;
    }

    private static int[][] colorMatrixReduce(int[][] m)
    {
        int[][] r = new int[m.length / 2][m[0].length / 2];

        for (int x = 0; x < m.length / 2; x++)
            for (int y = 0; y < m[x].length / 2; y++)
                r[x][y] = Util.colorMean(
                        Util.colorMean(m[x * 2][y * 2], m[x * 2][y * 2 + 1], 1, 1),
                        Util.colorMean(m[x * 2 + 1][y * 2], m[x * 2 + 1][y * 2 + 1], 1, 1),
                        1, 1);
        return r;
    }

    private class TileDiff implements Comparable<TileDiff>
    {

        public float diff;
        public int t1, t2;

        @Override
        public int compareTo(TileDiff o)
        {
            return Float.compare(diff, o.diff);
        }
    }

    private class Tile
    {

        public int[][] data, d1, d2;
        public int count = 1;

        public Tile(BufferedImage b, int xp, int yp)
        {
            data = new int[8][8];
            for (int x = 0; x < 8; x++)
                for (int y = 0; y < 8; y++)
                {
                    int c = b.getRGB(x + xp, y + yp);
                    if ((c & 0x80000000) == 0)
                        data[x][y] = 0;
                    else
                        data[x][y] = c | 0xFF000000;
                }
            makeReductions();
        }

        private void makeReductions()
        {
            d1 = colorMatrixReduce(data);
            d2 = colorMatrixReduce(d1);
        }

        public float difference(Tile b)
        {
            float res = 0;
            res += colorMatrixBorderDiff(data, b.data) * 5;
            res += colorMatrixDiff(d2, b.d2);
            res += colorMatrixDiff(d1, b.d1);
            res *= count + b.count;
            return res;
        }

        public void merge(Tile b)
        {
            for (int x = 0; x < 8; x++)
                for (int y = 0; y < 8; y++)
                    data[x][y] = Util.colorMean(data[x][y], b.data[x][y], count, b.count);
            count += b.count;
//                makeReductions();
        }
    }
}
