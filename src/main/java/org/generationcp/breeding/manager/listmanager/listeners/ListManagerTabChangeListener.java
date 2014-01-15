package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.ListManagerSearchListsComponent;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TextField;

public class ListManagerTabChangeListener implements TabSheet.SelectedTabChangeListener {

	private static final long serialVersionUID = -5338521605776826282L;
	
	private Object source;

	public ListManagerTabChangeListener(Object source) {
		super();
		this.source = source;
	}

	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		if (source instanceof ListManagerMain){
			focusSearchBoxIfTabSelected((ListManagerMain) source, event.getTabSheet());
		}
		
	}
	
	/*
	 * Focus search box if Search Lists tab selected in List Manager
	 */
	private void focusSearchBoxIfTabSelected(ListManagerMain listManagerMain, TabSheet tabSheet){
		ListManagerSearchListsComponent searchListsComponent = listManagerMain.getListManagerSearchListsComponent();
		if (searchListsComponent != null && searchListsComponent.equals(tabSheet.getSelectedTab())){
			TextField searchField = searchListsComponent.getSearchTextfield();
			if (searchField != null){
				searchField.focus();
			}
		}
	}

}
