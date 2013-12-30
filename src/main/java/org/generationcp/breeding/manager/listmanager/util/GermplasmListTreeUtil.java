package org.generationcp.breeding.manager.listmanager.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
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
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeTargetDetails;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GermplasmListTreeUtil implements Serializable {

	private static final long serialVersionUID = 1L;

	private Component source;
	private Tree targetTree;
	private static final String NO_SELECTION = "Please select a folder item";
    public final static String NOT_FOLDER = "Selected item is not a folder.";
    public final static String NO_PARENT = "Selected item is a root item, please choose another item on the list.";
    public final static String HAS_CHILDREN = "Folder has child items.";
    public static String MY_LIST = "";
	
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    
	public GermplasmListTreeUtil(Component source, Tree targetTree){
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
    	
    	
    	try {
			//studyDataManager.moveFolder(sourceId, targetId);
            GermplasmList sourceGermplasmList = germplasmListManager.getGermplasmListById(sourceId);
            if (targetId != null) {
                GermplasmList targetGermplasmList = germplasmListManager.getGermplasmListById(targetId);
                sourceGermplasmList.setParent(targetGermplasmList);
            } else {
            	sourceGermplasmList.setParent(null);
            }
            germplasmListManager.updateGermplasmList(sourceGermplasmList);
            if(targetItemId==null)
            	targetTree.setParent(sourceItemId, ListManagerTreeComponent.LOCAL);
            else
            	targetTree.setParent(sourceItemId, targetItemId);
		} catch (MiddlewareQueryException e) {
			MessageNotifier.showError(source.getWindow(), 
                    messageSource.getMessage(Message.ERROR_INTERNAL), 
                    messageSource.getMessage(Message.ERROR_REPORT_TO));
			e.printStackTrace();
		}
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
		        
		        if(location == VerticalDropLocation.MIDDLE && targetList==null){
					setParent(sourceItemId, targetItemId);		        	
		        } else if (location == VerticalDropLocation.MIDDLE && targetList.getType().equals("FOLDER")){
		            setParent(sourceItemId, targetItemId);
				} else {
					setParent(sourceItemId, targetList.getParentId());
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
	                	newFolder.setStatus(1);
	                	newFolder.setUserId(ibdbUserId);
	                	newFolder.setDate(Long.valueOf((new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime())));
	                	
	                    if (parentItemId==null || parentItemId instanceof String)
	                        newFolder.setParent(null);
	                    else
	                        newFolder.setParent(germplasmListManager.getGermplasmListById((Integer) parentItemId));
	                	
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
                    if (newFolder.getParent() != null) {
                        targetTree.setParent(newFolderId, parentItemId);
                    } else {
                    	targetTree.setParent(newFolderId, ListManagerTreeComponent.LOCAL);
                    }

                    if (targetTree.getValue() != null) {
                        if (!targetTree.isExpanded(targetTree.getValue()))
                            targetTree.expandItem(parentItemId);
                    } else {
                    	targetTree.expandItem(ListManagerTreeComponent.LOCAL);
					}
                    targetTree.select(newFolderId);
                }

                // close popup
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

    
    public void renameFolder(final Integer listId){

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
                
                if(name.getValue().toString().replace(" ","").equals("")){
                	MessageNotifier.showWarning(source.getWindow(),
                            messageSource.getMessage(Message.INVALID_INPUT), 
                            messageSource.getMessage(Message.INVALID_LIST_FOLDER_NAME));
                	return;
                }
                
                try {
                	GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);
                	
                	List<GermplasmList> matchingGermplasmLists = germplasmListManager.getGermplasmListByName(name.getValue().toString(), 0, 1, Operation.EQUAL, Database.LOCAL);
                	matchingGermplasmLists.addAll(germplasmListManager.getGermplasmListByName(name.getValue().toString(), 0, 1, Operation.EQUAL, Database.CENTRAL));
                	
                	Boolean nameAlreadyExisting = false;
                	for(GermplasmList glist : matchingGermplasmLists){
                		if(!glist.getId().equals(germplasmList.getId())){
                			nameAlreadyExisting = true;
                		}
                	}

                	if(!nameAlreadyExisting){
	                	germplasmList.setName(name.getValue().toString());
	                	germplasmListManager.updateGermplasmList(germplasmList);
	                	
	                    targetTree.setItemCaption(listId, name.getValue().toString());
	                    targetTree.select(listId);
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

	public void deleteFolder(final Integer lastItemId) {
		 
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

			if (!gpList.isFolder()) {
				throw new Error(NOT_FOLDER);
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
						germplasmListManager.deleteGermplasmList(finalGpList);
						targetTree.removeItem(lastItemId);
						targetTree.select(null);
						if (parent == null) {
							targetTree.select(MY_LIST);
						} else {
							targetTree.select(parent.getId());
						}
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
    
}
