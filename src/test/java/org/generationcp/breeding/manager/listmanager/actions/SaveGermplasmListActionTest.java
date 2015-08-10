package org.generationcp.breeding.manager.listmanager.actions;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.listimport.actions.SaveGermplasmListAction;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Name;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * The skeleton for the SaveGermplasmListAction test
 */

public class SaveGermplasmListActionTest {

	@InjectMocks
	private SaveGermplasmListAction saveGermplasmListAction;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private GermplasmList germplasmList;

	private List<GermplasmName> germplasmNameObjects = new ArrayList<GermplasmName>();
	private List<Name> newNames = new ArrayList<Name>();
	private String filename = "testFileName";
	private List<Integer> doNotCreateGermplasmsWithId = new ArrayList<Integer>();
	private ImportedGermplasmList importedGermplasmList = Mockito.mock(ImportedGermplasmList.class);
	private Integer seedStorageLocation = 0;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		saveGermplasmListAction = new SaveGermplasmListAction();
		saveGermplasmListAction.setGermplasmListManager(germplasmListManager);
		saveGermplasmListAction.setGermplasmManager(germplasmManager);
		saveGermplasmListAction.setInventoryDataManager(inventoryDataManager);
		saveGermplasmListAction.setOntologyDataManager(ontologyDataManager);
		saveGermplasmListAction.setContextUtil(contextUtil);
	}

	@Test
	@Ignore
	public void testSaveRecords() throws Exception {
		saveGermplasmListAction.saveRecords(germplasmList, germplasmNameObjects, newNames, filename, doNotCreateGermplasmsWithId,
				importedGermplasmList, seedStorageLocation);

	}

}
