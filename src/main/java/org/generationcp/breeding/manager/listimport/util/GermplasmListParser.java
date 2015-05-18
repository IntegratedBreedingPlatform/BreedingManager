package org.generationcp.breeding.manager.listimport.util;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
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
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;

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

	@Resource
	private InventoryDataManager inventoryDataManager;

	private int currentRowIndex = 0;
	private Map<Integer, String> observationColumnMap = new HashMap<>();

	private ImportedGermplasmList importedGermplasmList;
	private Map<FactorTypes, String> specialFactors;

	private String noInventoryWarning = "";
	private boolean importFileIsAdvanced = false;
	private String seedAmountVariate = "";
	private Set<String> nameFactors;
	private Set<String> attributeVariates;

	public String getNoInventoryWarning() {
		return noInventoryWarning;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public boolean hasInventoryAmountOnly() {
		return !seedAmountVariate.isEmpty() && !specialFactors.containsKey(FactorTypes.STOCK);
	}

	public boolean hasInventoryAmount() {
		return !seedAmountVariate.isEmpty();
	}

	public boolean hasStockIdFactor() {
		return specialFactors.containsKey(FactorTypes.STOCK);
	}

	public boolean isSeedAmountVariable(ImportedVariate variate) {
		return !seedAmountVariate.isEmpty() && seedAmountVariate.equals(variate.getVariate());
	}

	public boolean importFileIsAdvanced() {
		return importFileIsAdvanced;
	}

	enum ConditionHeaders {
		CONDITION("CONDITION"),
		DESCRIPTION("DESCRIPTION"),
		PROPERTY("PROPERTY"),
		SCALE("SCALE"),
		METHOD("METHOD");

		String label;

		ConditionHeaders(String label) {
			this.label = label;
		}

		public static String[] names() {
			ConditionHeaders[] values = values();
			String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum FactorHeaders {
		FACTOR("FACTOR"),
		DESCRIPTION("DESCRIPTION"),
		PROPERTY("PROPERTY"),
		SCALE("SCALE"),METHOD("METHOD");

		String label;

		FactorHeaders(String label) {
			this.label = label;
		}

		public static String[] names() {
			FactorHeaders[] values = values();
			String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum ConstantHeaders {
		CONSTANT("CONSTANT"),
		DESCRIPTION("DESCRIPTION"),
		PROPERTY("PROPERTY"),
		SCALE("SCALE"),
		METHOD("METHOD");

		String label;

		ConstantHeaders(String label) {
			this.label = label;
		}

		public static String[] names() {
			ConstantHeaders[] values = values();
			String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum VariateHeaders {
		VARIATE("VARIATE"),
		DESCRIPTION("DESCRIPTION"),
		PROPERTY("PROPERTY"),
		SCALE("SCALE"),
		METHOD("METHOD");

		String label;

		VariateHeaders(String label) {
			this.label = label;
		}

		public static String[] names() {
			VariateHeaders[] values = values();
			String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].label;
			}

			return names;
		}
	}

	enum FactorTypes {
		ENTRY,DESIG,GID,ENTRYCODE,SOURCE,CROSS,NAME,STOCK
	}

	@Override
	public ImportedGermplasmList parseWorkbook(Workbook workbook, Map<String,Object> additionalParams)
			throws FileParsingException {
		this.workbook = workbook;

		parseListDetails();
		parseConditions();
		parseFactors();
		parseConstants();
		parseVariates();
		parseObservationRows();
		return importedGermplasmList;
	}

	protected void parseVariates() throws FileParsingException  {
		// variate is optional so lets check first if its there
		if (!"VARIATE".equalsIgnoreCase(getCellStringValue(DESCRIPTION_SHEET_NO,currentRowIndex,0))) {
			return;
		}

		if ( isHeaderInvalid(currentRowIndex, DESCRIPTION_SHEET_NO, VariateHeaders.names()) ) {
			throw new FileParsingException("GERMPLASM_PARSE_VARIATE_HEADER_ERROR");
		}

		VariateDetailsConverter variateDetailsConverter = new VariateDetailsConverter(workbook,currentRowIndex + 1,DESCRIPTION_SHEET_NO,VariateHeaders.values().length,VariateHeaders.names());

		List<ImportedVariate> variateList = variateDetailsConverter.convertWorkbookRowsToObject(
				new WorkbookRowConverter.ContinueTillBlank());

		attributeVariates = variateDetailsConverter.getAttributeVariates();

		// if theres a stock id factor but no inventory column variate, we have to ignore the stock ids and treet it as a normal germplasm import
		// lets show a warning message after the import
		seedAmountVariate =  variateDetailsConverter.getSeedAmountVariate();

		if (seedAmountVariate.isEmpty() && specialFactors.containsKey(FactorTypes.STOCK)) {
			importedGermplasmList.removeImportedFactor(specialFactors.get(FactorTypes.STOCK));
			specialFactors.remove(FactorTypes.STOCK);

			noInventoryWarning = "StockIDs can only be added for germplasm if it has existing inventory in the BMS";
		}

		for (ImportedVariate variate : variateList) {
			importedGermplasmList.addImportedVariate(variate);
		}
	}

	protected void parseConstants() throws FileParsingException {
		// constants is optional so lets check first if its there
		if (!"CONSTANT".equalsIgnoreCase(getCellStringValue(DESCRIPTION_SHEET_NO,currentRowIndex,0))) {
			return;
		}

		if ( isHeaderInvalid(currentRowIndex, DESCRIPTION_SHEET_NO, ConstantHeaders.names()) ) {
			throw new FileParsingException("GERMPLASM_PARSE_CONSTANT_HEADER_ERROR");
		}

		ConstantsDetailsConverter constantsDetailsConverter = new ConstantsDetailsConverter(workbook,currentRowIndex + 1,DESCRIPTION_SHEET_NO,ConstantHeaders.values().length,ConstantHeaders.names());

		List<ImportedConstant> constantList = constantsDetailsConverter.convertWorkbookRowsToObject(
				new WorkbookRowConverter.ContinueTillBlank());

		for (ImportedConstant constant : constantList) {
			importedGermplasmList.addImportedConstant(constant);
		}

		// update current row index
		currentRowIndex = constantsDetailsConverter.getCurrentIndex();
		continueTillNextSection();
	}

	protected void parseFactors() throws FileParsingException {

		if ( isHeaderInvalid(currentRowIndex, DESCRIPTION_SHEET_NO, FactorHeaders.names()) ) {
			throw new FileParsingException("GERMPLASM_PARSE_FACTORS_HEADER_ERROR");
		}

		FactorDetailsConverter factorDetailsConverter = new FactorDetailsConverter(workbook,currentRowIndex + 1,DESCRIPTION_SHEET_NO,FactorHeaders.values().length,FactorHeaders.names());

		List<ImportedFactor> factorList = factorDetailsConverter.convertWorkbookRowsToObject(
				new WorkbookRowConverter.ContinueTillBlank());

		importFileIsAdvanced = factorDetailsConverter.isImportFileIsAdvanced();

		// validate all factor sets
		if (!factorDetailsConverter.specialFactors.containsKey(FactorTypes.ENTRY)) {
			throw new FileParsingException("GERMPLASM_PARSE_NO_ENTRY_FACTOR");
		} else if (!factorDetailsConverter.specialFactors.containsKey(FactorTypes.DESIG)) {
			throw new FileParsingException("GERMPLASM_PARSE_NO_DESIG_FACTOR");
		}

		specialFactors = factorDetailsConverter.getSpecialFactors();
		nameFactors = factorDetailsConverter.getNameFactors();
		for (ImportedFactor factor : factorList) {
			importedGermplasmList.addImportedFactor(factor);
		}

		// update current row index
		currentRowIndex = factorDetailsConverter.getCurrentIndex();
		continueTillNextSection();
	}

	protected void parseConditions()
			throws FileParsingException {

		if ( isHeaderInvalid(CONDITION_HEADER_ROW_INDEX, DESCRIPTION_SHEET_NO, ConditionHeaders.names()) ) {
			throw new FileParsingException("GERMPLASM_PARSE_CONDITION_HEADER_ERROR");
		}

		ConditionDetailsConverter conditionDetailsConverter = new ConditionDetailsConverter(workbook,
				CONDITION_HEADER_ROW_INDEX + 1,DESCRIPTION_SHEET_NO, ConditionHeaders.values().length,
				ConditionHeaders.names());

		List<ImportedCondition> conditionsList = conditionDetailsConverter.convertWorkbookRowsToObject(
				new WorkbookRowConverter.ContinueTillBlank());

		for (ImportedCondition condition : conditionsList) {
			importedGermplasmList.addImportedCondition(condition);
		}

		// update current row index
		currentRowIndex = conditionDetailsConverter.getCurrentIndex();
		continueTillNextSection();
	}

	protected void parseListDetails() throws FileParsingException {
		String listName = getCellStringValue(DESCRIPTION_SHEET_NO, 0, 1);
		String listTitle = getCellStringValue(DESCRIPTION_SHEET_NO, 1, 1);

		String labelId = getCellStringValue(DESCRIPTION_SHEET_NO, 2, 0);

		// we have this since listdate or listtype might switch in the template
		int listDateColNo = LIST_DATE.equalsIgnoreCase(labelId) ? 2 : 3;
		int listTypeColNo = LIST_TYPE.equalsIgnoreCase(labelId) ? 2 : 3;

		Date listDate;
		try {
			listDate = DateUtil.parseDate(
					getCellStringValue(DESCRIPTION_SHEET_NO, listDateColNo, 1));
		} catch (ParseException e) {
			throw new FileParsingException("GERMPLASM_PARSE_LIST_DATE_FORMAT_INVALID");
		}

		String listType = getCellStringValue(DESCRIPTION_SHEET_NO, listTypeColNo, 1);

		if (!TEMPLATE_LIST_TYPE.equalsIgnoreCase(listType)) {
			throw new FileParsingException("GERMPLASM_PARSE_LIST_TYPE_INVALID");
		}

		importedGermplasmList = new ImportedGermplasmList(originalFilename,listName,listTitle,listType,listDate);
	}

	/**
	 * This validator might be too strict for germplasm list parser
	 * @return ParseValidationMap
	 */
	protected ParseValidationMap parseObservationHeaders() throws FileParsingException {
		ParseValidationMap validationMap = new ParseValidationMap();

		final int headerSize = importedGermplasmList.sizeOfObservationHeader();

		boolean hasEntryColumn = false;
		boolean hasDesigColumn = false;
		boolean hasGidColumn = false;
		boolean hasStockId = false;
		boolean hasInventoryVariate = false;
		// were accounting for two additional unknown columns inserted between the headers, then we'll
		// just ignore it
		for (int i = 0; i < headerSize + 2; i++) {
			// search the current header
			String obsHeader = getCellStringValue(OBSERVATION_SHEET_NO, 0, i);


			// validation is only limited to existance of entry and desig factors
			if (specialFactors.get(FactorTypes.ENTRY).equals(obsHeader)) {
				validationMap.addValidation(i,new ValueTypeValidator(Integer.class));
				validationMap.addValidation(i,new NonEmptyValidator());

				hasEntryColumn = true;
			} else if (specialFactors.get(FactorTypes.DESIG).equals(obsHeader)) {
				hasDesigColumn = true;
			} else if (importFileIsAdvanced && specialFactors.get(FactorTypes.GID).equals(obsHeader)) {
				hasGidColumn = true;
			} else if (specialFactors.containsKey(FactorTypes.STOCK) && specialFactors.get(FactorTypes.STOCK).equals(obsHeader)) {
				hasStockId = true;
			} else if (!seedAmountVariate.isEmpty() && seedAmountVariate.equalsIgnoreCase(obsHeader)) {
				validationMap.addValidation(i,new ValueTypeValidator(Double.class));
				 hasInventoryVariate = true;
			}

			observationColumnMap.put(i, obsHeader);
		}

		if (!hasEntryColumn) {
			throw new FileParsingException("GERMPLASM_PARSE_ENTRY_COLUMN_MISSING");
		} else if (!hasGidColumn && !hasDesigColumn) {
			throw new FileParsingException("GERMPLASM_PARSE_DESIG_COLUMN_MISSING");
		} else if (importFileIsAdvanced && !hasGidColumn) {
			throw new FileParsingException("GERMPLASM_PARSE_GID_COLUMN_MISSING");
		} else if (specialFactors.containsKey(FactorTypes.STOCK) && !hasStockId) {
			throw new FileParsingException("GERMPLASM_PARSE_STOCK_COLUMN_MISSING");
		} else if (seedAmountVariate.isEmpty() && specialFactors.containsKey(FactorTypes.STOCK)
				|| !seedAmountVariate.isEmpty() && !hasInventoryVariate && specialFactors.containsKey(FactorTypes.STOCK)
				) {
			importedGermplasmList.removeImportedFactor(specialFactors.get(FactorTypes.STOCK));
			specialFactors.remove(FactorTypes.STOCK);
			seedAmountVariate = "";
			noInventoryWarning = "StockIDs can only be added for germplasm if it has existing inventory in the BMS";
		}

		return validationMap;
	}

	protected void parseObservationRows() throws FileParsingException {
		ParseValidationMap validationMap = parseObservationHeaders();
		ObservationRowConverter observationRowConverter = new ObservationRowConverter(workbook,1,1,observationColumnMap.size(),observationColumnMap.values().toArray(new String[observationColumnMap.size()]));
		observationRowConverter.setValidationMap(validationMap);

		List<ImportedGermplasm> importedGermplasms = observationRowConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		importedGermplasmList.setImportedGermplasms(importedGermplasms);
		validateForDuplicateStockIds();

		importedGermplasmList.normalizeGermplasmList();
	}

	private void continueTillNextSection() {
		// were limiting to 10 blank rows
		for (int i = 0;isRowEmpty(DESCRIPTION_SHEET_NO, currentRowIndex, DESCRIPTION_SHEET_COL_SIZE) && i < 10; i++) {
			currentRowIndex++;
		}
	}

	protected void validateForDuplicateStockIds() throws FileParsingException{
		if (!specialFactors.containsKey(FactorTypes.STOCK)) {
			return;
		}

		String possibleDuplicateStockId = importedGermplasmList.getDuplicateStockIdIfExists();
		if (!"".equals(possibleDuplicateStockId.trim())) {
			throw new FileParsingException("GERMPLASM_PARSE_DUPLICATE_STOCK_ID",0,possibleDuplicateStockId,specialFactors.get(FactorTypes.STOCK));
		}

		try {
			List<String> possibleExistingDBStockIds = inventoryDataManager.getSimilarStockIds(importedGermplasmList.getStockIdsAsList());
			if (!possibleExistingDBStockIds.isEmpty()) {
				throw new FileParsingException("GERMPLASM_PARSE_DUPLICATE_DB_STOCK_ID",0,
						StringUtils.abbreviate(StringUtils.join(possibleExistingDBStockIds, " "),20),specialFactors.get(FactorTypes.STOCK));
			}
		} catch (MiddlewareQueryException e) {
			throw new FileParsingException(e.getMessage());
		}
	}


	class ConditionDetailsConverter extends WorkbookRowConverter<ImportedCondition> {

		public ConditionDetailsConverter(Workbook workbook, int startingIndex, int targetSheetIndex,
				int columnCount, String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
		}

		@Override
		public ImportedCondition convertToObject(Map<Integer, String> rowValues)
				throws FileParsingException {
			return new ImportedCondition(rowValues.get(0),rowValues.get(1),rowValues.get(2),rowValues.get(3),rowValues.get(4),rowValues.get(5),rowValues.get(6),rowValues.get(7));

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


		private Map<FactorTypes,String> specialFactors = new HashMap<>();
		private Set<String> nameFactors = new HashSet<>();

		public FactorDetailsConverter(Workbook workbook, int startingIndex, int targetSheetIndex,
				int columnCount, String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
		}

		@Override
		public ImportedFactor convertToObject(Map<Integer, String> rowValues)
				throws FileParsingException {
			ImportedFactor importedFactor = new ImportedFactor(rowValues.get(0),rowValues.get(1),rowValues.get(2),rowValues.get(3),rowValues.get(4),rowValues.get(5),rowValues.get(6),rowValues.get(7));

			 // row based validations here
			String property = importedFactor.getProperty() == null ? "" :
					importedFactor.getProperty().toUpperCase();
			String scale = importedFactor.getScale() == null ? "" :
					importedFactor.getScale().toUpperCase();
			String method = importedFactor.getMethod() == null ? "" :
					importedFactor.getMethod().toUpperCase();

			if (GERMPLASM_ENTRY_PROPERTY.equals(property) && NUMBER_SCALE.equals(scale)) {
				specialFactors.put(FactorTypes.ENTRY,importedFactor.getFactor());
			} else if (GERMPLASM_ID_PROPERTY.equals(property) && DBCV_SCALE.equals(scale)) {
				specialFactors.put(FactorTypes.DESIG,importedFactor.getFactor());
			} else if (GERMPLASM_ID_PROPERTY.equals(property) && DBID_SCALE.equals(scale)) {
				specialFactors.put(FactorTypes.GID,importedFactor.getFactor());
				importFileIsAdvanced= true;
			} else if (GERMPLASM_ENTRY_PROPERTY.equals(property) && CODE_SCALE.equals(scale)) {
				specialFactors.put(FactorTypes.ENTRYCODE,importedFactor.getFactor());
			} else if (SEED_SOURCE_PROPERTY.equals(property) && NAME_SCALE.equals(scale)) {
				specialFactors.put(FactorTypes.SOURCE, importedFactor.getFactor());
			} else if (CROSS_NAME_PROPERTY.equals(property) && NAME_SCALE.equals(scale)) {
				specialFactors.put(FactorTypes.CROSS,importedFactor.getFactor());
			} else if (GERMPLASM_STOCK_ID_PROPERTY.equals(property) && DBCV_SCALE.equals(scale)) {
				specialFactors.put(FactorTypes.STOCK,importedFactor.getFactor());
			} else if (GERMPLASM_ID_PROPERTY.equals(property) && NAME_SCALE.equals(scale) && ASSIGNED_METHOD
					.equals(method)) {
				nameFactors.add(importedFactor.getFactor());
			}

			return importedFactor;
		}


		public Map<FactorTypes, String> getSpecialFactors() {
			return specialFactors;
		}

		public Set<String> getNameFactors() {
			return nameFactors;
		}

		public boolean isImportFileIsAdvanced() {
			return importFileIsAdvanced;
		}
	}

	class ConstantsDetailsConverter extends WorkbookRowConverter<ImportedConstant> {

		public ConstantsDetailsConverter(Workbook workbook, int startingIndex, int targetSheetIndex,
				int columnCount, String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
		}

		@Override
		public ImportedConstant convertToObject(Map<Integer, String> rowValues)
				throws FileParsingException {
			return new ImportedConstant(rowValues.get(0),rowValues.get(1),rowValues.get(2),rowValues.get(3),rowValues.get(4),rowValues.get(5),rowValues.get(6),rowValues.get(7));
		}
	}

	class VariateDetailsConverter extends WorkbookRowConverter<ImportedVariate> {

		private String seedAmountVariate = "";
		private Set<String> attributeVariates = new HashSet<>();

		public VariateDetailsConverter(Workbook workbook, int startingIndex, int targetSheetIndex,
				int columnCount, String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
		}

		@Override
		public ImportedVariate convertToObject(Map<Integer, String> rowValues)
				throws FileParsingException {


			ImportedVariate importedVariate = new ImportedVariate(rowValues.get(0),rowValues.get(1),rowValues.get(2),rowValues.get(3),rowValues.get(4),rowValues.get(5));

			String property = importedVariate.getProperty() == null ? "" :
					importedVariate.getProperty().toUpperCase();

			try {
				if (ontologyDataManager.isSeedAmountVariable(property)) {
					importedVariate.setSeedStockVariable(true);
					seedAmountVariate = importedVariate.getVariate();
					LOG.debug("SEED STOCK :" + importedVariate.getProperty());
				} else if ("ATTRIBUTE".equals(property) || "PASSPORT".equals(property)) {
					attributeVariates.add(importedVariate.getVariate());
				}
			} catch (MiddlewareQueryException e) {
				LOG.error("SEED STOCK " + importedVariate.getProperty());
			}

			return importedVariate;
		}

		public String getSeedAmountVariate() {
			return seedAmountVariate;
		}

		public Set<String> getAttributeVariates() {
			return attributeVariates;
		}
	}

	class ObservationRowConverter extends WorkbookRowConverter<ImportedGermplasm> {
		// we maintain an entrySet for checking dupes
		private final Set<String> entrySet = new HashSet<>();

		public ObservationRowConverter(Workbook workbook, int startingIndex, int targetSheetIndex,
				int columnCount, String[] columnLabels) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels,false);
		}

		@Override
		public ImportedGermplasm convertToObject(final Map<Integer, String> rowValues)
				throws FileParsingException {
			final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
			for (final int colIndex : rowValues.keySet()) {
				String colHeader = observationColumnMap.get(colIndex);
				//Map cell (given a column label) with a pojo setter

				Map<FactorTypes,Command> factorBehaviors = new HashMap<>();
				factorBehaviors.put(FactorTypes.ENTRY, new Command() {
					@Override public void run() throws FileParsingException {
						String entryId = rowValues.get(colIndex);
						if (!entrySet.contains(entryId)) {
							entrySet.add(entryId);
							importedGermplasm.setEntryId(Integer.valueOf(entryId));
						} else {
							throw new FileParsingException("GERMPLASM_PARSE_DUPLICATE_ENTRY");
						}
					}
				});

				factorBehaviors.put(FactorTypes.DESIG, new Command() {
					@Override public void run() throws FileParsingException {
						importedGermplasm.setDesig(rowValues.get(colIndex));
					}
				});

				factorBehaviors.put(FactorTypes.GID, new Command() {
					@Override public void run() throws FileParsingException {
						String val = rowValues.get(colIndex);

						if (val.matches("^-?\\d+$")) {
							importedGermplasm.setGid(Integer.valueOf(val));
						}
					}
				});

				factorBehaviors.put(FactorTypes.CROSS, new Command() {
					@Override public void run() throws FileParsingException {
						importedGermplasm.setCross(rowValues.get(colIndex));
					}
				});

				factorBehaviors.put(FactorTypes.SOURCE, new Command() {
					@Override public void run() throws FileParsingException {
						importedGermplasm.setSource(rowValues.get(colIndex));
					}
				});

				factorBehaviors.put(FactorTypes.ENTRYCODE, new Command() {
					@Override public void run() throws FileParsingException {
						importedGermplasm.setEntryCode(rowValues.get(colIndex));
					}
				});

				factorBehaviors.put(FactorTypes.STOCK, new Command() {
					@Override public void run() throws FileParsingException {
						importedGermplasm.setInventoryId(rowValues.get(colIndex));
					}
				});

				boolean shouldContinue = true;
				for (Map.Entry<FactorTypes,Command> entry : factorBehaviors.entrySet()) {
					if (executeOnFactorMatch(colHeader,entry.getKey(),entry.getValue())) {
						shouldContinue = false;
						break;
					}
				}

				if (!shouldContinue) {
					continue;
				}

				if (executeIfHasNameFactors(colHeader,
						rowValues.get(colIndex), importedGermplasm)) {
					continue;
				}

				if (executeIfIsAttributeVariate(colHeader, rowValues.get(colIndex),
						importedGermplasm)) {
					continue;
				}

				if (executeIfIsSeedAmountVariate(colHeader, rowValues.get(colIndex),
						importedGermplasm)) {
					continue;
				}

				LOG.debug(String.format("%s header is not recognized [parsing from row: %s]",
						colHeader, currentIndex));
			}

			// row based validation here
			//GID is given, but no DESIG, get value of DESIG given GID
			if (importedGermplasm.getGid() != null && (importedGermplasm.getDesig() == null || "".equals(importedGermplasm.getDesig()))) {
				try {

					//Check if germplasm exists
					Germplasm currentGermplasm = germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid());
					if (currentGermplasm == null) {
						throw new FileParsingException("GERMPLSM_PARSE_DB_GID_NOT_EXISTS",currentIndex,importedGermplasm.getGid().toString(),specialFactors.get(FactorTypes.GID));
					} else {

						List<Integer> importedGermplasmGids = new ArrayList<>();
						importedGermplasmGids.add(importedGermplasm.getGid());

						Map<Integer, String> preferredNames = germplasmDataManager.getPreferredNamesByGids(importedGermplasmGids);

						if (preferredNames.get(importedGermplasm.getGid()) != null) {
							importedGermplasm.setDesig(preferredNames.get(importedGermplasm.getGid()));
						}

					}
				} catch (MiddlewareQueryException e) {
					LOG.error(e.getMessage(),e);
				}

				//GID is not given or 0, and DESIG is not given
			} else if ((importedGermplasm.getGid() == null || importedGermplasm.getGid().equals(Integer.valueOf(0)))
					&& (importedGermplasm.getDesig() == null || importedGermplasm.getDesig().length() == 0)) {
				throw new FileParsingException("GERMPLSM_PARSE_GID_DESIG_NOT_EXISTS",currentIndex,"",specialFactors.get(FactorTypes.GID));
			}

			return importedGermplasm;
		}

		public boolean executeOnFactorMatch(String header,FactorTypes type,Command e) throws FileParsingException {
			if (specialFactors.containsKey(type) && specialFactors.get(type).equalsIgnoreCase(header)) {
				e.run();
				return true;
			}
			return false;
		}

		public boolean executeIfHasNameFactors(String header,String value,ImportedGermplasm germplasmReference) throws FileParsingException {
			if (nameFactors.contains(header)) {
				germplasmReference.addNameFactor(header,value);
				return true;
			}

			return false;
		}

		public boolean executeIfIsAttributeVariate(String header,String value,ImportedGermplasm germplasmReference) throws FileParsingException {
			if (attributeVariates.contains(header)) {
				germplasmReference.addAttributeVariate(header,value);
				return true;
			}

			return false;
		}

		public boolean executeIfIsSeedAmountVariate(String header,String value,ImportedGermplasm germplasmReference) throws FileParsingException {
			if ("".equals(header)) {
				return false;
			}

			if (seedAmountVariate.equals(header) && specialFactors.containsKey(FactorTypes.STOCK) && "".equals(value)) {
				noInventoryWarning = "StockIDs can only be added for germplasm if it has existing inventory in the BMS, or inventory"
						+ " is being added in the import. Some of the StockIDs in this import file do not meet there requirements and will be ignored";
			}

			if (seedAmountVariate.equals(header)) {

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
