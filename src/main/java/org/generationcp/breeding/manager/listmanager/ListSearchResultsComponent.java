package org.generationcp.breeding.manager.listmanager;

import java.util.List;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.listmanager.listeners.ListSearchResultsItemClickListener;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListSelectionLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

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
	
	private Label matchingListsLabel;
	private Button actionButton;
	private Table matchingListsTable;
	private TableWithSelectAllLayout matchingListsTableWithSelectAll;

	private static final String CHECKBOX_COLUMN_ID = "Tag All Column";
	public static final String MATCHING_LISTS_TABLE_DATA = "Matching Lists Table";
	public static final String TOOLS_BUTTON_ID = "Actions";

	static final Action ACTION_SELECT_ALL = new Action("Select All");
	static final Action ACTION_ADD_TO_NEW_LIST = new Action("Add Selected List(s) to New List");
	static final Action[] LISTS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_ADD_TO_NEW_LIST };
	
	//Tools Button Context Menu
    private ContextMenu menu;
    private ContextMenuItem menuSelectAll;
    private ContextMenuItem menuAddToNewList;

	private final org.generationcp.breeding.manager.listmanager.sidebyside.ListSelectionLayout displayDetailsLayout;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
    protected GermplasmListManager germplasmListManager;
	
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
		
		matchingListsLabel = new Label();
		
		
		matchingListsLabel = new Label(messageSource.getMessage(Message.TOTAL_RESULTS) + ": " 
	       		 + "  <b>" + 0 + "</b>", Label.CONTENT_XHTML);
		matchingListsLabel.setWidth("150px");	
		matchingListsLabel.setStyleName("lm-search-results-label");
		
		actionButton = new Button(messageSource.getMessage(Message.ACTIONS));
		actionButton.setData(TOOLS_BUTTON_ID);
		actionButton.setIcon(AppConstants.Icons.ICON_TOOLS);
		actionButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
		actionButton.setWidth("110px");
        actionButton.addStyleName("lm-tools-button");
        
        //Action Button ContextMenu
        menu = new ContextMenu();
        menu.setWidth("300px");
        
        menuAddToNewList = menu.addItem(messageSource.getMessage(Message.ADD_SELECTED_LIST_TO_NEW_LIST));
        menuSelectAll = menu.addItem(messageSource.getMessage(Message.SELECT_ALL));
        
		matchingListsTableWithSelectAll = new TableWithSelectAllLayout(5,
				CHECKBOX_COLUMN_ID);
		matchingListsTableWithSelectAll.setHeight("100%");
		matchingListsTable = matchingListsTableWithSelectAll.getTable();
		matchingListsTable.setData(MATCHING_LISTS_TABLE_DATA);
		matchingListsTable.addContainerProperty(CHECKBOX_COLUMN_ID,
				CheckBox.class, null);
		matchingListsTable.addContainerProperty("NAME", String.class, null);
		matchingListsTable.addContainerProperty("DESCRIPTION", String.class,
				null);
		matchingListsTable.setHeight("260px");
		matchingListsTable.setWidth("100%");
		matchingListsTable.setMultiSelect(true);
		matchingListsTable.setSelectable(true);
		matchingListsTable.setImmediate(true);
		matchingListsTable.setDragMode(TableDragMode.ROW);
		matchingListsTable.addListener(new ListSearchResultsItemClickListener(displayDetailsLayout));
		messageSource.setColumnHeader(matchingListsTable, CHECKBOX_COLUMN_ID,
				Message.CHECK_ICON);
		
		addSearchListResultsItemDescription();
		
		addActionHandler();
	}
	
	private void addSearchListResultsItemDescription(){
		matchingListsTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            private static final long serialVersionUID = -2669417630841097077L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
            	GermplasmList germplasmList;
				
            	try {
					if(!itemId.toString().equals("CENTRAL") &&  !itemId.toString().equals("LOCAL")){
						germplasmList = germplasmListManager.getGermplasmListById(Integer.valueOf(itemId.toString()));
						
						if(germplasmList != null){
							if(!germplasmList.getType().equals("FOLDER")){
								ViewListHeaderWindow viewListHeaderWindow = new ViewListHeaderWindow(germplasmList);
								return viewListHeaderWindow.getListHeaderComponent().toString();
							}
						}
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MiddlewareQueryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
                return "";
            }
        });
	}
	
	private void addActionHandler(){
		matchingListsTable.addActionHandler(new Action.Handler() {
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
				return LISTS_TABLE_CONTEXT_MENU;
			}
		});
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
	}

	@Override
	public void layoutComponents() {
		
		setWidth("100%");
		setSpacing(true);
		
		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setWidth("100%");
		headerLayout.addComponent(matchingListsLabel);
		headerLayout.addComponent(actionButton);
		
		headerLayout.setComponentAlignment(matchingListsLabel, Alignment.BOTTOM_LEFT);
		headerLayout.setComponentAlignment(actionButton, Alignment.BOTTOM_RIGHT);
		
		addComponent(headerLayout);
		addComponent(matchingListsTableWithSelectAll);
		addComponent(menu);
	}

	public void applyGermplasmListResults(List<GermplasmList> germplasmLists) {
		matchingListsLabel.setValue(new Label(messageSource.getMessage(Message.TOTAL_RESULTS) + ": " 
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

			matchingListsTable.addItem(new Object[] { itemCheckBox,
					germplasmList.getName(), germplasmList.getDescription() },
					germplasmList.getId());
		}
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
	
	public void addSelectedListToNewList(){
		source.showListBuilder();
		
		Set<Integer> listIds = (Set)matchingListsTable.getValue(); 
		source.getListBuilderComponent().addListsFromSearchResults(listIds);
	}
}
