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

package net.dirbaio.nsmbe.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import net.dirbaio.nsmbe.fs.AlreadyEditingException;
import net.dirbaio.nsmbe.fs.Directory;
import net.dirbaio.nsmbe.fs.File;
import net.dirbaio.nsmbe.fs.Filesystem;
import net.dirbaio.nsmbe.util.ArrayReader;
import net.dirbaio.nsmbe.util.ArrayWriter;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;

public class Server 
{
    Filesystem fs;
    ServerSocket listener;
    boolean running;
    
    
    public Server(Filesystem fs)
    {
        this.fs = fs;
    }
    
    public void run() throws IOException
    {
        listener = new ServerSocket(7373);
        running = true;
        while(running)
        {
            Socket s = listener.accept();
            Client t = new Client(this, s);
            t.start();
        }
    }
    
    
    public synchronized void handlePacket(byte[] packet, Client c) throws IOException
    {
        byte[] response = null;
        int error = 0;
        
        try
        {
            ArrayReader in = new ArrayReader(packet);
            ArrayWriter out = new ArrayWriter();

            byte packetId = in.readByte();
            switch(packetId)
            {
                case 0: //Ping
                    break;
                case 1: //Get file list
                    writeDir(fs.mainDir, out);
                    break;
                case 2: //Get file contents
                {
                    int fileId = in.readInt();
                    File f = fs.getFileById(fileId);
                    out.write(f.getContents());
                    break;
                }
                case 3: //Get filServer.java:129e range
                {
                    int fileId = in.readInt();
                    int start = in.readInt();
                    int end = in.readInt();
                    File f = fs.getFileById(fileId);
                    out.write(f.getInterval(start, end));
                    break;
                }
                case 4: //Lock file
                {
                    int fileId = in.readInt();
                    File f = fs.getFileById(fileId);
                    f.beginEdit(c);
                    c.lockedFiles.add(f);
                    break;
                }
                case 5: //Unlock file
                {
                    int fileId = in.readInt();
                    File f = fs.getFileById(fileId);
                    f.endEdit(c);
                    c.lockedFiles.remove(f);
                    break;
                }
                case 6: //Replace file
                {
                    int fileId = in.readInt();
                    int len = in.readInt();
                    byte[] data = new byte[len];
                    in.read(data);
                    File f = fs.getFileById(fileId);
                    f.replace(data, c);
                    break;
                }
                case 7: //Replace file interval
                {
                    int fileId = in.readInt();
                    int start = in.readInt();
                    int end = in.readInt();
                    byte[] data = new byte[end-start];
                    in.read(data);
                    File f = fs.getFileById(fileId);
                    f.replaceInterval(data, start);
                    break;
                }
                default:
                    throw new Exception("Invalid command: "+packetId);
            }

            response = out.getArray();
        }
        catch(Exception ex)
        {
            System.err.println("Error from client: "+c.socket.getRemoteSocketAddress());
            ex.printStackTrace();
            
            error = 1;
            if(ex instanceof AlreadyEditingException)
                error = 2;
            
            ArrayWriter out = new ArrayWriter();
            String msg = ex.getMessage();
            if(msg != null)
                out.writeString(msg);

            response = out.getArray();
            
        }
        
        ArrayWriter out2 = new ArrayWriter();
        out2.writeInt(error);
        out2.writeInt(response.length);
        out2.write(response);
        
        c.out.write(out2.getArray());
        c.out.flush();
    }
    
    private void writeDir(Directory d, ArrayWriter out)
    {
        out.writeInt(d.getId());
        out.writeString(d.getName());
        out.writeInt(d.childrenDirs.size());
        for(Directory dd : d.childrenDirs)
            writeDir(dd, out);
        out.writeInt(d.childrenFiles.size());
        for(File f : d.childrenFiles)
        {
            out.writeInt(f.getId());
            out.writeInt(f.getFileSize());
            out.writeString(f.getName());
        }
    }
}
