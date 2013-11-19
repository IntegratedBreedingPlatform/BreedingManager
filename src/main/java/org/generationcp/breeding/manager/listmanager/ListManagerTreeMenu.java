package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.util.ComponentTree;
import org.generationcp.breeding.manager.util.ComponentTree.ComponentTreeItem;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListManagerTreeMenu extends VerticalLayout implements InitializingBean, InternationalizableComponent {
	
	@SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ListManagerTreeMenu.class);
    private static final long serialVersionUID = -1409312205229461614L;
    
    private static final String LIST_DETAILS = "List Details";
    private static final String LIST_DATA = "List Data";
    private static final String LIST_SEED_INVENTORY = "List Seed Inventory";
	
    private int germplasmListId;
    private int germplasmListStatus;
    private String listName;
    private int userId;
    private ListDetailComponent listDetailComponent;
    
    private VerticalLayout layoutListData;
    private VerticalLayout layoutListDataInventory;
    
    private boolean fromUrl;    //this is true if this component is created by accessing the Germplasm List Details page directly from the URL
    
    private BreedingManagerApplication breedingManagerApplication;
//    private ListManagerTreeComponent listManagerTreeComponent;
    private ListManagerDetailsLayout detailsTabbedLayout;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    private boolean forGermplasmListWindow;
    private GermplasmList germplasmList;
	private ListDataComponent listDataComponent;
    
    public ListManagerTreeMenu(int germplasmListId,String listName,int germplasmListStatus,int userId, boolean fromUrl) {
        this.germplasmListId = germplasmListId;
        this.fromUrl = fromUrl;
        this.listName=listName;
        this.germplasmListStatus = germplasmListStatus;
        this.userId=userId;
    }

    public ListManagerTreeMenu(BreedingManagerApplication breedingManagerApplication, int germplasmListId,String listName, int userId, boolean fromUrl,boolean forGermplasmListWindow) {
        this.breedingManagerApplication = breedingManagerApplication;
        this.germplasmListId = germplasmListId;
        this.fromUrl = fromUrl;
        this.listName=listName;
        this.germplasmListStatus = 101;
        this.userId=userId;
        this.forGermplasmListWindow=forGermplasmListWindow;
    }
    
//    public ListManagerTreeMenu(ListManagerTreeComponent listManagerTreeComponent, int germplasmListId,String listName,int germplasmListStatus, int userId, boolean fromUrl,boolean forGermplasmListWindow) {
//        this.listManagerTreeComponent = listManagerTreeComponent;
//        this.germplasmListId = germplasmListId;
//        this.fromUrl = fromUrl;
//        this.listName=listName;
//        this.germplasmListStatus = germplasmListStatus;
//        this.userId=userId;
//        this.forGermplasmListWindow=forGermplasmListWindow;
//    }   
    
    public ListManagerTreeMenu(ListManagerDetailsLayout viewDetailsTabbedLayout, int germplasmListId,String listName,int germplasmListStatus, int userId, boolean fromUrl,boolean forGermplasmListWindow) {
        this.detailsTabbedLayout = viewDetailsTabbedLayout;
        this.germplasmListId = germplasmListId;
        this.fromUrl = fromUrl;
        this.listName=listName;
        this.germplasmListStatus = germplasmListStatus;
        this.userId=userId;
        this.forGermplasmListWindow=forGermplasmListWindow;
    } 
    
    public void refreshListData(){

    }
    
    public void selectedTabChangeAction() throws InternationalizableException{
        
    }
    
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
//		this.setSizeFull();
		setWidth("100%");
		setHeight("95%");
        listDetailComponent = new ListDetailComponent(this, germplasmListManager, germplasmListId, fromUrl);
        listDetailComponent.setData(LIST_DETAILS);
        
        listDataComponent = new ListDataComponent(germplasmListId,listName,userId,fromUrl,forGermplasmListWindow,germplasmListStatus, this);

        layoutListData = new VerticalLayout();
        layoutListData.setData(LIST_DATA);
        
        layoutListDataInventory = new VerticalLayout();
        layoutListDataInventory.setData(LIST_SEED_INVENTORY);
        
        ComponentTree content = new ComponentTree();
        content.setWidth("95%");
        
        ComponentTreeItem listDetails = content.addChild(createHeaderComponent(messageSource.getMessage(Message.LIST_DETAILS)));
        listDetails.showChild();
        ComponentTreeItem listDetailsContent = listDetails.addChild(listDetailComponent);
        
        ComponentTreeItem listData = content.addChild(createHeaderComponent(messageSource.getMessage(Message.LIST_DATA)));
        ComponentTreeItem listDataContent = listData.addChild(listDataComponent);
        
        ComponentTreeItem listSeedInventory = content.addChild(createHeaderComponent(messageSource.getMessage(Message.LIST_SEED_INVENTORY)));
        ComponentTreeItem listSeedInventoryContent = listSeedInventory.addChild(createHeaderComponent(messageSource.getMessage(Message.LIST_SEED_INVENTORY)));
        
        
        this.addComponent(content);
        
        //this.addTab(ListDetailComponent, messageSource.getMessage(Message.LIST_DETAILS)); 
        //this.addTab(layoutListData, messageSource.getMessage(Message.LIST_DATA)); 
        //this.addTab(layoutListDataInventory, messageSource.getMessage(Message.LIST_SEED_INVENTORY)); 
        
        //this.addListener(new GermplasmListSelectedTabChangeListener(this));
	}
	
	private Component createHeaderComponent (String header) {
        CssLayout l = new CssLayout();
        l.setWidth("200px");
        Label l1 = new Label("<b>" + header + "</b>",Label.CONTENT_XHTML);
        l.addComponent(l1);
        return l;
	}
	
	@Override
	public void attach() {
	    super.attach();
	    updateLabels();
	}
	
	public ListDetailComponent getListManagerListDetailComponent() {
	    return listDetailComponent;
	}
	
	public ListDataComponent getListManagerListDataComponent() {
	    return listDataComponent;
	}
	
	public BreedingManagerApplication getBreedingManagerApplication() {
	    return breedingManagerApplication;
	}
	
//	public ListManagerTreeComponent getListManagerTreeComponent() {
//	    return listManagerTreeComponent;
//	}
	
	public ListManagerDetailsLayout getDetailsLayout(){
		return this.detailsTabbedLayout;
	}

}
