
package org.generationcp.breeding.manager.action;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * The skeleton for the SaveGermplasmListAction test
 */

public class SaveGermplasmListActionTest {

	private static final int LIST_ID = 1;

	@Mock
	private SaveGermplasmListActionSource source;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@InjectMocks
	private SaveGermplasmListAction saveGermplasmListAction;

	private GermplasmList germplasmList;
	private List<GermplasmListEntry> listEntries;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		this.germplasmList = this.getGermplasmList();
		this.listEntries = this.getGermplasmListEntries();

		this.saveGermplasmListAction = Mockito.spy(new SaveGermplasmListAction(this.source, this.germplasmList, this.listEntries));
		this.saveGermplasmListAction.setContextUtil(this.contextUtil);
		this.saveGermplasmListAction.setCrossExpansionProperties(this.crossExpansionProperties);
		this.saveGermplasmListAction.setInventoryDataManager(this.inventoryDataManager);
		this.saveGermplasmListAction.setPedigreeService(this.pedigreeService);
		this.saveGermplasmListAction.setGermplasmListManager(this.germplasmListManager);

		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager).getGermplasmListById(this.germplasmList.getId());
		Mockito.doReturn(LIST_ID).when(this.germplasmListManager).updateGermplasmList(this.germplasmList);
		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager).getGermplasmListById(LIST_ID);
	}

	private List<GermplasmListEntry> getGermplasmListEntries() {
		List<GermplasmListEntry> listEntries = new ArrayList<GermplasmListEntry>();

		listEntries.add(new GermplasmListEntry(1, 1, 1));
		listEntries.add(new GermplasmListEntry(2, 2, 2));

		return listEntries;
	}

	private GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(LIST_ID);
		germplasmList.setName("Germplasm List");
		germplasmList.setDescription("Germplasm List Description");
		return germplasmList;
	}

	@Test
	public void testSaveRecords() throws Exception {
		GermplasmList germplasmList = this.saveGermplasmListAction.saveRecords();

		Assert.assertEquals("Expecting that a list id is returned after succesfull saving of records but didn't.", LIST_ID, germplasmList
				.getId().intValue());

	}

}
