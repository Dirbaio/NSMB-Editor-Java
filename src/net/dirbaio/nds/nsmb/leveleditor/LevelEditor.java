/*
 * Copyright (C) 2013 Piranhaplant
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
package net.dirbaio.nds.nsmb.leveleditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.dirbaio.nds.fs.*;
import net.dirbaio.nds.nsmb.NSMBRom;
import net.dirbaio.nds.nsmb.level.*;
import net.dirbaio.nds.nsmb.level.source.*;
import net.dirbaio.nds.nsmb.leveleditor.docking.*;
import net.infonode.docking.*;
import net.infonode.docking.theme.*;
import net.infonode.docking.util.*;
import net.infonode.util.*;

public class LevelEditor extends JFrame {
    public NSMBRom rom;
    public NSMBeTab curTab;
    
    JMenuBar menuBar;
    JFileChooser fileChooser;

    RootWindow rootWindow;
    ArrayList<View> rootViews = new ArrayList<>();
    ViewMap rootViewMap = new ViewMap();
    RootWindow tabWindow;
    ArrayList<View> tabViews = new ArrayList<>();
    ViewMap tabViewMap = new ViewMap();
    
    public LevelEditor() {
        super("NSMBe");
        
        this.setSize(600, 400);
        createMenuBar();
        createDocking();
        this.setJMenuBar(menuBar);
        this.add(rootWindow);
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Nintendo DS ROMs", "nds"));       
    }
    
    public final void setPanel(ArrayList<LevelItem> items)
    {
        for (View v : rootViews) {
            if (v instanceof NSMBeView) {
                ((NSMBeView)v).selectObjects(items);
            }
        }
    }
    
    public void openROM() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String name = fileChooser.getSelectedFile().getPath();
                NitroROMFilesystem fs = new NitroROMFilesystem(new ExternalFile(name));
                rom = new NSMBRom(fs);
                NSMBLevel l = new NSMBLevel(rom, new InternalLevelSource(rom, "A01_1", "1-1"));
                addTab(new LevelEditorTab(l, this));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void addTab(NSMBeTab tab) {
        tabViewMap.addView(tabViews.size(), tab);
        tabViews.add(tab);
        DockingWindow[] windows = new DockingWindow[tabViews.size()];
        for (int i = 0; i < tabViews.size(); i++) {
            windows[i] = tabViews.get(i);
        }
        tabWindow.setWindow(new TabWindow(windows));
        tab.requestFocus();
        updateViews();
    }
    
    public void updateViews() {
        View tab = tabWindow.getFocusedView();
        if (tab == curTab)
            return;
        if (tab instanceof NSMBeTab) {
            curTab = (NSMBeTab)tab;
            for (View v : rootViews) {
                if (v instanceof NSMBeView) {
                    ((NSMBeView)v).update(curTab);
                }
            }
        }
    }
    
    private void createMenuBar() {
        JMenu menu;
        JMenuItem menuItem;
        
        menuBar = new JMenuBar();
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        
        menuItem = new JMenuItem("Open ROM...", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openROM();
            }
        });
        menu.add(menuItem);
    }
    
    private void createDocking() {
        tabWindow = DockingUtil.createRootWindow(tabViewMap, true);
        
        rootViews.add(new ObjectView());
        View tabs = new View("Tabs", null, tabWindow);
        tabs.getViewProperties().getViewTitleBarProperties().setVisible(false);
        rootViews.add(tabs);
        
        for (int i = 0; i < rootViews.size(); i++) {
            rootViewMap.addView(i, rootViews.get(i));
        }
        
        rootWindow = DockingUtil.createRootWindow(rootViewMap, true);
        rootWindow.setWindow(new SplitWindow(true, 0.15f, rootViews.get(0), rootViews.get(1)));
        rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
        rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
        rootWindow.getRootWindowProperties().addSuperObject(PropertiesUtil.createTitleBarStyleRootWindowProperties());
        DockingWindowsTheme theme = new ShapedGradientDockingTheme();
        rootWindow.getRootWindowProperties().addSuperObject(theme.getRootWindowProperties());
        tabWindow.getRootWindowProperties().addSuperObject(theme.getRootWindowProperties());
        
        tabWindow.addListener(new DockingWindowListener() {
            @Override public void windowAdded(DockingWindow dw, DockingWindow dw1) { }
            @Override public void windowRemoved(DockingWindow dw, DockingWindow dw1) { }
            @Override public void windowShown(DockingWindow dw) { }
            @Override public void windowHidden(DockingWindow dw) { }
            @Override public void viewFocusChanged(View view, View view1) {
                updateViews();
            }
            @Override public void windowClosing(DockingWindow dw) throws OperationAbortedException { }
            @Override public void windowClosed(DockingWindow dw) { }
            @Override public void windowUndocking(DockingWindow dw) throws OperationAbortedException { }
            @Override public void windowUndocked(DockingWindow dw) { }
            @Override public void windowDocking(DockingWindow dw) throws OperationAbortedException { }
            @Override public void windowDocked(DockingWindow dw) { }
            @Override public void windowMinimizing(DockingWindow dw) throws OperationAbortedException { }
            @Override public void windowMinimized(DockingWindow dw) { }
            @Override public void windowMaximizing(DockingWindow dw) throws OperationAbortedException { }
            @Override public void windowMaximized(DockingWindow dw) { }
            @Override public void windowRestoring(DockingWindow dw) throws OperationAbortedException { }
            @Override public void windowRestored(DockingWindow dw) { }
        });
    }
}
