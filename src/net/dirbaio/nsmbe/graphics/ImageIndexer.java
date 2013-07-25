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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * This is the core of all the image importing. Takes one or more RGB bitmaps
 * and outputs: - The image data common to all images. (tiled or non-tiled) -
 * One palette for each image, so that viewing the image data with it shows the
 * original image.
 *
 * It could still be optimized more, I know. ~Dirbaio
 */
public class ImageIndexer
{

    private ArrayList<Box> boxes;
    private HashMap<MultiColor, Integer> freqTable;
    private MultiColor[] multiPalette;
    private int width, height;
    private int colorCount, palLen;
    private boolean useAlpha;
    private ArrayList<BufferedImage> images;
    private int imageCount;
    public int[][] palettes;
    public int[][] imageData;

    //Special version for ImageTexeler
    public ImageIndexer(BufferedImage image, int xx, int yy)
    {
        colorCount = 3;
        palLen = 4;
        useAlpha = true; //TODO: wtf
        width = 4;
        height = 4;
        imageCount = 1;

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
            {
                MultiColor c = new MultiColor(colorCount);
                int col = image.getRGB(x + xx, y + yy);

                if ((col & 0x80000000) == 0)
                {
                    c.data[0] = -1;
                    c.data[1] = -1;
                    c.data[2] = -1;
                } else
                {
                    c.data[0] = reduce((col >> 16) & 0xFF);
                    c.data[1] = reduce((col >> 8) & 0xFF);
                    c.data[2] = reduce((col) & 0xFF);
                }

                if (c.allTransparent())
                    continue;

                c.calcHash();

                if (freqTable.containsKey(c))
                    freqTable.put(c, freqTable.get(c) + 1);
                else
                    freqTable.put(c, 1);
            }

        computePalette();
    }

    public ImageIndexer(ArrayList<BufferedImage> images, int palLen, boolean useAlpha)
    {
        colorCount = images.size() * 3;
        this.palLen = palLen;
        this.useAlpha = useAlpha;
        this.images = images;
        imageCount = images.size();

        //COMPUTE FREQUENCY TABLE

        freqTable = new HashMap<>();

        //Quick check just in case...
        width = images.get(0).getWidth();
        height = images.get(0).getHeight();
        for (BufferedImage b : images)
            if (b.getWidth() != width || b.getHeight() != height)
                throw new RuntimeException("Not all images have the same size!!");


        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
            {
                MultiColor c = new MultiColor(colorCount);
                for (int i = 0; i < imageCount; i++)
                {
                    int col = images.get(i).getRGB(x, y);
                    //TODO: Alpha
                    if ((col & 0x80000000) == 0)
                    {
                        c.data[3 * i + 0] = -1;
                        c.data[3 * i + 1] = -1;
                        c.data[3 * i + 2] = -1;
                    } else
                    {
                        c.data[3 * i + 0] = reduce((col >> 16) & 0xFF);
                        c.data[3 * i + 1] = reduce((col >> 8) & 0xFF);
                        c.data[3 * i + 2] = reduce((col) & 0xFF);
                    }
                }

                if (c.allTransparent())
                    continue;

                c.calcHash();

                if (freqTable.containsKey(c))
                    freqTable.put(c, freqTable.get(c) + 1);
                else
                    freqTable.put(c, 1);
            }

        computePalette();
        computeIndexings();
    }

    private void computePalette()
    {
        //Create the boxes
        Box startBox = new Box(colorCount);
        startBox.shrink();
        boxes = new ArrayList<>();
        boxes.add(startBox);

        while (boxes.size() < (useAlpha ? palLen - 1 : palLen))
        {
            Box bo = getDominantBox();
            if (bo == null)
                break;

            split(bo);
        }

        //Create the final multipalette
        multiPalette = new MultiColor[palLen];
        for (int j = useAlpha ? 1 : 0; j < palLen; j++)
            if ((useAlpha ? j : j + 1) <= boxes.size())
                multiPalette[j] = boxes.get(useAlpha ? j - 1 : j).center();

        //Create the individual palettes.
        palettes = new int[imageCount][palLen];
        for (int i = 0; i < imageCount; i++)
        {
            for (int j = useAlpha ? 1 : 0; j < palLen; j++)
                if ((useAlpha ? j : j + 1) > boxes.size())
                    palettes[i][j] = 0x00FF00FF;
                else
                    palettes[i][j] = boxes.get(useAlpha ? j - 1 : j).center().getColor(i);

            if (useAlpha)
                palettes[i][0] = 0;

        }
    }

    private void computeIndexings()
    {
        //Index the images.
        imageData = new int[width][height];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
            {
                MultiColor c = new MultiColor(colorCount);
                for (int i = 0; i < imageCount; i++)
                {
                    int col = images.get(i).getRGB(x, y);
                    //TODO: Alpha
                    if ((col & 0x80000000) == 0)
                    {
                        c.data[3 * i + 0] = -1;
                        c.data[3 * i + 1] = -1;
                        c.data[3 * i + 2] = -1;
                    } else
                    {
                        c.data[3 * i + 0] = reduce((col) & 0xFF);
                        c.data[3 * i + 1] = reduce((col >> 8) & 0xFF);
                        c.data[3 * i + 2] = reduce((col >> 16) & 0xFF);
                    }
                }

                c.calcHash();
                if (c.allTransparent())
                    imageData[x][y] = 0;
                else
                    imageData[x][y] = closestMultiColor(c);
            }
    }
    //PUBLIC DATA-RETRIEVING FUNCTIONS

    public byte[] getTiledImageData()
    {

        byte[] palettedImage = new byte[width * height];
        int tileCount = width * height / 64;
        int tileWidth = width / 8;

        for (int t = 0; t < tileCount; t++)
            for (int y = 0; y < 8; y++)
                for (int x = 0; x < 8; x++)
                {
                    int tx = (t % tileWidth) * 8;
                    int ty = (int) (t / tileWidth) * 8;
                    palettedImage[t * 64 + y * 8 + x] =
                            (byte) imageData[tx + x][ty + y];
                }
        return palettedImage;
    }

    public byte[] getTiledImageDataPart(int px, int py, int ptx, int pty)
    {

        byte[] palettedImage = new byte[ptx * pty];
        int tileCount = ptx * pty / 64;
        int tileWidth = ptx / 8;

        for (int t = 0; t < tileCount; t++)
            for (int y = 0; y < 8; y++)
                for (int x = 0; x < 8; x++)
                {
                    int tx = (t % tileWidth) * 8;
                    int ty = (int) (t / tileWidth) * 8;
                    palettedImage[t * 64 + y * 8 + x] =
                            (byte) imageData[tx + x + px][ty + y + py];
                }
        return palettedImage;
    }

    //ALGORITHM CORE
    private int closestMultiColor(MultiColor mc)
    {
        if (mc.allTransparent())
            return 0;
        else
        {
            int best = -1;
            float bestd = Float.POSITIVE_INFINITY;
            for (int i = 0; i < multiPalette.length; i++)
            {
                if (multiPalette[i] == null)
                    continue;
                float d = mc.diff(multiPalette[i]);
                if (d < bestd || best == -1)
                {
                    best = i;
                    bestd = d;
                }
            }
            return (byte) best;
        }
    }

    private void split(Box b)
    {
        byte dim = b.dominantDimensionNum();
        ArrayList<ValFreqPair> values = new ArrayList<>();

        int total = 0;
        for (Entry<MultiColor, Integer> e : freqTable.entrySet())
        {
            MultiColor c = e.getKey();
            if (b.inside(c))
                if (c.data[dim] != -1)
                {
                    values.add(new ValFreqPair(c.data[dim], e.getValue()));
                    total += e.getValue();
                }
        }

        Collections.sort(values);

        if (values.isEmpty())
            throw new RuntimeException("WTF?!");

        int m = median(values, total);
        if (m == values.get(0).b)
            m++;

        Box nb = new Box(b);
        nb.setDimMax(dim, (byte) (m - 1));
        b.setDimMin(dim, m);

        boxes.add(nb);

        b.shrink();
        nb.shrink();
    }

    private int median(ArrayList<ValFreqPair> values, int total)
    {
        //Naive median algorithm

        int acum = 0;
        for (ValFreqPair val : values)
        {
            acum += val.i;
            if (acum * 2 > total)
                return val.b;
        }

        throw new RuntimeException("Bad, bad, bad!");
    }

    private Box getDominantBox()
    {
        Box best = null;
        int bestDim = 0;

        for (Box b : boxes)
        {
            int dim = b.dominantDimension();
            if ((dim > bestDim || best == null) && b.canSplit())
            {
                bestDim = dim;
                best = b;
            }
        }
        return best;
    }

    private static int reduce(int c)
    {
        return ((c >> 3) << 3);
    }

    //HELPER CLASSES
    private class MultiColor
    {

        public int[] data;
        public boolean deleteFlag;

        public MultiColor(int count)
        {
            data = new int[count];
            deleteFlag = false;
        }

        public void merge(MultiColor b)
        {
            for (int i = 0; i < data.length; i++)
                if (data[i] == -1)
                    data[i] = b.data[i];

            calcHash();
        }

        public boolean allTransparent()
        {
            for (int i = 0; i < data.length; i += 3)
                if (data[i] != -1)
                    return false;
            return true;
        }
        private int thehash;

        public void calcHash()
        {
            int p = 16777619;
            int hash = 1166136261;

            for (int i = 0; i < data.length; i++)
                hash = (hash ^ data[i]) * p; //                if (transp[i])
            //                    hash++;

            hash += hash << 13;
            hash ^= hash >> 7;
            hash += hash << 3;
            hash ^= hash >> 17;
            hash += hash << 5;

            thehash = hash;
        }

        @Override
        public int hashCode()
        {
            return thehash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof MultiColor))
                return false;
            MultiColor b = (MultiColor) obj;
            return Arrays.equals(data, b.data);
        }

        public float diff(MultiColor b)
        {
            float res = 0;
            for (int i = 0; i < data.length; i++)
            {
                if (data[i] == -1 || b.data[i] == -1)
                    continue;

                float d = data[i] - b.data[i];
                res += d * d;
            }
            return res;
        }

        private int getColor(int i)
        {
            if (data[i * 3] == -1)
                return 0;

            int r = 0;
            r |= data[i * 3 + 0] << 16;
            r |= data[i * 3 + 1] << 8;
            r |= data[i * 3 + 2];
            r |= 0xFF << 24;

            return r;
        }
    }

    private class Box
    {

        public int[] min, max;
        private boolean splittable = false;
        private boolean splittablecached = false;

        public Box(int[] min, int[] max)
        {
            this.min = min;
            this.max = max;
        }

        public Box(int count)
        {
            min = new int[count];
            max = new int[count];
            for (int i = 0; i < count; i++)
            {
                min[i] = 0;
                max[i] = 255;
            }
        }

        public Box(Box b)
        {
            this.min = Arrays.copyOf(b.min, b.min.length);
            this.max = Arrays.copyOf(b.max, b.max.length);
        }

        public boolean inside(MultiColor c)
        {
            for (int i = 0; i < min.length; i++)
            {
                if (c.data[i] == -1)
                    continue;
                if (c.data[i] < min[i])
                    return false;
                if (c.data[i] > max[i])
                    return false;
            }
            return true;
        }

        public int dominantDimension()
        {
            int d = dominantDimensionNum();
            if (d == 255)
                return 0;
            return max[d] - min[d];
        }

        public byte dominantDimensionNum()
        {
            int d = -1;
            int dl = -1;
            for (int i = 0; i < min.length; i++)
            {
                int il = max[i] - min[i];
                if (il > dl && canSplitInDim(i))
                {
                    dl = il;
                    d = i;
                }
            }
            return (byte) d;
        }

        public void setDimMin(int d, int a)
        {
            min[d] = a;
            splittablecached = false;
        }

        public void setDimMax(int d, int a)
        {
            max[d] = a;
            splittablecached = false;
        }

        public boolean canSplitInDim(int i)
        {
            int data = 0;
            boolean seen = false;

            for (MultiColor c : freqTable.keySet())
                if (inside(c))
                    if (c.data[i] != -1)
                        if (!seen) //First val we see
                        {
                            seen = true;
                            data = c.data[i];
                        } else if (data != c.data[i])
                            return true;
            return false;
        }

        public boolean canSplit()
        {
            if (splittablecached)
                return splittable;
            else
            {
                splittablecached = true;
                splittable = canSplit2();
                return splittable;
            }
        }

        public boolean canSplit2()
        {
            //Whoa... This gets complicated if I have to
            //take into acount the "don't care" of transparent colors...
            int[] data = new int[min.length];
            boolean[] seen = new boolean[min.length];

            for (MultiColor c : freqTable.keySet())
                if (inside(c))
                    for (int i = 0; i < min.length; i++)
                        if (c.data[i] != -1)
                            if (!seen[i]) //First val we see
                            {
                                seen[i] = true;
                                data[i] = c.data[i];
                            } else if (data[i] != c.data[i])
                                return true;
            return false;
        }

        public MultiColor center()
        {
            MultiColor res = new MultiColor(min.length);
            for (int i = 0; i < min.length; i++)
                res.data[i] = (byte) ((min[i] + max[i]) / 2);
            return res;
        }

        public void shrink()
        {
            int[] nmin = new int[min.length];
            int[] nmax = new int[min.length];
            for (int i = 0; i < min.length; i++)
                nmin[i] = nmax[i] = -1;

            for (MultiColor c : freqTable.keySet())
                if (inside(c))
                    for (int i = 0; i < c.data.length; i++)
                        if (c.data[i] != -1)
                            if (nmin[i] != -1)
                            {
                                if (nmin[i] > c.data[i])
                                    nmin[i] = c.data[i];
                                if (nmax[i] < c.data[i])
                                    nmax[i] = c.data[i];
                            } else
                            {
                                nmin[i] = c.data[i];
                                nmax[i] = c.data[i];
                            }

            min = nmin;
            max = nmax;
        }

        @Override
        public String toString()
        {
            return arr2str(min) + " - " + arr2str(max);
//                return "("+r1+"-"+r2+","+g1+"-"+g2+","+b1+"-"+b2+")";
        }

        private String arr2str(int[] a)
        {
            String s = "(" + a[0];
            for (int i = 1; i < a.length; i++)
                s += ", " + a[i];
            return s + ")";
        }
    }

    private class ValFreqPair implements Comparable<ValFreqPair>
    {

        public int b;
        public int i;

        public ValFreqPair(int b, int i)
        {
            this.b = b;
            this.i = i;
        }

        @Override
        public String toString()
        {
            return "(" + b + ", " + i + ")";
        }

        @Override
        public int compareTo(ValFreqPair o)
        {
            return Integer.compare(b, o.b);
        }
    }

    //GENERAL PURPOSE FUNCTIONS
    public static int[] createPaletteForImage(BufferedImage b)
    {
        return createPaletteForImage(b, 256);
    }

    public static int[] createPaletteForImage(BufferedImage b, int palLen)
    {
        ArrayList<BufferedImage> bl = new ArrayList<>();
        bl.add(b);

        ImageIndexer i = new ImageIndexer(bl, palLen, true);

        return i.palettes[0];
    }
    //This doesn't belong here.
    /*
     public static byte[] indexImageWithPalette(BufferedImage b, Color[] palette)
     {
     //More efficient now.
     byte[] palettedImage = new byte[b.getWidth() * b.getHeight()];
     int tileCount = b.getWidth() * b.getHeight() / 64;
     int tileWidth = b.getWidth() / 8;

     for (int t = 0; t < tileCount; t++)
     for (int y = 0; y < 8; y++)
     for (int x = 0; x < 8; x++)
     {
     int tx = (t % tileWidth) * 8;
     int ty = (int) (t / tileWidth) * 8;
     Color c = b.getRGB(tx + x, ty + y);
     if (c.A != 0)
     {
     c = Color.FromArgb(c.R, c.G, c.B);

     palettedImage[t * 64 + y * 8 + x] = closest(c, palette);
     }
     else
     palettedImage[t * 64 + y * 8 + x] = 0;
     }

     return palettedImage;
     }
     */
}
