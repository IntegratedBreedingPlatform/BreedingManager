package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;

import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.dms.ProgramFavorite;
import org.generationcp.middleware.pojos.dms.ProgramFavorite.FavoriteType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class BreedingLocationFieldTest {
	
	
	@Test
	public void testinitPopulateFavLocationsReturnsFalseWhenThereAreNoFavouriteLocation() throws MiddlewareQueryException {
		
		BreedingLocationField blf = new BreedingLocationField();
		
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);		
		ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();		
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.LOCATION, 1000)).thenReturn(favouriteLocations);
		blf.setGermplasmDataManager(gpdm);
		
		Assert.assertFalse("Expecting a false return value when there are no favourite locations.", blf.initPopulateFavLocations());
	}
	
	@Test
	public void testinitPopulateFavLocationsReturnsTrueWhenThereAreFavouriteLocation() throws MiddlewareQueryException {
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);
		LocationDataManager ldm = Mockito.mock(LocationDataManager.class);
		WorkbenchDataManager wdm = Mockito.mock(WorkbenchDataManager.class);
		
		ArrayList<ProgramFavorite> favouriteLocations = new ArrayList<ProgramFavorite>();
		favouriteLocations.add(Mockito.mock(ProgramFavorite.class));
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.LOCATION, 1000)).thenReturn(favouriteLocations);
		
		BreedingLocationField blf = new BreedingLocationField();
		blf.setGermplasmDataManager(gpdm);
		blf.setLocationDataManager(ldm);
		blf.setWorkbenchDataManager(wdm);
		blf.setMessageSource(Mockito.mock(SimpleResourceBundleMessageSource.class));
		
		blf.instantiateComponents();
		
		
		
		Assert.assertTrue("Expecting a true return value when there are favourite locations.", blf.initPopulateFavLocations());
	}

}
