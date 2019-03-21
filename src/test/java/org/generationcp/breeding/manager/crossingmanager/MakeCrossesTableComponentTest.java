package org.generationcp.breeding.manager.crossingmanager;

import static org.generationcp.breeding.manager.crossingmanager.ParentTabComponent.TAG_COLUMN_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossParents;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.commons.service.impl.SeedSourceGenerator;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.MeasurementDataTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

public class MakeCrossesTableComponentTest {
	private static final String PEDIGREE = "pedigree ";
	private static final String PREFERRED_NAME = "PREFERRED NAME ";
	private static final String FEMALE_LIST_NAME = "Female Lizt";
	private static final String MALE_LIST_NAME = "Male Lizt";

	private static final int PARENTS_TABLE_ROW_COUNT = 5;

	private MakeCrossesTableComponent makeCrossesTableComponent;

	@Mock
	private CrossingManagerMakeCrossesComponent makeCrossesMain;

	@Mock
	private OntologyDataManager ontologyDataManager;
	
	@Mock
	private GermplasmDataManager germplasmDataManager;
	
	@Mock
	private PedigreeService pedigreeService;
	
	@Mock
	private CrossExpansionProperties crossExpansionProps;

	@Mock
	private TableWithSelectAllLayout tableWithSelectAllLayout;

	@Mock
	private Table tableCrossesMade;

	@Mock
	private SeedSourceGenerator seedSourceGenerator;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	private GermplasmListEntry femaleParent;
	private GermplasmListEntry maleParent;
	private CrossParents parents;

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
		this.makeCrossesTableComponent.setTableWithSelectAllLayout(this.tableWithSelectAllLayout);
		this.makeCrossesTableComponent.setGermplasmDataManager(this.germplasmDataManager);
		this.makeCrossesTableComponent.setPedigreeService(this.pedigreeService);
		this.makeCrossesTableComponent.setCrossExpansionProperties(this.crossExpansionProps);

		this.femaleParent = new GermplasmListEntry(1, 1, 1);
		this.femaleParent.setDesignation("female parent");
		this.maleParent = new GermplasmListEntry(1, 1, 1);
		this.maleParent.setDesignation("male parent");
		this.parents = new CrossParents(this.femaleParent, Arrays.asList(this.maleParent));
		Mockito.when(this.tableCrossesMade.getItemIds()).thenReturn(new ArrayList());
		Mockito.doNothing().when(this.makeCrossesTableComponent).updateCrossesMadeUI();

		Mockito.when(this.messageSource.getMessage(Message.CLEAR_ALL)).thenReturn("CLEAR_ALL");
		Mockito.when(this.messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES)).thenReturn("REMOVE_SELECTED_ENTRIES");
		Mockito.when(this.messageSource.getMessage(Message.SELECT_ALL)).thenReturn("SELECT_ALL");
		Mockito.when(this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES)).thenReturn("SELECT_EVEN_ENTRIES");
		Mockito.when(this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES)).thenReturn("SELECT_ODD_ENTRIES");
	}

	@Test
	public void testInitializeCrossesMadeTableReturnsTheValueFromColumLabelDefaultName() {
		final Term fromOntologyDesignation = new Term();
		fromOntologyDesignation.setName("FEMALE PARENT");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(fromOntologyDesignation);

		final Term fromOntologyAvailableInventory = new Term();
		fromOntologyAvailableInventory.setName("MALE PARENT");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(fromOntologyAvailableInventory);

		final String hashtag = "#";
		final String tag = "Tag";
		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn(tag);
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn(hashtag);

		this.makeCrossesTableComponent.initializeCrossesMadeTable(initializeTable());
		final Table table = makeCrossesTableComponent.getTableCrossesMade();
		Mockito.when(this.makeCrossesTableComponent.getTableCrossesMade()).thenReturn(table);

		assertEquals(TAG_COLUMN_ID, table.getColumnHeader(TAG_COLUMN_ID));
		assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		assertEquals("FEMALE PARENT", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		assertEquals("MALE PARENT", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
		assertEquals("FEMALE CROSS", table.getColumnHeader("FEMALE CROSS"));
		assertEquals("MALE CROSS", table.getColumnHeader("MALE CROSS"));
	}

	@Test
	public void testInitializeCrossesMadeTableReturnsTheValueFromOntologyManager() throws MiddlewareQueryException {

		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");

		final Term fromOntologyDesignation = new Term();
		fromOntologyDesignation.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(fromOntology);

		this.makeCrossesTableComponent.initializeCrossesMadeTable(initializeTable());
		final Table table = makeCrossesTableComponent.getTableCrossesMade();
		Mockito.when(this.makeCrossesTableComponent.getTableCrossesMade()).thenReturn(table);

		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
	}

	@Test
	public void testHasSameParentForEqualGID() {
		assertTrue("Expecting to have the same parent (with same gid) but didn't.",
				this.makeCrossesTableComponent.hasSameParent(this.femaleParent, this.maleParent));
	}

	@Test
	public void testHasSameParentForNonEqualGID() {
		// change the value of male parent
		this.maleParent = new GermplasmListEntry(2, 2, 2);

		assertFalse("Expecting to have different parent (with different gid) but didn't.",
				this.makeCrossesTableComponent.hasSameParent(this.femaleParent, this.maleParent));
	}

	@Test
	public void testAddItemToMakeCrossesTableWhenTheExcludeSelfIsTrueAndParentsAreDifferent() {
		this.femaleParent = new GermplasmListEntry(2, 2, 2);
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = this.createPedigreeStringMap(2);

		this.makeCrossesTableComponent
				.addItemToMakeCrossesTable(FEMALE_LIST_NAME, MALE_LIST_NAME, true, this.femaleParent, this.maleParent, existingCrosses,
						germplasmWithPreferredName, parentsPedigreeString);

		try {

			final ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);
			final ArgumentCaptor<Object> itemIdCaptor = ArgumentCaptor.forClass(Object.class);
			
			Mockito.verify(tableCrossesMade, Mockito.times(1)).addItem(argumentCaptor.capture(), itemIdCaptor.capture());
			final Object[] newItemData = argumentCaptor.getValue();
			final CrossParents crossParents = (CrossParents) itemIdCaptor.getValue();

			// Verify the create cross parents
			assertEquals(maleParent.getGid(), crossParents.getMaleParents().get(0).getGid());
			assertEquals(femaleParent.getGid(), crossParents.getFemaleParent().getGid());

			// Verify the visible column data
			assertEquals(false, ((CheckBox) newItemData[0]).booleanValue());
			assertEquals(1, ((Integer) newItemData[1]).intValue());
			assertEquals("Unknown", ((Button) newItemData[2]).getCaption());
			assertEquals("Unknown", (((HorizontalLayout) newItemData[3]).getComponent(0)).getCaption());
			assertEquals("pedigree 2", newItemData[4]);
			assertEquals("pedigree 1", newItemData[5]);
		} catch (final TooLittleActualInvocations e) {
			fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTableWhenTheExcludeSelfIsTrueAndParentsAreTheSame() {
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = new HashMap<>();

		this.makeCrossesTableComponent
			.addItemToMakeCrossesTable(FEMALE_LIST_NAME, MALE_LIST_NAME, true, this.femaleParent, this.maleParent, existingCrosses,
					germplasmWithPreferredName, parentsPedigreeString);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(0)).addItem(
					ArgumentMatchers.<Object[]>any(), ArgumentMatchers.eq(this.parents));
		} catch (final NeverWantedButInvoked e) {
			fail("Expecting table crosses will not have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTableWhenTheExcludeSelfIsFalse() {
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = this.createPedigreeStringMap(2);
		this.makeCrossesTableComponent
			.addItemToMakeCrossesTable(FEMALE_LIST_NAME, MALE_LIST_NAME, false, this.femaleParent, this.maleParent, existingCrosses,
					germplasmWithPreferredName, parentsPedigreeString);

		try {
			final ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);
			final ArgumentCaptor<Object> itemIdCaptor = ArgumentCaptor.forClass(Object.class);
			
			Mockito.verify(tableCrossesMade, Mockito.times(1)).addItem(argumentCaptor.capture(), itemIdCaptor.capture());
			final Object[] newItemData = argumentCaptor.getValue();
			final CrossParents crossParents = (CrossParents) itemIdCaptor.getValue();

			// Verify the create cross parents
			assertEquals(maleParent.getGid(), crossParents.getMaleParents().get(0).getGid());
			assertEquals(femaleParent.getGid(), crossParents.getFemaleParent().getGid());

			// Verify the visible column data
			assertEquals(false, ((CheckBox) newItemData[0]).booleanValue());
			assertEquals(1, ((Integer) newItemData[1]).intValue());
			assertEquals("Unknown", ((Button) newItemData[2]).getCaption());
			assertEquals("Unknown", (((HorizontalLayout) newItemData[3]).getComponent(0)).getCaption());
			assertEquals("pedigree 1", newItemData[4]);
			assertEquals("pedigree 1", newItemData[5]);

		} catch (final TooLittleActualInvocations e) {
			fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTableForMultipleParentsWhenTheExcludeSelfIsFalse() {
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = this.createPedigreeStringMap(2);
		this.makeCrossesTableComponent
			.addItemToMakeCrossesTable(FEMALE_LIST_NAME, MALE_LIST_NAME, false, this.femaleParent, Arrays.asList(this.maleParent), existingCrosses,
				germplasmWithPreferredName, parentsPedigreeString);

		try {
			final ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);
			final ArgumentCaptor<Object> itemIdCaptor = ArgumentCaptor.forClass(Object.class);

			Mockito.verify(tableCrossesMade, Mockito.times(1)).addItem(argumentCaptor.capture(), itemIdCaptor.capture());
			final Object[] newItemData = argumentCaptor.getValue();
			final CrossParents crossParents = (CrossParents) itemIdCaptor.getValue();

			// Verify the create cross parents
			assertEquals(maleParent.getGid(), crossParents.getMaleParents().get(0).getGid());
			assertEquals(femaleParent.getGid(), crossParents.getFemaleParent().getGid());

			// Verify the visible column data
			assertEquals(false, ((CheckBox) newItemData[0]).booleanValue());
			assertEquals(1, ((Integer) newItemData[1]).intValue());
			assertEquals("Unknown", ((Button) newItemData[2]).getCaption());
			assertEquals("Unknown", (((HorizontalLayout) newItemData[3]).getComponent(0)).getCaption());
			assertEquals("pedigree 1", newItemData[4]);
			assertEquals("pedigree 1", newItemData[5]);

		} catch (final TooLittleActualInvocations e) {
			fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testAddItemToMakeCrossesTableForMultipleParentsWhenTheExcludeSelfIsTrue() {
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = this.createPedigreeStringMap(2);
		this.makeCrossesTableComponent
			.addItemToMakeCrossesTable(FEMALE_LIST_NAME, MALE_LIST_NAME, true, this.femaleParent, Arrays.asList(this.maleParent), existingCrosses,
				germplasmWithPreferredName, parentsPedigreeString);

		try {
			final ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);
			final ArgumentCaptor<Object> itemIdCaptor = ArgumentCaptor.forClass(Object.class);

			Mockito.verify(tableCrossesMade, Mockito.never()).addItem(argumentCaptor.capture(), itemIdCaptor.capture());
		} catch (final TooLittleActualInvocations e) {
			fail("Expecting table crosses will have an added entry but didn't.");
		}
	}

	@Test
	public void testGetMaleParentCell() {
		List<GermplasmListEntry> maleEntries = this.createListEntries(1);
		Mockito.doReturn(this.createGermplasmWithPreferredName(1)).when(this.germplasmDataManager).getGermplasmWithAllNamesAndAncestry(ArgumentMatchers.<Set<Integer>>any(), ArgumentMatchers.eq(0));
		Map<Integer, Germplasm> preferredNamesMap = this.makeCrossesTableComponent.getGermplasmWithPreferredNameForBothParents(Arrays.asList(this.femaleParent), maleEntries);
		HorizontalLayout maleParentsCell = this.makeCrossesTableComponent.getMaleParentCell(maleEntries, preferredNamesMap);
		Assert.assertEquals(PREFERRED_NAME + 1, maleParentsCell.getComponent(0).getCaption());

		maleEntries = this.createListEntries(2);
		Mockito.doReturn(this.createGermplasmWithPreferredName(2)).when(this.germplasmDataManager).getGermplasmWithAllNamesAndAncestry(ArgumentMatchers.<Set<Integer>>any(), ArgumentMatchers.eq(0));
		preferredNamesMap = this.makeCrossesTableComponent.getGermplasmWithPreferredNameForBothParents(Arrays.asList(this.femaleParent), maleEntries);
		maleParentsCell = this.makeCrossesTableComponent.getMaleParentCell(maleEntries, preferredNamesMap);
		Assert.assertEquals(MakeCrossesTableComponent.OPENING_SQUARE_BRACKET, ((Label)maleParentsCell.getComponent(0)).getValue());
		Assert.assertEquals( PREFERRED_NAME + 1, maleParentsCell.getComponent(1).getCaption());
		Assert.assertEquals(MakeCrossesTableComponent.SEPARATOR, ((Label)maleParentsCell.getComponent(2)).getValue());
		Assert.assertEquals( PREFERRED_NAME + 2, maleParentsCell.getComponent(3).getCaption());
		Assert.assertEquals(MakeCrossesTableComponent.CLOSING_SQUARE_BRACKET, ((Label)maleParentsCell.getComponent(4)).getValue());
	}

	@Test
	public void testGenerateMalePedigreeString() {
		List<GermplasmListEntry> maleEntries = this.createListEntries(1);
		Map<Integer, String> parentPedigreeStringMap = this.createPedigreeStringMap(1);
		String malePedigreeString = this.makeCrossesTableComponent.generateMalePedigreeString(maleEntries, parentPedigreeStringMap);
		Assert.assertEquals(PEDIGREE + 1, malePedigreeString);

		maleEntries = this.createListEntries(2);
		parentPedigreeStringMap = this.createPedigreeStringMap(2);
		malePedigreeString = this.makeCrossesTableComponent.generateMalePedigreeString(maleEntries, parentPedigreeStringMap);
		Assert.assertEquals(MakeCrossesTableComponent.OPENING_SQUARE_BRACKET + PEDIGREE + 1 + MakeCrossesTableComponent.SEPARATOR + PEDIGREE + 2 + MakeCrossesTableComponent.CLOSING_SQUARE_BRACKET, malePedigreeString);
	}

	@Test
	public void testRemoveSelfIfNecessary() {
		final GermplasmListEntry femaleParent = this.createListEntries(1).get(0);
		List<GermplasmListEntry> maleEntries = this.createListEntries(2);
		maleEntries.add(femaleParent);
		Assert.assertEquals(3, maleEntries.size());
		this.makeCrossesTableComponent.removeSelfIfNecessary(femaleParent, maleEntries, false);
		Assert.assertEquals(3, maleEntries.size());
		this.makeCrossesTableComponent.removeSelfIfNecessary(femaleParent, maleEntries, true);
		Assert.assertEquals(1, maleEntries.size());
	}

	@Test
	public void testSetMaleParentsSeedSource() {
		final List<GermplasmListEntry> maleEntries = this.createListEntries(1);
		this.makeCrossesTableComponent.setMaleParentsSeedSource(maleEntries, MALE_LIST_NAME);
		Assert.assertEquals(MALE_LIST_NAME + ":1", maleEntries.get(0).getSeedSource());
	}

	@Test
	public void testGenerateSeedSource() {
		final Workbook workbook =  Mockito.mock(Workbook.class);
		Mockito.when(this.makeCrossesMain.getWorkbook()).thenReturn(workbook);
		final String studyName = "STUDY " + new Random().nextInt();
		Mockito.when(workbook.getStudyName()).thenReturn(studyName);

		final String femaleGid = "50";
		final String femalePlot = "11";
		final String maleGid = "1023";
		final String malePlot = "22";
		final List<MeasurementData> dataList1 = new ArrayList<>();
		dataList1.add(MeasurementDataTestDataInitializer.createMeasurementData(TermId.GID.getId(), TermId.GID.name(), femaleGid));
		dataList1.add(MeasurementDataTestDataInitializer.createMeasurementData(TermId.PLOT_NO.getId(), TermId.PLOT_NO.name(), femalePlot));
		final MeasurementRow row1 = new MeasurementRow(dataList1);
		final List<MeasurementData> dataList2 = new ArrayList<>();
		dataList2.add(MeasurementDataTestDataInitializer.createMeasurementData(TermId.GID.getId(), TermId.GID.name(), maleGid));
		dataList2.add(MeasurementDataTestDataInitializer.createMeasurementData(TermId.PLOT_NO.getId(), TermId.PLOT_NO.name(), malePlot));
		final MeasurementRow row2 = new MeasurementRow(dataList2);
		Mockito.when(workbook.getObservations()).thenReturn(Arrays.asList(row1, row2));

		final List<GermplasmListEntry> maleEntries = this.createListEntries(1);
		maleEntries.get(0).setGid(Integer.valueOf(maleGid));
		final String expectedSeedSource = RandomStringUtils.random(20);
		Mockito.when(this.seedSourceGenerator.generateSeedSourceForCross(workbook, Arrays.asList(malePlot), femalePlot, studyName, studyName)).thenReturn(expectedSeedSource);
		final String seedSource = this.makeCrossesTableComponent.generateSeedSource(Integer.valueOf(femaleGid), maleEntries);
		Assert.assertEquals(expectedSeedSource, seedSource);
	}
	
	@Test
	public void testGenerateSeedSourceWhenParentGIDNotInStudy() {
		final Workbook workbook =  Mockito.mock(Workbook.class);
		Mockito.when(this.makeCrossesMain.getWorkbook()).thenReturn(workbook);
		final String studyName = "STUDY " + new Random().nextInt();
		Mockito.when(workbook.getStudyName()).thenReturn(studyName);

		final String femaleGid = "50";
		final String femalePlot = "11";
		// Male GID is not in study
		final String maleGid = "1023";
		final List<MeasurementData> dataList1 = new ArrayList<>();
		dataList1.add(MeasurementDataTestDataInitializer.createMeasurementData(TermId.GID.getId(), TermId.GID.name(), femaleGid));
		dataList1.add(MeasurementDataTestDataInitializer.createMeasurementData(TermId.PLOT_NO.getId(), TermId.PLOT_NO.name(), femalePlot));
		final MeasurementRow row1 = new MeasurementRow(dataList1);
		Mockito.when(workbook.getObservations()).thenReturn(Arrays.asList(row1));

		final List<GermplasmListEntry> maleEntries = this.createListEntries(1);
		maleEntries.get(0).setGid(Integer.valueOf(maleGid));
		final String expectedSeedSource = RandomStringUtils.random(20);
		// Expecting blank string to be used for seed source generation if plot number is not in study
		Mockito.when(this.seedSourceGenerator.generateSeedSourceForCross(workbook, Arrays.asList(""), femalePlot, studyName, studyName)).thenReturn(expectedSeedSource);
		final String seedSource = this.makeCrossesTableComponent.generateSeedSource(Integer.valueOf(femaleGid), maleEntries);
		Assert.assertEquals(expectedSeedSource, seedSource);
	}
	
	@Test
	public void testGenerateSeedSourceWhenParentIsUnknown() {
		final Workbook workbook =  Mockito.mock(Workbook.class);
		Mockito.when(this.makeCrossesMain.getWorkbook()).thenReturn(workbook);
		final String studyName = "STUDY " + new Random().nextInt();
		Mockito.when(workbook.getStudyName()).thenReturn(studyName);

		final String femaleGid = "50";
		final String femalePlot = "11";
		// Male GID is 0 = Unknown
		final String maleGid = "0";
		final List<MeasurementData> dataList1 = new ArrayList<>();
		dataList1.add(MeasurementDataTestDataInitializer.createMeasurementData(TermId.GID.getId(), TermId.GID.name(), femaleGid));
		dataList1.add(MeasurementDataTestDataInitializer.createMeasurementData(TermId.PLOT_NO.getId(), TermId.PLOT_NO.name(), femalePlot));
		final MeasurementRow row1 = new MeasurementRow(dataList1);
		Mockito.when(workbook.getObservations()).thenReturn(Arrays.asList(row1));

		final List<GermplasmListEntry> maleEntries = this.createListEntries(1);
		maleEntries.get(0).setGid(Integer.valueOf(maleGid));
		final String expectedSeedSource = RandomStringUtils.random(20);
		// Expecting "0" to be used for seed source generation if parent is unknown
		Mockito.when(this.seedSourceGenerator.generateSeedSourceForCross(workbook, Arrays.asList(maleGid), femalePlot, studyName, studyName)).thenReturn(expectedSeedSource);
		final String seedSource = this.makeCrossesTableComponent.generateSeedSource(Integer.valueOf(femaleGid), maleEntries);
		Assert.assertEquals(expectedSeedSource, seedSource);
	}

	@Test
	public void testAddItemToMakeCrossesTableParentsAreNotYetInExistingCrosses() {

		final String listnameFemaleParent = "FemaleList1";
		final String listnameMaleParent = "MaleList1";
		final Boolean excludeSelf = true;
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> pedigreeString = this.createPedigreeStringMap(6);

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
		assertEquals(maleParent.getGid(), itemId.getMaleParents().get(0).getGid());
		assertEquals(femaleParent.getGid(), itemId.getFemaleParent().getGid());

		// Verify the visible column data
		assertEquals(false, ((CheckBox) newItemData[0]).booleanValue());
		assertEquals(1, ((Integer) newItemData[1]).intValue());
		assertEquals("Unknown", ((Button) newItemData[2]).getCaption());
		assertEquals("Unknown", (((HorizontalLayout) newItemData[3]).getComponent(0)).getCaption());
		assertEquals("pedigree 5", newItemData[4]);
		assertEquals("pedigree 6", newItemData[5]);

		assertEquals("The female and male parent cross should be added to the existing crosses", Integer.valueOf(3), Integer.valueOf(existingCrosses.size()));

	}

	@Test
	public void testAddItemToMakeCrossesTableParentsAreAlreadyInExistingCrosses() {

		final String listnameFemaleParent = "FemaleList1";
		final String listnameMaleParent = "MaleList1";
		final Boolean excludeSelf = true;
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> pedigreeString = this.createPedigreeStringMap(2);

		final Set<CrossParents> existingCrosses = this.createCrossParentsList();

		// Create female and male germplasm which are already in existing crosses
		final GermplasmListEntry femaleParent = new GermplasmListEntry(10001, 1, 50);
		final GermplasmListEntry maleParent = new GermplasmListEntry(10002, 2, 51);

		this.makeCrossesTableComponent
			.addItemToMakeCrossesTable(listnameFemaleParent, listnameMaleParent, excludeSelf, femaleParent, maleParent, existingCrosses,
				germplasmWithPreferredName, pedigreeString);

		assertEquals("The female and male parent cross should not be added to the existing crosses", 2, existingCrosses.size());

	}
	
	@Test
	public void testMakeCrossesWithUnknownMaleParent() {
		final int numOfEntries = 5;
		final List<GermplasmListEntry> femaleEntries = this.createListEntries(numOfEntries);
		Mockito.doReturn(this.createGermplasmWithPreferredName(numOfEntries)).when(this.germplasmDataManager).getGermplasmWithAllNamesAndAncestry(ArgumentMatchers.<Set<Integer>>any(), ArgumentMatchers.eq(0));
		Mockito.doReturn(this.createPedigreeStringMap(numOfEntries)).when(this.pedigreeService).getCrossExpansions(ArgumentMatchers.<Set<Integer>>any(), ArgumentMatchers.<Integer>any(), ArgumentMatchers.eq(this.crossExpansionProps));
		
		this.makeCrossesTableComponent.makeCrossesWithUnknownMaleParent(femaleEntries, FEMALE_LIST_NAME);

		final ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);
		final ArgumentCaptor<Object> itemIdCaptor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(this.tableCrossesMade, Mockito.times(numOfEntries)).addItem(argumentCaptor.capture(), itemIdCaptor.capture());
		
		final List<Object[]> items = argumentCaptor.getAllValues();
		final ListIterator<Object[]> itemsIterator = items.listIterator();
		final List<Object> itemIds = itemIdCaptor.getAllValues();
		final ListIterator<Object> itemIdsIterator = itemIds.listIterator();
		for (int i = 0; i < numOfEntries; i++) {
			final CrossParents parents = (CrossParents) itemIdsIterator.next();
			final Object[] item = itemsIterator.next();
			
			final int gid = i+1;
			assertEquals(gid, parents.getFemaleParent().getGid().intValue());
			assertEquals(0, parents.getMaleParents().get(0).getGid().intValue());
			
			// Verify the visible column data
			assertEquals(false, ((CheckBox) item[0]).booleanValue());
			assertEquals(PREFERRED_NAME + gid, ((Button) item[2]).getCaption());
			assertEquals(Name.UNKNOWN, (((HorizontalLayout) item[3]).getComponent(0)).getCaption());
			assertEquals(PEDIGREE + gid, item[4]);
			assertEquals(Name.UNKNOWN, item[5]);
			
		}
	}

	@Test
	public void testMakeCrossesWithMultipleMaleParents() {
		final int numOfFemaleEntries = 2;
		final List<GermplasmListEntry> femaleEntries = this.createListEntries(numOfFemaleEntries);
		final List<GermplasmListEntry> maleEntries = this.createListEntries(numOfFemaleEntries);
		final Map<Integer, String> parentPedigreeStringMap = this.createPedigreeStringMap(numOfFemaleEntries);
		Mockito.doReturn(this.createGermplasmWithPreferredName(numOfFemaleEntries)).when(this.germplasmDataManager).getGermplasmWithAllNamesAndAncestry(ArgumentMatchers.<Set<Integer>>any(), ArgumentMatchers.eq(0));
		Mockito.doReturn(parentPedigreeStringMap).when(this.pedigreeService).getCrossExpansions(ArgumentMatchers.<Set<Integer>>any(), ArgumentMatchers.<Integer>any(), ArgumentMatchers.eq(this.crossExpansionProps));

		this.makeCrossesTableComponent.makeCrossesWithMultipleMaleParents(femaleEntries, maleEntries, FEMALE_LIST_NAME, MALE_LIST_NAME, false);

		final ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);
		final ArgumentCaptor<Object> itemIdCaptor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(this.tableCrossesMade, Mockito.times(numOfFemaleEntries)).addItem(argumentCaptor.capture(), itemIdCaptor.capture());

		final List<Object[]> items = argumentCaptor.getAllValues();
		final ListIterator<Object[]> itemsIterator = items.listIterator();
		final List<Object> itemIds = itemIdCaptor.getAllValues();
		final ListIterator<Object> itemIdsIterator = itemIds.listIterator();
		final String malePedigreeString = this.makeCrossesTableComponent.generateMalePedigreeString(maleEntries, parentPedigreeStringMap);
		for (int i = 0; i < numOfFemaleEntries; i++) {
			final CrossParents parents = (CrossParents) itemIdsIterator.next();
			final Object[] item = itemsIterator.next();

			final int gid = i+1;
			assertEquals(gid, parents.getFemaleParent().getGid().intValue());
			for(int j = 1; j<=numOfFemaleEntries; j++) {
				assertEquals(j, parents.getMaleParents().get(j-1).getGid().intValue());
			}

			// Verify the visible column data
			assertEquals(false, ((CheckBox) item[0]).booleanValue());
			assertEquals(PREFERRED_NAME + gid, ((Button) item[2]).getCaption());
			final HorizontalLayout maleParentsCell = ((HorizontalLayout) item[3]);
			final Iterator<Component> componentIterator = maleParentsCell.getComponentIterator();
			int componentCounter = 1;
			while(componentIterator.hasNext()) {
				Component component = componentIterator.next();
				if(maleParentsCell.getComponentIndex(component)%2!=0) {
					assertEquals(PREFERRED_NAME + componentCounter, component.getCaption());
					componentCounter++;
				}
			};
			assertEquals(PEDIGREE + gid, item[4]);
			assertEquals(malePedigreeString, item[5]);
		}
	}

	private Map<Integer, String> createPedigreeStringMap(int numOfGids) {

		final Map<Integer, String> pedigreeString = new HashMap<>();
		for (int i = 1; i <= numOfGids; i++) {
			pedigreeString.put(i, PEDIGREE + i);
		}

		return pedigreeString;
	}

	private Set<CrossParents> createCrossParentsList() {

		final HashSet<CrossParents> crossParentsList = new HashSet<>();

		// Create cross parents with objects that contain unique combination of male and female germplasm entry
		crossParentsList.add(createCrossParent(100, 1, 50, 101, 2, 51));
		crossParentsList.add(createCrossParent(102, 3, 52, 103, 4, 53));

		return crossParentsList;

	}

	private CrossParents createCrossParent(
		final Integer femaleListDataId, final Integer femaleGid, final Integer femaleEntryId, final Integer maleListDataId,
		final Integer maleGid, final Integer maleEntryId) {

		final GermplasmListEntry femaleParent = new GermplasmListEntry(femaleListDataId, femaleGid, femaleEntryId);
		final GermplasmListEntry maleParent = new GermplasmListEntry(maleListDataId, maleGid, maleEntryId);

		final CrossParents crossParents = new CrossParents(femaleParent, Arrays.asList(maleParent));
		return crossParents;

	}
	
	private List<GermplasmListEntry> createListEntries(final int numOfEntries) {
		final List<GermplasmListEntry> entries = new ArrayList<>();
		final Random random = new Random();
		for (int i = 1; i <= numOfEntries; i++){			
			entries.add(new GermplasmListEntry(random.nextInt(), i, i));
		}
		return entries;
	}
	
	private List<Germplasm> createGermplasmWithPreferredName(final int numOfEntries) {
		final List<Germplasm> list = new ArrayList<>();
		final Random random = new Random();
		for (int i = 1; i <= numOfEntries; i++){			
			final Germplasm germplasm = new Germplasm(i);
			final Name name = new Name();
			name.setNval(PREFERRED_NAME + i);
			germplasm.setPreferredName(name) ;
			list.add(germplasm);
		}
		return list;
	}
	
	private List<GermplasmListEntry> creteGermplasm(final int numOfEntries) {
		final List<GermplasmListEntry> femaleEntries = new ArrayList<>();
		final Random random = new Random();
		for (int i = 1; i <= numOfEntries; i++){			
			femaleEntries.add(new GermplasmListEntry(random.nextInt(), i, i));
		}
		return femaleEntries;
	}

	private TableWithSelectAllLayout initializeTable() {
		final TableWithSelectAllLayout tableWithSelectAllLayout = new TableWithSelectAllLayout(PARENTS_TABLE_ROW_COUNT, TAG_COLUMN_ID);
		tableWithSelectAllLayout.instantiateComponents();
		return tableWithSelectAllLayout;
	}
}
