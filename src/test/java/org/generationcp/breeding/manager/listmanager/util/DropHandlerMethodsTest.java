
package org.generationcp.breeding.manager.listmanager.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.domain.gms.GermplasmListNewColumnsInfo;
import org.generationcp.middleware.domain.gms.ListDataColumnValues;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

public class DropHandlerMethodsTest {

	private static final int NO_OF_ENTRIES = 5;

	private static final int GERMPLASM_LIST_ID = 1;

	private static final int GROUP_ID = 1;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private Table targetTable;

	@Mock
	private ListManagerMain listManagerMain;

	private final GermplasmListNewColumnsInfo germplasmListNewColumnsInfo = new GermplasmListNewColumnsInfo(1);
	
	private GermplasmListTestDataInitializer germplasmListTestDataInitializer;
	
	@InjectMocks
	private DropHandlerMethods dropHandlerMethods;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();
	}

	@Test
	public void testAddGermplasmFromList() {

		final Map<String, List<ListDataColumnValues>> map = new HashMap<>();
		final ListDataColumnValues ldcv = new ListDataColumnValues("GID", 1, "1");
		map.put("GID", Lists.newArrayList(ldcv));
		this.germplasmListNewColumnsInfo.setColumnValuesMap(map);
		this.dropHandlerMethods.setCurrentColumnsInfo(this.germplasmListNewColumnsInfo);

		final Container mockContainer = Mockito.mock(Container.class);
		Mockito.when(this.targetTable.getContainerDataSource()).thenReturn(mockContainer);

		final Item mockTableItem = Mockito.mock(Item.class);
		Mockito.when(mockContainer.addItem(Matchers.any())).thenReturn(mockTableItem);
		final Property mockProperty = Mockito.mock(Property.class);
		Mockito.when(mockTableItem.getItemProperty(Matchers.anyString())).thenReturn(mockProperty);

		final GermplasmList testList =
				this.germplasmListTestDataInitializer.createGermplasmListWithListDataAndInventoryInfo(GERMPLASM_LIST_ID, NO_OF_ENTRIES);

		// retrieve the first list entry from list data with inventory information
		final GermplasmListData listData = testList.getListData().get(0);
		// MGID or group ID of Germplasm List Data has default value to 0, so this field will never be null
		listData.setMgid(GROUP_ID);

		this.dropHandlerMethods.addGermplasmFromList(GERMPLASM_LIST_ID, listData.getId(), testList, false);

		// verify if the list data fields are properly retrieved
		Mockito.verify(mockProperty).setValue(listData.getEntryCode());
		Mockito.verify(mockProperty).setValue(listData.getSeedSource());
		Mockito.verify(mockProperty).setValue(listData.getGroupName());
		// Others (e.g. gid and designation) are added as buttons so hard to verify from outside the class in this test harness.
	}

}
