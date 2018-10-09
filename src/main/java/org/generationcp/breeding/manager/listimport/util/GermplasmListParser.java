
package org.generationcp.breeding.manager.listimport.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import org.generationcp.commons.workbook.generator.RowColumnType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.base.Strings;

/**
 * Class for parsing GermplsmList
 */
@Configurable
public class GermplasmListParser extends AbstractExcelFileParser<ImportedGermplasmList> {

	public static final int DESCRIPTION_SHEET_COL_SIZE = 8;
	public static final int DESCRIPTION_SHEET_NO = 0;
	public static final int CONDITION_HEADER_ROW_INDEX = 5;
	public static final String LIST_DATE = "LIST DATE";
	public static final String LIST_TYPE = "LIST TYPE";
	public static final String GERMPLASM_PARSE_HAS_NAME_TYPE_DUPLICATES_IN_DB = "GERMPLASM_PARSE_HAS_NAME_TYPE_DUPLICATES_IN_DB";
	public static final String GERMPLASM_PARSE_HAS_NAME_TYPE_DUPLICATES_IN_FILE = "GERMPLASM_PARSE_HAS_NAME_TYPE_DUPLICATES_IN_FILE";

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListParser.class);
	private static final int OBSERVATION_SHEET_NO = 1;

	@Resource
	private GermplasmListManager germplasmListManager;
	@Resource
	private GermplasmDataManager germplasmDataManager;
	@Resource
	private OntologyDataManager ontologyDataManager;
	@Resource
	private StockIDValidator stockIDValidator;

	private int currentRowIndex = 0;
	private ImportedGermplasmList importedGermplasmList;
	private Map<FactorTypes, String> specialFactors = new HashMap<>();

	private String noInventoryWarning = "";
	private boolean importFileIsAdvanced = false;
	private String seedAmountVariate = "";
	private List<String> nameFactors;
	private Set<String> attributeVariates;
	private Set<String> descriptionVariableNames = new HashSet<>();

	public String getNoInventoryWarning() {
		return this.noInventoryWarning;
	}

	public void setOriginalFilename(final String originalFilename) {
		this.originalFilename = originalFilename;
	}

	/**
	 * @return true if the inventory variable (i.e. SEED_AMOUNT_G) is included in the Description Sheet of the imported file
	 */
	public boolean hasInventoryVariable() {
		return !this.seedAmountVariate.isEmpty();
	}

	/**
	 * NOTE: The imported file contains 2 sheet: Description Sheet and Observation Sheet.
	 *
	 * @return true if there is an Inventory Variable in the Description Sheet (i.e SEED_AMOUNT_G) and either STOCKID is not included in the
	 *         Description Sheet or there is no values under STOCKID column in Observation Sheet
	 */
	public boolean hasInventoryAmountOnly() {
		return this.hasInventoryVariable()
				&& (!this.specialFactors.containsKey(FactorTypes.STOCK) || !this.importedGermplasmList.isHasStockIDValues());
	}

	/**
	 * NOTE: The imported file contains 2 sheet: Description Sheet and Observation Sheet.
	 *
	 * @return true if there is an Inventory Variable in the Description Sheet (i.e SEED_AMOUNT_G) and there is at least one value under the
	 *         Inventory Variable column in Observation Sheet
	 */
	public boolean hasInventoryAmount() {

		if (!this.hasInventoryVariable()) {
			return false;
		}

		for (final ImportedGermplasm germplasm : this.importedGermplasmList.getImportedGermplasm()) {
			final Double seedAmount = germplasm.getSeedAmount();

			// make sure that there is at least one row with inventory amount
			if (seedAmount > 0.0) {
				return true;
			}
		}

		return false;
	}

	public boolean hasAtLeastOneRowWithInventoryAmountButNoDefinedStockID() {

		if (!this.hasInventoryVariable()) {
			return false;
		}

		for (final ImportedGermplasm germplasm : this.importedGermplasmList.getImportedGermplasm()) {
			final Double seedAmount = germplasm.getSeedAmount();
			final String stockId = germplasm.getInventoryId();

			// make sure that there is at least one row with inventory amount
			// and stock Id is blank
			if (seedAmount > 0.0 && Strings.isNullOrEmpty(stockId)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasStockIdValues() {
		return this.importedGermplasmList.isHasStockIDValues();
	}

	public boolean hasStockIdFactor() {
		return this.specialFactors.containsKey(FactorTypes.STOCK);
	}

	public boolean isSeedAmountVariable(final ImportedVariate variate) {
		return !this.seedAmountVariate.isEmpty() && this.seedAmountVariate.equals(variate.getVariate());
	}

	public boolean importFileIsAdvanced() {
		return this.importFileIsAdvanced;
	}

	public List<String> getNameFactors() {
		return this.nameFactors;
	}

	@Override
	public ImportedGermplasmList parseWorkbook(final Workbook workbook, final Map<String, Object> additionalParams)
			throws FileParsingException {
		this.workbook = workbook;

		this.parseListDetails();
		this.parseConditions();
		this.parseFactors();
		this.parseConstants();
		this.parseInventory();
		this.parseVariates();
		this.parseObservationRows();
		return this.importedGermplasmList;
	}

	protected void parseInventory() throws FileParsingException {
		// inventory might be optional so lets check first if its there just in case
		if (!"INVENTORY".equalsIgnoreCase(this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, this.currentRowIndex, 0))) {
			return;
		}

		if (this.isHeaderInvalid(this.currentRowIndex, GermplasmListParser.DESCRIPTION_SHEET_NO, InventoryHeaders.names())) {
			throw new FileParsingException("GERMPLASM_PARSE_INVENTORY_HEADER_ERROR");
		}

		final WorkbookRowConverter<Boolean> converter = new WorkbookRowConverter<Boolean>(this.workbook, this.currentRowIndex + 1,
				GermplasmListParser.DESCRIPTION_SHEET_NO, InventoryHeaders.values().length, InventoryHeaders.names()) {

			@Override
			public Boolean convertToObject(final Map<Integer, String> rowValues) throws FileParsingException {
				final String property = rowValues.get(2) == null ? "" : rowValues.get(2).toUpperCase();
				final String scale = rowValues.get(3) == null ? "" : rowValues.get(3).toUpperCase();
				// stock id factor parse
				if (FactorDetailsConverter.GERMPLASM_STOCK_ID_PROPERTY.equalsIgnoreCase(property)
						&& FactorDetailsConverter.isStockIdScale(scale)) {

					final ImportedFactor importedFactor = new ImportedFactor(rowValues.get(0).toUpperCase(), rowValues.get(1),
							rowValues.get(2), rowValues.get(3), rowValues.get(4), rowValues.get(5), rowValues.get(6), rowValues.get(7));

					// lets remove if exists just in case
					GermplasmListParser.this.specialFactors.remove(FactorTypes.STOCK);
					GermplasmListParser.this.specialFactors.put(FactorTypes.STOCK, importedFactor.getFactor());

					// add to importedGermplasmList
					for (final Iterator<ImportedFactor> iter =
							GermplasmListParser.this.importedGermplasmList.getImportedFactors().listIterator(); iter.hasNext();) {
						final ImportedFactor factor = iter.next();
						if (factor.getFactor().equals(importedFactor.getFactor())) {
							iter.remove();
						}
					}
					GermplasmListParser.this.importedGermplasmList.addImportedFactor(importedFactor);
					GermplasmListParser.this.descriptionVariableNames.add(importedFactor.getFactor());

					return true;
				}

				// seed amount variate parse
				try {
					if (GermplasmListParser.this.ontologyDataManager.isSeedAmountVariable(property)) {
						final ImportedVariate seedAmountVariate = new ImportedVariate(rowValues.get(0).toUpperCase(), rowValues.get(1),
								rowValues.get(2), rowValues.get(3), rowValues.get(4), rowValues.get(5));

						seedAmountVariate.setSeedStockVariable(true);
						GermplasmListParser.this.seedAmountVariate = seedAmountVariate.getVariate();
						GermplasmListParser.this.importedGermplasmList.addImportedVariate(seedAmountVariate);
						GermplasmListParser.this.descriptionVariableNames.add(seedAmountVariate.getVariate());
						GermplasmListParser.LOG.debug("SEED STOCK :" + seedAmountVariate.getProperty());

						return true;
					}
				} catch (final MiddlewareQueryException e) {
					GermplasmListParser.LOG.error("SEED STOCK " + property, e);

				}

				return false;
			}
		};

		converter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		this.applyWarningIfNoInventory();

		this.currentRowIndex = converter.getCurrentIndex();
		this.continueTillNextSection();
	}

	protected void parseVariates() throws FileParsingException {
		// variate is optional so lets check first if its there
		if (!"VARIATE".equalsIgnoreCase(this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, this.currentRowIndex, 0))) {
			return;
		}

		if (this.isHeaderInvalid(this.currentRowIndex, GermplasmListParser.DESCRIPTION_SHEET_NO, VariateHeaders.names())) {
			throw new FileParsingException("GERMPLASM_PARSE_VARIATE_HEADER_ERROR");
		}

		final VariateDetailsConverter variateDetailsConverter = new VariateDetailsConverter(this.workbook, this.currentRowIndex + 1,
				GermplasmListParser.DESCRIPTION_SHEET_NO, VariateHeaders.values().length, VariateHeaders.names());

		final List<ImportedVariate> variateList =
				variateDetailsConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		this.attributeVariates = variateDetailsConverter.getAttributeVariates();

		// if theres a stock id factor but no inventory column variate, we have to ignore the stock ids and treet it as a normal germplasm
		// import
		// lets show a warning message after the import
		if (StringUtils.isBlank(this.seedAmountVariate)) {
			this.seedAmountVariate = variateDetailsConverter.getSeedAmountVariate();
		} else {
			// remove seedStockVariable if already added in inventory section
			for (final Iterator<ImportedVariate> iter = variateList.iterator(); iter.hasNext();) {
				final ImportedVariate currentVariate = iter.next();
				if (currentVariate.isSeedStockVariable()) {
					iter.remove();
				}
			}
		}

		this.applyWarningIfNoInventory();

		for (final ImportedVariate variate : variateList) {
			this.importedGermplasmList.addImportedVariate(variate);
			this.descriptionVariableNames.add(variate.getVariate());
		}
	}

	private void applyWarningIfNoInventory() {
		if (this.seedAmountVariate.isEmpty() && this.specialFactors.containsKey(FactorTypes.STOCK)) {
			this.importedGermplasmList.removeImportedFactor(this.specialFactors.get(FactorTypes.STOCK));
			this.specialFactors.remove(FactorTypes.STOCK);

			this.noInventoryWarning = "StockIDs can only be added for germplasm if it has existing inventory in the BMS";
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

		final ConstantsDetailsConverter constantsDetailsConverter = new ConstantsDetailsConverter(this.workbook, this.currentRowIndex + 1,
				GermplasmListParser.DESCRIPTION_SHEET_NO, ConstantHeaders.values().length, ConstantHeaders.names());

		final List<ImportedConstant> constantList =
				constantsDetailsConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		for (final ImportedConstant constant : constantList) {
			this.importedGermplasmList.addImportedConstant(constant);
			this.descriptionVariableNames.add(constant.getConstant());
		}

		// update current row index
		this.currentRowIndex = constantsDetailsConverter.getCurrentIndex();
		this.continueTillNextSection();
	}

	protected void parseFactors() throws FileParsingException {

		if (this.isHeaderInvalid(this.currentRowIndex, GermplasmListParser.DESCRIPTION_SHEET_NO, FactorHeaders.names())) {
			throw new FileParsingException("GERMPLASM_PARSE_FACTORS_HEADER_ERROR");
		}

		final FactorDetailsConverter factorDetailsConverter = new FactorDetailsConverter(this.workbook, this.currentRowIndex + 1,
				GermplasmListParser.DESCRIPTION_SHEET_NO, FactorHeaders.values().length, FactorHeaders.names());

		final List<ImportedFactor> factorList =
				factorDetailsConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		this.importFileIsAdvanced = factorDetailsConverter.isImportFileIsAdvanced();

		// validate all factor sets
		if (!factorDetailsConverter.hasSpecialFactor(FactorTypes.ENTRY)) {
			throw new FileParsingException("GERMPLASM_PARSE_NO_ENTRY_FACTOR");
		} else if (!factorDetailsConverter.hasSpecialFactor(FactorTypes.DESIG)) {
			throw new FileParsingException("GERMPLASM_PARSE_NO_DESIG_FACTOR");
		}

		this.specialFactors = factorDetailsConverter.getSpecialFactors();
		this.nameFactors = factorDetailsConverter.getNameFactors();
		for (final ImportedFactor factor : factorList) {
			this.importedGermplasmList.addImportedFactor(factor);
			this.descriptionVariableNames.add(factor.getFactor());
		}

		this.validateNameTypesForDuplicates(factorList);

		// update current row index
		this.currentRowIndex = factorDetailsConverter.getCurrentIndex();
		this.continueTillNextSection();
	}

	protected void validateNameTypesForDuplicates(final List<ImportedFactor> factors) throws  FileParsingException {
		final Set<String> nameTypesWithDuplicateInDB = new HashSet<>();
		final List<String> nameTypesDescriptions = new ArrayList<>();
		boolean hasNameTypeDescriptionDuplicates = false;

		for(final  ImportedFactor factor: factors) {
			if(this.nameFactors.contains(factor.getFactor())) {
				List<UserDefinedField> nameTypes = this.germplasmDataManager.getUserDefinedFieldByFieldTableNameAndFTypeAndFName(RowColumnType.NAME_TYPES.getFtable(),
						RowColumnType.NAME_TYPES.getFtype(), factor.getDescription());
				//Check if there are nameType factors in the import file that has the same description in the DB with different fcode
				if(nameTypes != null && !nameTypes.isEmpty()) {
					for(final UserDefinedField nameType: nameTypes) {
						if(!nameType.getFcode().equalsIgnoreCase(factor.getFactor())) {
							nameTypesWithDuplicateInDB.add(factor.getFactor());
						}
					}
				}

				//Check if there are nameType factors in the import file with same description
				if(!hasNameTypeDescriptionDuplicates && nameTypesWithDuplicateInDB.isEmpty() && nameTypesDescriptions.contains(factor.getDescription().toUpperCase())){
					hasNameTypeDescriptionDuplicates = true;
				} else if(!hasNameTypeDescriptionDuplicates) {
					nameTypesDescriptions.add(factor.getDescription().toUpperCase());
				}
			}
		}

		if(!nameTypesWithDuplicateInDB.isEmpty()) {
			throw new FileParsingException(GermplasmListParser.GERMPLASM_PARSE_HAS_NAME_TYPE_DUPLICATES_IN_DB, 1, "", StringUtils.join(nameTypesWithDuplicateInDB, ", "));
		} else if(hasNameTypeDescriptionDuplicates) {
			throw new FileParsingException(GermplasmListParser.GERMPLASM_PARSE_HAS_NAME_TYPE_DUPLICATES_IN_FILE);
		}
	}

	protected void parseConditions() throws FileParsingException {

		if (this.isHeaderInvalid(GermplasmListParser.CONDITION_HEADER_ROW_INDEX, GermplasmListParser.DESCRIPTION_SHEET_NO,
				ConditionHeaders.names())) {
			throw new FileParsingException("GERMPLASM_PARSE_CONDITION_HEADER_ERROR");
		}

		final ConditionDetailsConverter conditionDetailsConverter =
				new ConditionDetailsConverter(this.workbook, GermplasmListParser.CONDITION_HEADER_ROW_INDEX + 1,
						GermplasmListParser.DESCRIPTION_SHEET_NO, ConditionHeaders.values().length, ConditionHeaders.names());

		final List<ImportedCondition> conditionsList =
				conditionDetailsConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		for (final ImportedCondition condition : conditionsList) {
			this.importedGermplasmList.addImportedCondition(condition);
		}

		// update current row index
		this.currentRowIndex = conditionDetailsConverter.getCurrentIndex();
		this.continueTillNextSection();
	}

	protected void parseListDetails() throws FileParsingException {
		final String listName = this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, 0, 1);
		final String listTitle = this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, 1, 1);

		final String labelId = this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, 2, 0);

		// we have this since listdate or listtype might switch in the template
		final int listDateColNo = GermplasmListParser.LIST_DATE.equalsIgnoreCase(labelId) ? 2 : 3;
		final int listTypeColNo = GermplasmListParser.LIST_TYPE.equalsIgnoreCase(labelId) ? 2 : 3;

		final Date listDate;
		try {
			final String listDateCellValue = this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, listDateColNo, 1);

			if (StringUtils.isBlank(listDateCellValue.trim())) {
				listDate = DateUtil.getCurrentDate();
			} else {
				listDate = DateUtil.parseDate(listDateCellValue);
			}
		} catch (final ParseException e) {
			throw new FileParsingException("GERMPLASM_PARSE_LIST_DATE_FORMAT_INVALID");
		}

		final String listType = this.getCellStringValue(GermplasmListParser.DESCRIPTION_SHEET_NO, listTypeColNo, 1);

		if (!this.validateListType(listType)) {
			throw new FileParsingException("GERMPLASM_PARSE_LIST_TYPE_INVALID");
		}

		this.importedGermplasmList = new ImportedGermplasmList(this.originalFilename, listName, listTitle, listType, listDate);
	}

	/**
	 * This method parses the Observation sheet headers and adds validator for each header
	 *
	 * @param observationSheetHeaders
	 *
	 * @return ParseValidationMap
	 */
	protected ParseValidationMap parseObservationSheetHeaders(final Set<String> observationSheetHeaders) throws FileParsingException {
		final ParseValidationMap validationMap = new ParseValidationMap();

		boolean hasDesigColumn = false;
		boolean hasGidColumn = false;
		boolean hasInventoryVariate = false;
		// were accounting for two additional unknown columns inserted between the headers, then we'll
		// just ignore it
		final int headerSize = this.importedGermplasmList.sizeOfObservationHeader();
		for (int i = 0; i < headerSize + 2; i++) {
			// search the current header

			final String observationSheetHeader = this.getCellStringValue(GermplasmListParser.OBSERVATION_SHEET_NO, 0, i).toUpperCase();
			if (StringUtils.isNotBlank(observationSheetHeader)) {
				if (this.specialFactors.get(FactorTypes.ENTRY).equals(observationSheetHeader)) {
					validationMap.addValidation(i, new ValueTypeValidator(Integer.class));
					validationMap.addValidation(i, new NonEmptyValidator());
				} else if (this.specialFactors.get(FactorTypes.DESIG).equals(observationSheetHeader)) {
					hasDesigColumn = true;
				} else if (this.importFileIsAdvanced && this.specialFactors.get(FactorTypes.GID).equals(observationSheetHeader)) {
					hasGidColumn = true;
				} else if (!this.seedAmountVariate.isEmpty() && this.seedAmountVariate.equalsIgnoreCase(observationSheetHeader)) {
					validationMap.addValidation(i, new ValueTypeValidator(Double.class));
					hasInventoryVariate = true;
				}

				if (!observationSheetHeaders.add(observationSheetHeader)) {
					throw new FileParsingException("GERMPLASM_DUPLICATE_HEADER_ERROR", 1, "", observationSheetHeader);
				}
			}
		}

		this.validateObservationSheetHeaders(observationSheetHeaders, hasGidColumn, hasDesigColumn);

		if (this.seedAmountVariate.isEmpty() && this.specialFactors.containsKey(FactorTypes.STOCK)
				|| !this.seedAmountVariate.isEmpty() && !hasInventoryVariate && this.specialFactors.containsKey(FactorTypes.STOCK)) {
			this.importedGermplasmList.removeImportedFactor(this.specialFactors.get(FactorTypes.STOCK));
			this.specialFactors.remove(FactorTypes.STOCK);
			this.seedAmountVariate = "";
			this.noInventoryWarning = "StockIDs can only be added for germplasm if it has existing inventory in the BMS";
		}

		return validationMap;
	}

	protected void validateObservationSheetHeaders(final Set<String> observationSheetHeaders, final boolean hasGidColumn,
			final boolean hasDesigColumn) throws FileParsingException {
		// Checks if all variable names in Description Sheet are existing in the Observation sheet
		for (final String headerName : this.descriptionVariableNames) {
			if (!observationSheetHeaders.contains(headerName)) {
				throw new FileParsingException("GERMPLASM_PARSE_HEADER_ERROR", 1, "", headerName);
			}
		}

		if (!hasGidColumn && !hasDesigColumn) {
			throw new FileParsingException("GERMPLASM_PARSE_DESIG_COLUMN_MISSING");
		} else if (this.importFileIsAdvanced && !hasGidColumn) {
			throw new FileParsingException("GERMPLASM_PARSE_GID_COLUMN_MISSING");
		}
	}

	protected void parseObservationRows() throws FileParsingException {
		final Set<String> observationSheetHeaders = new LinkedHashSet<>();
		final ParseValidationMap validationMap = this.parseObservationSheetHeaders(observationSheetHeaders);
		final ObservationRowConverter observationRowConverter = new ObservationRowConverter(this.workbook, 1, 1,
				observationSheetHeaders.size(), observationSheetHeaders.toArray(new String[observationSheetHeaders.size()]));

		observationRowConverter.setValidationMap(validationMap);

		final List<ImportedGermplasm> importedGermplasms =
				observationRowConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		this.importedGermplasmList.setImportedGermplasm(importedGermplasms);

		if (this.specialFactors.containsKey(FactorTypes.STOCK)) {
			this.stockIDValidator.validate(this.specialFactors.get(FactorTypes.STOCK), this.importedGermplasmList);
			this.validateForMissingInventoryVariable(this.specialFactors.get(FactorTypes.STOCK), this.importedGermplasmList);
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

	enum ConditionHeaders {
		CONDITION("CONDITION"), DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD");

		String label;

		ConditionHeaders(final String label) {
			this.label = label;
		}

		public static String[] names() {
			final ConditionHeaders[] values = ConditionHeaders.values();
			final String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum FactorHeaders {
		FACTOR("FACTOR"), DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD");

		String label;

		FactorHeaders(final String label) {
			this.label = label;
		}

		public static String[] names() {
			final FactorHeaders[] values = FactorHeaders.values();
			final String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum ConstantHeaders {
		CONSTANT("CONSTANT"), DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD");

		String label;

		ConstantHeaders(final String label) {
			this.label = label;
		}

		public static String[] names() {
			final ConstantHeaders[] values = ConstantHeaders.values();
			final String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum InventoryHeaders {
		VARIATE("INVENTORY"), DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD");

		String label;

		InventoryHeaders(final String label) {
			this.label = label;
		}

		public static String[] names() {
			final InventoryHeaders[] values = InventoryHeaders.values();
			final String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum VariateHeaders {
		VARIATE("VARIATE"), DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD");

		String label;

		VariateHeaders(final String label) {
			this.label = label;
		}

		public static String[] names() {
			final VariateHeaders[] values = VariateHeaders.values();
			final String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum FactorTypes {
		ENTRY, DESIG, GID, ENTRYCODE, SOURCE, CROSS, NAME, STOCK
	}

	public interface Command {

		void run() throws FileParsingException;
	}

	class ConditionDetailsConverter extends WorkbookRowConverter<ImportedCondition> {

		public ConditionDetailsConverter(final Workbook workbook, final int startingIndex, final int targetSheetIndex,
				final int columnCount, final String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
		}

		@Override
		public ImportedCondition convertToObject(final Map<Integer, String> rowValues) throws FileParsingException {
			return new ImportedCondition(rowValues.get(0).toUpperCase(), rowValues.get(1), rowValues.get(2), rowValues.get(3),
					rowValues.get(4), rowValues.get(5), rowValues.get(6), rowValues.get(7));

		}
	}

	class ConstantsDetailsConverter extends WorkbookRowConverter<ImportedConstant> {

		public ConstantsDetailsConverter(final Workbook workbook, final int startingIndex, final int targetSheetIndex,
				final int columnCount, final String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
		}

		@Override
		public ImportedConstant convertToObject(final Map<Integer, String> rowValues) throws FileParsingException {
			return new ImportedConstant(rowValues.get(0).toUpperCase(), rowValues.get(1), rowValues.get(2), rowValues.get(3),
					rowValues.get(4), rowValues.get(5), rowValues.get(6), rowValues.get(7));
		}
	}

	class VariateDetailsConverter extends WorkbookRowConverter<ImportedVariate> {

		private final Set<String> attributeVariates = new HashSet<>();
		private String seedAmountVariate = "";

		public VariateDetailsConverter(final Workbook workbook, final int startingIndex, final int targetSheetIndex, final int columnCount,
				final String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
		}

		@Override
		public ImportedVariate convertToObject(final Map<Integer, String> rowValues) throws FileParsingException {

			final ImportedVariate importedVariate = new ImportedVariate(rowValues.get(0).toUpperCase(), rowValues.get(1), rowValues.get(2),
					rowValues.get(3), rowValues.get(4), rowValues.get(5));

			final String property = importedVariate.getProperty() == null ? "" : importedVariate.getProperty().toUpperCase();

			try {
				if (GermplasmListParser.this.ontologyDataManager.isSeedAmountVariable(property)) {
					importedVariate.setSeedStockVariable(true);
					this.seedAmountVariate = importedVariate.getVariate();
					GermplasmListParser.LOG.debug("SEED STOCK :" + importedVariate.getProperty());
				} else if ("ATTRIBUTE".equals(property) || "PASSPORT".equals(property)) {
					this.attributeVariates.add(importedVariate.getVariate());
				}
			} catch (final MiddlewareQueryException e) {
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

		// we maintain set of Entry IDs for checking dupes
		private final Set<String> entrySet = new HashSet<>();
		private final String[] observationSheetHeaders;

		public ObservationRowConverter(final Workbook workbook, final int startingIndex, final int targetSheetIndex, final int columnCount,
				final String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels, false);
			this.observationSheetHeaders = columnLabels;
		}

		@Override
		public ImportedGermplasm convertToObject(final Map<Integer, String> rowValues) throws FileParsingException {
			final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
			for (final int colIndex : rowValues.keySet()) {
				final String colHeader = this.observationSheetHeaders[colIndex];

				// Map cell (given a column label) with a pojo setter

				final Map<FactorTypes, Command> factorBehaviors = new HashMap<>();
				factorBehaviors.put(FactorTypes.ENTRY, new Command() {

					@Override
					public void run() throws FileParsingException {
						final String entryId = rowValues.get(colIndex);
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
						final String designation = rowValues.get(colIndex);
						if (designation != null && designation.length() > 255) {
							throw new FileParsingException("GERMPLSM_PARSE_DESIGNATION_ERROR", ObservationRowConverter.this.currentIndex,
									"", colHeader);
						}
						importedGermplasm.setDesig(rowValues.get(colIndex));
					}
				});

				factorBehaviors.put(FactorTypes.GID, new Command() {

					@Override
					public void run() throws FileParsingException {
						final String val = rowValues.get(colIndex);

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
						if (!StringUtils.isBlank(rowValues.get(colIndex))
								&& !GermplasmListParser.this.importedGermplasmList.isHasStockIDValues()) {
							GermplasmListParser.this.importedGermplasmList.setHasStockIDValues(true);
						}
						importedGermplasm.setInventoryId(rowValues.get(colIndex));
					}
				});

				boolean shouldContinue = true;
				for (final Map.Entry<FactorTypes, Command> entry : factorBehaviors.entrySet()) {
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

				GermplasmListParser.LOG
						.debug(String.format("%s header is not recognized [parsing from row: %s]", colHeader, this.currentIndex));
			}

			// row based validation here
			// GID is given, but no DESIG, get value of DESIG given GID
			if (importedGermplasm.getGid() != null
					&& (importedGermplasm.getDesig() == null || StringUtils.isBlank(importedGermplasm.getDesig()))) {
				try {

					// Check if germplasm exists
					final Germplasm currentGermplasm =
							GermplasmListParser.this.germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid());
					if (currentGermplasm == null) {
						throw new FileParsingException("GERMPLSM_PARSE_DB_GID_NOT_EXISTS", this.currentIndex,
								importedGermplasm.getGid().toString(), GermplasmListParser.this.specialFactors.get(FactorTypes.GID));
					} else {

						final List<Integer> importedGermplasmGids = new ArrayList<>();
						importedGermplasmGids.add(importedGermplasm.getGid());

						final Map<Integer, String> preferredNames =
								GermplasmListParser.this.germplasmDataManager.getPreferredNamesByGids(importedGermplasmGids);

						if (preferredNames.get(importedGermplasm.getGid()) != null) {
							importedGermplasm.setDesig(preferredNames.get(importedGermplasm.getGid()));
						}

					}
				} catch (final MiddlewareQueryException e) {
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

		public boolean executeOnFactorMatch(final String header, final FactorTypes type, final Command e) throws FileParsingException {
			if (GermplasmListParser.this.specialFactors.containsKey(type)
					&& GermplasmListParser.this.specialFactors.get(type).equalsIgnoreCase(header)) {
				e.run();
				return true;
			}
			return false;
		}

		public boolean executeIfHasNameFactors(final String header, final String value, final ImportedGermplasm germplasmReference)
				throws FileParsingException {
			if (GermplasmListParser.this.nameFactors != null && GermplasmListParser.this.nameFactors.contains(header)) {
				germplasmReference.addNameFactor(header, value);
				return true;
			}

			return false;
		}

		public boolean executeIfIsAttributeVariate(final String header, final String value, final ImportedGermplasm germplasmReference)
				throws FileParsingException {
			if (GermplasmListParser.this.attributeVariates != null && GermplasmListParser.this.attributeVariates.contains(header)) {
				germplasmReference.addAttributeVariate(header, value);
				return true;
			}

			return false;
		}

		public boolean executeIfIsSeedAmountVariate(final String header, final String value, final ImportedGermplasm germplasmReference)
				throws FileParsingException {
			if (StringUtils.isBlank(header)) {
				return false;
			}

			if (GermplasmListParser.this.seedAmountVariate != null && GermplasmListParser.this.seedAmountVariate.equals(header)) {
				final Double seedAmountValue = StringUtils.isBlank(value) ? 0 : Double.valueOf(value);
				germplasmReference.setSeedAmount(seedAmountValue);
			} else {
				return false;
			}

			return true;
		}
	}

	/**
	 * This method is to verify inventory variable is missing or not. This method move from StockIdValidator as now we need to check
	 * empty/missing inventory variable not stockId.
	 */
	private void validateForMissingInventoryVariable(final String header, final ImportedGermplasmList importedGermplasmList)
			throws FileParsingException {
		if (importedGermplasmList.hasMissingInventoryVariable()) {
			throw new FileParsingException("GERMPLSM_PARSE_GID_MISSING_SEED_AMOUNT_VALUE", 0, "", header);
		}
	}

	boolean validateListType(final String listType) {
		final List<UserDefinedField> udFields = this.germplasmListManager.getGermplasmListTypes();
		for (final UserDefinedField udField : udFields) {
			if (udField.getFcode().equalsIgnoreCase(listType)) {
				return true;
			}
		}
		return false;
	}

	Set<String> getDescriptionVariableNames() {
		return this.descriptionVariableNames;
	}

	/**
	 * For Test Only
	 *
	 * @param seedAmountVariate
	 */
	void setSeedAmountVariate(final String seedAmountVariate) {
		this.seedAmountVariate = seedAmountVariate;
	}

	/**
	 * For Test Only
	 *
	 * @param importedGermplasmList
	 */
	void setImportedGermplasmList(final ImportedGermplasmList importedGermplasmList) {
		this.importedGermplasmList = importedGermplasmList;
	}

	/**
	 * For Test Only
	 *
	 * @param descriptionVariableNames
	 */
	void setDescriptionVariableNames(final Set<String> descriptionVariableNames) {
		this.descriptionVariableNames = descriptionVariableNames;
	}

	/**
	 * For Test Only
	 *
	 * @param importFileIsAdvanced
	 */
	void setImportFileIsAdvanced(final boolean importFileIsAdvanced) {
		this.importFileIsAdvanced = importFileIsAdvanced;
	}

	void setNameFactors(final List<String> nameFactors) {
		this.nameFactors = nameFactors;
	}

}
