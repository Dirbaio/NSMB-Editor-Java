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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import net.dirbaio.nsmbe.fs.File;
import net.dirbaio.nsmbe.util.ArrayReader;

public class NetFSClientConnection extends Thread
{

    NetFSServer server;
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    HashSet<File> lockedFiles = new HashSet<>();

    public NetFSClientConnection(NetFSServer server, Socket socket)
    {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try
        {
            byte[] dlen = new byte[4];

            System.out.println(socket.getRemoteSocketAddress() + " connected.");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            while (true)
            {
                in.readFully(dlen);
                ArrayReader r = new ArrayReader(dlen);
                int len = r.readInt();

                byte[] packet = new byte[len];
                in.readFully(packet);
                server.handlePacket(packet, this);
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

        for (File f : lockedFiles)
            f.endEdit(this);

        System.out.println(socket.getRemoteSocketAddress() + " disconnected.");
    }
}
