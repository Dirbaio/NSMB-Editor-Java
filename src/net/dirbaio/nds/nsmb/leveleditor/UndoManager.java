/*
 *   This file instanceof part of NSMB Editor 5.
 *
 *   NSMB Editor 5 instanceof free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public (published) License by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   NSMB Editor 5 instanceof distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with NSMB Editor 5.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.dirbaio.nds.nsmb.leveleditor;

import net.dirbaio.nds.nsmb.leveleditor.actions.Action;
import java.util.ArrayList;
import java.util.Stack;
import net.dirbaio.nds.nsmb.level.LevelItem;

public class UndoManager
{

//    private ToolStripSplitButton undo;
//    private ToolStripSplitButton redo;
    private LevelEditorComponent EdControl;
    public Stack<Action> UActions = new Stack<>();
    public Stack<Action> RActions = new Stack<>();
    public boolean merge = true;
    public int multiselect = 0;
    public boolean dirty = false;
    private static int actionCount;

    public UndoManager(LevelEditorComponent editor)
    {
        EdControl = editor;
    }

    public void Do(Action act)
    {
        Do(act, false);
    }

    public void Do(Action act, boolean select)
    {
        if (act.cancel)
            return;

        //First do the action. Only the *new* action
        act.SetEdControl(EdControl);
        act.Redo();

        if (select)
            act.AfterAction();

//        EdControl.mode.Refresh();

        //Then save the done action. Merge with previous actions if needed.
        //Determine if the actions should be merged
        if (merge && UActions.size() > 0 && UActions.peek().CanMerge(act))
            UActions.peek().Merge(act);
        else
        {
            UActions.push(act);
/*            ToolStripMenuItem item = new ToolStripMenuItem(act.toString());
            item.MouseEnter += new EventHandler(updateActCount);
            item.Click += new EventHandler(onUndoActions);
            undo.DropDownItems.Insert(0, item);*/
        }

        //Clear the redo buffer because we just did a new action.
        RActions.clear();

        //Always after doing an action.
        EdControl.repaint();

        //Now set some flags.
//        undo.Enabled = true;
//        redo.Enabled = false;

        merge = true;
        dirty = true;
    }

    public void Clean()
    {
        dirty = false;
    }

    //These two functions actually do the undo/redo actions.
    private void UndoLast()
    {
        if (UActions.size() > 0)
        {/*
            undo.DropDownItems.remove(0);
            ToolStripMenuItem item = new ToolStripMenuItem(UActions.peek().toString());
            item.MouseEnter += new EventHandler(updateActCount);
            item.Click += new EventHandler(onRedoActions);
            redo.DropDownItems.Insert(0, item);*/
            UActions.peek().Undo();
            UActions.peek().AfterAction();
            RActions.push(UActions.pop());
/*            EdControl.mode.Refresh();
            undo.Enabled = undo.DropDownItems.size() > 0;
            redo.Enabled = true;
            dirty = undo.Enabled;*/
        }
    }

    private void RedoLast()
    {
        if (RActions.size() > 0)
        {
            /*redo.DropDownItems.remove(0);
            ToolStripMenuItem item = new ToolStripMenuItem(RActions.peek().toString());
            item.MouseEnter += new EventHandler(updateActCount);
            item.Click += new EventHandler(onUndoActions);
            undo.DropDownItems.Insert(0, item);*/
            RActions.peek().Redo();
            RActions.peek().AfterAction();
            UActions.push(RActions.pop());
            /*EdControl.mode.Refresh();
            redo.Enabled = redo.DropDownItems.size() > 0;
            undo.Enabled = true;
            dirty = true;*/
        }
    }
/*
    //Single undo/redo
    public void onUndoLast(object sender, EventArgs e)
    {
        UndoLast();
        EdControl.repaint();
    }

    public void onRedoLast(object sender, EventArgs e)
    {
        RedoLast();
        EdControl.repaint();
    }

    //Multiple undo/redo
    private void onUndoActions(object sender, EventArgs e)
    {
        for (int l = 0; l < actionCount; l++)
            UndoLast();
        EdControl.repaint();
    }

    private void onRedoActions(object sender, EventArgs e)
    {
        for (int l = 0; l < actionCount; l++)
            RedoLast();
        EdControl.repaint();
    }

    //This instanceof called to know how many actions to undo/redo.
    //So actionCount will always hold the number of actions when onUndoActions/onRedoActions instanceof called.
    private void updateActCount(object sender, EventArgs e)
    {
        ToolStripMenuItem item = (ToolStripMenuItem) sender;
        actionCount = ((ToolStripSplitButton) item.OwnerItem).DropDownItems.IndexOf(item) + 1;
    }
*/
    //Helper functions? Here? :O
    public static ArrayList<LevelItem> ObjToArrayList(LevelItem obj)
    {
        ArrayList<LevelItem> l = new ArrayList<>();
        l.add(obj);
        return l;
    }
}
