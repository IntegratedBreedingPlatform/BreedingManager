
package org.generationcp.breeding.manager.customcomponent.generator;

import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;

/**
 * Created by EfficioDaniel on 9/26/2014.
 */
public class GermplasmListSourceItemDescriptionGenerator implements AbstractSelect.ItemDescriptionGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(ListSelectorComponent.class);

	private static final long serialVersionUID = -2669417630841097077L;
	private final ListSelectorComponent listSelectorComponent;

	public GermplasmListSourceItemDescriptionGenerator(ListSelectorComponent listSelectorComponent) {
		this.listSelectorComponent = listSelectorComponent;

	}

	@Override
	public String generateDescription(Component source, Object itemId, Object propertyId) {
		GermplasmList germplasmList;

		try {
			if (!itemId.toString().equals(ListSelectorComponent.LISTS)) {
				germplasmList = this.listSelectorComponent.getGermplasmListsMap().get(Integer.valueOf(itemId.toString()));
				if (germplasmList != null && !"FOLDER".equalsIgnoreCase(germplasmList.getType())) {
					ViewListHeaderWindow viewListHeaderWindow = new ViewListHeaderWindow(germplasmList);
					return viewListHeaderWindow.getListHeaderComponent().toString();
				}
			}
		} catch (NumberFormatException e) {
			GermplasmListSourceItemDescriptionGenerator.LOG.debug("Item Id is non numeric");
		}
		return "";
	}
}
