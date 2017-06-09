
package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.middleware.data.initializer.NameTestDataInitializer;
import org.generationcp.middleware.pojos.Name;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Window;

import junit.framework.Assert;

public class NewDesignationForGermplasmConfirmDialogTest {

	private static final String DESIGNATION = "NEW NAME";

	private static final Integer GID = 10;

	private static final Integer INDEX = 5;

	@Mock
	private ProcessImportedGermplasmAction processImportedGermplasmAction;

	@Mock
	private Window parentWindow;

	private NewDesignationForGermplasmConfirmDialog dialog;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.dialog = new NewDesignationForGermplasmConfirmDialog(this.processImportedGermplasmAction,
				NewDesignationForGermplasmConfirmDialogTest.DESIGNATION, NewDesignationForGermplasmConfirmDialogTest.INDEX,
				NewDesignationForGermplasmConfirmDialogTest.GID, 0, 0, 0);
		this.dialog.instantiateComponents();
		this.dialog.addListeners();
		this.dialog.setParent(this.parentWindow);
	}

	@Test
	public void testAddNameButtonClick() {
		Mockito.when(this.processImportedGermplasmAction.createNameObject(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyString()))
				.thenReturn(NameTestDataInitializer.createName(1, NewDesignationForGermplasmConfirmDialogTest.DESIGNATION));

		this.dialog.getAddNameButton().click();

		final ArgumentCaptor<Name> nameCaptor = ArgumentCaptor.forClass(Name.class);
		final ArgumentCaptor<Integer> gidCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
		Mockito.verify(this.processImportedGermplasmAction).addNameToGermplasm(nameCaptor.capture(), gidCaptor.capture(),
				indexCaptor.capture());
		Assert.assertEquals("Expecting correct GID to be set in name to be added.", NewDesignationForGermplasmConfirmDialogTest.GID,
				nameCaptor.getValue().getGermplasmId());
		Assert.assertEquals("Expecting correct Designation to be set in name to be added.",
				NewDesignationForGermplasmConfirmDialogTest.DESIGNATION, nameCaptor.getValue().getNval());
		Assert.assertEquals("Expecting correct GID parameter sent back to ProcessImportedGermplasmAction.",
				NewDesignationForGermplasmConfirmDialogTest.GID, gidCaptor.getValue());
		Assert.assertEquals("Expecting correct Index parameter sent back to ProcessImportedGermplasmAction.",
				NewDesignationForGermplasmConfirmDialogTest.INDEX, indexCaptor.getValue());
		Mockito.verify(this.processImportedGermplasmAction).removeCurrentListenerAndProcessNextItem(this.dialog);
		Mockito.verify(this.parentWindow).removeWindow(this.dialog);
	}

	@Test
	public void testSearchCreateButtonClick() {
		this.dialog.getSearchCreateButton().click();

		Mockito.verify(this.processImportedGermplasmAction).searchOrAddANewGermplasm(this.dialog);
		Mockito.verify(this.parentWindow).removeWindow(this.dialog);
	}

}
