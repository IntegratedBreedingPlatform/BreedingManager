package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.util.ComponentTree;
import org.generationcp.breeding.manager.util.ComponentTree.ComponentTreeItem;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListManagerTreeMenu extends VerticalLayout implements InitializingBean, InternationalizableComponent {
	
	@SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ListManagerTreeMenu.class);
    private static final long serialVersionUID = -1409312205229461614L;
    
    public static final int TOGGABLE_Y_COORDINATE = 30;
    
    private static final String LIST_DETAILS = "List Details";
    private static final String LIST_DATA = "List Data";
    private static final String LIST_SEED_INVENTORY = "List Seed Inventory";
	
    private int germplasmListId;
    private int germplasmListStatus;
    private String listName;
    private int userId;
    private ListDetailComponent listDetailComponent;
    
    private ListInventoryComponent listInventoryComponent;
    private ListManagerMain listManagerMain;
    
    private boolean fromUrl;    //this is true if this component is created by accessing the Germplasm List Details page directly from the URL
    private boolean hasChanged = false;
    
    private BreedingManagerApplication breedingManagerApplication;
    private ListManagerDetailsLayout detailsTabbedLayout;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    private boolean forGermplasmListWindow;
    private ListDataComponent listDataComponent;
    
    private ComponentTreeItem listDetails;
    private ComponentTreeItem listData;
    private ComponentTreeItem listSeedInventory;
    
    
    public ListManagerTreeMenu(int germplasmListId,String listName,int germplasmListStatus,int userId, boolean fromUrl, ListManagerMain listManagerMain) {
        this.germplasmListId = germplasmListId;
        this.fromUrl = fromUrl;
        this.listName=listName;
        this.germplasmListStatus = germplasmListStatus;
        this.userId=userId;
        this.listManagerMain=listManagerMain;
    }

    public ListManagerTreeMenu(BreedingManagerApplication breedingManagerApplication, int germplasmListId,String listName, int userId, boolean fromUrl,boolean forGermplasmListWindow, ListManagerMain listManagerMain) {
        this.breedingManagerApplication = breedingManagerApplication;
        this.germplasmListId = germplasmListId;
        this.fromUrl = fromUrl;
        this.listName=listName;
        this.germplasmListStatus = 101;
        this.userId=userId;
        this.forGermplasmListWindow=forGermplasmListWindow;
        this.listManagerMain=listManagerMain;
    }
      
    public ListManagerTreeMenu(ListManagerDetailsLayout viewDetailsTabbedLayout, int germplasmListId,String listName,int germplasmListStatus, int userId, boolean fromUrl,boolean forGermplasmListWindow, ListManagerMain listManagerMain) {
        this.detailsTabbedLayout = viewDetailsTabbedLayout;
        this.germplasmListId = germplasmListId;
        this.fromUrl = fromUrl;
        this.listName=listName;
        this.germplasmListStatus = germplasmListStatus;
        this.userId=userId;
        this.forGermplasmListWindow=forGermplasmListWindow;
        this.listManagerMain=listManagerMain;
    } 
    
    public void refreshListData(){
    }
    
    public void selectedTabChangeAction() throws InternationalizableException{
    }
    
	@Override
	public void updateLabels() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setWidth("100%");
		setHeight("95%");
        listDetailComponent = new ListDetailComponent(this, germplasmListManager, germplasmListId, fromUrl);
        listDetailComponent.setData(LIST_DETAILS);
        
        listDataComponent = new ListDataComponent(this, germplasmListId,listName,userId,fromUrl,forGermplasmListWindow,germplasmListStatus, this, listManagerMain);
        listDataComponent.setData(LIST_DATA);
       
        listInventoryComponent = new ListInventoryComponent(germplasmListId);
        listInventoryComponent.setData(LIST_SEED_INVENTORY);
        
        ComponentTree content = new ComponentTree();
        content.setWidth("95%");
//        content.setMargin(true);
        
        listDetails = content.addChild(listDetailComponent.createBasicDetailsHeader(
        		messageSource.getMessage(Message.LIST_DETAILS)));
        listDetails.showChild();
        listDetails.addChild(listDetailComponent);
        listDetails.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					listDetails.toggleChild();
				}
			}
        });
        
        listData = content.addChild(Util.createHeaderComponent(messageSource.getMessage(Message.LIST_DATA)));
        listData.addChild(listDataComponent);
        listData.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					listData.toggleChild();
				}
			}
        });
        
        listSeedInventory = content.addChild(Util.createHeaderComponent(messageSource.getMessage(Message.LIST_SEED_INVENTORY)));
        listSeedInventory.addChild(listInventoryComponent);
        listSeedInventory.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					listSeedInventory.toggleChild();
				}
			}
        });
        
        
        this.addComponent(content);
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
	
	public ListManagerDetailsLayout getDetailsLayout(){
		return this.detailsTabbedLayout;
	}
	
	public boolean hasChanged() {
	    return hasChanged;
	}
	
	public void setChanged(boolean hasChanged) {
	    this.hasChanged = hasChanged;
	}

	public ListDetailComponent getListDetailComponent(){
		return listDetailComponent;
	}
	
}
