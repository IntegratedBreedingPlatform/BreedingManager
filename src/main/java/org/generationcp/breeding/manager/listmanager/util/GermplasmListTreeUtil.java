package org.generationcp.breeding.manager.listmanager.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Deque;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListDetailComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeMenu;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeTargetDetails;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GermplasmListTreeUtil implements Serializable {

	private static final long serialVersionUID = 1L;

	private ListManagerTreeComponent source;
	private Tree targetTree;
	private static final String NO_SELECTION = "Please select a folder item";
    public final static String NOT_FOLDER = "Selected item is not a folder.";
    public final static String NO_PARENT = "Selected item is a root item, please choose another item on the list.";
    public final static String HAS_CHILDREN = "Folder has child items.";

	public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";
    public static String MY_LIST = "";
	
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    
	public GermplasmListTreeUtil(ListManagerTreeComponent source, Tree targetTree){
		this.source = source;
		this.targetTree = targetTree;
		setupTreeDragAndDropHandler();
	}
	
    public void setParent(Object sourceItemId, Object targetItemId){

    	if(sourceItemId.equals(ListManagerTreeComponent.LOCAL) || sourceItemId.equals(ListManagerTreeComponent.CENTRAL)){
    		MessageNotifier.showWarning(source.getWindow(), 
                    messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE), 
                    messageSource.getMessage(Message.UNABLE_TO_MOVE_ROOT_FOLDERS));
    		return;
    	}
    	
    	if(targetItemId!=null && targetItemId.equals(ListManagerTreeComponent.CENTRAL)){
    		MessageNotifier.showWarning(source.getWindow(), 
                    messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE), 
                    messageSource.getMessage(Message.UNABLE_TO_MOVE_YOUR_LISTS_TO_PUBLIC_FOLDERS));
    		return;
    	}
    	
    	Integer sourceId = null;
    	Integer targetId = null;
    	
    	if(sourceItemId!=null && !sourceItemId.equals(ListManagerTreeComponent.LOCAL) && !sourceItemId.equals(ListManagerTreeComponent.CENTRAL))
    		sourceId = Integer.valueOf(sourceItemId.toString());
    	if(targetItemId!=null && !targetItemId.equals(ListManagerTreeComponent.LOCAL) && !targetItemId.equals(ListManagerTreeComponent.CENTRAL))
    		targetId = Integer.valueOf(targetItemId.toString());
    	
		if(sourceId!=null && sourceId>0){
			MessageNotifier.showWarning(source.getWindow(), 
					messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE), 
					messageSource.getMessage(Message.UNABLE_TO_MOVE_PUBLIC_LISTS));
			return;
		}    	
	
    	if(targetId!=null && targetId>0){
    		MessageNotifier.showWarning(source.getWindow(),
                    messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE), 
                    messageSource.getMessage(Message.UNABLE_TO_MOVE_YOUR_LISTS_TO_PUBLIC_FOLDERS));
    		return;
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
			e.printStackTrace();
		}
        
        //apply to UI
        if(targetItemId==null || targetTree.getItem(targetItemId)==null){
        	targetTree.setChildrenAllowed(sourceItemId, true);
        	targetTree.setParent(sourceItemId, ListManagerTreeComponent.LOCAL);
        	targetTree.expandItem(ListManagerTreeComponent.LOCAL);
		} else {
			targetTree.setChildrenAllowed(targetItemId, true);
        	targetTree.setParent(sourceItemId, targetItemId);
        	targetTree.expandItem(targetItemId);
		}
        
        source.setSelectedListId(sourceItemId);
        targetTree.select(sourceItemId);
        targetTree.setValue(sourceItemId);
    }

    public void setupTreeDragAndDropHandler(){
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
				
		        //HierarchicalContainer container = (HierarchicalContainer) germplasmListTree.getContainerDataSource();
				//Tree sourceTree = (Tree) t.getSourceComponent();
				
		        GermplasmList targetList = null;
		        try {
					targetList = germplasmListManager.getGermplasmListById((Integer) targetItemId);
				} catch (MiddlewareQueryException e) {
				} catch (ClassCastException e) {
				}
		        
		        //Dropped straight to LOCAL (so no germplasmList found for LOCAL)
		        if(location == VerticalDropLocation.MIDDLE && targetList==null){
					setParent(sourceItemId, "LOCAL");
				//Dropped on a folder
		        } else if (location == VerticalDropLocation.MIDDLE && targetList.getType().equals("FOLDER")){
	        		setParent(sourceItemId, targetItemId);
		        //Dropped on a list with parent != LOCAL
		        } else if (targetList!=null && targetList.getParentId()>=0){
		        	setParent(sourceItemId, targetList.getParentId());
		        //Dropped on a list with parent == LOCAL 
				} else {
					setParent(sourceItemId, "LOCAL");
				}
			}

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
				//return SourceIsTarget.get();
			}
		});
    }
    
    public void addFolder(final Object parentItemId){
    	
        final Window w = new Window("Add new folder");
        w.setWidth("300px");
        w.setHeight("150px");
        w.setModal(true);
        w.setResizable(false);
        w.setStyleName(Reindeer.WINDOW_LIGHT);

        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setMargin(true);

        HorizontalLayout formContainer = new HorizontalLayout();
        formContainer.setSpacing(true);

        Label l = new Label("Folder Name");
        final TextField name = new TextField();
        name.setMaxLength(50);

        formContainer.addComponent(l);
        formContainer.addComponent(name);

        HorizontalLayout btnContainer = new HorizontalLayout();
        btnContainer.setSpacing(true);
        btnContainer.setWidth("100%");

        Label spacer = new Label("");
        btnContainer.addComponent(spacer);
        btnContainer.setExpandRatio(spacer, 1.0F);

        Button ok = new Button("Ok");
        ok.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        ok.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Integer newFolderId = null;
                
                if(name.getValue().toString().replace(" ","").equals("")){
                	MessageNotifier.showWarning(source.getWindow(),
                            messageSource.getMessage(Message.INVALID_INPUT), 
                            messageSource.getMessage(Message.INVALID_LIST_FOLDER_NAME));
                	return;
                }

                GermplasmList newFolder = new GermplasmList();
                GermplasmList parentList = null;
                try {
                	
                	List<GermplasmList> matchingGermplasmLists = germplasmListManager.getGermplasmListByName(name.getValue().toString(), 0, 1, Operation.EQUAL, Database.LOCAL);
                	matchingGermplasmLists.addAll(germplasmListManager.getGermplasmListByName(name.getValue().toString(), 0, 1, Operation.EQUAL, Database.CENTRAL));

                	if(matchingGermplasmLists.size()==0) {
                		
                        User user =workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
                        Integer projectId= workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue();
                        Integer ibdbUserId=workbenchDataManager.getLocalIbdbUserId(user.getUserid(),Long.valueOf(projectId));
                		
	                	newFolder.setName(name.getValue().toString());
	                	newFolder.setDescription(name.getValue().toString());
	                	newFolder.setType("FOLDER");
	                	newFolder.setStatus(0);
	                	newFolder.setUserId(ibdbUserId);
	                	newFolder.setDate(Long.valueOf((new SimpleDateFormat(DATE_AS_NUMBER_FORMAT)).format(Calendar.getInstance().getTime())));
	                	
	                    if (parentItemId==null || parentItemId instanceof String || targetTree.getItem(parentItemId)==null) {
	                        newFolder.setParent(null);
	                    } else if (!source.isFolder(parentItemId)) {
	                    	parentList = germplasmListManager.getGermplasmListById((Integer) parentItemId);
	                        newFolder.setParent(germplasmListManager.getGermplasmListById(parentList.getParentId()));
	                    } else {
	                        newFolder.setParent(germplasmListManager.getGermplasmListById((Integer) parentItemId));
	                    }
	                	
	                	newFolderId = germplasmListManager.addGermplasmList(newFolder);
	                	
	            	} else {
	        			MessageNotifier.showWarning(source.getWindow(),
	                            messageSource.getMessage(Message.INVALID_INPUT), 
	                            messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE));                		
	            	}
                	
                	
                } catch (MiddlewareQueryException e){
                	MessageNotifier.showError(source.getWindow(), 
                            messageSource.getMessage(Message.ERROR_INTERNAL), 
                            messageSource.getMessage(Message.ERROR_REPORT_TO));
        			e.printStackTrace();
                } catch (Error e) {
                	MessageNotifier.showError(source.getWindow(), 
                            messageSource.getMessage(Message.ERROR_INTERNAL), 
                            messageSource.getMessage(Message.ERROR_REPORT_TO));
        			e.printStackTrace();
                    return;
                }

                //update UI
                if (newFolderId != null) {
                    targetTree.addItem(newFolderId);
                    targetTree.setItemCaption(newFolderId, name.getValue().toString());
                    targetTree.setChildrenAllowed(newFolderId, true);
                    
                    source.setSelectedListId(newFolderId);
    		        
                    //If parent of list does not exist
    		        if (parentList==null && !source.isFolder(parentItemId)){
    		        	targetTree.setChildrenAllowed(ListManagerTreeComponent.LOCAL, true);
    		        	targetTree.setParent(newFolderId, ListManagerTreeComponent.LOCAL);
    		        //If parent of list is root node
    		        } else if (parentList!=null && !source.isFolder(parentItemId) && (parentList.getParentId()==null || parentList.getParentId()==0)){
    		        	targetTree.setChildrenAllowed(ListManagerTreeComponent.LOCAL, true);
        		        targetTree.setParent(newFolderId, ListManagerTreeComponent.LOCAL);
                    //If folder
    		        } else if (newFolder.getParent() != null && targetTree.getItem(parentItemId)!=null && source.isFolder(parentItemId)) {
    		        	targetTree.setChildrenAllowed(parentItemId, true);
                        Boolean parentSet = targetTree.setParent(newFolderId, parentItemId);
                        if(!parentSet)
                        	parentSet = targetTree.setParent(newFolderId, ListManagerTreeComponent.LOCAL);
                    //If list, add to parent
                    } else if (newFolder.getParent() != null && targetTree.getItem(parentItemId)!=null) {
                    	targetTree.setChildrenAllowed(parentList.getParentId(), true);
                    	targetTree.setParent(newFolderId, parentList.getParentId());
                    //All else, add to LOCAL list
                    } else {
                    	targetTree.setChildrenAllowed(ListManagerTreeComponent.LOCAL, true);
                    	targetTree.setParent(newFolderId, ListManagerTreeComponent.LOCAL);
                    }

                    if (targetTree.getValue() != null) {
                        if (!targetTree.isExpanded(targetTree.getValue()))
                            targetTree.expandItem(parentItemId);
                    } else {
                    	targetTree.expandItem(ListManagerTreeComponent.LOCAL);
					}
                    targetTree.select(newFolderId);
                    source.updateButtons(newFolderId);
                }

                // close popup
                source.getWindow().removeWindow(event.getComponent().getWindow());
            }
        });

        Button cancel = new Button("Cancel");
        cancel.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = -971341450278698928L;

			@Override
            public void buttonClick(Button.ClickEvent event) {
            	source.getWindow().removeWindow(w);
            }
        });

        btnContainer.addComponent(ok);
        btnContainer.addComponent(cancel);

        container.addComponent(formContainer);
        container.addComponent(btnContainer);

        w.setContent(container);

        // show window
        source.getWindow().addWindow(w);    	
    }    

    
    public void renameFolderOrList(final Integer listId, final ListManagerMain listManagerMain){

    	GermplasmList germplasmList = null;
        try {
			germplasmList = germplasmListManager.getGermplasmListById(listId);
		} catch (MiddlewareQueryException e1) {
			e1.printStackTrace();
		}
    	
        final Window w = new Window();
        
    	if(germplasmList.getType().equalsIgnoreCase("FOLDER")){
    		w.setCaption("Rename Folder");
    	} else {
    		w.setCaption("Rename List");
    	}
    	
        
        w.setWidth("320px");
        w.setHeight("150px");
        w.setModal(true);
        w.setResizable(false);
        w.setStyleName(Reindeer.WINDOW_LIGHT);

        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setMargin(true);

        HorizontalLayout formContainer = new HorizontalLayout();
        formContainer.setSpacing(true);

        Label l = new Label();
        
    	if(germplasmList.getType().equalsIgnoreCase("FOLDER")){
    		l.setCaption("Folder Name");
    	} else {
    		l.setCaption("List Name");
    	}

        final TextField name = new TextField();
        name.setMaxLength(50);
        
        if(germplasmList!=null){
        	name.setValue(germplasmList.getName());
        }

        formContainer.addComponent(l);
        formContainer.addComponent(name);

        HorizontalLayout btnContainer = new HorizontalLayout();
        btnContainer.setSpacing(true);
        btnContainer.setWidth("100%");

        Label spacer = new Label("");
        btnContainer.addComponent(spacer);
        btnContainer.setExpandRatio(spacer, 1.0F);

        Button ok = new Button("Ok");
        ok.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        ok.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Integer newItem = null;
                
                String newName = name.getValue().toString();
				if(newName.replace(" ","").equals("")){
                	MessageNotifier.showWarning(source.getWindow(),
                            messageSource.getMessage(Message.INVALID_INPUT), 
                            messageSource.getMessage(Message.INVALID_LIST_FOLDER_NAME));
                	return;
                }
                
                try {
                	GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);
                	
                	List<GermplasmList> matchingGermplasmLists = germplasmListManager.getGermplasmListByName(newName, 0, 1, Operation.EQUAL, Database.LOCAL);
                	matchingGermplasmLists.addAll(germplasmListManager.getGermplasmListByName(newName, 0, 1, Operation.EQUAL, Database.CENTRAL));
                	
                	Boolean nameAlreadyExisting = false;
                	for(GermplasmList glist : matchingGermplasmLists){
                		if(!glist.getId().equals(germplasmList.getId())){
                			nameAlreadyExisting = true;
                		}
                	}

                	if(!nameAlreadyExisting){
	                	germplasmList.setName(newName);
	                	germplasmListManager.updateGermplasmList(germplasmList);
	                	
	                    targetTree.setItemCaption(listId, newName);
	                    targetTree.select(listId);
	                    
	                    //rename tabs
	                    listManagerMain.updateUIForRenamedList(listId, newName);
                	} else {
            			MessageNotifier.showWarning(source.getWindow(),
                                messageSource.getMessage(Message.INVALID_INPUT), 
                                messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE));                		
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

                source.getWindow().removeWindow(event.getComponent().getWindow());
            }
        });

        Button cancel = new Button("Cancel");
        cancel.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
            	source.getWindow().removeWindow(w);
            }
        });

        btnContainer.addComponent(ok);
        btnContainer.addComponent(cancel);

        container.addComponent(formContainer);
        container.addComponent(btnContainer);

        w.setContent(container);

        // show window
        source.getWindow().addWindow(w);    	
    }

	public void deleteFolderOrList(final ListManagerTreeComponent listManagerTreeComponent, final Integer lastItemId, final ListManagerMain listManagerMain) {
		 
		GermplasmList gpList = null; 
		try {
			if (lastItemId== null) {
				throw new Error(NO_SELECTION);
			}

			try {
				gpList = germplasmListManager.getGermplasmListById(lastItemId);

			} catch (MiddlewareQueryException e) {
				throw new Error(messageSource.getMessage(Message.ERROR_DATABASE));
			}

			if (gpList == null) {
				throw new Error(messageSource.getMessage(Message.ERROR_DATABASE));
			}
			
			if (gpList.getStatus()>100){
				throw new Error(messageSource.getMessage(Message.ERROR_UNABLE_TO_DELETE_LOCKED_LIST));
			}

			try {
				if (hasChildren(gpList.getId())) {
					throw new Error(HAS_CHILDREN);
				}
			} catch (MiddlewareQueryException e) {
				throw new Error(messageSource.getMessage(Message.ERROR_DATABASE));
			}

		} catch (Error e) {
			MessageNotifier.showError(source.getWindow(),messageSource.getMessage(Message.ERROR),e.getMessage());
			return;
		}

		final GermplasmList finalGpList = gpList;
		ConfirmDialog.show(source.getWindow(),
				messageSource.getMessage(Message.DELETE_LIST_FOLDER,targetTree.getItemCaption(lastItemId)),
				messageSource.getMessage(Message.DELETE_LIST_FOLDER_CONFIRM,targetTree.getItemCaption(lastItemId)),
				messageSource.getMessage(Message.YES),messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {
			@Override
			public void onClose(ConfirmDialog dialog) {
				if (dialog.isConfirmed()) {
					try {
						GermplasmList parent = germplasmListManager.getGermplasmListById(finalGpList.getId()).getParent();
						ListCommonActionsUtil.deleteGermplasmList(germplasmListManager, finalGpList, 
								workbenchDataManager, source.getWindow(), messageSource, "item");
						targetTree.removeItem(lastItemId);
						targetTree.select(null);
						if (parent == null) {
							targetTree.select(MY_LIST);
							listManagerTreeComponent.setSelectedListId(MY_LIST);
						} else {
							targetTree.select(parent.getId());
							targetTree.expandItem(parent.getId());
							listManagerTreeComponent.setSelectedListId(parent.getId());
						}
						
						listManagerMain.removeDeletedListFromUI(finalGpList.getId());
						
					} catch (Error e) {
						MessageNotifier.showError(source.getWindow(), e.getMessage(), "");
					} catch (MiddlewareQueryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
	}    
	
    public boolean hasChildren(Integer id) throws MiddlewareQueryException {
        return !germplasmListManager.getGermplasmListByParentFolderId(id,0,Integer.MAX_VALUE).isEmpty();
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
    
}
