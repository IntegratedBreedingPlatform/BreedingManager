package org.generationcp.browser.germplasm;



import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class GermplasmAttributesComponent  extends VerticalLayout {
	
	private Table tableAttribute;
	public GermplasmAttributesComponent(GermplasmIndexContainer DataIndexContainer, GermplasmDetailModel gDetailModel){
		
		IndexedContainer dataSourceAttribute=DataIndexContainer.getGermplasAttribute(gDetailModel);
		tableAttribute = new Table("",dataSourceAttribute);
		// selectable
		tableAttribute.setSelectable(true);
		tableAttribute.setMultiSelect(false);
		tableAttribute.setImmediate(true); // react at once when something is selected

		// turn on column reordering and collapsing
		tableAttribute.setColumnReorderingAllowed(true);
		tableAttribute.setColumnCollapsingAllowed(true);

		// set column headers
		tableAttribute.setColumnHeaders(new String[] {"Type","Name","Date","Location","Type Desc"});
		tableAttribute.setSizeFull();
		addComponent(tableAttribute);
		setSpacing(true);
		setMargin(true);
	}

}
