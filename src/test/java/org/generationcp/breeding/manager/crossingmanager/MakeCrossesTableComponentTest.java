
package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;

import org.generationcp.breeding.manager.crossingmanager.pojos.CrossParents;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.service.impl.SeedSourceGenerator;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
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

public class MakeCrossesTableComponentTest {

	private MakeCrossesTableComponent makeCrossesTableComponent;

	@Mock
	private CrossingManagerMakeCrossesComponent makeCrossesMain;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private BreedingManagerTable tableCrossesMade;

	@Mock
	private SeedSourceGenerator seedSourceGenerator;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	private GermplasmListEntry femaleParent;
	private GermplasmListEntry maleParent;
	private String femaleSource = "female source";
	private String maleSource = "male source";
	private CrossParents parents;
	private String listnameFemaleParent = "Female Parent";
	private String listnameMaleParent = "Male Parent";

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);
		ManagerFactory.getCurrentManagerFactoryThreadLocal().set(Mockito.mock(ManagerFactory.class));

		this.makeCrossesTableComponent = Mockito.spy(new MakeCrossesTableComponent(this.makeCrossesMain));
		this.makeCrossesTableComponent.setOntologyDataManager(this.ontologyDataManager);
		this.makeCrossesTableComponent.setTableCrossesMade(this.tableCrossesMade);
		this.makeCrossesTableComponent.setSeedSourceGenerator(this.seedSourceGenerator);
		this.makeCrossesTableComponent.setMessageSource(this.messageSource);
		this.makeCrossesTableComponent.setSeparator("/");

		this.femaleParent = new GermplasmListEntry(1, 1, 1);
		this.femaleParent.setDesignation("female parent");
		this.maleParent = new GermplasmListEntry(1, 1, 1);
		this.maleParent.setDesignation("male parent");
		this.parents = new CrossParents(this.femaleParent, this.maleParent);
		Mockito.when(this.tableCrossesMade.getItemIds()).thenReturn(new ArrayList());
	}

	@Test
	public void testInitializeCrossesMadeTable_returnsTheValueFromColumLabelDefaultName() {

		BreedingManagerTable table = new BreedingManagerTable(10, 10);

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
		BreedingManagerTable table = new BreedingManagerTable(10, 10);

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
		Assert.assertTrue("Expecting to have the same parent (with same gid) but didn't.",
				this.makeCrossesTableComponent.hasSameParent(this.femaleParent, this.maleParent));
	}

	@Test
	public void testHasSameParent_ForNonEqualGID() {
		// change the value of male parent
		this.maleParent = new GermplasmListEntry(2, 2, 2);

		Assert.assertFalse("Expecting to have different parent (with different gid) but didn't.",
				this.makeCrossesTableComponent.hasSameParent(this.femaleParent, this.maleParent));
	}

	@Test
	public void testAddItemToMakeCrossesTable_MultiplyParents_WhenTheExcludeSelfIsTrueAndParentsAreDifferent() {
		this.maleParent = new GermplasmListEntry(2, 2, 2);

		this.makeCrossesTableComponent.addItemToMakeCrossesTable(true, this.femaleParent, this.femaleSource, this.maleParent,
				this.maleSource, this.parents);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Mockito.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Mockito.anyString()}, this.parents);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTable_MultiplyParents_WhenTheExcludeSelfIsTrueAndParentsAreTheSame() {

		this.makeCrossesTableComponent.addItemToMakeCrossesTable(true, this.femaleParent, this.femaleSource, this.maleParent,
				this.maleSource, this.parents);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(0)).addItem(
					new Object[] {1, Mockito.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Mockito.anyString()}, this.parents);
		} catch (NeverWantedButInvoked e) {
			Assert.fail("Expecting table crosses will not have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTable_MultiplyParents_WhenTheExcludeSelfIsFalse() {
		this.maleParent = new GermplasmListEntry(2, 2, 2);

		this.makeCrossesTableComponent.addItemToMakeCrossesTable(false, this.femaleParent, this.femaleSource, this.maleParent,
				this.maleSource, this.parents);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Mockito.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Mockito.anyString()}, this.parents);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTable_TopToBottomCrosses_WhenTheExcludeSelfIsTrueAndParentsAreDifferent() {
		this.maleParent = new GermplasmListEntry(2, 2, 2);

		this.makeCrossesTableComponent.addItemToMakeCrossesTable(this.listnameFemaleParent, this.listnameMaleParent, true,
				this.femaleParent, this.maleParent);
		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Mockito.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Mockito.anyString()}, this.parents);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTable_TopToBottomCrosses_WhenTheExcludeSelfIsTrueAndParentsAreTheSame() {
		this.makeCrossesTableComponent.addItemToMakeCrossesTable(this.listnameFemaleParent, this.listnameMaleParent, true,
				this.femaleParent, this.maleParent);
		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(0)).addItem(
					new Object[] {1, Mockito.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Mockito.anyString()}, this.parents);
		} catch (NeverWantedButInvoked e) {
			Assert.fail("Expecting table crosses will not have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTable_TopToBottomCrosses_WhenTheExcludeSelfIsFalse() {
		this.maleParent = new GermplasmListEntry(2, 2, 2);

		this.makeCrossesTableComponent.addItemToMakeCrossesTable(this.listnameFemaleParent, this.listnameMaleParent, false,
				this.femaleParent, this.maleParent);
		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Mockito.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Mockito.anyString()}, this.parents);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testGenerateSeedSourceStandaloneCrossing() {
		String seedSource = this.makeCrossesTableComponent.generateSeedSource(1, "F:1", 2, "M:2");
		Assert.assertEquals("When crossing standalone (not in context of a Nursery), default seed source format is expected.", "F:1/M:2",
				seedSource);
	}

	@Test
	public void testGenerateSeedSourceCrossingWithNurseryInContext() throws Exception {
		Workbook testWorkbook = new Workbook();
		testWorkbook.setObservations(new ArrayList<MeasurementRow>());
		Mockito.when(this.makeCrossesMain.getNurseryWorkbook()).thenReturn(testWorkbook);
		Mockito.when(
				this.seedSourceGenerator.generateSeedSourceForCross(Mockito.any(Workbook.class), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString(), Mockito.anyString())).thenReturn("MEX-DrySeason-N1-1-2");

		// Run init sequence
		this.makeCrossesTableComponent.afterPropertiesSet();

		String seedSource = this.makeCrossesTableComponent.generateSeedSource(1, "WhateverF", 2, "WhateverM");
		Assert.assertEquals("When crossing in context of a Nursery, seed source should be generated using SeedSourceGenerator service.",
				"MEX-DrySeason-N1-1-2", seedSource);
	}
}
