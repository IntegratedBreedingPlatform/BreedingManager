
package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.ArrayList;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.service.BreedingManagerServiceImpl;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.dms.ProgramFavorite;
import org.generationcp.middleware.pojos.dms.ProgramFavorite.FavoriteType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CrossingSettingsOtherDetailsComponentTest {

	private static final String DUMMY_UNIQUE_ID = "1234567890";

	@Test
	public void testinitPopulateFavLocationsReturnsFalseWhenThereAreNoFavouriteLocation() throws MiddlewareQueryException {
		CrossingSettingsOtherDetailsComponent csodcp = new CrossingSettingsOtherDetailsComponent();

		SimpleResourceBundleMessageSource messageSource = Mockito.mock(SimpleResourceBundleMessageSource.class);
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);
		BreedingManagerServiceImpl service = Mockito.mock(BreedingManagerServiceImpl.class);

		ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();

		Mockito.when(service.getCurrentProject()).thenReturn(this.getProject(1L));
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.LOCATION, 1000, CrossingSettingsOtherDetailsComponentTest.DUMMY_UNIQUE_ID))
				.thenReturn(favouriteLocations);
		Mockito.when(messageSource.getMessage(Message.HARVEST_DETAILS)).thenReturn("Harvest Details");

		csodcp.setGermplasmDataManager(gpdm);
		csodcp.setMessageSource(messageSource);
		csodcp.setBreedingManagerService(service);
		csodcp.instantiateComponents();

		Assert.assertFalse("Expecting a false return value when there are no favourite locations.",
				csodcp.initPopulateFavLocation(CrossingSettingsOtherDetailsComponentTest.DUMMY_UNIQUE_ID));
	}

	private Project getProject(long id) {
		Project project = new Project();
		project.setProjectId(id);
		project.setUniqueID(CrossingSettingsOtherDetailsComponentTest.DUMMY_UNIQUE_ID);
		return project;
	}

	@Test
	public void testinitPopulateFavLocationsReturnsTrueWhenThereAreFavouriteLocation() throws MiddlewareQueryException {

		CrossingSettingsOtherDetailsComponent csodcp = new CrossingSettingsOtherDetailsComponent();

		SimpleResourceBundleMessageSource messageSource = Mockito.mock(SimpleResourceBundleMessageSource.class);
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);
		BreedingManagerServiceImpl service = Mockito.mock(BreedingManagerServiceImpl.class);

		ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();
		favouriteLocations.add(Mockito.mock(ProgramFavorite.class));

		Mockito.when(service.getCurrentProject()).thenReturn(this.getProject(1L));
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.LOCATION, 1000, CrossingSettingsOtherDetailsComponentTest.DUMMY_UNIQUE_ID))
				.thenReturn(favouriteLocations);
		Mockito.when(messageSource.getMessage(Message.HARVEST_DETAILS)).thenReturn("Harvest Details");

		csodcp.setGermplasmDataManager(gpdm);
		csodcp.setMessageSource(messageSource);
		csodcp.setBreedingManagerService(service);
		csodcp.instantiateComponents();

		Assert.assertTrue("Expecting a true return value when there are favourite locations.",
				csodcp.initPopulateFavLocation(CrossingSettingsOtherDetailsComponentTest.DUMMY_UNIQUE_ID));
	}
}
