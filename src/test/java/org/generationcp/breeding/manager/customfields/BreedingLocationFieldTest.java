
package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.service.BreedingManagerServiceImpl;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.LocationTestDataInitializer;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Location;
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

import com.vaadin.ui.OptionGroup;

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
	private OptionGroup breedingLocationsRadioBtn;
	@Mock
	private BreedingManagerServiceImpl breedingManagerService;
	@InjectMocks
	private BreedingLocationField breedingLocationField;

	private LocationTestDataInitializer locationTestDataInitializer;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(this.getProject(1L)).when(this.breedingManagerService).getCurrentProject();
		Mockito.when(breedingLocationsRadioBtn.getValue()).thenReturn(Boolean.FALSE.toString());
		this.breedingLocationField.setBreedingLocationsRadioBtn(breedingLocationsRadioBtn);
		this.breedingLocationField.instantiateComponents();
		this.locationTestDataInitializer = new LocationTestDataInitializer();
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
		OptionGroup opg = Mockito.mock(OptionGroup.class);
		favouriteLocations.add(Mockito.mock(ProgramFavorite.class));
		Mockito.when(opg.getValue()).thenReturn(Boolean.FALSE.toString());

		Mockito.when(this.germplasmDataManager.getProgramFavorites(FavoriteType.LOCATION, 1000, BreedingLocationFieldTest.DUMMY_UNIQUE_ID))
				.thenReturn(favouriteLocations);

		Assert.assertTrue("Expecting a true return value when there are favourite locations.",
				this.breedingLocationField.initPopulateFavLocations(BreedingLocationFieldTest.DUMMY_UNIQUE_ID));
	}

	@Test
	public void testInitLocationItemsWithoutPreSelectedItem() {
		final List<Location> locations = this.locationTestDataInitializer.createLocationList(5);
		// has pre-selected item after loading
		this.breedingLocationField.initLocationItems(locations, false);

		Assert.assertNull("Expecting that there is no selected item from the location combobox.", this.breedingLocationField
				.getBreedingLocationComboBox().getValue());
	}

	@Test
	public void testInitLocationItemsWithPreSelectedItem() {
		final List<Location> locations = this.locationTestDataInitializer.createLocationList(5);
		// has pre-selected item after loading
		this.breedingLocationField.initLocationItems(locations, true);

		Assert.assertEquals("Expecting that the first entry will be the selected location item.", locations.get(0).getLocid(),
				Integer.valueOf(this.breedingLocationField.getBreedingLocationComboBox().getValue().toString()));
	}

	@Test
	public void testInitLocationItemsWithPreSelectedItemSetToDefaultLocation() {
		final List<Location> locations = this.locationTestDataInitializer.createLocationList(5);
		locations.add(this.locationTestDataInitializer.createLocation(6, this.breedingLocationField.DEFAULT_LOCATION));

		// has pre-selected item after loading
		this.breedingLocationField.initLocationItems(locations, true);

		Assert.assertEquals("Expecting that the default location, 'UNKNOWN' will be the selected location item.", 6,
				Integer.valueOf(this.breedingLocationField.getBreedingLocationComboBox().getValue().toString()).intValue());
	}

	private Project getProject(final long id) {
		final Project project = new Project();
		project.setProjectId(id);
		project.setUniqueID(BreedingLocationFieldTest.DUMMY_UNIQUE_ID);
		return project;
	}

}
