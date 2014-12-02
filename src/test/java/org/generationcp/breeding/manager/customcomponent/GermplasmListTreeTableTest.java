package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.crossingmanager.CrossingManagerListTreeComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMakeCrossesComponent;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerTreeActionsListener;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.vaadin.ui.AbstractSelect;

public class GermplasmListTreeTableTest {
	
	private GermplasmListTreeTable listTreeTable;
	private ListManagerTreeComponent listManagerTreeComponent;
	private CrossingManagerListTreeComponent crossManagerTreeComponent;
	
	@Before
	public void setUp(){
		listTreeTable = Mockito.mock(GermplasmListTreeTable.class);
	}
	
	@Test
	public void testListManagerItemDescription(){
		listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setGermplasmListSource(listTreeTable);
		
		listManagerTreeComponent.addListTreeItemDescription();
		
		Mockito.verify(listTreeTable, Mockito.never()).setItemDescriptionGenerator(Matchers.any(AbstractSelect.ItemDescriptionGenerator.class));
	}
	
	@Test
	public void testCrossingManagerItemDescription(){
		CrossingManagerTreeActionsListener listener = Mockito.mock(CrossingManagerTreeActionsListener.class);
		CrossingManagerMakeCrossesComponent crossesComponent = Mockito.mock(CrossingManagerMakeCrossesComponent.class);
		
		crossManagerTreeComponent = new CrossingManagerListTreeComponent(listener, crossesComponent);
		crossManagerTreeComponent.setGermplasmListSource(listTreeTable);
		
		crossManagerTreeComponent.addListTreeItemDescription();
		
		Mockito.verify(listTreeTable, Mockito.never()).setItemDescriptionGenerator(Matchers.any(AbstractSelect.ItemDescriptionGenerator.class));
	}

}
