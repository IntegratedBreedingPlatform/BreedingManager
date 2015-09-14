
package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.data.initializer.GermplasmListDataInitializer;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Table;

@RunWith(MockitoJUnitRunner.class)
public class SelectParentsListDataComponentTest {

	private static final int GERMPLASM_LIST_ID = 1;
	private static final long NO_OF_ENTRIES = 5;
	private static final String DUMMY_MESSAGE = "Dummy Message";
	private static final String LIST_NAME = "Sample List";

	@Mock
	private MakeCrossesParentsComponent makeCrossesParentsComponent;
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	@Mock
	private OntologyDataManager ontologyDataManager;
	@Mock
	private GermplasmListManager germplasmListManager;
	@Mock
	private CrossingManagerMakeCrossesComponent makeCrossesMain;
	@Mock
	private ViewListHeaderWindow viewListHeaderWindow;
	@Mock
	private SelectParentsComponent selectParentComponent;

	@InjectMocks
	private SelectParentsListDataComponent selectParents = new SelectParentsListDataComponent(
			SelectParentsListDataComponentTest.GERMPLASM_LIST_ID, SelectParentsListDataComponentTest.LIST_NAME,
			this.makeCrossesParentsComponent);

	private GermplasmList germplasmList;

	@Before
	public void setUp() throws Exception {
		this.germplasmList = GermplasmListDataInitializer.createGermplasmList(SelectParentsListDataComponentTest.GERMPLASM_LIST_ID);
		Mockito.doReturn(this.germplasmList).when(this.germplasmListManager)
				.getGermplasmListById(SelectParentsListDataComponentTest.GERMPLASM_LIST_ID);
		Mockito.doReturn(NO_OF_ENTRIES).when(this.germplasmListManager)
				.countGermplasmListDataByListId(SelectParentsListDataComponentTest.GERMPLASM_LIST_ID);

		Mockito.doReturn(this.makeCrossesMain).when(this.makeCrossesParentsComponent).getMakeCrossesMain();
		Mockito.doReturn(this.selectParentComponent).when(this.makeCrossesMain).getSelectParentsComponent();
		Mockito.doReturn(ModeView.LIST_VIEW).when(this.makeCrossesMain).getModeView();

		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.ADD_TO_MALE_LIST);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.ADD_TO_FEMALE_LIST);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.INVENTORY_VIEW);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SELECT_ALL);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SELECT_EVEN_ENTRIES);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SELECT_ODD_ENTRIES);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.COPY_TO_NEW_LIST);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.RESERVE_INVENTORY);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.RETURN_TO_LIST_VIEW);
		Mockito.doReturn(DUMMY_MESSAGE).when(this.messageSource).getMessage(Message.SAVE_CHANGES);

		this.selectParents.instantiateComponents();
	}

	@Test
	public void testInitializeListDataTable_returnsTheValueFromColumLabelDefaultName() {
		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");

		TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		Table table = tableWithSelectAll.getTable();

		this.selectParents.initializeListDataTable(table);

		Assert.assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("LOTS AVAILABLE", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals("SEED RES", table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals("STOCKID", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("ENTRY CODE", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("SEED SOURCE", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testInitializeListDataTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException {
		Long count = 5L;
		this.selectParents.setCount(count);
		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");

		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_RESERVATION.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);

		TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		Table table = tableWithSelectAll.getTable();

		this.selectParents.initializeListDataTable(table);

		Assert.assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

}
