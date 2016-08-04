
package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.AdditionalDetailsCrossNameComponent;
import org.generationcp.breeding.manager.listmanager.FillWithAttributeWindow;
import org.generationcp.breeding.manager.listmanager.ListTabComponent;
import org.generationcp.breeding.manager.util.GermplasmDetailModel;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.google.common.collect.Iterables;
import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.HeaderClickEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class FillWith implements InternationalizableComponent {

	private static final String EMPTY_STRING = "";

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	private final class TableHeaderClickListener implements Table.HeaderClickListener {

		private static final long serialVersionUID = 4792602001489368804L;

		@Override
		public void headerClick(HeaderClickEvent event) {
			if (event.getButton() == com.vaadin.event.MouseEvents.ClickEvent.BUTTON_RIGHT) {
				String column = (String) event.getPropertyId();
				FillWith.this.fillWithMenu.setData(column);
				if (column.equals(ColumnLabels.ENTRY_CODE.getName())) {
					FillWith.this.menuFillWithLocationName.setVisible(false);
					FillWith.this.menuFillWithCrossExpansion.setVisible(false);
					FillWith.this.setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(true);
					FillWith.this.fillWithMenu.show(event.getClientX(), event.getClientY());
				} else if (column.equals(ColumnLabels.SEED_SOURCE.getName())) {
					FillWith.this.menuFillWithLocationName.setVisible(true);
					FillWith.this.menuFillWithCrossExpansion.setVisible(false);
					FillWith.this.setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(true);
					FillWith.this.fillWithMenu.show(event.getClientX(), event.getClientY());
				} else if (column.equals(ColumnLabels.PARENTAGE.getName())) {
					FillWith.this.setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(false);
					FillWith.this.menuFillWithLocationName.setVisible(false);
					FillWith.this.menuFillWithCrossExpansion.setVisible(true);
					FillWith.this.fillWithMenu.show(event.getClientX(), event.getClientY());
				}
			}
		}
	}

	private final class FillWithMenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2384037190598803030L;

		@Override
		public void contextItemClick(ClickEvent event) {
			// Get reference to clicked item
			ContextMenuItem clickedItem = event.getClickedItem();

			FillWith.this.trackFillWith((String) FillWith.this.fillWithMenu.getData());

			if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_EMPTY))) {
				FillWith.this.fillWithEmpty(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_LOCATION_NAME))) {
				FillWith.this.fillWithLocation(FillWith.this.targetTable);
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_GERMPLASM_DATE))) {
				FillWith.this.fillWithGermplasmDate(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_PREF_NAME))) {
				FillWith.this.fillWithPreferredName(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_PREF_ID))) {
				FillWith.this.fillWithPreferredID(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_ATTRIBUTE))) {
				FillWith.this.fillWithAttribute(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_NAME))) {
				FillWith.this.fillWithMethodName(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_ABBREVIATION))) {
				FillWith.this.fillWithMethodAbbreviation(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_NUMBER))) {
				FillWith.this.fillWithMethodNumber(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_GROUP))) {
				FillWith.this.fillWithMethodGroup(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_CROSS_FEMALE_GID))) {
				FillWith.this.fillWithCrossFemaleGID(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_CROSS_FEMALE_PREFERRED_NAME))) {
				FillWith.this.fillWithCrossFemalePreferredName(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_CROSS_MALE_GID))) {
				FillWith.this.fillWithCrossMaleGID(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_CROSS_MALE_PREFERRED_NAME))) {
				FillWith.this.fillWithCrossMalePreferredName(FillWith.this.targetTable, (String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_CROSS_EXPANSION))) {
				FillWith.this.displayExpansionLevelPopupWindow((String) FillWith.this.fillWithMenu.getData());
			} else if (clickedItem.getName().equals(FillWith.this.messageSource.getMessage(Message.FILL_WITH_SEQUENCE_NUMBER))) {
				FillWith.this.displaySequenceNumberPopupWindow((String) FillWith.this.fillWithMenu.getData());
			}
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(FillWith.class);

	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;
	@Autowired
	private PedigreeService pedigreeService;

	private AbstractLayout parentLayout;

	private final Table targetTable;
	private final String gidPropertyId;
	private List<String> filledWithPropertyIds;

	private ContextMenu fillWithMenu;
	private ContextMenuItem menuFillWithEmpty;
	private ContextMenuItem menuFillWithGermplasmDate;
	private ContextMenuItem menuFillWithPrefName;
	private ContextMenuItem menuFillWithPrefID;
	private ContextMenuItem menuFillWithAttribute;
	private ContextMenuItem menuFillWithLocationName;
	private ContextMenuItem menuFillWithBreedingMethodInfo;
	private ContextMenuItem menuFillWithBreedingMethodName;
	private ContextMenuItem menuFillWithBreedingMethodGroup;
	private ContextMenuItem menuFillWithBreedingMethodNumber;
	private ContextMenuItem menuFillWithBreedingMethodAbbreviation;
	private ContextMenuItem menuFillWithCrossFemaleInformation;
	private ContextMenuItem menuFillWithCrossFemaleGID;
	private ContextMenuItem menuFillWithCrossFemalePreferredName;
	private ContextMenuItem menuFillWithCrossMaleInformation;
	private ContextMenuItem menuFillWithCrossMaleGID;
	private ContextMenuItem menuFillWithCrossMalePreferredName;
	private ContextMenuItem menuFillWithCrossExpansion;
	private ContextMenuItem menuFillWithSequenceNumber;

	private Table.HeaderClickListener headerClickListener;

	private GermplasmDetailModel germplasmDetail;

	private Integer crossExpansionLevel = Integer.valueOf(1);

	private ListTabComponent listDetailsComponent;

	private org.generationcp.breeding.manager.listmanager.ListBuilderComponent buildListComponent;

	public FillWith(String gidPropertyId, Table targetTable) {
		this.gidPropertyId = gidPropertyId;
		this.targetTable = targetTable;
	}

	/**
	 * Add Fill With context menu to a table
	 * 
	 * @param listManagerTreeMenu - contextMenu will attach to this
	 * @param targetTable - table where data will be manipulated
	 * @param gidPropertyId - property of GID (button with GID as caption) on that table
	 * @param propertyIdsContextMenuAvailableTo - list of property ID's where context menu will be available for "right clicking"
	 */
	public FillWith(final SimpleResourceBundleMessageSource messageSource, final Table targetTable, String gidPropertyId) {
		this.gidPropertyId = gidPropertyId;
		this.targetTable = targetTable;
		this.messageSource = messageSource;
		this.filledWithPropertyIds = new ArrayList<String>();

		this.setupContextMenu();
	}

	/**
	 * Add Fill With context menu to a table
	 * 
	 * @param parentLayout - contextMenu will attach to this
	 * @param targetTable - table where data will be manipulated
	 * @param gidPropertyId - property of GID (button with GID as caption) on that table
	 * @param propertyIdsContextMenuAvailableTo - list of property ID's where context menu will be available for "right clicking"
	 */
	public FillWith(AbstractLayout parentLayout, final SimpleResourceBundleMessageSource messageSource, final Table targetTable,
			String gidPropertyId) {
		this.gidPropertyId = gidPropertyId;
		this.targetTable = targetTable;
		this.parentLayout = parentLayout;
		this.messageSource = messageSource;
		this.filledWithPropertyIds = new ArrayList<String>();

		this.setupContextMenu();
	}

	public FillWith(ListTabComponent listDetailsComponent, AbstractLayout parentLayout,
			final SimpleResourceBundleMessageSource messageSource, final Table targetTable, String gidPropertyId) {
		this.gidPropertyId = gidPropertyId;
		this.targetTable = targetTable;
		this.parentLayout = parentLayout;
		this.messageSource = messageSource;
		this.filledWithPropertyIds = new ArrayList<String>();
		this.listDetailsComponent = listDetailsComponent;

		this.setupContextMenu();
	}

	public FillWith(org.generationcp.breeding.manager.listmanager.ListBuilderComponent buildListComponent,
			final SimpleResourceBundleMessageSource messageSource, final Table targetTable, String gidPropertyId) {
		this.gidPropertyId = gidPropertyId;
		this.targetTable = targetTable;
		this.parentLayout = buildListComponent;
		this.messageSource = messageSource;
		this.filledWithPropertyIds = new ArrayList<String>();
		this.buildListComponent = buildListComponent;

		this.setupContextMenu();
	}

	public void fillWith(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		if (propertyId.equals(ColumnLabels.PREFERRED_ID.getName())) {
			this.fillWithPreferredID(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		} else if (propertyId.equals(ColumnLabels.PREFERRED_NAME.getName())) {
			this.fillWithPreferredName(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		} else if (propertyId.equals(ColumnLabels.GERMPLASM_DATE.getName())) {
			this.fillWithGermplasmDate(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		} else if (propertyId.equals(ColumnLabels.GERMPLASM_LOCATION.getName())) {
			this.fillWithLocation(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_NAME.getName())) {
			this.fillWithMethodName(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
			this.fillWithMethodAbbreviation(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
			this.fillWithMethodNumber(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
			this.fillWithMethodGroup(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		} else if (propertyId.equals(ColumnLabels.CROSS_FEMALE_GID.getName())) {
			this.fillWithCrossFemaleGID(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		} else if (propertyId.equals(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
			this.fillWithCrossFemalePreferredName(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		} else if (propertyId.equals(ColumnLabels.CROSS_MALE_GID.getName())) {
			this.fillWithCrossMaleGID(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		} else if (propertyId.equals(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())) {
			this.fillWithCrossMalePreferredName(table, propertyId, onlyFillWithThoseHavingEmptyValues);
		}
	}

	private void setupContextMenu() {

		this.headerClickListener = new TableHeaderClickListener();

		this.fillWithMenu = new ContextMenu();
		this.fillWithMenu.setWidth("310px");

		this.menuFillWithEmpty = this.fillWithMenu.addItem(this.messageSource.getMessage(Message.FILL_WITH_EMPTY));
		this.menuFillWithLocationName = this.fillWithMenu.addItem(this.messageSource.getMessage(Message.FILL_WITH_LOCATION_NAME));
		this.menuFillWithPrefID = this.fillWithMenu.addItem(this.messageSource.getMessage(Message.FILL_WITH_PREF_ID));
		this.menuFillWithGermplasmDate = this.fillWithMenu.addItem(this.messageSource.getMessage(Message.FILL_WITH_GERMPLASM_DATE));
		this.menuFillWithPrefName = this.fillWithMenu.addItem(this.messageSource.getMessage(Message.FILL_WITH_PREF_NAME));
		this.menuFillWithAttribute = this.fillWithMenu.addItem(this.messageSource.getMessage(Message.FILL_WITH_ATTRIBUTE));

		this.menuFillWithBreedingMethodInfo =
				this.fillWithMenu.addItem(this.messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_INFO));
		this.menuFillWithBreedingMethodName =
				this.menuFillWithBreedingMethodInfo.addItem(this.messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_NAME));
		this.menuFillWithBreedingMethodAbbreviation =
				this.menuFillWithBreedingMethodInfo.addItem(this.messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_ABBREVIATION));
		this.menuFillWithBreedingMethodNumber =
				this.menuFillWithBreedingMethodInfo.addItem(this.messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_NUMBER));
		this.menuFillWithBreedingMethodGroup =
				this.menuFillWithBreedingMethodInfo.addItem(this.messageSource.getMessage(Message.FILL_WITH_BREEDING_METHOD_GROUP));

		this.menuFillWithCrossFemaleInformation =
				this.fillWithMenu.addItem(this.messageSource.getMessage(Message.FILL_WITH_CROSS_FEMALE_INFORMATION));
		this.menuFillWithCrossFemaleGID =
				this.menuFillWithCrossFemaleInformation.addItem(this.messageSource.getMessage(Message.FILL_WITH_CROSS_FEMALE_GID));
		this.menuFillWithCrossFemalePreferredName =
				this.menuFillWithCrossFemaleInformation.addItem(this.messageSource
						.getMessage(Message.FILL_WITH_CROSS_FEMALE_PREFERRED_NAME));

		this.menuFillWithCrossMaleInformation =
				this.fillWithMenu.addItem(this.messageSource.getMessage(Message.FILL_WITH_CROSS_MALE_INFORMATION));
		this.menuFillWithCrossMaleGID =
				this.menuFillWithCrossMaleInformation.addItem(this.messageSource.getMessage(Message.FILL_WITH_CROSS_MALE_GID));
		this.menuFillWithCrossMalePreferredName =
				this.menuFillWithCrossMaleInformation.addItem(this.messageSource.getMessage(Message.FILL_WITH_CROSS_MALE_PREFERRED_NAME));

		this.menuFillWithCrossExpansion = this.fillWithMenu.addItem(this.messageSource.getMessage(Message.FILL_WITH_CROSS_EXPANSION));
		this.menuFillWithSequenceNumber = this.fillWithMenu.addItem("Fill with Sequence Number");

		this.fillWithMenu.addListener(new FillWithMenuClickListener());

		if (this.parentLayout != null) {
			this.parentLayout.addComponent(this.fillWithMenu);
		}

		this.targetTable.addListener(this.headerClickListener);
	}

	public void setContextMenuEnabled(Boolean isEnabled) {
		this.targetTable.removeListener(this.headerClickListener);
		if (isEnabled) {
			this.targetTable.addListener(this.headerClickListener);
		}
	}

	public List<Integer> getGidsFromTable(Table table) {
		List<Integer> gids = new ArrayList<Integer>();
		List<Integer> listDataItemIds = this.getItemIds(table);
		for (Integer itemId : listDataItemIds) {
			gids.add(Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
					.toString()));
		}
		return gids;
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getItemIds(Table table) {
		List<Integer> itemIds = new ArrayList<Integer>();
		itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
		return itemIds;
	}

	private void markHasChangesFlagsAndToggleTableEditable(Table table) {
		// mark flag that changes have been made in listDataTable
		if (this.listDetailsComponent != null) {
			this.listDetailsComponent.getListComponent().setHasUnsavedChanges(true);
		}

		if (this.buildListComponent != null) {
			this.buildListComponent.setHasUnsavedChanges(true);
		}

		// To trigger TableFieldFactory (fix for truncated data)
		if (table.isEditable()) {
			table.setEditable(false);
			table.setEditable(true);
		}
	}

	public void fillWithEmpty(Table table, String propertyId) {
		List<Integer> itemIds = this.getItemIds(table);
		for (Integer itemId : itemIds) {
			table.getItem(itemId).getItemProperty(propertyId).setValue(FillWith.EMPTY_STRING);
		}

		this.markHasChangesFlagsAndToggleTableEditable(table);
	}

	public void fillWithAttribute(Table table, String propertyId) {
		Window mainWindow = table.getWindow();
		Window attributeWindow =
				new FillWithAttributeWindow(table, this.gidPropertyId, propertyId, this.messageSource, this.listDetailsComponent,
						this.buildListComponent);
		attributeWindow.setStyleName(Reindeer.WINDOW_LIGHT);
		mainWindow.addWindow(attributeWindow);
	}

	public void fillWithGermplasmDate(Table table, String propertyId) {
		this.fillWithGermplasmDate(table, propertyId, false);
	}

	public void fillWithGermplasmDate(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		try {
			List<Integer> itemIds = this.getItemIds(table);
			List<Integer> gids = this.getGidsFromTable(table);
			Map<Integer, Integer> germplasmGidDateMap = this.germplasmDataManager.getGermplasmDatesByGids(gids);

			for (Integer itemId : itemIds) {
				if (!onlyFillWithThoseHavingEmptyValues || table.getItem(itemId).getItemProperty(propertyId).getValue() == null
						|| table.getItem(itemId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
					Integer gid =
							Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
									.toString());
					table.getItem(itemId).getItemProperty(propertyId).setValue(germplasmGidDateMap.get(gid));
				}
			}

			this.markHasChangesFlagsAndToggleTableEditable(table);

		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
	}

	public void fillWithMethodName(Table table, String propertyId) {
		this.fillWithMethodName(table, propertyId, false);
	}

	public void fillWithMethodName(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		try {
			List<Integer> itemIds = this.getItemIds(table);
			List<Integer> gids = this.getGidsFromTable(table);
			Map<Integer, Object> germplasmGidDateMap = this.germplasmDataManager.getMethodsByGids(gids);

			for (Integer itemId : itemIds) {
				if (!onlyFillWithThoseHavingEmptyValues || table.getItem(itemId).getItemProperty(propertyId).getValue() == null
						|| table.getItem(itemId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
					Integer gid =
							Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
									.toString());
					table.getItem(itemId).getItemProperty(propertyId)
							.setValue(((Method) germplasmGidDateMap.get(gid)).getMname().toString());
				}
			}

			this.markHasChangesFlagsAndToggleTableEditable(table);

		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
	}

	public void fillWithMethodAbbreviation(Table table, String propertyId) {
		this.fillWithMethodAbbreviation(table, propertyId, false);
	}

	public void fillWithMethodAbbreviation(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		try {
			List<Integer> itemIds = this.getItemIds(table);
			List<Integer> gids = this.getGidsFromTable(table);
			Map<Integer, Object> germplasmGidDateMap = this.germplasmDataManager.getMethodsByGids(gids);

			for (Integer itemId : itemIds) {
				if (!onlyFillWithThoseHavingEmptyValues || table.getItem(itemId).getItemProperty(propertyId).getValue() == null
						|| table.getItem(itemId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
					Integer gid =
							Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
									.toString());
					table.getItem(itemId).getItemProperty(propertyId)
							.setValue(((Method) germplasmGidDateMap.get(gid)).getMcode().toString());
				}
			}

			this.markHasChangesFlagsAndToggleTableEditable(table);

		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
	}

	public void fillWithMethodNumber(Table table, String propertyId) {
		this.fillWithMethodNumber(table, propertyId, false);
	}

	public void fillWithMethodNumber(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		try {
			List<Integer> itemIds = this.getItemIds(table);
			List<Integer> gids = this.getGidsFromTable(table);
			Map<Integer, Object> germplasmGidDateMap = this.germplasmDataManager.getMethodsByGids(gids);

			for (Integer itemId : itemIds) {
				if (!onlyFillWithThoseHavingEmptyValues || table.getItem(itemId).getItemProperty(propertyId).getValue() == null
						|| table.getItem(itemId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
					Integer gid =
							Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
									.toString());
					table.getItem(itemId).getItemProperty(propertyId).setValue(((Method) germplasmGidDateMap.get(gid)).getMid().toString());
				}
			}

			this.markHasChangesFlagsAndToggleTableEditable(table);

		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
	}

	public void fillWithMethodGroup(Table table, String propertyId) {
		this.fillWithMethodGroup(table, propertyId, false);
	}

	public void fillWithMethodGroup(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		try {
			List<Integer> itemIds = this.getItemIds(table);
			List<Integer> gids = this.getGidsFromTable(table);
			Map<Integer, Object> germplasmGidDateMap = this.germplasmDataManager.getMethodsByGids(gids);

			for (Integer itemId : itemIds) {
				if (!onlyFillWithThoseHavingEmptyValues || table.getItem(itemId).getItemProperty(propertyId).getValue() == null
						|| table.getItem(itemId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
					Integer gid =
							Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
									.toString());
					table.getItem(itemId).getItemProperty(propertyId)
							.setValue(((Method) germplasmGidDateMap.get(gid)).getMgrp().toString());
				}
			}

			this.markHasChangesFlagsAndToggleTableEditable(table);

		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
	}

	public void fillWithCrossFemaleGID(Table table, String propertyId) {
		this.fillWithCrossFemaleGID(table, propertyId, false);
	}

	public void fillWithCrossFemaleGID(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		try {
			List<Integer> itemIds = this.getItemIds(table);
			for (Integer itemId : itemIds) {
				if (!onlyFillWithThoseHavingEmptyValues || table.getItem(itemId).getItemProperty(propertyId).getValue() == null
						|| table.getItem(itemId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
					Integer gid =
							Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
									.toString());
					Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(gid);
					table.getItem(itemId).getItemProperty(propertyId).setValue(germplasm.getGpid1());
				}
			}

			this.markHasChangesFlagsAndToggleTableEditable(table);

		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
	}

	public void fillWithCrossFemalePreferredName(Table table, String propertyId) {
		this.fillWithCrossFemalePreferredName(table, propertyId, false);
	}

	public void fillWithCrossFemalePreferredName(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		try {
			List<Integer> itemIds = this.getItemIds(table);
			for (Integer itemId : itemIds) {
				if (!onlyFillWithThoseHavingEmptyValues || table.getItem(itemId).getItemProperty(propertyId).getValue() == null
						|| table.getItem(itemId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
					Integer gid =
							Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
									.toString());
					Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(gid);
					List<Integer> parentGids = new ArrayList<Integer>();
					parentGids.add(germplasm.getGpid1());
					Map<Integer, String> preferredNames = this.germplasmDataManager.getPreferredNamesByGids(parentGids);

					String femalePreferredName = FillWith.EMPTY_STRING;
					if (preferredNames.get(germplasm.getGpid1()) != null) {
						femalePreferredName = preferredNames.get(germplasm.getGpid1());
					}
					table.getItem(itemId).getItemProperty(propertyId).setValue(femalePreferredName);
				}
			}

			this.markHasChangesFlagsAndToggleTableEditable(table);

		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
	}

	public void fillWithCrossMaleGID(Table table, String propertyId) {
		this.fillWithCrossMaleGID(table, propertyId, false);
	}

	public void fillWithCrossMaleGID(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		try {
			List<Integer> itemIds = this.getItemIds(table);
			for (Integer itemId : itemIds) {
				if (!onlyFillWithThoseHavingEmptyValues || table.getItem(itemId).getItemProperty(propertyId).getValue() == null
						|| table.getItem(itemId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
					Integer gid =
							Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
									.toString());
					Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(gid);
					table.getItem(itemId).getItemProperty(propertyId).setValue(germplasm.getGpid2());
				}
			}

			this.markHasChangesFlagsAndToggleTableEditable(table);

		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
	}

	public void fillWithCrossMalePreferredName(Table table, String propertyId) {
		this.fillWithCrossMalePreferredName(table, propertyId, false);
	}

	public void fillWithCrossMalePreferredName(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		try {
			List<Integer> itemIds = this.getItemIds(table);
			for (Integer itemId : itemIds) {
				if (!onlyFillWithThoseHavingEmptyValues || table.getItem(itemId).getItemProperty(propertyId).getValue() == null
						|| table.getItem(itemId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
					Integer gid =
							Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
									.toString());
					Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(gid);
					List<Integer> parentGids = new ArrayList<Integer>();
					parentGids.add(germplasm.getGpid2());
					Map<Integer, String> preferredNames = this.germplasmDataManager.getPreferredNamesByGids(parentGids);
					String malePreferredName = FillWith.EMPTY_STRING;
					if (preferredNames.get(germplasm.getGpid2()) != null) {
						malePreferredName = preferredNames.get(germplasm.getGpid2());
					}
					table.getItem(itemId).getItemProperty(propertyId).setValue(malePreferredName);
				}
			}

			this.markHasChangesFlagsAndToggleTableEditable(table);

		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
	}

	protected void fillWithPreferredName(Table table, String propertyId) {
		this.fillWithPreferredName(table, propertyId, false);
	}

	protected void fillWithPreferredName(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		for (Iterator<?> i = table.getItemIds().iterator(); i.hasNext();) {
			int listDataId = (Integer) i.next();
			if (!onlyFillWithThoseHavingEmptyValues || table.getItem(listDataId).getItemProperty(propertyId).getValue() == null
					|| table.getItem(listDataId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
				// iterate through the table elements' IDs

				Item item = table.getItem(listDataId);
				String gid = getGidFromButton(item);
				GermplasmDetailModel gModel = this.getGermplasmDetails(Integer.valueOf(gid));
				item.getItemProperty(propertyId).setValue(gModel.getGermplasmPreferredName());
			}
		}

		this.markHasChangesFlagsAndToggleTableEditable(table);

	}

	protected void fillWithPreferredID(Table table, String propertyId) {
		this.fillWithPreferredID(table, propertyId, false);
	}

	protected void fillWithPreferredID(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {
		for (Iterator<?> i = table.getItemIds().iterator(); i.hasNext();) {
			// iterate through the table elements' IDs
			int listDataId = (Integer) i.next();

			if (!onlyFillWithThoseHavingEmptyValues || table.getItem(listDataId).getItemProperty(propertyId).getValue() == null
					|| table.getItem(listDataId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
				Item item = table.getItem(listDataId);
				String gid = getGidFromButton(item);
				GermplasmDetailModel gModel = this.getGermplasmDetails(Integer.valueOf(gid));
				item.getItemProperty(propertyId).setValue(gModel.getPrefID());
			}
		}

		this.markHasChangesFlagsAndToggleTableEditable(table);

	}

	protected void fillWithLocation(Table targetTable) {

		try {
			List<Integer> gidList = this.getGidsFromTable(targetTable);
			Map<Integer, String> gidLocations;
			gidLocations = this.germplasmDataManager.getLocationNamesByGids(gidList);

			List<Integer> itemIds = this.getItemIds(targetTable);
			for (Integer itemId : itemIds) {
				Item item = targetTable.getItem(itemId);
				String gid = getGidFromButton(item);
				item.getItemProperty(ColumnLabels.SEED_SOURCE.getName()).setValue(gidLocations.get(new Integer(gid)));
			}

			this.markHasChangesFlagsAndToggleTableEditable(targetTable);

		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
	}

	protected void fillWithLocation(Table table, String propertyId, Boolean onlyFillWithThoseHavingEmptyValues) {

		try {
			List<Integer> gidList = this.getGidsFromTable(table);
			Map<Integer, String> gidLocations;
			gidLocations = this.germplasmDataManager.getLocationNamesByGids(gidList);

			List<Integer> itemIds = this.getItemIds(table);
			for (Integer itemId : itemIds) {
				if (!onlyFillWithThoseHavingEmptyValues || table.getItem(itemId).getItemProperty(propertyId).getValue() == null
						|| table.getItem(itemId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
					Item item = table.getItem(itemId);
					String gid = getGidFromButton(item);
					item.getItemProperty(propertyId).setValue(gidLocations.get(new Integer(gid)));
				}
			}

			// To trigger TableFieldFactory (fix for truncated data)
			if (table.isEditable()) {
				table.setEditable(false);
				table.setEditable(true);
			}

		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
	}

	public void fillWithSequence(String propertyId, String prefix, String suffix, int startNumber, int numOfZeros,
			boolean spaceBetweenPrefixAndCode, boolean spaceBetweenSuffixAndCode) {
		this.fillWithSequence(propertyId, prefix, suffix, startNumber, numOfZeros, spaceBetweenPrefixAndCode, spaceBetweenSuffixAndCode,
				false);
	}

	public void fillWithSequence(String propertyId, String prefix, String suffix, int startNumber, int numOfZeros,
			boolean spaceBetweenPrefixAndCode, boolean spaceBetweenSuffixAndCode, Boolean onlyFillWithThoseHavingEmptyValues) {
		List<Integer> itemIds = this.getItemIds(this.targetTable);
		int number = startNumber;
		for (Integer itemId : itemIds) {
			if (!onlyFillWithThoseHavingEmptyValues || this.targetTable.getItem(itemId).getItemProperty(propertyId).getValue() == null
					|| this.targetTable.getItem(itemId).getItemProperty(propertyId).getValue().equals(FillWith.EMPTY_STRING)) {
				Item item = this.targetTable.getItem(itemId);
				StringBuilder builder = new StringBuilder();
				builder.append(prefix);
				if (spaceBetweenPrefixAndCode) {
					builder.append(" ");
				}

				if (numOfZeros > 0) {
					String numberString = FillWith.EMPTY_STRING + number;
					int numOfZerosNeeded = numOfZeros - numberString.length();
					for (int i = 0; i < numOfZerosNeeded; i++) {
						builder.append("0");
					}
				}
				builder.append(number);

				if (suffix != null && spaceBetweenSuffixAndCode) {
					builder.append(" ");
				}

				if (suffix != null) {
					builder.append(suffix);
				}

				item.getItemProperty(propertyId).setValue(builder.toString());
				++number;
			}
		}

		this.markHasChangesFlagsAndToggleTableEditable(this.targetTable);
	}

	private void displayExpansionLevelPopupWindow(final String propertyId) {
		this.crossExpansionLevel = Integer.valueOf(1);
		final Window specifyCrossExpansionLevelWindow = new BaseSubWindow("Specify Expansion Level");
		specifyCrossExpansionLevelWindow.setHeight("135px");
		specifyCrossExpansionLevelWindow.setWidth("250px");
		specifyCrossExpansionLevelWindow.setModal(true);
		specifyCrossExpansionLevelWindow.setResizable(false);
		specifyCrossExpansionLevelWindow.setStyleName(Reindeer.WINDOW_LIGHT);

		AbsoluteLayout layout = new AbsoluteLayout();
		final ComboBox levelComboBox = new ComboBox();
		for (int ctr = 1; ctr <= 5; ctr++) {
			levelComboBox.addItem(Integer.valueOf(ctr));
		}
		levelComboBox.setValue(Integer.valueOf(1));
		levelComboBox.setNullSelectionAllowed(false);
		layout.addComponent(levelComboBox, "top:10px;left:10px");

		Button okButton = new Button(this.messageSource.getMessage(Message.OK));
		okButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -3519880320817778816L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				FillWith.this.crossExpansionLevel = (Integer) levelComboBox.getValue();
				FillWith.this.fillWithCrossExpansion(propertyId);
				FillWith.this.targetTable.getWindow().removeWindow(specifyCrossExpansionLevelWindow);
			}
		});
		okButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		layout.addComponent(okButton, "top:50px;left:10px");

		Button cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		cancelButton.setStyleName(Bootstrap.Buttons.DEFAULT.styleName());
		cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -3519880320817778816L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				FillWith.this.crossExpansionLevel = null;
				FillWith.this.targetTable.getWindow().removeWindow(specifyCrossExpansionLevelWindow);
			}
		});
		layout.addComponent(cancelButton, "top:50px;left:60px");

		specifyCrossExpansionLevelWindow.setContent(layout);

		this.targetTable.getWindow().addWindow(specifyCrossExpansionLevelWindow);
	}

	private void fillWithCrossExpansion(String propertyId) {
		if (this.crossExpansionLevel != null) {
			
			final Map<Integer, String> crossExpansions = bulkGeneratePedigreeString();

			for (Iterator<?> i = this.targetTable.getItemIds().iterator(); i.hasNext();) {
				// iterate through the table elements' IDs
				int listDataId = (Integer) i.next();
				Item item = this.targetTable.getItem(listDataId);
				String gid = getGidFromButton(item);
				try {
					String crossExpansion = crossExpansions.get(Integer.parseInt(gid));
					item.getItemProperty(propertyId).setValue(crossExpansion);
				} catch (MiddlewareQueryException ex) {
					FillWith.LOG.error("Error with getting cross expansion: gid=" + gid + " level=" + this.crossExpansionLevel, ex);
					MessageNotifier.showError(this.targetTable.getWindow(), "Database Error!", "Error with getting Cross Expansion. "
							+ this.messageSource.getMessage(Message.ERROR_REPORT_TO));
					return;
				}
			}

			this.markHasChangesFlagsAndToggleTableEditable(this.targetTable);

		}
	}

	private Map<Integer, String> bulkGeneratePedigreeString() {
		
		final Set<Integer> gidIdList = new HashSet<>();

		for (final Iterator<?> i = this.targetTable.getItemIds().iterator(); i.hasNext();) {
			final int listDataId = (Integer) i.next();
			final Item item = this.targetTable.getItem(listDataId);
			final String gidFromButton = getGidFromButton(item);
			gidIdList.add(Integer.parseInt(gidFromButton));
		}

		final Iterable<List<Integer>> partition = Iterables.partition(gidIdList, 5000);

		final Map<Integer, String> crossExpansions = new HashMap<>();

		for (List<Integer> partitionedGidList : partition) {
			final Set<Integer> partitionedGidSet = new HashSet<Integer>(partitionedGidList);
			crossExpansions.putAll(this.pedigreeService.getCrossExpansions(partitionedGidSet, this.crossExpansionLevel.intValue(),
					this.crossExpansionProperties));
		}
		return crossExpansions;
	}

	private String getGidFromButton(Item item) {
		Object gidObject = item.getItemProperty(this.gidPropertyId).getValue();
		Button b = (Button) gidObject;
		String gid = b.getCaption();
		return gid;
	}

	private void displaySequenceNumberPopupWindow(String propertyId) {
		Window specifySequenceNumberWindow = new BaseSubWindow("Specify Sequence Number");
		specifySequenceNumberWindow.setHeight("320px");
		specifySequenceNumberWindow.setWidth("530px");
		specifySequenceNumberWindow.setModal(true);
		specifySequenceNumberWindow.setResizable(false);
		specifySequenceNumberWindow.setContent(new AdditionalDetailsCrossNameComponent(this, propertyId, specifySequenceNumberWindow));
		specifySequenceNumberWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		this.targetTable.getWindow().addWindow(specifySequenceNumberWindow);
	}

	public GermplasmDetailModel getGermplasmDetails(int gid) {
		try {
			this.germplasmDetail = new GermplasmDetailModel();
			Germplasm g = this.germplasmDataManager.getGermplasmByGID(new Integer(gid));
			Name name = this.germplasmDataManager.getPreferredNameByGID(gid);

			if (g != null) {
				this.germplasmDetail.setGid(g.getGid());
				this.germplasmDetail.setGermplasmMethod(this.germplasmDataManager.getMethodByID(g.getMethodId()).getMname());
				this.germplasmDetail.setGermplasmPreferredName(name == null ? FillWith.EMPTY_STRING : name.getNval());
				this.germplasmDetail.setPrefID(this.getGermplasmPrefID(g.getGid()));
			}
			return this.germplasmDetail;
		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error(e.getMessage(), e);
		}
		return this.germplasmDetail;
	}

	private String getGermplasmPrefID(int gid) {
		String prefId = FillWith.EMPTY_STRING;
		try {
			Name preferredIdName = this.germplasmDataManager.getPreferredIdByGID(gid);
			if (preferredIdName != null) {
				prefId = preferredIdName.getNval();
			}

			return prefId;
		} catch (MiddlewareQueryException e) {
			FillWith.LOG.error("Error with getting preferred id of germplasm: " + gid, e);
		}
		return prefId;
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public List<String> getFilledWithPropertyIds() {
		return this.filledWithPropertyIds;
	}

	public void trackFillWith(String propertyId) {
		if (!this.filledWithPropertyIds.contains(propertyId)) {
			this.filledWithPropertyIds.add(propertyId);
		}
	}

	private void setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(boolean visibility) {
		this.menuFillWithBreedingMethodAbbreviation.setVisible(visibility);
		this.menuFillWithBreedingMethodGroup.setVisible(visibility);
		this.menuFillWithBreedingMethodInfo.setVisible(visibility);
		this.menuFillWithBreedingMethodName.setVisible(visibility);
		this.menuFillWithBreedingMethodNumber.setVisible(visibility);
		this.menuFillWithCrossFemaleGID.setVisible(visibility);
		this.menuFillWithCrossFemaleInformation.setVisible(visibility);
		this.menuFillWithCrossFemalePreferredName.setVisible(visibility);
		this.menuFillWithCrossMaleGID.setVisible(visibility);
		this.menuFillWithCrossMaleInformation.setVisible(visibility);
		this.menuFillWithCrossMalePreferredName.setVisible(visibility);
		this.menuFillWithEmpty.setVisible(visibility);
		this.menuFillWithGermplasmDate.setVisible(visibility);
		this.menuFillWithPrefID.setVisible(visibility);
		this.menuFillWithPrefName.setVisible(visibility);
		this.menuFillWithAttribute.setVisible(visibility);
		this.menuFillWithSequenceNumber.setVisible(visibility);
	}

	public int getNumberOfEntries() {
		return this.targetTable.getItemIds().size();
	}
}
