package org.generationcp.breeding.manager.listmanager;

import java.util.List;

import com.vaadin.ui.*;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.listeners.ListSearchResultsItemClickListener;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListSelectionLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.Action;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.TableDragMode;

@Configurable
public class ListSearchResultsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 5314653969843976836L;

	private Label matchingListsLabel;
	private Table matchingListsTable;
	private TableWithSelectAllLayout matchingListsTableWithSelectAll;

	private static final String CHECKBOX_COLUMN_ID = "Tag All Column";

	public static final String MATCHING_LISTS_TABLE_DATA = "Matching Lists Table";

	static final Action ACTION_COPY_TO_NEW_LIST = new Action("Copy to new list");
	static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] { ACTION_COPY_TO_NEW_LIST };

	private final org.generationcp.breeding.manager.listmanager.sidebyside.ListSelectionLayout displayDetailsLayout;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public ListSearchResultsComponent(final ListSelectionLayout displayDetailsLayout) {
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
		matchingListsLabel.setWidth("100%");
		
		matchingListsLabel = new Label(messageSource.getMessage(Message.TOTAL_RESULTS) + ": " 
	       		 + "  <b>" + 0 + "</b>", Label.CONTENT_XHTML);
			
		matchingListsLabel.setStyleName("lm-search-results-label");

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
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
	}

	@Override
	public void layoutComponents() {
		
		setWidth("100%");

		addComponent(matchingListsLabel);
		addComponent(matchingListsTableWithSelectAll);
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
}
