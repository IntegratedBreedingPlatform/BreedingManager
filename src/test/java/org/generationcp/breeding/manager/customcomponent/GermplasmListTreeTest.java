
package org.generationcp.breeding.manager.customcomponent;

import java.util.Collections;

import org.generationcp.breeding.manager.customfields.ListTreeComponent;
import org.generationcp.breeding.manager.customfields.LocalListFoldersTreeComponent;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.vaadin.ui.AbstractSelect;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListTreeTest {


	@Mock
	private UserService userService;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmListTree listTree;

	@InjectMocks
	private ListTreeComponent listTreeComponent = new LocalListFoldersTreeComponent(1);


	@Before
	public void setUp() {
		this.listTreeComponent.setGermplasmListSource(this.listTree);
	}

	@Test
	public void testLocalTreeItemDescription() {
		listTreeComponent.setGermplasmListManager(Mockito.mock(GermplasmListManager.class));
		listTreeComponent.setGermplasmDataManager(Mockito.mock(GermplasmDataManager.class));
		listTreeComponent.addGermplasmListNodeToComponent(Collections.EMPTY_LIST, 1);
		Mockito.verify(this.listTree).setItemDescriptionGenerator(Matchers.any(AbstractSelect.ItemDescriptionGenerator.class));
	}

}
