package org.generationcp.breeding.manager.crossingmanager.listeners;

import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
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
	public void updateUIForDeletedList(GermplasmList list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUIForRenamedList(GermplasmList list, String newName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openListDetails(GermplasmList list) {
		if(saveListAsDialog != null && !list.getType().equals("FOLDER")){
			saveListAsDialog.getDetailsComponent().setGermplasmListDetails(list);
		}
	}

	@Override
	public void folderClicked(GermplasmList list){
		if(saveListAsDialog != null){
			//Check also if folder is clicked (or list is null == central/local folders)
			if((list!=null && list.getType().equals("FOLDER")) || list==null){
				//Check if list old (with ID), if so, remove list details 
				if(saveListAsDialog.getDetailsComponent().getCurrentGermplasmList()!=null 
						&& saveListAsDialog.getDetailsComponent().getCurrentGermplasmList().getId()!=null)
					saveListAsDialog.getDetailsComponent().setGermplasmListDetails(null);
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
