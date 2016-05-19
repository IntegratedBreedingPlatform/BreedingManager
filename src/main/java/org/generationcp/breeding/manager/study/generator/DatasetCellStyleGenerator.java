
package org.generationcp.breeding.manager.study.generator;

import org.generationcp.breeding.manager.study.containers.RepresentationDataSetQuery;

import com.vaadin.ui.Table;

/**
 * Created a new class for the cell styling of data
 */
public class DatasetCellStyleGenerator implements Table.CellStyleGenerator {

	/**
	 *
	 */
	private static final long serialVersionUID = 6410114406961928117L;
	private final Table table;

	public DatasetCellStyleGenerator(Table table) {
		this.table = table;
	}

	@Override
	public String getStyle(Object itemId, Object propertyId) {
		int row = ((Integer) itemId).intValue();
		String col = propertyId + RepresentationDataSetQuery.IS_ACCEPTED_VALUE_KEY;
		com.vaadin.data.Property itemProperty = this.table.getItem(row).getItemProperty(col);
		if (itemProperty != null) {
			Boolean isAcceptedValue = (Boolean) itemProperty.getValue();
			if (isAcceptedValue != null && isAcceptedValue.booleanValue()) {
				return "accepted-value";
			}
		}
		return "";
	}
}
