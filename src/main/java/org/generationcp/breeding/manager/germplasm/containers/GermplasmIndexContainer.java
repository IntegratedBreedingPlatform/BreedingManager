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

package org.generationcp.breeding.manager.germplasm.containers;

import java.util.List;

import org.generationcp.breeding.manager.cross.study.h2h.main.containers.GermplasmEnvironmentSearchQuery;
import org.generationcp.breeding.manager.cross.study.h2h.main.containers.GermplasmEnvironmentSearchQueryFactory;
import org.generationcp.breeding.manager.germplasm.GermplasmDetailModel;
import org.generationcp.breeding.manager.germplasm.GermplasmNamesAttributesModel;
import org.generationcp.breeding.manager.germplasm.GermplasmQueries;
import org.generationcp.breeding.manager.germplasm.GermplasmSearchResultModel;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.report.LotReportRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public final class GermplasmIndexContainer {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmIndexContainer.class);

	// Germplasm SearchResult Model
	private static final Object GERMPLASM_GID = "gid";
	private static final Object GERMPLASM_NAMES = "names";
	private static final Object GERMPLASM_METHOD = "method";
	private static final Object GERMPLASM_LOCATION = "location";

	// GermplasmNamesAttribute Model
	private static final Object GERMPLASM_NAMES_ATTRIBUTE_TYPE = "type";
	private static final Object GERMPLASM_NAMES_ATTRIBUTE_NAME = "name";
	private static final Object GERMPLASM_NAMES_ATTRIBUTE_DATE = "date";
	private static final Object GERMPLASM_NAMES_ATTRIBUTE_LOCATION = "location";
	private static final Object GERMPLASM_NAMES_ATTRIBUTE_TYPE_DESC = "typedesc";

	// Germplasm Inventory Model
	private static final Object GERMPLASM_INVENTORY_ACTUAL_LOT_BALANCE = "lotbalance";
	private static final Object GERMPLASM_INVENTORY_LOCATION_NAME = "location";
	private static final Object GERMPLASM_INVENTORY_SCALE_NAME = "scale";
	private static final Object GERMPLASM_INVENTORY_LOT_COMMENT = "lotcomment";

	// Study Information Model
	public static final String STUDY_ID = "studyid";
	public static final String STUDY_NAME = "studyname";
	public static final String STUDY_DESCRIPTION = "description";

	private static final Object GERMPLASM_PREFNAME = "prefname";

	private final GermplasmQueries qQuery;

	public GermplasmIndexContainer(GermplasmQueries qQuery) {
		this.qQuery = qQuery;
	}

	public IndexedContainer getGermplasmResultContainer(String choice, String searchValue) {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties - Germplasm Search Result
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_GID, Integer.class, 0);
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_NAMES, String.class, "");
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_METHOD, String.class, "");
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_LOCATION, String.class, "");

		List<GermplasmSearchResultModel> queryByNames = null;
		GermplasmSearchResultModel queryByGid = null;
		if (choice.equals(GermplasmQueries.SEARCH_OPTION_NAME)) {
			queryByNames = this.qQuery.getGermplasmListResultByPrefStandardizedName(searchValue);
			for (GermplasmSearchResultModel q : queryByNames) {
				GermplasmIndexContainer.addGermplasmResultContainer(container, q.getGid(), q.getNames(), q.getMethod(), q.getLocation());
			}
		} else {
			queryByGid = this.qQuery.getGermplasmResultByGID(searchValue);
			if (queryByGid != null) {
				GermplasmIndexContainer.addGermplasmResultContainer(container, queryByGid.getGid(), queryByGid.getNames(),
						queryByGid.getMethod(), queryByGid.getLocation());
			}
		}

		return container;
	}

	public LazyQueryContainer getGermplasmResultLazyContainer(GermplasmDataManager germplasmDataManager, String searchChoice,
			String searchValue) {

		GermplasmSearchQueryFactory factory = new GermplasmSearchQueryFactory(germplasmDataManager, searchChoice, searchValue);
		LazyQueryContainer container = new LazyQueryContainer(factory, false, 10);

		// add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
		container.addContainerProperty(GermplasmSearchQuery.GID, String.class, null);
		container.addContainerProperty(GermplasmSearchQuery.NAMES, String.class, null);
		container.addContainerProperty(GermplasmSearchQuery.METHOD, String.class, null);
		container.addContainerProperty(GermplasmSearchQuery.LOCATION, String.class, null);

		// initialize the first batch of data to be displayed
		container.getQueryView().getItem(0);
		return container;
	}

	public LazyQueryContainer getGermplasmEnvironmentResultLazyContainer(CrossStudyDataManager crossStudyDataManager, String searchChoice,
			String searchValue, List<Integer> environmentIds) {

		GermplasmEnvironmentSearchQueryFactory factory =
				new GermplasmEnvironmentSearchQueryFactory(crossStudyDataManager, searchChoice, searchValue, environmentIds);
		LazyQueryContainer container = new LazyQueryContainer(factory, false, 10);

		// add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
		container.addContainerProperty(GermplasmEnvironmentSearchQuery.GID, String.class, null);
		container.addContainerProperty(GermplasmEnvironmentSearchQuery.NAMES, String.class, null);
		container.addContainerProperty(GermplasmEnvironmentSearchQuery.LOCATION, String.class, null);

		// initialize the first batch of data to be displayed
		container.getQueryView().getItem(0);
		return container;
	}

	private static void addGermplasmResultContainer(Container container, int gid, String names, String method, String location) {
		Object itemId = container.addItem();
		Item item = container.getItem(itemId);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_GID).setValue(gid);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_NAMES).setValue(names);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_METHOD).setValue(method);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_LOCATION).setValue(location);
	}

	public IndexedContainer getGermplasmAttribute(GermplasmDetailModel g) {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties
		this.addContainerProperties(container);

		final List<GermplasmNamesAttributesModel> query = this.qQuery.getAttributes(g.getGid());
		GermplasmIndexContainer.LOG.info("Size of the query" + query.size());
		for (GermplasmNamesAttributesModel q : query) {
			GermplasmIndexContainer.addGermplasmNamesAttributeContainer(container, q.getType(), q.getTypeDesc(), q.getName(), q.getDate(),
					q.getLocation());
		}
		return container;
	}

	public IndexedContainer getGermplasmNames(GermplasmDetailModel g) {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties
		this.addContainerProperties(container);

		final List<GermplasmNamesAttributesModel> query = this.qQuery.getNames(Integer.valueOf(g.getGid()));
		for (GermplasmNamesAttributesModel q : query) {
			GermplasmIndexContainer.addGermplasmNamesAttributeContainer(container, q.getName(), q.getDate(), q.getLocation(), q.getType(),
					q.getTypeDesc());
		}
		return container;
	}

	private void addContainerProperties(Container container) {
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_NAMES_ATTRIBUTE_TYPE, String.class, "");
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_NAMES_ATTRIBUTE_NAME, String.class, "");
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_NAMES_ATTRIBUTE_DATE, String.class, "");
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_NAMES_ATTRIBUTE_LOCATION, String.class, "");
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_NAMES_ATTRIBUTE_TYPE_DESC, String.class, "");
	}

	private static void addGermplasmNamesAttributeContainer(Container container, String type, String name, String date, String location,
			String typeDesc) {
		Object itemId = container.addItem();
		Item item = container.getItem(itemId);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_NAMES_ATTRIBUTE_TYPE).setValue(type);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_NAMES_ATTRIBUTE_NAME).setValue(name);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_NAMES_ATTRIBUTE_DATE).setValue(date);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_NAMES_ATTRIBUTE_LOCATION).setValue(location);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_NAMES_ATTRIBUTE_TYPE_DESC).setValue(typeDesc);

	}

	public IndexedContainer getGermplasmGenerationHistory(GermplasmDetailModel gModel) {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_GID, Integer.class, 0);
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_PREFNAME, String.class, "");

		final List<GermplasmDetailModel> query = this.qQuery.getGenerationHistory(Integer.valueOf(gModel.getGid()));
		for (GermplasmDetailModel g : query) {
			GermplasmIndexContainer.addGermplasmGenerationHistory(container, g.getGid(), g.getGermplasmPreferredName());
		}
		return container;
	}

	private static void addGermplasmGenerationHistory(Container container, int gid, String prefname) {
		Object itemId = container.addItem();
		Item item = container.getItem(itemId);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_GID).setValue(gid);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_PREFNAME).setValue(prefname);
	}

	public IndexedContainer getReportOnLots(GermplasmDetailModel g) {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_INVENTORY_ACTUAL_LOT_BALANCE, String.class, "");
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_INVENTORY_LOCATION_NAME, String.class, "");
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_INVENTORY_SCALE_NAME, String.class, "");
		container.addContainerProperty(GermplasmIndexContainer.GERMPLASM_INVENTORY_LOT_COMMENT, String.class, "");

		final List<LotReportRow> lotReportRowData =
				this.qQuery.getReportOnLotsByEntityTypeAndEntityId("GERMPLSM", Integer.valueOf(g.getGid()));

		for (LotReportRow lotReportRow : lotReportRowData) {
			GermplasmIndexContainer.addLotReportRowContainer(container, String.valueOf(lotReportRow.getActualLotBalance()),
					lotReportRow.getLocationOfLot() == null ? null : lotReportRow.getLocationOfLot().getLname(),
					lotReportRow.getScaleOfLot() == null ? null : lotReportRow.getScaleOfLot().getName(), lotReportRow.getCommentOfLot());
		}

		return container;
	}

	private static void addLotReportRowContainer(Container container, String lotBalance, String locationName, String scaleName,
			String lotComment) {
		Object itemId = container.addItem();
		Item item = container.getItem(itemId);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_INVENTORY_ACTUAL_LOT_BALANCE).setValue(lotBalance);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_INVENTORY_LOCATION_NAME).setValue(locationName);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_INVENTORY_SCALE_NAME).setValue(scaleName);
		item.getItemProperty(GermplasmIndexContainer.GERMPLASM_INVENTORY_LOT_COMMENT).setValue(lotComment);
	}

	public IndexedContainer getGermplasmStudyInformation(GermplasmDetailModel gModel) {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties
		container.addContainerProperty(GermplasmIndexContainer.STUDY_ID, Integer.class, 0);
		container.addContainerProperty(GermplasmIndexContainer.STUDY_NAME, String.class, "");
		container.addContainerProperty(GermplasmIndexContainer.STUDY_DESCRIPTION, String.class, "");

		final List<StudyReference> studies = this.qQuery.getGermplasmStudyInfo(Integer.valueOf(gModel.getGid()));
		for (StudyReference study : studies) {
			GermplasmIndexContainer.addGermplasmStudyInformation(container, study);
		}
		return container;
	}

	private static void addGermplasmStudyInformation(Container container, StudyReference study) {
		Object itemId = container.addItem();
		Item item = container.getItem(itemId);
		item.getItemProperty(GermplasmIndexContainer.STUDY_ID).setValue(study.getId());
		item.getItemProperty(GermplasmIndexContainer.STUDY_NAME).setValue(study.getName());
		item.getItemProperty(GermplasmIndexContainer.STUDY_DESCRIPTION).setValue(study.getDescription());
	}

}
