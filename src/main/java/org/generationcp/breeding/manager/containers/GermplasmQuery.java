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

package org.generationcp.breeding.manager.containers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.Query;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

/**
 * An implementation of Query which is needed for using the LazyQueryContainer.
 */
@Configurable
public class GermplasmQuery implements Query {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmQuery.class);

	@Resource
	private GermplasmDataManager germplasmDataManager;
	@Resource
	private LocationDataManager locationDataManager;
	@Resource
	private PedigreeService pedigreeService;
	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	private final List<String> columnIds;
	private final ListManagerMain listManagerMain;
	private boolean viaToolUrl = true;
	private boolean showAddToList = true;

	private final List<Germplasm> germplasmSearchResults;
	private final Table matchingGermplasmsTable;

	public GermplasmQuery(final ListManagerMain listManagerMain, final boolean viaToolUrl, final boolean showAddToList,
			final List<Germplasm> germplasmSearchResults, final Table matchingGermplasmsTable) {
		super();

		this.listManagerMain = listManagerMain;
		this.viaToolUrl = viaToolUrl;
		this.showAddToList = showAddToList;
		this.germplasmSearchResults = germplasmSearchResults;
		this.matchingGermplasmsTable = matchingGermplasmsTable;
		this.columnIds = Arrays.asList(matchingGermplasmsTable.getColumnHeaders());
	}

	@Override
	public Item constructItem() {
		final PropertysetItem item = new PropertysetItem();
		for (final String id : this.columnIds) {
			item.addItemProperty(id, new ObjectProperty<String>(id));
		}
		return item;
	}

	@Override
	public boolean deleteAllItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Item> loadItems(final int startIndex, final int count) {
		final List<Item> items = new ArrayList<Item>();
		final List<Germplasm> list = this.getGermplasmSearchResults(startIndex, count);

		for (final Germplasm germplasm : list) {
			items.add(this.getGermplasmItem(germplasm));
		}
		return items;
	}

	private Item getGermplasmItem(final Germplasm germplasm) {

		final Map<Integer, String> locationsMap = new HashMap<>();
		final Map<Integer, String> methodsMap = new HashMap<>();

		final Integer gid = germplasm.getGid();
		final GermplasmInventory inventoryInfo = germplasm.getInventoryInfo();

		final Item item = new PropertysetItem();
		final int numOfCols = this.columnIds.size();
		for (int i = 0; i < numOfCols; i++) {
			switch (i) {
				case 0:
					item.addItemProperty(this.columnIds.get(i), new ObjectProperty<CheckBox>(this.getItemCheckBox(gid)));
					break;
				case 1:
					item.addItemProperty(this.columnIds.get(i), new ObjectProperty<Button>(this.getNamesButton(gid)));
					break;
				case 2:
					item.addItemProperty(this.columnIds.get(i), new ObjectProperty<String>(this.getAvailableInventory(inventoryInfo)));
					break;
				case 3:
					item.addItemProperty(this.columnIds.get(i), new ObjectProperty<String>(this.getCrossExpansion(gid)));
					break;
				case 4:
					item.addItemProperty(this.columnIds.get(i), new ObjectProperty<String>(this.getSeedReserved(inventoryInfo)));
					break;
				case 5:
					item.addItemProperty(this.columnIds.get(i), new ObjectProperty<Label>(this.getStockIDs(inventoryInfo)));
					break;
				case 6:
					item.addItemProperty(this.columnIds.get(i), new ObjectProperty<Button>(this.getGidButton(gid)));
					break;
				case 7:
					item.addItemProperty(this.columnIds.get(i), new ObjectProperty<Integer>(germplasm.getMgid()));
					break;
				case 8:
					item.addItemProperty(this.columnIds.get(i),
							new ObjectProperty<String>(this.retrieveMethodName(germplasm.getMethodId(), methodsMap)));
					break;
				case 9:
					item.addItemProperty(this.columnIds.get(i),
							new ObjectProperty<String>(this.retrieveLocationName(germplasm.getLocationId(), locationsMap)));
					break;
				default:
					break;
			}

		}
		return item;
	}

	private Button getGidButton(final Integer gid) {
		final Button gidButton = new Button(String.format("%s", gid.toString()), this.createGermplasmListener(gid));
		gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		return gidButton;
	}

	private String getCrossExpansion(final Integer gid) {
		return this.pedigreeService.getCrossExpansion(gid, this.crossExpansionProperties);

	}

	private GidLinkButtonClickListener createGermplasmListener(final Integer gid) {
		return new GidLinkButtonClickListener(this.listManagerMain, String.valueOf(gid), this.viaToolUrl, this.showAddToList);
	}

	private Button getNamesButton(final Integer gid) {
		final String germplasmFullName = this.getGermplasmNames(gid);
		final String shortenedNames = this.getShortenedNames(germplasmFullName);

		final Button namesButton = new Button(shortenedNames, this.createGermplasmListener(gid));
		namesButton.setStyleName(BaseTheme.BUTTON_LINK);
		namesButton.setDescription(germplasmFullName);

		return namesButton;
	}

	private CheckBox getItemCheckBox(final Integer gid) {
		final CheckBox itemCheckBox = new CheckBox();
		itemCheckBox.setData(gid);
		itemCheckBox.setImmediate(true);

		// TODO needs to extract this listener so that the matching germplasms table will not be tightly coupled to this class
		itemCheckBox.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				final CheckBox itemCheckBox = (CheckBox) event.getButton();
				if (((Boolean) itemCheckBox.getValue()).equals(true)) {
					GermplasmQuery.this.matchingGermplasmsTable.select(itemCheckBox.getData());
				} else {
					GermplasmQuery.this.matchingGermplasmsTable.unselect(itemCheckBox.getData());
				}
			}

		});
		return itemCheckBox;
	}

	protected List<Germplasm> getGermplasmSearchResults(final int startIndex, final int count) {
		return this.germplasmSearchResults.subList(startIndex, startIndex + count);
	}

	protected String getStudyDate(final String date, final SimpleDateFormat oldFormat, final SimpleDateFormat format) {
		String value;
		try {
			if (date == null) {
				value = "";
			} else {
				value = format.format(oldFormat.parse(date));
			}
		} catch (final ParseException e) {
			GermplasmQuery.LOG.debug(e.getMessage());
			value = "N/A";
		}
		return value;
	}

	@Override
	public void saveItems(final List<Item> arg0, final List<Item> arg1, final List<Item> arg2) {
		throw new UnsupportedOperationException();

	}

	@Override
	public int size() {
		return this.germplasmSearchResults.size();
	}

	String getShortenedNames(final String germplasmFullName) {
		return germplasmFullName.length() > 20 ? germplasmFullName.substring(0, 20) + "..." : germplasmFullName;
	}

	private String getGermplasmNames(final int gid) {
		final StringBuilder germplasmNames = new StringBuilder("");

		final List<Name> names = this.germplasmDataManager.getNamesByGID(new Integer(gid), null, null);

		int i = 0;
		for (final Name n : names) {
			if (i < names.size() - 1) {
				germplasmNames.append(n.getNval() + ", ");
			} else {
				germplasmNames.append(n.getNval());
			}
			i++;
		}

		return germplasmNames.toString();
	}

	private String getSeedReserved(final GermplasmInventory inventoryInfo) {
		String seedRes = "-";
		final Integer reservedLotCount = inventoryInfo.getReservedLotCount();
		if (reservedLotCount != null && reservedLotCount.intValue() != 0) {
			seedRes = reservedLotCount.toString();
		}
		return seedRes;
	}

	private String getAvailableInventory(final GermplasmInventory inventoryInfo) {
		String availInv = "-";
		final Integer actualInventoryLotCount = inventoryInfo.getActualInventoryLotCount();
		if (actualInventoryLotCount != null && actualInventoryLotCount.intValue() != 0) {
			availInv = actualInventoryLotCount.toString();
		}
		return availInv;
	}

	private Label getStockIDs(final GermplasmInventory inventoryInfo) {
		final String stockIDs = inventoryInfo.getStockIDs();
		final Label stockLabel = new Label(stockIDs);
		stockLabel.setDescription(stockIDs);
		return stockLabel;
	}

	private String retrieveMethodName(final Integer methodId, final Map<Integer, String> methodsMap) {
		String methodName = "-";
		if (methodsMap.get(methodId) == null) {
			final Method germplasmMethod = this.germplasmDataManager.getMethodByID(methodId);
			if (germplasmMethod != null && germplasmMethod.getMname() != null) {
				methodName = germplasmMethod.getMname();
				methodsMap.put(methodId, methodName);
			}
		} else {
			methodName = methodsMap.get(methodId);
		}
		return methodName;
	}

	private String retrieveLocationName(final Integer locId, final Map<Integer, String> locationsMap) {
		String locationName = "-";
		if (locationsMap.get(locId) == null) {
			final Location germplasmLocation = this.locationDataManager.getLocationByID(locId);
			if (germplasmLocation != null && germplasmLocation.getLname() != null) {
				locationName = germplasmLocation.getLname();
				locationsMap.put(locId, locationName);
			}
		} else {
			locationName = locationsMap.get(locId);
		}
		return locationName;
	}

}
