package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.SelectGermplasmListComponent;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeCollapseListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeExpandListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.pojos.GermplasmList;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeDragMode;

public class ListManagerTreeFoldersComponent extends ListManagerTreeComponent {

	private static final long serialVersionUID = 1L;

	public ListManagerTreeFoldersComponent(
			SelectGermplasmListComponent selectListComponent) {
		super(selectListComponent);
	}

	public ListManagerTreeFoldersComponent(ListManagerMain listManagerMain,
			AbsoluteLayout germplasmListBrowserMainLayout,
			boolean forGermplasmListWindow, Integer listId) {
		super(listManagerMain, germplasmListBrowserMainLayout, forGermplasmListWindow,
				listId);
		selectedListId = listId;
	}
	
	public ListManagerTreeFoldersComponent(ListManagerMain listManagerMain,
			AbsoluteLayout germplasmListBrowserMainLayout,
			boolean forGermplasmListWindow) {
		super(listManagerMain, germplasmListBrowserMainLayout, forGermplasmListWindow);
	}
	
	@Override
    protected Tree createGermplasmListTree() {
        List<GermplasmList> localGermplasmListParent = new ArrayList<GermplasmList>();

        try {
            localGermplasmListParent = this.germplasmListManager.getAllTopLevelListsBatched(BATCH_SIZE, Database.LOCAL);
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in getting top level lists.", e);
            if (getWindow() != null){
                MessageNotifier.showWarning(getWindow(), 
                        messageSource.getMessage(Message.ERROR_DATABASE),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_FOLDERS));
            }
            localGermplasmListParent = new ArrayList<GermplasmList>();
        }
        
        Tree germplasmListTree = new Tree();
        if (listManagerMain != null){
        	germplasmListTree.setDragMode(TreeDragMode.NODE);
        }

        germplasmListTree.addItem(LOCAL);
        germplasmListTree.setItemCaption(LOCAL, "Program Lists");
        
        for (GermplasmList localParentList : localGermplasmListParent) {
        	if(localParentList.getType().equalsIgnoreCase("FOLDER")){
        		germplasmListTree.addItem(localParentList.getId());
            	germplasmListTree.setItemCaption(localParentList.getId(), localParentList.getName());
            	germplasmListTree.setChildrenAllowed(localParentList.getId(), hasChildList(localParentList.getId()));
            	germplasmListTree.setParent(localParentList.getId(), LOCAL);
        	}
        }

        germplasmListTree.addListener(new GermplasmListTreeExpandListener(this));
        germplasmListTree.addListener(new GermplasmListItemClickListener(this));
        germplasmListTree.addListener(new GermplasmListTreeCollapseListener(this));
        
        try{
        	if(listId != null){
	        	GermplasmList list = germplasmListManager.getGermplasmListById(listId);
	    		
	    		if(list != null){
	    			Deque<GermplasmList> parents = new ArrayDeque<GermplasmList>();
	    			GermplasmListTreeUtil.traverseParentsOfList(germplasmListManager, list, parents);
	    			
	    			if(listId < 0){
	                	germplasmListTree.expandItem(LOCAL);
	    			} else{
	    				germplasmListTree.expandItem(CENTRAL);
	    			}
	    			
	    			while(!parents.isEmpty()){
	    				GermplasmList parent = parents.pop();
	    				germplasmListTree.setChildrenAllowed(parent.getId(), true);
	    				addGermplasmListNode(parent.getId().intValue(), germplasmListTree);
	    				germplasmListTree.expandItem(parent.getId());
	    			}
	    			
	    			germplasmListTree.select(listId);
	    		}
	        }
        } catch(MiddlewareQueryException ex){
    		LOG.error("Error with getting parents for hierarchy of list id: " + listId, ex);
    	}
                
        applyListManagerTreeFolderComponentCustomizations();
        
        return germplasmListTree;
    }

	@Override
	public void attach() {
		//Override so ListManagerTreeComponent's attach() won't open a tab
	}
	
	@Override
    public void addGermplasmListNode(int parentGermplasmListId) throws InternationalizableException{
        List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();

        try {
            germplasmListChildren = this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId, BATCH_SIZE);
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in getting germplasm lists by parent id.", e);
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_DATABASE), 
                    messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
            germplasmListChildren = new ArrayList<GermplasmList>();
        }

        for (GermplasmList listChild : germplasmListChildren) {
        	if(listChild.getType().equalsIgnoreCase("FOLDER")){
	            germplasmListTree.addItem(listChild.getId());
	            germplasmListTree.setItemCaption(listChild.getId(), listChild.getName());
	            germplasmListTree.setParent(listChild.getId(), parentGermplasmListId);
	            // allow children if list has sub-lists
	            germplasmListTree.setChildrenAllowed(listChild.getId(), hasChildList(listChild.getId()));
        	}
        }
        germplasmListTree.select(parentGermplasmListId);
    }	
	
	@Override
    public void addGermplasmListNode(int parentGermplasmListId, Tree germplasmListTree) throws InternationalizableException{
        List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();

        try {
            germplasmListChildren = this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId, BATCH_SIZE);
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in getting germplasm lists by parent id.", e);
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_DATABASE), 
                    messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
            germplasmListChildren = new ArrayList<GermplasmList>();
        }

        for (GermplasmList listChild : germplasmListChildren) {
        	if(listChild.getType().equalsIgnoreCase("FOLDER")){
	            germplasmListTree.addItem(listChild.getId());
	            germplasmListTree.setItemCaption(listChild.getId(), listChild.getName());
	            germplasmListTree.setParent(listChild.getId(), parentGermplasmListId);
	            // allow children if list has sub-lists
	            germplasmListTree.setChildrenAllowed(listChild.getId(), hasChildList(listChild.getId()));
        	}
        }
        germplasmListTree.select(parentGermplasmListId);
    }
	
	private void applyListManagerTreeFolderComponentCustomizations(){
	    if(listId==null || listId==0){
	    	germplasmListTree.select(LOCAL);
	    	germplasmListTree.setValue(LOCAL);
	    }
	    refreshButton.setVisible(false);
	    heading.setVisible(false);
	    germplasmListTree.setNullSelectionAllowed(false);
	}
	
	
}

