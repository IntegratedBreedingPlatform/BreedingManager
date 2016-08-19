
package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Arrays;

import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.customfields.ListTypeField;
import org.generationcp.breeding.manager.customfields.LocalListFoldersTreeComponent;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.ui.ComboBox;

import junit.framework.Assert;

public class SaveCrossListAsDialogTest {

	private static SaveCrossListAsDialog dialog;
	private static SaveListAsDialogSource source;
	private static ListTypeField listTypeField;

	@BeforeClass
	public static void beforeClass() {
		SaveCrossListAsDialogTest.source = Mockito.mock(SaveListAsDialogSource.class);
		SaveCrossListAsDialogTest.dialog =
				new SaveCrossListAsDialog(SaveCrossListAsDialogTest.source, GermplasmListTestDataInitializer.createGermplasmList(1));

		SaveCrossListAsDialogTest.listTypeField = new ListTypeField("", false);
		SaveCrossListAsDialogTest.listTypeField.setListTypeComboBox(new ComboBox("List Types", new ArrayList<>(
				Arrays.asList(GermplasmListType.CROSSES.name(), GermplasmListType.F1.name(), GermplasmListType.LST.name()))));
		final BreedingManagerListDetailsComponent listDetailsComponent = Mockito.mock(BreedingManagerListDetailsComponent.class);
		Mockito.when(listDetailsComponent.getListTypeField()).thenReturn(SaveCrossListAsDialogTest.listTypeField);
		SaveCrossListAsDialogTest.dialog.setListDetailsComponent(listDetailsComponent);

		SaveCrossListAsDialogTest.dialog.setGermplasmListTree(Mockito.mock(LocalListFoldersTreeComponent.class));
	}

	@Test
	public void testDefaultListType() {
		final String defaultListType = SaveCrossListAsDialogTest.dialog.defaultListType();
		Assert.assertEquals("The default list type should be " + GermplasmListType.F1.name(), GermplasmListType.F1.name(), defaultListType);
	}

	@Test
	public void testInitializeValues() {
		SaveCrossListAsDialogTest.dialog.initializeValues();
		final String listType = SaveCrossListAsDialogTest.dialog.getDetailsComponent().getListTypeField().getValue();
		Assert.assertEquals("The list type should be " + GermplasmListType.F1.name(), GermplasmListType.F1.name(), listType);
	}
}
