package org.generationcp.breeding.manager.listmanager.util;

import java.io.Serializable;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Tree.TreeTargetDetails;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GermplasmListTreeUtil implements Serializable {

	private static final long serialVersionUID = 1L;

	private Component source;
	private Tree targetTree;
	
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
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
    
    public void addFolder(){
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
                try {
//                    if (treeView.getValue() instanceof String)
//                        newItem = presenter.addGermplasmListFolder(name.getValue().toString(), null);
//                    else
//                        newItem = presenter.addGermplasmListFolder(name.getValue().toString(), (Integer) treeView.getValue());
                } catch (Error e) {
                    MessageNotifier.showError(event.getComponent().getWindow(), e.getMessage(), "");
                    return;
                }

                //update UI
                if (newItem != null) {
//                    treeView.addItem(newItem);
//                    treeView.setItemCaption(newItem, name.getValue().toString());
//                    treeView.setChildrenAllowed(newItem, true);
//                    treeView.setItemIcon(newItem, folderResource);
//
//                    if (presenter.getGermplasmListParent(newItem) != null) {
//                        treeView.setParent(newItem, treeView.getValue());
//                    } else {
//                        treeView.setParent(newItem, MY_LIST);
//                    }
//
//                    if (treeView.getValue() != null) {
//                        if (!treeView.isExpanded(treeView.getValue()))
//                            expandTree(treeView.getValue());
//                    } else
//                        treeView.expandItem(MY_LIST);
//
//                    treeView.select(newItem);
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
        final Window w = new Window("Rename folder");
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

        GermplasmList germplasmList = null;
        try {
			germplasmList = germplasmListManager.getGermplasmListById(listId);
		} catch (MiddlewareQueryException e1) {
			e1.printStackTrace();
		}
        
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
                                messageSource.getMessage(Message.ERROR_INTERNAL), 
                                messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE));                		
                	}
                	
                } catch (MiddlewareQueryException e) {
                	MessageNotifier.showWarning(source.getWindow(),
                            messageSource.getMessage(Message.ERROR_DATABASE), 
                            messageSource.getMessage(Message.ERROR_REPORT_TO));
                } catch (Error e) {
                    MessageNotifier.showError(source.getWindow(), e.getMessage(), "");
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
    
}
