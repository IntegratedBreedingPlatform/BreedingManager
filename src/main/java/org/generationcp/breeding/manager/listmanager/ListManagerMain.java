package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListManagerButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.ListManagerTabChangeListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListManagerMain extends VerticalLayout implements
		InternationalizableComponent, InitializingBean {

	private static final long serialVersionUID = -1014490637738627810L;
	private static final String VERSION = "1.0.0";

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private HorizontalLayout titleLayout;
    private Label mainTitle;
    private TabSheet tabSheet;
    
    private ListManagerBrowseListsComponent browseListsComponent;
    private ListManagerSearchListsComponent searchListsComponent;
    private BuildNewListComponent buildNewListComponent;
    
    private Label buildNewListTitle;
    
    private Button buildNewListButton;
    public static final String BUILD_NEW_LIST_BUTTON_DATA = "Build new list";
    
    private Integer listId;
    
    private static final ThemeResource ICON_PLUS = new ThemeResource("images/plus_icon.png");
    
    public ListManagerMain(){
    	super();
    	this.listId = null;
    }
    
    public ListManagerMain(Integer listId){
    	super();
    	this.listId = listId;
    }
    
	@Override
	public void afterPropertiesSet() throws Exception {
		titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        setTitleContent("");
        
        if(listId == null){
        	browseListsComponent = new ListManagerBrowseListsComponent(this);
        } else{
        	browseListsComponent = new ListManagerBrowseListsComponent(this, listId);
        }
        
        searchListsComponent = new ListManagerSearchListsComponent(this);
        
        buildNewListComponent = new BuildNewListComponent(this);
        
        tabSheet = new TabSheet();
        tabSheet.addTab(browseListsComponent, messageSource.getMessage(Message.BROWSE_LISTS));
        tabSheet.addTab(searchListsComponent, messageSource.getMessage(Message.SEARCH_LISTS_AND_GERMPLASM));
        tabSheet.setHeight("580px");
        tabSheet.addListener(new ListManagerTabChangeListener(this));
        
        HorizontalLayout buildNewActionBar = new HorizontalLayout();
        buildNewActionBar.setWidth("100%");
        buildNewActionBar.setHeight("30px");
        
		buildNewListTitle = new Label();
		buildNewListTitle.setValue(messageSource.getMessage(Message.BUILD_A_NEW_LIST));
		buildNewListTitle.addStyleName(Bootstrap.Typography.H3.styleName());
		buildNewListTitle.setVisible(false);
        
        buildNewActionBar.addComponent(buildNewListTitle);
        buildNewActionBar.setComponentAlignment(buildNewListTitle, Alignment.BOTTOM_LEFT);
        
        addComponent(titleLayout);
        addComponent(tabSheet);
        addComponent(buildNewActionBar);
	}

	@Override
	public void updateLabels() {
		
	}
	
	public void showBuildNewListComponent(){
		if(buildNewListButton.isVisible()){
			buildNewListButton.setVisible(false);
			
			//show the drop area in Browse List
			this.browseListsComponent.getListManagerTreeComponent().getDropHandlerComponent().enableDropHandler();
			
			//show the drop are in Search Germplasm List and Germplasm
			this.searchListsComponent.getSearchResultsComponent().getDropHandlerComponent().enableDropHandler();
			
			addComponent(buildNewListComponent);
			buildNewListTitle.setVisible(true);
			
			TextField listNameText =  buildNewListComponent.getListNameText();
			if(listNameText != null){
				listNameText.focus();
			}
			
		}
		
	}
	
	private void setTitleContent(String guideMessage){
        titleLayout.removeAllComponents();
        titleLayout.setSizeFull();
        
        //TODO put software version in title
        String title =  messageSource.getMessage(Message.LIST_MANAGER_SCREEN_LABEL)+ "  <h2>" + VERSION + "</h2>";
        mainTitle = new Label();
        mainTitle.setStyleName(Bootstrap.Typography.H1.styleName());
        mainTitle.setContentMode(Label.CONTENT_XHTML);
        mainTitle.setValue(title);
        titleLayout.addComponent(mainTitle);
        
        buildNewListButton = new Button();
        buildNewListButton.setCaption(messageSource.getMessage(Message.START_A_NEW_LIST));
        buildNewListButton.setData(BUILD_NEW_LIST_BUTTON_DATA);
        buildNewListButton.setStyleName(Bootstrap.Buttons.INFO.styleName());
        buildNewListButton.setIcon(ICON_PLUS);
        buildNewListButton.addListener(new GermplasmListManagerButtonClickListener(this));
        
        titleLayout.addComponent(buildNewListButton);
        titleLayout.setComponentAlignment(buildNewListButton, Alignment.MIDDLE_RIGHT);
        
        /**
        Label descLbl = new Label(guideMessage);
        descLbl.setWidth("300px");
        
        PopupView popup = new PopupView("?",descLbl);
        popup.setStyleName("gcp-popup-view");
        titleLayout.addComponent(popup);
        
        titleLayout.setComponentAlignment(popup, Alignment.MIDDLE_LEFT);
        **/
    }
	
	
	public ListManagerBrowseListsComponent getListManagerBrowseListsComponent(){
		return browseListsComponent;
	}
	
	public ListManagerSearchListsComponent getListManagerSearchListsComponent(){
		return searchListsComponent;
	}
	
	public BuildNewListComponent getBuildListComponent(){
		return buildNewListComponent;
	}

	public ListManagerBrowseListsComponent getBrowseListsComponent(){
		return browseListsComponent;
	}
	
	
}
