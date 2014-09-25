package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.ArrayList;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.dms.ProgramFavorite;
import org.generationcp.middleware.pojos.dms.ProgramFavorite.FavoriteType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CrossingSettingsMethodComponentTest {

	@Test
	public void testinitPopulateFavMethodReturnsFalseWhenThereAreNoFavouriteMethod() throws MiddlewareQueryException {		
		CrossingSettingsMethodComponent csmc = new CrossingSettingsMethodComponent();
		
		SimpleResourceBundleMessageSource messageSource = Mockito.mock(SimpleResourceBundleMessageSource.class);		
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);
		
		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();
		
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.METHOD, 1000)).thenReturn(favouriteMethods);
		Mockito.when(messageSource.getMessage(Message.BREEDING_METHOD)).thenReturn("Breeding Method");
		
		csmc.setGermplasmDataManager(gpdm);
		csmc.setMessageSource(messageSource);
		csmc.instantiateComponents();
		
		Assert.assertFalse("Expecting a false return value when there are no favourite method.", csmc.initPopulateFavMethod());
	}
	
	@Test
	public void testinitPopulateFavMethodReturnsTrueWhenThereAreFavouriteMethod() throws MiddlewareQueryException {		
		CrossingSettingsMethodComponent csmc = new CrossingSettingsMethodComponent();
		
		SimpleResourceBundleMessageSource messageSource = Mockito.mock(SimpleResourceBundleMessageSource.class);		
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);
		
		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();
		favouriteMethods.add(Mockito.mock(ProgramFavorite.class));
		
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.METHOD, 1000)).thenReturn(favouriteMethods);
		Mockito.when(messageSource.getMessage(Message.BREEDING_METHOD)).thenReturn("Breeding Method");
		
		csmc.setGermplasmDataManager(gpdm);
		csmc.setMessageSource(messageSource);
		csmc.instantiateComponents();
		
		Assert.assertTrue("Expecting a true return value when there are favourite method.", csmc.initPopulateFavMethod());
	}
}
