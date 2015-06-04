
package org.generationcp.breeding.manager.customcomponent.generator;

import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Tree;

/**
 * Created by EfficioDaniel on 9/26/2014.
 */
@Configurable
public class GermplasmListSourceItemStyleGenerator implements Tree.ItemStyleGenerator {

	protected static final long serialVersionUID = -5690995097357568121L;
	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListSourceItemStyleGenerator.class);

	@Autowired
	protected GermplasmListManager germplasmListManager;

	@Override
	public String getStyle(Object itemId) {
		GermplasmList currentList = null;

		try {
			currentList = this.germplasmListManager.getGermplasmListById(Integer.valueOf(itemId.toString()));
		} catch (NumberFormatException e) {
			currentList = null;
		} catch (MiddlewareQueryException e) {
			GermplasmListSourceItemStyleGenerator.LOG.error("Erro with getting list by id: " + itemId, e);
			currentList = null;
		}

		if (itemId.equals(ListSelectorComponent.LISTS)) {
			return AppConstants.CssStyles.TREE_ROOT_NODE;
		} else if (currentList != null && currentList.getType().equals(AppConstants.DB.FOLDER)) {
			return AppConstants.CssStyles.TREE_REGULAR_PARENT_NODE;
		} else {
			return AppConstants.CssStyles.TREE_REGULAR_CHILD_NODE;
		}

	}

}
