package org.generationcp.breeding.manager.listmanager.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Deque;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.GermplasmListSource;
import org.generationcp.breeding.manager.customcomponent.handler.GermplasmListSourceDropHandler;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.util.UserUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

@Configurable
public class GermplasmListTreeUtil implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTreeUtil.class);

	private ListSelectorComponent source;
	private GermplasmListSource targetListSource;

	public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";
	
    @Autowired
    private GermplasmListManager germplasmListManager;
    @Autowired
    private UserDataManager userDataManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    public GermplasmListTreeUtil(){
		
	}
    
	public GermplasmListTreeUtil(ListSelectorComponent source, GermplasmListSource targetListSource){
		this.source = source;
		this.targetListSource = targetListSource;
		setupTreeDragAndDropHandler();
	}
	
    public boolean setParent(Object sourceItemId, Object targetItemId){

    	if(sourceItemId.equals(ListSelectorComponent.LISTS)){
    		MessageNotifier.showWarning(source.getWindow(), 
                    messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE), 
                    messageSource.getMessage(Message.UNABLE_TO_MOVE_ROOT_FOLDERS));
    		return false;
    	}
    	
    	Integer sourceId = null;
    	Integer targetId = null;
    	
    	if(sourceItemId!=null && !sourceItemId.equals(ListSelectorComponent.LISTS)){
    		sourceId = Integer.valueOf(sourceItemId.toString());
    	}
    	if(targetItemId!=null && !targetItemId.equals(ListSelectorComponent.LISTS)){
    		targetId = Integer.valueOf(targetItemId.toString());
    	}
		
		//Apply to back-end data    	
    	try {
            GermplasmList sourceGermplasmList = germplasmListManager.getGermplasmListById(sourceId);
            if (targetId != null) {
                GermplasmList targetGermplasmList = germplasmListManager.getGermplasmListById(targetId);
                sourceGermplasmList.setParent(targetGermplasmList);
            } else {
            	sourceGermplasmList.setParent(null);
            }
            germplasmListManager.updateGermplasmList(sourceGermplasmList);
            
		} catch (MiddlewareQueryException e) {
			MessageNotifier.showError(source.getWindow(), 
                    messageSource.getMessage(Message.ERROR_INTERNAL), 
                    messageSource.getMessage(Message.ERROR_REPORT_TO));
			LOG.error("Error with update the germplasm list.", e);
		}
        
        //apply to UI
        if(targetItemId==null || targetListSource.getItem(targetItemId)==null){
            targetListSource.setChildrenAllowed(sourceItemId, true);
            targetListSource.setParent(sourceItemId, ListSelectorComponent.LISTS);
            targetListSource.expandItem(ListSelectorComponent.LISTS);
		} else {
            targetListSource.setChildrenAllowed(targetItemId, true);
            targetListSource.setParent(sourceItemId, targetItemId);
            targetListSource.expandItem(targetItemId);
		}
        
        source.setSelectedListId(sourceItemId);
        targetListSource.select(sourceItemId);
        targetListSource.setValue(sourceItemId);
        return true;
    }

    public void setupTreeDragAndDropHandler(){
        targetListSource.setDropHandler(new GermplasmListSourceDropHandler(targetListSource, source, this));
    }
    
    public void addFolder(final Object parentItemId, final TextField folderTextField){
    	
    	Integer newFolderId = null;
    	GermplasmList newFolder = new GermplasmList();
    	GermplasmList parentList = null;
    	
		try {
			folderTextField.validate();
			String folderName = folderTextField.getValue().toString().trim();
			
			try {
				Integer ibdbUserId = UserUtil.getCurrentUserLocalId(workbenchDataManager);
				
				newFolder.setName(folderName);
				newFolder.setDescription(folderName);
				newFolder.setType("FOLDER");
				newFolder.setStatus(0);
				newFolder.setUserId(ibdbUserId);
				newFolder.setDate(Long.valueOf((new SimpleDateFormat(DATE_AS_NUMBER_FORMAT)).format(Calendar.getInstance().getTime())));
				
				if (parentItemId==null || parentItemId instanceof String || targetListSource.getItem(parentItemId)==null) {
					newFolder.setParent(null);
				} else if (!source.isFolder(parentItemId)) {
					parentList = germplasmListManager.getGermplasmListById((Integer) parentItemId);
					newFolder.setParent(germplasmListManager.getGermplasmListById(parentList.getParentId()));
				} else {
					newFolder.setParent(germplasmListManager.getGermplasmListById((Integer) parentItemId));
				}
				
				newFolderId = germplasmListManager.addGermplasmList(newFolder);
				
			} catch (MiddlewareQueryException e) {
				MessageNotifier.showError(source.getWindow(), 
						messageSource.getMessage(Message.ERROR_INTERNAL), 
						messageSource.getMessage(Message.ERROR_REPORT_TO));
				LOG.error("Error with adding the new germplasm list.",e);
			}

			//update UI
			addFolderToTree(parentItemId, folderName, newFolderId, newFolder, parentList);
			
			
		} catch (InvalidValueException e) {
			MessageNotifier.showRequiredFieldError(source.getWindow(), e.getMessage());
			LOG.error("Error adding new folder.",e);
		}
    }

    public String renameFolderOrList(final Integer listId, final ListTreeActionsListener listener, TextField folderTextField, String oldName){
    	
    	String newName = folderTextField.getValue().toString().trim();
    	if (newName.equals(oldName)){
    		source.showAddRenameFolderSection(false);
    		return "";
    	}
    	
    	try {
    		folderTextField.validate();
    			
			GermplasmList germplasmList;
			try {
				germplasmList = germplasmListManager.getGermplasmListById(listId);
				
				germplasmList.setName(newName);
				germplasmListManager.updateGermplasmList(germplasmList);

                targetListSource.setItemCaption(listId, newName);
                targetListSource.select(listId);
				
				//rename tabs
				if (listener != null){
					listener.updateUIForRenamedList(germplasmList, newName);
				}
				
				source.showAddRenameFolderSection(false);
				//dennis
				source.assignNewNameToGermplasmListMap(listId.toString(), newName);
				source.refreshRemoteTree();
				if(source.getWindow() != null){
					MessageNotifier.showMessage(source.getWindow(), messageSource.getMessage(Message.SUCCESS), "Item renamed successfully.");
				}
				return targetListSource.getItemCaption(listId);
			} catch (MiddlewareQueryException e) {
				MessageNotifier.showWarning(source.getWindow(),
						messageSource.getMessage(Message.ERROR_DATABASE), 
						messageSource.getMessage(Message.ERROR_REPORT_TO));
				LOG.error(e.getMessage(),e);
			}

		} catch (InvalidValueException e) {
			MessageNotifier.showRequiredFieldError(source.getWindow(), e.getMessage());
			LOG.error(e.getMessage(),e);
		}
    	return "";
    }

	public void deleteFolderOrList(final ListSelectorComponent listSelectorComponent, final Integer lastItemId,
			final ListTreeActionsListener listener) {
		
		final Window mainWindow;
        if (source.usedInSubWindow()){
        	mainWindow = source.getWindow().getParent();
        } else {        	
        	mainWindow = source.getWindow();   	
        }
        
        try{
        	validateItemToDelete(lastItemId);	
        } catch (InvalidValueException e) {
        	MessageNotifier.showRequiredFieldError(mainWindow, e.getMessage());
        	LOG.error("Error validation for deleting a list.", e);
        	return;
		}
    
		final GermplasmList finalGpList = getGermplasmList(lastItemId);
		ConfirmDialog.show(mainWindow,
			messageSource.getMessage(Message.DELETE_ITEM),
			messageSource.getMessage(Message.DELETE_ITEM_CONFIRM),
			messageSource.getMessage(Message.YES),
			messageSource.getMessage(Message.NO), 
			new ConfirmDialog.Listener() {
				private static final long serialVersionUID = -6164460688355101277L;
				@Override
				public void onClose(ConfirmDialog dialog) {
					if (dialog.isConfirmed()) {
						try {
							ListCommonActionsUtil.deleteGermplasmList(germplasmListManager, finalGpList, 
									workbenchDataManager, source.getWindow(), messageSource, "item");
							listSelectorComponent.removeListFromTree(finalGpList);
							source.refreshRemoteTree();
							((BreedingManagerApplication) mainWindow.getApplication()).updateUIForDeletedList(finalGpList);
						} catch (MiddlewareQueryException e) {
							MessageNotifier.showError(mainWindow, messageSource.getMessage(Message.INVALID_OPERATION) , e.getMessage());
							LOG.error("Error with deleting a germplasm list.", e);
						}
					}
				}
		});
		
	}    
	
    protected void validateItemToDelete(Integer itemId) {
    	GermplasmList gpList = getGermplasmList(itemId);
    	
    	validateIfItemExist(itemId,gpList);
    	validateItemByStatusAndUser(gpList);
    	validateItemIfItIsAFolderWithContent(gpList);
	}

    private void validateIfItemExist(Integer itemId, GermplasmList gpList) {
    	if (itemId == null) {
    		throw new InvalidValueException(messageSource.getMessage(Message.ERROR_NO_SELECTION));
		}
    	
    	if(!doesGermplasmListExist(gpList)){
    		throw new InvalidValueException(messageSource.getMessage(Message.ERROR_ITEM_DOES_NOT_EXISTS));
    	}
	}
    
    private boolean doesGermplasmListExist(GermplasmList germplasmList) {
		if (germplasmList == null) {
			return false;
		}
		return true;
	}
    
    private void validateItemByStatusAndUser(GermplasmList gpList) {
    	if(isListLocked(gpList)){
    		throw new InvalidValueException(messageSource.getMessage(Message.ERROR_UNABLE_TO_DELETE_LOCKED_LIST));
    	}
    	
    	if(!isListOwnedByTheUser(gpList)){
    		throw new InvalidValueException(messageSource.getMessage(Message.ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER));
    	}
	}
    
    private boolean isListOwnedByTheUser(GermplasmList gpList) {
		try {
			Integer ibdbUserId = UserUtil.getCurrentUserLocalId(workbenchDataManager);
			if (!gpList.getUserId().equals(ibdbUserId)) {
				return false;
			}
		} catch (MiddlewareQueryException e) {
			LOG.error("Error retrieving workbench user id.", e);
		}
		return true;
	}
    
    private boolean isListLocked(GermplasmList gpList) {
    	if (gpList != null && gpList.getStatus()>100){
			return true;
		}
		return false;
	}
    
    private void validateItemIfItIsAFolderWithContent(GermplasmList gpList) {
    	try {
			if (hasChildren(gpList.getId())) {
				throw new InvalidValueException(messageSource.getMessage(Message.ERROR_HAS_CHILDREN));
			}
		} catch (MiddlewareQueryException e) {
			LOG.error("Error retrieving children items of a parent item.", e);
		}
	}
    
	public boolean hasChildren(Integer id) throws MiddlewareQueryException {
        return !germplasmListManager.getGermplasmListByParentFolderId(id,0,Integer.MAX_VALUE).isEmpty();
    }

    private GermplasmList getGermplasmList(Integer itemId) {
		GermplasmList gpList = null;
		
		try {
			gpList = germplasmListManager.getGermplasmListById(itemId);
		} catch (MiddlewareQueryException e) {
			LOG.error("Error retrieving germplasm list by Id.", e);
		}
		
		return gpList;
	}
    
    public static void traverseParentsOfList(GermplasmListManager germplasmListManager, GermplasmList list, Deque<GermplasmList> parents) throws MiddlewareQueryException{
    	if(list == null){
    		return;
    	} else{
    		Integer parentId = list.getParentId();
    		
    		if(parentId != null && parentId != 0){
	    		GermplasmList parent = germplasmListManager.getGermplasmListById(list.getParentId());
	    		
	    		if(parent != null){
	    			parents.push(parent);
	    			traverseParentsOfList(germplasmListManager, parent, parents);
	    		}
    		}
    		
    		return;
    	}
    }

    public void addFolderToTree(final Object parentItemId,
                                String folderName, Integer newFolderId,
                                GermplasmList newFolder, GermplasmList parentList) {
        if (newFolderId != null) {
            targetListSource.addItem(source.generateCellInfo(folderName, BreedingManagerUtil.getOwnerListName(newFolder.getUserId(), userDataManager), BreedingManagerUtil.getDescriptionForDisplay(newFolder), BreedingManagerUtil.getTypeString(newFolder.getType(), germplasmListManager), ""), newFolderId);
            source.setNodeItemIcon(newFolderId, true);
            targetListSource.setItemCaption(newFolderId, folderName);
            targetListSource.setChildrenAllowed(newFolderId, true);

            source.setSelectedListId(newFolderId);

            //If parent of list does not exist
            if (parentList==null && !source.isFolder(parentItemId)){
                targetListSource.setChildrenAllowed(ListSelectorComponent.LISTS, true);
                targetListSource.setParent(newFolderId, ListSelectorComponent.LISTS);
                //If parent of list is root node
            } else if (parentList!=null && !source.isFolder(parentItemId) && (parentList.getParentId()==null || parentList.getParentId()==0)){
                targetListSource.setChildrenAllowed(ListSelectorComponent.LISTS, true);
                targetListSource.setParent(newFolderId, ListSelectorComponent.LISTS);
                //If folder
            } else if (newFolder.getParent() != null && targetListSource.getItem(parentItemId)!=null && source.isFolder(parentItemId)) {
                targetListSource.setChildrenAllowed(parentItemId, true);
                Boolean parentSet = targetListSource.setParent(newFolderId, parentItemId);
                if(!parentSet){
                    parentSet = targetListSource.setParent(newFolderId, ListSelectorComponent.LISTS);
                }
                //If list, add to parent
            } else if (newFolder.getParent() != null && targetListSource.getItem(parentItemId)!=null) {
                targetListSource.setChildrenAllowed(parentList.getParentId(), true);
                targetListSource.setParent(newFolderId, parentList.getParentId());
                //All else, add to LOCAL list
            } else {
                targetListSource.setChildrenAllowed(ListSelectorComponent.LISTS, true);
                targetListSource.setParent(newFolderId, ListSelectorComponent.LISTS);
            }

            if (targetListSource.getValue() != null) {
                if (!targetListSource.isExpanded(targetListSource.getValue())){
                    targetListSource.expandItem(parentItemId);
                }
            } else {
                targetListSource.expandItem(ListSelectorComponent.LISTS);
            }

            targetListSource.select(newFolderId);
            source.updateButtons(newFolderId);
            source.showAddRenameFolderSection(false);
            source.refreshRemoteTree();
            MessageNotifier.showMessage(source.getWindow(), messageSource.getMessage(Message.SUCCESS), "Folder saved successfully.");
        }
    }

	public void setSource(ListSelectorComponent source) {
		this.source = source;
	}

	public void setTargetListSource(GermplasmListSource targetListSource) {
		this.targetListSource = targetListSource;
	}

	public void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}
	
	public void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager){
		this.workbenchDataManager = workbenchDataManager;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
    
}
