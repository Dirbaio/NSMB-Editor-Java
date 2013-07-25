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
package net.dirbaio.nsmbe.binary;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dirbaio.nsmbe.Rom;
import net.dirbaio.nsmbe.fs.AlreadyEditingException;
import net.dirbaio.nsmbe.util.ArrayWriter;
import static net.dirbaio.nsmbe.util.Util.*;

public class Analyzer
{

    Rom rom;
    ArrayList<Section> sections;

    public Analyzer(Rom rom) throws AlreadyEditingException
    {
        this.rom = rom;
        sections = new ArrayList<>();
        loadSections();
        findUniqueSections();
        allocateSections();
        makeSectionDependencies();

        Collections.sort(sections);

        if (false)
        {
            Section s = getSectionByName("main3");
            s.analyzeFunctions(0x0205147C);
            writeIdc();
        } else
        {
            for (Section s : sections)
                s.analyzeFunctions();

            for (Section s : sections)
                s.fixPointers();

            writeIdc();
            writeData();
        }
    }

    private void loadSections() throws AlreadyEditingException
    {

        Arm9 a = new Arm9(rom.arm9binFile);
        int ii = 0;
        for (Arm9.Section ss : a.sections)
        {
            byte[] data = new byte[ss.len + ss.bssSize];
            System.arraycopy(ss.data, 0, data, 0, ss.data.length);

            for (int j = ss.len; j < data.length; j++)
                data[j] = (byte) (j % 4 == 3 ? 0xE7 : 0xFF);

            Section s = new Section();
            s.name = "main" + (ii++);
            s.data = data;
            s.type = new byte[s.data.length];
            s.ramAddr = ss.ramAddr;
            s.codeSize = ss.len;
            sections.add(s);
        }

        for (int i = 0; i < rom.arm9ovs.length; i++)
        {
            Overlay o = rom.arm9ovs[i];

            byte[] data = new byte[o.getRamSize() + o.getBssSize()];
            byte[] data2 = o.getDecompressedData();
            System.arraycopy(data2, 0, data, 0, data2.length);

            for (int j = o.getRamSize(); j < data.length; j++)
                data[j] = (byte) (j % 4 == 3 ? 0xE7 : 0xFF);

            Section s = new Section();
            s.name = "ov" + i;
            s.data = data;
            s.type = new byte[s.data.length];
            s.ramAddr = o.getRamAddr();
            s.codeSize = o.getRamSize();
            sections.add(s);
        }
    }

    private void findUniqueSections()
    {
        for (Section sa : sections)
            for (Section sb : sections)
            {
                if (sa == sb)
                    continue;
                if (sa.collidesWith(sb))
                {
                    sa.unique = false;
                    sb.unique = false;
                }
            }

        //Hardcode some overlays so they're left at their original pos
        //They're the most commonly used, I don't want all pointers
        //to them to break.
        getSectionByName("ov0").unique = true;
        getSectionByName("ov10").unique = true;
        getSectionByName("ov11").unique = true;
        getSectionByName("ov12").unique = true;
        getSectionByName("ov22").unique = true;
        getSectionByName("ov54").unique = true;
        getSectionByName("ov89").unique = true;
        getSectionByName("ov99").unique = true;

        //This section sux, it moves everything by +8MB
        sections.remove(getSectionByName("main2"));
    }

    private void allocateSections()
    {
        int start = 0;
        int end = 0;
        boolean found = false;

        //Unique sections don't get moved.
        for (Section s : sections)
            if (s.unique)
            {
                int sstart = s.ramAddr;
                int send = s.ramAddr + s.data.length;
                if (sstart < start || !found)
                    start = sstart;
                if (send > end || !found)
                    end = send;
                found = true;
                s.newRamAddr = s.ramAddr;
            }

        //Everything else is slapped after it.
        for (Section s : sections)
            if (!s.unique)
            {
                s.newRamAddr = end;
                end += s.data.length;
            }
    }

    private Section overlay(int id)
    {
        return getSectionByName("ov" + id);
    }

    private void makeSectionDependencies()
    {
        for (int o = 0; o <= 125; o++)
        {
            Section s = overlay(o);
            s.dependencies.add(overlay(0));
            s.dependencies.add(overlay(11));
        }
        for (int o = 12; o <= 51; o++)
        {
            Section s = overlay(o);
            s.dependencies.add(overlay(10));
        }
        for (int o = 56; o <= 125; o++)
        {
            Section s = overlay(o);
            s.dependencies.add(overlay(10));
        }

        for (Section s : sections)
        {
            s.possiblePointers.addAll(s.dependencies);
            for (Section s2 : sections)
            {
                boolean ok = true;

                for (Section dep : s.dependencies)
                    ok &= !s2.collidesWith(dep);

                if (ok)
                    s.possiblePointers.add(s2);
            }
        }
    }

    private void writeIdc()
    {
        //Sort by new ram addr

        System.out.println("IDC script:");
        try
        {

            PrintStream fout = new PrintStream(new BufferedOutputStream(new FileOutputStream("data.idc")));

            fout.println("#include <idc.idc>\nstatic main(void){");

            fout.println("DeleteAll();");
            for (Section s : sections)
            {
                String base = "0";
                if (s.codeSize != 0)
                {
                    fout.println("SegCreate(" + hex(s.newRamAddr) + ", " + hex(s.newRamAddr + s.codeSize) + ", " + base + ", 1, 1, 2);");
                    fout.println("SegRename(" + hex(s.newRamAddr) + ", \"" + s.name + "\");");
                    fout.println("SegClass(" + hex(s.newRamAddr) + ", \"CODE\");");
                    fout.println("SegDefReg(" + hex(s.newRamAddr) + ", \"T\", 0x0);");
                    fout.println("SegDefReg(" + hex(s.newRamAddr) + ", \"DS\", 0x0);");
                    fout.println("SetSegmentType(" + hex(s.newRamAddr) + ", SEG_CODE);");
                }

                if (s.codeSize != s.data.length)
                {
                    fout.println("SegCreate(" + hex(s.newRamAddr + s.codeSize) + ", " + hex(s.newRamAddr + s.data.length) + ", " + base + ", 1, 1, 2);");
                    fout.println("SegRename(" + hex(s.newRamAddr + s.codeSize) + ", \"" + s.name + "b\");");
                    fout.println("SegClass(" + hex(s.newRamAddr + s.codeSize) + ", \"BSS\");");
                    fout.println("SegDefReg(" + hex(s.newRamAddr + s.codeSize) + ", \"T\", 0x0);");
                    fout.println("SegDefReg(" + hex(s.newRamAddr + s.codeSize) + ", \"DS\", 0x0);");
                    fout.println("SetSegmentType(" + hex(s.newRamAddr) + ", SEG_BSS);");
                }
            }

            int count = 0;
            for (Section s : sections)
            {
                for (Integer i : s.functions)
                {
                    fout.println("SetRegEx(" + hex(i + s.newRamAddr - s.ramAddr) + ",\"T\",0,3);");
                    fout.println("MakeFunction(" + hex(i + s.newRamAddr - s.ramAddr) + ", BADADDR);");
                    count++;
                }
                for (Integer i : s.thumbFunctions)
                {
                    fout.println("SetRegEx(" + hex(i + s.newRamAddr - s.ramAddr) + ",\"T\",1,3);");
                    fout.println("MakeFunction(" + hex(i + s.newRamAddr - s.ramAddr) + ", BADADDR);");
                    count++;
                }
                for (Integer i : s.offsets)
                {
                    fout.println("MakeDword(" + hex(i + s.newRamAddr - s.ramAddr) + ");");
                    fout.println("OpOff(" + hex(i + s.newRamAddr - s.ramAddr) + ", 0, 0);");
                    count++;
                }
            }
            fout.println("}");
            System.out.println("Found " + count + " functions all automatically for you!");
        } catch (FileNotFoundException fileNotFoundException)
        {
        }
    }

    private void writeData()
    {
        //Sections should be sorted by now.
        int addr = sections.get(0).newRamAddr;
        try
        {
            ArrayWriter out = new ArrayWriter();
            for (Section s : sections)
            {
                for (int i = addr; i < s.newRamAddr; i++)
                    out.writeByte((byte) 0x77);
                addr = s.newRamAddr + s.data.length;
                out.write(s.data);
            }
            FileOutputStream fout = new FileOutputStream("data.bin");
            fout.write(out.getArray());
            fout.close();
        } catch (IOException ex)
        {
            Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Data written to data.bin!");
    }

    private Section getSectionByName(String name)
    {
        for (Section s : sections)
            if (s.name.equals(name))
                return s;

        return null;
    }
    private static final int TYPE_DATA = 0;
    private static final int TYPE_ARM = 1;
    private static final int TYPE_THUMB = 2;
    private static final int TYPE_FUNCDATA = 3; //The value pool after functions. This one should never be pointed to.

    class Section implements Comparable<Section>
    {

        String name;
        byte[] data;
        int codeSize;
        int ramAddr;
        int newRamAddr;
        boolean unique = true;
        HashSet<Integer> offsets = new HashSet<>();
        HashSet<Integer> functions = new HashSet<>();
        HashSet<Integer> thumbFunctions = new HashSet<>();
        ArrayList<Section> dependencies = new ArrayList<>();
        HashSet<Section> noDependencies = new HashSet<>();
        ArrayList<Section> possiblePointers = new ArrayList<>();
        byte[] type;

        boolean collidesWith(Section b)
        {
            return ramAddr < b.ramAddr + b.data.length
                    && b.ramAddr < ramAddr + data.length;
        }

        public boolean containsAddress(int addr)
        {
            return addr >= ramAddr && addr < ramAddr + data.length;
        }

        public boolean containsAddressCode(int addr)
        {
            return addr >= ramAddr && addr < ramAddr + codeSize;
        }

        public boolean containsNewAddress(int addr)
        {
            return addr >= newRamAddr && addr < newRamAddr + data.length;
        }

        @Override
        public int compareTo(Section o)
        {
            return Integer.compare(newRamAddr, o.newRamAddr);
        }

        public int getWord(int addr)
        {
            addr -= ramAddr;
            if (addr >= codeSize)
                throw new RuntimeException("Out of bounds");

            return (data[addr] & 0xFF)
                    | ((data[addr + 1] & 0xFF) << 8);
        }

        public void setWord(int addr, int val)
        {
            addr -= ramAddr;
            if (addr >= codeSize)
                throw new RuntimeException("Out of bounds");

            data[addr] = (byte) (val);
            data[addr + 1] = (byte) (val >> 8);
        }

        public int getDword(int addr)
        {
            addr -= ramAddr;
            if (addr >= codeSize)
                throw new RuntimeException("Out of bounds");

            return (data[addr] & 0xFF)
                    | ((data[addr + 1] & 0xFF) << 8)
                    | ((data[addr + 2] & 0xFF) << 16)
                    | ((data[addr + 3] & 0xFF) << 24);
        }

        public void setDword(int addr, int val)
        {
            addr -= ramAddr;
            if (addr >= codeSize)
                throw new RuntimeException("Out of bounds");

            data[addr] = (byte) (val);
            data[addr + 1] = (byte) (val >> 8);
            data[addr + 2] = (byte) (val >> 16);
            data[addr + 3] = (byte) (val >> 24);
        }

        public void analyzeFunctions()
        {
            analyzeFunctions(ramAddr);
            for (int i = 0; i < Hax.adresses.length; i++)
            {
                int addr = Hax.adresses[i];
                if (containsNewAddress(addr))
                    analyzeFunctions(addr - newRamAddr + ramAddr);
            }
        }

        public void analyzeFunctions(int addr)
        {
            System.out.println("\n\nStarting at " + hex(toNew(addr)));
            while (addr + 4 < ramAddr + codeSize)
            {
                if (functions.contains(addr))
                    return;
                if (thumbFunctions.contains(addr))
                    return;

                int ret = analyzeFunction(addr);

                if (ret == -1)
                {
                    addr += 4;
                    while (addr + 4 < ramAddr + codeSize)
                    {
                        int instr = getDword(addr);
                        if (((instr >> 16) & 0xFFFF) == 0xE92D)
                        {
                            System.out.println("Recovered at " + hex(toNew(addr)));
                            break;
                        }
                        addr += 4;
                    }
                    /*
                     System.out.println("Stopping at " + hex(toNew(addr)) + " ("+percent+"%)");
                     int max = 0;
                     for(int i : functions)
                     if(i > max) max = i;
                     for(int i : thumbFunctions)
                     if(i > max) max = i;
                     System.out.println("Last function at "+hex(toNew(max)));
                     break;*/
                } else
                {
                    addr = ret;
                    //Thumb functions seem to be always aligned to 4 bytes
                    if (addr % 4 == 2)
                        addr += 2;
                }
            }
        }
        boolean lastThumb = false;

        public int analyzeFunction(int addr)
        {
            try
            {
                return analyzeFunctionUnsafe(addr);
            } catch (Exception e)
            {
                System.out.println("Exception");
                return -1;
            }
        }

        private boolean seemsBadInstruction(int instr)
        {
            if ((instr & 0x7FFFFFFF) < 16)
                return true;
            if (instr == 0xE7FFFFFF) //These can't be instructions.
                return true;
            if (isAddress(instr)) //Adress
                return true;

            return false;
        }

        private boolean seemsBadFunction(int addr)
        {
            if (!seemsBadInstruction(getDword(addr)))
                return false;
            if (!seemsBadInstruction(getDword(addr + 4)))
                return false;
            if (!seemsBadInstruction(getDword(addr + 8)))
                return false;
            return true;
        }

        public int analyzeFunctionUnsafe(int addr)
        {
            if (seemsBadFunction(addr))
            {
                System.out.println("Seems bad function!");
                return -1;
            }

            boolean thumb = lastThumb;

            //Most if not all Thumb functions start with PUSH.
            if (((getWord(addr) >> 8) & 0xFE) == 0xB4)
                thumb = true;

            //LDMFD R13! ... then it's ARM.
            if (getWord(addr + 2) == 0xE92D)
                thumb = false;

            int ret;

            if (thumb)
                ret = analyzeFunctionThumb(addr);
            else
                ret = analyzeFunctionArm(addr);

            if (ret == -1)
            {
                thumb = !thumb;
                if (thumb)
                    ret = analyzeFunctionThumb(addr);
                else
                    ret = analyzeFunctionArm(addr);
            }

            if (ret != -1)
            {
//                System.out.println("Function! " + thumb + " " + hex(addr));
                if (thumb)
                    thumbFunctions.add(addr);
                else
                    functions.add(addr);

                lastThumb = thumb;
            }

            return ret;
        }

        public int analyzeFunctionThumb(int addr)
        {
            Queue<Integer> q = new ArrayDeque<>();
            HashSet<Integer> exploredCode = new HashSet<>();
            HashSet<Integer> exploredData = new HashSet<>();

            q.add(addr);

            int max = 0;

            while (!q.isEmpty())
            {
                addr = q.poll();
                if (exploredCode.contains(addr))
                    continue;
                if (exploredData.contains(addr))
                    continue;
                exploredCode.add(addr);

                if (addr + 2 > max)
                    max = addr + 2;

                try
                {
                    int instr = getWord(addr);

                    boolean next = true;

                    if (instr == 0 || instr == 0xE7FF || instr == 0xFFFF) //These can't be instructions.
                    {
                        System.out.println("Bad Thumb " + hex(toNew(addr)));
                        return -1;
                    }

                    if (((instr >> 8) & 0xFF) == 0xBD) //POP {.., PC}
                        next = false;
                    else if (((instr >> 12) & 0xF) == 0b1101) //Conditional branch
                    {
                        int offset = instr & 0xFF;
                        offset <<= 24;
                        offset >>= 24;
                        int newaddr = addr + 4 + offset * 2;
                        q.add(newaddr);
                    } else if (((instr >> 11) & 0x1F) == 0b11100) //Unconditional branch
                    {
                        int offset = instr & 0x3FF;
                        offset <<= 22;
                        offset >>= 22;
                        int newaddr = addr + 4 + offset * 2;
                        q.add(newaddr);
                        next = false;
                    } else if (((instr >> 11) & 0x1F) == 0b01001) //LDR Rx, =0xxxx
                    {
                        int offset = instr & 0xFF;
                        offset <<= 24;
                        offset >>= 24;
                        int targ = (addr & ~3) + 4 + offset * 4;
                        exploredData.add(targ);
                        if (targ + 4 > max)
                            max = targ + 4;
                    } else if (((instr >> 7) & 0x1FF) == 0b010001110) //BX Rx
                        next = false;

                    if (next)
                        q.add(addr + 2);
                } catch (Exception e)
                {
                    System.err.println("ERROR AT " + hex(addr) + " (" + hex(addr - ramAddr + newRamAddr) + ")");
//                    e.printStackTrace();
                    throw e;
                }
            }

            for (int i : exploredCode)
                type[i - ramAddr] = TYPE_THUMB;
            for (int i : exploredData)
                type[i - ramAddr] = TYPE_FUNCDATA;
            return max;
        }

        public int analyzeFunctionArm(int addr)
        {

            int start = getDword(addr);
            if (((start >> 28) & 0xF) != 0xE) //Functions must start with ALWAYS condition.
            {
                System.out.println("Bad starting condition at " + hex(toNew(addr)));
                return -1;
            }
            Queue<Integer> q = new ArrayDeque<>();
            HashSet<Integer> exploredCode = new HashSet<>();
            HashSet<Integer> exploredData = new HashSet<>();

            q.add(addr);

            int max = 0;

            while (!q.isEmpty())
            {
                addr = q.poll();
                if (exploredCode.contains(addr))
                    continue;
                if (exploredData.contains(addr))
                    continue;
                exploredCode.add(addr);

                if (addr > max)
                    max = addr;

                try
                {

//                System.out.println("addr " + hex(addr));

                    int instr = getDword(addr);
                    int cond = (instr >> 28) & 0xF;

                    boolean next = true;

                    if (((instr & 0x7FFFFFFF) < 16 && !isBadWhitelisted(toNew(addr))) || instr == 0xE7FFFFFF) //These can't be instructions.
                    {
                        System.out.println("Bad " + hex(toNew(addr)));

                        return -1;
                    }


                    // E3500009 908FF100
                    // E3550006
                    if ((instr & 0xFFFFFFF0) == 0x908FF100) //JUMP TABLE!
                    {
                        int reg = instr & 0xFF;

                        int cases = -1;
                        for (int i = 1; i < 10 && cases == -1; i++)
                        {
                            int instr2 = getDword(addr - 4 * i);
                            if (((instr2 >> 16) & 0xFFFF) != (0xE350 | reg))
                                continue;

                            cases = instr2 & 0xFF;
                            int shift = (instr2 >> 8) & 0xF;
                            cases <<= (shift * 2);
                            cases++;
                        }
                        System.out.println("Detected Jumptable at " + hex(toNew(addr)) + " cases: " + cases + " Register: R" + reg);
                        if (cases == -1)
                            return -1;

                        for (int i = 0; i < cases; i++)
                            q.add(addr + 8 + i * 4);
                    } //A08FF102
                    else if ((instr & 0xFFFFFFF0) == 0xA08FF100) //WEIRD jump table IDA doesn't even recognize
                    {
                        System.out.println("Detected Weird Jumptable at " + hex(toNew(addr)));
                        //I have no idea how to get the case count. Let's hope this doesn't blow up.
                        int i = 4;
                        while (((getDword(addr + i) >> 24) & 0xF) == 0b1010)
                        {
                            q.add(addr + i);
                            i += 4;
                        }
                    } else if (((instr >> 25) & 0x7) == 0b101 && cond == 0xF) //BLX instruction
                    {
                    } else if (((instr >> 24) & 0xF) == 0b1010) //B instruction
                    {
//                    System.out.println("B");
//                    if (condition == 0xF)
//                        System.out.println("BLX!!");

                        int offs = instr & 0xFFFFFF;
                        offs <<= 8;
                        offs >>= 8;
                        int newaddr = addr + 8 + offs * 4;
                        getDword(newaddr);
                        q.add(newaddr);

                        next = cond != 0xE;
                    } else if (((instr >> 24) & 0xF) == 0b1011) //BL instruction
                    {
//                    System.out.println("B");
//                    if (condition == 0xF)
//                        System.out.println("BLX!!");

                        int offs = instr & 0xFFFFFF;
                        offs <<= 8;
                        offs >>= 8;
                        int newaddr = addr + 8 + offs * 4;
//                        getDword(newaddr);
//                        q.add(newaddr);
                    } else if (((instr >> 4) & 0xFFFFFF) == 0b000100101111111111110001) //BX reg instruction
//                    System.out.println("BX");
                        next = cond != 0xE;
                    else if (((instr >> 15) & 0x1FFF) == 0b1000101111011) //LDMFD SP!, {..., PC}
                        next = cond != 0xE;
                    else if (((instr >> 16) & 0xFFF) == 0b010110011111) //LDR Rx, [PC, ...]
                    {
                        int offs = instr & 0xFFF;
                        offs <<= 20; //Signextend
                        offs >>= 20;
                        int targ = addr + 8 + offs;
                        exploredData.add(targ);
                        if (targ > max)
                            max = targ;
//                    System.out.println("data " + hex(targ));
                    }

                    if (next)
                        q.add(addr + 4);
                } catch (Exception e)
                {
                    System.err.println("ERROR AT " + hex(addr) + " (" + hex(addr - ramAddr + newRamAddr) + ")");
                    throw e;
                }
            }

            for (int i : exploredCode)
                type[i - ramAddr] = TYPE_ARM;
            for (int i : exploredData)
                type[i - ramAddr] = TYPE_FUNCDATA;

            return max + 4;
        }

        void fixPointers()
        {
            for (int i = 0; i < codeSize; i += 4)
            {
                int addr = ramAddr + i;
                int t = type[i];

                if (t == TYPE_DATA || t == TYPE_FUNCDATA)
                {
                    int val = getDword(addr);
                    if (isAddress(val)) //Adress
                    {
                        int newVal = fixPointer(val, this, toNew(addr), TYPE_DATA);
                        setDword(addr, newVal);
                        offsets.add(addr);
                    }

                } else if (t == TYPE_ARM)
                {
                    int instr = getDword(addr);
                    int cond = (instr >> 28) & 0xF;

                    if (((instr >> 25) & 0x7) == 0b101 && cond == 0xF) //BLX instruction
                    {
                        int offs = instr & 0xFFFFFF;
                        offs <<= 8;
                        offs >>= 8;
                        int h = (instr >> 24) & 1;
                        int newaddr = addr + 8 + offs * 4 + h * 2;

                        newaddr = fixPointer(newaddr, this, toNew(addr), TYPE_THUMB);

                        newaddr += ramAddr - newRamAddr;
                        offs = (newaddr - addr - 8) / 4;
                        offs &= 0xFFFFFF;
                        instr &= ~0xFFFFFF;
                        instr |= offs;

                        setDword(addr, instr);
                    } else if (((instr >> 24) & 0xF) == 0b1011) //BL instruction
                    {
                        int offs = instr & 0xFFFFFF;
                        offs <<= 8;
                        offs >>= 8;
                        int newaddr = addr + 8 + offs * 4;
                        newaddr = fixPointer(newaddr, this, toNew(addr), TYPE_ARM);
                        newaddr += ramAddr - newRamAddr;
                        offs = (newaddr - addr - 8) / 4;
                        offs &= 0xFFFFFF;
                        instr &= ~0xFFFFFF;
                        instr |= offs;

                        setDword(addr, instr);
//                        getDword(newaddr);
//                        q.add(newaddr);
                    }
                } else if (t == TYPE_THUMB)
                    for (int j = 0; j < 4; j += 2)
                    {
                        int addr2 = addr + j;
                        int instr = getDword(addr2);
                        if (((instr >> 11) & 0x1F) == 0b11110) // BL/BLX long instruction
                        {
                            int offs = instr & 0x7FF;
                            offs <<= 11;
                            offs |= (instr >> 16) & 0x7FF;
                            //offs 22bit
                            offs <<= 10;
                            offs >>= 10;

                            int addr2b = addr2;
                            boolean arm = ((instr >> 28) & 1) == 0;
                            if (arm)
                                addr2b &= ~3; //Huh, why is this needed !?

                            int newaddr = addr2b + 4 + offs * 2;
                            System.out.println("THUMB BL " + hex(addr2) + " " + hex(newaddr));
                            newaddr = fixPointer(newaddr, this, toNew(addr2b), arm ? TYPE_ARM : TYPE_THUMB);
                            newaddr += ramAddr - newRamAddr;
                            offs = (newaddr - addr2b - 4) / 2;
                            instr &= ~0x07FF07FF;
                            instr |= (offs >> 11) & 0x7FF;
                            instr |= (offs << 16) & 0x07FF0000;

                            setDword(addr2, instr);
                        }
                    } //TODO
            }
        }

        int toNew(int addr)
        {
            return addr - ramAddr + newRamAddr;
        }
    }

    boolean isAddress(int addr)
    {
        return (addr & 0xFFE00000) == 0x02000000
                || (addr & 0xFFFF8000) == 0x01FF8000;
    }

    int fixPointer(int ptr, Section origin, int originAddr, int type)
    {
        if (origin.containsAddress(ptr))
            return origin.toNew(ptr);

        ArrayList<Section> possible = origin.possiblePointers;

        if (originAddr >= 0x0203997C && originAddr < 0x02039F80) //Profile pointer table
        {
            //It must point to *data*.
            //And the data must be an *address*

            for (Section s : possible)
                if (s.containsAddressCode(ptr) && s.type[ptr - s.ramAddr] == TYPE_DATA
                        && isAddress(s.getDword(ptr))
                        && !isAddress(s.getDword(ptr + 4))
                        && s.getDword(ptr + 4) != 0)
//                    System.out.println("Pointer profile "+hex(originAddr) + " -> "+hex(s.toNew(ptr)));
                    return s.toNew(ptr);

            System.out.println("Unfixed PROFILE pointer from " + origin.name + ":" + hex(originAddr) + " -- " + hex(ptr));
        }

        if (type != TYPE_THUMB)
            for (Section s : possible)
                if (s.containsAddress(ptr) && (s.functions.contains(ptr)))
                    return s.toNew(ptr);

        if (type != TYPE_ARM)
            for (Section s : possible)
                if (s.containsAddress(ptr) && (s.thumbFunctions.contains(ptr)))
                    return s.toNew(ptr);

        if (type != TYPE_ARM)
            for (Section s : possible)
                if (s.containsAddress(ptr - 1) && (s.thumbFunctions.contains(ptr - 1)))
                    return s.toNew(ptr);

        if (type != TYPE_DATA)
            System.out.println("Unfixed code pointer from " + origin.name + ":" + hex(originAddr) + " -- " + hex(ptr));

        for (Section s : possible)
            if (s.containsAddressCode(ptr) && s.type[ptr - s.ramAddr] == TYPE_DATA)
                return s.toNew(ptr);

        for (Section s : possible)
            if (s.containsAddress(ptr) && s.type[ptr - s.ramAddr] == TYPE_DATA)
                return s.toNew(ptr);

        for (Section s : possible)
            if (s.containsAddress(ptr))
                return s.toNew(ptr);

        if (type == TYPE_DATA)
            System.out.println("Unfixed data pointer from " + origin.name + ":" + hex(originAddr) + " -- " + hex(ptr));

        return ptr;
    }
    int[] badWhitelist =
    {
        0x020568E8,
        0x020B9780,
    };

    boolean isBadWhitelisted(int addr)
    {
        for (int i = 0; i < badWhitelist.length; i++)
            if (badWhitelist[i] == addr)
                return true;
        return false;
    }
}
