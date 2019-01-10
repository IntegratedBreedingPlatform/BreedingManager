package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;

import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.application.BreedingManagerWindowGenerator;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.service.BreedingManagerServiceImpl;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.dms.ProgramFavorite;
import org.generationcp.middleware.pojos.dms.ProgramFavorite.FavoriteType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BreedingMethodFieldTest {

	private static final String PROGRAM_UUID = "1234567890";
	public static final long PROJECT_ID = 1L;
	public static final String MANAGE_METHODS_LABEL = "Manage Methods";

	@Mock
	GermplasmDataManager germplasmDataManager;

	@Mock
	WorkbenchDataManager workbenchDataManager;

	@Mock
	SimpleResourceBundleMessageSource messageSource;

	@Mock
	ContextUtil contextUtil;

	@Mock
	private BreedingManagerServiceImpl service;

	@Mock
	private BreedingManagerWindowGenerator breedingManagerWindowGenerator;

	@InjectMocks
	BreedingMethodField breedingMethodField = new BreedingMethodField();

	@Before
	public void setUp() throws MiddlewareQueryException {
		MockitoAnnotations.initMocks(this);

		final Project project = this.getProject(PROJECT_ID);

		Mockito.when(this.service.getCurrentProject()).thenReturn(project);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);
		Mockito.when(this.messageSource.getMessage(Message.MANAGE_METHODS)).thenReturn(MANAGE_METHODS_LABEL);

		breedingMethodField.instantiateComponents();
	}

	private Project getProject(long id) {
		Project project = new Project();
		project.setProjectId(id);
		project.setUniqueID(BreedingMethodFieldTest.PROGRAM_UUID);
		return project;
	}

	@Test
	public void testinitPopulateFavMethodReturnsFalseWhenThereHasDefaultValue() throws MiddlewareQueryException {

		breedingMethodField.setHasDefaultValue(true);
		Assert.assertFalse("Expecting a false return when there is default value.",
				breedingMethodField.initPopulateFavMethod(BreedingMethodFieldTest.PROGRAM_UUID));
	}

	@Test
	public void testinitPopulateFavMethodReturnsFalseWhenThereAreNoFavouriteMethodAndHasNoDefaultValue() throws MiddlewareQueryException {

		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();
		Mockito.when(germplasmDataManager.getProgramFavorites(FavoriteType.METHOD, 1000, BreedingMethodFieldTest.PROGRAM_UUID))
				.thenReturn(favouriteMethods);
		breedingMethodField.setHasDefaultValue(false);
		Assert.assertFalse("Expecting a false return value when there are no favourite method and no default value.",
				breedingMethodField.initPopulateFavMethod(BreedingMethodFieldTest.PROGRAM_UUID));
	}

	@Test
	public void testinitPopulateFavMethodReturnsFalseWhenThereAreFavouriteMethodAndHasDefaultValue() throws MiddlewareQueryException {

		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();
		favouriteMethods.add(Mockito.mock(ProgramFavorite.class));

		breedingMethodField.setHasDefaultValue(true);

		Assert.assertFalse("Expecting a false return value when there are favourite method but has default value.",
				breedingMethodField.initPopulateFavMethod(BreedingMethodFieldTest.PROGRAM_UUID));
	}

	@Test
	public void testinitPopulateFavMethodReturnsTrueWhenThereAreFavouriteMethodAndHasNoDefaultValue() throws MiddlewareQueryException {

		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();
		favouriteMethods.add(Mockito.mock(ProgramFavorite.class));
		Mockito.when(germplasmDataManager.getProgramFavorites(FavoriteType.METHOD, 1000, BreedingMethodFieldTest.PROGRAM_UUID))
				.thenReturn(favouriteMethods);

		breedingMethodField.setHasDefaultValue(false);

		Assert.assertTrue("Expecting a true return value when there are favourite method and no default value.",
				breedingMethodField.initPopulateFavMethod(BreedingMethodFieldTest.PROGRAM_UUID));
	}

	@Test
	public void testLaunchManageWindow() {

		final Window manageFavoriteMethodsWindow = new Window();

		Mockito.when(breedingManagerWindowGenerator
				.openMethodManagerPopupWindow(PROJECT_ID, breedingMethodField.getWindow(), MANAGE_METHODS_LABEL))
				.thenReturn(manageFavoriteMethodsWindow);

		breedingMethodField.launchManageWindow();

		Mockito.verify(breedingManagerWindowGenerator)
				.openMethodManagerPopupWindow(PROJECT_ID, breedingMethodField.getWindow(), MANAGE_METHODS_LABEL);

		Assert.assertFalse(manageFavoriteMethodsWindow.getListeners(Window.CloseEvent.class).isEmpty());

		final Window.CloseListener closeListener =
				(Window.CloseListener) manageFavoriteMethodsWindow.getListeners(Window.CloseEvent.class).iterator().next();

		Assert.assertTrue(closeListener instanceof BreedingMethodField.ManageFavoriteMethodsWindowCloseListener);

	}

}
