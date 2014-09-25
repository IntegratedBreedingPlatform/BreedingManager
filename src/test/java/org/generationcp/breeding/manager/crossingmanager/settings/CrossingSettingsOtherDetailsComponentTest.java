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

public class CrossingSettingsOtherDetailsComponentTest {

	@Test
	public void testinitPopulateFavLocationsReturnsFalseWhenThereAreNoFavouriteLocation() throws MiddlewareQueryException {		
		CrossingSettingsOtherDetailsComponent csodcp = new CrossingSettingsOtherDetailsComponent();
		
		SimpleResourceBundleMessageSource messageSource = Mockito.mock(SimpleResourceBundleMessageSource.class);		
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);
		
		ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();
		
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.LOCATION, 1000)).thenReturn(favouriteLocations);
		Mockito.when(messageSource.getMessage(Message.HARVEST_DETAILS)).thenReturn("Harvest Details");
		
		csodcp.setGermplasmDataManager(gpdm);
		csodcp.setMessageSource(messageSource);
		csodcp.instantiateComponents();
		
		Assert.assertFalse("Expecting a false return value when there are no favourite locations.", csodcp.initPopulateFavLocation());
	}
	
	@Test
	public void testinitPopulateFavLocationsReturnsTrueWhenThereAreFavouriteLocation() throws MiddlewareQueryException {
		
		CrossingSettingsOtherDetailsComponent csodcp = new CrossingSettingsOtherDetailsComponent();
		
		SimpleResourceBundleMessageSource messageSource = Mockito.mock(SimpleResourceBundleMessageSource.class);		
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);
		
		ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();
		favouriteLocations.add(Mockito.mock(ProgramFavorite.class));
		
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.LOCATION, 1000)).thenReturn(favouriteLocations);
		Mockito.when(messageSource.getMessage(Message.HARVEST_DETAILS)).thenReturn("Harvest Details");
		
		csodcp.setGermplasmDataManager(gpdm);
		csodcp.setMessageSource(messageSource);
		csodcp.instantiateComponents();				
		
		Assert.assertTrue("Expecting a true return value when there are favourite locations.", csodcp.initPopulateFavLocation());
	}
}
