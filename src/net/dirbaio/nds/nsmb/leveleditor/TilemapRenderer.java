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
package net.dirbaio.nds.nsmb.leveleditor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.dirbaio.nds.nsmb.level.NSMBGraphics;

public class TilemapRenderer
{

    private static final int WIDTH = 512;
    private static final int HEIGHT = 256;
    private static final int TILESIZE = 16;
    private NSMBGraphics gfx;
    private int[][] tilemap;
    private int[][] tilemapRendered;
    private int renderSize;
    public BufferedImage buffer;
    private Rectangle oldRect = new Rectangle(-1, -1, 0, 0);

    public TilemapRenderer(int[][] tilemap, NSMBGraphics gfx)
    {
        this.tilemap = tilemap;
        this.gfx = gfx;

        if (tilemap.length != WIDTH || tilemap[0].length != HEIGHT)
            throw new RuntimeException("Tilemap wrong size");

        tilemapRendered = new int[WIDTH][HEIGHT];

        invalidate();
        resize(64);
    }

    private void update(Rectangle rect)
    {
        //First figure out if we need to change the size
        int newRenderSize = rect.width | rect.height;
        newRenderSize |= newRenderSize >> 1;
        newRenderSize |= newRenderSize >> 2;
        newRenderSize |= newRenderSize >> 4;
        newRenderSize |= newRenderSize >> 8;
        newRenderSize |= newRenderSize >> 16;

        if (newRenderSize != renderSize)
            resize(newRenderSize);

        Graphics2D g = buffer.createGraphics();
        g.setBackground(new Color(0, 0, 0, 0));

        for (int xx = rect.x; xx < rect.x + rect.width; xx++)
            for (int yy = rect.y; yy < rect.y + rect.height; yy++)
            {
                if (xx < 0 || xx >= WIDTH)
                    continue;
                if (yy < 0 || yy >= HEIGHT)
                    continue;

                int t = tilemap[xx][yy];
                boolean eq = (t == tilemapRendered[xx][yy]);

                if (oldRect.contains(xx, yy) && eq)
                    continue;
                tilemapRendered[xx][yy] = t;

                g.setComposite(AlphaComposite.Src);

                int dx = (xx % renderSize) * TILESIZE;
                int dy = (yy % renderSize) * TILESIZE;

                if (t == -1)
                {
                    g.clearRect(dx, dy, TILESIZE, TILESIZE);
                    continue;
                }

                int tileset = 0;
                if (t >= 256 * 4)
                {
                    t -= 256 * 4;
                    tileset = 2;
                } else if (t >= 256)
                {
                    t -= 256;
                    tileset = 1;
                }

                int sx = (t % 16) * 16;
                int sy = (t / 16) * 16;

                BufferedImage img = gfx.Tilesets[tileset].Map16Buffer;
                g.drawImage(img, dx, dy, dx + TILESIZE, dy + TILESIZE, sx, sy, sx + TILESIZE, sy + TILESIZE, null);

                if (!gfx.Tilesets[tileset].UseOverrides)
                    continue;
                int t2 = gfx.Tilesets[tileset].Overrides[t];
                if (t2 == -1)
                    continue;
                if (t2 == 0)
                    continue;

                sx = t2 * TILESIZE;
                sy = 0;

                g.setComposite(AlphaComposite.SrcOver);
                img = gfx.Tilesets[tileset].Map16Buffer;
                g.drawImage(img, dx, dy, dx + TILESIZE, dy + TILESIZE, sx, sy, sx + TILESIZE, sy + TILESIZE, null);
            }

        oldRect = rect;
    }

    private void resize(int newRenderSize)
    {
        renderSize = newRenderSize;
        buffer = new BufferedImage(renderSize * TILESIZE, renderSize * TILESIZE, BufferedImage.TYPE_INT_ARGB);
        invalidate();
    }

    public void invalidate()
    {
        oldRect = new Rectangle(-1, -1, 0, 0);
    }

    public void render(Graphics g, Rectangle r)
    {
        update(r);

        int x = oldRect.x;
        x -= x % renderSize;
        x *= 16;
        int y = oldRect.y;
        y -= y % renderSize;
        y *= 16;
        int d = renderSize * 16;

        g.drawImage(buffer, x, y, null);
        g.drawImage(buffer, x, y + d, null);
        g.drawImage(buffer, x + d, y, null);
        g.drawImage(buffer, x + d, y + d, null);
    }
}
