package org.generationcp.browser.germplasm;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class GermplasmNamesComponent  extends VerticalLayout {

	private Table tableNames;
	public GermplasmNamesComponent(GermplasmIndexContainer DataIndexContainer, GermplasmDetailModel gDetailModel){

		IndexedContainer dataSourceNames=DataIndexContainer.getGermplasNames(gDetailModel);
		tableNames = new Table("",dataSourceNames);
		tableNames.setSelectable(true);
		tableNames.setMultiSelect(false);
		tableNames.setImmediate(true); // react at once when something is selected
		// turn on column reordering and collapsing
		tableNames.setColumnReorderingAllowed(true);
		tableNames.setColumnCollapsingAllowed(true);
		// set column headers
		tableNames.setColumnHeaders(new String[] {"Type","Name","Date","Location","Type Desc"});
		tableNames.setSizeFull();

		addComponent(tableNames);
		setSpacing(true);
		setMargin(true);
	}

}
