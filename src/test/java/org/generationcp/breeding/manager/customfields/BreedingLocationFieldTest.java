
package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;

import org.generationcp.breeding.manager.service.BreedingManagerServiceImpl;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.dms.ProgramFavorite;
import org.generationcp.middleware.pojos.dms.ProgramFavorite.FavoriteType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class BreedingLocationFieldTest {

	private static final String DUMMY_UNIQUE_ID = "1234567890";

	@Test
	public void testinitPopulateFavLocationsReturnsFalseWhenThereAreNoFavouriteLocation() throws MiddlewareQueryException {

		BreedingLocationField blf = new BreedingLocationField();

		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);
		ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.LOCATION, 1000, BreedingLocationFieldTest.DUMMY_UNIQUE_ID)).thenReturn(
				favouriteLocations);
		blf.setGermplasmDataManager(gpdm);

		Assert.assertFalse("Expecting a false return value when there are no favourite locations.",
				blf.initPopulateFavLocations(BreedingLocationFieldTest.DUMMY_UNIQUE_ID));
	}

	@Test
	public void testinitPopulateFavLocationsReturnsTrueWhenThereAreFavouriteLocation() throws MiddlewareQueryException {
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);
		LocationDataManager ldm = Mockito.mock(LocationDataManager.class);
		WorkbenchDataManager wdm = Mockito.mock(WorkbenchDataManager.class);
		BreedingManagerServiceImpl service = Mockito.mock(BreedingManagerServiceImpl.class);

		ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();
		favouriteLocations.add(Mockito.mock(ProgramFavorite.class));
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.LOCATION, 1000, BreedingLocationFieldTest.DUMMY_UNIQUE_ID)).thenReturn(
				favouriteLocations);
		Mockito.when(service.getCurrentProject()).thenReturn(this.getProject(1L));

		BreedingLocationField blf = new BreedingLocationField();
		blf.setGermplasmDataManager(gpdm);
		blf.setLocationDataManager(ldm);
		blf.setWorkbenchDataManager(wdm);
		blf.setMessageSource(Mockito.mock(SimpleResourceBundleMessageSource.class));
		blf.setBreedingManagerService(service);

		blf.instantiateComponents();

		Assert.assertTrue("Expecting a true return value when there are favourite locations.",
				blf.initPopulateFavLocations(BreedingLocationFieldTest.DUMMY_UNIQUE_ID));
	}

	private Project getProject(long id) {
		Project project = new Project();
		project.setProjectId(id);
		project.setUniqueID(BreedingLocationFieldTest.DUMMY_UNIQUE_ID);
		return project;
	}

}
