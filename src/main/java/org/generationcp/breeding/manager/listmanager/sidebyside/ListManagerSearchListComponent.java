package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.DropHandlerComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeMenu;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Runo;

@Configurable
public class ListManagerSearchListComponent extends AbsoluteLayout implements
			InitializingBean {

	private static final String GUIDE = 
        "You may search for germplasms and germplasm lists using GID's, germplasm names (partial/full), or list names (partial/full)" +
        " <br/><br/><b>Matching lists would contain</b> <br/>" +
        "  - Lists with names containing the search query <br/>" +
        "  - Lists containing germplasms given a GID <br/>" +
        "  - Lists containing germplasms with names <br/>" +
        " containing the search query" +
        " <br/><br/><b>Matching germplasms would contain</b> <br/>" +
        "  - Germplasms with matching GID's <br/>" +
        "  - Germplasms with name containing search query <br/>" +
        "  - Parents of the result germplasms (if selected)" +
        " <br/><br/>The <b>Exact matches only</b> checkbox allows you search using partial names (when unchecked)" +
        " or to only return results which match the query exactly (when checked).";
	private static final String CHECKBOX_COLUMN_ID = "Tag All Column";
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;	
	
	// search bar components
	private AbsoluteLayout searchBar;
	private Label searchLabel;
	private TextField searchField;
	private Button searchButton;
	private CheckBox likeOrEqualCheckBox;
	private CheckBox includeParentsCheckBox;
	
	// search results component
	private Label matchingListsLabel;
	private Label matchingListsDescription;
	private Table matchingListsTable;
	private Label matchingGermplasmsLabel;
	private Label matchingGermplasmsDescription;
	private Table matchingGermplasmsTable;
	
	private CheckBox matchingListsTagAllCheckBox;
	private CheckBox matchingGermplasmsTagAllCheckBox;
	
	private boolean matchingListsTagAllWasJustClicked = false;
	private boolean matchingGermplasmsTagAllWasJustClicked = false;
	
	private Label projectLists;
	private Panel browseListPanel;
	private TabSheet tabSheetList;
	private ListManagerTreeMenu list1;
	private ListManagerTreeMenu list2;
	private DropHandlerComponent dropHandler;

	@Override
	public void afterPropertiesSet() throws Exception {
		initializeSearchBar();
		initializeSearchResultsTables();
	}

	private void initializeSearchResultsTables() {
		matchingListsLabel = new Label();
		matchingListsLabel.setValue(messageSource.getMessage(Message.MATCHING_LISTS)+": 0");
		matchingListsLabel.addStyleName(Bootstrap.Typography.H3.styleName());
		
		matchingListsDescription = new Label();
		matchingListsDescription.setValue(messageSource.getMessage(Message.SELECT_A_LIST_TO_VIEW_THE_DETAILS));
		
		matchingListsTable = new Table();
		matchingListsTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		matchingListsTable.addContainerProperty("NAME", String.class, null);
		matchingListsTable.addContainerProperty("DESCRIPTION", String.class, null);
		matchingListsTable.setWidth("350px");
		matchingListsTable.setHeight("120px");
		
		matchingListsTable.setMultiSelect(true);
		matchingListsTable.setSelectable(true);
		matchingListsTable.setImmediate(true);
//		matchingListsTable.addListener(new SearchResultsItemClickListener(MATCHING_LISTS_TABLE_DATA, displayDetailsLayout));
//		matchingListsTable.addListener(new Table.ValueChangeListener() {
//			 public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
//				 syncItemCheckBoxes(matchingListsTable);
//			 }
//		 });		
		messageSource.setColumnHeader(matchingListsTable, CHECKBOX_COLUMN_ID, Message.TAG);
		
		matchingGermplasmsLabel = new Label();
		matchingGermplasmsLabel.setValue(messageSource.getMessage(Message.MATCHING_GERMPLASM)+": 0");
		matchingGermplasmsLabel.addStyleName(Bootstrap.Typography.H3.styleName());
		
		matchingGermplasmsDescription = new Label();
		matchingGermplasmsDescription.setValue(messageSource.getMessage(Message.SELECT_A_GERMPLASM_TO_VIEW_THE_DETAILS));
		
		matchingGermplasmsTable = new Table();
		matchingGermplasmsTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		matchingGermplasmsTable.addContainerProperty("GID", Button.class, null);
		matchingGermplasmsTable.addContainerProperty("NAMES", String.class,null);
		matchingGermplasmsTable.addContainerProperty("PARENTAGE", String.class,null);
		matchingGermplasmsTable.setWidth("350px");
		matchingGermplasmsTable.setHeight("120px");
		matchingGermplasmsTable.setMultiSelect(true);
		matchingGermplasmsTable.setSelectable(true);
		matchingGermplasmsTable.setImmediate(true);
//		matchingGermplasmsTable.addListener(new SearchResultsItemClickListener(MATCHING_GEMRPLASMS_TABLE_DATA, displayDetailsLayout));
//		matchingGermplasmsTable.addListener(new Table.ValueChangeListener() {
//			 public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
//				 syncItemCheckBoxes(matchingGermplasmsTable);
//			 }
//		 });
		messageSource.setColumnHeader(matchingGermplasmsTable, CHECKBOX_COLUMN_ID, Message.TAG);
		
//		matchingGermplasmsTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
//			private static final long serialVersionUID = 1L;
//
//			public String generateDescription(Component source, Object itemId,
//					Object propertyId) {
//				if(propertyId=="NAMES"){
//					Item item = matchingGermplasmsTable.getItem(itemId);
//					Integer gid = Integer.valueOf(((Button) item.getItemProperty("GID").getValue()).getCaption());
//					return getGermplasmNames(gid);
//				} else {
//					return null;
//				}
//			}
//        });
		
//		matchingGermplasmsTable.addActionHandler(new Action.Handler() {
//       	 private static final long serialVersionUID = -897257270314381555L;
//
//			public Action[] getActions(Object target, Object sender) {
//				return GERMPLASMS_TABLE_CONTEXT_MENU;
//            }
//
//			@SuppressWarnings("unchecked")
//			@Override
//			public void handleAction(Action action, Object sender, Object target) {
//             	if (ACTION_COPY_TO_NEW_LIST == action) {
//             		listManagerMain.showBuildNewListComponent();
//             		List<Integer> gids = new ArrayList<Integer>();
//             		gids.addAll((Collection<? extends Integer>) matchingGermplasmsTable.getValue());
//             		for(Integer gid : gids){
//             			listManagerMain.getBuildListComponent().addGermplasmToGermplasmTable(gid, null);
//             		}
//             		
//             		listManagerMain.getBuildListComponent().updateDropListEntries();
//             	}
//			}
//		});

		matchingListsTagAllCheckBox = new CheckBox();
		matchingListsTagAllCheckBox.setCaption(messageSource.getMessage(Message.SELECT_ALL));
		matchingListsTagAllCheckBox.setImmediate(true);
		matchingListsTagAllCheckBox.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				if(((Boolean) matchingListsTagAllCheckBox.getValue()).equals(true)){
					matchingListsTagAllWasJustClicked = true;
					matchingListsTable.setValue(matchingListsTable.getItemIds());
				} else {
					matchingListsTagAllWasJustClicked = false;
					matchingListsTable.setValue(null);
				}
			}
			 
		 });

		matchingGermplasmsTagAllCheckBox = new CheckBox();
		matchingGermplasmsTagAllCheckBox.setCaption(messageSource.getMessage(Message.SELECT_ALL));
		matchingGermplasmsTagAllCheckBox.setImmediate(true);
		matchingGermplasmsTagAllCheckBox.setStyleName(Bootstrap.Buttons.INFO.styleName());
		matchingGermplasmsTagAllCheckBox.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				if(((Boolean) matchingGermplasmsTagAllCheckBox.getValue()).equals(true)){
					matchingGermplasmsTagAllWasJustClicked = true;
					matchingGermplasmsTable.setValue(matchingGermplasmsTable.getItemIds());
				} else {
					matchingGermplasmsTagAllWasJustClicked = false;
					matchingGermplasmsTable.setValue(null);
				}
			}
			 
		 });
		
//		dropHandler = new DropHandlerComponent(listManagerMain, 350);
		
		
		VerticalLayout listsLayout = new VerticalLayout();
		listsLayout.addComponent(matchingListsLabel);
		listsLayout.addComponent(matchingListsDescription);
		listsLayout.addComponent(matchingListsTable);
		listsLayout.addComponent(matchingListsTagAllCheckBox);
		
		VerticalLayout germplasmLayout = new VerticalLayout();
		germplasmLayout.addComponent(matchingGermplasmsLabel);
		germplasmLayout.addComponent(matchingGermplasmsDescription);
		germplasmLayout.addComponent(matchingGermplasmsTable);
		germplasmLayout.addComponent(matchingGermplasmsTagAllCheckBox);
		
		
		VerticalLayout spaceLayout = new VerticalLayout();
		spaceLayout.setWidth("20px");
		
		HorizontalLayout tablesLayout = new HorizontalLayout();
		tablesLayout.addComponent(listsLayout);
		tablesLayout.addComponent(spaceLayout);
		tablesLayout.addComponent(germplasmLayout);
		
		addComponent(tablesLayout, "top:55px; left:20px");

//		addComponent(dropHandler, "top:385px; left:0px;");
		
		tabSheetList = new TabSheet();
		tabSheetList.setWidth("95%");
		tabSheetList.setHeight("300px");
		
		VerticalLayout layout = new VerticalLayout();
		list1 = new ListManagerTreeMenu(1426,"IIRON-1986",1,1,false,null);
		layout.addComponent(list1);
		Tab tab1 = tabSheetList.addTab(layout, "IIRON-1986");
		tab1.setClosable(true);
    	
		VerticalLayout layout2 = new VerticalLayout();
		list2 = new ListManagerTreeMenu(1427,"IIRON-1987",1,1,false,null);
		layout2.addComponent(list2);
		Tab tab2 = tabSheetList.addTab(layout2, "IIRON-1987");
		tab2.setClosable(true);
		
		addComponent(tabSheetList,"top:250px; left:20px");
	}

	private void initializeSearchBar() {
		Panel p = new Panel();
        p.addStyleName("search-panel");
        p.addStyleName(Runo.PANEL_LIGHT);
        p.addStyleName("list-manager-search-bar");
        p.setScrollable(false);
        
        searchBar = new AbsoluteLayout();
        searchBar.setWidth("80%");
        searchBar.setHeight("40px");
        //searchBar.addStyleName("list-manager-search-bar");
        
        searchLabel = new Label();
        searchLabel.setValue(messageSource.getMessage(Message.SEARCH_FOR)+": ");
        searchLabel.setWidth("200px");
        
        searchField = new TextField();
        searchField.setImmediate(true);
        
        /**
         * TODO: replace with image button
         */
        
        
        searchButton = new Button();
        searchButton.setWidth("30px");
        searchButton.setHeight("30px");
        searchButton.setStyleName(BaseTheme.BUTTON_LINK);
        searchButton.addStyleName("search-button");
//        searchButton.setData(SEARCH_BUTTON);
//        searchButton.addListener(new GermplasmListManagerButtonClickListener(this));
        //searchButton.setClickShortcut(KeyCode.ENTER);

        p.addAction(new ShortcutListener("Next field", KeyCode.ENTER, null) {
            private static final long serialVersionUID = 288627665348761948L;

            @Override
            public void handleAction(Object sender, Object target) {
                // The panel is the sender, loop trough content
//                searchButtonClickAction();
            }
        });
        
                  

        Label descLbl = new Label(GUIDE, Label.CONTENT_XHTML);
        descLbl.setWidth("300px");
        PopupView popup = new PopupView(" ? ",descLbl);
        popup.setStyleName("gcp-popup-view");
        
        likeOrEqualCheckBox = new CheckBox();
        likeOrEqualCheckBox.setCaption(messageSource.getMessage(Message.EXACT_MATCHES_ONLY));
        
        includeParentsCheckBox = new CheckBox();
        includeParentsCheckBox.setCaption(messageSource.getMessage(Message.INCLUDE_PARENTS));
        
        searchBar.addComponent(searchLabel, "top:13px; left:20px;");
        searchBar.addComponent(searchField, "top:10px; left:100px;");
        searchBar.addComponent(searchButton, "top:8px; left:280px;");
        searchBar.addComponent(popup, "top:12px; left:330px;");
        searchBar.addComponent(likeOrEqualCheckBox, "top:13px; left: 350px;");
        searchBar.addComponent(includeParentsCheckBox, "top:13px; left: 500px;");
        
        
        p.setLayout(searchBar);
        addComponent(p, "top:10px; left:20px;");
	}
}

