package org.generationcp.breeding.manager.listimport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class SelectGermplasmWindowTest {
    
	@Mock
    private OntologyDataManager ontologyDataManager;
	@Mock
	private ProcessImportedGermplasmAction source;
	@Mock
	private Window parentWindow;
	
	private String germplasmName = "Germplasm Name";
	private int index = 2;
	private Germplasm germplasm;
	
	private SelectGermplasmWindow selectGermplasmWindow;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		
		germplasm = new Germplasm();
		germplasm.setGid(1);
		
		selectGermplasmWindow = new SelectGermplasmWindow(source,germplasmName,index, germplasm, parentWindow);
		selectGermplasmWindow.setOntologyDataManager(ontologyDataManager);
	}
	
	@Test
	public void testInitGermplasmTable_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException{
		Table germplasmTable = new Table();
		selectGermplasmWindow.setGermplasmTable(germplasmTable);
		
		Term fromOntology = new Term();
		when(ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GERMPLASM_LOCATION.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.BREEDING_METHOD_NAME.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		
		selectGermplasmWindow.initGermplasmTable();
		
		Table table = selectGermplasmWindow.getGermplasmTable();
		
		assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		assertEquals("LOCATIONS", table.getColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName()));
		assertEquals("METHOD NAME", table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
		assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
	}
	
	@Test
	public void testInitGermplasmTable_returnsTheValueFromOntology() throws MiddlewareQueryException{
		Table germplasmTable = new Table();
		selectGermplasmWindow.setGermplasmTable(germplasmTable);
		
		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		when(ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GERMPLASM_LOCATION.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.BREEDING_METHOD_NAME.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		
		selectGermplasmWindow.initGermplasmTable();
		
		Table table = selectGermplasmWindow.getGermplasmTable();
		
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GID.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
	}
}
