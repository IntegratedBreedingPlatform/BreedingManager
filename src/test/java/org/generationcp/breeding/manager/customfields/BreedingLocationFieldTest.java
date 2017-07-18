
package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.application.BreedingManagerWindowGenerator;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.service.BreedingManagerServiceImpl;
import org.generationcp.commons.spring.util.ContextUtil;
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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.OptionGroup;

public class BreedingLocationFieldTest {

	private static final String STORAGE_LOCATIONS = "Storage locations";

	private static final String BREEDING_LOCATIONS = "Breeding locations";

	private static final String ALL_LOCATIONS = "All locations";

	private static final java.lang.String MANAGE_LOCATIONS_LABEL = "Manage Locations";

	private static final String DUMMY_UNIQUE_ID = "1234567890";

	private static final Integer NON_BREEDING_NON_STORAGE_LOCATION_ID = 100;

	private static final Integer BREEDING_LOCATION_ID1 = 200;

	private static final Integer BREEDING_LOCATION_ID2 = 250;

	private static final Integer STORAGE_LOCATION_ID = 300;
	public static final long PROJECT_ID = 1L;


	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private LocationDataManager locationDataManager;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private BreedingManagerServiceImpl breedingManagerService;

	@Mock
	private BreedingManagerWindowGenerator breedingManagerWindowGenerator;

	@InjectMocks
	private BreedingLocationField breedingLocationField;

	private LocationTestDataInitializer locationTestDataInitializer;

	private List<Location> favoriteLocations;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		final Project project = this.getProject(PROJECT_ID);

		Mockito.when(this.breedingManagerService.getCurrentProject()).thenReturn(project);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);

		Mockito.when(this.messageSource.getMessage(Message.SHOW_ALL_LOCATIONS)).thenReturn(BreedingLocationFieldTest.ALL_LOCATIONS);
		Mockito.when(this.messageSource.getMessage(Message.SHOW_BREEDING_LOCATIONS))
				.thenReturn(BreedingLocationFieldTest.BREEDING_LOCATIONS);
		Mockito.when(this.messageSource.getMessage(Message.SHOW_STORAGE_LOCATIONS)).thenReturn(BreedingLocationFieldTest.STORAGE_LOCATIONS);
		Mockito.when(this.messageSource.getMessage(Message.MANAGE_LOCATIONS)).thenReturn(MANAGE_LOCATIONS_LABEL);

		this.breedingLocationField.instantiateComponents();
		this.locationTestDataInitializer = new LocationTestDataInitializer();

		this.favoriteLocations = this.createFavoriteLocations();
		Mockito.doReturn(this.favoriteLocations).when(this.germplasmDataManager).getLocationsByIDs(Matchers.anyListOf(Integer.class));
	}

	@Test
	public void testInstantiateComponentsWithDefaultLocationType() {
		final OptionGroup breedingLocationsRadioBtn = this.breedingLocationField.getBreedingLocationsRadioBtn();
		final Collection<?> radioBtnItemIds = breedingLocationsRadioBtn.getItemIds();
		final Iterator<?> iterator = radioBtnItemIds.iterator();

		// Checking options for breeding location radio button
		Assert.assertEquals("Expecting two options for location radio box", 2, radioBtnItemIds.size());
		Assert.assertEquals("Expecting first option to be 'All locations'", BreedingLocationFieldTest.ALL_LOCATIONS, iterator.next());
		Assert.assertEquals("Expecting second option to be 'Breeding locations'", BreedingLocationFieldTest.BREEDING_LOCATIONS,
				iterator.next());
		Assert.assertEquals("Expecting the 'Breeding locations' option is chosen", BreedingLocationFieldTest.BREEDING_LOCATIONS,
				breedingLocationsRadioBtn.getValue());
	}

	@Test
	public void testInstantiateComponentsWithSeedStorageLocationType() {
		// set to Seed Storage location type and call instantiateComponents again
		this.breedingLocationField.setLocationType(BreedingLocationField.STORAGE_LOCATION_TYPEID);
		this.breedingLocationField.instantiateComponents();

		final OptionGroup breedingLocationsRadioBtn = this.breedingLocationField.getBreedingLocationsRadioBtn();
		final Collection<?> radioBtnItemIds = breedingLocationsRadioBtn.getItemIds();
		final Iterator<?> iterator = radioBtnItemIds.iterator();

		// Checking options for breeding location radio button
		Assert.assertEquals("Expecting two options for location radio box", 2, radioBtnItemIds.size());
		Assert.assertEquals("Expecting first option to be 'All locations'", BreedingLocationFieldTest.ALL_LOCATIONS, iterator.next());
		Assert.assertEquals("Expecting second option to be 'Storage locations'", BreedingLocationFieldTest.STORAGE_LOCATIONS,
				iterator.next());
		Assert.assertEquals("Expecting the 'Storage locations' option is chosen", BreedingLocationFieldTest.STORAGE_LOCATIONS,
				breedingLocationsRadioBtn.getValue());
	}

	@Test
	public void testinitPopulateFavLocationsReturnsFalseWhenThereAreNoFavouriteLocation() throws MiddlewareQueryException {
		final ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();
		Mockito.when(this.germplasmDataManager.getProgramFavorites(FavoriteType.LOCATION, 1000, BreedingLocationFieldTest.DUMMY_UNIQUE_ID))
				.thenReturn(favouriteLocations);

		Assert.assertFalse("Expecting a false return value when there are no favourite locations.",
				this.breedingLocationField.initPopulateFavLocations(BreedingLocationFieldTest.DUMMY_UNIQUE_ID, 0));
	}

	@Test
	public void testinitPopulateFavLocationsReturnsTrueWhenThereAreFavouriteLocation() throws MiddlewareQueryException {
		final ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();
		final OptionGroup opg = Mockito.mock(OptionGroup.class);
		favouriteLocations.add(Mockito.mock(ProgramFavorite.class));
		Mockito.when(opg.getValue()).thenReturn(Boolean.FALSE.toString());

		Mockito.when(this.germplasmDataManager.getProgramFavorites(FavoriteType.LOCATION, 1000, BreedingLocationFieldTest.DUMMY_UNIQUE_ID))
				.thenReturn(favouriteLocations);

		Assert.assertTrue("Expecting a true return value when there are favourite locations.",
				this.breedingLocationField.initPopulateFavLocations(BreedingLocationFieldTest.DUMMY_UNIQUE_ID, 0));
	}

	@Test
	public void testInitLocationItemsWithoutPreSelectedItem() {
		final List<Location> locations = this.locationTestDataInitializer.createLocationList(5);
		// has pre-selected item after loading
		this.breedingLocationField.initLocationItems(locations, false);

		Assert.assertNull("Expecting that there is no selected item from the location combobox.",
				this.breedingLocationField.getBreedingLocationComboBox().getValue());
	}

	@Test
	public void testInitLocationItemsWithPreSelectedItemSetToDefaultLocation() {
		final List<Location> locations = this.locationTestDataInitializer.createLocationList(5);
		locations.add(this.locationTestDataInitializer.createLocation(6, BreedingLocationField.DEFAULT_LOCATION));

		// has pre-selected item after loading
		this.breedingLocationField.initLocationItems(locations, true);

		Assert.assertEquals("Expecting that the default location, 'UNKNOWN' will be the selected location item.", 6,
				Integer.valueOf(this.breedingLocationField.getBreedingLocationComboBox().getValue().toString()).intValue());
	}

	@Test
	public void testPopulateHarvestLocationsWithAllLocations() {
		this.breedingLocationField.getBreedingLocationsRadioBtn().select(BreedingLocationFieldTest.ALL_LOCATIONS);

		this.breedingLocationField.populateHarvestLocation(false, BreedingLocationFieldTest.DUMMY_UNIQUE_ID);

		// Verify that Middleware method for getting all locations for program was called
		Mockito.verify(this.locationDataManager, Mockito.times(1)).getLocationsByUniqueID(BreedingLocationFieldTest.DUMMY_UNIQUE_ID);
		Mockito.verifyNoMoreInteractions(this.locationDataManager);

		// Verify that Middleware methods for getting program favorites were not called
		Mockito.verifyZeroInteractions(this.workbenchDataManager);
		Mockito.verifyZeroInteractions(this.germplasmDataManager);
	}

	@Test
	public void testPopulateHarvestLocationsWithBreedingLocations() {
		this.breedingLocationField.populateHarvestLocation(false, BreedingLocationFieldTest.DUMMY_UNIQUE_ID);

		// Verify that Middleware method for getting all breeding locations for program was called
		Mockito.verify(this.locationDataManager, Mockito.times(1))
				.getAllBreedingLocationsByUniqueID(BreedingLocationFieldTest.DUMMY_UNIQUE_ID);
		Mockito.verifyNoMoreInteractions(this.locationDataManager);

		// Verify that Middleware methods for getting program favorites were not called
		Mockito.verifyZeroInteractions(this.workbenchDataManager);
		Mockito.verifyZeroInteractions(this.germplasmDataManager);
	}

	@Test
	public void testPopulateHarvestLocationsWithSeedStorageLocations() {
		this.breedingLocationField.setLocationType(BreedingLocationField.STORAGE_LOCATION_TYPEID);

		this.breedingLocationField.populateHarvestLocation(false, BreedingLocationFieldTest.DUMMY_UNIQUE_ID);

		// Verify that Middleware method for getting all seed storage locations for program was called
		Mockito.verify(this.locationDataManager, Mockito.times(1)).getLocationsByType(BreedingLocationField.STORAGE_LOCATION_TYPEID,
				BreedingLocationFieldTest.DUMMY_UNIQUE_ID);
		Mockito.verifyNoMoreInteractions(this.locationDataManager);

		// Verify that Middleware methods for getting program favorites were not called
		Mockito.verifyZeroInteractions(this.workbenchDataManager);
		Mockito.verifyZeroInteractions(this.germplasmDataManager);
	}

	@Test
	public void testPopulateHarvestLocationsWithAllFavoriteLocations() {
		this.breedingLocationField.getBreedingLocationsRadioBtn().select(BreedingLocationFieldTest.ALL_LOCATIONS);

		this.breedingLocationField.populateHarvestLocation(true, BreedingLocationFieldTest.DUMMY_UNIQUE_ID);

		// Verify Middleware interactions
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getProgramFavorites(FavoriteType.LOCATION, 1000,
				BreedingLocationFieldTest.DUMMY_UNIQUE_ID);
		Mockito.verifyZeroInteractions(this.locationDataManager);

		// Expecting all favorite locations to be in location combobox
		final ComboBox locationComboBox = this.breedingLocationField.getBreedingLocationComboBox();
		Assert.assertEquals("Expecting size of favorite locations to be equal size of location combobox", this.favoriteLocations.size(),
				locationComboBox.size());
	}

	@Test
	public void testPopulateHarvestLocationsWithFavoriteBreedingLocations() {
		this.breedingLocationField.populateHarvestLocation(true, BreedingLocationFieldTest.DUMMY_UNIQUE_ID);

		// Verify Middleware interactions
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getProgramFavorites(FavoriteType.LOCATION, 1000,
				BreedingLocationFieldTest.DUMMY_UNIQUE_ID);
		Mockito.verifyZeroInteractions(this.locationDataManager);

		// Verify contents of location combobox
		final ComboBox locationComboBox = this.breedingLocationField.getBreedingLocationComboBox();
		Assert.assertEquals("Expecting 2 breeding locations in location combobox", 2, locationComboBox.size());
		for (final Object id : locationComboBox.getItemIds()) {
			if (!(BreedingLocationFieldTest.BREEDING_LOCATION_ID1.equals(id)
					|| BreedingLocationFieldTest.BREEDING_LOCATION_ID2.equals(id))) {
				Assert.fail("A non-breeding location was included in location combobox");
			}
		}
	}

	@Test
	public void testPopulateHarvestLocationsWithFavoriteSeedStorageLocations() {
		this.breedingLocationField.setLocationType(BreedingLocationField.STORAGE_LOCATION_TYPEID);

		this.breedingLocationField.populateHarvestLocation(true, BreedingLocationFieldTest.DUMMY_UNIQUE_ID);

		// Verify Middleware interactions
		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).getProgramFavorites(FavoriteType.LOCATION, 1000,
				BreedingLocationFieldTest.DUMMY_UNIQUE_ID);
		Mockito.verifyZeroInteractions(this.locationDataManager);

		// Verify contents of location combobox
		final ComboBox locationComboBox = this.breedingLocationField.getBreedingLocationComboBox();
		Assert.assertEquals("Expecting 1 storage location in location combobox", 1, locationComboBox.size());
		Assert.assertEquals("Expecting favorite Storage location to be present location combobox",
				BreedingLocationFieldTest.STORAGE_LOCATION_ID, locationComboBox.getItemIds().iterator().next());
	}

	private Project getProject(final long id) {
		final Project project = new Project();
		project.setProjectId(id);
		project.setUniqueID(BreedingLocationFieldTest.DUMMY_UNIQUE_ID);
		return project;
	}

	// Create 4 locations. 2 breeding, 1 storage and 1 non-breeding,non-storage location
	private List<Location> createFavoriteLocations() {
		final List<Location> locationList = new ArrayList<>();
		locationList.add(this.locationTestDataInitializer.createLocation(BreedingLocationFieldTest.NON_BREEDING_NON_STORAGE_LOCATION_ID,
				"LOCATION" + BreedingLocationFieldTest.NON_BREEDING_NON_STORAGE_LOCATION_ID, 1,
				"ABBR" + BreedingLocationFieldTest.NON_BREEDING_NON_STORAGE_LOCATION_ID, DUMMY_UNIQUE_ID));
		locationList.add(this.locationTestDataInitializer.createLocation(BreedingLocationFieldTest.BREEDING_LOCATION_ID1,
				"LOCATION" + BreedingLocationFieldTest.BREEDING_LOCATION_ID1, Location.BREEDING_LOCATION_TYPE_IDS[0],
				"ABBR" + BreedingLocationFieldTest.BREEDING_LOCATION_ID1, DUMMY_UNIQUE_ID));
		locationList.add(this.locationTestDataInitializer.createLocation(BreedingLocationFieldTest.BREEDING_LOCATION_ID2,
				"LOCATION" + BreedingLocationFieldTest.BREEDING_LOCATION_ID1, Location.BREEDING_LOCATION_TYPE_IDS[1],
				"ABBR" + BreedingLocationFieldTest.BREEDING_LOCATION_ID2, DUMMY_UNIQUE_ID));
		locationList.add(this.locationTestDataInitializer.createLocation(BreedingLocationFieldTest.STORAGE_LOCATION_ID,
				"LOCATION" + BreedingLocationFieldTest.STORAGE_LOCATION_ID, BreedingLocationField.STORAGE_LOCATION_TYPEID,
				"ABBR" + BreedingLocationFieldTest.STORAGE_LOCATION_ID, DUMMY_UNIQUE_ID));
		return locationList;
	}

	@Test
	public void testLaunchManageWindow() {

		final Window manageFavoriteLocationsWindow = new Window();

		Mockito.when(breedingManagerWindowGenerator
				.openLocationManagerPopupWindow(PROJECT_ID, breedingLocationField.getWindow(), MANAGE_LOCATIONS_LABEL))
				.thenReturn(manageFavoriteLocationsWindow);

		breedingLocationField.launchManageWindow();

		Mockito.verify(breedingManagerWindowGenerator)
				.openLocationManagerPopupWindow(PROJECT_ID, breedingLocationField.getWindow(), MANAGE_LOCATIONS_LABEL);

		Assert.assertFalse(manageFavoriteLocationsWindow.getListeners(Window.CloseEvent.class).isEmpty());

		final Window.CloseListener closeListener =
				(Window.CloseListener) manageFavoriteLocationsWindow.getListeners(Window.CloseEvent.class).iterator().next();

		Assert.assertTrue(closeListener instanceof BreedingLocationField.ManageFavoriteLocationsWindowCloseListener);

	}

}
