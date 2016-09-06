package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.ui.Table;

/**
 * This class subclasses the Vaadin Table class, in order to expose the ability 
 * to control when the Table is notified by changes to the table contents.
 * 
 * All BMS classes that subclass the Vaadin Table class should subclass this class 
 * instead and thereby can disable/enable the HTML rendering until all changes to the table contents are made.
 * 
 * This reduces significantly the time spent refreshing the entire table each time a new row is 
 * added (due to non-linear scaling).
 * 
 */

public class ControllableRefreshTable extends Table {

	private static final long serialVersionUID = 5077109972147858717L;

	@Override
	public boolean disableContentRefreshing() {
		return super.disableContentRefreshing();
	}

	@Override
	public void enableContentRefreshing(boolean refreshContent) {
		super.enableContentRefreshing(refreshContent);
	}
	
	

}
