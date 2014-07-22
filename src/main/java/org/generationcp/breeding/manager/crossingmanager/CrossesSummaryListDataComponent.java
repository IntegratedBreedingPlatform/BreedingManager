package org.generationcp.breeding.manager.crossingmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerExporter;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerExporterException;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class CrossesSummaryListDataComponent extends VerticalLayout implements 
		BreedingManagerLayout, InitializingBean {

	private static final String CLICK_TO_VIEW_CROSS_INFORMATION = "Click to view Cross information";
	private static final String CLICK_TO_VIEW_FEMALE_INFORMATION = "Click to view Female Parent information";
	private static final String CLICK_TO_VIEW_MALE_INFORMATION = "Click to view Male Parent information";

	private static final long serialVersionUID = -6058352152291932651L;
	
	private static final Logger LOG = LoggerFactory.getLogger(CrossesSummaryListDataComponent.class);
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;

	private Label listEntriesLabel;
	
	private Table listDataTable;
	private Button toolsButton;
	private Button viewHeaderButton;
	
	private Long count;
	
	//Menu for tools button
	private ContextMenu menu; 
	private ContextMenuItem menuExportList;
	
	private ViewListHeaderWindow viewListHeaderWindow;
	
	private enum CrossListDataColumn {
		FGID, FEMALE_PARENT, MGID, MALE_PARENT, METHOD
	}
	
	private GermplasmList list;

	private List<GermplasmListData> listEntries;
	private Map<Integer, Germplasm> germplasmMap;
	private Map<Integer, CrossParents> parentsInfo; //list data id, CrossParents info

	//Used maps to make use of existing Middleware methods
	private Map<Integer, String> parentGermplasmNames; // gid of parent, preferred name
	private Map<Integer, Object> methodMap; // Gid, Method of germplasm
	
	public CrossesSummaryListDataComponent(GermplasmList list){
		this.list = list;
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
		listEntriesLabel = new Label(messageSource.getMessage(Message.CROSS_LIST_ENTRIES).toUpperCase());
		listEntriesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		listEntriesLabel.setWidth("160px");
		
		viewListHeaderWindow = new ViewListHeaderWindow(list);
		
		viewHeaderButton = new Button(messageSource.getMessage(Message.VIEW_HEADER));
		viewHeaderButton.addStyleName(Reindeer.BUTTON_LINK);
		viewHeaderButton.setDescription(viewListHeaderWindow.getListHeaderComponent().toString());
		
		initializeListEntriesTable();
		
		toolsButton = new ActionButton();
		
		menu = new ContextMenu();
		menu.setWidth("200px");
		menu.setVisible(true);
		
		// Generate menu items
		menuExportList = menu.addItem(messageSource.getMessage(Message.EXPORT_CROSS_LIST));
		menuExportList.setVisible(true);
		
	}

	@Override
	public void initializeValues() {
		retrieveGermplasmsInformation();
		populateTable();
	}

	
	private void populateTable() {
		parentsInfo = new HashMap<Integer, CrossParents>();
		
		for(GermplasmListData entry : listEntries){
			Integer gid = entry.getGid();
			String gidString = String.format("%s", gid.toString());
            
			Button gidButton = generateLaunchGermplasmDetailsButton(gidString, gidString, CLICK_TO_VIEW_CROSS_INFORMATION);
            Button desigButton = generateLaunchGermplasmDetailsButton(entry.getDesignation(), gidString, CLICK_TO_VIEW_CROSS_INFORMATION);
            
            Germplasm germplasm = germplasmMap.get(gid);
            Integer femaleGid = germplasm.getGpid1();
            String femaleGidString = femaleGid.toString();
			Button femaleGidButton = generateLaunchGermplasmDetailsButton(femaleGidString, femaleGidString, CLICK_TO_VIEW_FEMALE_INFORMATION);
            String femaleDesig = parentGermplasmNames.get(femaleGid);
			Button femaleDesigButton = generateLaunchGermplasmDetailsButton(femaleDesig, femaleGidString, CLICK_TO_VIEW_FEMALE_INFORMATION);
            
            Integer maleGid = germplasm.getGpid2();
            String maleGidString = maleGid.toString();
            Button maleGidButton = generateLaunchGermplasmDetailsButton(maleGidString, maleGidString, CLICK_TO_VIEW_MALE_INFORMATION);
            String maleDesig = parentGermplasmNames.get(maleGid);
			Button maleDesigButton = generateLaunchGermplasmDetailsButton(maleDesig, maleGidString, CLICK_TO_VIEW_MALE_INFORMATION);
            
            Method method = (Method) methodMap.get(gid);
            
            
	   		listDataTable.addItem(new Object[] {
	   				entry.getEntryId(), desigButton, entry.getGroupName(), entry.getEntryCode(), gidButton, entry.getSeedSource(),
	   				femaleDesigButton, femaleGidButton, maleDesigButton, maleGidButton, method.getMname()
   				}, entry.getId());
	   		
	   		
	   		addToParentsInfoMap(entry.getId(), femaleGid, femaleDesig, maleGid, maleDesig);
		}
	
	}

	
	private void addToParentsInfoMap(Integer id, Integer femaleGid,
			String femaleDesig, Integer maleGid, String maleDesig) {
		
		GermplasmListEntry femaleEntry = new GermplasmListEntry(null, femaleGid, null, femaleDesig); 
		GermplasmListEntry maleEntry = new GermplasmListEntry(null, maleGid, null, maleDesig); 
		CrossParents parents = new CrossParents(femaleEntry, maleEntry);
		parentsInfo.put(id, parents);
	}

	
	private Button generateLaunchGermplasmDetailsButton(String caption, String gid, String description) {
		Button gidButton = new Button(caption, new GidLinkClickListener(gid,true));
		gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		gidButton.setDescription(description);
		return gidButton;
	}
	

	@Override
	public void addListeners() {
		toolsButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -7600642919550425308L;

			@Override
			public void buttonClick(ClickEvent event) {
				menu.show(event.getClientX(),event.getClientY());
			}
		});
		
		menu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void contextItemClick(
					org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
				ContextMenuItem clickedItem = event.getClickedItem();
                if(menuExportList.equals(clickedItem)){
                	exportCrossesMadeAction();
                }      
			}
		});
		
		viewHeaderButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 329434322390122057L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				openViewListHeaderWindow();
			}
		});
		
	}

	@Override
	public void layoutComponents() {
		setSpacing(true);
			
		HorizontalLayout tableHeaderLayout = new HorizontalLayout();
		tableHeaderLayout.setHeight("30px");
		tableHeaderLayout.setWidth("100%");
		
		HorizontalLayout leftHeaderLayout = new HorizontalLayout();
		leftHeaderLayout.setSpacing(true);
		leftHeaderLayout.addComponent(listEntriesLabel);
		leftHeaderLayout.addComponent(viewHeaderButton);
		leftHeaderLayout.setComponentAlignment(viewHeaderButton, Alignment.MIDDLE_RIGHT);
		
		
		tableHeaderLayout.addComponent(leftHeaderLayout);
		tableHeaderLayout.addComponent(toolsButton);
		tableHeaderLayout.setComponentAlignment(leftHeaderLayout, Alignment.MIDDLE_LEFT);
		tableHeaderLayout.setComponentAlignment(toolsButton, Alignment.MIDDLE_RIGHT);
		
		VerticalLayout tableLayout = new VerticalLayout();
		listDataTable.setWidth("100%");
		tableLayout.addComponent(listDataTable);
		tableLayout.setComponentAlignment(listDataTable, Alignment.TOP_LEFT);
		
		addComponent(tableHeaderLayout);
		addComponent(tableLayout);
		addComponent(menu);
	}
	
	
	private void retrieveGermplasmsInformation(){
		try{
			List<Integer> germplasmIds = new ArrayList<Integer>();
			germplasmMap = new HashMap<Integer, Germplasm>();

			// retrieve germplasm of list data to get its parent germplasms
			this.listEntries = germplasmListManager.getGermplasmListDataByListId(list.getId(), 0, Integer.MAX_VALUE);
			for(GermplasmListData entry : listEntries){
				germplasmIds.add(entry.getGid());
			}
			List<Germplasm> existingGermplasms = germplasmDataManager.getGermplasms(germplasmIds);
			
			
			//retrieve methods of germplasms
			methodMap = germplasmDataManager.getMethodsByGids(germplasmIds);
			
			
			//retrieve preferred names of parent germplasms
			List<Integer> parentIds = new ArrayList<Integer>();
			for (Germplasm germplasm : existingGermplasms){
				germplasmMap.put(germplasm.getGid(), germplasm);
				parentIds.add(germplasm.getGpid1());
				parentIds.add(germplasm.getGpid2());
			}
			parentGermplasmNames = germplasmDataManager.getPreferredNamesByGids(parentIds);
			
		} catch(MiddlewareQueryException ex){
			LOG.error(ex.getMessage() + list.getId());
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), "Error in getting list and/or germplasm information."
					, Notification.POSITION_CENTERED);
		}
	}
	
	private void initializeListEntriesTable(){
		count = Long.valueOf(0);
		try {
			count = germplasmListManager.countGermplasmListDataByListId(this.list.getId());
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}
		
		listDataTable = new BreedingManagerTable(count.intValue(), 8);
		listDataTable.setColumnCollapsingAllowed(true);
		listDataTable.setColumnReorderingAllowed(true);
		listDataTable.setImmediate(true);
		
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.PARENTAGE.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
		listDataTable.addContainerProperty(CrossListDataColumn.FEMALE_PARENT, Button.class, null);
		listDataTable.addContainerProperty(CrossListDataColumn.FGID, Button.class, null);
		listDataTable.addContainerProperty(CrossListDataColumn.MALE_PARENT, Button.class, null);
		listDataTable.addContainerProperty(CrossListDataColumn.MGID, Button.class, null);
		listDataTable.addContainerProperty(CrossListDataColumn.METHOD, String.class, null);
		
		listDataTable.setColumnHeader(ListDataTablePropertyID.ENTRY_ID.getName(), messageSource.getMessage(Message.HASHTAG));
		listDataTable.setColumnHeader(ListDataTablePropertyID.DESIGNATION.getName(), messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.PARENTAGE.getName(), messageSource.getMessage(Message.LISTDATA_GROUPNAME_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.ENTRY_CODE.getName(), messageSource.getMessage(Message.LISTDATA_ENTRY_CODE_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.GID.getName(), messageSource.getMessage(Message.LISTDATA_GID_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.SEED_SOURCE.getName(), messageSource.getMessage(Message.LISTDATA_SEEDSOURCE_HEADER));
		listDataTable.setColumnHeader(CrossListDataColumn.FEMALE_PARENT, messageSource.getMessage(Message.LABEL_FEMALE_PARENT));
		listDataTable.setColumnHeader(CrossListDataColumn.FGID, messageSource.getMessage(Message.FGID));
		listDataTable.setColumnHeader(CrossListDataColumn.MALE_PARENT, messageSource.getMessage(Message.LABEL_MALE_PARENT));
		listDataTable.setColumnHeader(CrossListDataColumn.MGID, messageSource.getMessage(Message.MGID));
		listDataTable.setColumnHeader(CrossListDataColumn.METHOD, messageSource.getMessage(Message.METHOD_LABEL));
		
		listDataTable.setVisibleColumns(new Object[] { 
        		ListDataTablePropertyID.ENTRY_ID.getName()
        		,ListDataTablePropertyID.DESIGNATION.getName()
        		,ListDataTablePropertyID.PARENTAGE.getName()
        		,ListDataTablePropertyID.ENTRY_CODE.getName()
        		,ListDataTablePropertyID.GID.getName()
        		,ListDataTablePropertyID.SEED_SOURCE.getName()
        		,CrossListDataColumn.FEMALE_PARENT
        		,CrossListDataColumn.FGID
        		,CrossListDataColumn.MALE_PARENT
        		,CrossListDataColumn.MGID
        		,CrossListDataColumn.METHOD
    		}
		);
	}
	
	private void exportCrossesMadeAction(){
		CrossingManagerExporter exporter = new CrossingManagerExporter(list, listEntries, 
				parentsInfo);
		 String tempFileName = System.getProperty(AppConstants.USER_HOME) + "/temp.xls";
		 
        try {
            exporter.exportCrossingManagerExcel(tempFileName);
            FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), this.getApplication());
            fileDownloadResource.setFilename(list.getName().replace(" ", "_") + ".xls");

            this.getWindow().open(fileDownloadResource);
    
        } catch (CrossingManagerExporterException e) {
            MessageNotifier.showError(getWindow(), "Error with exporting crossing file.", e.getMessage(), Notification.POSITION_CENTERED);
        }
	}
	
	public void openViewListHeaderWindow(){
		this.getWindow().addWindow(viewListHeaderWindow);
	}
	
	public void focus(){
		listDataTable.focus();
	}

}
