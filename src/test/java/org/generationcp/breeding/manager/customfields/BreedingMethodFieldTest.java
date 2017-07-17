package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;

import org.generationcp.breeding.manager.service.BreedingManagerServiceImpl;
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

	@Mock
	GermplasmDataManager germplasmDataManager;

	@Mock
	WorkbenchDataManager workbenchDataManager;

	@Mock
	SimpleResourceBundleMessageSource messageSource;

	@Mock
	private BreedingManagerServiceImpl service;

	@InjectMocks
	BreedingMethodField breedingMethodField = new BreedingMethodField();

	@Before
	public void setUp() throws MiddlewareQueryException {
		MockitoAnnotations.initMocks(this);
		Mockito.when(this.service.getCurrentProject()).thenReturn(this.getProject(1L));
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
		Mockito.when(germplasmDataManager.getProgramFavorites(FavoriteType.METHOD, 1000, BreedingMethodFieldTest.PROGRAM_UUID))
				.thenReturn(favouriteMethods);

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

}
