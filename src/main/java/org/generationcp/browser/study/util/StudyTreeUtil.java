package org.generationcp.browser.study.util;

import java.io.Serializable;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.exception.GermplasmStudyBrowserException;
import org.generationcp.browser.study.StudyTreeComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeTargetDetails;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class StudyTreeUtil implements Serializable {
	private static final long serialVersionUID = -4427723835290060592L;
	private static final int STUDY_NAME_LIMITS = 255;
	private final static Logger LOG = LoggerFactory.getLogger(StudyTreeUtil.class);
	
	private static final String NO_SELECTION = "Please select a folder item";
    public final static String NOT_FOLDER = "Selected item is not a folder.";
    public final static String NO_PARENT = "Selected item is a root item, please choose another item on the tree";
    public final static String HAS_CHILDREN = "Folder has child items.";

	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private StudyDataManager studyDataManager;
	
	private Tree targetTree;
	private StudyTreeComponent source;
	
	public StudyTreeUtil(Tree targetTree, StudyTreeComponent source){
		this.targetTree = targetTree;
		this.source = source;
		setupTreeDragAndDropHandler();
	}

	public void addFolder(final Object parentItemId){
    	
        final Window w = new Window("Add new folder");
        w.setWidth("320px");
        w.setHeight("160px");
        w.setModal(true);
        w.setResizable(false);
        w.setStyleName(Reindeer.WINDOW_LIGHT);

        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setMargin(true);

        HorizontalLayout formContainer = new HorizontalLayout();
        formContainer.setSpacing(true);

        Label l = new Label("Folder Name");
        l.addStyleName("gcp-form-title");
        final TextField name = new TextField();
        name.setMaxLength(50);
        name.setWidth("190px");
        name.focus();

        formContainer.addComponent(l);
        formContainer.addComponent(name);

        HorizontalLayout btnContainer = new HorizontalLayout();
        btnContainer.setSpacing(true);
        btnContainer.setWidth("100%");

        Label spacer = new Label("");
        btnContainer.addComponent(spacer);
        btnContainer.setExpandRatio(spacer, 1.0F);

        Button ok = new Button("Ok");
        ok.setClickShortcut(KeyCode.ENTER);
        ok.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        ok.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = -6313787074401316900L;

			@Override
            public void buttonClick(Button.ClickEvent event) {
                Integer newFolderId = null;
                String newFolderName = name.getValue().toString();
                int parentFolderId = 1;	//1 by default because root study folder has id = 1
                
                try{
                	if (!isValidNameInput(name.getValue().toString())){
                    	return;
                    }
                	
                	if(parentItemId != null && parentItemId instanceof Integer){
                		if(source.isFolder((Integer) parentItemId)){
                			parentFolderId = ((Integer) parentItemId).intValue();
                		} else{
                			int selectItemId = ((Integer) parentItemId).intValue();
                			DmsProject parentFolder = studyDataManager.getParentFolder(selectItemId);
                			parentFolderId = parentFolder.getProjectId().intValue();
                		}
                	}
                	
                	newFolderId = Integer.valueOf(studyDataManager.addSubFolder(parentFolderId, newFolderName, newFolderName));
                } catch(MiddlewareQueryException ex){
                	LOG.error("Error with adding a study folder.", ex);
                	MessageNotifier.showError(source.getWindow(),
                            messageSource.getMessage(Message.ERROR_DATABASE), 
                            messageSource.getMessage(Message.PLEASE_SEE_ERROR_LOG));
                	return;
                }
                
                //update UI
                if (newFolderId != null) {
                    targetTree.addItem(newFolderId);
                    targetTree.setItemCaption(newFolderId, newFolderName);
                    targetTree.setItemIcon(newFolderId, new ThemeResource("../vaadin-retro/svg/folder-icon.svg"));
                    targetTree.setChildrenAllowed(newFolderId, true);
                    
                    source.setSelectedStudyTreeNodeId(newFolderId);
    		        
                    if(parentFolderId == 1){
                    	targetTree.setChildrenAllowed(StudyTreeComponent.LOCAL, true);
    		        	targetTree.setParent(newFolderId, StudyTreeComponent.LOCAL);
                    } else{
                    	targetTree.setChildrenAllowed(Integer.valueOf(parentFolderId), true);
    		        	targetTree.setParent(newFolderId, Integer.valueOf(parentFolderId));
                    }
                    
                    if (targetTree.getValue() != null) {
                        if (!targetTree.isExpanded(targetTree.getValue()))
                            targetTree.expandItem(parentItemId);
                    } else {
                    	targetTree.expandItem(StudyTreeComponent.LOCAL);
					}
                    
                    targetTree.select(newFolderId);
                    source.updateButtons(newFolderId);
                }

                // close popup
                source.getParentComponent().getWindow().removeWindow(event.getComponent().getWindow());
            }


        });

        Button cancel = new Button("Cancel");
        cancel.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = -6542741100092010158L;

			@Override
            public void buttonClick(Button.ClickEvent event) {
            	source.getParentComponent().getWindow().removeWindow(w);
            }
        });

        btnContainer.addComponent(ok);
        btnContainer.addComponent(cancel);

        container.addComponent(formContainer);
        container.addComponent(btnContainer);

        w.setContent(container);

        // show window
        source.getParentComponent().getWindow().addWindow(w);    	
    }
	
	private boolean isValidNameInput(String newFolderName) throws MiddlewareQueryException {
		if(newFolderName.replace(" ","").equals("")){
        	MessageNotifier.showError(source.getWindow(),
                    messageSource.getMessage(Message.INVALID_INPUT), 
                    messageSource.getMessage(Message.INVALID_ITEM_NAME));
        	return false;
        	
        } else if(newFolderName.length() > STUDY_NAME_LIMITS){
        	MessageNotifier.showError(source.getWindow(),
                    messageSource.getMessage(Message.INVALID_INPUT), 
                    messageSource.getMessage(Message.INVALID_LONG_STUDY_FOLDER_NAME));
        	return false;
        	
        } else if(studyDataManager.checkIfProjectNameIsExisting(newFolderName)){
    		MessageNotifier.showError(source.getWindow(),
                    messageSource.getMessage(Message.INVALID_INPUT), 
                    messageSource.getMessage(Message.EXISTING_STUDY_ERROR_MESSAGE));
        	return false;
    	} else if(newFolderName.toLowerCase().equals(messageSource.getMessage(Message.PROGRAM_STUDIES).toString().toLowerCase()) ||
        		newFolderName.toLowerCase().equals(messageSource.getMessage(Message.PUBLIC_STUDIES).toString().toLowerCase())
        			){
        		MessageNotifier.showError(source.getWindow(),
                	messageSource.getMessage(Message.INVALID_INPUT), 
                	messageSource.getMessage(Message.EXISTING_STUDY_ERROR_MESSAGE));
        		return false;
       	}
		
		return true;
	}
	
	private void setParent(Object sourceItemId, Object targetItemId, boolean isStudy){

    	if(sourceItemId.equals(StudyTreeComponent.LOCAL) || sourceItemId.equals(StudyTreeComponent.CENTRAL)){
    		MessageNotifier.showWarning(source.getWindow(), 
                    messageSource.getMessage(Message.ERROR_WITH_MODIFYING_STUDY_TREE), 
                    messageSource.getMessage(Message.MOVE_ROOT_FOLDERS_NOT_ALLOWED));
    		return;
    	}
    	
    	if(targetItemId!=null && targetItemId.equals(StudyTreeComponent.CENTRAL)){
    		MessageNotifier.showWarning(source.getWindow(), 
                    messageSource.getMessage(Message.ERROR_WITH_MODIFYING_STUDY_TREE), 
                    messageSource.getMessage(Message.MOVE_YOUR_LISTS_TO_PUBLIC_FOLDERS_NOT_ALLOWED));
    		return;
    	}
    	
    	Integer sourceId = null;
    	Integer targetId = null;
    	
    	if(sourceItemId!=null && !sourceItemId.equals(StudyTreeComponent.LOCAL) && !sourceItemId.equals(StudyTreeComponent.CENTRAL))
    		sourceId = Integer.valueOf(sourceItemId.toString());
    	
    	if(targetItemId!=null) {
    		if (StudyTreeComponent.LOCAL.equals(targetItemId)){
    			targetId = 1; // 1 = Local Root folder
    		} else if (!StudyTreeComponent.CENTRAL.equals(targetItemId)){
    			targetId = Integer.valueOf(targetItemId.toString());
    		}
    	}
    	
		if(sourceId!=null && sourceId>0){
			MessageNotifier.showWarning(source.getWindow(), 
					messageSource.getMessage(Message.ERROR_WITH_MODIFYING_STUDY_TREE), 
					messageSource.getMessage(Message.MOVE_PUBLIC_LISTS_NOT_ALLOWED));
			return;
		}    	
	
    	if(targetId!=null && targetId>1){ // 1 = Local Root Folder
    		MessageNotifier.showWarning(source.getWindow(),
                    messageSource.getMessage(Message.ERROR_WITH_MODIFYING_STUDY_TREE), 
                    messageSource.getMessage(Message.MOVE_YOUR_LISTS_TO_PUBLIC_FOLDERS_NOT_ALLOWED));
    		return;
    	}    	
    	
    	
    	try {
    		if (targetId != null && sourceId != null){
    			studyDataManager.moveDmsProject(sourceId.intValue(), targetId.intValue(), isStudy);
    		}
		} catch (MiddlewareQueryException e) {
			LOG.error("Error with moving node to target folder.", e);
			MessageNotifier.showError(source.getWindow(), 
                    messageSource.getMessage(Message.ERROR_INTERNAL), 
                    messageSource.getMessage(Message.ERROR_REPORT_TO));
		}
        
        //apply to UI
        if(targetItemId==null || targetTree.getItem(targetItemId)==null){
        	targetTree.setChildrenAllowed(sourceItemId, true);
        	targetTree.setParent(sourceItemId, StudyTreeComponent.LOCAL);
        	targetTree.expandItem(StudyTreeComponent.LOCAL);
		} else {
			targetTree.setChildrenAllowed(targetItemId, true);
        	targetTree.setParent(sourceItemId, targetItemId);
        	targetTree.expandItem(targetItemId);
		}
        targetTree.select(sourceItemId);
    }
	
	private void setupTreeDragAndDropHandler(){
		targetTree.setDropHandler(new DropHandler() {
			private static final long serialVersionUID = -6676297159926786216L;

			public void drop(DragAndDropEvent dropEvent) {
		        Transferable t = dropEvent.getTransferable();
		        if (t.getSourceComponent() != targetTree)
		            return;
		        
		        TreeTargetDetails target = (TreeTargetDetails) dropEvent.getTargetDetails();
		        
		        Object sourceItemId = t.getData("itemId");
		        Object targetItemId = target.getItemIdOver();
		        
		        VerticalDropLocation location = target.getDropLocation();
		        
		        if (location != VerticalDropLocation.MIDDLE || sourceItemId.equals(targetItemId)){
		        	return;
		        }
		        
		        boolean sourceIsStudy = !source.isFolder((Integer) sourceItemId);
		        if(targetItemId instanceof Integer){
		        	Boolean targetIsFolder = source.isFolder((Integer) targetItemId);
					if(targetIsFolder){
		        		setParent(sourceItemId, targetItemId, sourceIsStudy);
		        	} else{
		        		try{
				        	DmsProject parentFolder = studyDataManager.getParentFolder(((Integer) targetItemId).intValue());
				        	if(parentFolder != null){
				        		if(((Integer) targetItemId).intValue() < 0 && parentFolder.getProjectId().equals(Integer.valueOf(1))){
				        			setParent(sourceItemId, StudyTreeComponent.LOCAL, sourceIsStudy);
				        		} else{
				        			setParent(sourceItemId, parentFolder.getProjectId(), sourceIsStudy);
				        		}
				        	} else{
				        		setParent(sourceItemId, StudyTreeComponent.LOCAL, sourceIsStudy);
				        	}
		        		} catch (MiddlewareQueryException e) {
		        			LOG.error("Error with getting parent folder of a project record.", e);
		        			MessageNotifier.showError(source.getWindow(), 
		                            messageSource.getMessage(Message.ERROR_INTERNAL), 
		                            messageSource.getMessage(Message.ERROR_REPORT_TO));
		        		}
		        	}
		        } else{
		        	setParent(sourceItemId, targetItemId, sourceIsStudy);
		        }
			}

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
			}
		});
    }
	
	public void renameFolder(final Integer studyId, final String name){

		final Window w = new Window();
		
        w.setCaption(messageSource.getMessage(Message.RENAME_ITEM));
        w.setWidth("320px");
        w.setHeight("160px");
        w.setModal(true);
        w.setResizable(false);
        w.setStyleName(Reindeer.WINDOW_LIGHT);

        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setMargin(true);

        HorizontalLayout formContainer = new HorizontalLayout();
        formContainer.setSpacing(true);

        Label l = new Label(messageSource.getMessage(Message.ITEM_NAME));
        l.addStyleName("gcp-form-title");

        final TextField nameField = new TextField();
        nameField.setMaxLength(50);
        nameField.setValue(name);
        nameField.setCursorPosition(nameField.getValue() == null ? 0 : nameField.getValue().toString().length());
        nameField.setWidth("200px");

        formContainer.addComponent(l);
        formContainer.addComponent(nameField);

        HorizontalLayout btnContainer = new HorizontalLayout();
        btnContainer.setSpacing(true);
        btnContainer.setWidth("100%");

        Label spacer = new Label("");
        btnContainer.addComponent(spacer);
        btnContainer.setExpandRatio(spacer, 1.0F);

        Button ok = new Button("Ok");
        ok.setClickShortcut(KeyCode.ENTER);
        ok.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        ok.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
            public void buttonClick(Button.ClickEvent event) {
                
                try {
                	String newName = nameField.getValue().toString().trim();
                	if (!name.equals(newName)){
                		if (!isValidNameInput(newName)){
                			return;
                		}
                		
                		studyDataManager.renameSubFolder(newName, studyId);
                		
                		targetTree.setItemCaption(studyId, newName);
                		targetTree.select(studyId);
                		
                		//if node is study - rename tab name to new name
                		if (!source.isFolder(studyId)){
                			source.renameStudyTab(name, newName);
                		}
                	}
                	
                } catch (MiddlewareQueryException e) {
                	MessageNotifier.showWarning(source.getWindow(),
                            messageSource.getMessage(Message.ERROR_DATABASE), 
                            messageSource.getMessage(Message.ERROR_REPORT_TO));
                } catch (Error e) {
                	MessageNotifier.showError(source.getWindow(), 
                            messageSource.getMessage(Message.ERROR_INTERNAL), 
                            messageSource.getMessage(Message.ERROR_REPORT_TO));
        			e.printStackTrace();
                    return;
                }

                source.getParentComponent().getWindow().removeWindow(event.getComponent().getWindow());
            }
        });

        Button cancel = new Button("Cancel");
        cancel.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
            public void buttonClick(Button.ClickEvent event) {
            	source.getParentComponent().getWindow().removeWindow(w);
            }
        });

        btnContainer.addComponent(ok);
        btnContainer.addComponent(cancel);

        container.addComponent(formContainer);
        container.addComponent(btnContainer);

        w.setContent(container);

        // show window
        source.getParentComponent().getWindow().addWindow(w);    	
    }
	
	/**
	 * Checks if given id is:
	 * 1. existing in the database
	 * 2. is a folder
	 * 3. does not has have children items
	 * 
	 * If any of the checking failed, throws exception
	 * 
	 * @param id
	 * @throws GermplasmStudyBrowserException
	 */
    public void validateForDeleteNurseryList(Integer id) throws GermplasmStudyBrowserException {
        LOG.info("id = " + id);
        if (id == null) {
            throw new Error(NO_SELECTION);
        }
        DmsProject project = null;

        try {
            project = studyDataManager.getProject(id);

        } catch (MiddlewareQueryException e) {
            throw new GermplasmStudyBrowserException(messageSource.getMessage(Message.ERROR_DATABASE));
        }

        if (project == null) {
            throw new GermplasmStudyBrowserException(messageSource.getMessage(Message.ERROR_DATABASE));
        }

        if (!source.isFolder(id)) {
            throw new GermplasmStudyBrowserException(NOT_FOLDER);
        }

        if (source.hasChildStudy(id)) {
            throw new GermplasmStudyBrowserException(HAS_CHILDREN);
        }

    }
    
    /**
     * Performs validations on folder to be deleted.
     * If folder can be deleted, deletes it from database and adjusts tree view
     * 
     * @param studyId
     */
    public void deleteFolder(final Integer studyId){
    	try {
			validateForDeleteNurseryList(studyId);
		} catch (GermplasmStudyBrowserException e) {
			LOG.error(e.getMessage());
			MessageNotifier.showError(source.getWindow(), messageSource.getMessage(Message.ERROR_TEXT), e.getMessage());
			return;
		}
    	
		ConfirmDialog.show(source.getParentComponent().getWindow(),
				messageSource.getMessage(Message.DELETE_ITEM),
				messageSource.getMessage(Message.DELETE_ITEM_CONFIRM),	
				messageSource.getMessage(Message.YES), messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {

				private static final long serialVersionUID = 1L;

				@Override
	             public void onClose(ConfirmDialog dialog) {
	                 if (dialog.isConfirmed()) {
	                     try {
	                    	 
	                         DmsProject parent = studyDataManager.getParentFolder(studyId);
	                         studyDataManager.deleteEmptyFolder(studyId);
	                         
	                         targetTree.removeItem(targetTree.getValue());
	                         if (parent != null){
	                        	 Integer parentId = parent.getProjectId();
	                        	 if (parentId == 1){
	                        		 targetTree.select(StudyTreeComponent.LOCAL);
	                        	 } else {
	                        		 targetTree.select(parentId);
	                        		 targetTree.expandItem(parentId);
	                        	 }
	                         }
	                         targetTree.setImmediate(true);
	                         source.updateButtons(targetTree.getValue());
	
	                     } catch (MiddlewareQueryException e) {
	                         MessageNotifier.showError(source.getWindow(),messageSource.getMessage(Message.ERROR_DATABASE), 
	                        		 messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
	                     }
	                 }
	             }
         });
    }
}
