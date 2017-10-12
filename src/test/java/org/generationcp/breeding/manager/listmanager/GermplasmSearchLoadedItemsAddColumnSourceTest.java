
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vaadin.ui.Table;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Window;

import junit.framework.Assert;

public class GermplasmSearchLoadedItemsAddColumnSourceTest {

	private static final List<Integer> CURRENT_ITEMS_LIST = Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
	
	private static final List<Integer> CURRENT_GID_LIST = Arrays.asList(101, 102, 103, 104, 105, 106, 107, 108, 109, 110);
	
	private static final int CURRENT_PAGE = 2;

	private static final String LISTDATA_PROPERTY_ID = "LISTDATA_ID";

	private static final String GID_PROPERTY_ID = "REAL_GID";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private PagedBreedingManagerTable targetTable;

	@Mock
	private LazyQueryDefinition definition;
	
	@Mock
	private GermplasmSearchResultsComponent searchResultsComponent;

	@Mock
	private IndexedContainer container;

	@Mock
	private Window window;

	private GermplasmSearchLoadedItemsAddColumnSource addColumnSource;
	
	private List<Integer> allGids;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(this.definition).when(this.searchResultsComponent).getDefinition();
		
		this.addColumnSource = new GermplasmSearchLoadedItemsAddColumnSource(this.targetTable, this.searchResultsComponent,
				GermplasmSearchLoadedItemsAddColumnSourceTest.GID_PROPERTY_ID);
		this.addColumnSource.setOntologyDataManager(this.ontologyDataManager);
		
		Mockito.doReturn(GermplasmSearchLoadedItemsAddColumnSourceTest.CURRENT_PAGE).when(this.targetTable).getCurrentPage();
		Mockito.doReturn(GermplasmSearchLoadedItemsAddColumnSourceTest.CURRENT_ITEMS_LIST).when(this.targetTable)
				.getAllEntriesForPage(GermplasmSearchLoadedItemsAddColumnSourceTest.CURRENT_PAGE);
		Mockito.doReturn(this.container).when(this.targetTable).getContainerDataSource();
		Mockito.doReturn(Arrays.asList(GermplasmSearchLoadedItemsAddColumnSourceTest.LISTDATA_PROPERTY_ID,
				GermplasmSearchLoadedItemsAddColumnSourceTest.GID_PROPERTY_ID)).when(this.targetTable).getContainerPropertyIds();
		Mockito.doReturn(this.window).when(this.targetTable).getWindow();
		
		this.allGids = new ArrayList<>(Arrays.asList(91, 92, 93, 94, 95, 96, 97, 98, 99, 100));
		this.allGids.addAll(CURRENT_GID_LIST);
		this.allGids.addAll(Arrays.asList(111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123));
		Mockito.doReturn(this.allGids).when(this.searchResultsComponent).getAllGids();
		// Create items for current page
		for (int i = 0; i < CURRENT_ITEMS_LIST.size(); i++) {
			final Integer itemId = CURRENT_ITEMS_LIST.get(i);
			final Item item = new PropertysetItem();
			item.addItemProperty(GermplasmSearchLoadedItemsAddColumnSourceTest.LISTDATA_PROPERTY_ID, new ObjectProperty<Integer>(itemId));
			item.addItemProperty(GermplasmSearchLoadedItemsAddColumnSourceTest.GID_PROPERTY_ID,
					new ObjectProperty<String>(CURRENT_GID_LIST.get(i).toString()));
			Mockito.doReturn(item).when(this.targetTable).getItem(itemId);
			Mockito.doReturn(item).when(this.container).getItem(itemId);
		}
	}

	@Test
	public void testGetItemIdsToProcess() {
		Assert.assertEquals(GermplasmSearchLoadedItemsAddColumnSourceTest.CURRENT_ITEMS_LIST, this.addColumnSource.getItemIdsToProcess());
	}

	@Test
	public void testGetGidForItemId() {
		for (int i = 0; i < GermplasmSearchLoadedItemsAddColumnSourceTest.CURRENT_ITEMS_LIST.size(); i++) {
			final Integer itemId = GermplasmSearchLoadedItemsAddColumnSourceTest.CURRENT_ITEMS_LIST.get(i);
			Assert.assertEquals(GermplasmSearchLoadedItemsAddColumnSourceTest.CURRENT_GID_LIST.get(i),
					this.addColumnSource.getGidForItemId(itemId));
		}
	}

	@Test
	public void testGetGidsToProcess() {
		Assert.assertEquals(GermplasmSearchLoadedItemsAddColumnSourceTest.CURRENT_GID_LIST, this.addColumnSource.getGidsToProcess());
	}
	
	@Test
	public void testGetAllGids() {
		final List<Integer> gids = this.addColumnSource.getAllGids();
		Mockito.verify(this.searchResultsComponent).getAllGids();
		Assert.assertEquals(this.allGids, gids);
	}

	@Test
	public void testSetColumnValueForItem() {
		final Integer itemId = GermplasmSearchLoadedItemsAddColumnSourceTest.CURRENT_ITEMS_LIST.get(1);
		final String dateValue = "20170717";
		final String newGid = "1234";
		// GERMPLASM_DATE column doesn't exist yet
		this.addColumnSource.setColumnValueForItem(itemId, ColumnLabels.GERMPLASM_DATE.getName(), dateValue);
		this.addColumnSource.setColumnValueForItem(itemId, GermplasmSearchLoadedItemsAddColumnSourceTest.GID_PROPERTY_ID, newGid);

		final Item item = this.targetTable.getItem(itemId);
		Assert.assertEquals(dateValue, item.getItemProperty(ColumnLabels.GERMPLASM_DATE.getName()).getValue());
		Assert.assertEquals(newGid, item.getItemProperty(GermplasmSearchLoadedItemsAddColumnSourceTest.GID_PROPERTY_ID).getValue());
	}

	@Test
	public void testColumnExists() {
		Assert.assertTrue(this.addColumnSource.columnExists(GermplasmSearchLoadedItemsAddColumnSourceTest.GID_PROPERTY_ID));
		Assert.assertFalse(this.addColumnSource.columnExists(ColumnLabels.PREFERRED_NAME.getName()));
	}

	@Test
	public void testPropagateUIChanges() {
		this.addColumnSource.propagateUIChanges();
		Mockito.verify(this.targetTable).refreshRowCache();
	}

	@Test
	public void testGetWindow() {
		Assert.assertEquals(this.window, this.addColumnSource.getWindow());
	}

	@Test
	public void testGetColumnsToExclude() {
		Assert.assertEquals(Arrays.asList(FillWithOption.FILL_WITH_LOCATION, FillWithOption.FILL_WITH_BREEDING_METHOD_NAME),
				this.addColumnSource.getColumnsToExclude());
	}

}
