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

import net.dirbaio.nds.fs.File;

public class FileType {
    Type t;
    CompressionType comp;

    public FileType()
    {
    }

    public FileType(Type t, CompressionType comp)
    {
        this.t = t;
        this.comp = comp;
    }

    @Override
    public String toString()
    {
        return (comp != CompressionType.None?comp.toString()+"-":"")+t.toString();
    }
    
    
    
    public static FileType typeForFile(File f)
    {
        if(f.getFileSize() < 4)
            return new FileType(Type.Binary, CompressionType.None);

        int magic = f.getUintAt(0);
        
        FileType ft = typeForMagic(magic);
        if(ft != null)
            return ft;
        
        if(magic == 0x37375a4c && f.getFileSize() >= 13)
        {
            int magic2 = f.getUintAt(0x9);
            ft = typeForMagic(magic2);
            if(ft != null)
            {
                ft.comp = CompressionType.LzWithHeader;
                return ft;
            }
        }
        
        String name = f.getName();
        name = name.toLowerCase();
        
        if(name.startsWith("overlay")) return new FileType(Type.Overlay, CompressionType.None);
        if(name.endsWith("_ncl.bin")) return new FileType(Type.NCL, CompressionType.None);
        if(name.endsWith("_ncg.bin")) return new FileType(Type.NCG, CompressionType.None);
        if(name.endsWith("_nsc.bin")) return new FileType(Type.NSC, CompressionType.None);
        
        if((magic & 0xFF) == 0x10 && f.getFileSize() >= 9) //LZ without header
        {
            int magic2 = f.getUintAt(0x5);
            ft = typeForMagic(magic2);
            if(ft != null)
            {
                ft.comp = CompressionType.Lz;
                return ft;
            }
        }
        return new FileType(Type.Binary, CompressionType.None);
    }
    
    private static FileType typeForMagic(int magic)
    {
        
        switch(magic)
        {
            case 0x54414453: return new FileType(Type.SDAT, CompressionType.None);
            case 0x4352414e: return new FileType(Type.NARC, CompressionType.None);
                
            case 0x30414342: return new FileType(Type.NSBCA, CompressionType.None);
            case 0x30444d42: return new FileType(Type.NSBMD, CompressionType.None);
            case 0x30505442: return new FileType(Type.NSBTP, CompressionType.None);
            case 0x30585442: return new FileType(Type.NSBTX, CompressionType.None);
            case 0x30415642: return new FileType(Type.NSBVA, CompressionType.None);
                
            case 0x53504120: return new FileType(Type.SPA, CompressionType.None);
            case 0x4753454d: return new FileType(Type.BMG, CompressionType.None);
            case 0x4c424e4a: return new FileType(Type.BNBL, CompressionType.None);
            case 0x4c434e4a: return new FileType(Type.BNCL, CompressionType.None);
            case 0x44434e4a: return new FileType(Type.BNCD, CompressionType.None);
            case 0x4c4C4e4a: return new FileType(Type.BNLL, CompressionType.None);
        }
        
        return null;
    }
    public enum Type {
        Binary,
        Overlay,
        NARC,
        SDAT,
        NCL,
        NCG,
        NSC,
        NSBMD,
        NSBTX,
        NSBCA,
        NSBVA,
        NSBTP,
        ENPG,
        SPA,
        BMG,
        BNBL,
        BNCL, 
        BNCD,
        BNLL,
        ROM
    }
}
