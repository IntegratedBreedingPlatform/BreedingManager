package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;

import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.dms.ProgramFavorite;
import org.generationcp.middleware.pojos.dms.ProgramFavorite.FavoriteType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class BreedingMethodFieldTest {
	
	
	@Test
	public void testinitPopulateFavMethodReturnsFalseWhenThereHasDefaultValue() throws MiddlewareQueryException {
		
		BreedingMethodField bmf = new BreedingMethodField();		
		bmf.setHasDefaultValue(true);
		Assert.assertFalse("Expecting a false return when there is default value.", bmf.initPopulateFavMethod());
	}
	
	@Test
	public void testinitPopulateFavMethodReturnsFalseWhenThereAreNoFavouriteMethodAndHasNoDefaultValue() throws MiddlewareQueryException {
		
		BreedingMethodField bmf = new BreedingMethodField();
		
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);		
		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();		
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.METHOD, 1000)).thenReturn(favouriteMethods);
		bmf.setGermplasmDataManager(gpdm);
		bmf.setHasDefaultValue(false);
		Assert.assertFalse("Expecting a false return value when there are no favourite method and no default value.", bmf.initPopulateFavMethod());
	}
	
	@Test
	public void testinitPopulateFavMethodReturnsFalseWhenThereAreFavouriteMethodAndHasDefaultValue() throws MiddlewareQueryException {
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);
		WorkbenchDataManager wdm = Mockito.mock(WorkbenchDataManager.class);
		
		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();
		favouriteMethods.add(Mockito.mock(ProgramFavorite.class));
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.METHOD, 1000)).thenReturn(favouriteMethods);
		
		BreedingMethodField bmf = new BreedingMethodField();
		bmf.setGermplasmDataManager(gpdm);
		bmf.setWorkbenchDataManager(wdm);
		bmf.setMessageSource(Mockito.mock(SimpleResourceBundleMessageSource.class));
		
		bmf.instantiateComponents();
		bmf.setHasDefaultValue(true);
		
		Assert.assertFalse("Expecting a false return value when there are favourite method but has default value.", bmf.initPopulateFavMethod());
	}
	
	@Test
	public void testinitPopulateFavMethodReturnsTrueWhenThereAreFavouriteMethodAndHasNoDefaultValue() throws MiddlewareQueryException {
		GermplasmDataManager gpdm = Mockito.mock(GermplasmDataManager.class);
		WorkbenchDataManager wdm = Mockito.mock(WorkbenchDataManager.class);
		
		ArrayList<ProgramFavorite> favouriteMethods = new ArrayList<ProgramFavorite>();
		favouriteMethods.add(Mockito.mock(ProgramFavorite.class));
		Mockito.when(gpdm.getProgramFavorites(FavoriteType.METHOD, 1000)).thenReturn(favouriteMethods);
		
		BreedingMethodField bmf = new BreedingMethodField();
		bmf.setGermplasmDataManager(gpdm);
		bmf.setWorkbenchDataManager(wdm);
		bmf.setMessageSource(Mockito.mock(SimpleResourceBundleMessageSource.class));
		
		bmf.instantiateComponents();
		bmf.setHasDefaultValue(false);
		
		Assert.assertTrue("Expecting a true return value when there are favourite method and no default value.", bmf.initPopulateFavMethod());
	}

}
