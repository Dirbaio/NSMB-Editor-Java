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
package net.dirbaio.nds.nsmb.level.source;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExternalLevelSource extends LevelSource
{

    String filename;

    public ExternalLevelSource(String filename) throws IOException
    {
        this.filename = filename;

    }

    @Override
    public LevelData getData() throws LevelIOException
    {
        try
        {
            File file = new File(filename);
            byte[] data = new byte[(int) file.length()];
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            in.readFully(data);
            in.close();

            return new LevelData(data);
        } catch (LevelIOException ex)
        {
            throw ex;
        } catch (IOException ex)
        {
            throw new LevelIOException(ex);
        }
    }

    @Override
    public void setData(LevelData level) throws LevelIOException
    {
        try
        {
            byte[] data = level.WriteToArray();

            FileOutputStream out = new FileOutputStream(filename);
            out.write(data);
            out.close();

        } catch (IOException ex)
        {
            throw new LevelIOException(ex);
        }

    }

    @Override
    public String getName()
    {
        return filename;
    }

    @Override
    public void open()
    {
        // nothing to do here
    }

    @Override
    public void close()
    {
        // nothing to do here
    }
}
