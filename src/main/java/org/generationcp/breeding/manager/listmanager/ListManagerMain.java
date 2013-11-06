package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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

    
	@Override
	public void afterPropertiesSet() throws Exception {
		titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        setTitleContent("");
        
        browseListsComponent = new ListManagerBrowseListsComponent();
        searchListsComponent = new ListManagerSearchListsComponent();
        
        tabSheet = new TabSheet();
        tabSheet.addTab(browseListsComponent, messageSource.getMessage(Message.BROWSE_LISTS));
        tabSheet.addTab(searchListsComponent, messageSource.getMessage(Message.SEARCH_LISTS_AND_GERMPLASM));
        tabSheet.setHeight("600px");

        addComponent(titleLayout);
        addComponent(tabSheet);
	}

	@Override
	public void updateLabels() {
		
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

}
