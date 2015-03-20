package org.generationcp.breeding.manager.listmanager.util;

import java.util.List;
import java.util.Map.Entry;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.GermplasmListNewColumnsInfo;
import org.generationcp.middleware.domain.gms.ListDataColumnValues;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

/**
 * Reusable class for querying and displaying additional columns of a Germplasm 
 * List Data table
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
	
	public ListDataPropertiesRenderer(Integer listId, Table targetTable){
		this.listId = listId;
		this.targetTable = targetTable;
	}
	
	public void render() throws MiddlewareQueryException{
		GermplasmListNewColumnsInfo columnsInfo = listManager.getAdditionalColumnsForList(listId);
		for (Entry<String, List<ListDataColumnValues>> columnEntry: columnsInfo.getColumnValuesMap().entrySet()){
			String column = columnEntry.getKey();
			targetTable.addContainerProperty(column, String.class, "");
			targetTable.setColumnHeader(column, ColumnLabels.get(column).getTermNameFromOntology(ontologyDataManager));
			targetTable.setColumnWidth(column, 250);
			setColumnValues(column, columnEntry.getValue());
		}
		
	}
	
	public void setColumnValues(String column, List<ListDataColumnValues> columnValues){
		for (ListDataColumnValues columnValue : columnValues){
			Integer listDataId = columnValue.getListDataId();
			Item tableItem = targetTable.getItem(listDataId);
			if (tableItem != null){
				String value = columnValue.getValue();
				tableItem.getItemProperty(column).setValue(value == null ? "" : value);
			}
		}
    }
 

}
