package org.generationcp.breeding.manager.listmanager;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.SelectGermplasmListComponent;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeExpandListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
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

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@Configurable
public class ListManagerTreeComponent extends VerticalLayout implements
		InternationalizableComponent, InitializingBean, Serializable {

	private static final Logger LOG = LoggerFactory.getLogger(ListManagerTreeComponent.class);
	
	private static final long serialVersionUID = -224052511814636864L;
	private final static int BATCH_SIZE = 50;
	public final static String REFRESH_BUTTON_ID = "ListManagerTreeComponent Refresh Button";
	public static final String CENTRAL = "CENTRAL";
	public static final String LOCAL = "LOCAL";
	
	private Label heading;
	private ListManagerMain listManagerMain;
	private Tree germplasmListTree;
    private AbsoluteLayout germplasmListBrowserMainLayout;
	private Button refreshButton;
	
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private ListManagerDetailsLayout displayDetailsLayout; 
    
    private boolean forGermplasmListWindow;

    private HorizontalLayout controlButtonsLayout; 
    private VerticalLayout treeContainerLayout;
    
    private Integer listId;
    private GermplasmListTreeUtil germplasmListTreeUtil;
    private SelectGermplasmListComponent selectListComponent;
    
    private final ThemeResource ICON_REFRESH = new ThemeResource("images/refresh-icon.png");
    
    
    private Button addFolderBtn;
    private Button deleteFolderBtn;
    private Button renameFolderBtn;
    
    private Object selectedListId;
    
    public ListManagerTreeComponent(ListManagerMain listManagerMain, AbsoluteLayout germplasmListBrowserMainLayout, boolean forGermplasmListWindow) {
    	this.listManagerMain = listManagerMain;
        this.germplasmListBrowserMainLayout = germplasmListBrowserMainLayout;
        this.forGermplasmListWindow=forGermplasmListWindow;
        this.listId = null;
    }
    
    public ListManagerTreeComponent(ListManagerMain listManagerMain, AbsoluteLayout germplasmListBrowserMainLayout, boolean forGermplasmListWindow, Integer listId) {
    	this.listManagerMain = listManagerMain;
        this.germplasmListBrowserMainLayout = germplasmListBrowserMainLayout;
        this.forGermplasmListWindow=forGermplasmListWindow;
        this.listId = listId;
    }
    
    public ListManagerTreeComponent(SelectGermplasmListComponent selectListComponent){
    	this.selectListComponent = selectListComponent;
    }

    @Override
	public void afterPropertiesSet() throws Exception {
    	
		setSpacing(true);
		
    	displayDetailsLayout = new ListManagerDetailsLayout(listManagerMain, this, germplasmListBrowserMainLayout, forGermplasmListWindow);
    	//setComponentAlignment(displayDetailsLayout, Alignment.MIDDLE_RIGHT);
    	
		heading = new Label();
		heading.setValue(messageSource.getMessage(Message.PROJECT_LISTS));
		heading.setStyleName(Bootstrap.Typography.H4.styleName());
    			
        
		if (this.germplasmListBrowserMainLayout != null){
			displayDetailsLayout = new ListManagerDetailsLayout(listManagerMain, this, germplasmListBrowserMainLayout, forGermplasmListWindow);
			initializeButtonPanel();
			addComponent(controlButtonsLayout);
		}
    	
		germplasmListTree = new Tree();
		
		refreshButton = new Button();
		refreshButton.setData(REFRESH_BUTTON_ID);
		refreshButton.addListener(new GermplasmListButtonClickListener(this));
		refreshButton.setCaption(messageSource.getMessage(Message.REFRESH_LABEL));
		refreshButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		
		treeContainerLayout = new VerticalLayout();
		treeContainerLayout.addComponent(germplasmListTree);
		
		addComponent(treeContainerLayout);
		addComponent(refreshButton);
		
		createTree();
		
		germplasmListTreeUtil = new GermplasmListTreeUtil(this, germplasmListTree);
	}

	private void initializeButtonPanel() {
		renameFolderBtn =new Button("<span class='glyphicon glyphicon-pencil' style='right: 2px;'></span>");
        renameFolderBtn.setHtmlContentAllowed(true);
        renameFolderBtn.setDescription("Rename Folder");
        renameFolderBtn.setStyleName(Bootstrap.Buttons.INFO.styleName());
        renameFolderBtn.setWidth("40px");
        renameFolderBtn.setEnabled(false);
        renameFolderBtn.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				germplasmListTreeUtil.renameFolder(Integer.valueOf(selectedListId.toString()));
            }
        });
        
        addFolderBtn = new Button("<span class='glyphicon glyphicon-plus' style='right: 2px'></span>");
        addFolderBtn.setHtmlContentAllowed(true);
        addFolderBtn.setDescription("Add New Folder");
        addFolderBtn.setStyleName(Bootstrap.Buttons.INFO.styleName());
        addFolderBtn.setWidth("40px");
        addFolderBtn.setEnabled(false);
        addFolderBtn.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				germplasmListTreeUtil.addFolder(selectedListId);
            }
        });
        

        deleteFolderBtn = new Button("<span class='glyphicon glyphicon-trash' style='right: 2px'></span>");
        deleteFolderBtn.setHtmlContentAllowed(true);
        deleteFolderBtn.setDescription("Delete Selected Folder");
        deleteFolderBtn.setStyleName(Bootstrap.Buttons.DANGER.styleName());
        deleteFolderBtn.setWidth("40px");
        deleteFolderBtn.setEnabled(false);
        deleteFolderBtn.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				germplasmListTreeUtil.deleteFolder(Integer.valueOf(selectedListId.toString()));
            }
        });
        
        controlButtonsLayout = new HorizontalLayout();
        
        controlButtonsLayout.addComponent(heading);
        controlButtonsLayout.addComponent(new Label("&nbsp;&nbsp;",Label.CONTENT_XHTML));
        controlButtonsLayout.addComponent(renameFolderBtn);
        controlButtonsLayout.addComponent(addFolderBtn);
        controlButtonsLayout.addComponent(deleteFolderBtn);
	}

	@Override
	public void updateLabels() {
	}
	
	@Override
	public void attach() {
		super.attach();
		if(listId != null){
			try{
				displayDetailsLayout.createListInfoFromBrowseScreen(listId.intValue());
			} catch(MiddlewareQueryException ex){
				
			}
		}
	}
	
	public void simulateItemClickForNewlyAdded(Integer listId, boolean openDetails ){
	    germplasmListTree.expandItem(LOCAL);
	    if(openDetails){
    	    try{
                displayDetailsLayout.createListInfoFromBrowseScreen(listId.intValue());
            } catch(MiddlewareQueryException ex){
                
            }
    	    germplasmListTree.setValue(listId);
	    }
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

        germplasmListTree.setImmediate(true);
        
        if (this.listManagerMain != null){
        	germplasmListTreeUtil = new GermplasmListTreeUtil(this, germplasmListTree);
        }
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
    
    public void updateButtons(Object itemId){
    	
    	try {
    		//If any of the central lists/folders is selected
			if(Integer.valueOf(itemId.toString())>0){
				addFolderBtn.setEnabled(false);
				renameFolderBtn.setEnabled(false);
				deleteFolderBtn.setEnabled(false);
    		//If any of the local folders is selected
			} else if(Integer.valueOf(itemId.toString())<=0 && isFolder(itemId)){
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
    	
    
    public void listManagerTreeItemClickAction(int germplasmListId) throws InternationalizableException{

        try {
    		
        	GermplasmList germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);
        	
        	selectedListId = germplasmListId;
        	
        	boolean hasChildList = hasChildList(germplasmListId);
        	boolean isEmptyFolder = isEmptyFolder(germplasmList);
        	if (!isEmptyFolder){

        		if (!hasChildList){
        			//open details of list in List Manager
        			if (this.listManagerMain != null){
        				this.displayDetailsLayout.createListInfoFromBrowseScreen(germplasmListId);
        				
        			//open details in Select List pop-up
        			} else if (this.selectListComponent != null){
        				this.selectListComponent.getListInfoComponent().displayListInfo(germplasmList);
        			}
        			
        		//toggle folder
	        	} else if(hasChildList){
	        		expandOrCollapseListTreeNode(Integer.valueOf(germplasmListId));
	        	}
        		
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
    
    public boolean isFolder(Object itemId){
    	try {
    		int listId = Integer.valueOf(itemId.toString());
    		GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);
    		if(germplasmList==null)
    			return false;
    		return germplasmList.getType().equalsIgnoreCase("FOLDER");
    	} catch (MiddlewareQueryException e){
    		return false;
    	} catch (NumberFormatException e){
    		if(listId.toString().equals(LOCAL) || listId.toString().equals(CENTRAL)){
    			return true;
    		} else {
    			return false;
    		}
    	}
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
    
    
    public static boolean isInteger(String s) {
        try { 
            Integer.parseInt(s); 
        } catch(NumberFormatException e) { 
            return false; 
        }
        return true;
    }
    
    public ListManagerDetailsLayout getViewDetailsTabbedLayout(){
    	return this.displayDetailsLayout;
    }
    
    public void expandOrCollapseListTreeNode(Object nodeId){
    	if(!this.germplasmListTree.isExpanded(nodeId)){
    		this.germplasmListTree.expandItem(nodeId);
    	} else{
    		this.germplasmListTree.collapseItem(nodeId);
    	}
    	germplasmListTree.select(nodeId);
    	germplasmListTree.setValue(nodeId);
    }
    
    public Tree getGermplasmListTree(){
    	return germplasmListTree;
    }

    public void setSelectedListId(Object listId){
    	this.selectedListId = listId;
    }
    
}
