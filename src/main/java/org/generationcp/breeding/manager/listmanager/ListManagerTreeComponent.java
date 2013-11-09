package org.generationcp.breeding.manager.listmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTabChangeListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeExpandListener;
import org.generationcp.breeding.manager.util.SelectedTabCloseHandler;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.Runo;

@Configurable
public class ListManagerTreeComponent extends VerticalLayout implements
		InternationalizableComponent, InitializingBean, Serializable {

	private static final long serialVersionUID = -224052511814636864L;
	private final static int BATCH_SIZE = 50;
	public final static String REFRESH_BUTTON_ID = "ListManagerTreeComponent Refresh Button";
	public static final String CLOSE_ALL_TABS_ID = "ListManagerTreeComponent Close All Tabs ID";
	
	private Tree germplasmListTree;
	private static TabSheet tabSheetGermplasmList;
    private AbsoluteLayout germplasmListBrowserMainLayout;
	private Button refreshButton;
	private Label heading;
	private Button btnCloseAllTabs;
	
    @Autowired
    private GermplasmListManager germplasmListManager;
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private ListManagerMain listManagerMain;
    
    private boolean forGermplasmListWindow;
    
    public ListManagerTreeComponent(AbsoluteLayout germplasmListBrowserMainLayout, boolean forGermplasmListWindow) {
        this.germplasmListBrowserMainLayout = germplasmListBrowserMainLayout;
        this.forGermplasmListWindow=forGermplasmListWindow;
    }

    public ListManagerTreeComponent(ListManagerMain listManagerMain, AbsoluteLayout germplasmListBrowserMainLayout, boolean forGermplasmListWindow) {
        this.listManagerMain = listManagerMain;
        this.germplasmListBrowserMainLayout = germplasmListBrowserMainLayout;
        this.forGermplasmListWindow=forGermplasmListWindow;
    }    
    
	@Override
	public void afterPropertiesSet() throws Exception {
		
		tabSheetGermplasmList = new TabSheet();
		
		germplasmListTree = new Tree();
		
		refreshButton = new Button();
		refreshButton.setData(REFRESH_BUTTON_ID);
		refreshButton.addListener(new GermplasmListButtonClickListener(this));
		refreshButton.setCaption(messageSource.getMessage(Message.REFRESH_LABEL));
		
		createTree();
		
		this.addComponent(germplasmListTree);
		this.addComponent(refreshButton);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

    public void createTree() {
        germplasmListTree = createGermplasmListTree();
        germplasmListTree.addStyleName("listManagerTree");
        
        germplasmListTree.setItemStyleGenerator(new ItemStyleGenerator() {
            @Override
            public String getStyle(Object itemId) {
            	if(itemId.equals("LOCAL") || itemId.equals("CENTRAL")){
            		return "listManagerTreeRootNode"; 
            	//} else if(germplasmListTree.hasChildren(itemId)){
            	} else if(isInteger((String) itemId.toString()) && hasChildList((Integer) itemId)){
            		return "listManagerTreeRegularParentNode";
            	} else {
            		return "listManagerTreeRegularChildNode";
            	}
            }
        });
        
        germplasmListTree.requestRepaint();
    }

    private Tree createGermplasmListTree() {
        List<GermplasmList> localGermplasmListParent = new ArrayList<GermplasmList>();
        List<GermplasmList> centralGermplasmListParent = new ArrayList<GermplasmList>();

        try {
            localGermplasmListParent = this.germplasmListManager.getAllTopLevelListsBatched(BATCH_SIZE, Database.LOCAL);
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
                createGermplasmListInfoTab(germplasmListId);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                    messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
        }catch (MiddlewareQueryException e){
            throw new InternationalizableException(e, Message.ERROR_DATABASE,
                    Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
        }
        
    }    
	
    private boolean hasChildList(int listId) {

        List<GermplasmList> listChildren = new ArrayList<GermplasmList>();

        try {
            listChildren = this.germplasmListManager.getGermplasmListByParentFolderId(listId, 0, 1);
        } catch (MiddlewareQueryException e) {
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
            e.printStackTrace();
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
    
    
    private void createGermplasmListInfoTab(int germplasmListId) throws MiddlewareQueryException {
    	VerticalLayout layout = new VerticalLayout();

        GermplasmList germplasmList=getGermplasmList(germplasmListId);
        
        if (!Util.isTabExist(tabSheetGermplasmList, germplasmList.getName())) {
        	ListManagerTreeMenu component = new ListManagerTreeMenu(this, germplasmListId,germplasmList.getName(),germplasmList.getStatus(), germplasmList.getUserId(), false, forGermplasmListWindow);
        	layout.addComponent(component);
            
            Tab tab = tabSheetGermplasmList.addTab(layout, germplasmList.getName(), null);
            tab.setClosable(true);
            
            if(tabSheetGermplasmList.getComponentCount() <= 1){
            	
            	//reset
            	germplasmListBrowserMainLayout.removeComponent(tabSheetGermplasmList);
            	
            	btnCloseAllTabs = new Button(messageSource.getMessage(Message.CLOSE_ALL_TABS));
            	btnCloseAllTabs.setData(CLOSE_ALL_TABS_ID);
            	btnCloseAllTabs.setImmediate(true);
            	btnCloseAllTabs.setStyleName(Reindeer.BUTTON_LINK);
            	btnCloseAllTabs.addListener(new GermplasmListButtonClickListener(this));
            	germplasmListBrowserMainLayout.addComponent(btnCloseAllTabs,"top:30px; left:1190px;");
            	
            	heading = new Label();
            	heading.setWidth("300px");
        		heading.setValue(messageSource.getMessage(Message.REVIEW_LIST_DETAILS));
        		heading.addStyleName("gcp-content-title");
        		germplasmListBrowserMainLayout.addComponent(heading,"top:30px; left:340px;");
        		
            	germplasmListBrowserMainLayout.addComponent(tabSheetGermplasmList, "top:55px;left:340px");
                germplasmListBrowserMainLayout.setWidth("98%");
                germplasmListBrowserMainLayout.setStyleName(Runo.TABSHEET_SMALL);
            }
            
            tabSheetGermplasmList.setSelectedTab(layout);
            tabSheetGermplasmList.setCloseHandler(new SelectedTabCloseHandler());
            tabSheetGermplasmList.addListener(new GermplasmListTabChangeListener(component));
        } else {
            Tab tab = Util.getTabAlreadyExist(tabSheetGermplasmList, germplasmList.getName());
            tabSheetGermplasmList.setSelectedTab(tab.getComponent());
        }
    }
    
    public void closeAllListDetailTabButtonClickAction() {
    	System.out.println("Close All Tabs..");
        Util.closeAllTab(tabSheetGermplasmList);
        germplasmListBrowserMainLayout.removeComponent(heading);
        germplasmListBrowserMainLayout.removeComponent(btnCloseAllTabs);
        germplasmListBrowserMainLayout.removeComponent(tabSheetGermplasmList);
    }
    
    private GermplasmList getGermplasmList(int germplasmListId) throws MiddlewareQueryException {
        return this.germplasmListManager.getGermplasmListById(germplasmListId);
    }
    
    public static boolean isInteger(String s) {
        try { 
            Integer.parseInt(s); 
        } catch(NumberFormatException e) { 
            return false; 
        }
        return true;
    }
    
    public TabSheet getTabSheetGermplasmList() {
        return tabSheetGermplasmList;
    }
}
