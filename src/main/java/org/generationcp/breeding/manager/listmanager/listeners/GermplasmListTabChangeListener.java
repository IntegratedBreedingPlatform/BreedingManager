package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.ListManagerTreeMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

@Deprecated
public class GermplasmListTabChangeListener implements TabSheet.SelectedTabChangeListener{
    
	@SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTabChangeListener.class);
    private static final long serialVersionUID = -5145904396164706110L;

    private Object source;

    public GermplasmListTabChangeListener(Object source) {
        this.source = source;
    }
	@Override
    public void selectedTabChange(SelectedTabChangeEvent event){

		if (this.source instanceof ListManagerTreeMenu){
			ListManagerTreeMenu treeMenu = (ListManagerTreeMenu) source;
			treeMenu.refreshListData();
			
		}
	}
}
