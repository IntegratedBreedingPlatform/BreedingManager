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
import static org.mockito.Mockito.*;

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
	public void setUp() throws MiddlewareQueryException{
		MockitoAnnotations.initMocks(this);
		
		Mockito.when(service.getCurrentProject()).thenReturn(getProject(1L));
		Mockito.when(messageSource.getMessage(Message.BREEDING_METHOD)).thenReturn("Breeding Method");
		
		csmc = spy(new CrossingSettingsMethodComponent());
		
		csmc.setGermplasmDataManager(gpdm);
		csmc.setMessageSource(messageSource);
		csmc.setBreedingManagerService(service);
	}
	
	@Test
	public void testinitPopulateFavMethodReturnsFalseWhenThereAreNoFavouriteMethod() throws MiddlewareQueryException {		
		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();
		
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.METHOD, 1000, DUMMY_UNIQUE_ID)).thenReturn(favouriteMethods);
		Mockito.when(messageSource.getMessage(Message.BREEDING_METHOD)).thenReturn("Breeding Method");
		
		csmc.instantiateComponents();
		
		Assert.assertFalse("Expecting a false return value when there are no favourite method.", csmc.initPopulateFavMethod(DUMMY_UNIQUE_ID));
	}
	
	private Project getProject(long id) {
		Project project = new Project();
		project.setProjectId(id);
		project.setUniqueID(DUMMY_UNIQUE_ID);
		return project;
	}

	@Test
	public void testinitPopulateFavMethodReturnsTrueWhenThereAreFavouriteMethod() throws MiddlewareQueryException {		
		
		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();
		favouriteMethods.add(Mockito.mock(ProgramFavorite.class));
		
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.METHOD, 1000, DUMMY_UNIQUE_ID)).thenReturn(favouriteMethods);
		csmc.instantiateComponents();
		
		Assert.assertTrue("Expecting a true return value when there are favourite method.", csmc.initPopulateFavMethod(DUMMY_UNIQUE_ID));
	}
}
