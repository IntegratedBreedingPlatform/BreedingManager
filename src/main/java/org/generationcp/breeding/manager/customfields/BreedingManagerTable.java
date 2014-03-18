package org.generationcp.breeding.manager.customfields;

import com.vaadin.ui.Table;

public class BreedingManagerTable extends Table {

	private static final long serialVersionUID = 745102380412622592L;
	public BreedingManagerTable(int recordCount, int maxRecords){
		super();
		Integer pageLength = Math.min(recordCount, maxRecords);
		if (pageLength > 0){
			setPageLength(pageLength);
		}
	}
	
}
