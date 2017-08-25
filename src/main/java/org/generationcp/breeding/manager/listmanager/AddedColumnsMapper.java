package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.listmanager.api.FillColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.constant.ColumnLabels;

public class AddedColumnsMapper {
	
	private GermplasmColumnValuesGenerator valuesGenerator;
	
	public AddedColumnsMapper(FillColumnSource fillWithSource) {
		super();
		this.valuesGenerator = new GermplasmColumnValuesGenerator(fillWithSource);
	}
	
	public void generateValuesForAddedColumns(final Object[] visibleColumns) {
		if (this.isColumnVisible(visibleColumns, ColumnLabels.PREFERRED_ID.getName())) {
			valuesGenerator.setPreferredIdColumnValues(ColumnLabels.PREFERRED_ID.getName());
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.GERMPLASM_LOCATION.getName())) {
			valuesGenerator.setLocationNameColumnValues(ColumnLabels.GERMPLASM_LOCATION.getName());
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.PREFERRED_NAME.getName())) {
			valuesGenerator.setPreferredNameColumnValues(ColumnLabels.PREFERRED_NAME.getName());
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.GERMPLASM_DATE.getName())) {
			valuesGenerator.setGermplasmDateColumnValues(ColumnLabels.GERMPLASM_DATE.getName());
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.BREEDING_METHOD_NAME.getName())) {
			valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NAME.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
			valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV);
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
			valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NUMBER.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER);
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
			valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_GROUP.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.CROSS_FEMALE_GID.getName())) {
			valuesGenerator.setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_GID.getName(),
					FillWithOption.FILL_WITH_CROSS_FEMALE_GID);
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
			valuesGenerator.setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(),
					FillWithOption.FILL_WITH_CROSS_FEMALE_NAME);
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.CROSS_MALE_GID.getName())) {
			valuesGenerator.setCrossMaleGIDColumnValues(ColumnLabels.CROSS_MALE_GID.getName());
		}
		if (this.isColumnVisible(visibleColumns, ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())) {
			valuesGenerator.setCrossMalePrefNameColumnValues(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());
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
