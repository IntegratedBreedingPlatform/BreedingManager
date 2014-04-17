package org.generationcp.breeding.manager.crossingmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SelectParentsListDataComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(SelectParentsListDataComponent.class);
	private static final long serialVersionUID = 7907737258051595316L;
	private static final String CHECKBOX_COLUMN_ID="Checkbox Column ID";
	
	public static final String LIST_DATA_TABLE_ID = "SelectParentsListDataComponent List Data Table ID";
	
	private static final Action ACTION_ADD_TO_FEMALE_LIST = new Action("Add to Female List");
	private static final Action ACTION_ADD_TO_MALE_LIST = new Action("Add to Male List");
	private static final Action[] LIST_DATA_TABLE_ACTIONS = new Action[] {ACTION_ADD_TO_FEMALE_LIST, ACTION_ADD_TO_MALE_LIST};
	
	private Integer germplasmListId;
	private GermplasmList germplasmList;
	private Long count;
	
	private Label listEntriesLabel;
	private Table listDataTable;
	private CheckBox selectAllCheckBox;
	private Button viewListHeaderButton;
	private String listName;
	
	private ViewListHeaderWindow viewListHeaderWindow;
	
	private TableWithSelectAllLayout tableWithSelectAllLayout;
	
	private MakeCrossesParentsComponent makeCrossesParentsComponent;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;

	@Autowired
    private GermplasmListManager germplasmListManager;
	
	public SelectParentsListDataComponent(Integer germplasmListId, String listName, MakeCrossesParentsComponent makeCrossesParentsComponent){
		super();
		this.germplasmListId = germplasmListId;
		this.listName = listName;
		this.makeCrossesParentsComponent = makeCrossesParentsComponent;
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
		
	}

	@Override
	public void instantiateComponents() {
		retrieveListDetails();
		
		listEntriesLabel = new Label(messageSource.getMessage(Message.LIST_ENTRIES_LABEL).toUpperCase());
		listEntriesLabel.setStyleName(Bootstrap.Typography.H5.styleName());
		listEntriesLabel.addStyleName(AppConstants.CssStyles.BOLD);
		
		viewListHeaderWindow = new ViewListHeaderWindow(germplasmList);
		
		viewListHeaderButton = new Button(messageSource.getMessage(Message.VIEW_HEADER));
		viewListHeaderButton.addStyleName(Reindeer.BUTTON_LINK);
		viewListHeaderButton.setDescription(viewListHeaderWindow.getListHeaderComponent().toString());
		
		tableWithSelectAllLayout = new TableWithSelectAllLayout(count.intValue(),9,CHECKBOX_COLUMN_ID);
		tableWithSelectAllLayout.setWidth("100%");
		
		listDataTable = tableWithSelectAllLayout.getTable();
		listDataTable.setWidth("100%");
		listDataTable.setData(LIST_DATA_TABLE_ID);
		listDataTable.setSelectable(true);
		listDataTable.setMultiSelect(true);
		listDataTable.setColumnCollapsingAllowed(true);
		listDataTable.setColumnReorderingAllowed(true);
		listDataTable.setImmediate(true);
		listDataTable.setDragMode(TableDragMode.MULTIROW);
		
		listDataTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.PARENTAGE.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
		
		listDataTable.setColumnHeader(CHECKBOX_COLUMN_ID, messageSource.getMessage(Message.CHECK_ICON));
		listDataTable.setColumnHeader(ListDataTablePropertyID.ENTRY_ID.getName(), messageSource.getMessage(Message.HASHTAG));
		listDataTable.setColumnHeader(ListDataTablePropertyID.DESIGNATION.getName(), messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.PARENTAGE.getName(), messageSource.getMessage(Message.LISTDATA_GROUPNAME_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.ENTRY_CODE.getName(), messageSource.getMessage(Message.LISTDATA_ENTRY_CODE_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.GID.getName(), messageSource.getMessage(Message.LISTDATA_GID_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.SEED_SOURCE.getName(), messageSource.getMessage(Message.LISTDATA_SEEDSOURCE_HEADER));
		
		listDataTable.setColumnWidth(CHECKBOX_COLUMN_ID, 25);
		listDataTable.setColumnWidth(ListDataTablePropertyID.ENTRY_ID.getName(), 25);
		listDataTable.setColumnWidth(ListDataTablePropertyID.DESIGNATION.getName(), 130);
		listDataTable.setColumnWidth(ListDataTablePropertyID.PARENTAGE.getName(), 130);
		listDataTable.setColumnWidth(ListDataTablePropertyID.ENTRY_CODE.getName(), 100);
		listDataTable.setColumnWidth(ListDataTablePropertyID.GID.getName(), 60);
		listDataTable.setColumnWidth(ListDataTablePropertyID.SEED_SOURCE.getName(), 110);
		
		listDataTable.setVisibleColumns(new String[] { 
        		CHECKBOX_COLUMN_ID
        		,ListDataTablePropertyID.ENTRY_ID.getName()
        		,ListDataTablePropertyID.DESIGNATION.getName()
        		,ListDataTablePropertyID.PARENTAGE.getName()
        		,ListDataTablePropertyID.ENTRY_CODE.getName()
        		,ListDataTablePropertyID.GID.getName()
        		,ListDataTablePropertyID.SEED_SOURCE.getName()});
		
		selectAllCheckBox = new CheckBox(messageSource.getMessage(Message.SELECT_ALL));
		selectAllCheckBox.setImmediate(true);
		
		viewListHeaderButton = new Button("View List Header");
		viewListHeaderButton.setStyleName(BaseTheme.BUTTON_LINK);
	}

	private void retrieveListDetails() {
		try {
			germplasmList = germplasmListManager.getGermplasmListById(this.germplasmListId);
			count = germplasmListManager.countGermplasmListDataByListId(this.germplasmListId);
		} catch (MiddlewareQueryException e) {
			LOG.error("Error getting list details" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void initializeValues() {
		try{
			List<GermplasmListData> listEntries = germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0, Integer.MAX_VALUE);
			for(GermplasmListData entry : listEntries){
				String gid = String.format("%s", entry.getGid().toString());
                Button gidButton = new Button(gid, new GidLinkButtonClickListener(gid,true));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                gidButton.setDescription("Click to view Germplasm information");
                
                Button desigButton = new Button(entry.getDesignation(), new GidLinkButtonClickListener(gid,true));
                desigButton.setStyleName(BaseTheme.BUTTON_LINK);
                desigButton.setDescription("Click to view Germplasm information");
                
                CheckBox itemCheckBox = new CheckBox();
                itemCheckBox.setData(entry.getId());
                itemCheckBox.setImmediate(true);
    	   		itemCheckBox.addListener(new ClickListener() {
    	 			private static final long serialVersionUID = 1L;
    	 			@Override
    	 			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
    	 				CheckBox itemCheckBox = (CheckBox) event.getButton();
    	 				if(((Boolean) itemCheckBox.getValue()).equals(true)){
    	 					listDataTable.select(itemCheckBox.getData());
    	 				} else {
    	 					listDataTable.unselect(itemCheckBox.getData());
    	 				}
    	 			}
    	 		});
    	   		
    	   		listDataTable.addItem(new Object[] {
                        itemCheckBox, entry.getEntryId(), desigButton, entry.getGroupName(), entry.getEntryCode(), gidButton, entry.getSeedSource()}
    	   			, entry.getId());
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting list entries for list: " + germplasmListId);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), "Error in getting list entries."
					, Notification.POSITION_CENTERED);
		}
	}

	@Override
	public void addListeners() {
		viewListHeaderButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 329434322390122057L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				openViewListHeaderWindow();
			}
		});
		
		listDataTable.addActionHandler(new Action.Handler() {
			private static final long serialVersionUID = -2173636726748988046L;

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if(action.equals(ACTION_ADD_TO_FEMALE_LIST)){
					makeCrossesParentsComponent.dropToFemaleOrMaleTable(listDataTable, makeCrossesParentsComponent.getFemaleTable());
					makeCrossesParentsComponent.assignEntryNumber(makeCrossesParentsComponent.getFemaleTable());
				} else if(action.equals(ACTION_ADD_TO_MALE_LIST)){
					makeCrossesParentsComponent.dropToFemaleOrMaleTable(listDataTable, makeCrossesParentsComponent.getMaleTable());
					makeCrossesParentsComponent.assignEntryNumber(makeCrossesParentsComponent.getMaleTable());
				}
			}
			
			@Override
			public Action[] getActions(Object target, Object sender) {
				return LIST_DATA_TABLE_ACTIONS;
			}
		});
	}

	@Override
	public void layoutComponents() {
		setMargin(true);
		setSpacing(true);
		
		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setWidth("100%");
		headerLayout.addComponent(listEntriesLabel);
		headerLayout.addComponent(viewListHeaderButton);
		headerLayout.setComponentAlignment(listEntriesLabel, Alignment.MIDDLE_LEFT);
		headerLayout.setComponentAlignment(viewListHeaderButton, Alignment.MIDDLE_RIGHT);
		
		addComponent(headerLayout);
		addComponent(tableWithSelectAllLayout);
	}
	
	private void openViewListHeaderWindow(){
		this.getWindow().addWindow(viewListHeaderWindow);
	}
	
	public Table getListDataTable(){
		return this.listDataTable;
	}

	public String getListName() {
		return this.listName;
	}
}
