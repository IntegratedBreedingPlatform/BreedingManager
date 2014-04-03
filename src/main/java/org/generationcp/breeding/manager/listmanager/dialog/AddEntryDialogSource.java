package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.List;



public interface AddEntryDialogSource{

    public void finishAddingEntry(Integer gid);
    
    public void finishAddingEntry(List<Integer> gids);
   
}
