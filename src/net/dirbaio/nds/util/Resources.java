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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class Resources
{

    private static HashMap<String, BufferedImage> cache = new HashMap<>();

    public static BufferedImage get(String name)
    {
        if(cache.containsKey(name))
            return cache.get(name);
        
        BufferedImage img;
        try
        {
            String path = "/images/"+name+".png";
            System.out.println(path);
            img = ImageIO.read(Resources.class.getResource(path));
        } catch (IOException ex)
        {
            img = null;
        }
        
        cache.put(name, img);
        return img;
    }
}
