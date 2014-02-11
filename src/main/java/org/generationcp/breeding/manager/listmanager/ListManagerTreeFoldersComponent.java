package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.SelectGermplasmListComponent;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeExpandListener;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.pojos.GermplasmList;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
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
        
        try{
        	if(listId != null){
	        	GermplasmList list = germplasmListManager.getGermplasmListById(listId);
	    		
	    		if(list != null){
	    			Deque<GermplasmList> parents = new ArrayDeque<GermplasmList>();
	    			traverseParentsOfList(list, parents);
	    			
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

