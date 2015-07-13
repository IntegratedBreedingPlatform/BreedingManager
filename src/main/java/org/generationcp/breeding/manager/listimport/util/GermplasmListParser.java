
package org.generationcp.breeding.manager.listimport.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.listimport.validator.StockIDValidator;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.parsing.AbstractExcelFileParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.WorkbookRowConverter;
import org.generationcp.commons.parsing.pojo.ImportedCondition;
import org.generationcp.commons.parsing.pojo.ImportedConstant;
import org.generationcp.commons.parsing.pojo.ImportedFactor;
import org.generationcp.commons.parsing.pojo.ImportedVariate;
import org.generationcp.commons.parsing.validation.NonEmptyValidator;
import org.generationcp.commons.parsing.validation.ParseValidationMap;
import org.generationcp.commons.parsing.validation.ValueTypeValidator;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Class for parsing GermplsmList
 */
@Configurable
public class GermplasmListParser extends AbstractExcelFileParser<ImportedGermplasmList> {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListParser.class);

	public static final int DESCRIPTION_SHEET_COL_SIZE = 8;
	public static final int DESCRIPTION_SHEET_NO = 0;
	public static final int CONDITION_HEADER_ROW_INDEX = 5;
	public static final String TEMPLATE_LIST_TYPE = "LST";
	public static final String LIST_DATE = "LIST DATE";
	public static final String LIST_TYPE = "LIST TYPE";
	private static final int OBSERVATION_SHEET_NO = 1;

	@Resource
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private OntologyDataManager ontologyDataManager;

	private int currentRowIndex = 0;
	private final Map<Integer, String> observationColumnMap = new HashMap<>();

	private ImportedGermplasmList importedGermplasmList;
	private Map<FactorTypes, String> specialFactors;

	private String noInventoryWarning = "";
	private String noVariatesWarning = "";
	private boolean importFileIsAdvanced = false;
	private String seedAmountVariate = "";
	private Set<String> nameFactors;
	private Set<String> attributeVariates;

	public String getNoInventoryWarning() {
		return this.noInventoryWarning;
	}

	public String getNoVariatesWarning() {
	    return this.noVariatesWarning;
	}
	
	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public boolean hasInventoryAmountOnly() {
		return !this.seedAmountVariate.isEmpty()
				&& (!this.specialFactors.containsKey(FactorTypes.STOCK) || !this.importedGermplasmList.isHasStockIDValues());
	}

	public boolean hasInventoryAmount() {
		return !this.seedAmountVariate.isEmpty();
	}

	public boolean hasStockIdFactor() {
		return this.specialFactors.containsKey(FactorTypes.STOCK);
	}

	public boolean isSeedAmountVariable(ImportedVariate variate) {
		return !this.seedAmountVariate.isEmpty() && this.seedAmountVariate.equals(variate.getVariate());
	}

	public boolean importFileIsAdvanced() {
		return this.importFileIsAdvanced;
	}

	enum ConditionHeaders {
		CONDITION("CONDITION"), DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD");

		String label;

		ConditionHeaders(String label) {
			this.label = label;
		}

		public static String[] names() {
			ConditionHeaders[] values = ConditionHeaders.values();
			String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum FactorHeaders {
		FACTOR("FACTOR"), DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD");

		String label;

		FactorHeaders(String label) {
			this.label = label;
		}

		public static String[] names() {
			FactorHeaders[] values = FactorHeaders.values();
			String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum ConstantHeaders {
		CONSTANT("CONSTANT"), DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD");

		String label;

		ConstantHeaders(String label) {
			this.label = label;
		}

		public static String[] names() {
			ConstantHeaders[] values = ConstantHeaders.values();
			String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum VariateHeaders {
		VARIATE("VARIATE"), DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD");

		String label;

		VariateHeaders(String label) {
			this.label = label;
		}

		public static String[] names() {
			VariateHeaders[] values = VariateHeaders.values();
			String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum FactorTypes {
		ENTRY, DESIG, GID, ENTRYCODE, SOURCE, CROSS, NAME, STOCK
	}

	@Override
	public ImportedGermplasmList parseWorkbook(Workbook workbook, Map<String, Object> additionalParams) throws FileParsingException {
		this.workbook = workbook;

		this.parseListDetails();
		this.parseConditions();
		this.parseFactors();
		this.parseConstants();
		this.parseVariates();
		this.parseObservationRows();
		return this.importedGermplasmList;
	}

	protected void parseVariates() throws FileParsingException {
		// variate is optional so lets check first if its there
		if (!"VARIATE".equalsIgnoreCase(this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, this.currentRowIndex, 0))) {
			return;
		}

		if (this.isHeaderInvalid(this.currentRowIndex, GermplasmListParser.DESCRIPTION_SHEET_NO, VariateHeaders.names())) {
			throw new FileParsingException("GERMPLASM_PARSE_VARIATE_HEADER_ERROR");
		}

		VariateDetailsConverter variateDetailsConverter =
				new VariateDetailsConverter(this.workbook, this.currentRowIndex + 1, GermplasmListParser.DESCRIPTION_SHEET_NO,
						VariateHeaders.values().length, VariateHeaders.names());

		List<ImportedVariate> variateList =
				variateDetailsConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		// If a VARIATE header exists without accompanying data, show a warning after the import
		// This alerts the user to a case of multiple VARIATE headers, where the first does not
		// have data.  User has requested a warning message.
		if (variateList.isEmpty()) {
		    this.noVariatesWarning = "VARIATE header present with no data";
		}
		this.attributeVariates = variateDetailsConverter.getAttributeVariates();

		// if there's a stock id factor but no inventory column variate, we have to ignore the stock ids and treat it as a normal germplasm
		// import                                                            
		// lets show a warning message after the import
		this.seedAmountVariate = variateDetailsConverter.getSeedAmountVariate();

		if (this.seedAmountVariate.isEmpty() && this.specialFactors.containsKey(FactorTypes.STOCK)) {
			this.importedGermplasmList.removeImportedFactor(this.specialFactors.get(FactorTypes.STOCK));
			this.specialFactors.remove(FactorTypes.STOCK);

			this.noInventoryWarning = "StockIDs can only be added for germplasm if it has existing inventory in the BMS";
		}

		for (ImportedVariate variate : variateList) {
			this.importedGermplasmList.addImportedVariate(variate);
		}
	}

	protected void parseConstants() throws FileParsingException {
		// constants is optional so lets check first if its there
		if (!"CONSTANT".equalsIgnoreCase(this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, this.currentRowIndex, 0))) {
			return;
		}

		if (this.isHeaderInvalid(this.currentRowIndex, GermplasmListParser.DESCRIPTION_SHEET_NO, ConstantHeaders.names())) {
			throw new FileParsingException("GERMPLASM_PARSE_CONSTANT_HEADER_ERROR");
		}

		ConstantsDetailsConverter constantsDetailsConverter =
				new ConstantsDetailsConverter(this.workbook, this.currentRowIndex + 1, GermplasmListParser.DESCRIPTION_SHEET_NO,
						ConstantHeaders.values().length, ConstantHeaders.names());

		List<ImportedConstant> constantList =
				constantsDetailsConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		for (ImportedConstant constant : constantList) {
			this.importedGermplasmList.addImportedConstant(constant);
		}

		// update current row index
		this.currentRowIndex = constantsDetailsConverter.getCurrentIndex();
		this.continueTillNextSection();
	}

	protected void parseFactors() throws FileParsingException {

		if (this.isHeaderInvalid(this.currentRowIndex, GermplasmListParser.DESCRIPTION_SHEET_NO, FactorHeaders.names())) {
			throw new FileParsingException("GERMPLASM_PARSE_FACTORS_HEADER_ERROR");
		}

		FactorDetailsConverter factorDetailsConverter =
				new FactorDetailsConverter(this.workbook, this.currentRowIndex + 1, GermplasmListParser.DESCRIPTION_SHEET_NO,
						FactorHeaders.values().length, FactorHeaders.names());

		List<ImportedFactor> factorList = factorDetailsConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		this.importFileIsAdvanced = factorDetailsConverter.isImportFileIsAdvanced();

		// validate all factor sets
		if (!factorDetailsConverter.specialFactors.containsKey(FactorTypes.ENTRY)) {
			throw new FileParsingException("GERMPLASM_PARSE_NO_ENTRY_FACTOR");
		} else if (!factorDetailsConverter.specialFactors.containsKey(FactorTypes.DESIG)) {
			throw new FileParsingException("GERMPLASM_PARSE_NO_DESIG_FACTOR");
		}

		this.specialFactors = factorDetailsConverter.getSpecialFactors();
		this.nameFactors = factorDetailsConverter.getNameFactors();
		for (ImportedFactor factor : factorList) {
			this.importedGermplasmList.addImportedFactor(factor);
		}

		// update current row index
		this.currentRowIndex = factorDetailsConverter.getCurrentIndex();
		this.continueTillNextSection();
	}

	protected void parseConditions() throws FileParsingException {

		if (this.isHeaderInvalid(GermplasmListParser.CONDITION_HEADER_ROW_INDEX, GermplasmListParser.DESCRIPTION_SHEET_NO,
				ConditionHeaders.names())) {
			throw new FileParsingException("GERMPLASM_PARSE_CONDITION_HEADER_ERROR");
		}

		ConditionDetailsConverter conditionDetailsConverter =
				new ConditionDetailsConverter(this.workbook, GermplasmListParser.CONDITION_HEADER_ROW_INDEX + 1,
						GermplasmListParser.DESCRIPTION_SHEET_NO, ConditionHeaders.values().length, ConditionHeaders.names());

		List<ImportedCondition> conditionsList =
				conditionDetailsConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		for (ImportedCondition condition : conditionsList) {
			this.importedGermplasmList.addImportedCondition(condition);
		}

		// update current row index
		this.currentRowIndex = conditionDetailsConverter.getCurrentIndex();
		this.continueTillNextSection();
	}

	protected void parseListDetails() throws FileParsingException {
		String listName = this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, 0, 1);
		String listTitle = this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, 1, 1);

		String labelId = this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, 2, 0);

		// we have this since listdate or listtype might switch in the template
		int listDateColNo = GermplasmListParser.LIST_DATE.equalsIgnoreCase(labelId) ? 2 : 3;
		int listTypeColNo = GermplasmListParser.LIST_TYPE.equalsIgnoreCase(labelId) ? 2 : 3;

		Date listDate;
		try {
			listDate = DateUtil.parseDate(this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, listDateColNo, 1));
		} catch (ParseException e) {
			throw new FileParsingException("GERMPLASM_PARSE_LIST_DATE_FORMAT_INVALID");
		}

		String listType = this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, listTypeColNo, 1);

		if (!GermplasmListParser.TEMPLATE_LIST_TYPE.equalsIgnoreCase(listType)) {
			throw new FileParsingException("GERMPLASM_PARSE_LIST_TYPE_INVALID");
		}

		this.importedGermplasmList = new ImportedGermplasmList(this.originalFilename, listName, listTitle, listType, listDate);
	}

	/**
	 * This validator might be too strict for germplasm list parser
	 * 
	 * @return ParseValidationMap
	 */
	protected ParseValidationMap parseObservationHeaders() throws FileParsingException {
		ParseValidationMap validationMap = new ParseValidationMap();

		final int headerSize = this.importedGermplasmList.sizeOfObservationHeader();

		boolean hasEntryColumn = false;
		boolean hasDesigColumn = false;
		boolean hasGidColumn = false;
		boolean hasStockId = false;
		boolean hasInventoryVariate = false;
		// were accounting for two additional unknown columns inserted between the headers, then we'll
		// just ignore it
		for (int i = 0; i < headerSize + 2; i++) {
			// search the current header
			String obsHeader = this.getCellStringValue(GermplasmListParser.OBSERVATION_SHEET_NO, 0, i);

			// validation is only limited to existance of entry and desig factors
			if (this.specialFactors.get(FactorTypes.ENTRY).equals(obsHeader)) {
				validationMap.addValidation(i, new ValueTypeValidator(Integer.class));
				validationMap.addValidation(i, new NonEmptyValidator());

				hasEntryColumn = true;
			} else if (this.specialFactors.get(FactorTypes.DESIG).equals(obsHeader)) {
				hasDesigColumn = true;
			} else if (this.importFileIsAdvanced && this.specialFactors.get(FactorTypes.GID).equals(obsHeader)) {
				hasGidColumn = true;
			} else if (this.specialFactors.containsKey(FactorTypes.STOCK) && this.specialFactors.get(FactorTypes.STOCK).equals(obsHeader)) {
				hasStockId = true;
			} else if (!this.seedAmountVariate.isEmpty() && this.seedAmountVariate.equalsIgnoreCase(obsHeader)) {
				validationMap.addValidation(i, new ValueTypeValidator(Double.class));
				hasInventoryVariate = true;
			}

			this.observationColumnMap.put(i, obsHeader);
		}

		if (!hasEntryColumn) {
			throw new FileParsingException("GERMPLASM_PARSE_ENTRY_COLUMN_MISSING");
		} else if (!hasGidColumn && !hasDesigColumn) {
			throw new FileParsingException("GERMPLASM_PARSE_DESIG_COLUMN_MISSING");
		} else if (this.importFileIsAdvanced && !hasGidColumn) {
			throw new FileParsingException("GERMPLASM_PARSE_GID_COLUMN_MISSING");
		} else if (this.specialFactors.containsKey(FactorTypes.STOCK) && !hasStockId) {
			throw new FileParsingException("GERMPLASM_PARSE_STOCK_COLUMN_MISSING");
		} else if (this.seedAmountVariate.isEmpty() && this.specialFactors.containsKey(FactorTypes.STOCK)
				|| !this.seedAmountVariate.isEmpty() && !hasInventoryVariate && this.specialFactors.containsKey(FactorTypes.STOCK)) {
			this.importedGermplasmList.removeImportedFactor(this.specialFactors.get(FactorTypes.STOCK));
			this.specialFactors.remove(FactorTypes.STOCK);
			this.seedAmountVariate = "";
			this.noInventoryWarning = "StockIDs can only be added for germplasm if it has existing inventory in the BMS";
		}

		return validationMap;
	}

	protected void parseObservationRows() throws FileParsingException {
		ParseValidationMap validationMap = this.parseObservationHeaders();
		ObservationRowConverter observationRowConverter =
				new ObservationRowConverter(this.workbook, 1, 1, this.observationColumnMap.size(), this.observationColumnMap.values()
						.toArray(new String[this.observationColumnMap.size()]));
		observationRowConverter.setValidationMap(validationMap);

		List<ImportedGermplasm> importedGermplasms =
				observationRowConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		this.importedGermplasmList.setImportedGermplasms(importedGermplasms);
		if (this.specialFactors.containsKey(FactorTypes.STOCK)) {
			StockIDValidator validator = new StockIDValidator(this.specialFactors.get(FactorTypes.STOCK), this.importedGermplasmList);
			validator.validate();
		}

		this.importedGermplasmList.normalizeGermplasmList();
	}

	private void continueTillNextSection() {
		// were limiting to 10 blank rows
		for (int i = 0; this.isRowEmpty(GermplasmListParser.DESCRIPTION_SHEET_NO, this.currentRowIndex,
				GermplasmListParser.DESCRIPTION_SHEET_COL_SIZE) && i < 10; i++) {
			this.currentRowIndex++;
		}
	}

	class ConditionDetailsConverter extends WorkbookRowConverter<ImportedCondition> {

		public ConditionDetailsConverter(Workbook workbook, int startingIndex, int targetSheetIndex, int columnCount, String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
		}

		@Override
		public ImportedCondition convertToObject(Map<Integer, String> rowValues) throws FileParsingException {
			return new ImportedCondition(rowValues.get(0), rowValues.get(1), rowValues.get(2), rowValues.get(3), rowValues.get(4),
					rowValues.get(5), rowValues.get(6), rowValues.get(7));

		}
	}

	class FactorDetailsConverter extends WorkbookRowConverter<ImportedFactor> {

		public static final String GERMPLASM_ENTRY_PROPERTY = "GERMPLASM ENTRY";
		public static final String GERMPLASM_ID_PROPERTY = "GERMPLASM ID";
		public static final String SEED_SOURCE_PROPERTY = "SEED SOURCE";
		public static final String CROSS_NAME_PROPERTY = "CROSS NAME";
		public static final String GERMPLASM_STOCK_ID_PROPERTY = "GERMPLASM STOCK ID";
		public static final String NUMBER_SCALE = "NUMBER";
		public static final String DBCV_SCALE = "DBCV";
		public static final String DBID_SCALE = "DBID";
		public static final String CODE_SCALE = "CODE";
		public static final String NAME_SCALE = "NAME";
		public static final String ASSIGNED_METHOD = "ASSIGNED";
		private boolean importFileIsAdvanced = false;

		private final Map<FactorTypes, String> specialFactors = new HashMap<>();
		private final Set<String> nameFactors = new HashSet<>();

		public FactorDetailsConverter(Workbook workbook, int startingIndex, int targetSheetIndex, int columnCount, String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
		}

		@Override
		public ImportedFactor convertToObject(Map<Integer, String> rowValues) throws FileParsingException {
			ImportedFactor importedFactor =
					new ImportedFactor(rowValues.get(0), rowValues.get(1), rowValues.get(2), rowValues.get(3), rowValues.get(4),
							rowValues.get(5), rowValues.get(6), rowValues.get(7));

			// row based validations here
			String property = importedFactor.getProperty() == null ? "" : importedFactor.getProperty().toUpperCase();
			String scale = importedFactor.getScale() == null ? "" : importedFactor.getScale().toUpperCase();
			String method = importedFactor.getMethod() == null ? "" : importedFactor.getMethod().toUpperCase();

			if (FactorDetailsConverter.GERMPLASM_ENTRY_PROPERTY.equals(property) && FactorDetailsConverter.NUMBER_SCALE.equals(scale)) {
				this.specialFactors.put(FactorTypes.ENTRY, importedFactor.getFactor());
			} else if (FactorDetailsConverter.GERMPLASM_ID_PROPERTY.equals(property) && FactorDetailsConverter.DBCV_SCALE.equals(scale)) {
				this.specialFactors.put(FactorTypes.DESIG, importedFactor.getFactor());
			} else if (FactorDetailsConverter.GERMPLASM_ID_PROPERTY.equals(property) && FactorDetailsConverter.DBID_SCALE.equals(scale)) {
				this.specialFactors.put(FactorTypes.GID, importedFactor.getFactor());
				this.importFileIsAdvanced = true;
			} else if (FactorDetailsConverter.GERMPLASM_ENTRY_PROPERTY.equals(property) && FactorDetailsConverter.CODE_SCALE.equals(scale)) {
				this.specialFactors.put(FactorTypes.ENTRYCODE, importedFactor.getFactor());
			} else if (FactorDetailsConverter.SEED_SOURCE_PROPERTY.equals(property) && FactorDetailsConverter.NAME_SCALE.equals(scale)) {
				this.specialFactors.put(FactorTypes.SOURCE, importedFactor.getFactor());
			} else if (FactorDetailsConverter.CROSS_NAME_PROPERTY.equals(property) && FactorDetailsConverter.NAME_SCALE.equals(scale)) {
				this.specialFactors.put(FactorTypes.CROSS, importedFactor.getFactor());
			} else if (FactorDetailsConverter.GERMPLASM_STOCK_ID_PROPERTY.equals(property)
					&& FactorDetailsConverter.DBCV_SCALE.equals(scale)) {
				this.specialFactors.put(FactorTypes.STOCK, importedFactor.getFactor());
			} else if (FactorDetailsConverter.GERMPLASM_ID_PROPERTY.equals(property) && FactorDetailsConverter.NAME_SCALE.equals(scale)
					&& FactorDetailsConverter.ASSIGNED_METHOD.equals(method)) {
				this.nameFactors.add(importedFactor.getFactor());
			}

			return importedFactor;
		}

		public Map<FactorTypes, String> getSpecialFactors() {
			return this.specialFactors;
		}

		public Set<String> getNameFactors() {
			return this.nameFactors;
		}

		public boolean isImportFileIsAdvanced() {
			return this.importFileIsAdvanced;
		}
	}

	class ConstantsDetailsConverter extends WorkbookRowConverter<ImportedConstant> {

		public ConstantsDetailsConverter(Workbook workbook, int startingIndex, int targetSheetIndex, int columnCount, String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
		}

		@Override
		public ImportedConstant convertToObject(Map<Integer, String> rowValues) throws FileParsingException {
			return new ImportedConstant(rowValues.get(0), rowValues.get(1), rowValues.get(2), rowValues.get(3), rowValues.get(4),
					rowValues.get(5), rowValues.get(6), rowValues.get(7));
		}
	}

	class VariateDetailsConverter extends WorkbookRowConverter<ImportedVariate> {

		private String seedAmountVariate = "";
		private final Set<String> attributeVariates = new HashSet<>();

		public VariateDetailsConverter(Workbook workbook, int startingIndex, int targetSheetIndex, int columnCount, String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
		}

		@Override
		public ImportedVariate convertToObject(Map<Integer, String> rowValues) throws FileParsingException {

			ImportedVariate importedVariate =
					new ImportedVariate(rowValues.get(0), rowValues.get(1), rowValues.get(2), rowValues.get(3), rowValues.get(4),
							rowValues.get(5));

			String property = importedVariate.getProperty() == null ? "" : importedVariate.getProperty().toUpperCase();

			try {
				if (GermplasmListParser.this.ontologyDataManager.isSeedAmountVariable(property)) {
					importedVariate.setSeedStockVariable(true);
					this.seedAmountVariate = importedVariate.getVariate();
					GermplasmListParser.LOG.debug("SEED STOCK :" + importedVariate.getProperty());
				} else if ("ATTRIBUTE".equals(property) || "PASSPORT".equals(property)) {
					this.attributeVariates.add(importedVariate.getVariate());
				}
			} catch (MiddlewareQueryException e) {
				GermplasmListParser.LOG.error("SEED STOCK " + importedVariate.getProperty(), e);
			}

			return importedVariate;
		}

		public String getSeedAmountVariate() {
			return this.seedAmountVariate;
		}

		public Set<String> getAttributeVariates() {
			return this.attributeVariates;
		}
	}

	class ObservationRowConverter extends WorkbookRowConverter<ImportedGermplasm> {

		// we maintain an entrySet for checking dupes
		private final Set<String> entrySet = new HashSet<>();

		public ObservationRowConverter(Workbook workbook, int startingIndex, int targetSheetIndex, int columnCount, String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels, false);
		}

		@Override
		public ImportedGermplasm convertToObject(final Map<Integer, String> rowValues) throws FileParsingException {
			final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
			for (final int colIndex : rowValues.keySet()) {
				String colHeader = GermplasmListParser.this.observationColumnMap.get(colIndex);
				// Map cell (given a column label) with a pojo setter

				Map<FactorTypes, Command> factorBehaviors = new HashMap<>();
				factorBehaviors.put(FactorTypes.ENTRY, new Command() {

					@Override
					public void run() throws FileParsingException {
						String entryId = rowValues.get(colIndex);
						if (!ObservationRowConverter.this.entrySet.contains(entryId)) {
							ObservationRowConverter.this.entrySet.add(entryId);
							importedGermplasm.setEntryId(Integer.valueOf(entryId));
						} else {
							throw new FileParsingException("GERMPLASM_PARSE_DUPLICATE_ENTRY");
						}
					}
				});

				factorBehaviors.put(FactorTypes.DESIG, new Command() {

					@Override
					public void run() throws FileParsingException {
						importedGermplasm.setDesig(rowValues.get(colIndex));
					}
				});

				factorBehaviors.put(FactorTypes.GID, new Command() {

					@Override
					public void run() throws FileParsingException {
						String val = rowValues.get(colIndex);

						if (val.matches("^-?\\d+$")) {
							importedGermplasm.setGid(Integer.valueOf(val));
						}
					}
				});

				factorBehaviors.put(FactorTypes.CROSS, new Command() {

					@Override
					public void run() throws FileParsingException {
						importedGermplasm.setCross(rowValues.get(colIndex));
					}
				});

				factorBehaviors.put(FactorTypes.SOURCE, new Command() {

					@Override
					public void run() throws FileParsingException {
						importedGermplasm.setSource(rowValues.get(colIndex));
					}
				});

				factorBehaviors.put(FactorTypes.ENTRYCODE, new Command() {

					@Override
					public void run() throws FileParsingException {
						importedGermplasm.setEntryCode(rowValues.get(colIndex));
					}
				});

				factorBehaviors.put(FactorTypes.STOCK, new Command() {

					@Override
					public void run() throws FileParsingException {
						if (!"".equals(rowValues.get(colIndex)) && !GermplasmListParser.this.importedGermplasmList.isHasStockIDValues()) {
							GermplasmListParser.this.importedGermplasmList.setHasStockIDValues(true);
						}
						importedGermplasm.setInventoryId(rowValues.get(colIndex));
					}
				});

				boolean shouldContinue = true;
				for (Map.Entry<FactorTypes, Command> entry : factorBehaviors.entrySet()) {
					if (this.executeOnFactorMatch(colHeader, entry.getKey(), entry.getValue())) {
						shouldContinue = false;
						break;
					}
				}

				if (!shouldContinue) {
					continue;
				}

				if (this.executeIfHasNameFactors(colHeader, rowValues.get(colIndex), importedGermplasm)) {
					continue;
				}

				if (this.executeIfIsAttributeVariate(colHeader, rowValues.get(colIndex), importedGermplasm)) {
					continue;
				}

				if (this.executeIfIsSeedAmountVariate(colHeader, rowValues.get(colIndex), importedGermplasm)) {
					continue;
				}

				GermplasmListParser.LOG.debug(String.format("%s header is not recognized [parsing from row: %s]", colHeader,
						this.currentIndex));
			}

			// row based validation here
			// GID is given, but no DESIG, get value of DESIG given GID
			if (importedGermplasm.getGid() != null && (importedGermplasm.getDesig() == null || "".equals(importedGermplasm.getDesig()))) {
				try {

					// Check if germplasm exists
					Germplasm currentGermplasm =
							GermplasmListParser.this.germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid());
					if (currentGermplasm == null) {
						throw new FileParsingException("GERMPLSM_PARSE_DB_GID_NOT_EXISTS", this.currentIndex, importedGermplasm.getGid()
								.toString(), GermplasmListParser.this.specialFactors.get(FactorTypes.GID));
					} else {

						List<Integer> importedGermplasmGids = new ArrayList<>();
						importedGermplasmGids.add(importedGermplasm.getGid());

						Map<Integer, String> preferredNames =
								GermplasmListParser.this.germplasmDataManager.getPreferredNamesByGids(importedGermplasmGids);

						if (preferredNames.get(importedGermplasm.getGid()) != null) {
							importedGermplasm.setDesig(preferredNames.get(importedGermplasm.getGid()));
						}

					}
				} catch (MiddlewareQueryException e) {
					GermplasmListParser.LOG.error(e.getMessage(), e);
				}

				// GID is not given or 0, and DESIG is not given
			} else if ((importedGermplasm.getGid() == null || importedGermplasm.getGid().equals(Integer.valueOf(0)))
					&& (importedGermplasm.getDesig() == null || importedGermplasm.getDesig().length() == 0)) {
				throw new FileParsingException("GERMPLSM_PARSE_GID_DESIG_NOT_EXISTS", this.currentIndex, "",
						GermplasmListParser.this.specialFactors.get(FactorTypes.GID));

			} else if ((importedGermplasm.getSeedAmount() == null || importedGermplasm.getSeedAmount() == 0)
					&& importedGermplasm.getInventoryId() != null && !StringUtils.isEmpty(importedGermplasm.getInventoryId())) {
				GermplasmListParser.this.noInventoryWarning =
						"StockIDs can only be added for germplasm if it has existing inventory in the BMS, or inventory"
								+ " is being added in the import. Some of the StockIDs in this import file do not meet these requirements and will be ignored.";
			}

			return importedGermplasm;
		}

		public boolean executeOnFactorMatch(String header, FactorTypes type, Command e) throws FileParsingException {
			if (GermplasmListParser.this.specialFactors.containsKey(type)
					&& GermplasmListParser.this.specialFactors.get(type).equalsIgnoreCase(header)) {
				e.run();
				return true;
			}
			return false;
		}

		public boolean executeIfHasNameFactors(String header, String value, ImportedGermplasm germplasmReference)
				throws FileParsingException {
			if (GermplasmListParser.this.nameFactors != null && GermplasmListParser.this.nameFactors.contains(header)) {
				germplasmReference.addNameFactor(header, value);
				return true;
			}

			return false;
		}

		public boolean executeIfIsAttributeVariate(String header, String value, ImportedGermplasm germplasmReference)
				throws FileParsingException {
			if (GermplasmListParser.this.attributeVariates != null && GermplasmListParser.this.attributeVariates.contains(header)) {
				germplasmReference.addAttributeVariate(header, value);
				return true;
			}

			return false;
		}

		public boolean executeIfIsSeedAmountVariate(String header, String value, ImportedGermplasm germplasmReference)
				throws FileParsingException {
			if ("".equals(header)) {
				return false;
			}

			if (GermplasmListParser.this.seedAmountVariate != null && GermplasmListParser.this.seedAmountVariate.equals(header)) {
				Double seedAmountValue = "".equals(value) ? 0 : Double.valueOf(value);
				germplasmReference.setSeedAmount(seedAmountValue);
			} else {
				return false;
			}

			return true;
		}
	}

	public interface Command {

		void run() throws FileParsingException;
	}
}
