package org.generationcp.breeding.manager.listimport.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.WorkbookRowConverter;
import org.generationcp.commons.parsing.pojo.ImportedFactor;

/**
 * Converts parsed factors into ImportedFactor object
 */
class FactorDetailsConverter extends WorkbookRowConverter<ImportedFactor> {

	public static final String GERMPLASM_ENTRY_PROPERTY = "GERMPLASM ENTRY";
	public static final String GERMPLASM_ID_PROPERTY = "GERMPLASM ID";
	public static final String SEED_SOURCE_PROPERTY = "SEED SOURCE";
	public static final String CROSS_NAME_PROPERTY = "CROSS NAME";
	public static final String CROSS_HISTORY_PROPERTY = "CROSS HISTORY";
	public static final String GERMPLASM_STOCK_ID_PROPERTY = "GERMPLASM STOCK ID";
	public static final String NUMBER_SCALE = "NUMBER";
	public static final String DBCV_SCALE = "DBCV";
	public static final String DBID_SCALE = "DBID";
	public static final String CODE_SCALE = "CODE";
	public static final String NAME_SCALE = "NAME";
	public static final String TEXT_SCALE = "TEXT";
	public static final String ASSIGNED_METHOD = "ASSIGNED";
	public static final String GERMPLASM_NAME = "GERMPLASM NAME";
	public static final String GERMPLASM_ID = "GERMPLASM ID";

	private final Map<GermplasmListParser.FactorTypes, String> specialFactors = new HashMap<>();
	private final Set<String> nameFactors = new TreeSet<>();

	private boolean importFileIsAdvanced = false;

	public FactorDetailsConverter(final Workbook workbook, final int startingIndex,
			final int targetSheetIndex, final int columnCount, final String[] columnLabels) {
		super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
	}

	public static boolean isGermplasmNameScale(String scale) {
		return FactorDetailsConverter.DBCV_SCALE.equals(scale) || FactorDetailsConverter.GERMPLASM_NAME.equals(scale);
	}

	public static boolean isGermplasmIdScale(String scale) {
		return FactorDetailsConverter.DBID_SCALE.equals(scale) || FactorDetailsConverter.GERMPLASM_ID.equals(scale);
	}

	public static boolean isStockIdScale(String scale) {
		return FactorDetailsConverter.DBCV_SCALE.equals(scale) || FactorDetailsConverter.GERMPLASM_ID.equals(scale);
	}

	public static boolean isCrossScale(String scale) {
		return FactorDetailsConverter.NAME_SCALE.equals(scale) || FactorDetailsConverter.TEXT_SCALE.equals(scale);
	}

	public static boolean isSeedSourceScale(String scale) {
		return FactorDetailsConverter.NAME_SCALE.equals(scale) || FactorDetailsConverter.isCodeScale(scale);
	}

	public static boolean isCrossNameProperty(String property) {
		return FactorDetailsConverter.CROSS_NAME_PROPERTY.equals(property) || FactorDetailsConverter.CROSS_HISTORY_PROPERTY.equals(property);
	}


	public static boolean isCodeScale(String scale) {
		return scale != null && scale.contains(FactorDetailsConverter.CODE_SCALE);
	}

	@Override
	public ImportedFactor convertToObject(final Map<Integer, String> rowValues) throws FileParsingException {
		final ImportedFactor importedFactor =
				new ImportedFactor(rowValues.get(0).toUpperCase(), rowValues.get(1), rowValues.get(2), rowValues.get(3), rowValues.get(4),
						rowValues.get(5), rowValues.get(6), rowValues.get(7));

		// row based validations here
		final String property = importedFactor.getProperty() == null ? "" : importedFactor.getProperty().toUpperCase();
		final String scale = importedFactor.getScale() == null ? "" : importedFactor.getScale().toUpperCase();
		final String method = importedFactor.getMethod() == null ? "" : importedFactor.getMethod().toUpperCase();

		if (FactorDetailsConverter.GERMPLASM_ENTRY_PROPERTY.equals(property) && FactorDetailsConverter.NUMBER_SCALE.equals(scale)) {
			this.specialFactors.put(GermplasmListParser.FactorTypes.ENTRY, importedFactor.getFactor());
		} else if (FactorDetailsConverter.GERMPLASM_ID_PROPERTY.equals(property) && FactorDetailsConverter.isGermplasmNameScale(scale)) {
			this.specialFactors.put(GermplasmListParser.FactorTypes.DESIG, importedFactor.getFactor());
		} else if (FactorDetailsConverter.GERMPLASM_ID_PROPERTY.equals(property) && FactorDetailsConverter.isGermplasmIdScale(scale)) {
			this.specialFactors.put(GermplasmListParser.FactorTypes.GID, importedFactor.getFactor());
			this.importFileIsAdvanced = true;
		} else if (FactorDetailsConverter.GERMPLASM_ENTRY_PROPERTY.equals(property) && FactorDetailsConverter.isCodeScale(scale)) {
			this.specialFactors.put(GermplasmListParser.FactorTypes.ENTRYCODE, importedFactor.getFactor());
		} else if (FactorDetailsConverter.SEED_SOURCE_PROPERTY.equals(property) && FactorDetailsConverter.isSeedSourceScale(scale)) {
			this.specialFactors.put(GermplasmListParser.FactorTypes.SOURCE, importedFactor.getFactor());
		} else if (FactorDetailsConverter.isCrossNameProperty(property) && FactorDetailsConverter.isCrossScale(scale)) {
			this.specialFactors.put(GermplasmListParser.FactorTypes.CROSS, importedFactor.getFactor());
		} else if (FactorDetailsConverter.GERMPLASM_STOCK_ID_PROPERTY.equals(property) && FactorDetailsConverter.isStockIdScale(scale)) {
			this.specialFactors.put(GermplasmListParser.FactorTypes.STOCK, importedFactor.getFactor());
		} else if (FactorDetailsConverter.NAME_SCALE.equals(scale) && FactorDetailsConverter.ASSIGNED_METHOD.equals(method)) {
			this.nameFactors.add(importedFactor.getFactor());
		}

		return importedFactor;
	}

	public Map<GermplasmListParser.FactorTypes, String> getSpecialFactors() {
		return this.specialFactors;
	}

	public List<String> getNameFactors() {
		return new ArrayList<>(this.nameFactors);
	}

	public boolean isImportFileIsAdvanced() {
		return this.importFileIsAdvanced;
	}

	public boolean hasSpecialFactor(GermplasmListParser.FactorTypes factorType) {
		return this.specialFactors.containsKey(factorType);
	}
}
