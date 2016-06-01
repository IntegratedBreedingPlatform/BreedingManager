
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class BreedingLocationFieldTest {

	private static final String DUMMY_UNIQUE_ID = "1234567890";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	@Mock
	private GermplasmDataManager germplasmDataManager;
	@Mock
	private LocationDataManager locationDataManager;
	@Mock
	private WorkbenchDataManager workbenchDataManager;
	@Mock
	private BreedingManagerServiceImpl breedingManagerService;
	@InjectMocks
	private BreedingLocationField breedingLocationField;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(this.getProject(1L)).when(this.breedingManagerService).getCurrentProject();
		this.breedingLocationField.instantiateComponents();
	}

	@Test
	public void testinitPopulateFavLocationsReturnsFalseWhenThereAreNoFavouriteLocation() throws MiddlewareQueryException {
		final ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();
		Mockito.when(this.germplasmDataManager.getProgramFavorites(FavoriteType.LOCATION, 1000, BreedingLocationFieldTest.DUMMY_UNIQUE_ID))
				.thenReturn(favouriteLocations);

		Assert.assertFalse("Expecting a false return value when there are no favourite locations.",
				this.breedingLocationField.initPopulateFavLocations(BreedingLocationFieldTest.DUMMY_UNIQUE_ID));
	}

	@Test
	public void testinitPopulateFavLocationsReturnsTrueWhenThereAreFavouriteLocation() throws MiddlewareQueryException {
		final ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();
		favouriteLocations.add(Mockito.mock(ProgramFavorite.class));

		Mockito.when(this.germplasmDataManager.getProgramFavorites(FavoriteType.LOCATION, 1000, BreedingLocationFieldTest.DUMMY_UNIQUE_ID))
				.thenReturn(favouriteLocations);

		Assert.assertTrue("Expecting a true return value when there are favourite locations.",
				this.breedingLocationField.initPopulateFavLocations(BreedingLocationFieldTest.DUMMY_UNIQUE_ID));
	}

	private Project getProject(final long id) {
		final Project project = new Project();
		project.setProjectId(id);
		project.setUniqueID(BreedingLocationFieldTest.DUMMY_UNIQUE_ID);
		return project;
	}

}
