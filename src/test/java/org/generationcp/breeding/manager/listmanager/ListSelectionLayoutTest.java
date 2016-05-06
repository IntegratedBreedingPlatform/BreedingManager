
package org.generationcp.breeding.manager.listmanager;

import junit.framework.Assert;

import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ListSelectionLayoutTest {

	private static final Integer LIST_ID = 1;

	private ListSelectionLayout layout;
	private GermplasmList germplasmList;

	@Mock
	private ListManagerMain source;
	@Mock
	private ListBuilderComponent listBuilderComponent;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.layout = new ListSelectionLayout(this.source, LIST_ID);

		this.germplasmList = GermplasmListTestDataInitializer.createGermplasmList(LIST_ID);

		Mockito.doReturn(this.listBuilderComponent).when(this.source).getListBuilderComponent();
		Mockito.doReturn(this.germplasmList).when(this.listBuilderComponent).getCurrentListInSaveDialog();
		Mockito.doReturn(this.germplasmList).when(this.listBuilderComponent).getCurrentlySavedGermplasmList();

	}

	@Test
	public void testUpdateGermplasmListInListBuilder() {
		final String newName = "newName";
		this.layout.updateGermplasmListInListBuilder(LIST_ID, newName);
		Assert.assertEquals("Expecting that the name in list builder save dialog is updated after update in Browse For List dialog.",
				newName, this.germplasmList.getName());
	}
}
