
package org.generationcp.breeding.manager.customcomponent;

import java.util.Collections;

import org.generationcp.breeding.manager.customfields.ListTreeComponent;
import org.generationcp.breeding.manager.customfields.LocalListFoldersTreeComponent;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.vaadin.ui.AbstractSelect;

public class GermplasmListTreeTest {

	private GermplasmListTree listTree;
	private ListTreeComponent listTreeComponent;

	@Before
	public void setUp() {
		this.listTree = Mockito.mock(GermplasmListTree.class);

		this.listTreeComponent = new LocalListFoldersTreeComponent(1);
		this.listTreeComponent.setGermplasmListSource(this.listTree);
	}

	@Test
	public void testLocalTreeItemDescription() {
		listTreeComponent.setGermplasmListManager(Mockito.mock(GermplasmListManager.class));
		listTreeComponent.setUserDataManager(Mockito.mock(UserDataManager.class));
		listTreeComponent.setGermplasmDataManager(Mockito.mock(GermplasmDataManager.class));
		listTreeComponent.addGermplasmListNodeToComponent(Collections.EMPTY_LIST, 1);
		Mockito.verify(this.listTree).setItemDescriptionGenerator(Matchers.any(AbstractSelect.ItemDescriptionGenerator.class));
	}

}
