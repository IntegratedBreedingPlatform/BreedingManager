package org.generationcp.breeding.manager.crossingmanager;

import com.vaadin.ui.Button;
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
import org.generationcp.middleware.pojos.Germplasm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Ignore("test commented until finish the ticket BMS-3785")
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
	private final String femaleSource = "female source";
	private final String maleSource = "male source";
	private CrossParents parents;
	private final String listnameFemaleParent = "Female Parent";
	private final String listnameMaleParent = "Male Parent";

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
	public void testInitializeCrossesMadeTableReturnsTheValueFromColumLabelDefaultName() {

		final BreedingManagerTable table = new BreedingManagerTable(10, 10);

		Mockito.when(this.makeCrossesTableComponent.getTableCrossesMade()).thenReturn(table);

		this.makeCrossesTableComponent.initializeCrossesMadeTable();

		Assert.assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("Female Parent", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		Assert.assertEquals("Male Parent", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
		Assert.assertEquals("SEED SOURCE", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testInitializeCrossesMadeTableReturnsTheValueFromOntologyManager() throws MiddlewareQueryException {
		final BreedingManagerTable table = new BreedingManagerTable(10, 10);

		Mockito.when(this.makeCrossesTableComponent.getTableCrossesMade()).thenReturn(table);

		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");

		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS_FEMALE_GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS_MALE_GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(fromOntology);

		this.makeCrossesTableComponent.initializeCrossesMadeTable();

		Assert.assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.CROSS_FEMALE_GID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.CROSS_MALE_GID.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
	}

	@Test
	public void testHasSameParentForEqualGID() {
		Assert.assertTrue("Expecting to have the same parent (with same gid) but didn't.",
				this.makeCrossesTableComponent.hasSameParent(this.femaleParent, this.maleParent));
	}

	@Test
	public void testHasSameParentForNonEqualGID() {
		// change the value of male parent
		this.maleParent = new GermplasmListEntry(2, 2, 2);

		Assert.assertFalse("Expecting to have different parent (with different gid) but didn't.",
				this.makeCrossesTableComponent.hasSameParent(this.femaleParent, this.maleParent));
	}

	@Test
	public void testAddItemToMakeCrossesTableMultiplyParentsWhenTheExcludeSelfIsTrueAndParentsAreDifferent() {
		this.maleParent = new GermplasmListEntry(2, 2, 2);
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = new HashMap<>();
		this.makeCrossesTableComponent
				.addItemToMakeCrossesTable(true, this.femaleParent, this.femaleSource, this.maleParent, this.maleSource, this.parents,
						existingCrosses, germplasmWithPreferredName, parentsPedigreeString);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Matchers.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Matchers.anyString()}, this.parents);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTableMultiplyParentsWhenTheExcludeSelfIsTrueAndParentsAreTheSame() {
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = new HashMap<>();

		this.makeCrossesTableComponent
				.addItemToMakeCrossesTable(true, this.femaleParent, this.femaleSource, this.maleParent, this.maleSource, this.parents,
						existingCrosses, germplasmWithPreferredName, parentsPedigreeString);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(0)).addItem(
					new Object[] {1, Matchers.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Matchers.anyString()}, this.parents);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting table crosses will not have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTableMultiplyParentsWhenTheExcludeSelfIsFalse() {
		this.maleParent = new GermplasmListEntry(2, 2, 2);
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = new HashMap<>();

		this.makeCrossesTableComponent
				.addItemToMakeCrossesTable(false, this.femaleParent, this.femaleSource, this.maleParent, this.maleSource, this.parents,
						existingCrosses, germplasmWithPreferredName, parentsPedigreeString);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Matchers.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Matchers.anyString()}, this.parents);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTableTopToBottomCrossesWhenTheExcludeSelfIsTrueAndParentsAreDifferent() {
		this.maleParent = new GermplasmListEntry(2, 2, 2);
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = new HashMap<>();

		this.makeCrossesTableComponent
				.addItemToMakeCrossesTable(this.listnameFemaleParent, this.listnameMaleParent, true, this.femaleParent, this.maleParent,
						existingCrosses, germplasmWithPreferredName, parentsPedigreeString);
		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Matchers.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Matchers.anyString()}, this.parents);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTableTopToBottomCrossesWhenTheExcludeSelfIsTrueAndParentsAreTheSame() {
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = new HashMap<>();

		this.makeCrossesTableComponent
				.addItemToMakeCrossesTable(this.listnameFemaleParent, this.listnameMaleParent, true, this.femaleParent, this.maleParent,
						existingCrosses, germplasmWithPreferredName, parentsPedigreeString);
		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(0)).addItem(
					new Object[] {1, Matchers.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Matchers.anyString()}, this.parents);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting table crosses will not have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTableTopToBottomCrossesWhenTheExcludeSelfIsFalse() {
		this.maleParent = new GermplasmListEntry(2, 2, 2);
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = new HashMap<>();

		this.makeCrossesTableComponent
				.addItemToMakeCrossesTable(this.listnameFemaleParent, this.listnameMaleParent, false, this.femaleParent, this.maleParent,
						existingCrosses, germplasmWithPreferredName, parentsPedigreeString);
		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					new Object[] {1, Matchers.anyString(), this.femaleParent.getDesignation(), this.maleParent.getDesignation(),
							Matchers.anyString()}, this.parents);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testGenerateSeedSourceStandaloneCrossing() {
		final String seedSource = this.makeCrossesTableComponent.generateSeedSource(1, "F:1", 2, "M:2");
		Assert.assertEquals("When crossing standalone (not in context of a Nursery), default seed source format is expected.", "F:1/M:2",
				seedSource);
	}

	@Test
	public void testGenerateSeedSourceCrossingWithNurseryInContext() throws Exception {
		final Workbook testWorkbook = new Workbook();
		testWorkbook.setObservations(new ArrayList<MeasurementRow>());
		Mockito.when(this.makeCrossesMain.getNurseryWorkbook()).thenReturn(testWorkbook);
		Mockito.when(this.seedSourceGenerator
				.generateSeedSourceForCross(Matchers.any(Workbook.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
						Matchers.anyString())).thenReturn("MEX-DrySeason-N1-1-2");

		// Run init sequence
		this.makeCrossesTableComponent.afterPropertiesSet();

		final String seedSource = this.makeCrossesTableComponent.generateSeedSource(1, "WhateverF", 2, "WhateverM");
		Assert.assertEquals("When crossing in context of a Nursery, seed source should be generated using SeedSourceGenerator service.",
				"MEX-DrySeason-N1-1-2", seedSource);
	}

//	@Test
//	public void testSaveCrossListWithAllValidationsPassed() throws Exception {
//		// Setup mock and run init sequence
//		Mockito.when(this.makeCrossesMain.isValidationsBeforeSavePassed()).thenReturn(true);
//		Mockito.doNothing().when(this.makeCrossesTableComponent).launchSaveListAsWindow();
//		this.makeCrossesTableComponent.afterPropertiesSet();
//
//		// Enable save button manually so that click listener will be called
//		final Button saveButton = this.makeCrossesTableComponent.getSaveButton();
//		saveButton.setEnabled(true);
//		saveButton.click();
//
//		// Check that Save List As pop-up window was launched
//		Mockito.verify(this.makeCrossesTableComponent, Mockito.times(1)).launchSaveListAsWindow();
//	}

//	@Test
//	public void testSaveCrossListWithValidationError() throws Exception {
//		// Setup mock and run init sequence
//		Mockito.when(this.makeCrossesMain.isValidationsBeforeSavePassed()).thenReturn(false);
//		this.makeCrossesTableComponent.afterPropertiesSet();
//
//		// Enable save button manually so that click listener will be called
//		final Button saveButton = this.makeCrossesTableComponent.getSaveButton();
//		saveButton.setEnabled(true);
//		saveButton.click();
//
//		// Check that Save List As pop-up window was not launched
//		Mockito.verify(this.makeCrossesTableComponent, Mockito.times(0)).launchSaveListAsWindow();
//	}

	@Test
	public void testAddItemToMakeCrossesTableTopToBottomCrossesParentsAreNotYetInExistingCrosses() {

		final String listnameFemaleParent = "FemaleList1";
		final String listnameMaleParent = "MaleList1";
		final Boolean excludeSelf = true;
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> pedigreeString = this.createPedigreeString();

		final Set<CrossParents> existingCrosses = this.createCrossParentsList();

		// Create female and male germplasm which are not yet in existing crosses
		final GermplasmListEntry femaleParent = new GermplasmListEntry(104, 5, 54);
		final GermplasmListEntry maleParent = new GermplasmListEntry(105, 6, 55);

		final ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);
		final ArgumentCaptor<Object> itemIdCaptor = ArgumentCaptor.forClass(Object.class);

		this.makeCrossesTableComponent
				.addItemToMakeCrossesTable(listnameFemaleParent, listnameMaleParent, excludeSelf, femaleParent, maleParent, existingCrosses,
						germplasmWithPreferredName, pedigreeString);

		// Make sure the parent crossed is added to the table
		Mockito.verify(tableCrossesMade, Mockito.times(1)).addItem(argumentCaptor.capture(), itemIdCaptor.capture());
		final Object[] newItemData = argumentCaptor.getValue();
		final CrossParents itemId = (CrossParents) itemIdCaptor.getValue();

		// Verify the create cross parents
		Assert.assertEquals(maleParent.getGid(), itemId.getMaleParent().getGid());
		Assert.assertEquals(femaleParent.getGid(), itemId.getFemaleParent().getGid());
		Assert.assertEquals("MaleList1:55", itemId.getMaleParent().getSeedSource());
		Assert.assertEquals("FemaleList1:54", itemId.getFemaleParent().getSeedSource());

		// Verify the visible column data
		Assert.assertEquals(3, newItemData[0]);
		Assert.assertEquals("pedigree 5", newItemData[1]);
		Assert.assertEquals(null, newItemData[2]);
		Assert.assertEquals("Unknown", newItemData[3]);
		Assert.assertEquals("Unknown", newItemData[4]);
		Assert.assertEquals("FemaleList1:54/MaleList1:55", newItemData[5]);

		Assert.assertEquals("The female and male parent cross should be added to the existing crosses", 3, existingCrosses.size());

	}

	@Test
	public void testAddItemToMakeCrossesTableTopToBottomCrossesParentsAreAlreadyInExistingCrosses() {

		final String listnameFemaleParent = "FemaleList1";
		final String listnameMaleParent = "MaleList1";
		final Boolean excludeSelf = true;
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> pedigreeString = this.createPedigreeString();

		final Set<CrossParents> existingCrosses = this.createCrossParentsList();

		// Create female and male germplasm which are already in existing crosses
		final GermplasmListEntry femaleParent = new GermplasmListEntry(10001, 1, 50);
		final GermplasmListEntry maleParent = new GermplasmListEntry(10002, 2, 51);

		this.makeCrossesTableComponent
				.addItemToMakeCrossesTable(listnameFemaleParent, listnameMaleParent, excludeSelf, femaleParent, maleParent, existingCrosses,
						germplasmWithPreferredName, pedigreeString);

		Assert.assertEquals("The female and male parent cross should not be added to the existing crosses", 2, existingCrosses.size());

	}

	private Map<Integer, String> createPedigreeString() {

		final Map<Integer, String> pedigreeString = new HashMap<>();
		pedigreeString.put(1, "pedigree 1");
		pedigreeString.put(2, "pedigree 2");
		pedigreeString.put(3, "pedigree 3");
		pedigreeString.put(4, "pedigree 4");
		pedigreeString.put(5, "pedigree 5");

		return pedigreeString;

	}

	private Set<CrossParents> createCrossParentsList() {

		final HashSet<CrossParents> crossParentsList = new HashSet<>();

		// Create cross parents with objects that contain unique combination of male and female germplasm entry
		crossParentsList.add(createCrossParent(100, 1, 50, 101, 2, 51));
		crossParentsList.add(createCrossParent(102, 3, 52, 103, 4, 53));

		return crossParentsList;

	}

	private CrossParents createCrossParent(final Integer femaleListDataId, final Integer femaleGid, final Integer femaleEntryId, final Integer maleListDataId,
			final Integer maleGid, final Integer maleEntryId) {

		final GermplasmListEntry femaleParent = new GermplasmListEntry(femaleListDataId, femaleGid, femaleEntryId);
		final GermplasmListEntry maleParent = new GermplasmListEntry(maleListDataId, maleGid, maleEntryId);

		final CrossParents crossParents = new CrossParents(femaleParent, maleParent);
		return crossParents;

	}

}
