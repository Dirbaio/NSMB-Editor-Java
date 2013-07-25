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
package net.dirbaio.nds.nsmb.level;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import net.dirbaio.nds.fs.AlreadyEditingException;
import net.dirbaio.nds.fs.File;
import net.dirbaio.nds.fs.InlineFile;
import net.dirbaio.nds.fs.LZFile;
import net.dirbaio.nds.graphics.FilePalette;
import net.dirbaio.nds.graphics.Image2D;
import net.dirbaio.nds.graphics.Palette;
import net.dirbaio.nds.tilemap.Map16Tilemap;
import net.dirbaio.nds.util.ArrayReader;
import net.dirbaio.nds.util.ArrayWriter;
import net.dirbaio.nds.util.LZ;
import net.dirbaio.nds.util.Resources;

/**
 * A NSMB tileset.
 *
 * TILESET FORMAT DOCS:
 *
 * Graphics file: - LZ Compressed - 8bpp 8x8 tiles
 *
 * Palette file: - LZ Compressed - 512 entries - 2-byte entries in RGB15 format
 * - 0 and 256 transparent - 2 different palettes: 0-255 and 256-511
 *
 * Map16 file: - Groups 8x8 tiles into 16x16 tiles - 8-byte per 16x16 tile -
 * order: top-left, top-right, bottom-left, bottom-right - 2-byte per tile.
 *
 * Object index file: - Defines offsets in the Object file - 4 bytes per object
 * - Offset as short - Width and Height as 2 bytes. These are unused by the
 * game, and are inaccurate for slopes, so my implementation doesn't use them
 *
 * Object file: - Data for each object.
 *
 * 0xFF - End of object 0xFE - New Line 0x8x - Slope Control Byte Else, its the
 * beginning of a tile: Control Byte Map16 tile number as short
 *
 * STANDARD OBJECTS:
 *
 * Tile control byte: 1 = horizontal repeat 2 = vertical repeat
 *
 * If no repeats, repeat all. If there are repeats, divide everyting in 3:
 * Before repeat (no repeat set) In repeat (repeat set) After repeat (no repeat
 * set)
 *
 * Then put the before repeat at the beginning. The after repeat at the end And
 * then fill the space between them (if any) repeating the In repeat tiles.
 *
 * SLOPED OBJECTS:
 *
 * These objects are a pain to work with.
 *
 * The first slope control byte defines the direction of slope:
 *
 * & with 1 -> Go left & with 2 -> Upside-down slope
 *
 *
 * The slope format is:
 *
 * A slope control indicating it's a slope and its direction. Then follows a
 * rectangular block of tiles. These have to be placed corner-by-corner,
 * respecting their size, like this:
 *
 * _|_| _ | | __ _|_| _| _| __|__| _|_| | | __|__| |_| |_| |__|
 *
 * The first corner must be placed on a corner of the object, on the opposite
 * side of the direction (if slope goes right, start is at left), and at the
 * bottom, or at the top if its an upside-down slope.
 *
 * Optional: Then follows a 0x85 slope control, then another block of tiles that
 * has to be placed under the previous blocks, or OVER if its an upside-down
 * slope.
 *
 * If the slope goes right the blocks have to be left-aligned: _ |_|__ main
 * block |____| sub (0x85) block
 *
 * If the slope goes left the blocks have to be right-aligned: _ __|_| |____|
 *
 * EXAMPLE: Slope going up right with 1x1 main block and 2x1 sub block in a 6x5
 * obj _ _ _|_|_| _|_|___| _|_|___| |_|___|
 *
 * NOTE: This info is not complete. This works for all the slopes used in-game,
 * but there are some unused bits that change their behavior:
 *
 * -0x04 control byte in 0x85 slope block: All slopes that have the 0x85 block
 * have all its tiles with an 0x04 control byte.
 *
 * IF ITS NOT SET, then the 0x85 block is used to fill all the area below (or
 * over if its upside down???) the slope. Not sure how does it behave if the
 * 0x85 block has multiple tiles. Probably the Nintendo Guys thought it was best
 * to have it like that, and then realized that it caused the triangle below the
 * slope to be unusable (filled) and then created a new mode. NOTE: The editor
 * doesn't implement this.
 *
 * Not sure if there's more like this...
 *
 */
public class NSMBTileset
{

    public File GFXFile;
    public File PalFile;
    public File Map16File;
    public File ObjFile;
    public File ObjIndexFile;
    public File TileBehaviorFile;
    public int TilesetNumber; // 0 for Jyotyu, 1 for Normal, 2 for SubUnit
    //Palettes
    public Palette[] palettes;
    //Graphics
    public Image2D graphics;
    //Map16
    public Map16Tilemap map16;
    public BufferedImage Map16Buffer;
    private boolean overrideFlag;
    public boolean UseOverrides;
    public BufferedImage OverrideBufferedImage = Resources.get("tileoverrides");
    public short[] Overrides;
    public static int[] BehaviorOverrides =
    {
        0x00000500, 0x01000500, 0x02000500, 0x03000500, 0x04000500, 0x05000500, 0x06000500,
        0x07000500, 0x08000500, 0x09000500, 0x0A000500, 0x0B000500, 0x0C000500, 0x0D000500
    };
    //Tile behaviors 
    public int[] TileBehaviors;
    //Objects
    public boolean UseNotes;
    public String[] ObjNotes;
    public int objectCount = 256;
    public ObjectDef[] Objects;

    public NSMBTileset(File GFXFile, File PalFile, File Map16File, File ObjFile, File ObjIndexFile, File TileBehaviorFile, boolean OverrideFlag, int TilesetNumber)
    {
        this.GFXFile = GFXFile;
        this.PalFile = PalFile;
        this.Map16File = Map16File;
        this.ObjFile = ObjFile;
        this.ObjIndexFile = ObjIndexFile;
        this.TileBehaviorFile = TileBehaviorFile;
        /*
         System.out.println("=== TILESET");
         System.out.println("GFX   "+GFXFile);
         System.out.println("Pal   "+PalFile);
         System.out.println("Map16 "+Map16File);
         System.out.println("Obj   "+ObjFile);
         System.out.println("ObjIn "+ObjIndexFile);
         System.out.println("Behav "+TileBehaviorFile);
         */

        this.TilesetNumber = TilesetNumber;
        this.overrideFlag = OverrideFlag;
        load();
    }

    private void load()
    {
        //Palettes
        int palCount = LZ.getDecompressedSize(PalFile.getContents()) / 512;

        palettes = new Palette[palCount];

        LZFile PalFileLz = new LZFile(PalFile, LZFile.COMP_LZ);
        for (int i = 0; i < palCount; i++)
            palettes[i] = new FilePalette(new InlineFile(PalFileLz, i * 512, 512, "Palette " + i));

        //Graphics
        graphics = new Image2D(new LZFile(GFXFile, LZFile.COMP_LZ), 256, false);
        //Map16
        map16 = new Map16Tilemap(Map16File, 32, graphics, palettes, getMap16TileOffset(), getMap16PaletteOffset());
        Overrides = new short[map16.getMap16TileCount()];
        Map16Buffer = map16.render();

        /*
         TilemapEditorTest t = new TilemapEditorTest();
         t.load(map16);
         t.Show();
         */

        //Tile Behaviors
        loadTileBehaviors();

        //Objects
        loadObjects();

        /*            // Finally, load overrides
         if (overrideFlag)
         {
         UseOverrides = true;
         OverrideBufferedImage = Resources.get("tileoverrides");

         Overrides = new short[Map16.length];
         EditorOverrides = new short[Map16.length];

         for (int idx = 0; idx < Map16.length; idx++)
         {
         Overrides[idx] = -1;
         EditorOverrides[idx] = -1;
         }
         }*/
    }

    public void beginEdit() throws AlreadyEditingException
    {

        try
        {
            for (int i = 0; i < palettes.length; i++)
                palettes[i].beginEdit();

            graphics.beginEdit();
            map16.beginEdit();

            ObjFile.beginEdit(this);
            ObjIndexFile.beginEdit(this);
            if (TileBehaviorFile != null)
                TileBehaviorFile.beginEdit(this);
        } catch (AlreadyEditingException ex)
        {
            try
            {
                for (int i = 0; i < palettes.length; i++)
                    palettes[i].endEdit();
                graphics.endEdit();
                map16.endEdit();
            } catch (Exception e)
            {
            }

            if (ObjFile.beingEditedBy(this))
                ObjFile.endEdit(this);
            if (ObjIndexFile.beingEditedBy(this))
                ObjIndexFile.endEdit(this);
            if (TileBehaviorFile != null)
                if (TileBehaviorFile.beingEditedBy(this))
                    TileBehaviorFile.endEdit(this);
            throw ex;
        }
    }

    public void save()
    {
        for (int i = 0; i < palettes.length; i++)
            palettes[i].save();

        graphics.save();
        map16.save();

        saveObjects();
        saveTileBehaviors();
    }

    public void endEdit()
    {
        for (int i = 0; i < palettes.length; i++)
            palettes[i].endEdit();

        graphics.endEdit();
        map16.endEdit();

        ObjFile.endEdit(this);
        ObjIndexFile.endEdit(this);
        if (TileBehaviorFile != null)
            TileBehaviorFile.endEdit(this);
    }

    private void loadTileBehaviors()
    {
        byte[] x = TileBehaviorFile.getContents();

        ArrayReader inp = new ArrayReader(x);

        int len = inp.available() / 4;
        TileBehaviors = new int[len];

        for (int i = 0; i < len; i++)
            TileBehaviors[i] = inp.readInt();
    }

    private void saveTileBehaviors()
    {
        ArrayWriter file = new ArrayWriter();
        for (int i = 0; i < TileBehaviors.length; i++)
            file.writeInt(TileBehaviors[i]);

        TileBehaviorFile.replace(file.getArray(), this);
    }

    public class ObjectDef
    {

        public ArrayList<ArrayList<ObjectDefTile>> tiles;
        public int width, height; //these are useless, but I keep them
        //in case the game uses them.
        private NSMBTileset t;

        public ObjectDef(NSMBTileset t)
        {
            this.t = t;
            tiles = new ArrayList<>();
            ArrayList<ObjectDefTile> row = new ArrayList<>();
            tiles.add(row);
        }

        public ObjectDef(byte[] data, NSMBTileset t)
        {
            this.t = t;
            load(new ArrayReader(data));
        }

        public void load(ArrayReader inp)
        {
            tiles = new ArrayList<>();
            ArrayList<ObjectDefTile> row = new ArrayList<>();

            while (true)
            {
                ObjectDefTile ti = new ObjectDefTile(inp, this.t);
                if (ti.isLineBreak())
                {
                    tiles.add(row);
                    row = new ArrayList<>();
                } else if (ti.isObjectEnd())
                    break;
                else
                    row.add(ti);
            }
        }

        public void save(ArrayWriter outp)
        {
            for (ArrayList<ObjectDefTile> row : tiles)
            {
                for (ObjectDefTile ti : row)
                    ti.write(outp);
                outp.writeByte(0xFE); //new line
            }
            outp.writeByte(0xFF); //end object
        }

        public boolean isSlopeObject()
        {
            if (tiles.isEmpty())
                return false;
            if (tiles.get(0).isEmpty())
                return false;
            return tiles.get(0).get(0).isSlopeControl();
        }

        public int getHeight()
        {
            int r = tiles.size();
            if (tiles.size() < 1)
                r = 1;
            if (isSlopeObject())
                r *= 2;
            return r;
        }

        public int getWidth()
        {
            int w = 1;
            for (ArrayList<ObjectDefTile> row : tiles)
            {
                int tw = 0;
                for (ObjectDefTile tile : row)
                    tw++;
                if (tw > w)
                    w = tw;
            }
            if (isSlopeObject())
                w *= 2;
            return w;
        }
    }

    public class ObjectDefTile
    {

        public int tileID;
        public byte controlByte;
        private NSMBTileset t;

        public boolean isEmptyTile()
        {
            return tileID == -1;
        }

        public boolean isLineBreak()
        {
            return controlByte == (byte) 0xFE;
        }

        public boolean isObjectEnd()
        {
            return controlByte == (byte) 0xFF;
        }

        public boolean isSlopeControl()
        {
            return (controlByte & 0x80) != 0;
        }

        public boolean isControlTile()
        {
            return isLineBreak() || isObjectEnd() || isSlopeControl();
        }

        public ObjectDefTile(NSMBTileset t)
        {
            this.t = t;
        }

        public ObjectDefTile(ArrayReader inp, NSMBTileset t)
        {
            this.t = t;
            if (inp.available() < 1) //This should never happen. But sometimes, it does.
            {
                controlByte = (byte) 0xFF; //Simulate object end.
                return;
            }

            controlByte = inp.readByte();

            if (!isControlTile())
            {
                tileID = inp.readShort();
                if (tileID == 0)
                    tileID = -1;
                else
                    tileID -= t.getObjectDefTileOffset() * 256;

                //OVERRIDES
                //Is this used? dafuqs
                //if ((controlByte & 64) != 0) 
                //    tileID += 768;
            }
        }

        public void write(ArrayWriter outp)
        {
            if (isControlTile())
                outp.writeByte(controlByte);
            else if (isEmptyTile())
            {
                outp.writeByte(0);
                outp.writeByte(0);
                outp.writeByte(0);
            } else
            {
                outp.writeByte(controlByte);
                outp.writeByte((byte) (tileID % 256));
                outp.writeByte((byte) (tileID / 256 + t.getObjectDefTileOffset()));
            }
        }
    }

    public void loadObjects()
    {
        ArrayReader eObjIndexFile = new ArrayReader(ObjIndexFile.getContents());
        ArrayReader eObjFile = new ArrayReader(ObjFile.getContents());

        Objects = new ObjectDef[objectCount];

        //read object index
        int obj = 0;
        while (eObjIndexFile.available(4) && obj < Objects.length)
        {
            Objects[obj] = new ObjectDef(this);
            int offset = eObjIndexFile.readShort();
            Objects[obj].width = eObjIndexFile.readByte();
            Objects[obj].height = eObjIndexFile.readByte();

            eObjFile.seek(offset);
            Objects[obj].load(eObjFile);
            obj++;
        }
    }

    public void saveObjects()
    {
        ArrayWriter eObjIndexFile = new ArrayWriter();
        ArrayWriter eObjFile = new ArrayWriter();

        for (int i = 0; i < Objects.length; i++)
        {
            if (Objects[i] == null)
                break;

            eObjIndexFile.writeShort((short) eObjFile.getPos());
            eObjIndexFile.writeByte((byte) Objects[i].width);
            eObjIndexFile.writeByte((byte) Objects[i].height);
            Objects[i].save(eObjFile);
        }

        ObjFile.replace(eObjFile.getArray(), this);
        ObjIndexFile.replace(eObjIndexFile.getArray(), this);
    }

    public int[][] RenderObject(int ObjNum, int Width, int Height) throws ObjectRenderingException
    {
        // First allocate an array
        int[][] Dest = new int[Width][Height];

        // Non-existent objects shouldn't happen
        if (ObjNum >= Objects.length || ObjNum < 0 || Objects[ObjNum] == null)
            throw new ObjectRenderingException("Object doesn't exist");

        ObjectDef obj = Objects[ObjNum];

        if (Objects[ObjNum].tiles.isEmpty())
            throw new ObjectRenderingException("Objects can't be empty.");

        for (int i = 0; i < Objects[ObjNum].tiles.size(); i++)
            if (Objects[ObjNum].tiles.get(i).isEmpty())
                throw new ObjectRenderingException("Objects can't have empty rows.");

        // Diagonal objects are rendered differently
        if ((Objects[ObjNum].tiles.get(0).get(0).controlByte & 0x80) != 0)
            RenderDiagonalObject(Dest, obj, Width, Height);
        else
        {
            boolean repeatFound = false;
            ArrayList<ArrayList<ObjectDefTile>> beforeRepeat = new ArrayList<>();
            ArrayList<ArrayList<ObjectDefTile>> inRepeat = new ArrayList<>();
            ArrayList<ArrayList<ObjectDefTile>> afterRepeat = new ArrayList<>();

            for (ArrayList<ObjectDefTile> row : obj.tiles)
            {
                if (row.isEmpty())
                    continue;

                if ((row.get(0).controlByte & 2) != 0)
                {
                    repeatFound = true;
                    inRepeat.add(row);
                } else if (repeatFound)
                    afterRepeat.add(row);
                else
                    beforeRepeat.add(row);
            }

            for (int y = 0; y < Height; y++)
                if (inRepeat.isEmpty()) //if no repeat data, just repeat all
                    renderStandardRow(Dest, beforeRepeat.get(y % beforeRepeat.size()), y, Width);
                else if (y < beforeRepeat.size()) //if repeat data
                    renderStandardRow(Dest, beforeRepeat.get(y), y, Width);
                else if (y > Height - afterRepeat.size() - 1)
                    renderStandardRow(Dest, afterRepeat.get(y - Height + afterRepeat.size()), y, Width);
                else
                    renderStandardRow(Dest, inRepeat.get((y - beforeRepeat.size()) % inRepeat.size()), y, Width);

        }
        return Dest;
    }

    private void renderStandardRow(int[][] Dest, ArrayList<ObjectDefTile> row, int y, int width)
    {
        boolean repeatFound = false;
        ArrayList<ObjectDefTile> beforeRepeat = new ArrayList<>();
        ArrayList<ObjectDefTile> inRepeat = new ArrayList<>();
        ArrayList<ObjectDefTile> afterRepeat = new ArrayList<>();

        for (ObjectDefTile tile : row)
            if ((tile.controlByte & 1) != 0)
            {
                repeatFound = true;
                inRepeat.add(tile);
            } else if (repeatFound)
                afterRepeat.add(tile);
            else
                beforeRepeat.add(tile);

        for (int x = 0; x < width; x++)
            if (inRepeat.isEmpty()) //if no repeat data, just repeat all
                Dest[x][y] = beforeRepeat.get(x % beforeRepeat.size()).tileID;
            else if (x < beforeRepeat.size()) //if repeat data
                Dest[x][y] = beforeRepeat.get(x).tileID;
            else if (x > width - afterRepeat.size() - 1)
                Dest[x][y] = afterRepeat.get(x - width + afterRepeat.size()).tileID;
            else
                Dest[x][y] = inRepeat.get((x - beforeRepeat.size()) % inRepeat.size()).tileID;
    }

    private void RenderDiagonalObject(int[][] Dest, ObjectDef obj, int width, int height)
    {
        //empty tiles fill
        for (int xp = 0; xp < width; xp++)
            for (int yp = 0; yp < height; yp++)
                Dest[xp][yp] = -2;

        //get sections
        ArrayList<ObjectDefTile[][]> sections = getSlopeSections(obj);
        ObjectDefTile[][] mainBlock = sections.get(0);
        ObjectDefTile[][] subBlock = null;
        if (sections.size() > 1)
            subBlock = sections.get(1);

        byte controlByte = obj.tiles.get(0).get(0).controlByte;

        //get direction
        boolean goLeft = (controlByte & 1) != 0;
        boolean goDown = (controlByte & 2) != 0;

        //get starting point

        int x = 0;
        int y = 0;
        if (!goDown)
            y = height - mainBlock[0].length;
        if (goLeft)
            x = width - mainBlock.length;

        //get increments
        int xi = mainBlock.length;
        if (goLeft)
            xi = -xi;

        int yi = mainBlock[0].length;
        if (!goDown)
            yi = -yi;

        //this is a strange stop condition.
        //Put tells if we have put a tile in the destination
        //When we don't put any tile in destination we are completely
        //out of bounds, so stop.
        boolean put = true;
        while (put)
        {
            put = false;
            put |= putArray(Dest, x, y, mainBlock, width, height);
            if (subBlock != null)
            {
                int xb = x;
                if (goLeft) // right align
                    xb = x + mainBlock.length - subBlock.length;
                if (goDown)
                    put |= putArray(Dest, xb, y - subBlock[0].length, subBlock, width, height);
                else
                    put |= putArray(Dest, xb, y + mainBlock[0].length, subBlock, width, height);
            }
            x += xi;
            y += yi;
        }
    }

    private boolean putArray(int[][] Dest, int xo, int yo, ObjectDefTile[][] block, int width, int height)
    {
        boolean put = false;
        for (int x = 0; x < block.length; x++)
            for (int y = 0; y < block[0].length; y++)
                put |= putTile(Dest, x + xo, y + yo, width, height, block[x][y]);
        return put;
    }

    private ArrayList<ObjectDefTile[][]> getSlopeSections(ObjectDef d)
    {
        ArrayList<ObjectDefTile[][]> sections = new ArrayList<>();
        ArrayList<ArrayList<ObjectDefTile>> currentSection = null;

        for (ArrayList<ObjectDefTile> row : d.tiles)
        {
            if (!row.isEmpty() && row.get(0).isSlopeControl()) // begin new section
            {
                if (currentSection != null)
                    sections.add(createSection(currentSection));
                currentSection = new ArrayList<>();
            }
            currentSection.add(row);
        }
        if (currentSection != null) //end last section
            sections.add(createSection(currentSection));

        return sections;
    }

    private ObjectDefTile[][] createSection(ArrayList<ArrayList<ObjectDefTile>> tiles)
    {
        //calculate width
        int width = 0;
        for (ArrayList<ObjectDefTile> row : tiles)
        {
            int thiswidth = countTiles(row);
            if (width < thiswidth)
                width = thiswidth;
        }

        //allocate array
        ObjectDefTile[][] section = new ObjectDefTile[width][tiles.size()];

        for (int y = 0; y < tiles.size(); y++)
        {
            int x = 0;
            for (ObjectDefTile t : tiles.get(y))
                if (!t.isControlTile())
                {
                    section[x][y] = t;
                    x++;
                }
        }

        return section;
    }

    private int countTiles(ArrayList<ObjectDefTile> l)
    {
        int res = 0;
        for (ObjectDefTile t : l)
            if (!t.isControlTile())
                res++;
        return res;
    }

    private boolean putTile(int[][] Dest, int x, int y, int width, int height, ObjectDefTile t)
    {
        boolean put = false;

        if (x >= 0 && x < width)
            if (y >= 0 && y < height)
            {
                put = true;
                if (t != null)
                    Dest[x][y] = t.tileID;
            }

        return put;
    }

    public boolean objectExists(int objNum)
    {
        if (objNum < 0)
            return false;
        if (objNum >= Objects.length)
            return false;
        if (Objects[objNum] == null)
            return false;
        return true;
    }
    public static final String tilesetFileHeader = "NSMBe Exported Tileset";

    public void exportTileset(String filename) throws IOException
    {
        DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));

        out.writeUTF(tilesetFileHeader);
        writeFileContents(PalFile, out);
        writeFileContents(GFXFile, out);
        writeFileContents(Map16File, out);
        writeFileContents(ObjFile, out);
        writeFileContents(ObjIndexFile, out);
        if (TileBehaviorFile != null)
            writeFileContents(TileBehaviorFile, out);

        out.close();
    }

    private void writeFileContents(File f, DataOutputStream out) throws IOException
    {
        out.writeInt(f.getFileSize());
        out.write(f.getContents());
    }

    public void importTileset(String filename) throws AlreadyEditingException
    {
        try
        {
            PalFile.beginEdit(this);
            GFXFile.beginEdit(this);
            Map16File.beginEdit(this);
            ObjFile.beginEdit(this);
            ObjIndexFile.beginEdit(this);
            if (TileBehaviorFile != null)
                TileBehaviorFile.beginEdit(this);
        } catch (AlreadyEditingException ex)
        {
            if (PalFile.beingEditedBy(this))
                PalFile.endEdit(this);
            if (GFXFile.beingEditedBy(this))
                GFXFile.endEdit(this);
            if (Map16File.beingEditedBy(this))
                Map16File.endEdit(this);
            if (ObjFile.beingEditedBy(this))
                ObjFile.endEdit(this);
            if (ObjIndexFile.beingEditedBy(this))
                ObjIndexFile.endEdit(this);
            if (TileBehaviorFile != null)
                if (TileBehaviorFile.beingEditedBy(this))
                    TileBehaviorFile.endEdit(this);
            throw ex;
        }

        //TODO
/*
         System.IO.BinaryReader br = new System.IO.BinaryReader(
         new System.IO.FileStream(filename, System.IO.FileMode.Open, System.IO.FileAccess.Read));
         String header = br.ReadString();
         if (header != tilesetFileHeader)
         {
         MessageBox.Show(
         LanguageManager.Get("NSMBLevel", "InvalidFile"),
         LanguageManager.Get("NSMBLevel", "Unreadable"),
         MessageBoxButtons.OK, MessageBoxIcon.Error);
         return;
         }

         readFileContents(PalFile, br);
         readFileContents(GFXFile, br);
         readFileContents(Map16File, br);
         readFileContents(ObjFile, br);
         readFileContents(ObjIndexFile, br);
         if (TileBehaviorFile != null)
         readFileContents(TileBehaviorFile, br);
         br.Close();
         */
        PalFile.endEdit(this);
        GFXFile.endEdit(this);
        Map16File.endEdit(this);
        ObjFile.endEdit(this);
        ObjIndexFile.endEdit(this);
        if (TileBehaviorFile != null)
            TileBehaviorFile.endEdit(this);
    }

    void readFileContents(File f, DataInputStream br) throws IOException
    {
        int len = br.readInt();
        byte[] data = new byte[len];
        br.read(data, 0, len);
        f.replace(data, this);
    }

    public int getMap16TileOffset()
    {
        if (TilesetNumber == 1)
            return 192;
        else if (TilesetNumber == 2)
            return 640;
        else
            return 0;
    }

    public int getObjectDefTileOffset()
    {
        if (TilesetNumber == 1)
            return 1;
        else if (TilesetNumber == 2)
            return 4;
        else
            return 0;
    }

    public int getMap16PaletteOffset()
    {
        if (TilesetNumber == 0)
            return 0;
        if (TilesetNumber == 1)
            return 2;
        else
            return 6;
    }
}
