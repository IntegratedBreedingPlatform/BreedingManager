package org.generationcp.breeding.manager.customfields;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.IconButton;
import org.generationcp.breeding.manager.customcomponent.ToggleButton;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeCollapseListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeExpandListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.commons.exceptions.InternationalizableException;
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

import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.Window.Notification;

@Configurable
public abstract class ListTreeComponent extends CssLayout implements
		InitializingBean, BreedingManagerLayout {
	
	private static final Logger LOG = LoggerFactory.getLogger(ListTreeComponent.class);
	private static final long serialVersionUID = -4025353842975688857L;

	protected static final int BATCH_SIZE = 50;
	public static final String REFRESH_BUTTON_ID = "ListManagerTreeComponent Refresh Button";
	public static final String CENTRAL = "CENTRAL";
	public static final String LOCAL = "LOCAL";
	
	public static final String PROGRAM_LISTS = "Program Lists";
	public static final String PUBLIC_LISTS = "Public Lists";
	
    @Autowired
    protected GermplasmListManager germplasmListManager;
    
    @Autowired
    protected SimpleResourceBundleMessageSource messageSource;
    
    protected HorizontalLayout controlButtonsLayout;
    protected HorizontalLayout ctrlBtnsLeftSubLayout;
    protected HorizontalLayout ctrlBtnsRightSubLayout;
    protected CssLayout treeContainerLayout;
    
    protected Integer listId;
    protected GermplasmListTreeUtil germplasmListTreeUtil;
    protected ListTreeActionsListener treeActionsListener;

    protected Button addFolderBtn;
    protected Button deleteFolderBtn;
    protected Button renameFolderBtn;
    
    protected HeaderLabelLayout treeHeadingLayout;
    protected Label heading;
	protected Tree germplasmListTree;
	protected Button refreshButton;
	
	protected Boolean selectProgramListsByDefault;
    
    protected Object selectedListId;
    
    protected ToggleButton toggleListTreeButton;
    
    public ListTreeComponent(Integer selectListId){
    	this.listId = selectListId;
    	selectProgramListsByDefault = false;
    }

    public ListTreeComponent(ListTreeActionsListener treeActionsListener){
    	this.treeActionsListener = treeActionsListener;
    	selectProgramListsByDefault = false;
    }
    
    public ListTreeComponent(ListTreeActionsListener treeActionsListener, Integer selectedListId){
    	this.treeActionsListener = treeActionsListener;
    	this.listId = selectedListId;
    	selectProgramListsByDefault = true;
    }
    
	@Override
	public void instantiateComponents() {
		setHeight("580px");
		setWidth("880px");
    	
    	heading = new Label();
		heading.setValue(getTreeHeading());
		heading.addStyleName(getTreeHeadingStyleName());
		heading.addStyleName(AppConstants.CssStyles.BOLD);
		
		treeHeadingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_BUILD_NEW_LIST, heading);
    	
		// if tree will include the toogle button to hide itself
		if (doIncludeToggleButton()){
			toggleListTreeButton = new ToggleButton("Toggle Build New List Pane");
		}
		
		// assumes that all tree will display control buttons
		if (doIncludeActionsButtons()){
			initializeButtonPanel();
		}
		
		treeContainerLayout = new CssLayout();
		germplasmListTree = new Tree();
		if (doIncludeRefreshButton()){
			initializeRefreshButton();
		}
		
		createTree();
		
		germplasmListTreeUtil = new GermplasmListTreeUtil(this, germplasmListTree);
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
		if (doIncludeRefreshButton()){
			refreshButton.addListener(new Button.ClickListener() {
				private static final long serialVersionUID = 1L;
				@Override
				public void buttonClick(ClickEvent event) {
					refreshTree();
				}
			});
		}
	}

	@Override
	public void layoutComponents() {
		setWidth("100%");
		if (doIncludeActionsButtons()){
			addComponent(controlButtonsLayout);
		}
		
		treeContainerLayout.addComponent(germplasmListTree);
		addComponent(treeContainerLayout);
		
		if(doIncludeRefreshButton()){
			addComponent(refreshButton);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	/* #########################################################################
	 * START OF ABSTRACT / PROTECTED METHODS W/C CAN BE OVERRIDEN BY SUBCLASSES
	 * #########################################################################
	 */
	protected abstract boolean doIncludeActionsButtons();

	protected String getTreeHeading(){
		return messageSource.getMessage(Message.LISTS);
	}
	
	protected String getTreeHeadingStyleName(){
		return Bootstrap.Typography.H4.styleName();
	}
	protected String getTreeStyleName(){
		return "listTree";
	}
	
	public boolean usedInSubWindow(){
		return false;
	}
	
	protected boolean doIncludeTreeHeadingIcon(){
		return true;
	}
	
	protected boolean doIncludeToggleButton(){
		return false;
	}
	
	protected abstract boolean doIncludeRefreshButton();
	
	protected abstract boolean isTreeItemsDraggable();
	
	protected abstract boolean doIncludeCentralLists();
	
	protected abstract boolean doShowFoldersOnly();
	
	/* #########################################################################
	 * END OF ABSTRACT / PROTECTED METHODS W/C CAN BE OVERRIDEN BY SUBCLASSES
	 * #########################################################################
	 */

	private void initializeRefreshButton(){
		refreshButton = new Button();
		refreshButton.setData(REFRESH_BUTTON_ID);
		refreshButton.setCaption(messageSource.getMessage(Message.REFRESH_LABEL));
		refreshButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
	}
	
	protected void initializeButtonPanel() {
		renameFolderBtn = new IconButton("<span class='bms-edit' style='left: 2px; color: #0083c0;font-size: 18px; font-weight: bold;'></span>","Rename Item");
        renameFolderBtn.setEnabled(false);
        renameFolderBtn.addListener(new Button.ClickListener() {
			protected static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				germplasmListTreeUtil.renameFolderOrList(Integer.valueOf(selectedListId.toString()), treeActionsListener);
            }
        });
        
        addFolderBtn = new IconButton("<span class='bms-add' style='left: 2px; color: #00a950;font-size: 18px; font-weight: bold;'></span>","Add New Folder");
        addFolderBtn.setEnabled(false);
        addFolderBtn.addListener(new Button.ClickListener() {
			protected static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				germplasmListTreeUtil.addFolder(selectedListId);
            }
        });
        

        deleteFolderBtn = new IconButton("<span class='bms-delete' style='left: 2px; color: #f4a41c;font-size: 18px; font-weight: bold;'></span>","Delete Selected List/Folder");
        deleteFolderBtn.setEnabled(false);
        deleteFolderBtn.setData(this);
        deleteFolderBtn.addListener(new Button.ClickListener() {
			protected static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				Object data = event.getButton().getData();
				if (data instanceof ListTreeComponent){
					germplasmListTreeUtil.deleteFolderOrList((ListTreeComponent) data, 
							Integer.valueOf(selectedListId.toString()), treeActionsListener);
				}
            }
        });
        
        ctrlBtnsRightSubLayout = new HorizontalLayout();
        ctrlBtnsRightSubLayout.setHeight("30px");
        ctrlBtnsRightSubLayout.addComponent(addFolderBtn);
        ctrlBtnsRightSubLayout.addComponent(renameFolderBtn);
        ctrlBtnsRightSubLayout.addComponent(deleteFolderBtn);
        ctrlBtnsRightSubLayout.setComponentAlignment(addFolderBtn, Alignment.BOTTOM_RIGHT);
        ctrlBtnsRightSubLayout.setComponentAlignment(renameFolderBtn, Alignment.BOTTOM_RIGHT);
        ctrlBtnsRightSubLayout.setComponentAlignment(deleteFolderBtn, Alignment.BOTTOM_RIGHT);
        
        ctrlBtnsLeftSubLayout = new HorizontalLayout();
        ctrlBtnsLeftSubLayout.setHeight("30px");
    	
        if(doIncludeToggleButton()){
        	ctrlBtnsLeftSubLayout.addComponent(toggleListTreeButton);
        	ctrlBtnsLeftSubLayout.setComponentAlignment(toggleListTreeButton, Alignment.BOTTOM_LEFT);
        }
        
        if (doIncludeTreeHeadingIcon()){
        	ctrlBtnsLeftSubLayout.addComponent(treeHeadingLayout);
        	heading.setWidth("80px");
        } else {
        	ctrlBtnsLeftSubLayout.addComponent(heading);
        	heading.setWidth("140px");
        }
        
        controlButtonsLayout = new HorizontalLayout();
        controlButtonsLayout.setWidth("100%");
        controlButtonsLayout.setHeight("30px");
        controlButtonsLayout.setSpacing(true);

        controlButtonsLayout.addComponent(ctrlBtnsLeftSubLayout);
        controlButtonsLayout.addComponent(ctrlBtnsRightSubLayout);
        controlButtonsLayout.setComponentAlignment(ctrlBtnsLeftSubLayout, Alignment.BOTTOM_LEFT);
        controlButtonsLayout.setComponentAlignment(ctrlBtnsRightSubLayout, Alignment.BOTTOM_RIGHT);
        
        
        
	}
	
    public void removeListFromTree(GermplasmList germplasmList){
    	Integer listId = germplasmList.getId();
		Item item = germplasmListTree.getItem(listId);
    	if (item != null){
    		germplasmListTree.removeItem(listId);
    	}
    	GermplasmList parent = germplasmList.getParent();
		if (parent == null) {
			germplasmListTree.select(LOCAL);
			setSelectedListId(LOCAL);
		} else {
			germplasmListTree.select(parent.getId());
			germplasmListTree.expandItem(parent.getId());
			setSelectedListId(parent.getId());
		}
    }
    
	/*
	 * Resets listid to null (in case list was launched via Dashboard)
	 * so that tree can be refreshed
	 */
	public void refreshTree(){
		this.listId = null; 
		createTree();
	}
	
	public void createTree() {
		if (treeContainerLayout != null && treeContainerLayout.getComponentCount() > 0){
			treeContainerLayout.removeComponent(germplasmListTree);
		}
		germplasmListTree.removeAllItems();
		createGermplasmListTree();
		germplasmListTree.setStyleName("listTree");
	    germplasmListTree.addStyleName(getTreeStyleName());


	
	    germplasmListTree.setItemStyleGenerator(new ItemStyleGenerator() {
	    	protected static final long serialVersionUID = -5690995097357568121L;
	
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
		    		return AppConstants.CssStyles.TREE_ROOT_NODE;
		    	} else if(currentList!=null && currentList.getType().equals(AppConstants.DB.FOLDER)){
		    		return AppConstants.CssStyles.TREE_REGULAR_PARENT_NODE;
		    	} else {
		    		return AppConstants.CssStyles.TREE_REGULAR_CHILD_NODE;
		        	}
		
		        }
		    }
	    );
	
	    addListTreeItemDescription();
        
	    germplasmListTree.setImmediate(true);
	    if (doIncludeActionsButtons()){
	    	germplasmListTreeUtil = new GermplasmListTreeUtil(this, germplasmListTree);
	    }
	    treeContainerLayout.addComponent(germplasmListTree);
	    germplasmListTree.requestRepaint();
	
	}
	
	private void addListTreeItemDescription(){
		germplasmListTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            private static final long serialVersionUID = -2669417630841097077L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
            	GermplasmList germplasmList;
				
            	try {
					
					germplasmList = germplasmListManager.getGermplasmListById(Integer.valueOf(itemId.toString()));
					
					if(germplasmList != null){
						if(!germplasmList.getType().equals("FOLDER")){
							ViewListHeaderWindow viewListHeaderWindow = new ViewListHeaderWindow(germplasmList);
							return viewListHeaderWindow.getListHeaderComponent().toString();
						}
					}
					
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MiddlewareQueryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
                return "";
            }
        });
	}
	  
	protected void createGermplasmListTree() {
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
        
        
        germplasmListTree = new Tree();
        if (isTreeItemsDraggable()){
        	germplasmListTree.setDragMode(TreeDragMode.NODE);
        }

        germplasmListTree.addItem(LOCAL);
        germplasmListTree.setItemCaption(LOCAL, PROGRAM_LISTS);
        if(doIncludeCentralLists()){
        	try {
        		centralGermplasmListParent = this.germplasmListManager.getAllTopLevelListsBatched(BATCH_SIZE, Database.CENTRAL);
        	} catch (MiddlewareQueryException e) {
        		LOG.error("Error in getting top level lists.", e);
        		centralGermplasmListParent = new ArrayList<GermplasmList>();
        	}
        	germplasmListTree.addItem(CENTRAL);
        	germplasmListTree.setItemCaption(CENTRAL, PUBLIC_LISTS);
        }
        
        for (GermplasmList localParentList : localGermplasmListParent) {
        	if(doAddItem(localParentList)){
	            germplasmListTree.addItem(localParentList.getId());
	            germplasmListTree.setItemCaption(localParentList.getId(), localParentList.getName());
	            germplasmListTree.setChildrenAllowed(localParentList.getId(), hasChildList(localParentList.getId()));
	            germplasmListTree.setParent(localParentList.getId(), LOCAL);
        	}
        }
        
        if(doIncludeCentralLists()){
	        for (GermplasmList centralParentList : centralGermplasmListParent) {
	            germplasmListTree.addItem(centralParentList.getId());
	            germplasmListTree.setItemCaption(centralParentList.getId(), centralParentList.getName());
	            germplasmListTree.setChildrenAllowed(centralParentList.getId(), hasChildList(centralParentList.getId()));
	            germplasmListTree.setParent(centralParentList.getId(), CENTRAL);
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
	    				addGermplasmListNode(parent.getId().intValue());
	    				germplasmListTree.expandItem(parent.getId());
	    			}

	    			germplasmListTree.setNullSelectionAllowed(false);
	    			germplasmListTree.select(listId);
	    			germplasmListTree.setValue(listId);
	    			setSelectedListId(listId);
	    		}
	        } else if(selectProgramListsByDefault) {
	        	germplasmListTree.select("LOCAL");
    			germplasmListTree.setValue("LOCAL");
	        }
        } catch(MiddlewareQueryException ex){
    		LOG.error("Error with getting parents for hierarchy of list id: " + listId, ex);
    	}
        
        //TODO - verify if this is needed. from original code
//        if(forSelectingFolderToSaveIn){
//        	germplasmListTree.setNullSelectionAllowed(false);
//        }
    }

    
    public void setSelectedListId(Object listId){
    	this.selectedListId = listId;
    	germplasmListTree.setNullSelectionAllowed(false);
    	germplasmListTree.select(listId);
    	germplasmListTree.setValue(listId);
    }
    
    protected void setListActionsListener(ListTreeActionsListener listener){
    	this.treeActionsListener = listener;
    }
    
    protected boolean isEmptyFolder(GermplasmList list) throws MiddlewareQueryException{
        boolean isFolder = list.getType().equalsIgnoreCase(AppConstants.DB.FOLDER);
        return isFolder && !hasChildList(list.getId());
    }
    
    public boolean isFolder(Object itemId){
    	try {
    		int listId = Integer.valueOf(itemId.toString());
    		GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);
    		if(germplasmList==null)
    			return false;
    		return germplasmList.getType().equalsIgnoreCase(AppConstants.DB.FOLDER);
    	} catch (MiddlewareQueryException e){
    		return false;
    	} catch (NumberFormatException e){
    		if(listId!=null && (listId.toString().equals(LOCAL) || listId.toString().equals(CENTRAL))){
    			return true;
    		} else {
    			return false;
    		}
    	}
    }
    
    protected boolean hasChildList(int listId) {

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
        	if(doAddItem(listChild)){
	            germplasmListTree.addItem(listChild.getId());
	            germplasmListTree.setItemCaption(listChild.getId(), listChild.getName());
	            germplasmListTree.setParent(listChild.getId(), parentGermplasmListId);
	            // allow children if list has sub-lists
	            germplasmListTree.setChildrenAllowed(listChild.getId(), hasChildList(listChild.getId()));
	            
	            if(!hasChildList(listChild.getId())){
	            	ViewListHeaderWindow viewListHeaderWindow = new ViewListHeaderWindow(listChild);
	            	germplasmListTree.setDescription(viewListHeaderWindow.getListHeaderComponent().toString());
	            }
	            
        	}
        }
        germplasmListTree.setNullSelectionAllowed(false);
        germplasmListTree.select(parentGermplasmListId);
        germplasmListTree.setValue(parentGermplasmListId);
        
        germplasmListTree.setDescription("Beheehehe");
    }
    
    public void updateButtons(Object itemId){
    	setSelectedListId(itemId);
    	if (doIncludeActionsButtons()){
    		try {
    			//If any of the central lists/folders is selected
    			if(Integer.valueOf(itemId.toString())>0){
    				addFolderBtn.setEnabled(false);
    				renameFolderBtn.setEnabled(false);
    				deleteFolderBtn.setEnabled(false);
    				//If any of the local folders/lists are selected
    			} else if(Integer.valueOf(itemId.toString())<=0){
    				addFolderBtn.setEnabled(true);
    				renameFolderBtn.setEnabled(true);
    				deleteFolderBtn.setEnabled(true);
    				//The rest of the local lists
    			} else {
    				addFolderBtn.setEnabled(true);
    				renameFolderBtn.setEnabled(true);
    				deleteFolderBtn.setEnabled(false);
    			}
    		} catch(NumberFormatException e) {
    			//If selected item is "Shared Lists"
    			if(itemId.toString().equals("CENTRAL")) {
    				addFolderBtn.setEnabled(false);
    				renameFolderBtn.setEnabled(false);
    				deleteFolderBtn.setEnabled(false);
    				//If selected item is "Program Lists"
    			} else if(itemId.toString().equals(LOCAL)) {
    				addFolderBtn.setEnabled(true);
    				renameFolderBtn.setEnabled(false);
    				deleteFolderBtn.setEnabled(false);
    				//Any non-numeric itemID (nothing goes here as of the moment)
    			} else {
    				addFolderBtn.setEnabled(true);
    				renameFolderBtn.setEnabled(true);
    				deleteFolderBtn.setEnabled(true);
    			}
    		}
    	}
    }
    
    private boolean doAddItem(GermplasmList list){
    	return !doShowFoldersOnly() || isFolder(list.getId());
    }
    
    public void listManagerTreeItemClickAction(int germplasmListId) throws InternationalizableException{

        try {
    		
        	GermplasmList germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);
        	
        	selectedListId = germplasmListId;
        	
        	boolean hasChildList = hasChildList(germplasmListId);
        	boolean isEmptyFolder = isEmptyFolder(germplasmList);
        	if (!isEmptyFolder){

        		if (!hasChildList){
        			if (treeActionsListener != null){
        				treeActionsListener.openListDetails(germplasmList);
        			}
        			
        		//toggle folder
	        	} else if(hasChildList){
	        		expandOrCollapseListTreeNode(Integer.valueOf(germplasmListId));
	        		treeActionsListener.folderClicked(germplasmList);
	        	}
        		
        		germplasmListTree.setNullSelectionAllowed(false);
        		germplasmListTree.select(germplasmListId);
        		germplasmListTree.setValue(germplasmListId);
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
    
    public void expandOrCollapseListTreeNode(Object nodeId){
    	if(!this.germplasmListTree.isExpanded(nodeId)){
    		this.germplasmListTree.expandItem(nodeId);
    	} else{
    		this.germplasmListTree.collapseItem(nodeId);
    	}
    	germplasmListTree.setNullSelectionAllowed(false);
    	germplasmListTree.setValue(nodeId);
    	germplasmListTree.select(nodeId);
    }
    
    public Object getSelectedListId(){
    	return selectedListId;
    }
    
    public void setListId(Integer listId){
    	this.listId = listId; 
    }
    
    public Tree getGermplasmListTree(){
    	return germplasmListTree;
    }

	public ToggleButton getToggleListTreeButton() {
		return toggleListTreeButton;
	}
    
	public ListTreeActionsListener getTreeActionsListener(){
		return treeActionsListener;
	}
	
}
