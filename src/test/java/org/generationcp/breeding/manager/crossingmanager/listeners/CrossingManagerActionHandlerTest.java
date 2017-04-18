
package org.generationcp.breeding.manager.crossingmanager.listeners;

import java.util.ArrayList;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMakeCrossesComponent;
import org.generationcp.breeding.manager.crossingmanager.MakeCrossesParentsComponent;
import org.generationcp.breeding.manager.crossingmanager.ParentTabComponent;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.listinventory.CrossingManagerInventoryTable;
import org.generationcp.breeding.manager.inventory.ReserveInventoryActionFactory;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.data.Container;
import com.vaadin.ui.Table;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class CrossingManagerActionHandlerTest {

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private Table table;

	private CrossingManagerActionHandler crossingManagerActionHandler;

	private GermplasmListTestDataInitializer germplasmListTestDataInitializer;

	@Before
	public void setUp() {
		this.germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();

		Mockito.doReturn("TestString").when(this.messageSource).getMessage(Matchers.any(Message.class));
		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.doReturn(fromOntology).when(this.ontologyDataManager).getTermById(Matchers.anyInt());

		Mockito.doReturn(new ArrayList<Integer>()).when(this.table).getValue();
		Mockito.doReturn(Mockito.mock(Container.class)).when(this.table).getContainerDataSource();

		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();

		final MakeCrossesParentsComponent makeCrossesParentsComponent =
				new MakeCrossesParentsComponent(Mockito.mock(CrossingManagerMakeCrossesComponent.class));
		final ParentTabComponent parentTabComponent = new ParentTabComponent(Mockito.mock(CrossingManagerMakeCrossesComponent.class),
				makeCrossesParentsComponent, "test", 10, new ReserveInventoryActionFactory());
		parentTabComponent.setMessageSource(this.messageSource);
		parentTabComponent.setOntologyDataManager(this.ontologyDataManager);
		parentTabComponent.initializeMainComponents();
		parentTabComponent.initializeParentTable(tableWithSelectAll);
		final CrossingManagerInventoryTable inventoryTable = new CrossingManagerInventoryTable(null);
		inventoryTable.setMessageSource(this.messageSource);
		inventoryTable.setOntologyDataManager(this.ontologyDataManager);
		inventoryTable.instantiateComponents();
		parentTabComponent.initializeListInventoryTable(inventoryTable);
		parentTabComponent.addListeners();
		parentTabComponent.setGermplasmList(this.germplasmListTestDataInitializer.createGermplasmList(10));
		makeCrossesParentsComponent.setFemaleParentTab(parentTabComponent);
		makeCrossesParentsComponent.setMaleParentTab(parentTabComponent);
		makeCrossesParentsComponent
				.setMakeCrossesMain(new CrossingManagerMakeCrossesComponent(Mockito.mock(ManageCrossingSettingsMain.class)));
		this.crossingManagerActionHandler = new CrossingManagerActionHandler(makeCrossesParentsComponent);
	}

	@Test
	public void testHandleActionRemoveSelectedEntries() throws Exception {

		this.crossingManagerActionHandler.handleAction(CrossingManagerActionHandler.ACTION_REMOVE_SELECTED_ENTRIES, this.table,
				Mockito.mock(Table.class));

		Assert.assertTrue(this.crossingManagerActionHandler.getSource() instanceof MakeCrossesParentsComponent);
		final MakeCrossesParentsComponent makeCrosses = (MakeCrossesParentsComponent) this.crossingManagerActionHandler.getSource();
		Assert.assertNotNull(makeCrosses.getFemaleList());
		Assert.assertNotNull(makeCrosses.getMaleList());
	}

}
