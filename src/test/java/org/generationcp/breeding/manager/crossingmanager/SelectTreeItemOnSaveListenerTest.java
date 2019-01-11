package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.crossingmanager.listeners.SelectTreeItemOnSaveListener;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.listeners.SaveListButtonClickListener;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SelectTreeItemOnSaveListenerTest {

	@Mock
	private SaveListAsDialog saveListAsDialog;

	@Mock
	private ListBuilderComponent listBuilderComponent;

	private SelectTreeItemOnSaveListener selectTreeItemOnSaveListener;

	@Before
	public void init() {

		this.selectTreeItemOnSaveListener = new SelectTreeItemOnSaveListener(this.saveListAsDialog, this.listBuilderComponent);

	}

	@Test
	public void testStudyClickedGermplasListIsListType() {

		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setType(GermplasmList.LIST_TYPE);

		final SaveListButtonClickListener listener = Mockito.mock(SaveListButtonClickListener.class);
		final BreedingManagerListDetailsComponent detailsComponent = Mockito.mock(BreedingManagerListDetailsComponent.class);

		Mockito.when(this.listBuilderComponent.getSaveListButtonListener()).thenReturn(listener);
		Mockito.when(this.saveListAsDialog.getSource()).thenReturn(this.listBuilderComponent);
		Mockito.when(this.saveListAsDialog.getDetailsComponent()).thenReturn(detailsComponent);

		this.selectTreeItemOnSaveListener.studyClicked(germplasmList);

		Mockito.verify(detailsComponent).populateGermplasmListDetails(germplasmList);
		Mockito.verify(listener).setForceHasChanges(true);

	}

	@Test
	public void testStudyClickedGermplasListIsFolderType() {

		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setType(GermplasmList.FOLDER_TYPE);

		final SaveListButtonClickListener listener = Mockito.mock(SaveListButtonClickListener.class);
		final BreedingManagerListDetailsComponent detailsComponent = Mockito.mock(BreedingManagerListDetailsComponent.class);

		this.selectTreeItemOnSaveListener.studyClicked(germplasmList);

		Mockito.verify(detailsComponent, Mockito.never()).populateGermplasmListDetails(germplasmList);
		Mockito.verify(listener, Mockito.never()).setForceHasChanges(true);

	}

}
