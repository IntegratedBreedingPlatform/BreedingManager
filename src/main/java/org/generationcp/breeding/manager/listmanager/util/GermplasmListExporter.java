
package org.generationcp.breeding.manager.listmanager.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.pojo.GermplasmParents;
import org.generationcp.commons.service.ExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

@Configurable
public class GermplasmListExporter {

	private static final String FEMALE_PARENT = "FEMALE PARENT";

	public static final String PROGRAM_UUID = UUID.randomUUID().toString();

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListExporter.class);

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private UserDataManager userDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private ExportService exportService;

	private final Integer listId;

	public GermplasmListExporter(Integer germplasmListId) {
		this.listId = germplasmListId;
	}

	public FileOutputStream exportKBioScienceGenotypingOrderXLS(String filename, int plateSize) throws GermplasmListExporterException {

		List<ExportColumnHeader> exportColumnHeaders = this.getColumnHeadersForGenotypingData(plateSize);
		List<Map<Integer, ExportColumnValue>> exportColumnValues = this.getColumnValuesForGenotypingData(plateSize);

		try {
			return this.exportService.generateExcelFileForSingleSheet(exportColumnValues, exportColumnHeaders, filename, "List");
		} catch (IOException e) {
			throw new GermplasmListExporterException("Error with writing to: " + filename, e);
		}
	}

	protected List<ExportColumnHeader> getColumnHeadersForGenotypingData(int plateSize) {
		// generate columns headers
		List<ExportColumnHeader> exportColumnHeaders = new ArrayList<ExportColumnHeader>();
		exportColumnHeaders.add(new ExportColumnHeader(0, "Subject ID", true));
		exportColumnHeaders.add(new ExportColumnHeader(1, "Plate ID", true));
		exportColumnHeaders.add(new ExportColumnHeader(2, "Well", true));
		exportColumnHeaders.add(new ExportColumnHeader(3, "Sample type", true));
		exportColumnHeaders.add(new ExportColumnHeader(4, String.valueOf(plateSize), true));
		exportColumnHeaders.add(new ExportColumnHeader(5, "Primer", true));
		exportColumnHeaders.add(new ExportColumnHeader(6, "Subject BC", true));
		exportColumnHeaders.add(new ExportColumnHeader(7, "Plate BC", true));
		return exportColumnHeaders;
	}

	protected List<Map<Integer, ExportColumnValue>> getColumnValuesForGenotypingData(int plateSize) throws GermplasmListExporterException {

		List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<Map<Integer, ExportColumnValue>>();

		GermplasmList germplasmList = this.getGermplasmListAndListData(this.listId);
		String listName = germplasmList.getName();

		List<GermplasmListData> listDatas = germplasmList.getListData();

		String plateName = listName;
		int plateNum = 0;
		if (plateSize == 96 && listDatas.size() > 95) {
			plateNum = 1;
			plateName = plateName + "-" + plateNum;
		}

		String[] wellLetters = {"A", "B", "C", "D", "E", "F", "G", "H"};
		int wellLetterIndex = 0;
		int wellNumberIndex = 1;
		for (GermplasmListData listData : listDatas) {
			if (wellLetterIndex == 7 && wellNumberIndex == 12) {
				// skip H12
				wellLetterIndex = 0;
				wellNumberIndex = 1;
				if (plateNum != 0) {
					plateNum++;
					plateName = listName + "-" + plateNum;
				}
			}

			if (wellNumberIndex == 13) {
				wellLetterIndex++;
				wellNumberIndex = 1;
			}

			String well = wellLetters[wellLetterIndex];
			if (wellNumberIndex < 10) {
				well = well + "0" + wellNumberIndex;
			} else {
				well = well + wellNumberIndex;
			}

			Map<Integer, ExportColumnValue> exportRowValue = new HashMap<Integer, ExportColumnValue>();
			exportRowValue.put(0, new ExportColumnValue(0, listData.getEntryId().toString()));
			exportRowValue.put(1, new ExportColumnValue(1, plateName));
			exportRowValue.put(2, new ExportColumnValue(2, well));
			exportRowValue.put(3, new ExportColumnValue(3, null));
			exportRowValue.put(4, new ExportColumnValue(4, null));
			exportRowValue.put(5, new ExportColumnValue(5, null));
			exportRowValue.put(6, new ExportColumnValue(6, null));
			exportRowValue.put(7, new ExportColumnValue(7, null));

			exportColumnValues.add(exportRowValue);

			wellNumberIndex++;
		}
		return exportColumnValues;
	}

	public FileOutputStream exportGermplasmListXLS(String fileName, Table listDataTable) throws GermplasmListExporterException {

		Integer currentLocalIbdbUserId = this.getCurrentLocalIbdbUserId();

		GermplasmListExportInputValues input = new GermplasmListExportInputValues();
		input.setFileName(fileName);

		GermplasmList germplasmList = this.getGermplasmListAndListData(this.listId);

		input.setGermplasmList(germplasmList);

		input.setListData(germplasmList.getListData());

		input.setOwnerName(this.getOwnerName(germplasmList.getUserId()));

		input.setCurrentLocalIbdbUserId(currentLocalIbdbUserId);

		input.setExporterName(this.getExporterName(currentLocalIbdbUserId));

		input.setVisibleColumnMap(this.getVisibleColumnMap(listDataTable));

		input.setColumnStandardVariableMap(this.getGermplasmStandardVariableMap(listDataTable));

		input.setInventoryStandardVariableMap(this.getInventoryStandardVariables());

		input.setVariateStandardVariableMap(this.getVariateStandardVariables());

		input.setGermplasmParents(this.getGermplasmParentsMap(listDataTable, this.listId));

		return this.exportService.generateGermplasmListExcelFile(input);
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, GermplasmParents> getGermplasmParentsMap(Table listDataTable, Integer listId) {
		Map<Integer, GermplasmParents> germplasmParentsMap = new HashMap<Integer, GermplasmParents>();

		List<Integer> itemIds = new ArrayList<Integer>();
		itemIds.addAll((Collection<? extends Integer>) listDataTable.getItemIds());

		if (this.hasParentsColumn(listDataTable)) {
			for (Integer itemId : itemIds) {
				Button femaleParentButton =
						(Button) listDataTable.getItem(itemId).getItemProperty(ColumnLabels.FEMALE_PARENT.getName()).getValue();
				String femaleParentName = femaleParentButton.getCaption();

				Button maleParentButton =
						(Button) listDataTable.getItem(itemId).getItemProperty(ColumnLabels.MALE_PARENT.getName()).getValue();
				String maleParentName = maleParentButton.getCaption();

				Button fgidButton = (Button) listDataTable.getItem(itemId).getItemProperty(ColumnLabels.FGID.getName()).getValue();
				Integer fgid = Integer.valueOf(fgidButton.getCaption());

				Button mgidButton = (Button) listDataTable.getItem(itemId).getItemProperty(ColumnLabels.MGID.getName()).getValue();
				Integer mgid = Integer.valueOf(mgidButton.getCaption());

				Button gidButton = (Button) listDataTable.getItem(itemId).getItemProperty(ColumnLabels.GID.getName()).getValue();
				Integer gid = Integer.valueOf(gidButton.getCaption());

				germplasmParentsMap.put(gid, new GermplasmParents(gid, femaleParentName, maleParentName, fgid, mgid));
			}
		}

		return germplasmParentsMap;
	}

	protected boolean hasParentsColumn(Table listDataTable) {
		String[] columnHeaders = listDataTable.getColumnHeaders();

		for (int i = 0; i < columnHeaders.length; i++) {
			// only checks if the existence of the female parent to determine if the export came from crossing manager
			if (columnHeaders[i].equals(GermplasmListExporter.FEMALE_PARENT)) {
				return true;
			}
		}

		return false;
	}

	protected String getExporterName(Integer currentLocalIbdbUserId) throws GermplasmListExporterException {
		String exporterName = "";
		try {
			User exporterUser = this.userDataManager.getUserById(currentLocalIbdbUserId);
			Person exporterPerson = this.userDataManager.getPersonById(exporterUser.getPersonid());
			exporterName = exporterPerson.getFirstName() + " " + exporterPerson.getLastName();
		} catch (MiddlewareQueryException e) {
			throw new GermplasmListExporterException("Error with getting current workbench user information.", e);
		} catch (NullPointerException ex) {
			GermplasmListExporter.LOG.error("Error with getting user information for exporter with id = " + currentLocalIbdbUserId, ex);
		}
		return exporterName;
	}

	protected String getOwnerName(Integer userId) throws GermplasmListExporterException {
		// retrieve user details
		String ownerName = "";
		try {
			User ownerUser = this.userDataManager.getUserById(userId);
			Person ownerPerson = this.userDataManager.getPersonById(ownerUser.getPersonid());
			if (ownerPerson != null) {
				ownerName = ownerPerson.getFirstName() + " " + ownerPerson.getLastName();
			} else {
				ownerName = ownerUser.getName();
			}
		} catch (MiddlewareQueryException e) {
			throw new GermplasmListExporterException("Error with getting user information.", e);
		} catch (NullPointerException ex) {
			GermplasmListExporter.LOG.error("Error with getting user information for list owner with id = " + userId, ex);
		}
		return ownerName;
	}

	protected GermplasmList getGermplasmListAndListData(Integer listId) throws GermplasmListExporterException {
		GermplasmList germplasmList;
		// set germplasmList and germplasmListData
		try {
			germplasmList = this.germplasmListManager.getGermplasmListById(listId);
		} catch (MiddlewareQueryException e) {
			throw new GermplasmListExporterException("Error with getting Germplasm List with id: " + listId, e);
		}

		List<GermplasmListData> germplasmlistData = new ArrayList<GermplasmListData>();
		try {
			long listDataCount = this.germplasmListManager.countGermplasmListDataByListId(listId);
			germplasmlistData = this.inventoryDataManager.getLotCountsForList(listId, 0, (int) listDataCount);
		} catch (MiddlewareQueryException e1) {
			GermplasmListExporter.LOG.error(e1.getMessage(), e1);
		}
		germplasmList.setListData(germplasmlistData);

		return germplasmList;
	}

	protected Map<String, Boolean> getVisibleColumnMap(Table listDataTable) {

		Map<String, Boolean> columnHeaderMap = new HashMap<String, Boolean>();

		Collection<?> columnHeaders = listDataTable.getContainerPropertyIds();
		Object[] visibleColumns = listDataTable.getVisibleColumns();

		// change the visibleColumns array to list
		List<String> visibleColumnList = new ArrayList<String>();
		for (Object column : visibleColumns) {
			if (!listDataTable.isColumnCollapsed(column)) {
				visibleColumnList.add(column.toString());
			}
		}

		for (Object column : columnHeaders) {
			String key = column.toString();
			ColumnLabels columnLabel = ColumnLabels.get(column.toString());
			if (columnLabel != null && columnLabel.getTermId() != null) {
				key = String.valueOf(columnLabel.getTermId().getId());
			}

			// always set to true for required columns
			if (ColumnLabels.ENTRY_ID.getName().equalsIgnoreCase(column.toString())
					|| ColumnLabels.GID.getName().equalsIgnoreCase(column.toString())
					|| ColumnLabels.DESIGNATION.getName().equalsIgnoreCase(column.toString())) {
				columnHeaderMap.put(key, true);
			} else {
				columnHeaderMap.put(key, visibleColumnList.contains(column.toString()));
			}

		}

		return columnHeaderMap;
	}

	protected Map<Integer, StandardVariable> getGermplasmStandardVariableMap(Table listDataTable) {

		Map<Integer, StandardVariable> columnStandardVariableMap = new HashMap<>();
		Collection<?> columnHeaders = listDataTable.getContainerPropertyIds();

		for (Object column : columnHeaders) {
			String columnHeader = column.toString();
			ColumnLabels columnLabel = ColumnLabels.get(columnHeader);
			if (columnLabel != null && columnLabel.getTermId() != null) {
				this.addStandardVariableToMap(columnStandardVariableMap, columnLabel.getTermId().getId());
			}
		}

		return columnStandardVariableMap;
	}

	protected Map<Integer, StandardVariable> getInventoryStandardVariables() {

		Map<Integer, StandardVariable> standardVariableMap = new HashMap<>();
		this.addStandardVariableToMap(standardVariableMap, TermId.SEED_AMOUNT_G.getId());
		this.addStandardVariableToMap(standardVariableMap, TermId.STOCKID.getId());
		return standardVariableMap;
	}

	protected Map<Integer, StandardVariable> getVariateStandardVariables() {

		Map<Integer, StandardVariable> standardVariableMap = new HashMap<>();
		this.addStandardVariableToMap(standardVariableMap, TermId.NOTES.getId());
		return standardVariableMap;

	}

	private void addStandardVariableToMap(Map<Integer, StandardVariable> standardVariableMap, int termId) {

		try {
			StandardVariable standardVar = this.ontologyDataManager.getStandardVariable(termId, PROGRAM_UUID);
			if (standardVar != null) {
				standardVariableMap.put(standardVar.getId(), standardVar);
			}

		} catch (MiddlewareQueryException e) {
			GermplasmListExporter.LOG.error(e.getMessage(), e);
		}
	}

	public void exportGermplasmListCSV(String fileName, Table listDataTable) throws GermplasmListExporterException {

		List<Map<Integer, ExportColumnValue>> exportColumnValues = this.getExportColumnValuesFromTable(listDataTable);
		List<ExportColumnHeader> exportColumnHeaders = this.getExportColumnHeadersFromTable(listDataTable);

		try {

			this.exportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName);

		} catch (IOException e) {
			throw new GermplasmListExporterException("Error with exporting list to CSV File.", e);
		}

	}

	protected List<ExportColumnHeader> getExportColumnHeadersFromTable(Table listDataTable) {

		Map<String, Boolean> visibleColumns = this.getVisibleColumnMap(listDataTable);

		List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();

		exportColumnHeaders.add(new ExportColumnHeader(0, this.getTermNameFromOntology(ColumnLabels.ENTRY_ID), visibleColumns.get(String
				.valueOf(ColumnLabels.ENTRY_ID.getTermId().getId()))));

		exportColumnHeaders.add(new ExportColumnHeader(1, this.getTermNameFromOntology(ColumnLabels.GID), visibleColumns.get(String
				.valueOf(ColumnLabels.GID.getTermId().getId()))));

		exportColumnHeaders.add(new ExportColumnHeader(2, this.getTermNameFromOntology(ColumnLabels.ENTRY_CODE), visibleColumns.get(String
				.valueOf(ColumnLabels.ENTRY_CODE.getTermId().getId()))));

		exportColumnHeaders.add(new ExportColumnHeader(3, this.getTermNameFromOntology(ColumnLabels.DESIGNATION), visibleColumns.get(String
				.valueOf(ColumnLabels.DESIGNATION.getTermId().getId()))));

		exportColumnHeaders.add(new ExportColumnHeader(4, this.getTermNameFromOntology(ColumnLabels.PARENTAGE), visibleColumns.get(String
				.valueOf(ColumnLabels.PARENTAGE.getTermId().getId()))));

		exportColumnHeaders.add(new ExportColumnHeader(5, this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE), visibleColumns.get(String
				.valueOf(ColumnLabels.SEED_SOURCE.getTermId().getId()))));

		return exportColumnHeaders;
	}

	protected List<Map<Integer, ExportColumnValue>> getExportColumnValuesFromTable(Table listDataTable) {

		List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<>();

		for (Object itemId : listDataTable.getItemIds()) {
			Map<Integer, ExportColumnValue> row = new HashMap<>();

			String entryIdValue = listDataTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue().toString();
			String gidValue = ((Button) listDataTable.getItem(itemId).getItemProperty(ColumnLabels.GID.getName()).getValue()).getCaption();
			String entryCodeValue = listDataTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_CODE.getName()).getValue().toString();
			String designationValue =
					((Button) listDataTable.getItem(itemId).getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue()).getCaption();
			String parentageValue = listDataTable.getItem(itemId).getItemProperty(ColumnLabels.PARENTAGE.getName()).getValue().toString();
			String seedSourceValue =
					listDataTable.getItem(itemId).getItemProperty(ColumnLabels.SEED_SOURCE.getName()).getValue().toString();

			row.put(0, new ExportColumnValue(0, entryIdValue));
			row.put(1, new ExportColumnValue(1, gidValue));
			row.put(2, new ExportColumnValue(2, entryCodeValue));
			row.put(3, new ExportColumnValue(3, designationValue));
			row.put(4, new ExportColumnValue(4, parentageValue));
			row.put(5, new ExportColumnValue(5, seedSourceValue));

			exportColumnValues.add(row);
		}

		return exportColumnValues;
	}

	protected Integer getCurrentLocalIbdbUserId() {
		Integer currentLocalIbdbUserId = 0;

		try {
			currentLocalIbdbUserId = this.contextUtil.getCurrentUserLocalId();
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
		return currentLocalIbdbUserId;
	}

	protected void setExportService(ExportService exportService) {
		this.exportService = exportService;
	}

	protected void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	protected void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	protected void setUserDataManager(UserDataManager userDataManager) {
		this.userDataManager = userDataManager;
	}

	protected void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	protected String getTermNameFromOntology(ColumnLabels columnLabel) {
		return columnLabel.getTermNameFromOntology(this.ontologyDataManager);
	}
}
