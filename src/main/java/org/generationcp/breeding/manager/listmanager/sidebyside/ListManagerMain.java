package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ListManagerMain extends VerticalLayout implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {
	
    private static final long serialVersionUID = 5976245899964745758L;
    
    private static final Logger LOG = LoggerFactory.getLogger(ListManagerMain.class);
    
    private static final String VERSION_STRING = "<h2>1.0.0</h2>";
    
    
    private AbsoluteLayout titleLayout;
    private Label mainTitle;
    public static final String BUILD_NEW_LIST_BUTTON_DATA = "Build new list";
    
    // Tabs
    private HorizontalLayout tabHeaderLayout;
    private Button listSelectionTabButton;
    private Button plantSelectionTabButton;

    protected Button listBuilderToggleBtn1;   // toggle on list
    protected Button listBuilderToggleBtn2;   // toggle on germplasm search

    // The tab content will be split between a plant finder component and a list builder component
    private HorizontalSplitPanel splitPanel;
    
    private AbsoluteLayout plantFinderContent;
    private ListBuilderComponent listBuilderComponent;
    
    // You can toggle the plant selection content to display a list view, or a germplasm view
    private ListSelectionComponent listSelectionComponent;
    private GermplasmSelectionComponent plantSelectionComponent;
	
	private final Integer selectedListId;
	
	//Handles Universal Mode View for ListManagerMain
	private ModeView modeView;
	private boolean hasChanges; //marks if there are unsaved changes in List from ListComponent and ListBuilderComponent 
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;

    
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
        listBuilderToggleBtn1 = new Button("<span class='bms-fa-chevron-left'" +
                "style='" +
                "position: relative;" +
                " bottom: 3px;" +
                "'></span>" + "Show List Builder");
        listBuilderToggleBtn1.setHtmlContentAllowed(true);
        listBuilderToggleBtn1.setStyleName(Bootstrap.Buttons.BORDERED.styleName() + " lm-toggle");

        listBuilderToggleBtn2 = new Button("<span class='bms-fa-chevron-left'" +
                "style='" +
                "position: relative;" +
                " bottom: 3px;" +
                "'></span>" + "Show List Builder");
        listBuilderToggleBtn2.setHtmlContentAllowed(true);
        listBuilderToggleBtn2.setStyleName(Bootstrap.Buttons.BORDERED.styleName() + " lm-toggle");

        modeView = ModeView.LIST_VIEW;
        hasChanges = false;
        
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


        listBuilderToggleBtn1.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -8178708255873293566L;

			@Override
            public void buttonClick(ClickEvent event) {
                ListManagerMain.this.toggleListBuilder();
            }
        });


        listBuilderToggleBtn2.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 4202348847712247508L;

			@Override
            public void buttonClick(ClickEvent event) {
                ListManagerMain.this.toggleListBuilder();
            }
        });
	}

    @Override
	public void layoutComponents() {

        final VerticalLayout titleAndTabContainer = new VerticalLayout();
        titleAndTabContainer.setMargin(new MarginInfo(false,false,false,true));
        titleAndTabContainer.setSpacing(true);

        titleAndTabContainer.addComponent(titleLayout);
        titleAndTabContainer.addComponent(tabHeaderLayout);

        this.addComponent(titleAndTabContainer);

        final Panel splitPanelContainer = new Panel();
        splitPanelContainer.setScrollable(true);
        splitPanelContainer.setSizeFull();
        splitPanelContainer.setStyleName(Reindeer.PANEL_LIGHT + " lm-panel");

        splitPanelContainer.setContent(splitPanel);

        this.addComponent(splitPanelContainer);
        this.setExpandRatio(splitPanelContainer,1.0F);

        //this.setStyleName("green");
        this.setMargin(false);
        this.setSpacing(false);
	}

	/**
	 * Loads the specified list in the list builder. Ensures the list is not currently open anywhere else.
	 *
	 * @param list the list to load for editing
	 */
    public void loadListForEditing(final GermplasmList list){
    	updateUIForEditingList(list);
    	listSelectionComponent.getListDetailsLayout().repaintTabsheet();
    	listBuilderComponent.editList(list);
    	showListBuilder();
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

	public void showNodeOnTree(Integer listId){
		listSelectionComponent.getListTreeComponent().setListId(listId);
		listSelectionComponent.getListTreeComponent().createTree();
	}

	public ListBuilderComponent getListBuilderComponent() {
		return listBuilderComponent;
	}

	public ListSelectionComponent getListSelectionComponent(){
		return listSelectionComponent;
	}

	public GermplasmSelectionComponent getPlantSelectionComponent(){
		return plantSelectionComponent;
	}

	protected void showPlantSelection() {

		plantFinderContent.setCaption("100%");

		plantFinderContent.removeAllComponents();
		plantFinderContent.addComponent(plantSelectionComponent);

		listSelectionComponent.setVisible(false);
		plantSelectionComponent.setVisible(true);
		plantSelectionComponent.getSearchBarComponent().focusOnSearchField();

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

        //buildNewListButton = new Button();
        //buildNewListButton.setCaption(messageSource.getMessage(Message.START_A_NEW_LIST));
        //buildNewListButton.setData(BUILD_NEW_LIST_BUTTON_DATA);
        //buildNewListButton.setStyleName(Bootstrap.Buttons.INFO.styleName());
        //buildNewListButton.setIcon(AppConstants.Icons.ICON_PLUS);

        titleLayout.addComponent(mainTitle,"top:0px;left:0px");
        //titleLayout.addComponent(buildNewListButton,"top:10px;right:0px");
	}

	private void setTabHeader(){
        listSelectionTabButton = new Button(messageSource.getMessage(Message.VIEW_LISTS));
        plantSelectionTabButton = new Button(messageSource.getMessage(Message.VIEW_GERMPLASM));
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
		//splitPanel.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE);
		splitPanel.setMaxSplitPosition(50, Sizeable.UNITS_PERCENTAGE);
		//splitPanel.setMinSplitPosition(, Sizeable.UNITS_PERCENTAGE);
		splitPanel.setSplitPosition(0,Sizeable.UNITS_PIXELS,true);

        splitPanel.setImmediate(true);
        splitPanel.setStyleName(Reindeer.SPLITPANEL_SMALL);
        splitPanel.addStyleName("tabContainerStyle");



        listSelectionComponent = new ListSelectionComponent(this, selectedListId);
		plantSelectionComponent = new GermplasmSelectionComponent(this);

        plantFinderContent = new AbsoluteLayout();
        plantFinderContent.addComponent(listSelectionComponent,"top:0px;left:0px");
        plantFinderContent.addComponent(plantSelectionComponent,"top:0px;left:0px");

        listBuilderComponent = new ListBuilderComponent(this);

		splitPanel.setFirstComponent(plantFinderContent);
		splitPanel.setSecondComponent(listBuilderComponent);

        splitPanel.setWidth("100%");
        splitPanel.setHeight("780px");

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


	public void updateUIForEditingList(GermplasmList list) {
		//Check if tab for deleted list is opened
		listSelectionComponent.getListDetailsLayout().removeTab(list.getId());
	}

	public void updateUIForDeletedList(GermplasmList list) {

		//Check if tab for deleted list is opened
		listSelectionComponent.getListDetailsLayout().removeTab(list.getId());

		//Check if deleted list is currently being edited in the list builder
		if(getListBuilderComponent().getCurrentlySetGermplasmListInfo()!=null
			&& list!=null
			&& getListBuilderComponent().getCurrentlySetGermplasmListInfo().getId() == list.getId()){
			getListBuilderComponent().resetList();
		}

		//Check if deleted list is in the search results
		listSelectionComponent.getListSearchComponent().getSearchResultsComponent().removeSearchResult(list.getId());
	}


	public Boolean lockGermplasmList(GermplasmList germplasmList){
	    if(!germplasmList.isLockedList()){
		    germplasmList.setStatus(germplasmList.getStatus()+100);
		    try {
		        germplasmListManager.updateGermplasmList(germplasmList);

		        User user = workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
		        ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()),
		                workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()),
		                "Locked a germplasm list.",
		                "Locked list "+germplasmList.getId()+" - "+germplasmList.getName(),
		                user,
		                new Date());
		        workbenchDataManager.addProjectActivity(projAct);
		        return true;
		    } catch (MiddlewareQueryException e) {
		        LOG.error("Error with locking list.", e);
	            MessageNotifier.showError(getWindow(), "Database Error!", "Error with locking list. " + messageSource.getMessage(Message.ERROR_REPORT_TO)
	                    , Notification.POSITION_CENTERED);
	            return false;
		    }
		}
	    return false;
	}

    private boolean isListBuilderShown = false;

    public void setUIForLockedListBuilder(){
    	plantSelectionComponent.getSearchResultsComponent().setRightClickActionHandlerEnabled(false);
    	listSelectionComponent.getListSearchComponent().getSearchResultsComponent().refreshActionHandler();
    }

    public void setUIForUnlockedListBuilder(){
    	plantSelectionComponent.getSearchResultsComponent().setRightClickActionHandlerEnabled(true);
    	listSelectionComponent.getListSearchComponent().getSearchResultsComponent().refreshActionHandler();
    }

    public Boolean unlockGermplasmList(GermplasmList germplasmList){
	    if(germplasmList.isLockedList()){
		    germplasmList.setStatus(germplasmList.getStatus() - 100);
		    try {
		        germplasmListManager.updateGermplasmList(germplasmList);

		        User user = workbenchDataManager.getUserById(workbenchDataManager.getWorkbenchRuntimeData().getUserId());
		        ProjectActivity projAct = new ProjectActivity(new Integer(workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()).getProjectId().intValue()),
		                workbenchDataManager.getLastOpenedProject(workbenchDataManager.getWorkbenchRuntimeData().getUserId()),
		                "Unlocked a germplasm list.",
		                "Unlocked list "+germplasmList.getId()+" - "+germplasmList.getName(),
		                user,
		                new Date());
		        workbenchDataManager.addProjectActivity(projAct);
		        return true;
		    } catch (MiddlewareQueryException e) {
		        LOG.error("Error with unlocking list.", e);
	            MessageNotifier.showError(getWindow(), "Database Error!", "Error with unlocking list. " + messageSource.getMessage(Message.ERROR_REPORT_TO)
	                    , Notification.POSITION_CENTERED);
	            return false;
		    }
		}
	    return false;
	}

    public void toggleListBuilder() {
        if (!isListBuilderShown) {
           showListBuilder();
        }
        else {
        	hideListBuilder();
        }

        listSelectionComponent.getListDetailsLayout().repaintTabsheet();
    }

    public void showListBuilder() {
        splitPanel.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE,true);

        String hideTxt = "<span class='bms-fa-chevron-right'" +
                "style='position: relative;" +
                " bottom: 3px;'" +
                "'></span>" + "Hide List Builder";

        listBuilderToggleBtn1.setCaption(hideTxt);
        listBuilderToggleBtn2.setCaption(hideTxt);
        
        isListBuilderShown = true;
    }

    public void hideListBuilder(){
    	splitPanel.setSplitPosition(0,Sizeable.UNITS_PIXELS,true);

        String showTxt = "<span class='bms-fa-chevron-left'" +
                "style='position: relative;" +
                " bottom: 3px;'" +
                "'></span>" + "Show List Builder";

        listBuilderToggleBtn1.setCaption(showTxt);
        listBuilderToggleBtn2.setCaption(showTxt);
        
        isListBuilderShown = false;
    }
	
    public Integer getListBuilderStatus(){
    	if(listBuilderComponent!=null && listBuilderComponent.getCurrentlySavedGermplasmList()!=null)
    		return listBuilderComponent.getCurrentlySavedGermplasmList().getStatus();
    	return 0;
    }
    
    public Boolean listBuilderIsLocked(){
    	if(getListBuilderStatus()>100)
    		return true;
    	return false;
    }

	public ModeView getModeView() {
		return modeView;
	}

	public void setModeView(ModeView newModeView) {
		String message = "";
		
		if(this.modeView != newModeView){
			if(hasChanges){
				if(this.modeView.equals(ModeView.LIST_VIEW) && newModeView.equals(ModeView.INVENTORY_VIEW)){
					
					modeView = newModeView;
					
					message = "You have unsaved changes to one or more lists. Do you want to save them before changing views?";
					
		    		ConfirmDialog.show(getWindow(), "Unsaved Changes", message, "Yes", "No", new ConfirmDialog.Listener() {
		    			
						private static final long serialVersionUID = 1L;	
						@Override
						public void onClose(ConfirmDialog dialog) {
							if (dialog.isConfirmed()) {
								saveAllListChangesAction();	
							}
							else{
								//cancel all the unsaved changes
								listSelectionComponent.getListDetailsLayout().resetListViewForCancelledChanges();
								listSelectionComponent.getListDetailsLayout().updateViewForAllLists(modeView);
								
								if(listBuilderComponent.getCurrentlySavedGermplasmList() != null){
									//Has currently saved List, just load the previous lots
									listBuilderComponent.viewInventoryActionConfirmed();
								}
								else{
									//if no list save, just reset the list
									listBuilderComponent.resetList();
								}
								
								resetUnsavedStatus();
							}
						}
					});
				}
				else if(this.modeView.equals(ModeView.INVENTORY_VIEW) && newModeView.equals(ModeView.LIST_VIEW)){
					
					modeView = newModeView;
					
					message = "You have unsaved reservations to one or more lists. Do you want to save them before changing views?";
					
					ConfirmDialog.show(getWindow(), "Unsaved Changes", message, "Yes", "No", new ConfirmDialog.Listener() {
		    			
						private static final long serialVersionUID = 1L;	
						@Override
						public void onClose(ConfirmDialog dialog) {
							if (dialog.isConfirmed()) {
								saveAllListChangesAction();	
							}
							else{
								//cancel all the unsaved changes
								listSelectionComponent.getListDetailsLayout().resetListViewForCancelledChanges();
								listSelectionComponent.getListDetailsLayout().updateViewForAllLists(modeView);
								
								if(listBuilderComponent.getCurrentlySavedGermplasmList() != null){
									listBuilderComponent.changeToListView();
								}
								else{
									listBuilderComponent.resetList();
								}
								
								resetUnsavedStatus();
							}
						}
					});
				}
			}
			else{
				modeView = newModeView;
				updateView(modeView);
			}
		}
		
	}

	public void updateView(ModeView modeView) {
		listSelectionComponent.getListDetailsLayout().updateViewForAllLists(modeView);
		
		if(modeView.equals(ModeView.INVENTORY_VIEW)){
			listBuilderComponent.viewInventoryActionConfirmed();
		}
		else if(modeView.equals(ModeView.LIST_VIEW)){
			listBuilderComponent.changeToListView();
		}
			
	}

	public void saveAllListChangesAction() {
		
		if(getListSelectionComponent().getListDetailsLayout().hasUnsavedChanges()){
			Map<ListComponent,Boolean> listToUpdate = new HashMap<ListComponent, Boolean>(); 
			listToUpdate.putAll(listSelectionComponent.getListDetailsLayout().getListStatusForChanges());
			
			for(Map.Entry<ListComponent, Boolean> list : listToUpdate.entrySet()){
				Boolean isListHasUnsavedChanges = list.getValue();
				if(isListHasUnsavedChanges){
					ListComponent toSave = list.getKey();
					toSave.saveChangesAction();
				}
			}
		}
		
		if(listBuilderComponent.hasUnsavedChanges()){
			//Save all changes in ListBuilder
			GermplasmList currentlySavedGermplasmList = listBuilderComponent.getCurrentlySavedGermplasmList();
			if(currentlySavedGermplasmList == null){
				listBuilderComponent.openSaveListAsDialog();
			}
			else{
				listBuilderComponent.getSaveListButtonListener().doSaveAction();
				//Change ListBuilder View to List View
				listBuilderComponent.viewInventoryActionConfirmed();
			}	
		}
		
		resetUnsavedStatus();
		updateView(modeView);
	}
	
	public void resetUnsavedStatus(){
		listSelectionComponent.getListDetailsLayout().updateHasChangesForAllList(false);
		listBuilderComponent.setHasUnsavedChanges(false);
	}

	public boolean hasUnsavedChanges() {
		return hasChanges;
	}

	public void setHasUnsavedChanges(boolean hasChanges) {
		this.hasChanges = hasChanges;
	}
    
}
