package org.generationcp.breeding.manager.listmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeExpandListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
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

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@Configurable
public class ListManagerTreeComponent extends VerticalLayout implements
		InternationalizableComponent, InitializingBean, Serializable {

	private static final Logger LOG = LoggerFactory.getLogger(ListManagerTreeComponent.class);
	
	private static final long serialVersionUID = -224052511814636864L;
	private final static int BATCH_SIZE = 50;
	public final static String REFRESH_BUTTON_ID = "ListManagerTreeComponent Refresh Button";
	
	private Tree germplasmListTree;
    private AbsoluteLayout germplasmListBrowserMainLayout;
	private Button refreshButton;
	
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private ListManagerDetailsLayout displayDetailsLayout; 
    
    private boolean forGermplasmListWindow;

    private VerticalLayout treeContainerLayout;
    
    private Integer listId;
    
    public ListManagerTreeComponent(AbsoluteLayout germplasmListBrowserMainLayout, boolean forGermplasmListWindow) {
        this.germplasmListBrowserMainLayout = germplasmListBrowserMainLayout;
        this.forGermplasmListWindow=forGermplasmListWindow;
        this.listId = null;
    }
    
    public ListManagerTreeComponent(AbsoluteLayout germplasmListBrowserMainLayout, boolean forGermplasmListWindow, Integer listId) {
        this.germplasmListBrowserMainLayout = germplasmListBrowserMainLayout;
        this.forGermplasmListWindow=forGermplasmListWindow;
        this.listId = listId;
    }

    @Override
	public void afterPropertiesSet() throws Exception {
		
    	displayDetailsLayout = new ListManagerDetailsLayout(this, germplasmListBrowserMainLayout, forGermplasmListWindow);
    	
		germplasmListTree = new Tree();
		germplasmListTree.setImmediate(true);
		
		refreshButton = new Button();
		refreshButton.setData(REFRESH_BUTTON_ID);
		refreshButton.addListener(new GermplasmListButtonClickListener(this));
		refreshButton.setCaption(messageSource.getMessage(Message.REFRESH_LABEL));
		
		treeContainerLayout = new VerticalLayout();
		treeContainerLayout.addComponent(germplasmListTree);
		
		addComponent(treeContainerLayout);
		addComponent(refreshButton);
		
		createTree();
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

    public void createTree() {
    	treeContainerLayout.removeComponent(germplasmListTree);
   		germplasmListTree.removeAllItems();
   		germplasmListTree = createGermplasmListTree();
        germplasmListTree.addStyleName("listManagerTree");
        
        germplasmListTree.setItemStyleGenerator(new ItemStyleGenerator() {
        	private static final long serialVersionUID = -5690995097357568121L;

			@Override
            public String getStyle(Object itemId) {
            	if(itemId.equals("LOCAL") || itemId.equals("CENTRAL")){
            		return "listManagerTreeRootNode"; 
            	} else if(isInteger((String) itemId.toString()) && hasChildList((Integer) itemId)){
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

        germplasmListTree.addItem("LOCAL");
        germplasmListTree.setItemCaption("LOCAL", "My Lists");
        
        germplasmListTree.addItem("CENTRAL");
        germplasmListTree.setItemCaption("CENTRAL", "Shared Lists");        
        
        for (GermplasmList localParentList : localGermplasmListParent) {
            germplasmListTree.addItem(localParentList.getId());
            germplasmListTree.setItemCaption(localParentList.getId(), localParentList.getName());
            germplasmListTree.setChildrenAllowed(localParentList.getId(), false);
            germplasmListTree.setParent(localParentList.getId(), "LOCAL");
        }

        for (GermplasmList centralParentList : centralGermplasmListParent) {
            germplasmListTree.addItem(centralParentList.getId());
            germplasmListTree.setItemCaption(centralParentList.getId(), centralParentList.getName());
            germplasmListTree.setParent(centralParentList.getId(), "CENTRAL");
        }        
        
        germplasmListTree.addListener(new GermplasmListTreeExpandListener(this));
        germplasmListTree.addListener(new GermplasmListItemClickListener(this));

        return germplasmListTree;
    }
    
    public void listManagerTreeItemClickAction(int germplasmListId) throws InternationalizableException{
        try {
            if (!hasChildList(germplasmListId) && !isEmptyFolder(germplasmListId)) {
                this.displayDetailsLayout.createListInfoFromBrowseScreen(germplasmListId);
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

    private boolean isEmptyFolder(int listId) throws MiddlewareQueryException{
        boolean isFolder = germplasmListManager.getGermplasmListById(listId).getType().equalsIgnoreCase("FOLDER");
        return isFolder && !hasChildList(listId);
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
}
