
package org.generationcp.breeding.manager.cross.study.h2h.main;

import java.util.Set;

import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.SelectGermplasmEntryDialog;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.ui.Window;

public class SpecifyGermplasmsComponentTest {

	@Test
	public void testSelectTestEntryButtonClickAction() {
		SpecifyGermplasmsComponent specifyGermplasm =
				new SpecifyGermplasmsComponent(Mockito.mock(HeadToHeadCrossStudyMain.class), Mockito.mock(TraitsAvailableComponent.class));
		SpecifyGermplasmsComponent mockScreen = Mockito.spy(specifyGermplasm);

		Window parentWindow = new Window();
		Mockito.doReturn(parentWindow).when(mockScreen).getWindow();

		mockScreen.selectTestEntryButtonClickAction();

		Set<Window> childWindows = parentWindow.getChildWindows();
		Assert.assertTrue("Only 1 child window attached", childWindows.size() == 1);
		Window selectWindow = childWindows.iterator().next();
		Assert.assertTrue("Child window is Select Germplasm Entry Dialog", selectWindow instanceof SelectGermplasmEntryDialog);
		SelectGermplasmEntryDialog selectDialog = (SelectGermplasmEntryDialog) selectWindow;
		Assert.assertTrue("Dialog is for selecting test entry", selectDialog.isTestEntry());
	}

	@Test
	public void testSelectStandardEntryButtonClickAction() {
		SpecifyGermplasmsComponent specifyGermplasm =
				new SpecifyGermplasmsComponent(Mockito.mock(HeadToHeadCrossStudyMain.class), Mockito.mock(TraitsAvailableComponent.class));
		SpecifyGermplasmsComponent mockScreen = Mockito.spy(specifyGermplasm);

		Window parentWindow = new Window();
		Mockito.doReturn(parentWindow).when(mockScreen).getWindow();

		mockScreen.selectStandardEntryButtonClickAction();

		Set<Window> childWindows = parentWindow.getChildWindows();
		Assert.assertTrue("Only 1 child window attached", childWindows.size() == 1);
		Window selectWindow = childWindows.iterator().next();
		Assert.assertTrue("Child window is Select Germplasm Entry Dialog", selectWindow instanceof SelectGermplasmEntryDialog);
		SelectGermplasmEntryDialog selectDialog = (SelectGermplasmEntryDialog) selectWindow;
		Assert.assertFalse("Dialog is for selecting test entry", selectDialog.isTestEntry());
	}

}
