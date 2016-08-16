
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

public class CrossingSettingsMethodComponentTest {

	private static final String DUMMY_UNIQUE_ID = "1234567890";

	private CrossingSettingsMethodComponent csmc;

	@Mock
	private BreedingManagerServiceImpl service;
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	@Mock
	private GermplasmDataManager gpdm;

	@Before
	public void setUp() throws MiddlewareQueryException {
		MockitoAnnotations.initMocks(this);

		Mockito.when(this.service.getCurrentProject()).thenReturn(this.getProject(1L));
		Mockito.when(this.messageSource.getMessage(Message.BREEDING_METHOD)).thenReturn("Breeding Method");
		Mockito.when(this.messageSource.getMessage(Message.SHOW_GENERATIVE_METHODS)).thenReturn("Gen Method");
		Mockito.when(this.messageSource.getMessage(Message.SHOW_ALL_METHODS)).thenReturn("All Method");

		this.csmc = Mockito.spy(new CrossingSettingsMethodComponent());

		this.csmc.setGermplasmDataManager(this.gpdm);
		this.csmc.setMessageSource(this.messageSource);
		this.csmc.setBreedingManagerService(this.service);
	}

	@Test
	public void testinitPopulateFavMethodReturnsFalseWhenThereAreNoFavouriteMethod() throws MiddlewareQueryException {
		final ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();

		Mockito.when(this.gpdm.getProgramFavorites(FavoriteType.METHOD, 1000, CrossingSettingsMethodComponentTest.DUMMY_UNIQUE_ID))
				.thenReturn(favouriteMethods);
		Mockito.when(this.messageSource.getMessage(Message.BREEDING_METHOD)).thenReturn("Breeding Method");

		this.csmc.instantiateComponents();

		Assert.assertFalse("Expecting a false return value when there are no favourite method.",
				this.csmc.initPopulateFavMethod(CrossingSettingsMethodComponentTest.DUMMY_UNIQUE_ID));
	}

	private Project getProject(final long id) {
		final Project project = new Project();
		project.setProjectId(id);
		project.setUniqueID(CrossingSettingsMethodComponentTest.DUMMY_UNIQUE_ID);
		return project;
	}

	@Test
	public void testinitPopulateFavMethodReturnsTrueWhenThereAreFavouriteMethod() throws MiddlewareQueryException {

		final ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();
		favouriteMethods.add(Mockito.mock(ProgramFavorite.class));

		Mockito.when(this.gpdm.getProgramFavorites(FavoriteType.METHOD, 1000, CrossingSettingsMethodComponentTest.DUMMY_UNIQUE_ID))
				.thenReturn(favouriteMethods);
		this.csmc.instantiateComponents();

		Assert.assertTrue("Expecting a true return value when there are favourite method.",
				this.csmc.initPopulateFavMethod(CrossingSettingsMethodComponentTest.DUMMY_UNIQUE_ID));
	}

	@Test
	public void testValidationPassedWhenMethodIsFromParentalLines() {
		// Unselected checkbox means method will be based on parental lines
		final CrossingSettingsMethodComponent breedingMethodComponent = new CrossingSettingsMethodComponent();
		final CheckBox selectMethodForAllCheckbox = new CheckBox();
		selectMethodForAllCheckbox.setValue(false);

		breedingMethodComponent.setSelectMethod(selectMethodForAllCheckbox);
		final boolean isValidationsPassed = breedingMethodComponent.validateInputFields();

		Assert.assertTrue("Expecting validation to pass when method is based on parental lines", isValidationsPassed);
	}

	@Test
	public void testValidationPassedWhenBreedingMethodChosenForAllCrosses() {
		final CrossingSettingsMethodComponent breedingMethodComponent = new CrossingSettingsMethodComponent();

		// "Select method to use for all crosses" option was ticked off
		final CheckBox selectMethodForAllCheckbox = new CheckBox();
		selectMethodForAllCheckbox.setValue(true);
		breedingMethodComponent.setSelectMethod(selectMethodForAllCheckbox);

		// Breeding method chosen for all crosses
		final ComboBox breedingMethodsSelection = new ComboBox();
		breedingMethodsSelection.addItem(1);
		breedingMethodsSelection.setValue(1);
		breedingMethodComponent.setBreedingMethods(breedingMethodsSelection);

		final boolean isValidationsPassed = breedingMethodComponent.validateInputFields();
		Assert.assertTrue("Expecting validation to pass when breeding method was chosen for all crosses", isValidationsPassed);
	}

	@Test
	public void testValidationFailedWhenNoBreedingMethodChosenForAllCrosses() {
		// Initialise and set mocks
		final CrossingSettingsMethodComponent breedingMethodComponent = new CrossingSettingsMethodComponent();
		final Component parent = Mockito.mock(Component.class);
		Mockito.when(parent.getWindow()).thenReturn(new Window());
		breedingMethodComponent.setParent(parent);
		breedingMethodComponent.setMessageSource(this.messageSource);

		// "Select method to use for all crosses" option was ticked off
		final CheckBox selectMethodForAllCheckbox = new CheckBox();
		selectMethodForAllCheckbox.setValue(true);
		breedingMethodComponent.setSelectMethod(selectMethodForAllCheckbox);

		// No value chosen for breeding method
		final ComboBox breedingMethodsSelection = new ComboBox();
		breedingMethodComponent.setBreedingMethods(breedingMethodsSelection);

		final boolean isValidationsPassed = breedingMethodComponent.validateInputFields();
		Assert.assertFalse("Expecting validation to fail when there's no breeding method chosen for all crosses", isValidationsPassed);
	}

}
