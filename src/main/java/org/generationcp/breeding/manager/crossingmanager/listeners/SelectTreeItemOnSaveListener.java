package org.generationcp.breeding.manager.crossingmanager.listeners;

import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customfields.ListNameField;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;

@Configurable
public class SelectTreeItemOnSaveListener extends AbsoluteLayout 
	        implements InitializingBean, InternationalizableComponent, ListTreeActionsListener {
	
	private static final long serialVersionUID = 1L;
	private SaveListAsDialog saveListAsDialog;
	private Component parentComponent;
	
	public SelectTreeItemOnSaveListener(SaveListAsDialog saveListAsDialog, Component parentComponent){
		this.saveListAsDialog = saveListAsDialog;
		this.parentComponent = parentComponent;
	}
	
	@Override
	public void updateUIForRenamedList(GermplasmList list, String newName) {
		System.out.println("parentCOmponent: " + parentComponent);
    	if(parentComponent instanceof ListManagerMain){
    		ListManagerMain listManagerMain = (ListManagerMain)parentComponent;
    				listManagerMain.getListSelectionComponent().updateUIForRenamedList(list, newName);
    	}
    	
    	if(parentComponent instanceof ManageCrossingSettingsMain){
    		ManageCrossingSettingsMain manageCrossingSettingsMain = (ManageCrossingSettingsMain)parentComponent;
    		manageCrossingSettingsMain.getMakeCrossesComponent().getSelectParentsComponent().updateUIForRenamedList(list, newName);
    	}
    	
   		ListNameField listNameField = saveListAsDialog.getListDetailsComponent().getListNameField();
   		listNameField.getListNameValidator().setCurrentListName(newName);
   		listNameField.setValue(newName);
   		listNameField.setListNameValidator(listNameField.getListNameValidator());
   		
   		saveListAsDialog.getGermplasmListTree().reloadTreeItemDescription();
	}
	
	@Override
	public void openListDetails(GermplasmList list) {
		if(saveListAsDialog != null && !list.getType().equals("FOLDER")){
			saveListAsDialog.getDetailsComponent().setGermplasmListDetails(list);
			
			if(saveListAsDialog.getSource() instanceof ListBuilderComponent){
				ListBuilderComponent LBC = (ListBuilderComponent) saveListAsDialog.getSource();
				LBC.getSaveListButtonListener().setForceHasChanges(true);
			}

		}
	}

	@Override
	public void folderClicked(GermplasmList list){
		if(saveListAsDialog != null){
			//Check also if folder is clicked (or list is null == central/local folders)
			if((list!=null && list.getType().equals("FOLDER")) || list==null){
				//Check if list old (with ID), if so, remove list details 
				if(saveListAsDialog.getDetailsComponent().getCurrentGermplasmList()!=null 
						&& saveListAsDialog.getDetailsComponent().getCurrentGermplasmList().getId()!=null){
					saveListAsDialog.getDetailsComponent().setGermplasmListDetails(null);
					saveListAsDialog.setGermplasmList(null);//reset also the current list to save
				}	
			} else {
				saveListAsDialog.getDetailsComponent().setGermplasmListDetails(list);
			}
		}
	}
	
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListToFemaleList(Integer germplasmListId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListToMaleList(Integer germplasmListId) {
		// TODO Auto-generated method stub
		
	}
	
}
