package org.generationcp.breeding.manager.listmanager.listeners;

import java.util.List;

import org.generationcp.breeding.manager.listmanager.BuildNewListComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window.Notification;

public class SaveListButtonClickListener implements Button.ClickListener{

	private static final long serialVersionUID = -2641642996209640461L;
	
	private static final Logger LOG = LoggerFactory.getLogger(SaveListButtonClickListener.class);

	private BuildNewListComponent source;
	private GermplasmListManager dataManager;
	private WorkbenchDataManager workbenchDataManager;
	private Table listDataTable;
	
	private SimpleResourceBundleMessageSource messageSource;
	
	public SaveListButtonClickListener(BuildNewListComponent source, GermplasmListManager dataManager, Table listDataTable
			, SimpleResourceBundleMessageSource messageSource, WorkbenchDataManager workbenchDataManager){
		this.source = source;
		this.dataManager = dataManager;
		this.listDataTable = listDataTable;
		this.messageSource = messageSource;
		this.workbenchDataManager = workbenchDataManager;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		GermplasmList currentlySavedList = this.source.getCurrentlySavedGermplasmList();
		GermplasmList listToSave = this.source.getCurrentlySetGermplasmListInfo();
		List<GermplasmListData> listEntries = this.source.getListEntriesFromTable();
		
		if(listEntries.isEmpty()){
			MessageNotifier.showError(this.source.getWindow(), "Invalid Input"
					, "There are no list entries. Please add some entries to the table first.", Notification.POSITION_CENTERED);
			return;
		}
		
		if(!validateListDetails(listToSave)){
			return;
		}
		
		if(currentlySavedList != null){
			listToSave.setStatus(Integer.valueOf(1));
			listToSave.setParent(null);
			listToSave.setUserId(getLocalIBDBUserId());
			
			try{
				Integer listId = this.dataManager.addGermplasmList(listToSave);
				
				if(listId != null){
					GermplasmList listSaved = this.dataManager.getGermplasmListById(listId);
					currentlySavedList = listSaved;
					this.source.setCurrentlySavedGermplasmList(listSaved);
				} else{
					MessageNotifier.showError(this.source.getWindow(), "Database Error!", "Error with saving germplasm list. Please report to IBP.", Notification.POSITION_CENTERED);
					return;
				}
			} catch(MiddlewareQueryException ex){
				LOG.error("Error in saving germplasm list: " + listToSave, ex);
				MessageNotifier.showError(this.source.getWindow(), "Database Error!", "Error with saving germplasm list. Please report to IBP.", Notification.POSITION_CENTERED);
				return;
			}
			
			for(GermplasmListData listEntry : listEntries){
				listEntry.setList(currentlySavedList);
				listEntry.setStatus(Integer.valueOf(0));
				listEntry.setLocalRecordId(Integer.valueOf(0));
			}
			
			try{
				this.dataManager.addGermplasmListData(listEntries);
				
				//TODO update list data table
			} catch(MiddlewareQueryException ex){
				LOG.error("Error in saving germplasm list entries.", ex);
				MessageNotifier.showError(this.source.getWindow(), "Database Error!", "Error with saving germplasm list entries. Please report to IBP.", Notification.POSITION_CENTERED);
				return;
			}
		}
		
		
	}
	
	private boolean validateListDetails(GermplasmList list){
		if(list.getName() == null){
			MessageNotifier.showError(this.source.getWindow(), "Invalid Input", "Name can not be blank. Please enter a name.", Notification.POSITION_CENTERED);
			return false;
		} else if(list.getDescription() == null){
			MessageNotifier.showError(this.source.getWindow(), "Invalid Input", "Description can not be blank. Please enter a description.", Notification.POSITION_CENTERED);
			return false;
		} else if(list.getName().length() > 50){
			MessageNotifier.showError(this.source.getWindow(), "Invalid Input", "Name can not be longer than 50 characters.", Notification.POSITION_CENTERED);
			return false;
		} else if(list.getDescription().length() > 255){
			MessageNotifier.showError(this.source.getWindow(), "Invalid Input", "Description can not be longer than 255 characters.", Notification.POSITION_CENTERED);
			return false;
		}
		return true;
	}
	
	private Integer getLocalIBDBUserId(){
		try{
			WorkbenchRuntimeData runtimeData = this.workbenchDataManager.getWorkbenchRuntimeData();
			Project project = this.workbenchDataManager.getLastOpenedProject(runtimeData.getUserId());
			return this.workbenchDataManager.getLocalIbdbUserId(runtimeData.getUserId(), project.getProjectId());
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting the local IBDB user ID of the currently logged in workbench user.", ex);
			MessageNotifier.showError(this.source.getWindow(), "Database Error!", "Error with getting local IBDB user id. Please report to IBP.", Notification.POSITION_CENTERED);
			return null;
		}
	}
}
