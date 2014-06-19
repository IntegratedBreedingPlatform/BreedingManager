package org.generationcp.breeding.manager.crossingmanager.listeners;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
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

@Configurable
public class SelectTreeItemOnSaveListener extends AbsoluteLayout 
	        implements InitializingBean, InternationalizableComponent, ListTreeActionsListener {
	
	private static final long serialVersionUID = 1L;
	private SaveListAsDialog saveListAsDialog;
	
	public SelectTreeItemOnSaveListener(SaveListAsDialog saveListAsDialog){
		this.saveListAsDialog = saveListAsDialog;
	}
	
	@Override
	public void updateUIForRenamedList(GermplasmList list, String newName) {
		BreedingManagerApplication breedingManagerApplication = (BreedingManagerApplication) saveListAsDialog.getApplication(); 
    	ListManagerMain listManagerMain = breedingManagerApplication.getListManagerMain();
   		listManagerMain.getListSelectionComponent().updateUIForRenamedList(list, newName);
   		
   		ListNameField listNameField = saveListAsDialog.getListDetailsComponent().getListNameField();
   		listNameField.getListNameValidator().setCurrentListName(newName);
   		listNameField.setValue(newName);
   		listNameField.setListNameValidator(listNameField.getListNameValidator());
   		
   		saveListAsDialog.getGermplasmListTree().reloadTreeItemDescription();
	}

//	public void updateUIForRenamedList(GermplasmList list, String newName) {
//		this.listSelectionLayout.renameTab(list.getId(), newName);
//	}
//	
//    public void renameTab(Integer listId, String newName){
//        String tabDescription = generateTabDescription(listId);
//        Tab tab = Util.getTabWithDescription(detailsTabSheet, tabDescription);
//        if (tab != null){
//            tab.setCaption(newName);
//            ListTabComponent listDetails = (ListTabComponent) tab.getComponent();
//            listDetails.setListNameLabel(newName);
//        }
//    }
	
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
	
}
