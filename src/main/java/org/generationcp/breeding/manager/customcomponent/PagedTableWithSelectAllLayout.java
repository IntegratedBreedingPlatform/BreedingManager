package org.generationcp.breeding.manager.customcomponent;

/**
 * Created by cyrus on 05/05/2016.
 */
public class PagedTableWithSelectAllLayout extends TableWithSelectAllLayout {

	public PagedTableWithSelectAllLayout(int recordCount, int maxRecords, Object checkboxColumnId) {
		super(recordCount, maxRecords, checkboxColumnId);
	}

	public PagedTableWithSelectAllLayout(Object checkboxColumnId) {
		super(checkboxColumnId);
	}

	public PagedTableWithSelectAllLayout(int recordCount, Object checkboxColumnId) {
		super(recordCount, checkboxColumnId);
	}
}
