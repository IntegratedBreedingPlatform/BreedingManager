package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import com.vaadin.ui.Component;

public class ListCommonActionsUtilTest {
	
	private ListCommonActionsUtil util;
	
	private GermplasmList listToSave; 
	private List<GermplasmListData> listEntries; 
	private Boolean forceHasChanges;
	private List<GermplasmListData> newEntries;
	private List<GermplasmListData> entriesToUpdate;
	private List<GermplasmListData> entriesToDelete;
	
	@Mock
	private GermplasmListManager dataManager;
	@Mock
	private Component source;
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		
		
	}
	
	@Test
	public void testGetNewEntriesToSaveUpdateDelete_ForNewEntries() throws MiddlewareQueryException{
		forceHasChanges = false;
		listToSave = getGermplasmList();
		listEntries = getGermplasmListData(5);
		newEntries = new ArrayList<GermplasmListData>();
		entriesToUpdate = new ArrayList<GermplasmListData>();
		entriesToDelete = new ArrayList<GermplasmListData>();
		
		when(dataManager.countGermplasmListDataByListId(listToSave.getId())).thenReturn(4L);
		when(dataManager.getGermplasmListDataByListId(listToSave.getId(), 0, 4)).thenReturn(listToSave.getListData());
		
		ListCommonActionsUtil.getNewEntriesToSaveUpdateDelete(listToSave, listEntries, forceHasChanges, newEntries, 
				entriesToUpdate, entriesToDelete, dataManager, source, messageSource);
		
		Assert.assertTrue("Expecting that the newEntries has entries but didn't.", !newEntries.isEmpty());
	}

	private GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		germplasmList.setName("List 001");
		germplasmList.setDescription("List 001 Description");
		germplasmList.setDate(20150101L);
		germplasmList.setListData(getGermplasmListData(4));
		
		return germplasmList;
	}
	
	private List<GermplasmListData> getGermplasmListData(Integer itemNo){
		List<GermplasmListData> listEntries = new ArrayList<GermplasmListData>();
		for(int i = 1; i <= itemNo ; i++){
			GermplasmListData listEntry = new GermplasmListData();
			listEntry.setId(i);
			listEntry.setDesignation("Designation " + i);
			listEntry.setEntryCode("EntryCode " + i);
			listEntry.setEntryId(i);
			listEntry.setGroupName("GroupName " + i);
			listEntry.setStatus(1);
			listEntry.setSeedSource("SeedSource " + i);
			
			listEntries.add(listEntry);
		}
		
		return listEntries;
	}
}
