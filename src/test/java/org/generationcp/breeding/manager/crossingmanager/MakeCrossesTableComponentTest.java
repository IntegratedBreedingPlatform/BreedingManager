package org.generationcp.breeding.manager.crossingmanager;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossParents;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.service.impl.SeedSourceGenerator;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
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

import static org.generationcp.breeding.manager.crossingmanager.ParentTabComponent.TAG_COLUMN_ID;

public class MakeCrossesTableComponentTest {
	private static final String FEMALE_LIST_NAME = "Female Lizt";
	private static final String MALE_LIST_NAME = "Female Lizt";

	private static final int PARENTS_TABLE_ROW_COUNT = 5;

	private MakeCrossesTableComponent makeCrossesTableComponent;

	@Mock
	private CrossingManagerMakeCrossesComponent makeCrossesMain;

	@Mock
	private OntologyDataManager ontologyDataManager;

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
		this.makeCrossesTableComponent.setTableWithSelectAllLayout(this.tableWithSelectAllLayout);

		this.femaleParent = new GermplasmListEntry(1, 1, 1);
		this.femaleParent.setDesignation("female parent");
		this.maleParent = new GermplasmListEntry(1, 1, 1);
		this.maleParent.setDesignation("male parent");
		this.parents = new CrossParents(this.femaleParent, this.maleParent);
		Mockito.when(this.tableCrossesMade.getItemIds()).thenReturn(new ArrayList());
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

		Assert.assertEquals(TAG_COLUMN_ID, table.getColumnHeader(TAG_COLUMN_ID));
		Assert.assertEquals("#", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("FEMALE PARENT", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		Assert.assertEquals("MALE PARENT", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
		Assert.assertEquals("FEMALE CROSS", table.getColumnHeader("FEMALE CROSS"));
		Assert.assertEquals("MALE CROSS", table.getColumnHeader("MALE CROSS"));
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

		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.FEMALE_PARENT.getName()));
		Assert.assertEquals("Ontology Name", table.getColumnHeader(ColumnLabels.MALE_PARENT.getName()));
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
		this.femaleParent = new GermplasmListEntry(2, 2, 2);
		final Set<CrossParents> existingCrosses = new HashSet<>();
		final Map<Integer, Germplasm> germplasmWithPreferredName = new HashMap<>();
		final Map<Integer, String> parentsPedigreeString = new HashMap<>();

		this.makeCrossesTableComponent
				.addItemToMakeCrossesTable(FEMALE_LIST_NAME, MALE_LIST_NAME, true, this.femaleParent, this.maleParent, existingCrosses,
						germplasmWithPreferredName, parentsPedigreeString);

		try {

			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
				ArgumentMatchers.<Object[]>any(), ArgumentMatchers.eq(this.parents));
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
			.addItemToMakeCrossesTable(FEMALE_LIST_NAME, MALE_LIST_NAME, true, this.femaleParent, this.maleParent, existingCrosses,
					germplasmWithPreferredName, parentsPedigreeString);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(0)).addItem(
					ArgumentMatchers.<Object[]>any(), ArgumentMatchers.eq(this.parents));
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
		Mockito.when(this.messageSource.getMessage(Message.CLEAR_ALL)).thenReturn("CLEAR_ALL");
		Mockito.when(this.messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES)).thenReturn("REMOVE_SELECTED_ENTRIES");
		Mockito.when(this.messageSource.getMessage(Message.SELECT_ALL)).thenReturn("SELECT_ALL");
		Mockito.when(this.messageSource.getMessage(Message.SELECT_EVEN_ENTRIES)).thenReturn("SELECT_EVEN_ENTRIES");
		Mockito.when(this.messageSource.getMessage(Message.SELECT_ODD_ENTRIES)).thenReturn("SELECT_ODD_ENTRIES");
		this.makeCrossesTableComponent
			.addItemToMakeCrossesTable(FEMALE_LIST_NAME, MALE_LIST_NAME, false, this.femaleParent, this.maleParent, existingCrosses,
					germplasmWithPreferredName, parentsPedigreeString);

		try {
			Mockito.verify(this.tableCrossesMade, Mockito.times(1)).addItem(
					ArgumentMatchers.<Object[]>any(), ArgumentMatchers.eq(this.parents));
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
					ArgumentMatchers.<Object[]>any(), ArgumentMatchers.eq(this.parents));
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting table crosses will not have an added entry but didn't.");
		}
	}

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

		// Verify the visible column data
		Assert.assertEquals(false, ((CheckBox) newItemData[0]).booleanValue());
		Assert.assertEquals(1, ((Integer) newItemData[1]).intValue());
		Assert.assertEquals("Unknown", ((Button) newItemData[2]).getCaption());
		Assert.assertEquals("Unknown", ((Button) newItemData[3]).getCaption());
		Assert.assertEquals("pedigree 5", newItemData[4]);

		Assert.assertEquals("The female and male parent cross should be added to the existing crosses", Integer.valueOf(3), Integer.valueOf(existingCrosses.size()));

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

	private CrossParents createCrossParent(
		final Integer femaleListDataId, final Integer femaleGid, final Integer femaleEntryId, final Integer maleListDataId,
		final Integer maleGid, final Integer maleEntryId) {

		final GermplasmListEntry femaleParent = new GermplasmListEntry(femaleListDataId, femaleGid, femaleEntryId);
		final GermplasmListEntry maleParent = new GermplasmListEntry(maleListDataId, maleGid, maleEntryId);

		final CrossParents crossParents = new CrossParents(femaleParent, maleParent);
		return crossParents;

	}

	private TableWithSelectAllLayout initializeTable() {
		final TableWithSelectAllLayout tableWithSelectAllLayout = new TableWithSelectAllLayout(PARENTS_TABLE_ROW_COUNT, TAG_COLUMN_ID);
		tableWithSelectAllLayout.instantiateComponents();
		return tableWithSelectAllLayout;
	}
}
