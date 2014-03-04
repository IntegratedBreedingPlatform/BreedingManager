package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

@Configurable
public class ListManagerSidebysideMain extends AbsoluteLayout implements
		InternationalizableComponent, InitializingBean, BreedingManagerLayout {

    private static final long serialVersionUID = 5976245899964745758L;
    private static final String VERSION = "1.0.0";
    
    private AbsoluteLayout titleLayout;
    private Label mainTitle;
    private Button buildNewListButton;
    public static final String BUILD_NEW_LIST_BUTTON_DATA = "Build new list";
    private static final ThemeResource ICON_PLUS = new ThemeResource("images/plus_icon.png");
    
    HorizontalLayout headerLayout;
    private Panel containerPanel;
    private Button showBrowseListsButton;
    private Button showSearchListsButton;
    private Button toggleLeftButton;
    private Button toggleRightButton;
    private HorizontalSplitPanel splitPanel;
	
    AbsoluteLayout browserSearchLayout;
    HorizontalLayout buildListLayout;
    
    private static Float EXPANDED_SPLIT_POSITION_LEFT = Float.valueOf("96");
	private static Float COLLAPSED_SPLIT_POSITION_LEFT = Float.valueOf("4");
	
	private static Float EXPANDED_SPLIT_POSITION_RIGHT = Float.valueOf("50");
	private static Float COLLAPSED_SPLIT_POSITION_RIGHT = Float.valueOf("96");

    private ListManagerBrowseListComponent browseListsComponent;
    private ListManagerSearchListComponent searchListsComponent;
        
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
    @Override
    public void afterPropertiesSet() throws Exception {
    	
        instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
		
		collapseRight();
    }

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
	}

	@SuppressWarnings("deprecation")
	@Override
	public void instantiateComponents() {
		setSizeFull(); 
		
		//title
		titleLayout = new AbsoluteLayout();
        setTitleContent("");
        
        //header
        showBrowseListsButton = new Button(messageSource.getMessage(Message.BROWSE_LISTS));
        showSearchListsButton = new Button(messageSource.getMessage(Message.SEARCH_LISTS_AND_GERMPLASM));

        showBrowseListsButton.addStyleName("tabStyleButton");
        showSearchListsButton.addStyleName("tabStyleButton");
        
        headerLayout = new HorizontalLayout();
        headerLayout.addStyleName("tabHeaderStyle");
        headerLayout.setSpacing(true);
        headerLayout.addComponent(showBrowseListsButton);
        headerLayout.addComponent(showSearchListsButton);
        
        splitPanel = new HorizontalSplitPanel();
		splitPanel.setSizeFull();
		splitPanel.setMargin(false);
        
		//content
        containerPanel = new Panel();
        containerPanel.setLayout(splitPanel);
        toggleLeftButton = new Button();
		toggleRightButton = new Button();
		
        browseListsComponent = new ListManagerBrowseListComponent();
        searchListsComponent = new ListManagerSearchListComponent();
        
        browserSearchLayout = new AbsoluteLayout();
        browserSearchLayout.addStyleName("leftPane");
        browserSearchLayout.addComponent(browseListsComponent,"top:0px;left:0px");
        browserSearchLayout.addComponent(searchListsComponent,"top:0px;left:0px");
        browserSearchLayout.addComponent(toggleLeftButton,"top:0px;right:0px");
        splitPanel.setFirstComponent(browserSearchLayout);
		
		buildListLayout = new HorizontalLayout();
		buildListLayout.setSpacing(true);
		buildListLayout.addComponent(toggleRightButton);
		buildListLayout.addComponent(new BuildNewListComponentSidebyside());
		splitPanel.setSecondComponent(buildListLayout);
		
	}

	private void setTitleContent(String string) {
		titleLayout.removeAllComponents();
        titleLayout.setWidth("100%");
        titleLayout.setHeight("40px");
        
        //TODO put software version in title
        String title =  messageSource.getMessage(Message.LIST_MANAGER_SCREEN_LABEL)+ "  <h2>" + VERSION + "</h2>";
        mainTitle = new Label();
        mainTitle.setStyleName(Bootstrap.Typography.H1.styleName());
        mainTitle.setContentMode(Label.CONTENT_XHTML);
        mainTitle.setValue(title);
        
        buildNewListButton = new Button();
        buildNewListButton.setCaption(messageSource.getMessage(Message.START_A_NEW_LIST));
        buildNewListButton.setData(BUILD_NEW_LIST_BUTTON_DATA);
        buildNewListButton.setStyleName(Bootstrap.Buttons.INFO.styleName());
        buildNewListButton.setIcon(ICON_PLUS);
        
        titleLayout.addComponent(mainTitle,"top:0px;left:0px");
        titleLayout.addComponent(buildNewListButton,"top:10px;right:0px");
	}

	@Override
	public void initializeValues() {
		browserSearchLayout.setWidth("100%");
        browserSearchLayout.setHeight("600px");
		
        searchListsComponent.setVisible(false);
        
		toggleLeftButton.setCaption("<<");
		toggleRightButton.setCaption(">>");
	}

	@Override
	public void addListeners() {
		
		showBrowseListsButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				showBrowseListPane();
			}

		});
		
		showSearchListsButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				showSearchListPane();
			}
		});
		
		
		toggleLeftButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				if(splitPanel.getSplitPosition() == EXPANDED_SPLIT_POSITION_LEFT){
					collapseLeft();
				} else {
					expandLeft();
				}
			}
		});
		
		toggleRightButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				if(splitPanel.getSplitPosition() == EXPANDED_SPLIT_POSITION_RIGHT || splitPanel.getSplitPosition() == COLLAPSED_SPLIT_POSITION_LEFT){
					collapseRight();
				} else {
					expandRight();
				}
			}
		});
		
		buildNewListButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				expandRight();
			}
		});
	}

	protected void showSearchListPane() {
		browseListsComponent.setVisible(false);
		searchListsComponent.setVisible(true);
	}

	protected void showBrowseListPane() {
		browseListsComponent.setVisible(true);
		searchListsComponent.setVisible(false);
	}

	@Override
	public void layoutComponents() {
		addComponent(titleLayout,"top:10px; left:10px");
		addComponent(headerLayout,"top:50px;left:10px;");
		addComponent(containerPanel,"top:75px;left:10px;");
	}

    private void expandLeft(){
    	splitPanel.setSplitPosition(EXPANDED_SPLIT_POSITION_LEFT);
    	toggleLeftButton.setCaption("<<");
    	toggleRightButton.setCaption("<<");
    }

    private void collapseLeft(){
    	splitPanel.setSplitPosition(COLLAPSED_SPLIT_POSITION_LEFT);
    	toggleLeftButton.setCaption(">>");
    	toggleRightButton.setCaption(">>");
    }
	
    private void expandRight(){
    	splitPanel.setSplitPosition(EXPANDED_SPLIT_POSITION_RIGHT);
    	toggleLeftButton.setCaption(">>");
    	toggleRightButton.setCaption(">>");
    }

    private void collapseRight(){
    	splitPanel.setSplitPosition(COLLAPSED_SPLIT_POSITION_RIGHT);
    	toggleLeftButton.setCaption("<<");
    	toggleRightButton.setCaption("<<");
    }
}
