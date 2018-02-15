
package org.generationcp.breeding.manager.listmanager.util;

import java.util.List;
import java.util.Map.Entry;

import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.GermplasmListNewColumnsInfo;
import org.generationcp.middleware.domain.gms.ListDataColumnValues;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

/**
 * Reusable class for querying and displaying additional columns of a Germplasm List Data table
 * 
 * @author Darla Ani
 * 
 */
@Configurable
public class ListDataPropertiesRenderer {

	@Autowired
	private GermplasmListManager listManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	private Integer listId;

	private Table targetTable;

	public ListDataPropertiesRenderer() {

	}

	public ListDataPropertiesRenderer(final Integer listId, final Table targetTable) {
		this.listId = listId;
		this.targetTable = targetTable;
	}

	public void render() {
		final GermplasmListNewColumnsInfo columnsInfo = this.listManager.getAdditionalColumnsForList(this.listId);
		for (final Entry<String, List<ListDataColumnValues>> columnEntry : columnsInfo.getColumnValuesMap().entrySet()) {
			final String column = columnEntry.getKey();
			this.targetTable.addContainerProperty(column, String.class, "");
			final String columnName;

			// Valid for germplasm attributes that not exits in ColumnLabels.
			if (ColumnLabels.get(column) != null) {
				columnName = ColumnLabels.get(column).getTermNameFromOntology(this.ontologyDataManager);
			}else{
				columnName = column;
			}

			this.targetTable.setColumnHeader(column, columnName);
			this.targetTable.setColumnWidth(column, 250);
			this.setColumnValues(column, columnEntry.getValue());
		}

	}

	public void setColumnValues(final String column, final List<ListDataColumnValues> columnValues) {
		for (final ListDataColumnValues columnValue : columnValues) {
			final Integer listDataId = columnValue.getListDataId();
			final Item tableItem = this.targetTable.getItem(listDataId);
			if (tableItem != null) {
				final String value = columnValue.getValue();
				tableItem.getItemProperty(column).setValue(value == null ? "" : value);
			}
		}
	}

	public void setListId(final Integer listId) {
		this.listId = listId;
	}

	public void setTargetTable(final Table targetTable) {
		this.targetTable = targetTable;
	}

}
