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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.dirbaio.nds.nsmb.leveleditor.LevelEditorComponent;
import net.dirbaio.nds.util.ArrayReader;
import net.dirbaio.nds.util.ArrayWriter;
import net.dirbaio.nds.util.LanguageManager;
import net.dirbaio.nds.util.Resources;
import net.dirbaio.nds.util.Util;
import net.dirbaio.nds.util.Util.RotateFlipType;

public class NSMBSprite implements LevelItem
{

    public int X;
    public int Y;
    public int Type;
    public byte[] Data;
    private NSMBLevel Level;
//    private static SolidBrush invalidBrush = new SolidBrush(Color.FromArgb(100, 255, 0, 0));

    public NSMBSprite(NSMBLevel Level)
    {
        this.Level = Level;
    }

    public NSMBSprite(NSMBSprite s)
    {
        this.X = s.X;
        this.Y = s.Y;
        this.Type = s.Type;
        this.Level = s.Level;

        this.Data = new byte[6];
        System.arraycopy(s.Data, 0, Data, 0, 6);
    }
    private static int[] AlwaysDrawNums =
    {
        68, 69, 73, 141, 226, 305, 306
    };
    private static int[] TileCreateNums =
    {
        0x67, 0x50, -0x870, 0x1B, 0x19, 0x67, -0x430, -0x240, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x18
    };

    public boolean AlwaysDraw()
    {
        for (int i = 0; i < AlwaysDrawNums.length; i++)
            if (AlwaysDrawNums[i] == Type)
                return true;

        return false;
    }

    @Override
    public void setRect(Rectangle r)
    {
        //rofl
        Rectangle r2 = getRect();
        int snap = getSnap();
        X += (r.x - r2.x) / snap;
        Y += (r.y - r2.y) / snap;
    }

    @Override
    public Rectangle getRealRect()
    {
        int snap = getSnap();
        return new Rectangle(X * snap, Y * snap, snap, snap);
    }

    @Override
    public boolean isResizable()
    {
        return false;
    }

    @Override
    public int getSnap()
    {
        return 16;
    }
    
    @Override
    public LevelItem clone()
    {
        return new NSMBSprite(this);
    }

    @Override
    public Rectangle getRect()
    {
        int x = this.X * 16;
        int y = this.Y * 16;
        int width = 16;
        int height = 16;
        switch (this.Type)
        {
            case 23:
                y -= 28;
                width = 32;
                height = 28;
                break;
            case 24:
                y += 32;
                width = 32;
                height = 28;
                break;
            case 25:
                x += 32;
                width = 28;
                height = 32;
                break;
            case 26:
                x -= 28;
                width = 28;
                height = 32;
                break;
            case 27:
                height = (Data[5] & 0xF0) + 32;
                y -= height - 16;
                break;
            case 28:
                height = 18;
                y -= 2;
                break;
            case 31:
                width = 21;
                break;
            case 32:
                x -= 29;
                y -= 119;
                width = 27;
                height = 24;
                break;
            case 33:
                if ((Data[5] & 0xF) == 1)
                {
                    x -= 8;
                    y -= 16;
                    width = 32;
                    height = 32;
                }
                if (((Data[5] >> 4) & 0xF) == 1)
                    x += 8;
                break;
            case 34:
                x -= 7;
                y -= 26;
                width = 30;
                height = 42;
                if (((Data[5] >> 4) & 0xF) == 1)
                    x += 8;
                break;
            case 36:
                width = 32;
                height = 36;
                break;
            case 37:
                width = 20;
                break;
            case 38:
                width = 24;
                height = 20;
                break;
            //case 40: see 183
            case 42:
                x -= 8;
                y -= 48;
                height = 48;
                if ((Data[5] & 0xF) == 1)
                {
                    x -= 29;
                    width = 45;
                }
                break;
            case 43:
                y -= 28;
                height = 28;
                x -= 29;
                width = 44;
                break;
            case 48:
                y -= 90;
                width = 32;
                height = 74;
                break;
            case 49:
                y += 32;
                width = 32;
                height = 74;
                break;
            case 50:
                x += 32;
                width = 74;
                height = 32;
                break;
            case 51:
                x -= 90;
                width = 74;
                height = 32;
                break;
            //case 52:
            //    Buzzy Beetles don't need to be modified
            case 53:
                y -= 10;
                width = 17;
                height = 26;
                break;
            case 54:
                height = 29;
                break;
            case 55:
                width = 21;
                break;
            case 56:
                width = ((Data[5] & 0xF)) * 10;
                y -= width;
                if (((Data[5] >> 4) & 0xF) == 1)
                {
                    x -= width;
                    width *= 2;
                }
                width += 12;
                height = width;
                break;
            case 57:
                if ((Data[5] & 0xF) == 1)
                {
                    x -= 4;
                    y -= 4;
                    width = 24;
                    height = 24;
                } else if ((Data[5] & 0xF) == 2)
                {
                    x -= 4;
                    y -= 20;
                    width = 72;
                    height = 40;
                }
                if ((Data[5] & 0xF) != 1 && (Data[5] & 0xF) != 2)
                {
                    if (((Data[3] >> 4) & 0xF) == 1)
                        y += 8;
                    if ((Data[2] & 0xF) == 1)
                        x += 8;
                }
                break;
            case 59:
                y -= 18;
                width = 18;
                height = 34;
                break;
            case 64:
                if ((Data[5] & 0xF) == 1)
                {
                    x -= 38;
                    y -= 74;
                    width = 96;
                    height = 90;
                } else
                {
                    x -= 29;
                    y -= 57;
                    width = 78;
                    height = 73;
                }
                break;
            case 66:
                if ((Data[5] & 0xF) != 1)
                    y -= 2;
                if (((Data[5] >> 4) & 0xF) == 1)
                    x += 8;
                height = 18;
                break;
            case 67:
                x -= 27;
                y -= 8;
                width = 71;
                height = 26;
                break;
            case 68:
            case 69:
                width = Math.max(16 * ((Data[5] & 0xF) + 1), 32);
                break;
            case 70:
                if ((Data[5] & 0xF) == 1)
                {
                    x -= 56;
                    width = 128;
                } else
                {
                    x -= 24;
                    width = 64;
                }
                break;
            case 71:
            case 72:
                width = Math.max(16 * ((Data[5] & 0xF) + 1), 32);
                break;
            case 73:
                width = 192;
                height = 32;
                break;
            case 74:
                y -= 8;
                width = 64;
                height = 40;
                break;
            case 75:
                width = 256;
                break;
            case 76:
                x -= 24;
                y -= 10;
                width = 16 * ((Data[5] & 0xF)) + 48;
                height = 16 * Math.max(((Data[5] >> 4) & 0xF), (Data[4] & 0xF)) + 18;
                break;
            case 77:
            case 78:
                width = (Data[5] & 0xF);
                int s = 2;
                if (this.Type == 78)
                {
                    width = Math.max(0, width - 1);
                    s = 1;
                    x -= 8;
                }
                x -= (width * s + 1) * 8;
                width = (width * s + 2) * 16;
                break;
            case 79:
                x -= 28;
                y -= 34;
                width = 92;
                height = 85;
                break;
            case 80:
                if ((Data[3] & 0xF) == 1)
                    y -= 16;
                else
                    y += 16;
                width = 64;
                break;
            case 82:
                x -= 24;
                y -= 8;
                width = 64;
                height = 32;
                break;
            case 86:
                x -= 14;
                y -= 6;
                width = 44;
                height = 39;
                break;
            case 89:
                y -= 16;
                width = 30;
                height = 32;
                break;
            case 90:
                if ((Data[5] & 0xF) == 1)
                {
                    y -= 44;
                    width = 92;
                    height = 60;
                } else
                {
                    y -= 14;
                    width = 52;
                    height = 30;
                }
                break;
            case 91:
                y += 8;
                width = 64;
                break;
            case 92:
                if ((Data[5] & 0xF) == 1)
                {
                    x += 3;
                    y += 7;
                    width = 25;
                    height = 23;
                } else if (((Data[5] >> 4) & 0xF) == 1)
                {
                    x += 45;
                    y += 3;
                    width = 104;
                    height = 28;
                } else
                {
                    x += 12;
                    y += 3;
                    width = 104;
                    height = 28;
                }
                break;
            case 93:
                width = 48;
                height = 48;
                if ((Data[3] & 0xF) == 1)
                {
                    width = 24;
                    height = 24;
                }
                if (((Data[5] >> 4) & 0xF) % 2 == 1)
                {
                    x += (width / 9) * ((((Data[5] >> 4) & 0xF) > 4) ? 1 : -1);
                    y += (width / 9) * ((((Data[5] >> 4) & 0xF) == 3 || ((Data[5] >> 4) & 0xF) == 5) ? 1 : -1);
                }
                x -= (width - 16) / 2;
                y -= (height - 16);
                if (((Data[4] >> 4) & 0xF) == 1)
                    x += 8;
                if ((Data[4] & 0xF) == 1)
                    y += 8;
                break;
            case 94:
                if ((Data[5] & 0xF) == 1)
                {
                    x -= 6;
                    y -= 16;
                    width = 26;
                    height = 37;
                } else
                    height = 18;
                break;
            case 95:
                x -= 6;
                width = 26;
                height = 26;
                break;
            case 96:
                x -= 32;
                y -= 127;
                width = 90;
                height = 147;
                break;
            case 99:
                x -= 38;
                y -= 29;
                width = 91;
                height = 75;
                break;
            case 102:
                width = 30;
                height = 32;
                break;
            case 103:
                switch ((Data[2] & 0xF))
                {
                    case 2:
                        x -= 40;
                        y -= 59;
                        width = 82;
                        height = 75;
                        break;
                    case 3:
                        x -= 40;
                        y -= 59;
                        width = 83;
                        height = 75;
                        break;
                    default:
                        x -= 51;
                        y -= 59;
                        width = 115;
                        height = 76;
                        break;
                }
                break;
            case 104:
                x -= 88;
                y -= 130;
                width = 191;
                height = 150;
                break;
            case 105:
                x -= 78;
                y -= 164;
                width = 165;
                height = 182;
                break;
            //case 106:
            //    Red Coins don't need to be modified
            case 107:
            case 108:
                if ((Data[5] & 0xF) != 1)
                    y -= 2;
                if (((Data[5] >> 4) & 0xF) == 1)
                    x += 8;
                height = 18;
                break;
            case 109:
                x -= 6;
                y -= 6;
                int shift = ((Data[5] >> 4) & 0xF);
                if (shift == 1 || shift == 3)
                    x += 8;
                if (shift == 2 || shift == 3)
                    y += 8;
                height = 28;
                width = 27;
                break;
            case 111:
                x -= 128;
                y -= 32;
                width = 256;
                height = 64;
                break;
            case 113:
                width = 60;
                height = 47;
                break;
            case 114:
            case 118:
                int w = 64;
                if (this.Type == 118)
                    w = 128;
                switch (Data[5] % 0x40)
                {
                    case 0:
                        width = w;
                        height = 16;
                        break;
                    case 1:
                        x -= w - 16;
                        width = w;
                        height = 16;
                        break;
                    case 2:
                        y -= w - 16;
                        width = 16;
                        height = w;
                        break;
                    case 3:
                        width = 16;
                        height = w;
                        break;
                }
                break;
            case 115:
                y -= 42;
                width = 63;
                height = 58;
                break;
            case 116:
                x -= 16;
                y -= 5;
                width = 41;
                height = 21;
                break;
            case 117:
                x -= 12;
                y -= 8;
                width = 44;
                height = 23;
                break;
            case 119:
                int sz = ((Data[2] >> 4) & 0xF);
                if (sz == 0)
                    sz = 1;
                width = sz * 64;
                height = sz * 68;
                x -= sz * 32;
                y -= sz * 6;
                break;
            case 120:
                y += 3;
                width = 33;
                height = 29;
                break;
            case 122:
                x -= 37;
                y -= 48;
                width = 64;
                height = 64;
                break;
            case 123:
                x -= 8;
                y += 2;
                width = 40;
                height = 30;
                break;
            case 124:
                x -= 38;
                y -= 45;
                width = 79;
                height = 61;
                break;
            case 126:
                width = (Data[2] & 0xF0) * 2;
                x -= width / 2;
                if (width == 0)
                    width = 16;
                break;
            case 127:
                x -= 144;
                y -= 104;
                width = 288;
                height = 208;
                break;
            case 128:
                x -= 9;
                y -= 26;
                width = 66;
                height = 90;
                break;
            case 130:
                width = 21;
                break;
            case 131:
            case 132:
                width = 22;
                height = 33;
                break;
            case 136:
                width = 18;
                height = 16 * (Data[5] + 2) + 9;
                y -= height - 16;
                break;
            case 141:
                x -= 128;
                width = 276;
                height = 128;
                break;
            case 142:
                x -= 8;
                width = (((Data[2] >> 4) & 0xF) + 1) * 16;
                height = ((Data[2] & 0xF));
                if (height >= 8)
                    height -= 16;
                if (height >= 0)
                {
                    y -= height * 16;
                    height += 1;
                } else
                    height -= 1;
                height *= 16;
                height = Math.abs(height);
                break;
            case 144:
                x -= 2;
                y -= 7;
                width = 19;
                height = 26;
                break;
            case 146:
                width = 32;
                height = 8;
                break;
            case 147:
                x -= Data[5] * 8 + 6;
                width = 16 * (Data[5] - 1) + 28;
                height = 10;
                break;
            case 148:
                y -= 2;
                height = 18;
                break;
            case 149:
                if (((Data[5] >> 4) & 0xF) != 1)
                {
                    y -= 13;
                    height = 29;
                }
                break;
            case 150:
                y -= 16;
                width = 19;
                height = 32;
                break;
            //case 152:
            //    Switch blocks don't need to be modified
            case 155:
                width = 16 * ((Data[4] & 0xF) + 1);
                height = 16 * (((Data[5] >> 4) & 0xF) + 1);
                break;
            case 157:
                y -= 15;
                width = 18;
                height = 31;
                break;
            case 158:
                y -= 18;
                width = 18;
                height = 34;
                break;
            case 162:
                x -= 16 * (((Data[4] >> 4) & 0xF) + 1);
                width = 32 * (((Data[4] >> 4) & 0xF) + 2);
                height = 16 * ((Data[3] & 0xF) + 4);
                break;
            case 173:
                x -= 3;
                width = 8;
                height = 16 * ((Data[5] & 0xF) + 4);
                break;
            case 174:
                x -= 16 * (((Data[4] >> 4) & 0xF) + 1);
                width = 32 * (((Data[4] >> 4) & 0xF) + 2);
                height = 64;
                break;
            case 175:
                x -= 40;
                y -= 48;
                width = 96;
                height = 64;
                break;
            case 180:
                width = 18;
                height = 26;
                break;
            case 40:
            case 183:
                y -= 23;
                width = 26;
                height = 39;
                break;
            case 186:
                y -= 12;
                width = 24;
                height = 28;
                break;
            case 187:
                width = 112;
                height = 16;
                break;
            case 189:
                y -= 48;
                width = 32;
                height = 64;
                break;
            case 191:
                y -= 80;
                height = ((Data[3] >> 4) & 0xF);
                if (height == 0)
                    height = 8;
                height = (height + 1) * 16;
                break;
            case 193:
                y -= 28;
                width = 25;
                height = 44;
                break;
            case 194:
                width = 64;
                height = 74;
                break;
            case 197:
                width = 16 * Math.max(1, ((Data[5] >> 4) & 0xF));
                height = 16 * Math.max(1, (Data[5] & 0xF));
                break;
            case 204:
                y -= 7;
                width = 27;
                height = 23;
                break;
            case 205:
                y -= 27;
                width = 43;
                height = 33;
                break;
            case 206:
                y -= 32;
                width = 256;
                height = 48;
                break;
            case 207:
                x -= 20;
                y -= 32;
                width = 63;
                height = 52;
                break;
            case 209:
                y -= 15;
                width = 26;
                height = 31;
                break;
            case 210:
                width = 32;
                height = 32;
                break;
            case 211:
                y -= 16;
                width = 26;
                height = 32;
                break;
            case 212:
                y -= 16;
                width = 59;
                height = 32;
                break;
            case 213:
                y -= 32;
                x -= 8;
                width = 42;
                height = 64;
                break;
            case 219:
                switch (((Data[5] >> 4) & 0xF))
                {
                    case 1:
                        x -= 4;
                        width = 20;
                        break;
                    case 2:
                        height = 20;
                        break;
                    case 3:
                        width = 20;
                        break;
                    default:
                        y -= 4;
                        height = 20;
                        break;
                }
                break;
            case 220:
                y -= 25;
                width = 31;
                height = 41;
                break;
            case 222:
                if (((Data[5] >> 4) & 0xF) == 1)
                    x += 8;
                if ((Data[5] & 0xF) == 1)
                    y += 8;
                width = 8;
                height = 8;
                break;
            case 223:
                width = 32;
                height = 32;
                break;
            case 224:
                width = 192;
                height = 64;
                break;
            case 226:
                y += (Data[5] & 0xF0) + 64;
                x -= 14;
                width = 44;
                height = 37;
                break;
            case 227:
                y -= 5;
                width = 23;
                height = 21;
                break;
            case 228:
                width = 20;
                height = 20;
                break;
            case 232:
                y -= 128;
                height += 128;
                break;
            case 233:
                x -= 11;
                y -= 11;
                width = 22;
                height = ((Data[5] & 0xF)) * 16 + 75;
                break;
            case 235:
                if (((Data[5] >> 4) & 0xF) == 1)
                    x += 8;
                if ((Data[5] & 0xF) == 1)
                    y += 8;
                width = 32;
                height = 32;
                break;
            case 236:
                x -= 16;
                y -= 16;
                if (((Data[2] >> 4) & 0xF) == 1)
                    y += 8;
                width = 48;
                height = 48;
                break;
            case 237:
                y -= 15;
                width = 45;
                height = 31;
                break;
            case 238:
                x -= 16 * (((Data[5] >> 4) & 0xF) + 1);
                width = 32 * (((Data[5] >> 4) & 0xF) + 2);
                height = 112;
                break;
            case 239:
                x -= 8 * ((Data[5] & 0xF) + 1);
                width = 16 * ((Data[5] & 0xF) + 2);
                height = 16 * (Math.max(((Data[5] >> 4) & 0xF), (Data[4] & 0xF)) + 1) + 32;
                break;
            case 241:
                width = 23;
                height = Math.min(8, Data[3] + 1) * 16;
                y -= height - 16;
                x -= 3;
                break;
            case 242:
                if (((Data[5] >> 4) & 0xF) == 1)
                    if ((Data[4] & 0xF) == 1)
                    {
                        x -= 32;
                        width = 80;
                    } else
                    {
                        x -= 72;
                        width = 160;
                    }
                else
                {
                    x -= 8;
                    width = 32;
                }
                height = 32 + 16 * ((Data[5] & 0xF));
                break;
            //case 243:
            //    Roof Spinys don't need to be modified
            case 244:
                switch (((Data[5] >> 4) & 0xF))
                {
                    case 1:
                        x -= 8;
                        break;
                    case 2:
                        x += 8;
                        break;
                }
                x -= 40;
                width = 96;
                height = 16 * ((Data[5] & 0xF) + 2);
                break;
            case 246:
                x -= 16;
                y -= 8;
                width = 48;
                height = 48;
                break;
            case 247:
                x -= 27;
                y -= 8;
                width = 71;
                height = 26;
                break;
            case 248:
                x -= 53;
                y -= 106;
                width = 109;
                height = 108;
                break;
            case 249:
                x -= (Data[5] & 0xF) * 8 + 24;
                y += 5;
                width = (Data[5] & 0xF) * 16 + 64;
                height = ((Data[4] >> 4) & 0xF) * 16 + 83;
                break;
            case 250:
                width = 22;
                height = 14;
                break;
            case 252:
                x -= 32;
                y -= 64;
                width = 64;
                height = 80;
                break;
            case 254:
                y -= 3;
                width = 16;
                height = 19;
                break;
            case 255:
                width = 256;
                height = 64;
                break;
            case 256:
                width = 96;
                height = 16;
                if (((Data[3] >> 4) & 0xF) == 0)
                    x -= 80;
                break;
            case 265:
                x += 8;
                width = 16;
                height = 20;
                break;
            case 268:
                x -= 26;
                y -= 26;
                width = 53;
                height = 53;
                break;
            case 272:
                if ((Data[5] & 0xF) == 1)
                {
                    x -= 31;
                    y -= 12;
                    width = 34;
                    height = 23;
                } else
                {
                    x += 15;
                    y -= 12;
                    width = 30;
                    height = 23;
                }
                break;
            case 273:
                width = 32;
                height = 32;
                break;
            case 274:
                x -= 16;
                height = 35;
                width = ((Data[5] & 0xF) + 3) * 16;
                break;
            case 275:
                height = ((Data[5] & 0xF)) * 16 + 30;
                y -= height - 16;
                break;
            case 277:
                width = 32;
                height = 32;
                break;
            case 278:
                width = ((Data[5] & 0xF) + 1) * 16;
                height = (((Data[5] >> 4) & 0xF) + 1) * 16;
                break;
            case 279:
                int nvalue = (Data[5] & 0xF);
                if (nvalue == 0 || nvalue == 1)
                {
                    y -= 39;
                    width = 16;
                    height = 39;
                } else if (nvalue == 2 || nvalue == 3)
                {
                    y += 16;
                    width = 16;
                    height = 39;
                } else if (nvalue == 4 || nvalue == 5)
                {
                    x += 16;
                    width = 39;
                    height = 16;
                } else if (nvalue == 6 || nvalue == 7)
                {
                    x -= 39;
                    width = 39;
                    height = 16;
                }
                break;
            case 281:
                if ((Data[5] & 0xF) == 1)
                    y += 16;
                else
                    y -= 15;
                x += 10;
                width = 13;
                height = 15;
                break;
            case 282:
                x -= 16;
                width = 32;
                height = ((Data[5] & 0xF) + 4) * 16;
                break;
            case 283:
                y -= 32;
                width = 54;
                height = 55;
                break;
            case 284:
                y -= 2;
                width = 18;
                height = 18;
                break;
            case 285:
                y -= 8;
                width = 46;
                height = 24;
                break;
            //case 289:
            //  Expandable Blocks don't need to be modified
            case 290:
                x -= 9;
                y -= 13;
                width = 38;
                height = 28;
                break;
            case 292:
                y -= 32;
                width = 32;
                height = 48;
                break;
            case 298:
                width = Math.max(16 * ((Data[5] & 0xF) + 1), 32);
                height = 16 * (((Data[5] >> 4) & 0xF) + 1);
                int spikes = (Data[2] & 0xF);
                if (width == 32 && (spikes == 1 || spikes == 2))
                    width = 48;
                if (width == 32 && spikes == 3)
                    width = 64;
                if (height == 16 && (spikes == 4 || spikes == 5))
                    height = 32;
                if (height == 16 && spikes == 6)
                    height = 48;
                break;
            case 300:
                y += -8 + (Data[5] & 0xF);
                x -= 64;
                width = 128;
                height = 16;
                break;
            case 303:
                x += 4;
                y -= 61;
                width = 72;
                height = 74;
                break;
            case 260:
            case 304:
                y -= 128;
                width = 64;
                height = 144;
                break;
            case 261:
            case 307:
                width = 64;
                height = 144;
                break;
            case 262:
            case 308:
                width = 144;
                height = 64;
                break;
            case 263:
            case 309:
                x -= 128;
                width = 144;
                height = 64;
                break;
            //case 305:
            //case 306:
            //    These are still selected with the original sprite icon
            case 312:
                width = ((Data[5] & 0xF) + 2) * 16;
                x -= (width - 16) / 2;
                height = (Data[5] & 0xF0) + 49;
                break;
            case 323:
                height = 32;
                width = 32 * (Data[5] + 2);
                break;
        }

        return new Rectangle(x, y, width, height);
    }

    public Rectangle getRectB()
    {
        Rectangle rect = getRect();
        int t = rect.x;
        rect.x = (int) Math.floor((float) rect.x / 16);
        rect.width += t - 16 * rect.x;
        t = rect.y;
        rect.y = (int) Math.floor((float) rect.y / 16);
        rect.height += t - 16 * rect.y;
        rect.width = (int) Math.ceil((float) rect.getWidth() / 16);
        rect.height = (int) Math.ceil((float) rect.getHeight() / 16);
        return rect;
    }

    @Override
    public void render(Graphics2D g, LevelEditorComponent ed)
    {
        int RenderX = X * 16, RenderX2 = RenderX;
        int RenderY = Y * 16, RenderY2 = RenderY;
        int width, height;
        BufferedImage img = null;

        boolean customRendered = true;

        switch (this.Type)
        {
            case 23:
                g.drawImage(Resources.get("PiranhaplantTube"), RenderX, RenderY - 28, null);
                break;
            case 24:
                img = Resources.get("PiranhaplantTube");
                img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipY);
                g.drawImage(img, RenderX, RenderY + 32, null);
                break;
            case 25:
                img = Resources.get("PiranhaplantTube");
                img = Util.rotateFlip(img, RotateFlipType.Rotate90FlipNone);
                g.drawImage(img, RenderX + 32, RenderY, null);
                break;
            case 26:
                img = Resources.get("PiranhaplantTube");
                img = Util.rotateFlip(img, RotateFlipType.Rotate270FlipNone);
                g.drawImage(img, RenderX - 28, RenderY, null);
                break;
            case 27:
                for (int l = 0; l <= ((Data[5] >> 4) & 0xF); l++)
                    if (l == ((Data[5] >> 4) & 0xF))
                        g.drawImage(Resources.get("BulletBillCannonTop"), RenderX, RenderY - 16, null);
                    else
                    {
                        g.drawImage(Resources.get("BulletBillCannon"), RenderX, RenderY, null);
                        RenderY -= 16;
                    }
                break;
            case 28:
                g.drawImage(Resources.get("BomOmb"), RenderX, RenderY - 2, null);
                break;
            case 31:
                g.drawImage(Resources.get("CheepCheep"), RenderX, RenderY, null);
                break;
            case 32:
                if (((Data[2] >> 4) & 0xF) == 1)
                    img = Resources.get("EndingFlagRed");
                else
                    img = Resources.get("EndingFlag");
                g.drawImage(img, RenderX - 29, RenderY - 119, null);
                break;
            case 33:
                if ((Data[5] & 0xF) == 1)
                {
                    img = Resources.get("SpringGiant");
                    RenderY -= 16;
                    RenderX -= 8;
                } else
                    img = Resources.get("Spring");
                if (((Data[5] >> 4) & 0xF) == 1)
                    RenderX += 8;
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 34:
                if (((Data[5] >> 4) & 0xF) == 1)
                    RenderX += 8;
                g.drawImage(Resources.get("RedCoinRing"), RenderX - 7, RenderY - 26, null);
                break;
            case 36:
                g.drawImage(Resources.get("Thwomp"), RenderX, RenderY, null);
                break;
            case 37:
                g.drawImage(Resources.get("Spiny"), RenderX, RenderY, null);
                break;
            case 38:
                g.drawImage(Resources.get("Boo"), RenderX, RenderY, null);
                break;
            //case 40: see 183
            case 42:
                g.drawImage(Resources.get("ChainChompLog"), RenderX - 8, RenderY - 48, null);
                if ((Data[5] & 0xF) == 1)
                    g.drawImage(Resources.get("ChainChomp"), RenderX - 37, RenderY - 28, null);
                break;
            case 43:
                g.drawImage(Resources.get("ChainChomp"), RenderX - 29, RenderY - 28, null);
                break;
            case 48:
                g.drawImage(Resources.get("TubeBubbles"), RenderX, RenderY - 90, null);
                break;
            case 49:
                img = Resources.get("TubeBubbles");
                img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipY);
                g.drawImage(img, RenderX, RenderY + 32, null);
                break;
            case 50:
                img = Resources.get("TubeBubbles");
                img = Util.rotateFlip(img, RotateFlipType.Rotate90FlipNone);
                g.drawImage(img, RenderX + 32, RenderY, null);
                break;
            case 51:
                img = Resources.get("TubeBubbles");
                img = Util.rotateFlip(img, RotateFlipType.Rotate270FlipNone);
                g.drawImage(img, RenderX - 90, RenderY, null);
                break;
            case 52:
                if ((Data[5] & 0xF) == 1)
                    img = Resources.get("BuzzyBeetleU");
                else
                    img = Resources.get("BuzzyBeetle");
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 53:
                g.drawImage(Resources.get("DryBones"), RenderX, RenderY - 10, null);
                break;
            case 54:
                g.drawImage(Resources.get("FireBall"), RenderX, RenderY, null);
                break;
            case 55:
                g.drawImage(Resources.get("BulletBill"), RenderX, RenderY, null);
                break;
            case 56:
                RenderY2 = ((Data[5] & 0xF)) * 10;
                if (((Data[5] >> 4) & 0xF) == 1)
                {
                    RenderX -= RenderY2;
                    RenderY += RenderY2;
                }
                while (!(RenderX == RenderX2 + RenderY2 + 10))
                {
                    if (RenderX == RenderX2)
                        g.drawImage(Resources.get("FireBarMiddle"), RenderX + 4, RenderY + 4, null);
                    else
                        g.drawImage(Resources.get("FireBarBall"), RenderX, RenderY, null);
                    RenderX += 10;
                    RenderY -= 10;
                }
                break;
            case 57:
                if ((Data[5] & 0xF) != 1 && (Data[5] & 0xF) != 2)
                {
                    if (((Data[3] >> 4) & 0xF) == 1)
                        RenderY += 8;
                    if ((Data[2] & 0xF) == 1)
                        RenderX += 8;
                }
                if ((Data[5] & 0xF) == 1)
                    g.drawImage(Resources.get("CoinInBubble"), RenderX - 4, RenderY - 4, null);
                else if ((Data[5] & 0xF) == 2)
                {
                    g.drawImage(Resources.get("CoinInBubble"), RenderX - 4, RenderY - 4, null);
                    g.drawImage(Resources.get("CoinInBubble"), RenderX + 20, RenderY - 20, null);
                    g.drawImage(Resources.get("CoinInBubble"), RenderX + 44, RenderY - 4, null);
                } else
                    g.drawImage(Resources.get("Coin"), RenderX, RenderY, null);
                break;
            case 59:
                g.drawImage(Resources.get("HammerBro"), RenderX, RenderY - 18, null);
                break;
            case 64:
                if ((Data[5] & 0xF) == 1)
                {
                    img = Resources.get("WhompL");
                    RenderX -= 38;
                    RenderY -= 74;
                } else
                {
                    img = Resources.get("Whomp");
                    RenderX -= 29;
                    RenderY -= 57;
                }
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 66:
                img = Resources.get("PSwitch");
                if ((Data[5] & 0xF) == 1)
                    img = Util.rotateFlip(img, RotateFlipType.Rotate180FlipNone);
                else
                    RenderY -= 2;
                if (((Data[5] >> 4) & 0xF) == 1)
                    RenderX += 8;
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 67:
                g.drawImage(Resources.get("Shark"), RenderX - 27, RenderY - 8, null);
                break;
            case 68:
            case 69:
                int dist = ((Data[4] & 0xF)) * 16;
                if ((Data[3] & 0xF) == 1)
                    dist = -dist;
                if (this.Type == 68)
                    RenderY2 -= dist;
                else
                    RenderX2 += dist;
                width = Math.max(16 * ((Data[5] & 0xF) + 1), 32) / 2;
                g.setColor(Color.WHITE);
                g.drawLine(RenderX + width, RenderY + 8, RenderX2 + width, RenderY2 + 8);
                g.fillOval(RenderX2 + width - 4, RenderY2 + 4, 7, 7);
                g.drawImage(Resources.get("MovingPlatformLeft"), RenderX, RenderY, null);
                for (int l = 0; l < (Data[5] & 0xF) - 1; l++)
                {
                    RenderX += 16;
                    g.drawImage(Resources.get("MovingPlatformSection"), RenderX, RenderY, 16, 16, null);
                }
                g.drawImage(Resources.get("MovingPlatformRight"), RenderX + 16, RenderY, null);
                RenderX = X * 16;
                RenderY = Y * 16;
                if (RenderX != RenderX2 || RenderY != RenderY2)
                {
                    g.drawImage(Resources.get("MovingPlatformLeftE"), RenderX2, RenderY2, null);
                    for (int l = 0; l < (Data[5] & 0xF) - 1; l++)
                    {
                        RenderX2 += 16;
                        g.drawImage(Resources.get("MovingPlatformSectionE"), RenderX2, RenderY2, null);
                    }
                    g.drawImage(Resources.get("MovingPlatformRightE"), RenderX2 + 16, RenderY2, null);
                }
                break;
            case 70:
                if ((Data[5] & 0xF) == 1)
                    g.drawImage(Resources.get("SpinningLogL"), RenderX - 56, RenderY, null);
                else
                    g.drawImage(Resources.get("SpinningLog"), RenderX - 24, RenderY, null);
                break;
            case 73:
                g.drawImage(Resources.get("HangingPlatform"), RenderX, RenderY, null);
                RenderX += 92;
                for (int l = 0; l < 6; l++)
                {
                    RenderY -= 32;
                    g.drawImage(Resources.get("HangingPlatformChain"), RenderX, RenderY, null);
                }
                break;
            case 74:
                g.drawImage(Resources.get("TiltingRock"), RenderX, RenderY - 8, null);
                break;
            case 75:
                g.drawImage(Resources.get("SeeSaw"), RenderX, RenderY, null);
                break;
            case 76:


                RenderX2 = RenderX + 16 * ((Data[5] & 0xF)) - 10;
                g.drawImage(Resources.get("ScalePlatformBolt"), RenderX - 2, RenderY - 10, null);
                g.drawImage(Resources.get("ScalePlatformBolt"), RenderX2, RenderY - 10, null);
                Color rope = new Color(49, 24, 74);
                g.setColor(Color.WHITE);
                g.drawLine(RenderX + 8, RenderY - 9, RenderX2 + 2, RenderY - 9);
                g.setColor(rope);
                g.drawLine(RenderX + 9, RenderY - 8, RenderX2 + 1, RenderY - 8);
                RenderY2 = RenderY + 16 * ((Data[4] & 0xF)) - 8;
                int RenderY3 = RenderY + 16 * (((Data[5] >> 4) & 0xF)) - 8;
                g.setColor(Color.WHITE);
                g.drawLine(RenderX - 1, RenderY, RenderX - 1, RenderY2 - 1);
                g.setColor(rope);
                g.drawLine(RenderX, RenderY + 1, RenderX, RenderY2 - 1);
                g.setColor(Color.WHITE);
                g.drawLine(RenderX2 + 9, RenderY + 3, RenderX2 + 9, RenderY3 - 1);
                g.setColor(rope);
                g.drawLine(RenderX2 + 10, RenderY + 1, RenderX2 + 10, RenderY3 - 1);
                g.drawImage(Resources.get("ScalePlatformEnd"), RenderX2 - 14, RenderY3, null);
                g.drawImage(Resources.get("ScalePlatformEnd"), RenderX - 24, RenderY2, null);
                break;
            case 77:
            case 78:
                int w = (Data[5] & 0xF),
                 s = 2;
                if (this.Type == 78)
                {
                    w = Math.max(0, w - 1);
                    s = 1;
                    RenderX -= 8;
                }
                RenderX -= (w * s + 1) * 8;
                g.drawImage(Resources.get("MovingPlatformLeft"), RenderX, RenderY, null);
                for (int l = 0; l < w * s; l++)
                {
                    RenderX += 16;
                    g.drawImage(Resources.get("MovingPlatformSection"), RenderX, RenderY, null);
                }
                g.drawImage(Resources.get("MovingPlatformRight"), RenderX + 16, RenderY, null);
                break;
            case 79:
                g.drawImage(Resources.get("Spinning3PointedPlatform"), RenderX - 28, RenderY - 34, null);
                break;
            case 80:
                img = Resources.get("arrow_down");
                if ((Data[3] & 0xF) == 1)
                    RenderY -= 16;
                else
                {
                    RenderY += 16;
                    img = Util.rotateFlip(img, RotateFlipType.Rotate180FlipNone);
                }
                g.drawImage(Resources.get("MovingPlatformLeft"), RenderX, RenderY, null);
                g.drawImage(Resources.get("MovingPlatformSection"), RenderX + 16, RenderY, null);
                g.drawImage(Resources.get("MovingPlatformSection"), RenderX + 32, RenderY, null);
                g.drawImage(Resources.get("MovingPlatformRight"), RenderX + 48, RenderY, null);
                g.drawImage(img, RenderX + 24, RenderY2, 16, 16, null);
                break;
            case 82:
                g.drawImage(Resources.get("SpinningRectanglePlatform"), RenderX - 24, RenderY - 8, null);
                break;
            case 86:
                g.drawImage(Resources.get("SpinningTrianglePlatform"), RenderX - 14, RenderY - 6, null);
                break;
            case 88:
                Util.drawImage(g, Level.GFX.Tilesets[0].Map16Buffer, RenderX, RenderY, new Rectangle(0, 0x50, 16, 16));
                g.drawImage(Resources.get("PSwitch"), RenderX + 2, RenderY + 1, 12, 14, null);
                break;
            case 89:
                g.drawImage(Resources.get("Snailicorn"), RenderX, RenderY - 16, null);
                break;
            case 90:
                if (Data[5] == 1)
                    g.drawImage(Resources.get("WigglerL"), RenderX, RenderY - 44, null);
                else
                    g.drawImage(Resources.get("Wiggler"), RenderX, RenderY - 14, null);
                break;
            case 91:
                g.drawImage(Resources.get("MovingPlatformLeft"), RenderX, RenderY + 8, null);
                g.drawImage(Resources.get("MovingPlatformSection"), RenderX + 16, RenderY + 8, null);
                g.drawImage(Resources.get("MovingPlatformSection"), RenderX + 32, RenderY + 8, null);
                g.drawImage(Resources.get("MovingPlatformRight"), RenderX + 48, RenderY + 8, null);
                break;
            case 92:
                if ((Data[5] & 0xF) == 1)
                    g.drawImage(Resources.get("EelNonMoving"), RenderX + 3, RenderY + 7, null);
                else if (((Data[5] >> 4) & 0xF) == 1)
                {
                    img = Resources.get("Eel");
                    img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                    g.drawImage(img, RenderX + 45, RenderY + 3, null);
                } else if ((Data[3] & 0xF) == 1)
                    g.drawImage(Resources.get("Eel"), RenderX + 12, RenderY + 3, null);
                else
                {
                    Rectangle r = new Rectangle(RenderX + 12, RenderY + 3, 104, 28);
                    g.setColor(Color.BLACK);
                    g.fillRect(r.x, r.y, r.width, r.height);
                    g.drawString(LanguageManager.Get("Sprites", "Eel"), r.x, r.y);
                }
                break;
            case 93:
                if (((Data[5] >> 4) & 0xF) % 2 == 1)
                {
                    img = Resources.get("ArrowSignRotate45");
                    if ((Data[5] & 0xF) == 1)
                        img = Resources.get("ArrowSignRotate45F");
                } else
                {
                    img = Resources.get("ArrowSign");
                    if ((Data[5] & 0xF) == 1)
                        img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                }
                int rot = -Data[5] / 0x20 * 90;
                if (rot == 90)
                    img = Util.rotateFlip(img, RotateFlipType.Rotate90FlipNone);
                else if (rot == 120)
                    img = Util.rotateFlip(img, RotateFlipType.Rotate180FlipNone);
                else if (rot == 270)
                    img = Util.rotateFlip(img, RotateFlipType.Rotate270FlipNone);
                if (((Data[5] >> 4) & 0xF) % 2 == 1)
                {
                    RenderX += (img.getWidth() / 9) * ((((Data[5] >> 4) & 0xF) > 4) ? 1 : -1);
                    RenderY += (img.getWidth() / 9) * ((((Data[5] >> 4) & 0xF) == 3 || ((Data[5] >> 4) & 0xF) == 5) ? 1 : -1);
                }
                if (((Data[4] >> 4) & 0xF) == 1)
                    RenderX += 8;
                if ((Data[4] & 0xF) == 1)
                    RenderY += 8;
                if ((Data[3] & 0xF) == 1)
                    g.drawImage(img, RenderX - (img.getWidth() - 16) / 2, RenderY - (img.getHeight() - 16), null);
                else
                    g.drawImage(img, RenderX - (24 - 16) / 2, RenderY - (24 - 16), 24, 24, null);
                break;
            case 94:
                if ((Data[5] & 0xF) == 1)
                    g.drawImage(Resources.get("SwooperLarge"), RenderX - 6, RenderY - 16, null);
                else
                    g.drawImage(Resources.get("Swooper"), RenderX, RenderY, null);
                break;
            case 95:
                g.drawImage(Resources.get("SpinBoard"), RenderX - 6, RenderY, null);
                break;
            case 96:
                g.drawImage(Resources.get("SeaWeed"), RenderX - 32, RenderY - 127, null);
                break;
            case 99:
                g.drawImage(Resources.get("Spinning4PointedPlatform"), RenderX - 38, RenderY - 29, null);
                break;
            case 102:
                g.drawImage(Resources.get("SpikeBallSmall"), RenderX, RenderY, null);
                break;
            case 103:
                switch ((Data[2] & 0xF))
                {
                    case 1:
                        img = Resources.get("Dorrie");
                        img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                        g.drawImage(img, RenderX - 51, RenderY - 59, null);
                        break;
                    case 2:
                        g.drawImage(Resources.get("DorrieAway"), RenderX - 40, RenderY - 59, null);
                        break;
                    case 3:
                        g.drawImage(Resources.get("DorrieTowards"), RenderX - 40, RenderY - 59, null);
                        break;
                    default:
                        g.drawImage(Resources.get("Dorrie"), RenderX - 51, RenderY - 59, null);
                        break;
                }
                break;
            case 104:
                g.drawImage(Resources.get("Tornado"), RenderX - 88, RenderY - 130, null);
                break;
            case 105:
                g.drawImage(Resources.get("WhirlPool"), RenderX - 78, RenderY - 164, null);
                break;
            case 106:
                g.drawImage(Resources.get("RedCoin"), RenderX, RenderY, null);
                break;
            case 107:
                img = Resources.get("QSwitch");
                if ((Data[5] & 0xF) == 1)
                    img = Util.rotateFlip(img, RotateFlipType.Rotate180FlipNone);
                else
                    RenderY -= 2;
                if (((Data[5] >> 4) & 0xF) == 1)
                    RenderX += 8;
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 108:
                img = Resources.get("RedSwitch");
                if ((Data[5] & 0xF) == 1)
                    img = Util.rotateFlip(img, RotateFlipType.Rotate180FlipNone);
                else
                    RenderY -= 2;
                if (((Data[5] >> 4) & 0xF) == 1)
                    RenderX += 8;
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 109:
                int shift = ((Data[5] >> 4) & 0xF);
                if (shift == 1 || shift == 3)
                    RenderX += 8;
                if (shift == 2 || shift == 3)
                    RenderY += 8;
                g.drawImage(Resources.get("ElectricBall"), RenderX - 6, RenderY - 6, null);
                break;
            case 110:
                Util.drawImage(g, Level.GFX.Tilesets[0].Map16Buffer, RenderX, RenderY, new Rectangle(0, 0x50, 16, 16));
                g.drawImage(Resources.get("RedSwitch"), RenderX + 2, RenderY + 1, 12, 14, null);
                break;
            case 111:
                g.drawImage(Resources.get("Log"), RenderX - 128, RenderY - 32, null);
                break;
            case 113:
                g.drawImage(Resources.get("CheepChomp"), RenderX, RenderY, null);
                break;
            case 115:
                g.drawImage(Resources.get("SpikeBallLarge"), RenderX, RenderY - 42, null);
                break;
            case 114:
            case 118:
                if (this.Type == 114)
                    img = Resources.get("SmallFlamethrower");
                else
                    img = Resources.get("Flamethrower");
                switch (Data[5] % 0x40)
                {
                    case 0:
                        g.drawImage(img, RenderX, RenderY, null);
                        break;
                    case 1:
                        img = Util.rotateFlip(img, RotateFlipType.Rotate180FlipNone);
                        g.drawImage(img, RenderX - (img.getWidth() - 16), RenderY, null);
                        break;
                    case 2:
                        img = Util.rotateFlip(img, RotateFlipType.Rotate270FlipNone);
                        g.drawImage(img, RenderX, RenderY - (img.getHeight() - 16), null);
                        break;
                    case 3:
                        img = Util.rotateFlip(img, RotateFlipType.Rotate90FlipNone);
                        g.drawImage(img, RenderX, RenderY, null);
                        break;
                }
                break;
            case 116:
                g.drawImage(Resources.get("WaterBug"), RenderX - 16, RenderY - 5, null);
                break;
            case 117:
                g.drawImage(Resources.get("FlyingBlock"), RenderX - 12, RenderY - 8, null);
                break;
            case 119:
                int sz = ((Data[2] >> 4) & 0xF);
                if (sz == 0)
                    sz = 1;
                g.drawImage(Resources.get("Pendulum"), RenderX - sz * 32, RenderY - sz * 6, sz * 64, sz * 68, null);
                break;
            case 120:
                g.drawImage(Resources.get("PiranhaplantGround"), RenderX, RenderY + 3, null);
                break;
            case 122:
                g.drawImage(Resources.get("GiantPiranhaplant"), RenderX - 37, RenderY - 48, null);
                break;
            case 123:
                g.drawImage(Resources.get("FirePiranhaplant"), RenderX - 8, RenderY + 2, null);
                break;
            case 124:
                g.drawImage(Resources.get("GiantFirePiranhaplant"), RenderX - 38, RenderY - 45, null);
                break;
            case 126:
                BufferedImage img2 = null;
                RenderX2 = RenderX - 16;
                for (int l = 0; l < ((Data[2] >> 4) & 0xF); l++)
                {
                    if (l == 0)
                    {
                        img = Resources.get("DrawBridgeEnd");
                        img2 = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                    } else if (l == ((Data[2] >> 4) & 0xF) - 1)
                    {
                        img = Resources.get("DrawBridgeHinge");
                        img2 = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                    } else if (l == 1)
                    {
                        img = Resources.get("DrawBridgeSection");
                        img2 = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                    }
                    g.drawImage(img2, RenderX, RenderY, null);
                    g.drawImage(img, RenderX2, RenderY, null);
                    RenderX += 16;
                    RenderX2 -= 16;
                }
                if (((Data[2] >> 4) & 0xF) == 0)
                    g.drawImage(Resources.get("DrawBridgeHinge"), RenderX, RenderY, null);
                break;
            case 127:
                g.drawImage(Resources.get("GiantSpinningPlatform"), RenderX - 144, RenderY - 104, null);
                break;
            case 128:
                g.drawImage(Resources.get("WarpCannon"), RenderX - 9, RenderY - 26, null);
                int world = ((Data[2] >> 4) & 0xF);
                if (world >= 5 && world <= 8)
                    world -= 5;
                else
                    world = 0;
                g.drawImage(Resources.get("WarpWorlds"), RenderX + 26, RenderY + 23, RenderX + 26 + 18, RenderY + 23 + 19, world * 18, 0, world * 18 + 18, 19, null);
                break;
            case 130:
                g.drawImage(Resources.get("CheepCheep"), RenderX, RenderY, null);
                break;
            case 131:
            case 132:
                g.drawImage(Resources.get("MidpointFlag"), RenderX, RenderY, null);
                break;
            case 136:
                for (int l = 0; l <= Data[5]; l++)
                {
                    g.drawImage(Resources.get("PokeySection"), RenderX, RenderY, null);
                    RenderY -= 16;
                }
                g.drawImage(Resources.get("PokeyHead"), RenderX, RenderY - 9, null);
                break;
            case 141:
                g.drawImage(Resources.get("SwellingGround"), RenderX - 128, RenderY, null);
                if ((Data[2] & 0xF) == 0)
                    g.drawImage(Resources.get("SwellingGroundOut"), RenderX - 128, RenderY - 112, null);
                else if ((Data[2] & 0xF) == 1)
                    g.drawImage(Resources.get("SwellingGroundIn"), RenderX - 128, RenderY, null);
                break;
            case 142:
                RenderX -= 8;
                RenderX2 = RenderX + (((Data[2] >> 4) & 0xF)) * 16;
                RenderY2 = (Data[2] & 0xF);
                if (RenderY2 >= 8)
                    RenderY2 -= 16;
                RenderY2 = RenderY - RenderY2 * 16;
                g.setColor(Color.WHITE);
                g.drawLine(RenderX + 8, RenderY + 8, RenderX2 + 8, RenderY2 + 8);
                g.drawImage(Resources.get("TightRopeEnd"), RenderX, RenderY, null);
                g.drawImage(Resources.get("TightRopeEnd"), RenderX2, RenderY2, null);
                break;
            case 144:
                g.drawImage(Resources.get("SpikedBlock"), RenderX - 2, RenderY - 7, null);
                RenderY2 = (Data[5] & 0xF);
                if (RenderY2 == 2)
                    RenderY2 = 3;
                if (RenderY2 <= 3)
                    g.drawImage(Resources.get("FlyingQBlockOverrides"), RenderX, RenderY + 1, RenderX + 16, RenderY + 1 + 16, RenderY2 * 16, 0, RenderY2 * 16 + 16, 16, null);
                break;
            case 146:
                g.drawImage(Resources.get("StarGate"), RenderX, RenderY, null);
                break;
            case 147:
                RenderX -= Data[5] * 8 + 6;
                g.drawImage(Resources.get("BumpPlatformLeft"), RenderX, RenderY, null);
                RenderX += 14;
                for (int l = 0; l < Data[5] - 1; l++)
                {
                    g.drawImage(Resources.get("BumpPlatformSection"), RenderX, RenderY, null);
                    RenderX += 16;
                }
                if (Data[5] > 0)
                    g.drawImage(Resources.get("BumpPlatformRight"), RenderX, RenderY, null);
                break;
            case 148:
                g.drawImage(Resources.get("Goomba"), RenderX, RenderY - 2, null);
                break;
            case 149:
                boolean inShell = ((Data[5] >> 4) & 0xF) == 1;
                switch ((Data[5] & 0xF))
                {
                    case 1:
                        img = inShell ? Resources.get("RedKoopaShell") : Resources.get("KoopaRed");
                        break;
                    case 2:
                    case 3:
                        img = inShell ? Resources.get("BlueKoopaShell") : Resources.get("KoopaBlue");
                        break;
                    default:
                        img = inShell ? Resources.get("GreenKoopaShell") : Resources.get("KoopaGreen");
                        break;
                }
                if (inShell)
                    g.drawImage(img, RenderX, RenderY, null);
                else
                    g.drawImage(img, RenderX, RenderY - 13, null);
                break;
            case 150:
                switch ((Data[5] & 0xF))
                {
                    case 1:
                        img = Resources.get("ParakoopaRed");
                        break;
                    case 2:
                        img = Resources.get("ParakoopaBlue");
                        break;
                    default:
                        img = Resources.get("ParakoopaGreen");
                        break;
                }
                g.drawImage(img, RenderX, RenderY - 16, null);
                break;
            case 152:
                g.drawImage(Resources.get("SwitchBlock"), RenderX, RenderY, null);
                break;
            case 155:
                Rectangle rect = this.getRect();
                rect.grow(1, 1);
                g.setColor(Color.BLACK);
                g.drawRect(rect.x, rect.y, rect.width, rect.height);
                rect.grow(-1, -1);
                g.setColor(Color.GREEN);
                g.drawRect(rect.x, rect.y, rect.width, rect.height);
                g.setColor(Color.BLACK);
                g.drawString(LanguageManager.Get("Sprites", "Warp"), RenderX + 1, RenderY + 1);
                g.setColor(Color.GREEN);
                g.drawString(LanguageManager.Get("Sprites", "Warp"), RenderX, RenderY);
                break;
            case 157:
                g.drawImage(Resources.get("FireBro"), RenderX, RenderY - 15, null);
                break;
            case 158:
                g.drawImage(Resources.get("BoomerangBro"), RenderX, RenderY - 18, null);
                break;
            case 162:
                RenderY += 24;
                RenderX += 6;
                g.drawImage(Resources.get("MushroomStalkTop"), RenderX, RenderY, null);
                RenderY += 8;
                g.drawImage(Resources.get("MushroomStalk"), RenderX, RenderY, null);
                for (int l = 0; l <= (Data[3] & 0xF); l++)
                {
                    RenderY += 16;
                    g.drawImage(Resources.get("MushroomStalk"), RenderX, RenderY, null);
                }
                RenderX = X * 16 - 16 * (((Data[4] >> 4) & 0xF) + 1);
                RenderY = Y * 16;
                img = Resources.get("MushroomEdge");
                g.drawImage(img, RenderX, RenderY, null);
                RenderX += 32;
                for (int l = 0; l < ((Data[4] >> 4) & 0xF); l++)
                {
                    g.drawImage(Resources.get("MushroomSection"), RenderX, RenderY, null);
                    RenderX += 32;
                }
                img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 173:
                for (int l = 0; l < (Data[5] & 0xF) + 4; l++)
                {
                    g.drawImage(Resources.get("Rope"), RenderX - 3, RenderY, null);
                    RenderY += 16;
                }
                break;
            case 174:
                RenderY += 24;
                RenderX += 6;
                g.drawImage(Resources.get("MushroomStalkTop"), RenderX, RenderY, null);
                RenderY += 8;
                g.drawImage(Resources.get("MushroomStalk"), RenderX, RenderY, null);
                g.drawImage(Resources.get("MushroomStalk"), RenderX, RenderY + 16, null);
                RenderX = X * 16 - 16 * (((Data[4] >> 4) & 0xF) + 1);
                RenderY = RenderY2;
                img = Resources.get("MushroomEdge");
                g.drawImage(img, RenderX, RenderY, null);
                RenderX += 32;
                for (int l = 0; l < ((Data[4] >> 4) & 0xF); l++)
                {
                    g.drawImage(Resources.get("MushroomSection"), RenderX, RenderY, null);
                    RenderX += 32;
                }
                img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 175:
                g.drawImage(Resources.get("BouncyBricks"), RenderX - 40, RenderY - 48, null);
                break;
            case 180:
                if ((Data[5] & 0xF) == 1)
                    img = Resources.get("FenceKoopaRed");
                else
                    img = Resources.get("FenceKoopaGreen");
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 40:
            case 183:
                g.drawImage(Resources.get("Lakitu"), RenderX, RenderY - 23, null);
                break;
            case 186:
                g.drawImage(Resources.get("Paragoomba"), RenderX, RenderY - 12, null);
                break;
            case 187:
                g.drawImage(Resources.get("ManualPlatform"), RenderX, RenderY, null);
                if ((Data[5] & 0xF) == 0 || (Data[5] & 0xF) == 2)
                    g.drawImage(Resources.get("UpArrow"), RenderX + 51, RenderY + 4, null);
                else if ((Data[5] & 0xF) == 1)
                    g.drawImage(Resources.get("DownArrow"), RenderX + 51, RenderY + 4, null);
                break;
            case 189:
                g.drawImage(Resources.get("Pipe"), RenderX, RenderY - 48, null);
                break;
            case 191:
                RenderY2 = ((Data[3] >> 4) & 0xF);
                if (RenderY2 == 0)
                    RenderY2 = 8;
                g.drawImage(Resources.get("QBlock"), RenderX, RenderY + (RenderY2 - 5) * 16, null);
                if ((Data[5] & 0xF) <= 8)
                    g.drawImage(Resources.get("HangingBlockOverrides"), RenderX, RenderY + (RenderY2 - 5) * 16, RenderX + 16, RenderY + (RenderY2 - 5) * 16 + 16,
                            ((Data[5] & 0xF)) * 16, 0, ((Data[5] & 0xF)) * 16 + 16, 16, null);
                g.setColor(Color.WHITE);
                g.drawLine(RenderX + 7, RenderY + (RenderY2 - 5) * 16, RenderX + 7, RenderY - 80);
                g.setColor(Color.WHITE);
                g.drawLine(RenderX + 8, RenderY + (RenderY2 - 5) * 16, RenderX + 8, RenderY - 80);
                break;
            case 193:
                g.drawImage(Resources.get("DryBonesLarge"), RenderX, RenderY - 28, null);
                break;
            case 194:
                g.drawImage(Resources.get("GiantThwomp"), RenderX, RenderY, null);
                break;
            case 197:
                RenderY2 = ((Data[4] >> 4) & 0xF); //Tile number
                RenderY2 = RenderY2 < TileCreateNums.length ? TileCreateNums[RenderY2] : 0x67; //Load actual number from hardcoded list
                width = (Data[3] & 0xF); //Checkerboard pattern
                height = (Data[4] & 0xF); //Create or destroy
                for (int l = 1; l <= Math.max(1, (Data[5] & 0xF)); l++)
                {
                    for (int m = 1; m <= Math.max(1, ((Data[5] >> 4) & 0xF)); m++)
                    {
                        if (width == 0 || width == 1 && l % 2 == m % 2 || width == 2 && l % 2 != m % 2) //Checkerboard pattern
                            if (height == 0)
                                g.drawImage(Resources.get("DestroyTile"), RenderX, RenderY, null);
                            else if (RenderY2 < 0) //Negative number indicates an override tile
                                g.drawImage(Resources.get("tileoverrides"), RenderX, RenderY, RenderX + 16, RenderY + 16, -RenderY2, 0, -RenderY2 + 16, 16, null);
                            else
                                g.drawImage(Level.GFX.Tilesets[0].Map16Buffer, RenderX, RenderY, RenderX + 16, RenderY + 16,
                                        RenderY2 % 0x10 * 0x10, RenderY2 & 0xF0,
                                        RenderY2 % 0x10 * 0x10 + 16, RenderY2 & 0xF0 + 16, null);
                        RenderX += 16;
                    }
                    RenderY += 16;
                    RenderX = RenderX2;
                }
                if (height == 0)
                {
                    Rectangle r = this.getRect();
                    g.setColor(Color.BLACK);
                    g.drawRect(r.x, r.y, r.width, r.height);
                }
                break;
            case 204:
                g.drawImage(Resources.get("JumpingFlame"), RenderX, RenderY - 7, null);
                break;
            case 205:
                g.drawImage(Resources.get("FlameChomp"), RenderX, RenderY - 27, null);
                break;
            case 206:
                g.drawImage(Resources.get("GhostGoo"), RenderX, RenderY - 32, null);
                break;
            case 207:
                if ((Data[5] & 0xF) == 1)
                    img = Resources.get("CheepCheepGiantGreen");
                else
                    img = Resources.get("CheepCheepGiant");
                g.drawImage(img, RenderX - 20, RenderY - 32, null);
                break;
            case 209:
                g.drawImage(Resources.get("GiantHammerBro"), RenderX, RenderY - 15, null);
                break;
            case 210:
                g.drawImage(Resources.get("VSBattleStar"), RenderX, RenderY, null);
                break;
            case 211:
                g.drawImage(Resources.get("Blooper"), RenderX, RenderY - 16, null);
                break;
            case 212:
                g.drawImage(Resources.get("BlooperNanny"), RenderX, RenderY - 16, null);
                break;
            case 213:
                g.drawImage(Resources.get("BlooperWithMini"), RenderX - 8, RenderY - 32, null);
                break;
            case 219:
                switch (((Data[5] >> 4) & 0xF))
                {
                    case 1:
                        g.drawImage(Resources.get("SpinyBeetleLeft"), RenderX - 4, RenderY, null);
                        break;
                    case 2:
                        g.drawImage(Resources.get("SpinyBeetleDown"), RenderX, RenderY, null);
                        break;
                    case 3:
                        g.drawImage(Resources.get("SpinyBeetleRight"), RenderX, RenderY, null);
                        break;
                    default:
                        g.drawImage(Resources.get("SpinyBeetleUp"), RenderX, RenderY - 4, null);
                        break;
                }
                break;
            case 220:
                g.drawImage(Resources.get("BowserJr"), RenderX, RenderY - 25, null);
                break;
            case 222:
                if (((Data[5] >> 4) & 0xF) == 1)
                    RenderX += 8;
                if ((Data[5] & 0xF) == 1)
                    RenderY += 8;
                g.drawImage(Resources.get("MiniGoomba"), RenderX, RenderY, null);
                break;
            case 223:
                g.drawImage(Resources.get("FlipGateSmall"), RenderX, RenderY, null);
                break;
            case 224:
                g.drawImage(Resources.get("FlipGateLarge"), RenderX, RenderY, null);
                break;
            case 226:
                RenderY += (Data[5] & 0xF0) + 64;
                g.setColor(Color.WHITE);
                g.drawLine(RenderX + 7, RenderY2, RenderX + 7, RenderY);
                g.setColor(Color.WHITE);
                g.drawLine(RenderX + 8, RenderY2, RenderX + 8, RenderY);
                g.drawImage(Resources.get("HangingScuttleBug"), RenderX - 14, RenderY, null);
                break;
            case 227:
                g.drawImage(Resources.get("MoneyBag"), RenderX, RenderY - 5, null);
                break;
            case 228:
                g.drawImage(Resources.get("RouletteBlock"), RenderX, RenderY, null);
                break;
            case 232:
                g.drawImage(Resources.get("QBlock"), RenderX, RenderY, null);
                if ((Data[5] & 0xF) < 10)
                    g.drawImage(Resources.get("HangingBlockOverrides"), RenderX, RenderY, RenderX + 16, RenderY + 16, ((Data[5] & 0xF)) * 16, 0, ((Data[5] & 0xF)) * 16 + 16, 16, null);
                g.setColor(Color.WHITE);
                g.drawLine(RenderX + 7, RenderY - 128, RenderX + 7, RenderY);
                g.setColor(Color.WHITE);
                g.drawLine(RenderX + 8, RenderY - 128, RenderX + 8, RenderY);
                break;
            case 233:
                for (int l = 0; l < (Data[5] & 0xF) + 4; l++)
                {
                    g.drawImage(Resources.get("SwingingPole"), RenderX - 3, RenderY, null);
                    RenderY += 16;
                }
                g.drawImage(Resources.get("LineAttachment"), RenderX2 - 11, RenderY2 - 11, null);
                break;
            case 235:
                if (((Data[5] >> 4) & 0xF) == 1)
                    RenderX += 8;
                if ((Data[5] & 0xF) == 1)
                    RenderY += 8;
                g.drawImage(Resources.get("StarCoin"), RenderX, RenderY, null);
                break;
            case 236:
                if (((Data[2] >> 4) & 0xF) == 1)
                    RenderY += 8;
                g.drawImage(Resources.get("SpinningSquarePlatform"), RenderX - 16, RenderY - 16, null);
                break;
            case 237:
                g.drawImage(Resources.get("Broozer"), RenderX, RenderY - 15, null);
                break;
            case 238:
                g.drawImage(Resources.get("MushroomStalkTop"), RenderX + 6, RenderY + 24, null);
                RenderY += 32;
                for (int l = 0; l < 5; l++)
                {
                    g.drawImage(Resources.get("MushroomStalk"), RenderX + 6, RenderY, null);
                    RenderY += 16;
                }
                RenderX = X * 16 - 16 * (((Data[5] >> 4) & 0xF) + 1);
                RenderY = Y * 16;
                img = Resources.get("PurpleMushroomEdge");
                g.drawImage(img, RenderX, RenderY, null);
                RenderX += 32;
                for (int l = 0; l < ((Data[5] >> 4) & 0xF); l++)
                {
                    g.drawImage(Resources.get("PurpleMushroomSection"), RenderX, RenderY, null);
                    RenderX += 32;
                }
                img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 239:
                g.drawImage(Resources.get("RiseLowerMushroomStalkTop"), RenderX, RenderY + 24, null);
                RenderY += 32;
                for (int l = 0; l <= Math.max(((Data[5] >> 4) & 0xF), (Data[4] & 0xF)); l++)
                {
                    g.drawImage(Resources.get("RiseLowerMushroomStalk"), RenderX, RenderY, null);
                    RenderY += 16;
                }
                RenderX -= ((Data[5] & 0xF) + 1) * 8;
                if (((Data[5] >> 4) & 0xF) > (Data[4] & 0xF))
                {
                    img = Resources.get("LoweringMushroomEdge");
                    img2 = Resources.get("LoweringMushroomMiddle");
                } else
                {
                    img = Resources.get("RisingMushroomEdge");
                    img2 = Resources.get("RisingMushroomMiddle");
                }
                g.drawImage(img, RenderX, RenderY2, null);
                for (int l = 0; l < (Data[5] & 0xF); l++)
                {
                    RenderX += 16;
                    g.drawImage(img2, RenderX, RenderY2, null);
                }
                img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                g.drawImage(img, RenderX + 16, RenderY2, null);
                break;
            case 241:
                img = Resources.get("RotatingCannon");
                img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                for (int l = 0; l <= Math.min(7, (int) Data[3]); l++)
                {
                    if ((Data[4] & (1 << l)) > 0)
                        g.drawImage(Resources.get("RotatingCannonEmpty"), RenderX + 2, RenderY, null);
                    else if ((Data[5] & (1 << l)) > 0)
                        g.drawImage(img, RenderX - 3, RenderY, null);
                    else
                        g.drawImage(Resources.get("RotatingCannon"), RenderX + 1, RenderY, null);
                    RenderY -= 16;
                }
                break;
            case 242:
                RenderX2 = (Data[5] & 0xF);
                if (((Data[5] >> 4) & 0xF) == 1)
                {
                    if ((Data[4] & 0xF) == 1)
                        g.drawImage(Resources.get("ExpandedMushroomS"), RenderX - 32, RenderY, null);
                    else
                        g.drawImage(Resources.get("ExpandedMushroomL"), RenderX - 72, RenderY, null);
                    RenderX2++;
                    RenderY2 -= 16;
                } else
                    g.drawImage(Resources.get("ContractedMushroom"), RenderX - 8, RenderY, null);
                RenderY2 += 32;
                for (int l = 0; l < RenderX2; l++)
                {
                    if (l == 0)
                        g.drawImage(Resources.get("ExpandMushroomStalkTop"), RenderX, RenderY2, null);
                    else
                        g.drawImage(Resources.get("ExpandMushroomStalk"), RenderX, RenderY2, null);
                    RenderY2 += 16;
                }
                break;
            case 243:
                g.drawImage(Resources.get("RoofSpiny"), RenderX, RenderY, null);
                break;
            case 244:
                switch (((Data[5] >> 4) & 0xF))
                {
                    case 1:
                        RenderX -= 8;
                        break;
                    case 2:
                        RenderX += 8;
                        break;
                }
                g.drawImage(Resources.get("BouncingMushroom"), RenderX - 40, RenderY, null);
                RenderY += 32;
                RenderX += 3;
                for (int l = 0; l < (Data[5] & 0xF); l++)
                {
                    g.drawImage(Resources.get("BouncingMushroomStalk"), RenderX, RenderY, null);
                    RenderY += 16;
                }
                break;
            case 246:
                g.drawImage(Resources.get("Barrel"), RenderX - 16, RenderY - 8, null);
                break;
            case 247:
                g.drawImage(Resources.get("Shark"), RenderX - 27, RenderY - 8, null);
                g.drawImage(Resources.get("Infinity"), RenderX - 4, RenderY + 1, null);
                break;
            case 248:
                g.drawImage(Resources.get("BalloonBoo"), RenderX - 53, RenderY - 106, null);
                break;
            case 249:
                RenderX -= (Data[5] & 0xF) * 8 + 24;
                RenderY += ((Data[4] >> 4) & 0xF) * 16 + 88;
                int XOff = (Data[5] & 0xF) * 16 + 48;
                for (int l = 0; l < ((Data[4] >> 4) & 0xF) + 4; l++)
                {
                    RenderY -= 16;
                    g.drawImage(Resources.get("WallJumpPlatformBlock"), RenderX, RenderY, null);
                    g.drawImage(Resources.get("WallJumpPlatformBlock"), RenderX + XOff, RenderY, null);
                }
                for (int l = 0; l < (Data[5] & 0xF) + 2; l++)
                {
                    RenderX += 16;
                    g.drawImage(Resources.get("WallJumpPlatformBlock"), RenderX, RenderY, null);
                }
                g.drawImage(Resources.get("LineAttachment"), X * 16 - 3, Y * 16 + 5, null);
                break;
            case 250:
                g.drawImage(Resources.get("Crow"), RenderX, RenderY, null);
                break;
            case 252:
                g.drawImage(Resources.get("BanzaiBillCannon"), RenderX - 32, RenderY - 64, null);
                break;
            case 254:
                g.drawImage(Resources.get("Kabomb"), RenderX, RenderY - 3, null);
                break;
            case 255:
                g.drawImage(Resources.get("Jungle"), RenderX, RenderY, null);
                break;
            case 256:
                img = Resources.get("ThruWallPlatform");
                if (((Data[3] >> 4) & 0xF) == 0)
                    g.drawImage(img, RenderX - 80, RenderY, null);
                else
                {
                    img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                    g.drawImage(img, RenderX, RenderY, null);
                }
                break;
            case 265:
                g.drawImage(Resources.get("Hand"), RenderX + 8, RenderY, null);
                break;
            case 268:
                g.drawImage(Resources.get("UnderwaterBounceBall"), RenderX - 26, RenderY - 26, null);
                break;
            case 272:
                if ((Data[5] & 0xF) == 1)
                    g.drawImage(Resources.get("SnowBranchLeft"), RenderX - 31, RenderY - 12, null);
                else
                    g.drawImage(Resources.get("SnowBranchRight"), RenderX + 15, RenderY - 12, null);
                break;
            case 273:
                g.drawImage(Resources.get("SnowballThrower"), RenderX, RenderY, null);
                break;
            case 274:
                img = Resources.get("SinkingSnowEdge");
                g.drawImage(img, RenderX - 16, RenderY, null);
                for (int l = 0; l <= (Data[5] & 0xF); l++)
                {
                    g.drawImage(Resources.get("SinkingSnow"), RenderX, RenderY, null);
                    RenderX += 16;
                }
                img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 275:
                RenderY -= 14;
                g.drawImage(Resources.get("JumpingQBlock"), RenderX, RenderY, null);
                if (((Data[4] & 0xF)) <= 3)
                    g.drawImage(Resources.get("JumpingQBlockOverrides"), RenderX, RenderY + 3, RenderX + 16, RenderY + 3 + 16, ((Data[4] & 0xF)) * 16, 0, ((Data[4] & 0xF)) * 16 + 16, 16, null);
                for (int l = 0; l < (Data[5] & 0xF); l++)
                {
                    RenderY -= 16;
                    g.drawImage(Resources.get("Brick"), RenderX, RenderY, null);
                }
                break;
            case 277:
                int direction = Data[2] % 8;
                if (direction == 0 || direction == 2 || direction == 4 || direction == 6)
                    img = Resources.get("Arrow");
                else
                    img = Resources.get("ArrowRotate45");
                if (direction == 2 || direction == 3)
                    img = Util.rotateFlip(img, RotateFlipType.Rotate90FlipNone);
                if (direction == 4 || direction == 5)
                    img = Util.rotateFlip(img, RotateFlipType.Rotate180FlipNone);
                if (direction == 6 || direction == 7)
                    img = Util.rotateFlip(img, RotateFlipType.Rotate270FlipNone);
                g.drawImage(img, RenderX, RenderY, 32, 32, null);
                break;
            case 278:
                int sx = ((Data[5] & 0xF) + 1) * 16;
                int sy = (((Data[5] >> 4) & 0xF) + 1) * 16;
                img = Resources.get("GroundpoundGoo");
                g.drawImage(img, RenderX, RenderY, sx, sy, null);
                break;
            case 279:
                img = Resources.get("OneWayDoor");
                if (Data[5] == 0 || Data[5] == 1)
                {
                    img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipY);
                    RenderY -= 39;
                } else if (Data[5] == 2 || Data[5] == 3)
                    RenderY += 16;
                else if (Data[5] == 4 || Data[5] == 5)
                {
                    img = Util.rotateFlip(img, RotateFlipType.Rotate270FlipNone);
                    RenderX += 16;
                } else if (Data[5] == 6 || Data[5] == 7)
                {
                    img = Util.rotateFlip(img, RotateFlipType.Rotate270FlipX);
                    RenderX -= 39;
                }
                if (Data[5] == 0 || Data[5] == 2)
                    img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                else if (Data[5] == 4 || Data[5] == 6)
                    img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipY);
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 281:
                img = Resources.get("PipeCaterpiller");
                if ((Data[5] & 0xF) == 1)
                {
                    img = Util.rotateFlip(img, RotateFlipType.Rotate180FlipNone);
                    g.drawImage(img, RenderX + 10, RenderY + 16, null);
                } else
                    g.drawImage(img, RenderX + 10, RenderY - 15, null);
                break;
            case 282:
                RenderX -= 16;
                g.drawImage(Resources.get("VineTop"), RenderX, RenderY, null);
                for (int l = 0; l < (Data[5] & 0xF) + 2; l++)
                {
                    RenderY += 16;
                    g.drawImage(Resources.get("Vine"), RenderX, RenderY, null);
                }
                g.drawImage(Resources.get("VineBottom"), RenderX, RenderY + 16, null);
                break;
            case 283:
                g.drawImage(Resources.get("SpikeBass"), RenderX, RenderY - 32, null);
                break;
            case 284:
                g.drawImage(Resources.get("Pumpkin"), RenderX, RenderY - 2, null);
                break;
            case 285:
                g.drawImage(Resources.get("ScuttleBug"), RenderX, RenderY - 8, null);
                break;
            case 289:
                g.drawImage(Resources.get("ExpandableBlock"), RenderX, RenderY, null);
                break;
            case 290:
                g.drawImage(Resources.get("FlyingQBlock"), RenderX - 9, RenderY - 13, null);
                if ((Data[5] & 0xF) < 8)
                    g.drawImage(Resources.get("FlyingQBlockOverrides"), RenderX, RenderY, RenderX + 16, RenderY + 16, ((Data[5] & 0xF)) * 16, 0, ((Data[5] & 0xF)) * 16 + 16, 16, null);
                break;
            case 291:
                g.drawImage(Level.GFX.Tilesets[0].Map16Buffer, RenderX, RenderY, RenderX + 16, RenderY + 16, 0, 0x50, 16, 0x50 + 16, null);
                g.drawImage(Resources.get("QSwitch"), RenderX + 2, RenderY + 1, 12, 14, null);
                break;
            case 292:
                g.drawImage(Resources.get("Door2"), RenderX, RenderY - 32, null);
                break;
            case 71:
            case 72:
            case 298:
                width = (Data[5] & 0xF);
                int spikes = 0;
                if (this.Type == 298)
                {
                    height = ((Data[5] >> 4) & 0xF);
                    spikes = (Data[2] & 0xF);
                    if (spikes == 1 || spikes == 3)
                    {
                        height -= 1;
                        RenderY += 16;
                    }
                    if (spikes == 2 || spikes == 3)
                        height -= 1;
                    if (spikes == 4 || spikes == 6)
                        width -= 1;
                    if (spikes == 5 || spikes == 6)
                    {
                        width -= 1;
                        RenderX += 16;
                    }
                } else
                    height = 0;
                if (width < 1)
                    width = 1;
                if (height < 0)
                    height = 0;
                int xp = RenderX;
                int yp = RenderY;
                if (height == 0)
                {
                    g.drawImage(Resources.get("StoneBlockFlatHorizLeft"), RenderX, RenderY, null);
                    for (int l = 0; l < width - 1; l++)
                    {
                        RenderX += 16;
                        g.drawImage(Resources.get("StoneBlockFlatHorizMiddle"), RenderX, RenderY, null);
                    }
                    g.drawImage(Resources.get("StoneBlockFlatHorizRight"), RenderX + 16, RenderY, null);
                } else
                {
                    g.drawImage(Resources.get("StoneBlockTopLeft"), RenderX, RenderY, null);
                    for (int l = 0; l < width - 1; l++)
                    {
                        RenderX += 16;
                        g.drawImage(Resources.get("StoneBlockTop"), RenderX, RenderY, null);
                    }
                    RenderX += 16;
                    g.drawImage(Resources.get("StoneBlockTopRight"), RenderX, RenderY, null);
                    for (int l = 0; l < height - 1; l++)
                    {
                        RenderY += 16;
                        g.drawImage(Resources.get("StoneBlockRight"), RenderX, RenderY, null);
                    }
                    RenderY += 16;
                    g.drawImage(Resources.get("StoneBlockBottomRight"), RenderX, RenderY, null);
                    for (int l = 0; l < width - 1; l++)
                    {
                        RenderX -= 16;
                        g.drawImage(Resources.get("StoneBlockBottom"), RenderX, RenderY, null);
                    }
                    RenderX -= 16;
                    g.drawImage(Resources.get("StoneBlockBottomLeft"), RenderX, RenderY, null);
                    for (int l = 0; l < height - 1; l++)
                    {
                        RenderY -= 16;
                        g.drawImage(Resources.get("StoneBlockLeft"), RenderX, RenderY, null);
                    }
                    RenderX = xp;
                    RenderY = yp;
                    int xStart = RenderX;
                    for (int l = 0; l < height - 1; l++)
                    {
                        RenderY += 16;
                        for (int m = 0; m < width - 1; m++)
                        {
                            RenderX += 16;
                            g.drawImage(Resources.get("StoneBlockMiddle"), RenderX, RenderY, null);
                        }
                        RenderX = xStart;
                    }
                }
                if (spikes == 1 || spikes == 3)
                {
                    RenderX = xp;
                    RenderY = yp - 16;
                    for (int l = 0; l <= width; l++)
                    {
                        g.drawImage(Resources.get("StoneBlockSpikes"), RenderX, RenderY, null);
                        RenderX += 16;
                    }
                }
                if (spikes == 2 || spikes == 3)
                {
                    RenderX = xp;
                    RenderY = (Y + height + 1) * 16;
                    if (spikes == 3)
                        RenderY += 16;
                    BufferedImage spikes2 = Resources.get("StoneBlockSpikes");
                    spikes2 = Util.rotateFlip(spikes2, RotateFlipType.RotateNoneFlipY);
                    for (int l = 0; l <= (Data[5] & 0xF); l++)
                    {
                        g.drawImage(spikes2, RenderX, RenderY, null);
                        RenderX += 16;
                    }
                }
                if (spikes == 4 || spikes == 6)
                {
                    RenderX = (X + width + 1) * 16;
                    RenderY = yp;
                    if (spikes == 6)
                        RenderX += 16;
                    BufferedImage spikes2 = Resources.get("StoneBlockSpikes");
                    spikes2 = Util.rotateFlip(spikes2, RotateFlipType.Rotate90FlipNone);
                    for (int l = 0; l <= ((Data[5] >> 4) & 0xF); l++)
                    {
                        g.drawImage(spikes2, RenderX, RenderY, null);
                        RenderY += 16;
                    }
                }
                if (spikes == 5 || spikes == 6)
                {
                    RenderX = xp - 16;
                    RenderY = yp;
                    BufferedImage spikes2 = Resources.get("StoneBlockSpikes");
                    spikes2 = Util.rotateFlip(spikes2, RotateFlipType.Rotate90FlipX);
                    for (int l = 0; l <= height; l++)
                    {
                        g.drawImage(spikes2, RenderX, RenderY, null);
                        RenderY += 16;
                    }
                }
                break;
            case 300:
                RenderY += -8 + (Data[5] & 0xF);
                g.drawImage(Resources.get("GhostPlatform"), RenderX - 64, RenderY, null);
                break;
            case 303:
                g.drawImage(Resources.get("SpinningSpikeBall"), RenderX + 4, RenderY - 61, null);
                break;
            case 260:
            case 304:
                g.drawImage(Resources.get("GiantSpike"), RenderX, RenderY - 128, null);
                break;
            case 305:
                customRendered = false;
                RenderX2 = Math.max(0, ((Data[4] >> 4) + ((Data[4] & 0xF) << 4) - 15) * 16);
                if (RenderX2 != 0)
                {
                    g.setColor(Color.BLACK);
                    g.fillRect(RenderX, RenderY + 6, RenderX2, 4);
                    g.fillRect(RenderX + RenderX2 - 4, RenderY, 4, 10);
                    g.setColor(Color.WHITE);
                    g.fillRect(RenderX + 1, RenderY + 7, RenderX2 - 2, 2);
                    g.fillRect(RenderX + RenderX2 - 3, RenderY + 1, 2, 8);
                }
                renderDefaultImg(g, RenderX, RenderY);
                break;
            case 306:
                customRendered = false;
                RenderX2 = (Data[5] & 0xF0) - 16;
                if (RenderX2 != 0)
                {
                    g.setColor(Color.BLACK);
                    g.fillRect(RenderX + 6, RenderY - RenderX2, 4, RenderX2 + 16);
                    g.fillRect(RenderX, RenderY - RenderX2, 16, 4);
                    g.setColor(Color.WHITE);
                    g.fillRect(RenderX + 7, RenderY - RenderX2 + 1, 2, RenderX2 + 14);
                    g.fillRect(RenderX + 1, RenderY - RenderX2 + 1, 14, 2);
                }
                renderDefaultImg(g, RenderX, RenderY);
                break;
            case 261:
            case 307:
                img = Resources.get("GiantSpike");
                img = Util.rotateFlip(img, RotateFlipType.Rotate180FlipNone);
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 262:
            case 308:
                img = Resources.get("GiantSpike");
                img = Util.rotateFlip(img, RotateFlipType.Rotate90FlipNone);
                g.drawImage(img, RenderX, RenderY, null);
                break;
            case 263:
            case 309:
                img = Resources.get("GiantSpike");
                img = Util.rotateFlip(img, RotateFlipType.Rotate270FlipNone);
                g.drawImage(img, RenderX - 128, RenderY, null);
                break;
            case 312:
                img = Resources.get("GreenMushroomEdge");
                img = Util.rotateFlip(img, RotateFlipType.RotateNoneFlipX);
                width = (Data[5] & 0xF) + 1;
                RenderX -= width * 8;
                g.drawImage(Resources.get("GreenMushroomEdge"), RenderX, RenderY, null);
                for (int l = 0; l <= width - 2; l++)
                {
                    RenderX += 16;
                    g.drawImage(Resources.get("GreenMushroomMiddle"), RenderX, RenderY, null);
                }
                g.drawImage(img, RenderX + 16, RenderY, null);
                g.drawImage(Resources.get("GreenMushroomStalkTop"), RenderX2, RenderY + 24, null);
                for (int l = 0; l < ((Data[5] >> 4) & 0xF); l++)
                {
                    g.drawImage(Resources.get("GreenMushroomStalk"), RenderX2, RenderY + 49, null);
                    RenderY += 16;
                }
                break;
            case 323:
                g.drawImage(Resources.get("CloudLeftEdge"), RenderX, RenderY, null);
                RenderX += 16;
                for (int l = 0; l <= Data[5]; l++)
                {
                    g.drawImage(Resources.get("CloudSection"), RenderX, RenderY, null);
                    RenderX += 32;
                }
                g.drawImage(Resources.get("CloudRightEdge"), RenderX, RenderY, null);
                break;
            default:
                customRendered = false;
                renderDefaultImg(g, RenderX, RenderY);
                break;
        }

        if (!Level.ValidSprites[this.Type] && customRendered)
        {
            g.setColor(Color.RED);
            Rectangle r = this.getRect();
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        //I dunno what's this user for. ~Dirbaio
//            return customRendered;
    }

    private void renderDefaultImg(Graphics g, int RenderX, int RenderY)
    {
        BufferedImage img;
        if (Level.ValidSprites[this.Type])
            img = Resources.get("sprite");
        else
            img = Resources.get("sprite_invalid");
        g.drawImage(img, RenderX, RenderY, null);
        g.drawString(Type + "", RenderX, RenderY);
    }
    /*
     @Override
     public String toString()
     {
     return String.Format("SPR:{0}:{1}:{2}:{3:X2}{4:X2}{5:X2}{6:X2}{7:X2}{8:X2}", X, Y, Type,
     Data[0], Data[1], Data[2], Data[3], Data[4], Data[5]);
     }

     public static NSMBSprite FromString(String[] strs, int idx, NSMBLevel lvl)
     {
     NSMBSprite s = new NSMBSprite(lvl);
     s.x = int.Parse
     (strs[1 + idx]
     );
     s.y = int.Parse
     (strs[2 + idx]
     );
     s.Type = int.Parse
     (strs[3 + idx]
     );
     s.Data = new byte[6];
     for (int l = 0; l < 6; l++)
     {
     s.Data[l] = byte.Parse
     (strs[4 + idx].SubString(l * 2, 2)
     , System.Glorefbalization.NumberStyles.HexNumber
        
        
        
     );
     }
     idx += 5;
     return s;
     }*/

    public static NSMBSprite read(NSMBLevel lvl, ArrayReader in)
    {
        NSMBSprite s = new NSMBSprite(lvl);
        s.Type = in.readShort();
        s.X = in.readShort();
        s.Y = in.readShort();
        s.Data = new byte[6];
        s.Data[1] = in.readByte();
        s.Data[0] = in.readByte();
        s.Data[5] = in.readByte();
        s.Data[4] = in.readByte();
        s.Data[3] = in.readByte();
        s.Data[2] = in.readByte();
        return s;
    }

    public void write(ArrayWriter out)
    {
        out.writeShort((short) Type);
        out.writeShort((short) X);
        out.writeShort((short) Y);
        out.writeByte(Data[1]);
        out.writeByte(Data[0]);
        out.writeByte(Data[5]);
        out.writeByte(Data[4]);
        out.writeByte(Data[3]);
        out.writeByte(Data[2]);
    }
}
