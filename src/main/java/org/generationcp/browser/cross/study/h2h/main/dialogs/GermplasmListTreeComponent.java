package org.generationcp.browser.cross.study.h2h.main.dialogs;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.h2h.main.listeners.SelectListButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.listeners.SelectListItemClickListener;
import org.generationcp.browser.cross.study.h2h.main.listeners.SelectListTreeExpandListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@Configurable
public class GermplasmListTreeComponent extends VerticalLayout implements
		InternationalizableComponent, InitializingBean, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTreeComponent.class);
	
	private final static int BATCH_SIZE = 50;
	public final static String REFRESH_BUTTON_ID = "NewListTreeUI Refresh Button";
	public static final String CENTRAL = "CENTRAL";
	public static final String LOCAL = "LOCAL";
	
	private Tree germplasmListTree;
	private Button refreshButton;
	
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private VerticalLayout treeContainerLayout;
    
    private Integer listId;
    private SelectGermplasmListComponent selectListComponent;
          
    public GermplasmListTreeComponent(SelectGermplasmListComponent selectListComponent){
    	this.selectListComponent = selectListComponent;
    }

    @Override
	public void afterPropertiesSet() throws Exception {
		setSpacing(true);
		
		germplasmListTree = new Tree();
		germplasmListTree.setImmediate(true);
		
		refreshButton = new Button();
		refreshButton.setData(REFRESH_BUTTON_ID);
		refreshButton.addListener(new SelectListButtonClickListener(this));
		//refreshButton.setIcon(ICON_REFRESH);
		refreshButton.setCaption(messageSource.getMessage(Message.REFRESH_LABEL));
		refreshButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		
		treeContainerLayout = new VerticalLayout();
		treeContainerLayout.addComponent(germplasmListTree);
		
		addComponent(treeContainerLayout);
		addComponent(refreshButton);
		
		createTree();
		
	}

	@Override
	public void updateLabels() {
	}
	

    public void createTree() {
    	treeContainerLayout.removeComponent(germplasmListTree);
   		germplasmListTree.removeAllItems();
   		germplasmListTree = createGermplasmListTree();
        germplasmListTree.addStyleName("listManagerTree");
        
        germplasmListTree.setItemStyleGenerator(new ItemStyleGenerator() {
        	private static final long serialVersionUID = -5690995097357568121L;

			@Override
            public String getStyle(Object itemId) {
				
				GermplasmList currentList = null;
				
				try {
					currentList = germplasmListManager.getGermplasmListById(Integer.valueOf(itemId.toString()));
				} catch (NumberFormatException e) {
					currentList = null;
				} catch (MiddlewareQueryException e) {
					LOG.error("Erro with getting list by id: " + itemId, e);
					currentList = null;
				} 
				
            	if(itemId.equals(LOCAL) || itemId.equals(CENTRAL)){
            		return "listManagerTreeRootNode"; 
            	} else if(currentList!=null && currentList.getType().equals("FOLDER")){
            		return "listManagerTreeRegularParentNode";
            	} else {
            		return "listManagerTreeRegularChildNode";
            	}

            }
        });

        treeContainerLayout.addComponent(germplasmListTree);
        germplasmListTree.requestRepaint();

    }

    private Tree createGermplasmListTree() {
        List<GermplasmList> localGermplasmListParent = new ArrayList<GermplasmList>();
        List<GermplasmList> centralGermplasmListParent = new ArrayList<GermplasmList>();

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
        
        try {
            centralGermplasmListParent = this.germplasmListManager.getAllTopLevelListsBatched(BATCH_SIZE, Database.CENTRAL);
        } catch (MiddlewareQueryException e) {
        	LOG.error("Error in getting top level lists.", e);
            if (getWindow() != null){
                MessageNotifier.showWarning(getWindow(), 
                        messageSource.getMessage(Message.ERROR_DATABASE),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_FOLDERS));
            }
            centralGermplasmListParent = new ArrayList<GermplasmList>();
        }
        
        Tree germplasmListTree = new Tree();
		germplasmListTree.setDragMode(TreeDragMode.NODE);

        germplasmListTree.addItem(LOCAL);
        germplasmListTree.setItemCaption(LOCAL, "Program Lists");
        
        germplasmListTree.addItem(CENTRAL);
        germplasmListTree.setItemCaption(CENTRAL, "Public Lists");        
        
        for (GermplasmList localParentList : localGermplasmListParent) {
            germplasmListTree.addItem(localParentList.getId());
            germplasmListTree.setItemCaption(localParentList.getId(), localParentList.getName());
            germplasmListTree.setChildrenAllowed(localParentList.getId(), hasChildList(localParentList.getId()));
            germplasmListTree.setParent(localParentList.getId(), LOCAL);
        }

        for (GermplasmList centralParentList : centralGermplasmListParent) {
            germplasmListTree.addItem(centralParentList.getId());
            germplasmListTree.setItemCaption(centralParentList.getId(), centralParentList.getName());
            germplasmListTree.setChildrenAllowed(centralParentList.getId(), hasChildList(centralParentList.getId()));
            germplasmListTree.setParent(centralParentList.getId(), CENTRAL);
        }        
        
        germplasmListTree.addListener(new SelectListTreeExpandListener(this));
        germplasmListTree.addListener(new SelectListItemClickListener(this));

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
        
        return germplasmListTree;
    }
    
    private void traverseParentsOfList(GermplasmList list, Deque<GermplasmList> parents) throws MiddlewareQueryException{
    	if(list == null){
    		return;
    	} else{
    		Integer parentId = list.getParentId();
    		
    		if(parentId != null && parentId != 0){
	    		GermplasmList parent = germplasmListManager.getGermplasmListById(list.getParentId());
	    		
	    		if(parent != null){
	    			parents.push(parent);
	    			traverseParentsOfList(parent, parents);
	    		}
    		}
    		
    		return;
    	}
    }
    
    public void listManagerTreeItemClickAction(int germplasmListId) throws InternationalizableException{

        try {
        	GermplasmList list = getGermplasmList(germplasmListId);
            boolean isEmptyFolder = isEmptyFolder(list);
			boolean hasChildList = hasChildList(germplasmListId);
			
			if (!hasChildList && !isEmptyFolder && this.selectListComponent != null) {
				this.selectListComponent.getListInfoComponent().displayListInfo(list);
                
			//toggle folder
            } else if(hasChildList && !isEmptyFolder){
            	expandOrCollapseListTreeNode(Integer.valueOf(germplasmListId));
            }
        } catch (NumberFormatException e) {
        	LOG.error("Error clicking of list.", e);
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                    messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT),
                    Notification.POSITION_CENTERED);
        }catch (MiddlewareQueryException e){
        	LOG.error("Error in displaying germplasm list details.", e);
            throw new InternationalizableException(e, Message.ERROR_DATABASE,
                    Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
        }
        
    }    
	
    private boolean hasChildList(int listId) {

        List<GermplasmList> listChildren = new ArrayList<GermplasmList>();

        try {
            listChildren = this.germplasmListManager.getGermplasmListByParentFolderId(listId, 0, 1);
        } catch (MiddlewareQueryException e) {
        	LOG.error("Error in getting germplasm lists by parent id.", e);
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_DATABASE), 
                    messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
            listChildren = new ArrayList<GermplasmList>();
        }
        
        return !listChildren.isEmpty();
    }

    private boolean isEmptyFolder(GermplasmList list) throws MiddlewareQueryException{
        boolean isFolder = list.getType().equalsIgnoreCase("FOLDER");
        return isFolder && !hasChildList(list.getId());
    }
    
    private GermplasmList getGermplasmList(int listId) throws MiddlewareQueryException{
    	return germplasmListManager.getGermplasmListById(listId);
    }
    
    public void addGermplasmListNode(int parentGermplasmListId) throws InternationalizableException{
    	germplasmListTree.select(null);
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
            germplasmListTree.addItem(listChild.getId());
            germplasmListTree.setItemCaption(listChild.getId(), listChild.getName());
            germplasmListTree.setParent(listChild.getId(), parentGermplasmListId);
            // allow children if list has sub-lists
            germplasmListTree.setChildrenAllowed(listChild.getId(), hasChildList(listChild.getId()));
        }
    }
    
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
            germplasmListTree.addItem(listChild.getId());
            germplasmListTree.setItemCaption(listChild.getId(), listChild.getName());
            germplasmListTree.setParent(listChild.getId(), parentGermplasmListId);
            // allow children if list has sub-lists
            germplasmListTree.setChildrenAllowed(listChild.getId(), hasChildList(listChild.getId()));
        }
    }
    
    
    
    public void expandOrCollapseListTreeNode(Object nodeId){
    	if(!this.germplasmListTree.isExpanded(nodeId)){
    		this.germplasmListTree.expandItem(nodeId);
    	} else{
    		this.germplasmListTree.collapseItem(nodeId);
    	}
    }
    

    

}
