package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListManagerButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
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
    
    private Button buildNewListButton;
    public static final String BUILD_NEW_LIST_BUTTON_DATA = "Build new list";
    
	@Override
	public void afterPropertiesSet() throws Exception {
		titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        setTitleContent("");
        
        browseListsComponent = new ListManagerBrowseListsComponent();
        searchListsComponent = new ListManagerSearchListsComponent();
        
        buildNewListComponent = new BuildNewListComponent(this);
        
        tabSheet = new TabSheet();
        tabSheet.addTab(browseListsComponent, messageSource.getMessage(Message.BROWSE_LISTS));
        tabSheet.addTab(searchListsComponent, messageSource.getMessage(Message.SEARCH_LISTS_AND_GERMPLASM));
        tabSheet.setHeight("580px");

        buildNewListButton = new Button();
        buildNewListButton.setCaption(messageSource.getMessage(Message.BUILD_A_NEW_LIST));
        buildNewListButton.setData(BUILD_NEW_LIST_BUTTON_DATA);
        buildNewListButton.addListener(new GermplasmListManagerButtonClickListener(this));
        
        addComponent(titleLayout);
        addComponent(tabSheet);
        addComponent(buildNewListButton);
	}

	@Override
	public void updateLabels() {
		
	}
	
	public void showBuildNewListComponent(){
		buildNewListButton.setVisible(false);
		addComponent(buildNewListComponent);
	}
	
	private void setTitleContent(String guideMessage){
        titleLayout.removeAllComponents();
        
        //TODO put software version in title
        String title =  "<h1>" + messageSource.getMessage(Message.LIST_MANAGER_SCREEN_LABEL)+ "</h1> <h2>" + VERSION + "</h2>";
        mainTitle = new Label();
        mainTitle.setStyleName("gcp-window-title");
        mainTitle.setContentMode(Label.CONTENT_XHTML);
        mainTitle.setValue(title);
        titleLayout.addComponent(mainTitle);
        
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

}
