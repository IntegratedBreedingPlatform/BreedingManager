package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.customfields.ListTreeComponent;
import org.generationcp.breeding.manager.customfields.LocalListFoldersTreeComponent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.vaadin.ui.AbstractSelect;

public class GermplasmListTreeTest {
	
	private GermplasmListTree listTree;
	private ListTreeComponent listTreeComponent;
	
	@Before
	public void setUp(){
		listTree = Mockito.mock(GermplasmListTree.class);
		
		listTreeComponent = new LocalListFoldersTreeComponent(1);
		listTreeComponent.setGermplasmListSource(listTree);
	}
	
	@Test
	public void testLocalTreeItemDescription(){
		listTreeComponent.addListTreeItemDescription();
		
		Mockito.verify(listTree).setItemDescriptionGenerator(Matchers.any(AbstractSelect.ItemDescriptionGenerator.class));
	}
	
	
	

}
