/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dirbaio.nsmbe.leveleditor.actions;

import java.util.ArrayList;
import net.dirbaio.nsmbe.level.LevelItem;
import net.dirbaio.nsmbe.level.NSMBSprite;
import net.dirbaio.nsmbe.util.LanguageManager;

public class ChangeSpriteTypeAction extends LvlItemAction
{

    ArrayList<Integer> OrigType;
    int NewType;

    public ChangeSpriteTypeAction(ArrayList<LevelItem> objs, int NewType)
    {
        super(objs);
        OrigType = new ArrayList<>();
        for (int l = 0; l < this.objs.size(); l++)
            if (this.objs.get(l) instanceof NSMBSprite)
                OrigType.add(((NSMBSprite) this.objs.get(l)).Type);
            else
            {
                this.objs.remove(l);
                l--;
            }
        this.NewType = NewType;
    }

    @Override
    public void Undo()
    {
        for (int l = 0; l < objs.size(); l++)
            ((NSMBSprite) objs.get(l)).Type = OrigType.get(l);
    }

    @Override
    public void Redo()
    {
        for (LevelItem obj : objs)
            ((NSMBSprite) obj).Type = NewType;
    }

    @Override
    public boolean CanMerge(Action act)
    {
        return act instanceof ChangeSpriteTypeAction && sameItems(act);
    }

    @Override
    public void Merge(Action act)
    {
        ChangeSpriteTypeAction csta = (ChangeSpriteTypeAction) act;
        this.NewType = csta.NewType;
    }

    @Override
    public void AfterAction()
    {
        //if (EdControl.mode instanceof ObjectsEditionMode)
        //    ((ObjectsEditionMode) EdControl.mode).tabs.sprites.UpdateDataEditor();
    }

    @Override
    public String toString()
    {
        return LanguageManager.GetArrayList("UndoActions")[8];
    }
}
