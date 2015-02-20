package org.generationcp.breeding.manager.listmanager.listeners.test;

import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.apache.commons.lang.reflect.FieldUtils;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.listeners.SaveListButtonClickListener;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class SaveListButtonClickListenerTest {

	private SaveListButtonClickListener saveListener;

	@Mock
	private ListBuilderComponent source;

	@Mock
	private GermplasmListManager dataManager;

	@Mock
	private Table listDataTable;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private ContextUtil contextUtil;

	private Project project;

	private GermplasmList germplasmList;
	private static final Integer DUMMY_ID = 1;
	private static final Long DUMMY_LONG_ID = 1L;
	private static final String DUMMY_OPTION = "Dummy Option";

	@Before
	public void setUp() throws MiddlewareQueryException, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		initializeGermplasmList();
		initializeProject();

		when(contextUtil.getCurrentUserLocalId()).thenReturn(DUMMY_ID);

		SaveListButtonClickListener _listener = new SaveListButtonClickListener(source,
				listDataTable,
				messageSource);

		FieldUtils.writeDeclaredField(_listener, "contextUtil", contextUtil, true);

		saveListener = Mockito.spy(_listener);

		Mockito.when(source.getWindow()).thenReturn(new Window());

		Mockito.when(messageSource.getMessage(Message.NAME_CAN_NOT_BE_BLANK))
				.thenReturn(DUMMY_OPTION);

		saveListener.setDataManager(dataManager);
		saveListener.setMessageSource(messageSource);
		saveListener.setInventoryDataManager(inventoryDataManager);
		saveListener.setSource(source);

	}

	private void initializeProject() {
		project = new Project();
		project.setProjectId(DUMMY_LONG_ID);
	}

	private void initializeGermplasmList() {
		germplasmList = new GermplasmList();
		germplasmList.setName("List Name");
		germplasmList.setDescription("This is a description.");
		germplasmList.setDate(20150801L);
		germplasmList.setStatus(Integer.valueOf(1));
		germplasmList.setUserId(DUMMY_ID);
	}

	@Test
	public void testShowErrorOnSavingGermplasmListIsCalledWhenErrorEncounteredOnSaveList()
			throws MiddlewareQueryException {
		Mockito.when(source.getCurrentlySavedGermplasmList()).thenReturn(null);
		Mockito.when(source.getCurrentlySetGermplasmListInfo()).thenReturn(germplasmList);
		Mockito.when(dataManager.addGermplasmList(germplasmList)).thenReturn(null);

		saveListener.doSaveAction(true, true);
		Mockito.verify(saveListener, Mockito.times(1)).showErrorOnSavingGermplasmList(true);

		doThrow(new MiddlewareQueryException("There is an error")).when(dataManager)
				.addGermplasmList(germplasmList);
		saveListener.doSaveAction(true, true);
		Mockito.verify(saveListener, Mockito.times(2)).showErrorOnSavingGermplasmList(true);
	}

	@Test
	public void testValidateListDetailsForListNameThatIsNull() {
		initializeGermplasmList();
		germplasmList.setName(null);
		Assert.assertFalse("Expected to invalidate for germplasm list without listname.",
				saveListener.validateListDetails(germplasmList, null));
	}

	@Test
	public void testValidateListDetailsForListNameThatIsGreaterThan50() {
		initializeGermplasmList();
		germplasmList.setName("abcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijk");
		Assert.assertFalse("Expected to invalidate for germplasm list with listname.length > 50.",
				saveListener.validateListDetails(germplasmList, null));
	}

	@Test
	public void testValidateListDetailsForDescriptionThatIsNull() {
		initializeGermplasmList();
		germplasmList.setDescription(null);
		Assert.assertFalse("Expected to invalidate for germplasm list without description.",
				saveListener.validateListDetails(germplasmList, null));
	}

	@Test
	public void testValidateListDetailsForDescriptionThatIsGreaterThan255() {
		initializeGermplasmList();
		germplasmList.setDescription(
				"abcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijk"
						+ "abcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijk"
						+ "abcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdef"
						+ "ghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijk");
		Assert.assertFalse(
				"Expected to invalidate for germplasm list with description.length > 255.",
				saveListener.validateListDetails(germplasmList, null));
	}

	@Test
	public void testValidateListDetailsForDateThatIsNull() {
		initializeGermplasmList();
		germplasmList.setDate(null);
		;
		Assert.assertFalse("Expected to invalidate for germplasm list without date.",
				saveListener.validateListDetails(germplasmList, null));

	}

	@Test
	public void testCreateContainerPropertyOfAddedColumnToTempTable() {
		Table newTable = new Table();
		Integer noOfColumnsAdded = 0;

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "PREFERRED ID");
		Assert.assertTrue("PREFERRED ID is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "PREFERRED NAME");
		Assert.assertTrue("PREFERRED NAME is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "GERMPLASM DATE");
		Assert.assertTrue("GERMPLASM DATE is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "LOCATIONS");
		Assert.assertTrue("LOCATIONS is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "METHOD NAME");
		Assert.assertTrue("METHOD NAME is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "METHOD ABBREV");
		Assert.assertTrue("METHOD ABBREV is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "METHOD NUMBER");
		Assert.assertTrue("METHOD NUMBER is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "METHOD GROUP");
		Assert.assertTrue("METHOD GROUP is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "CROSS-FEMALE GID");
		Assert.assertTrue("CROSS-FEMALE GID is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable,
				"CROSS-FEMALE PREFERRED NAME");
		Assert.assertTrue("CROSS-FEMALE PREFERRED NAME is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "CROSS-MALE GID");
		Assert.assertTrue("CROSS-MALE GID is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable,
				"CROSS-MALE PREFERRED NAME");
		Assert.assertTrue("CROSS-MALE PREFERRED NAME is added to the newTable.",
				++noOfColumnsAdded == newTable.getColumnHeaders().length);

		saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "DUMMY COLUMN");
		Assert.assertTrue("DUMMY COLUMN is added to the newTable.",
				noOfColumnsAdded == newTable.getColumnHeaders().length);
	}
}
