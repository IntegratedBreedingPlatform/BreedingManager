
package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.listinventory.CrossingManagerInventoryTable;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class ParentTabComponentTest {

	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	@Mock
	private ManageCrossingSettingsMain makeCrossesSettingsMain;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private Component parent;

	private ParentTabComponent parentTabComponent;
	private final String parentLabel = "Female Parents";
	private final Integer rowCount = 10;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		final CrossingManagerMakeCrossesComponent makeCrossesMain = new CrossingManagerMakeCrossesComponent(makeCrossesSettingsMain);
		makeCrossesMain.setModeViewOnly(ModeView.LIST_VIEW);
		final MakeCrossesParentsComponent source = new MakeCrossesParentsComponent(makeCrossesMain);
		this.parentTabComponent = new ParentTabComponent(makeCrossesMain, source, this.parentLabel, this.rowCount);
		Mockito.doReturn("TestString").when(this.messageSource).getMessage(Mockito.any(Message.class));
		this.parentTabComponent.setMessageSource(this.messageSource);
		this.parentTabComponent.setOntologyDataManager(this.ontologyDataManager);
		Mockito.doReturn(new Window()).when(this.parent).getWindow();
		this.parentTabComponent.setParent(this.parent);
	}

	@Test
	public void testInitializeParentTable_returnsTheValueFromColumLabelDefaultName() {
		Integer rowCount = 10;
		this.parentTabComponent.setRowCount(rowCount);

		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");

		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();

		this.parentTabComponent.initializeParentTable(tableWithSelectAll);

		Table table = tableWithSelectAll.getTable();

		Assert.assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("LOTS AVAILABLE", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals("SEED RES", table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals("STOCKID", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
	}

	@Test
	public void testInitializeParentTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException {
		final Integer rowCount = 10;
		this.parentTabComponent.setRowCount(rowCount);

		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_RESERVATION.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId())).thenReturn(fromOntology);

		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");

		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();

		this.parentTabComponent.initializeParentTable(tableWithSelectAll);

		Table table = tableWithSelectAll.getTable();

		Assert.assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
	}

	@Test
	public void testDoSaveAction() {
		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.doReturn(fromOntology).when(this.ontologyDataManager).getTermById(Mockito.anyInt());
		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();

		this.parentTabComponent.initializeMainComponents();
		this.parentTabComponent.initializeParentTable(tableWithSelectAll);
		final CrossingManagerInventoryTable inventoryTable = new CrossingManagerInventoryTable(null);
		inventoryTable.setMessageSource(this.messageSource);
		inventoryTable.setOntologyDataManager(this.ontologyDataManager);
		inventoryTable.instantiateComponents();
		this.parentTabComponent.initializeListInventoryTable(inventoryTable);
		this.parentTabComponent.addListeners();
		this.parentTabComponent.setHasChanges(true);

		// function to test
		this.parentTabComponent.doSaveAction();
		Assert.assertNotNull(this.parentTabComponent.getSaveListAsWindow());
	}

	@Test
	public void testDoSaveActionWithNoChanges() {
		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.doReturn(fromOntology).when(this.ontologyDataManager).getTermById(Mockito.anyInt());
		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();

		this.parentTabComponent.initializeMainComponents();
		this.parentTabComponent.initializeParentTable(tableWithSelectAll);
		final CrossingManagerInventoryTable inventoryTable = new CrossingManagerInventoryTable(null);
		inventoryTable.setMessageSource(this.messageSource);
		inventoryTable.setOntologyDataManager(this.ontologyDataManager);
		inventoryTable.instantiateComponents();
		this.parentTabComponent.initializeListInventoryTable(inventoryTable);
		this.parentTabComponent.addListeners();
		this.parentTabComponent.setHasChanges(false);

		// function to test
		this.parentTabComponent.doSaveAction();
		Assert.assertNull(this.parentTabComponent.getSaveListAsWindow());
	}
}
