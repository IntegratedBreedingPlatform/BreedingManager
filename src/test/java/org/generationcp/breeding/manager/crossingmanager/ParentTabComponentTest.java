package org.generationcp.breeding.manager.crossingmanager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;

public class ParentTabComponentTest {
	
    @Mock
    private SimpleResourceBundleMessageSource messageSource;
    @Mock
    private CrossingManagerMakeCrossesComponent makeCrossesMain;
	@Mock
	private MakeCrossesParentsComponent source;
	@Mock
	private OntologyDataManager ontologyDataManager;
	
	private ParentTabComponent parentTabComponent;
	private String parentLabel = "Female Parents";
	private Integer rowCount = 10;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		parentTabComponent = spy(new ParentTabComponent(makeCrossesMain, source, parentLabel, rowCount));
		parentTabComponent.setMessageSource(messageSource);
		parentTabComponent.setOntologyDataManager(ontologyDataManager);
	}
	
	@Test
	public void testInitializeParentTable_returnsTheValueFromColumLabelDefaultName(){
		Integer rowCount = 10;
		parentTabComponent.setRowCount(rowCount);
		
		when(messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		when(messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");
		
		TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		
		doReturn(tableWithSelectAll).when(parentTabComponent).getTableWithSelectAllLayout();
		
		parentTabComponent.initializeParentTable();
		
		Table table = tableWithSelectAll.getTable();
		
		assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals("AVAIL INV", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		assertEquals("SEED RES", table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
	}
	
	@Test
	public void testInitializeParentTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException{
		Integer rowCount = 10;
		parentTabComponent.setRowCount(rowCount);
		
		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		when(ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_RESERVATION.getId())).thenReturn(fromOntology);
		
		when(messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		when(messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");
		
		TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();
		
		doReturn(tableWithSelectAll).when(parentTabComponent).getTableWithSelectAllLayout();
		
		parentTabComponent.initializeParentTable();
		
		Table table = tableWithSelectAll.getTable();
		
		assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
	}
}
