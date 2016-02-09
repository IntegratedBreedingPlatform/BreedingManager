
package org.generationcp.breeding.manager.listmanager.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import net.sf.jasperreports.engine.JRException;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.pojo.GermplasmParents;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.reports.BuildReportException;
import org.generationcp.middleware.reports.Reporter;
import org.generationcp.middleware.service.api.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class GermplasmListExporter {

	private static final String FEMALE_PARENT = "FEMALE PARENT";

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
	private OntologyMethodDataManager ontologyMethodDataManager;

	@Autowired
	private OntologyPropertyDataManager ontologyPropertyDataManager;

	@Autowired
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private ReportService reportService;

	@Resource
	private GermplasmExportService germplasmExportService;

	private final Integer listId;

	public GermplasmListExporter(Integer germplasmListId) {
		this.listId = germplasmListId;
	}

	public FileOutputStream exportKBioScienceGenotypingOrderXLS(String filename, int plateSize) throws GermplasmListExporterException {

		List<ExportColumnHeader> exportColumnHeaders = this.getColumnHeadersForGenotypingData(plateSize);
		List<Map<Integer, ExportColumnValue>> exportColumnValues = this.getColumnValuesForGenotypingData(plateSize);

		try {
			return this.germplasmExportService.generateExcelFileForSingleSheet(exportColumnValues, exportColumnHeaders, filename, "List");
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

	public Reporter exportGermplasmListCustomReport(String fileName, String reportCode) throws GermplasmListExporterException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final Reporter customReport =
					this.reportService.getStreamGermplasmListReport(reportCode, this.listId, contextUtil.getProjectInContext()
							.getProjectName(), baos);
			final File createdFile = new File(fileName);
			baos.writeTo(new FileOutputStream(createdFile));
			return customReport;
		} catch (JRException | IOException | BuildReportException e) {
			throw new GermplasmListExporterException("Error with exporting using a custom report", e);
		}
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

		input.setColumnTermMap(this.getOntologyTermMap(listDataTable));

		input.setInventoryVariableMap(this.getInventoryVariables());

		input.setVariateVariableMap(this.getVariateVariables());

		input.setGermplasmParents(this.getGermplasmParentsMap(listDataTable, this.listId));

		return this.germplasmExportService.generateGermplasmListExcelFile(input);
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

	protected String getExporterName(final Integer currentLocalIbdbUserId) throws GermplasmListExporterException {
		if (currentLocalIbdbUserId == null) {
			throw new IllegalArgumentException("User id could not be null");
		}
		final String exporterName;
		try {
			final User exporterUser = this.userDataManager.getUserById(currentLocalIbdbUserId);
			if (exporterUser == null) {
				throw new GermplasmListExporterException("Could not retrieve the exporter name from the database");
			}
			final Person exporterPerson = this.userDataManager.getPersonById(exporterUser.getPersonid());
			if (exporterPerson == null) {
				throw new GermplasmListExporterException("Could not retrieve the exporter name from the database");
			}
			exporterName = exporterPerson.getFirstName() + " " + exporterPerson.getLastName();
		} catch (final MiddlewareQueryException e) {
			throw new GermplasmListExporterException("Error with getting current workbench user information.", e);
		}
		return exporterName;
	}

	protected String getOwnerName(final Integer userId) throws GermplasmListExporterException {
		// retrieve user details
		if (userId == null) {
			throw new IllegalArgumentException("User id could not be null");
		}
		final String ownerName;
		try {
			final User ownerUser = this.userDataManager.getUserById(userId);
			if (ownerUser == null) {
				throw new GermplasmListExporterException("Could not retrieve the owner name from the database");
			}
			final Person ownerPerson = this.userDataManager.getPersonById(ownerUser.getPersonid());
			if (ownerPerson != null) {
				ownerName = ownerPerson.getFirstName() + " " + ownerPerson.getLastName();
			} else {
				ownerName = ownerUser.getName();
			}
		} catch (final MiddlewareQueryException e) {
			throw new GermplasmListExporterException("Error with getting user information.", e);
		}
		return ownerName;
	}

	protected GermplasmList getGermplasmListAndListData(Integer listId) throws GermplasmListExporterException {
		GermplasmList germplasmList;
		try {
			germplasmList = this.germplasmListManager.getGermplasmListById(listId);
			this.inventoryDataManager.populateLotCountsIntoExistingList(germplasmList);
		} catch (MiddlewareQueryException e) {
			throw new GermplasmListExporterException("Error with getting Germplasm List with id: " + listId, e);
		}
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

	protected Map<Integer, Term> getOntologyTermMap(Table listDataTable) {

		Map<Integer, Term> columnTermMap = new HashMap<>();
		Collection<?> columnHeaders = listDataTable.getContainerPropertyIds();

		for (Object column : columnHeaders) {
			String columnHeader = column.toString();
			ColumnLabels columnLabel = ColumnLabels.get(columnHeader);
			if (columnLabel != null && columnLabel.getTermId() != null) {
				this.addOntologyTermToMap(columnTermMap, columnLabel.getTermId().getId());
			}
		}

		return columnTermMap;
	}

	protected Map<Integer, Variable> getInventoryVariables() {

		Map<Integer, Variable> variableMap = new HashMap<>();
		this.addVariableToMap(variableMap, TermId.SEED_AMOUNT_G.getId());
		this.addVariableToMap(variableMap, TermId.STOCKID.getId());
		return variableMap;
	}

	protected Map<Integer, Variable> getVariateVariables() {

		Map<Integer, Variable> variableMap = new HashMap<>();
		this.addVariableToMap(variableMap, TermId.NOTES.getId());
		return variableMap;

	}

	private void addVariableToMap(Map<Integer, Variable> variableMap, int termId) {

		try {
			Variable variable =
					this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), termId, false, false);
			if (variable != null) {
				variableMap.put(variable.getId(), variable);
			}

		} catch (MiddlewareQueryException e) {
			GermplasmListExporter.LOG.error(e.getMessage(), e);
		}
	}

	private void addOntologyTermToMap(Map<Integer, Term> termMap, int termId) {

		try {
			// Term should exist with that id in database.
			Term term = this.ontologyDataManager.getTermById(termId);

			GermplasmListExporter.LOG.debug("Finding term with id:" + termId + ". Found: " + (term != null));

			if (term == null) {
				throw new MiddlewareException("Term does not exist with id:" + termId);
			}

			CvId cvId = CvId.valueOf(term.getVocabularyId());

			if (Objects.equals(cvId, CvId.IBDB_TERMS)) {
				termMap.put(term.getId(), term);
			} else if (Objects.equals(cvId, CvId.METHODS)) {
				termMap.put(term.getId(), this.ontologyMethodDataManager.getMethod(term.getId(), false));
			} else if (Objects.equals(cvId, CvId.PROPERTIES)) {
				termMap.put(term.getId(), this.ontologyPropertyDataManager.getProperty(term.getId(), false));
			} else if (Objects.equals(cvId, CvId.SCALES)) {
				termMap.put(term.getId(), this.ontologyScaleDataManager.getScaleById(term.getId(), false));
			} else {
				termMap.put(term.getId(),
						this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), term.getId(), false, false));
			}
		} catch (MiddlewareQueryException e) {
			GermplasmListExporter.LOG.error(e.getMessage(), e);
		}
	}

	public void exportGermplasmListCSV(String fileName, Table listDataTable) throws GermplasmListExporterException {

		List<Map<Integer, ExportColumnValue>> exportColumnValues = this.getExportColumnValuesFromTable(listDataTable);
		List<ExportColumnHeader> exportColumnHeaders = this.getExportColumnHeadersFromTable(listDataTable);

		try {

			this.germplasmExportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName);

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

	protected void setGermplasmExportService(GermplasmExportService germplasmExportService) {
		this.germplasmExportService = germplasmExportService;
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

	protected void setInventoryDataManager(InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

	protected void setOntologyVariableDataManager(OntologyVariableDataManager ontologyVariableDataManager) {
		this.ontologyVariableDataManager = ontologyVariableDataManager;
	}

	protected String getTermNameFromOntology(ColumnLabels columnLabel) {
		return columnLabel.getTermNameFromOntology(this.ontologyDataManager);
	}
}
