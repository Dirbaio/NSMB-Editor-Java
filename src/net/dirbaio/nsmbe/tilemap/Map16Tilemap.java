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

import net.dirbaio.nsmbe.fs.File;
import net.dirbaio.nsmbe.graphics.Image2D;
import net.dirbaio.nsmbe.graphics.Palette;
import net.dirbaio.nsmbe.util.ArrayReader;
import net.dirbaio.nsmbe.util.ArrayWriter;

public class Map16Tilemap extends Tilemap
{

    public Map16Tilemap(File f, int tileWidth, Image2D i, Palette[] pals, int a, int b)
    {
        super(f, tileWidth, i, pals, a, b);
    }

    @Override
    protected void load()
    {
        int fsize = f.getFileSize();

        height = (fsize / 2 + width - 1) / width;
        tiles = new Tile[width][height];

        ArrayReader b = new ArrayReader(f.getContents());

        for (int i = 0; i < fsize / 8; i++)
        {
            int x = (i % (width / 2)) * 2;
            int y = (i / (width / 2)) * 2;
            tiles[x][y] = shortToTile(b.readShort());
            tiles[x + 1][y] = shortToTile(b.readShort());
            tiles[x][y + 1] = shortToTile(b.readShort());
            tiles[x + 1][y + 1] = shortToTile(b.readShort());
        }
    }

    @Override
    public void save()
    {
        ArrayWriter os = new ArrayWriter();

        for (int i = 0; i < width * height / 4; i++)
        {
            int x = (i % (width / 2)) * 2;
            int y = (i / (width / 2)) * 2;
            os.writeShort(tileToShort(tiles[x][y]));
            os.writeShort(tileToShort(tiles[x + 1][y]));
            os.writeShort(tileToShort(tiles[x][y + 1]));
            os.writeShort(tileToShort(tiles[x + 1][y + 1]));
        }

        f.replace(os.getArray(), this);
    }

    public int getMap16TileCount()
    {
        return (width * height) / 4;
    }
}
