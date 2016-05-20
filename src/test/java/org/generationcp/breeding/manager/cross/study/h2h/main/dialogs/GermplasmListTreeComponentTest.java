
package org.generationcp.breeding.manager.cross.study.h2h.main.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

public class GermplasmListTreeComponentTest {

	private static final Integer NO_OF_ITEMS = 4;
	private static final Integer LIST_ID = 2;

	private static final String DUMMY_STRING = "DUMMY_STRING";

	private GermplasmListTreeComponent germplasmListTreeComponent;

	@Mock
	private SelectGermplasmListComponent selectListComponent;
	@Mock
	private GermplasmListManager germplasmListManager;
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	@Mock
	private ContextUtil contextUtil;

	private List<GermplasmList> germplasmLists;
	private GermplasmList parentList;
	private VerticalLayout treeContainerLayout;
	private Tree germplasmListTree;

	private static final int BATCH_SIZE = 50;
	private static final String PROGRAM_UUID = "1234567";

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);

		this.initParentList();
		this.initGermplasmLists();

		this.germplasmListTree = new Tree();
		this.treeContainerLayout = new VerticalLayout();

		Mockito.doReturn(GermplasmListTreeComponentTest.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		Mockito.when(this.germplasmListManager.getAllTopLevelListsBatched(GermplasmListTreeComponentTest.PROGRAM_UUID,
				GermplasmListTreeComponentTest.BATCH_SIZE)).thenReturn(this.germplasmLists);
		Mockito.when(this.germplasmListManager.getGermplasmListById(GermplasmListTreeComponentTest.LIST_ID))
				.thenReturn(this.getGermplasmListById(GermplasmListTreeComponentTest.LIST_ID));
		Mockito.when(this.messageSource.getMessage(Message.REFRESH_LABEL)).thenReturn(GermplasmListTreeComponentTest.DUMMY_STRING);

		this.germplasmListTreeComponent = Mockito.spy(new GermplasmListTreeComponent(this.selectListComponent));
		this.germplasmListTreeComponent.setGermplasmListManager(this.germplasmListManager);
		this.germplasmListTreeComponent.setTreeContainerLayout(this.treeContainerLayout);
		this.germplasmListTreeComponent.setGermplasmListTree(this.germplasmListTree);
		this.germplasmListTreeComponent.setMessageSource(this.messageSource);
		this.germplasmListTreeComponent.setListId(GermplasmListTreeComponentTest.LIST_ID);
		this.germplasmListTreeComponent.setContextUtil(this.contextUtil);
	}

	private void initParentList() {
		this.parentList = new GermplasmList();
		this.parentList.setId(1);
		this.parentList.setName("Parent Lists");
	}

	private GermplasmList getGermplasmListById(Integer id) {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(id);
		germplasmList.setName("Sample List " + id);
		germplasmList.setDescription("This is a description....");
		germplasmList.setParent(this.parentList);

		return germplasmList;
	}

	private void initGermplasmLists() {
		this.germplasmLists = new ArrayList<GermplasmList>();
		for (Integer i = 1; i < GermplasmListTreeComponentTest.NO_OF_ITEMS; i++) {
			this.germplasmLists.add(this.getGermplasmListById(i + 1));
		}
	}

	@Test
	public void testCreateTree() throws MiddlewareQueryException {
		this.germplasmListTreeComponent.createTree();

		Integer actualNoOfItems = this.treeContainerLayout.getComponentCount();
		Integer expectedNoOfItems = 1;
		Assert.assertEquals(
				"Expecting the no of component inside treeContainerLayout is " + expectedNoOfItems + " but returned " + actualNoOfItems,
				expectedNoOfItems, actualNoOfItems);
	}

	@Test
	public void testCreateGermplasmListTreeReturnsATreeWithAllGermplasmListItems() throws MiddlewareQueryException {

		this.germplasmListTree = this.germplasmListTreeComponent.createGermplasmListTree();

		Integer actualNoOfItems = this.germplasmListTree.getItemIds().size();
		Assert.assertEquals("Expecting the no of items of germplasm list tree is " + GermplasmListTreeComponentTest.NO_OF_ITEMS
				+ " but returned " + actualNoOfItems, GermplasmListTreeComponentTest.NO_OF_ITEMS, actualNoOfItems);

	}

	@Test
	public void testCreateGermplasmListTreeReturnsATreeWithOnlyTheRootFolderAsAnItem() throws MiddlewareQueryException {

		Mockito.when(this.germplasmListManager.getAllTopLevelListsBatched(GermplasmListTreeComponentTest.PROGRAM_UUID,
				GermplasmListTreeComponentTest.BATCH_SIZE)).thenThrow(new MiddlewareQueryException("This is an error"));

		this.germplasmListTree = this.germplasmListTreeComponent.createGermplasmListTree();

		Integer actualNoOfItems = this.germplasmListTree.getItemIds().size();
		Integer expectedNoOfItems = 1;
		Assert.assertEquals("Expecting the no of items of germplasm list tree is " + expectedNoOfItems + " but returned " + actualNoOfItems,
				expectedNoOfItems, actualNoOfItems);

	}
}
