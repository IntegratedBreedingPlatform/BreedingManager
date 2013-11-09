package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.ListManagerTreeMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class GermplasmListTabChangeListener implements TabSheet.SelectedTabChangeListener{
    
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTabChangeListener.class);
    private static final long serialVersionUID = -5145904396164706110L;

    private ListManagerTreeMenu treeMenu;

    public GermplasmListTabChangeListener(ListManagerTreeMenu treeMenu) {
        this.treeMenu = treeMenu;
    }
	@Override
    public void selectedTabChange(SelectedTabChangeEvent event){
		treeMenu.refreshListData();
	}
}
