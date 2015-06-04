
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

		this.csmc = Mockito.spy(new CrossingSettingsMethodComponent());

		this.csmc.setGermplasmDataManager(this.gpdm);
		this.csmc.setMessageSource(this.messageSource);
		this.csmc.setBreedingManagerService(this.service);
	}

	@Test
	public void testinitPopulateFavMethodReturnsFalseWhenThereAreNoFavouriteMethod() throws MiddlewareQueryException {
		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();

		Mockito.when(this.gpdm.getProgramFavorites(FavoriteType.METHOD, 1000, CrossingSettingsMethodComponentTest.DUMMY_UNIQUE_ID))
				.thenReturn(favouriteMethods);
		Mockito.when(this.messageSource.getMessage(Message.BREEDING_METHOD)).thenReturn("Breeding Method");

		this.csmc.instantiateComponents();

		Assert.assertFalse("Expecting a false return value when there are no favourite method.",
				this.csmc.initPopulateFavMethod(CrossingSettingsMethodComponentTest.DUMMY_UNIQUE_ID));
	}

	private Project getProject(long id) {
		Project project = new Project();
		project.setProjectId(id);
		project.setUniqueID(CrossingSettingsMethodComponentTest.DUMMY_UNIQUE_ID);
		return project;
	}

	@Test
	public void testinitPopulateFavMethodReturnsTrueWhenThereAreFavouriteMethod() throws MiddlewareQueryException {

		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();
		favouriteMethods.add(Mockito.mock(ProgramFavorite.class));

		Mockito.when(this.gpdm.getProgramFavorites(FavoriteType.METHOD, 1000, CrossingSettingsMethodComponentTest.DUMMY_UNIQUE_ID))
				.thenReturn(favouriteMethods);
		this.csmc.instantiateComponents();

		Assert.assertTrue("Expecting a true return value when there are favourite method.",
				this.csmc.initPopulateFavMethod(CrossingSettingsMethodComponentTest.DUMMY_UNIQUE_ID));
	}
}
