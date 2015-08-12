
package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;

import org.generationcp.breeding.manager.crossingmanager.pojos.CrossParents;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

import com.vaadin.ui.Table;

public class MakeCrossesTableComponentTest {

	private MakeCrossesTableComponent makeCrossesTableComponent;

	@Mock
	private CrossingManagerMakeCrossesComponent makeCrossesMain;
	@Mock
	private OntologyDataManager ontologyDataManager;
	@Mock
	private Table tableCrossesMade;

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);
		ManagerFactory.getCurrentManagerFactoryThreadLocal().set(Mockito.mock(ManagerFactory.class));

		this.makeCrossesTableComponent = Mockito.spy(new MakeCrossesTableComponent(this.makeCrossesMain));
		this.makeCrossesTableComponent.setOntologyDataManager(this.ontologyDataManager);
		this.makeCrossesTableComponent.setTableCrossesMade(this.tableCrossesMade);
	}

	@Test
	public void testInitializeCrossesMadeTable_returnsTheValueFromColumLabelDefaultName() {

		Table table = new Table();

		Mockito.when(this.makeCrossesTableComponent.getTableCrossesMade()).thenReturn(table);

		this.makeCrossesTableComponent.initializeCrossesMadeTable();

		Assert.assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("Female Parent", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		Assert.assertEquals("Male Parent", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
		Assert.assertEquals("SEED SOURCE", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testInitializeCrossesMadeTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException {
		Table table = new Table();

		Mockito.when(this.makeCrossesTableComponent.getTableCrossesMade()).thenReturn(table);

		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");

		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(fromOntology);

		this.makeCrossesTableComponent.initializeCrossesMadeTable();

		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
	}

	@Test
	public void testHasSameParent_ForEqualGID() {
		GermplasmListEntry femaleParent = new GermplasmListEntry(1, 1, 1);
		GermplasmListEntry maleParent = new GermplasmListEntry(1, 1, 1);

		Assert.assertTrue("Expecting to have the same parent (with same gid) but didn't.",
				this.makeCrossesTableComponent.hasSameParent(femaleParent, maleParent));
	}

	@Test
	public void testHasSameParent_ForNonEqualGID() {
		GermplasmListEntry femaleParent = new GermplasmListEntry(1, 1, 1);
		GermplasmListEntry maleParent = new GermplasmListEntry(2, 2, 2);

		Assert.assertFalse("Expecting to have different parent (with different gid) but didn't.",
				this.makeCrossesTableComponent.hasSameParent(femaleParent, maleParent));
	}

	@Test
	public void testAddItemToMakeCrossesTable_MultiplyParents_WhenTheExcludeSelfIsTrueAndParentsAreDifferent() {
		GermplasmListEntry femaleParent = new GermplasmListEntry(1, 1, 1);
		femaleParent.setDesignation("female parent");
		GermplasmListEntry maleParent = new GermplasmListEntry(2, 2, 2);
		maleParent.setDesignation("male parent");
		String femaleSource = "female source";
		String maleSource = "male source";
		CrossParents parents = new CrossParents(femaleParent, maleParent);
		Mockito.when(this.tableCrossesMade.getItemIds()).thenReturn(new ArrayList());

		this.makeCrossesTableComponent.addItemToMakeCrossesTable(true, femaleParent, femaleSource, maleParent, maleSource, parents);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Mockito.anyString(), femaleParent.getDesignation(), maleParent.getDesignation(), Mockito.anyString()},
					parents);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTable_MultiplyParents_WhenTheExcludeSelfIsTrueAndParentsAreTheSame() {
		GermplasmListEntry femaleParent = new GermplasmListEntry(1, 1, 1);
		femaleParent.setDesignation("parent");
		GermplasmListEntry maleParent = new GermplasmListEntry(1, 1, 1);
		maleParent.setDesignation("parent");
		String femaleSource = "source";
		String maleSource = "source";
		CrossParents parents = new CrossParents(femaleParent, maleParent);
		Mockito.when(this.tableCrossesMade.getItemIds()).thenReturn(new ArrayList());

		this.makeCrossesTableComponent.addItemToMakeCrossesTable(true, femaleParent, femaleSource, maleParent, maleSource, parents);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(0)).addItem(
					new Object[] {1, Mockito.anyString(), femaleParent.getDesignation(), maleParent.getDesignation(), Mockito.anyString()},
					parents);
		} catch (NeverWantedButInvoked e) {
			Assert.fail("Expecting table crosses will not have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTable_MultiplyParents_WhenTheExcludeSelfIsFalse() {
		GermplasmListEntry femaleParent = new GermplasmListEntry(1, 1, 1);
		femaleParent.setDesignation("female parent");
		GermplasmListEntry maleParent = new GermplasmListEntry(2, 2, 2);
		maleParent.setDesignation("male parent");
		String femaleSource = "female source";
		String maleSource = "male source";
		CrossParents parents = new CrossParents(femaleParent, maleParent);
		Mockito.when(this.tableCrossesMade.getItemIds()).thenReturn(new ArrayList());

		this.makeCrossesTableComponent.addItemToMakeCrossesTable(false, femaleParent, femaleSource, maleParent, maleSource, parents);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Mockito.anyString(), femaleParent.getDesignation(), maleParent.getDesignation(), Mockito.anyString()},
					parents);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTable_TopToBottomCrosses_WhenTheExcludeSelfIsTrueAndParentsAreDifferent() {
		String listnameFemaleParent = "Female Parent";
		String listnameMaleParent = "Male Parent";
		GermplasmListEntry femaleParent = new GermplasmListEntry(1, 1, 1);
		femaleParent.setDesignation("female parent");
		GermplasmListEntry maleParent = new GermplasmListEntry(2, 2, 2);
		maleParent.setDesignation("male parent");
		CrossParents parents = new CrossParents(femaleParent, maleParent);
		Mockito.when(this.tableCrossesMade.getItemIds()).thenReturn(new ArrayList());

		this.makeCrossesTableComponent.addItemToMakeCrossesTable(listnameFemaleParent, listnameMaleParent, true, femaleParent, maleParent);
		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Mockito.anyString(), femaleParent.getDesignation(), maleParent.getDesignation(), Mockito.anyString()},
					parents);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTable_TopToBottomCrosses_WhenTheExcludeSelfIsTrueAndParentsAreTheSame() {
		String listnameFemaleParent = "Female Parent";
		String listnameMaleParent = "Male Parent";
		GermplasmListEntry femaleParent = new GermplasmListEntry(1, 1, 1);
		femaleParent.setDesignation("female parent");
		GermplasmListEntry maleParent = new GermplasmListEntry(1, 1, 1);
		maleParent.setDesignation("male parent");
		CrossParents parents = new CrossParents(femaleParent, maleParent);
		Mockito.when(this.tableCrossesMade.getItemIds()).thenReturn(new ArrayList());

		this.makeCrossesTableComponent.addItemToMakeCrossesTable(listnameFemaleParent, listnameMaleParent, true, femaleParent, maleParent);
		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(0)).addItem(
					new Object[] {1, Mockito.anyString(), femaleParent.getDesignation(), maleParent.getDesignation(), Mockito.anyString()},
					parents);
		} catch (NeverWantedButInvoked e) {
			Assert.fail("Expecting table crosses will not have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTable_TopToBottomCrosses_WhenTheExcludeSelfIsFalse() {
		String listnameFemaleParent = "Female Parent";
		String listnameMaleParent = "Male Parent";
		GermplasmListEntry femaleParent = new GermplasmListEntry(1, 1, 1);
		femaleParent.setDesignation("female parent");
		GermplasmListEntry maleParent = new GermplasmListEntry(2, 2, 2);
		maleParent.setDesignation("male parent");
		CrossParents parents = new CrossParents(femaleParent, maleParent);
		Mockito.when(this.tableCrossesMade.getItemIds()).thenReturn(new ArrayList());

		this.makeCrossesTableComponent.addItemToMakeCrossesTable(listnameFemaleParent, listnameMaleParent, false, femaleParent, maleParent);
		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Mockito.anyString(), femaleParent.getDesignation(), maleParent.getDesignation(), Mockito.anyString()},
					parents);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}
}
