package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.listmanager.api.FillColumnSource;
import org.generationcp.commons.constant.ColumnLabels;

public class AddedColumnsMapper {
	
	private GermplasmColumnValuesGenerator valuesGenerator;
	
	public AddedColumnsMapper(FillColumnSource fillWithSource) {
		super();
		this.valuesGenerator = new GermplasmColumnValuesGenerator(fillWithSource);
	}
	
	public void generateValuesForAddedColumns(final Object[] visibleColumns) {
		if (this.isColumnVisible(visibleColumns, ColumnLabels.PREFERRED_ID.getName())) {
			valuesGenerator.setPreferredIdColumnValues();
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.GERMPLASM_LOCATION.getName())) {
			valuesGenerator.setLocationNameColumnValues();
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.PREFERRED_NAME.getName())) {
			valuesGenerator.setPreferredNameColumnValues();
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.GERMPLASM_DATE.getName())) {
			valuesGenerator.setGermplasmDateColumnValues();
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.BREEDING_METHOD_NAME.getName())) {
			valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NAME.getName());
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
			valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
			valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NUMBER.getName());
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
			valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_GROUP.getName());
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.CROSS_FEMALE_GID.getName())) {
			valuesGenerator.setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_GID.getName());
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
			valuesGenerator.setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.CROSS_MALE_GID.getName())) {
			valuesGenerator.setCrossMaleGIDColumnValues();
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())) {
			valuesGenerator.setCrossMalePrefNameColumnValues();
		}
	}
	
	private boolean isColumnVisible(final Object[] columns, final String columnName) {

		for (final Object col : columns) {
			if (col.equals(columnName)) {
				return true;
			}
		}

		return false;
	}
	
	
	

}
