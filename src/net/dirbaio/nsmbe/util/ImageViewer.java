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
package net.dirbaio.nsmbe.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class ImageViewer extends JFrame
{

    Image ii;

    public ImageViewer(Image i)
    {
        super("Image Viewer");
        ii = i;
        setSize(800, 500);
        JScrollPane jsp = new JScrollPane(new ImagePreviewerControl());
        jsp.setBorder(null);
        add(jsp, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    class ImagePreviewerControl extends JComponent
    {

        public ImagePreviewerControl()
        {
            //setSize(ii.getWidth(null), ii.getHeight(null));
            setPreferredSize(new Dimension(ii.getWidth(null), ii.getHeight(null)));
        }

        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Util.drawTransparent(g, 0, 0, ii.getWidth(null), ii.getHeight(null));
            g.drawImage(ii, 0, 0, null);
        }
    }
}