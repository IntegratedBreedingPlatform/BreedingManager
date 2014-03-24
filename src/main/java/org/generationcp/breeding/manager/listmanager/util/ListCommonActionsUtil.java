package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class ListCommonActionsUtil {
	
	// so class cannot be instantiated
	private ListCommonActionsUtil(){
	}
	
	public static void deleteGermplasmList(GermplasmListManager germplasmListManager, 
			GermplasmList germplasmList, WorkbenchDataManager workbenchDataManager, 
			Window window, SimpleResourceBundleMessageSource messageSource, String item) throws MiddlewareQueryException{
		
		germplasmListManager.deleteGermplasmList(germplasmList);
        
        User user = (User) workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
        ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()), 
                workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()), 
                "Deleted a germplasm list.", 
                "Deleted germplasm list with id = "+germplasmList.getId()+" and name = "+germplasmList.getName()+".",
                user,
                new Date());
        workbenchDataManager.addProjectActivity(projAct);
        
        MessageNotifier.showMessage(window,
                messageSource.getMessage(Message.SUCCESS), 
                messageSource.getMessage(Message.SUCCESSFULLY_DELETED_ITEM, item), Notification.POSITION_CENTERED);      

	}
	
	/**
     * Iterates through the whole table, gets selected item GID's, make sure it's sorted as seen on the UI
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> getSelectedGidsFromListDataTable(Table table, String GIDItemId){
        List<Integer> itemIds = new ArrayList<Integer>();
        List<Integer> selectedItemIds = new ArrayList<Integer>();
        List<Integer> trueOrderedSelectedGIDs = new ArrayList<Integer>();
        
        selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
        itemIds.addAll((Collection<Integer>) table.getItemIds());
    
        for(Integer itemId: itemIds){
            if(selectedItemIds.contains(itemId)){
                Integer gid = Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDItemId).getValue()).getCaption().toString());
                trueOrderedSelectedGIDs.add(gid);
            }
        }
        
        return trueOrderedSelectedGIDs;
    }

}
