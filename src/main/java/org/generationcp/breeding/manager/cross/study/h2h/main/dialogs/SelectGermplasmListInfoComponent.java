/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.cross.study.h2h.main.dialogs;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@Configurable
public class SelectGermplasmListInfoComponent extends GridLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 3594330437767497353L;

	private final static Logger LOG = LoggerFactory.getLogger(SelectGermplasmListInfoComponent.class);

	public static final String GID = "gid";
	public static final String ENTRY_ID = "entryId";
	public static final String SEED_SOURCE = "seedSource";
	public static final String DESIGNATION = "designation";
	public static final String GROUP_NAME = "groupName";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	private Label selectedListLabel;
	private Label selectedListValue;
	private Label descriptionLabel;
	private Label descriptionValue;
	private Label listEntriesLabel;
	private Table listEntryValues;
	private String listName = "";

	private final Integer lastOpenedListId;
	private Integer germplasmListId;
	private final Component source;

	public SelectGermplasmListInfoComponent(Integer lastOpenedListId, Component source) {
		this.lastOpenedListId = lastOpenedListId;
		this.source = source;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.assemble();
	}

	public Integer getGermplasmListId() {
		return this.germplasmListId;
	}

	public void setGermplasmListId(Integer germplasmListId) {
		this.germplasmListId = germplasmListId;
	}

	protected void assemble() {
		this.initializeComponents();
		this.initializeValues();
		this.initializeLayout();
		this.initializeActions();
	}

	protected void initializeComponents() {
		this.selectedListLabel = new Label(this.messageSource.getMessage(Message.SELECTED_LIST_LABEL));
		this.selectedListValue = new Label();
		this.descriptionLabel = new Label(this.messageSource.getMessage(Message.DESCRIPTION_LABEL));
		this.descriptionValue = new Label();
		this.listEntriesLabel = new Label(this.messageSource.getMessage(Message.LIST_ENTRIES_LABEL));
		this.listEntryValues = this.createEntryTable();
	}

	protected void initializeValues() {
		if (this.lastOpenedListId != null) {
			try {
				GermplasmList lastOpenedList = this.germplasmListManager.getGermplasmListById(this.lastOpenedListId);
				this.displayListInfo(lastOpenedList);
				this.populateEntryTable(lastOpenedList);
			} catch (MiddlewareQueryException e) {
				SelectGermplasmListInfoComponent.LOG.error(e.toString() + "\n" + e.getStackTrace());
				e.printStackTrace();
			}
		}
	}

	protected void initializeLayout() {
		this.setColumns(2);
		// set column 1 to take up 1/5 (20%) the width of the layout. column 2 gets 4/5 (80%).
		this.setColumnExpandRatio(0, 1);
		this.setColumnExpandRatio(1, 4);
		this.setRows(4);
		this.setRowExpandRatio(0, 1);
		this.setRowExpandRatio(1, 1);
		this.setRowExpandRatio(2, 1);
		this.setRowExpandRatio(3, 35);
		this.setSpacing(false);

		this.addComponent(this.selectedListLabel, 0, 0);
		this.setComponentAlignment(this.selectedListLabel, Alignment.MIDDLE_LEFT);
		this.addComponent(this.selectedListValue, 1, 0);
		this.setComponentAlignment(this.selectedListValue, Alignment.MIDDLE_LEFT);
		this.addComponent(this.descriptionLabel, 0, 1);
		this.setComponentAlignment(this.descriptionLabel, Alignment.MIDDLE_LEFT);
		this.addComponent(this.descriptionValue, 1, 1);
		this.setComponentAlignment(this.descriptionValue, Alignment.MIDDLE_LEFT);
		this.addComponent(this.listEntriesLabel, 0, 2);
		this.setComponentAlignment(this.listEntriesLabel, Alignment.MIDDLE_LEFT);
		this.addComponent(this.listEntryValues, 0, 3, 1, 3);
	}

	protected void initializeActions() {

	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {

	}

	public void displayListInfo(GermplasmList germplasmList) throws MiddlewareQueryException {
		String listDesc = "";
		if (germplasmList != null) {
			this.listName = germplasmList.getName();
			listDesc = germplasmList.getDescription();
			this.germplasmListId = germplasmList.getId();

			// assign Germplasm List ID as data for List Entries table, to be retrieved in
			// SelectGermplasmListWindow.populateParentList() to remember last selected Germplasm List
			this.listEntryValues.setData(germplasmList.getId());
		}
		this.selectedListValue.setValue(this.listName);
		this.descriptionValue.setValue(listDesc);

		this.populateEntryTable(germplasmList);
		this.setWidth("100%");
		this.requestRepaint();
	}

	private Table createEntryTable() {
		Table listEntryValues = new Table("");

		listEntryValues.setPageLength(15); // number of rows to display in the Table
		listEntryValues.setWidth("495px");
		listEntryValues.setHeight("100%");

		listEntryValues.addContainerProperty(SelectGermplasmListInfoComponent.ENTRY_ID, Integer.class, null);
		listEntryValues.addContainerProperty(SelectGermplasmListInfoComponent.GID, Integer.class, null);
		listEntryValues.addContainerProperty(SelectGermplasmListInfoComponent.DESIGNATION, String.class, null);
		listEntryValues.addContainerProperty(SelectGermplasmListInfoComponent.SEED_SOURCE, String.class, null);
		listEntryValues.addContainerProperty(SelectGermplasmListInfoComponent.GROUP_NAME, String.class, null);

		this.messageSource.setColumnHeader(listEntryValues, SelectGermplasmListInfoComponent.ENTRY_ID, Message.LISTDATA_ENTRY_ID_HEADER);
		this.messageSource.setColumnHeader(listEntryValues, SelectGermplasmListInfoComponent.GID, Message.LISTDATA_GID_HEADER);
		this.messageSource.setColumnHeader(listEntryValues, SelectGermplasmListInfoComponent.DESIGNATION,
				Message.LISTDATA_DESIGNATION_HEADER);
		this.messageSource.setColumnHeader(listEntryValues, SelectGermplasmListInfoComponent.SEED_SOURCE,
				Message.LISTDATA_SEEDSOURCE_HEADER);
		this.messageSource.setColumnHeader(listEntryValues, SelectGermplasmListInfoComponent.GROUP_NAME, Message.LISTDATA_GROUPNAME_HEADER);

		return listEntryValues;
	}

	private void populateEntryTable(GermplasmList germplasmList) throws MiddlewareQueryException {
		if (this.listEntryValues.removeAllItems() && germplasmList != null) {
			int germplasmListId = germplasmList.getId();
			List<GermplasmListData> listDatas =
					this.germplasmListManager.getGermplasmListDataByListId(germplasmListId);
			for (GermplasmListData data : listDatas) {
				this.listEntryValues.addItem(new Object[] {data.getEntryId(), data.getGid(), data.getDesignation(), data.getSeedSource(),
						data.getGroupName()}, data.getId());
			}
			this.listEntryValues.sort(new Object[] {SelectGermplasmListInfoComponent.ENTRY_ID}, new boolean[] {true});
			this.listEntryValues.setVisibleColumns(new String[] {SelectGermplasmListInfoComponent.ENTRY_ID,
					SelectGermplasmListInfoComponent.GID, SelectGermplasmListInfoComponent.DESIGNATION,
					SelectGermplasmListInfoComponent.SEED_SOURCE, SelectGermplasmListInfoComponent.GROUP_NAME});

			if (this.source instanceof SelectGermplasmListDialog) {
				((SelectGermplasmListDialog) this.source).setDoneButton(true);
			}

		}
	}

	public Table getEntriesTable() {
		return this.listEntryValues;
	}

	public String getListName() {
		return this.listName;
	}
}
