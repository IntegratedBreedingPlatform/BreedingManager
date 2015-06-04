
package org.generationcp.breeding.manager.listmanager.listeners.test;

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

import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

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

		this.initializeGermplasmList();
		this.initializeProject();

		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(SaveListButtonClickListenerTest.DUMMY_ID);

		SaveListButtonClickListener _listener = new SaveListButtonClickListener(this.source, this.listDataTable, this.messageSource);

		FieldUtils.writeDeclaredField(_listener, "contextUtil", this.contextUtil, true);

		this.saveListener = Mockito.spy(_listener);

		Mockito.when(this.source.getWindow()).thenReturn(new Window());

		Mockito.when(this.messageSource.getMessage(Message.NAME_CAN_NOT_BE_BLANK)).thenReturn(SaveListButtonClickListenerTest.DUMMY_OPTION);

		this.saveListener.setDataManager(this.dataManager);
		this.saveListener.setMessageSource(this.messageSource);
		this.saveListener.setInventoryDataManager(this.inventoryDataManager);
		this.saveListener.setSource(this.source);

	}

	private void initializeProject() {
		this.project = new Project();
		this.project.setProjectId(SaveListButtonClickListenerTest.DUMMY_LONG_ID);
	}

	private void initializeGermplasmList() {
		this.germplasmList = new GermplasmList();
		this.germplasmList.setName("List Name");
		this.germplasmList.setDescription("This is a description.");
		this.germplasmList.setDate(20150801L);
		this.germplasmList.setStatus(Integer.valueOf(1));
		this.germplasmList.setUserId(SaveListButtonClickListenerTest.DUMMY_ID);
	}

	@Test
	public void testShowErrorOnSavingGermplasmListIsCalledWhenErrorEncounteredOnSaveList() throws MiddlewareQueryException {
		Mockito.when(this.source.getCurrentlySavedGermplasmList()).thenReturn(null);
		Mockito.when(this.source.getCurrentlySetGermplasmListInfo()).thenReturn(this.germplasmList);
		Mockito.when(this.dataManager.addGermplasmList(this.germplasmList)).thenReturn(null);

		this.saveListener.doSaveAction(true, true);
		Mockito.verify(this.saveListener, Mockito.times(1)).showErrorOnSavingGermplasmList(true);

		Mockito.doThrow(new MiddlewareQueryException("There is an error")).when(this.dataManager).addGermplasmList(this.germplasmList);
		this.saveListener.doSaveAction(true, true);
		Mockito.verify(this.saveListener, Mockito.times(2)).showErrorOnSavingGermplasmList(true);
	}

	@Test
	public void testValidateListDetailsForListNameThatIsNull() {
		this.initializeGermplasmList();
		this.germplasmList.setName(null);
		Assert.assertFalse("Expected to invalidate for germplasm list without listname.",
				this.saveListener.validateListDetails(this.germplasmList, null));
	}

	@Test
	public void testValidateListDetailsForListNameThatIsGreaterThan50() {
		this.initializeGermplasmList();
		this.germplasmList.setName("abcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijk");
		Assert.assertFalse("Expected to invalidate for germplasm list with listname.length > 50.",
				this.saveListener.validateListDetails(this.germplasmList, null));
	}

	@Test
	public void testValidateListDetailsForDescriptionThatIsNull() {
		this.initializeGermplasmList();
		this.germplasmList.setDescription(null);
		Assert.assertFalse("Expected to invalidate for germplasm list without description.",
				this.saveListener.validateListDetails(this.germplasmList, null));
	}

	@Test
	public void testValidateListDetailsForDescriptionThatIsGreaterThan255() {
		this.initializeGermplasmList();
		this.germplasmList.setDescription("abcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijk"
				+ "abcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijk"
				+ "abcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdef"
				+ "ghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijkabcdefghijk");
		Assert.assertFalse("Expected to invalidate for germplasm list with description.length > 255.",
				this.saveListener.validateListDetails(this.germplasmList, null));
	}

	@Test
	public void testValidateListDetailsForDateThatIsNull() {
		this.initializeGermplasmList();
		this.germplasmList.setDate(null);
		;
		Assert.assertFalse("Expected to invalidate for germplasm list without date.",
				this.saveListener.validateListDetails(this.germplasmList, null));

	}

	@Test
	public void testCreateContainerPropertyOfAddedColumnToTempTable() {
		Table newTable = new Table();
		Integer noOfColumnsAdded = 0;

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "PREFERRED ID");
		Assert.assertTrue("PREFERRED ID is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "PREFERRED NAME");
		Assert.assertTrue("PREFERRED NAME is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "GERMPLASM DATE");
		Assert.assertTrue("GERMPLASM DATE is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "LOCATIONS");
		Assert.assertTrue("LOCATIONS is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "METHOD NAME");
		Assert.assertTrue("METHOD NAME is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "METHOD ABBREV");
		Assert.assertTrue("METHOD ABBREV is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "METHOD NUMBER");
		Assert.assertTrue("METHOD NUMBER is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "METHOD GROUP");
		Assert.assertTrue("METHOD GROUP is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "CROSS-FEMALE GID");
		Assert.assertTrue("CROSS-FEMALE GID is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "CROSS-FEMALE PREFERRED NAME");
		Assert.assertTrue("CROSS-FEMALE PREFERRED NAME is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "CROSS-MALE GID");
		Assert.assertTrue("CROSS-MALE GID is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "CROSS-MALE PREFERRED NAME");
		Assert.assertTrue("CROSS-MALE PREFERRED NAME is added to the newTable.", ++noOfColumnsAdded == newTable.getColumnHeaders().length);

		this.saveListener.createContainerPropertyOfAddedColumnToTempTable(newTable, "DUMMY COLUMN");
		Assert.assertTrue("DUMMY COLUMN is added to the newTable.", noOfColumnsAdded == newTable.getColumnHeaders().length);
	}
}
