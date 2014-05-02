package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@Configurable
public class ListManagerMain extends AbsoluteLayout implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

    private static final long serialVersionUID = 5976245899964745758L;
    
    private static final String VERSION_STRING = "<h2>1.0.0</h2>";
    
    private AbsoluteLayout titleLayout;
    private Label mainTitle;
    private Button buildNewListButton;
    public static final String BUILD_NEW_LIST_BUTTON_DATA = "Build new list";
    
    // Tabs
    private HorizontalLayout tabHeaderLayout;
    private Button listSelectionTabButton;
    private Button plantSelectionTabButton;

    // The tab content will be split between a plant finder component and a list builder component
    private HorizontalSplitPanel splitPanel;
    
    private AbsoluteLayout plantFinderContent;
    private ListBuilderComponent listBuilderComponent;
    
    // You can toggle the plant selection content to display a list view, or a germplasm view
    private ListSelectionComponent listSelectionComponent;
    private PlantSelectionComponent plantSelectionComponent;
    
    // The split pane can be expanded and collapsed
	private static Float COLLAPSED_SPLIT_POSITION_RIGHT = Float.valueOf(94); //actual width in pixel 50
	private static Float MAX_EXPANDED_SPLIT_POSITION_RIGHT = Float.valueOf(50);
	
	private final Integer selectedListId;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    	
    public ListManagerMain(){
    	super();
    	this.selectedListId = null;
    }
    
    public ListManagerMain(final Integer selectedListId){
    	super();
    	this.selectedListId = selectedListId;
    }
	
    @Override
    public void afterPropertiesSet() throws Exception {
        instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
    }

	@Override
	public void updateLabels() {
		mainTitle.setValue(messageSource.getMessage(Message.LIST_MANAGER_SCREEN_LABEL) + "  " + VERSION_STRING);
	}

	@Override
	public void instantiateComponents() {
		setSizeFull(); 
        setTitleContent();
        setTabHeader();
        setTabContent();	
	}

	@Override
	public void initializeValues() {
		plantFinderContent.setWidth("100%");
		
		// By default, the list selection component will be opened first
        plantSelectionComponent.setVisible(false);
	}

	@Override
	public void addListeners() {
		
		listSelectionTabButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				showListSelection();
				selectTab(listSelectionTabButton);
				deselectTab(plantSelectionTabButton);
			}

		});
		
		plantSelectionTabButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				showPlantSelection();
				selectTab(plantSelectionTabButton);
				deselectTab(listSelectionTabButton);
			}
		});
	}

	@Override
	public void layoutComponents() {
		addComponent(titleLayout,"top:0px; left:10px");
		addComponent(tabHeaderLayout,"top:40px;left:10px;");
		addComponent(splitPanel,"top:65px;left:10px;");
	}

	/**
	 * Loads the specified list in the list builder. Ensures the list is not currently open anywhere else.
	 * 
	 * @param list the list to load for editing
	 */
    public void loadListForEditing(final GermplasmList list){
    	listSelectionComponent.updateUIForDeletedList(list);
    	listSelectionComponent.getListDetailsLayout().repaintTabsheet();
		
    	listBuilderComponent.editList(list);
    }

	/**
	 * Closes the specified list from any open views.
	 * 
	 * @param list the list to close
	 */
	public void closeList(final GermplasmList list) {
		listSelectionComponent.getListDetailsLayout().removeTab(list.getId());
	}

	/**
	 * Add selected plants to the list open in the list builder.
	 * @param sourceTable the table to retrieve the selected plants from
	 */
	public void addSelectedPlantsToList(Table sourceTable){
		listBuilderComponent.addFromListDataTable(sourceTable);
	}
	
	/**
	 * Add a plant to the list open in the list builder.
	 * @param gid ID of the germplasm to add
	 */
	public void addPlantToList(final Integer gid) {
		listBuilderComponent.addGermplasm(gid);
	}
	
	// TODO Helen ??
	public void showNodeOnTree(Integer listId){
		listSelectionComponent.getListTreeComponent().setListId(listId);
		listSelectionComponent.getListTreeComponent().createTree();
	}

	public ListBuilderComponent getBuildNewListComponent() {
		return listBuilderComponent;
	}
	
	public ListSelectionComponent getListSelectionComponent(){
		return listSelectionComponent;
	}
	
	protected void showPlantSelection() {
		plantFinderContent.setCaption("100%");
		
		plantFinderContent.removeAllComponents();
		plantFinderContent.addComponent(plantSelectionComponent);
		
		listSelectionComponent.setVisible(false);
		plantSelectionComponent.setVisible(true);
		
		plantFinderContent.requestRepaint();
	}

	protected void showListSelection() {
		plantFinderContent.setCaption("100%");
		
		plantFinderContent.removeAllComponents();
		plantFinderContent.addComponent(listSelectionComponent);
		
		listSelectionComponent.setVisible(true);
		plantSelectionComponent.setVisible(false);
		
		plantFinderContent.requestRepaint();
	}
	
	private void setTitleContent() {
		titleLayout = new AbsoluteLayout();
        titleLayout.setWidth("100%");
        titleLayout.setHeight("40px");
        
        mainTitle = new Label();
        mainTitle.setStyleName(Bootstrap.Typography.H1.styleName());
        mainTitle.setContentMode(Label.CONTENT_XHTML);
        mainTitle.setValue(messageSource.getMessage(Message.LIST_MANAGER_SCREEN_LABEL) + "  " + VERSION_STRING);
        
        buildNewListButton = new Button();
        buildNewListButton.setCaption(messageSource.getMessage(Message.START_A_NEW_LIST));
        buildNewListButton.setData(BUILD_NEW_LIST_BUTTON_DATA);
        buildNewListButton.setStyleName(Bootstrap.Buttons.INFO.styleName());
        buildNewListButton.setIcon(AppConstants.Icons.ICON_PLUS);
        
        titleLayout.addComponent(mainTitle,"top:0px;left:0px");
        titleLayout.addComponent(buildNewListButton,"top:10px;right:0px");
	}
	
	private void setTabHeader(){
        listSelectionTabButton = new Button(messageSource.getMessage(Message.BROWSE_LISTS));
        plantSelectionTabButton = new Button(messageSource.getMessage(Message.SEARCH_LISTS_AND_GERMPLASM));
        listSelectionTabButton.addStyleName("tabHeaderSelectedStyle");
        listSelectionTabButton.addStyleName("tabStyleButton");
        plantSelectionTabButton.addStyleName("tabStyleButton");
        listSelectionTabButton.setImmediate(true);
        plantSelectionTabButton.setImmediate(true);
        
        tabHeaderLayout = new HorizontalLayout();
        tabHeaderLayout.addStyleName("tabHeaderStyle");
        tabHeaderLayout.setSpacing(true);
        tabHeaderLayout.addComponent(listSelectionTabButton);
        tabHeaderLayout.addComponent(plantSelectionTabButton);
	}
	
	private void setTabContent(){
		splitPanel = new HorizontalSplitPanel();
		splitPanel.setMargin(false);
		splitPanel.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE);
		splitPanel.setMaxSplitPosition(COLLAPSED_SPLIT_POSITION_RIGHT, Sizeable.UNITS_PERCENTAGE);
		splitPanel.setMinSplitPosition(MAX_EXPANDED_SPLIT_POSITION_RIGHT, Sizeable.UNITS_PERCENTAGE);
		splitPanel.setImmediate(true);
		splitPanel.setWidth("100%");
		splitPanel.addStyleName("tabContainerStyle");
		
		listSelectionComponent = new ListSelectionComponent(this, selectedListId);
		plantSelectionComponent = new PlantSelectionComponent(this);
		
        plantFinderContent = new AbsoluteLayout();
        plantFinderContent.addComponent(listSelectionComponent,"top:0px;left:0px");
        plantFinderContent.addComponent(plantSelectionComponent,"top:0px;left:0px");
        
        listBuilderComponent = new ListBuilderComponent(this);
        
		splitPanel.setFirstComponent(plantFinderContent);
		splitPanel.setSecondComponent(listBuilderComponent);
		
		splitPanel.setHeight("610px");
		plantFinderContent.setHeight("98%");
		splitPanel.addStyleName("splitPanel");
		plantFinderContent.addStyleName("plantFinderContent");
		listBuilderComponent.setHeight("98%");
		
		addStyleName("lm-list-manager-main");
	}
	
	private void selectTab(final Button tabToSelect) {
		tabToSelect.removeStyleName("tabHeaderStyle");
		tabToSelect.addStyleName("tabHeaderSelectedStyle");
	}
	
	private void deselectTab(final Button tabToUnselect) {
		tabToUnselect.removeStyleName("tabHeaderSelectedStyle");
		tabToUnselect.addStyleName("tabHeaderStyle");
	}
}
