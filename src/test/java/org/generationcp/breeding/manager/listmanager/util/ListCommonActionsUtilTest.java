
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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
	public void setUp() {
		MockitoAnnotations.initMocks(this);

	}

	@Test
	public void testGetNewEntriesToSaveUpdateDelete_ForNewEntries() throws MiddlewareQueryException {
		this.forceHasChanges = false;
		this.listToSave = this.getGermplasmList();
		this.listEntries = this.getGermplasmListData(5);
		this.newEntries = new ArrayList<GermplasmListData>();
		this.entriesToUpdate = new ArrayList<GermplasmListData>();
		this.entriesToDelete = new ArrayList<GermplasmListData>();

		Mockito.when(this.dataManager.countGermplasmListDataByListId(this.listToSave.getId())).thenReturn(4L);
		Mockito.when(this.dataManager.getGermplasmListDataByListId(this.listToSave.getId()))
				.thenReturn(this.listToSave.getListData());

		ListCommonActionsUtil.getNewEntriesToSaveUpdateDelete(this.listToSave, this.listEntries, this.forceHasChanges, this.newEntries,
				this.entriesToUpdate, this.entriesToDelete, this.dataManager, this.source, this.messageSource);

		Assert.assertTrue("Expecting that the newEntries has entries but didn't.", !this.newEntries.isEmpty());
	}

	@Test
	public void testSetDesignationOfMatchingSavedEntry_WhenDesignationIsNull() {
		GermplasmListData entry = new GermplasmListData();
		GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setDesignationOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals("Expecting that the designation value is \"-\" when the source designation is null.", "-",
				matchingSavedEntry.getDesignation());
	}

	@Test
	public void testSetDesignationOfMatchingSavedEntry_WhenDesignationIsNotNull() {
		GermplasmListData entry = new GermplasmListData();
		String expectedDesignation = "Expected Designation";
		entry.setDesignation(expectedDesignation);
		GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setDesignationOfMatchingSavedEntry(entry, matchingSavedEntry);

		String actualDesignation = matchingSavedEntry.getDesignation();
		Assert.assertEquals("Expecting that the designation value is " + expectedDesignation + " but returned " + actualDesignation,
				expectedDesignation, actualDesignation);
	}

	@Test
	public void testSetEntryCodeOfMatchingSavedEntry_WhenEntryCodeIsNull() {
		GermplasmListData entry = new GermplasmListData();
		entry.setEntryId(1);
		GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setEntryCodeOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals("Expecting that the entry code value is " + entry.getEntryId() + " when the source entry is null.", entry
				.getEntryId().toString(), matchingSavedEntry.getEntryCode());
	}

	@Test
	public void testSetEntryCodeOfMatchingSavedEntry_WhenEntryCodeIsNotNull() {
		GermplasmListData entry = new GermplasmListData();
		String expectedEntryCode = "Expected Entry Code";
		entry.setEntryCode(expectedEntryCode);
		GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setEntryCodeOfMatchingSavedEntry(entry, matchingSavedEntry);

		String actualEntryCode = matchingSavedEntry.getEntryCode();
		Assert.assertEquals("Expecting that the entry code value is " + expectedEntryCode + " but returned " + actualEntryCode,
				expectedEntryCode, actualEntryCode);
	}

	@Test
	public void testSetSeedSourceOfMatchingSavedEntry_WhenSeedSourceIsNull() {
		GermplasmListData entry = new GermplasmListData();
		GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setSeedSourceOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals("Expecting that the seed source value is \"-\" when the source entry is null.", "-",
				matchingSavedEntry.getSeedSource());
	}

	@Test
	public void testSetSeedSourceOfMatchingSavedEntry_WhenSeedSourceIsNotNull() {
		GermplasmListData entry = new GermplasmListData();
		String expectedSeedSource = "Expected Seed Source";
		entry.setSeedSource(expectedSeedSource);
		GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setSeedSourceOfMatchingSavedEntry(entry, matchingSavedEntry);

		String actualSeedSource = matchingSavedEntry.getSeedSource();
		Assert.assertEquals("Expecting that the seed source value is " + expectedSeedSource + " but returned " + actualSeedSource,
				expectedSeedSource, actualSeedSource);
	}

	@Test
	public void testSetGroupNameOfMatchingSavedEntry_WhenGroupNameIsNull() {
		GermplasmListData entry = new GermplasmListData();
		GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setGroupNameOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals("Expecting that the groupname value is \"-\" when the source entry is null.", "-",
				matchingSavedEntry.getGroupName());
	}

	@Test
	public void testSetGroupNameOfMatchingSavedEntry_WhenGroupNameIsNotNull() {
		GermplasmListData entry = new GermplasmListData();
		String expectedGroupName = "Expected Group Name";
		entry.setGroupName(expectedGroupName);
		GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setGroupNameOfMatchingSavedEntry(entry, matchingSavedEntry);

		String actualGroupName = matchingSavedEntry.getGroupName();
		Assert.assertEquals("Expecting that the group name value is " + expectedGroupName + " but returned " + actualGroupName,
				expectedGroupName, actualGroupName);
	}

	private GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		germplasmList.setName("List 001");
		germplasmList.setDescription("List 001 Description");
		germplasmList.setDate(20150101L);
		germplasmList.setListData(this.getGermplasmListData(4));

		return germplasmList;
	}

	private List<GermplasmListData> getGermplasmListData(Integer itemNo) {
		List<GermplasmListData> listEntries = new ArrayList<GermplasmListData>();
		for (int i = 1; i <= itemNo; i++) {
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
