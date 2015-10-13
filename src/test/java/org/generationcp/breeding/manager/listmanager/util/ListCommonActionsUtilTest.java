
package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.data.initializer.GermplasmListDataInitializer;
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
		this.listToSave = GermplasmListDataInitializer.createGermplasmList(1);
		this.listEntries = GermplasmListDataInitializer.createGermplasmListData(5);
		this.newEntries = new ArrayList<GermplasmListData>();
		this.entriesToUpdate = new ArrayList<GermplasmListData>();
		this.entriesToDelete = new ArrayList<GermplasmListData>();

		Mockito.when(this.dataManager.countGermplasmListDataByListId(this.listToSave.getId())).thenReturn(5L);
		Mockito.when(this.dataManager.getGermplasmListDataByListId(this.listToSave.getId())).thenReturn(this.listToSave.getListData());

		ListCommonActionsUtil.getNewEntriesToSaveUpdateDelete(this.listToSave, this.listEntries, this.forceHasChanges, this.newEntries,
				this.entriesToUpdate, this.entriesToDelete, this.dataManager, this.source, this.messageSource);

		Assert.assertTrue("Expecting that the newEntries has entries but didn't.", !this.newEntries.isEmpty());
	}

	@Test
	public void testSetDesignationOfMatchingSavedEntry_WhenDesignationIsNull() {
		final GermplasmListData entry = new GermplasmListData();
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setDesignationOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals("Expecting that the designation value is \"-\" when the source designation is null.", "-",
				matchingSavedEntry.getDesignation());
	}

	@Test
	public void testSetDesignationOfMatchingSavedEntry_WhenDesignationIsNotNull() {
		final GermplasmListData entry = new GermplasmListData();
		final String expectedDesignation = "Expected Designation";
		entry.setDesignation(expectedDesignation);
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setDesignationOfMatchingSavedEntry(entry, matchingSavedEntry);

		final String actualDesignation = matchingSavedEntry.getDesignation();
		Assert.assertEquals("Expecting that the designation value is " + expectedDesignation + " but returned " + actualDesignation,
				expectedDesignation, actualDesignation);
	}

	@Test
	public void testSetEntryCodeOfMatchingSavedEntry_WhenEntryCodeIsNull() {
		final GermplasmListData entry = new GermplasmListData();
		entry.setEntryId(1);
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setEntryCodeOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals("Expecting that the entry code value is " + entry.getEntryId() + " when the source entry is null.", entry
				.getEntryId().toString(), matchingSavedEntry.getEntryCode());
	}

	@Test
	public void testSetEntryCodeOfMatchingSavedEntry_WhenEntryCodeIsNotNull() {
		final GermplasmListData entry = new GermplasmListData();
		final String expectedEntryCode = "Expected Entry Code";
		entry.setEntryCode(expectedEntryCode);
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setEntryCodeOfMatchingSavedEntry(entry, matchingSavedEntry);

		final String actualEntryCode = matchingSavedEntry.getEntryCode();
		Assert.assertEquals("Expecting that the entry code value is " + expectedEntryCode + " but returned " + actualEntryCode,
				expectedEntryCode, actualEntryCode);
	}

	@Test
	public void testSetSeedSourceOfMatchingSavedEntry_WhenSeedSourceIsNull() {
		final GermplasmListData entry = new GermplasmListData();
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setSeedSourceOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals("Expecting that the seed source value is \"-\" when the source entry is null.", "-",
				matchingSavedEntry.getSeedSource());
	}

	@Test
	public void testSetSeedSourceOfMatchingSavedEntry_WhenSeedSourceIsNotNull() {
		final GermplasmListData entry = new GermplasmListData();
		final String expectedSeedSource = "Expected Seed Source";
		entry.setSeedSource(expectedSeedSource);
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setSeedSourceOfMatchingSavedEntry(entry, matchingSavedEntry);

		final String actualSeedSource = matchingSavedEntry.getSeedSource();
		Assert.assertEquals("Expecting that the seed source value is " + expectedSeedSource + " but returned " + actualSeedSource,
				expectedSeedSource, actualSeedSource);
	}

	@Test
	public void testSetGroupNameOfMatchingSavedEntry_WhenGroupNameIsNull() {
		final GermplasmListData entry = new GermplasmListData();
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setGroupNameOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals("Expecting that the groupname value is \"-\" when the source entry is null.", "-",
				matchingSavedEntry.getGroupName());
	}

	@Test
	public void testSetGroupNameOfMatchingSavedEntry_WhenGroupNameIsNotNull() {
		final GermplasmListData entry = new GermplasmListData();
		final String expectedGroupName = "Expected Group Name";
		entry.setGroupName(expectedGroupName);
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setGroupNameOfMatchingSavedEntry(entry, matchingSavedEntry);

		final String actualGroupName = matchingSavedEntry.getGroupName();
		Assert.assertEquals("Expecting that the group name value is " + expectedGroupName + " but returned " + actualGroupName,
				expectedGroupName, actualGroupName);
	}

	@Test
	public void testDeleteExistingListEntries() {
		this.listToSave = GermplasmListDataInitializer.createGermplasmList(1);
		final Integer listId = this.listToSave.getId();

		ListCommonActionsUtil.deleteExistingListEntries(listId, this.dataManager);

		Mockito.verify(this.dataManager, Mockito.times(1)).deleteGermplasmListDataByListId(listId);
	}
}
