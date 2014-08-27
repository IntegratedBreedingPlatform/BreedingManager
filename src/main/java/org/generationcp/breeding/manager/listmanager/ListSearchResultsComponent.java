package org.generationcp.breeding.manager.listmanager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.listmanager.listeners.ListSearchResultsItemClickListener;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListSearchResultsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 5314653969843976836L;
	
	private ListManagerMain source;
	
	private Label totalMatchingListsLabel;
	private Label totalSelectedMatchingListsLabel;
	
	private Button actionButton;
	private Table matchingListsTable;
	private TableWithSelectAllLayout matchingListsTableWithSelectAll;

	private static final String CHECKBOX_COLUMN_ID = "Tag All Column";
	private static final String NAME_ID = "NAME";
	private static final String DESCRIPTION_ID = "DESCRIPTION";
	
	public static final String MATCHING_LISTS_TABLE_DATA = "Matching Lists Table";
	public static final String TOOLS_BUTTON_ID = "Actions";

	static final Action ACTION_SELECT_ALL = new Action("Select All");
	static final Action ACTION_ADD_TO_NEW_LIST = new Action("Add Selected List(s) to New List");
	static final Action[] LISTS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_ADD_TO_NEW_LIST };
	static final Action[] LISTS_TABLE_CONTEXT_MENU_LOCKED = new Action[] { ACTION_SELECT_ALL };
	
	private Action.Handler lockedRightClickActionHandler;
	private Action.Handler unlockedRightClickActionHandler;
	
	//Tools Button Context Menu
    private ContextMenu menu;
    private ContextMenuItem menuSelectAll;
    private ContextMenuItem menuAddToNewList;

	private final org.generationcp.breeding.manager.listmanager.ListSelectionLayout displayDetailsLayout;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
    protected GermplasmListManager germplasmListManager;
	
    private Map<Integer, GermplasmList> germplasmListsMap;
	
	public ListSearchResultsComponent(ListManagerMain source, final ListSelectionLayout displayDetailsLayout) {
		this.source = source;
		this.displayDetailsLayout = displayDetailsLayout;
	}

	@Override
	public void updateLabels() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		
		totalMatchingListsLabel = new Label("",Label.CONTENT_XHTML);
		totalMatchingListsLabel.setWidth("120px");	
		updateNoOfEntries(0);
		
		totalSelectedMatchingListsLabel = new Label("",Label.CONTENT_XHTML);
		totalSelectedMatchingListsLabel.setWidth("95px");
		updateNoOfSelectedEntries(0);
		
		actionButton = new ActionButton();
		actionButton.setData(TOOLS_BUTTON_ID);
        
        //Action Button ContextMenu
        menu = new ContextMenu();
        menu.setWidth("300px");
        
        menuAddToNewList = menu.addItem(messageSource.getMessage(Message.ADD_SELECTED_LIST_TO_NEW_LIST));
        menuSelectAll = menu.addItem(messageSource.getMessage(Message.SELECT_ALL));
        
        updateActionMenuOptions(false);
        
		matchingListsTableWithSelectAll = new TableWithSelectAllLayout(5,
				CHECKBOX_COLUMN_ID);
		matchingListsTableWithSelectAll.setHeight("100%");
		matchingListsTable = matchingListsTableWithSelectAll.getTable();
		matchingListsTable.setData(MATCHING_LISTS_TABLE_DATA);
		matchingListsTable.addContainerProperty(CHECKBOX_COLUMN_ID,
				CheckBox.class, null);
		matchingListsTable.addContainerProperty(NAME_ID, String.class, null);
		matchingListsTable.addContainerProperty(DESCRIPTION_ID, String.class,null);
		matchingListsTable.setHeight("260px");
		matchingListsTable.setWidth("100%");
		matchingListsTable.setMultiSelect(true);
		matchingListsTable.setSelectable(true);
		matchingListsTable.setImmediate(true);
		matchingListsTable.setDragMode(TableDragMode.ROW);
		matchingListsTable.addListener(new ListSearchResultsItemClickListener(displayDetailsLayout));
		messageSource.setColumnHeader(matchingListsTable, CHECKBOX_COLUMN_ID,
				Message.CHECK_ICON);
		
		updateGermplasmListsMap();
		addSearchListResultsItemDescription();

		lockedRightClickActionHandler = new Action.Handler() {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if(ACTION_SELECT_ALL == action){
					matchingListsTable.setValue(matchingListsTable.getItemIds());
				}
			}
			
			@Override
			public Action[] getActions(Object target, Object sender) {
				return LISTS_TABLE_CONTEXT_MENU_LOCKED;
			}
		};
		
		unlockedRightClickActionHandler = new Action.Handler() {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if(ACTION_SELECT_ALL == action){
					matchingListsTable.setValue(matchingListsTable.getItemIds());
				}
				else if(ACTION_ADD_TO_NEW_LIST == action){
					addSelectedListToNewList();
				}
			}
			
			@Override
			public Action[] getActions(Object target, Object sender) {
				
				if(source.getModeView().equals(ModeView.INVENTORY_VIEW)){
					return LISTS_TABLE_CONTEXT_MENU_LOCKED;
				}
				else{
					return LISTS_TABLE_CONTEXT_MENU;
				}
			}
		};
		
		addActionHandler();
	}
	
	private void updateActionMenuOptions(boolean status) {
		menuAddToNewList.setEnabled(status);
		menuSelectAll.setEnabled(status);
	}

	public void updateGermplasmListsMap(){
		germplasmListsMap = Util.getAllGermplasmLists(germplasmListManager);
	}
	
	private void addSearchListResultsItemDescription(){
		matchingListsTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            private static final long serialVersionUID = -2669417630841097077L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
            	GermplasmList germplasmList;
				
            	try {
					germplasmList = germplasmListsMap.get(Integer.valueOf(itemId.toString()));
					if(germplasmList != null){
						ViewListHeaderWindow viewListHeaderWindow = new ViewListHeaderWindow(germplasmList);
						return viewListHeaderWindow.getListHeaderComponent().toString();
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                return "";
            }
        });
	}
	
	private void addActionHandler(){
		refreshActionHandler();
	}
	
	public void refreshActionHandler(){
		matchingListsTable.removeActionHandler(lockedRightClickActionHandler);
		matchingListsTable.removeActionHandler(unlockedRightClickActionHandler);
		
		if(source.listBuilderIsLocked()){
			matchingListsTable.addActionHandler(lockedRightClickActionHandler);
			menuAddToNewList.setVisible(false);
		} else {
			matchingListsTable.addActionHandler(unlockedRightClickActionHandler);
			menuAddToNewList.setVisible(true);
		}
	}

	
	
	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
		
		menu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void contextItemClick(
					org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
				ContextMenuItem clickedItem = event.getClickedItem();
				
				if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
				    matchingListsTable.setValue(matchingListsTable.getItemIds());
				}else if(clickedItem.getName().equals(messageSource.getMessage(Message.ADD_SELECTED_LIST_TO_NEW_LIST))){
				  	addSelectedListToNewList();
				}      
			}
		});
		
		actionButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1345004576139547723L;

            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                menu.show(event.getClientX(), event.getClientY());
            }
		});
		
		matchingListsTable.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				updateNoOfSelectedEntries();
			}
		});
	}

	@Override
	public void layoutComponents() {
		
		setWidth("100%");
		setSpacing(true);
		
		HorizontalLayout leftHeaderLayout = new HorizontalLayout();
		leftHeaderLayout.setSpacing(true);
		leftHeaderLayout.addComponent(totalMatchingListsLabel);
		leftHeaderLayout.addComponent(totalSelectedMatchingListsLabel);
		leftHeaderLayout.setComponentAlignment(totalMatchingListsLabel, Alignment.MIDDLE_LEFT);
		leftHeaderLayout.setComponentAlignment(totalSelectedMatchingListsLabel, Alignment.MIDDLE_LEFT);
		
		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setWidth("100%");
		headerLayout.addComponent(leftHeaderLayout);
		headerLayout.addComponent(actionButton);
		headerLayout.setComponentAlignment(leftHeaderLayout, Alignment.BOTTOM_LEFT);
		headerLayout.setComponentAlignment(actionButton, Alignment.BOTTOM_RIGHT);
		
		addComponent(headerLayout);
		addComponent(matchingListsTableWithSelectAll);
		addComponent(menu);
	}

	public void applyGermplasmListResults(List<GermplasmList> germplasmLists) {
		totalMatchingListsLabel.setValue(new Label(messageSource.getMessage(Message.TOTAL_RESULTS) + ": " 
	       		 + "  <b>" + String.valueOf(germplasmLists.size()) + "</b>", Label.CONTENT_XHTML));
		matchingListsTable.removeAllItems();
		for (GermplasmList germplasmList : germplasmLists) {

			CheckBox itemCheckBox = new CheckBox();
			itemCheckBox.setData(germplasmList.getId());
			itemCheckBox.setImmediate(true);
			itemCheckBox.addListener(new ClickListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
					CheckBox itemCheckBox = (CheckBox) event.getButton();
					if (((Boolean) itemCheckBox.getValue()).equals(true)) {
						matchingListsTable.select(itemCheckBox.getData());
					} else {
						matchingListsTable.unselect(itemCheckBox.getData());
					}
				}

			});

			Item newItem = matchingListsTable.getContainerDataSource().addItem(germplasmList.getId());
			
			newItem.getItemProperty(CHECKBOX_COLUMN_ID).setValue(itemCheckBox);
			newItem.getItemProperty(NAME_ID).setValue(germplasmList.getName());
			newItem.getItemProperty(DESCRIPTION_ID).setValue(germplasmList.getDescription());
		}
		
		if(matchingListsTable.getItemIds().size() > 0){
			updateActionMenuOptions(true);
		}
	}
	
	private void updateNoOfEntries(long count){
		totalMatchingListsLabel.setValue(messageSource.getMessage(Message.TOTAL_RESULTS) + ": " 
       		 + "  <b>" + count + "</b>");
	}
	
	@SuppressWarnings("unused")
	private void updateNoOfEntries(){
		int count = 0;
		count = matchingListsTable.getItemIds().size();
		updateNoOfEntries(count);
	}
	
	private void updateNoOfSelectedEntries(int count){
		totalSelectedMatchingListsLabel.setValue("<i>" + messageSource.getMessage(Message.SELECTED) + ": " 
	        		 + "  <b>" + count + "</b></i>");
	}
	
	private void updateNoOfSelectedEntries(){
		int count = 0;
		
		Collection<?> selectedItems = (Collection<?>)matchingListsTable.getValue();
		count = selectedItems.size();
		
		updateNoOfSelectedEntries(count);
	}


	public Table getMatchingListsTable() {
		return matchingListsTable;
	}

	public ListSelectionLayout getListManagerDetailsLayout() {
		return this.displayDetailsLayout;
	}
	
	public void removeSearchResult(Object itemId){
		matchingListsTable.removeItem(itemId);
	}
	
	@SuppressWarnings("unchecked")
	public void addSelectedListToNewList(){
		source.showListBuilder();
		
		Set<Integer> listIds = (Set<Integer>)matchingListsTable.getValue(); 
		source.getListBuilderComponent().addListsFromSearchResults(listIds);
	}
}
