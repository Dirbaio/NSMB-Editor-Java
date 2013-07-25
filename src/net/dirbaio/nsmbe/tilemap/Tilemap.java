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
package net.dirbaio.nsmbe.tilemap;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.dirbaio.nsmbe.fs.AlreadyEditingException;
import net.dirbaio.nsmbe.fs.File;
import net.dirbaio.nsmbe.graphics.Image2D;
import net.dirbaio.nsmbe.graphics.Palette;
import net.dirbaio.nsmbe.util.ArrayReader;
import net.dirbaio.nsmbe.util.ArrayWriter;

public class Tilemap
{

    public Tile[][] tiles;
    public int width, height;
    protected File f;
    public Palette[] palettes;
    public Image2D tileset;
    public int tileCount;
    public BufferedImage[] buffers;
    public int tileOffset;
    public int paletteOffset;

    public Tilemap(File f, int tileWidth, Image2D i, Palette[] pals, int tileOffset, int paletteOffset)
    {
        this.f = f;
        this.width = tileWidth;
        this.tileOffset = tileOffset;
        this.paletteOffset = paletteOffset;

        this.tileset = i;
        this.palettes = pals;
        this.tileCount = tileset.getWidth() * tileset.getHeight() / 64;

        load();
    }

    public void beginEdit() throws AlreadyEditingException
    {
        f.beginEdit(this);
    }

    public void endEdit()
    {
        f.endEdit(this);
    }

    protected void load()
    {
        int fsize = f.getFileSize();
        height = (fsize / 2 + width - 1) / width;
        tiles = new Tile[width][height];

        ArrayReader in = new ArrayReader(f.getContents());

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                tiles[x][y] = shortToTile(in.readShort());
    }

    public void save()
    {
        ArrayWriter out = new ArrayWriter();

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                out.writeShort(tileToShort(tiles[x][y]));

        f.replace(out.getArray(), this);
    }

    public Tile getTileAtPos(int x, int y)
    {
        return tiles[x][y];
    }

    public void updateBuffers()
    {
        buffers = new BufferedImage[palettes.length];

        for (int i = 0; i < palettes.length; i++)
            buffers[i] = tileset.render(palettes[i]);
    }
    public BufferedImage buffer;
    Graphics2D bufferGx;

    public BufferedImage render()
    {
        if (buffers == null)
            updateBuffers();

        if (buffer == null)
        {
            buffer = new BufferedImage(width * 8, height * 8, BufferedImage.TYPE_INT_ARGB);
            bufferGx = buffer.createGraphics();
            bufferGx.setComposite(AlphaComposite.Src);
            bufferGx.setBackground(new Color(0, 0, 0, 0));
        }

        reRenderAll();

        return buffer;
    }

    public BufferedImage reRenderAll()
    {
        updateBuffers();
        return reRender(0, 0, width, height);
    }

    public BufferedImage reRender(int xMin, int yMin, int width, int height)
    {
        for (int x = xMin; x < xMin + width; x++)
            for (int y = yMin; y < yMin + height; y++)
            {
                if (x >= this.width)
                    continue;
                if (y >= this.height)
                    continue;
                Tile t = tiles[x][y];

                if (t.tileNum < 0 || t.tileNum >= tileCount)
                {
                    bufferGx.clearRect(x * 8, y * 8, 8, 8);
                    continue;
                }
                if (t.palNum >= palettes.length)
                    continue;
                if (t.palNum < 0)
                    continue;

                Rectangle rect = Image2D.getTileRectangle(buffers[t.palNum], 8, t.tileNum);
                int x1 = rect.x;
                int x2 = rect.x + rect.width;
                int y1 = rect.y;
                int y2 = rect.y + rect.height;
                if (t.hflip)
                {
                    int aux = x1;
                    x1 = x2;
                    x2 = aux;
                }
                if (t.vflip)
                {
                    int aux = y1;
                    y1 = y2;
                    y2 = aux;
                }

                bufferGx.drawImage(buffers[t.palNum], x * 8, y * 8, x * 8 + 8, y * 8 + 8, x1, y1, x2, y2, null);
            }
        return buffer;
    }

    public short tileToShort(Tile t)
    {
        short res = 0;

        if (t.tileNum != -1)
            res |= (short) ((t.tileNum + tileOffset) & 0x3FF);
        res |= (short) ((t.hflip ? 1 : 0) << 10);
        res |= (short) ((t.vflip ? 1 : 0) << 11);
        res |= (short) (((t.palNum + paletteOffset) & 0x00F) << 12);

        return res;
    }

    public Tile shortToTile(int u)
    {
        Tile res = new Tile();

        res.tileNum = (u & 0x3FF) - tileOffset;
        if (res.tileNum < 0)
            res.tileNum = -1;

        res.hflip = ((u >> 10) & 1) == 1;
        res.vflip = ((u >> 11) & 1) == 1;
        res.palNum = ((u >> 12) & 0xF) - paletteOffset;

        return res;
    }

    public class Tile
    {

        public int tileNum;
        public int palNum;
        public boolean hflip;
        public boolean vflip;
    }
}
