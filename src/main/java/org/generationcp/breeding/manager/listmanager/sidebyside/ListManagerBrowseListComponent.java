package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.crossingmanager.SelectGermplasmListComponent;
import org.generationcp.breeding.manager.listmanager.DropHandlerComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeMenu;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.TabSheet.Tab;

@Configurable
public class ListManagerBrowseListComponent extends AbsoluteLayout implements
	InternationalizableComponent, InitializingBean {

	private static final long serialVersionUID = 1L;
	private ListManagerTreeComponent listManagerTreeComponent;
	private SelectGermplasmListComponent selectListComponent;
	
	private Label projectLists;
	private Panel browseListPanel;
	private TabSheet tabSheetList;
	private ListManagerTreeMenu list1;
	private ListManagerTreeMenu list2;
	private DropHandlerComponent dropHandler;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		
		listManagerTreeComponent = new ListManagerTreeComponent(selectListComponent);
		
		tabSheetList = new TabSheet();
		tabSheetList.setWidth("95%");
		tabSheetList.setHeight("500px");
		
		VerticalLayout layout = new VerticalLayout();
		list1 = new ListManagerTreeMenu(1426,"IIRON-1986",1,1,false,null);
		layout.addComponent(list1);
		Tab tab1 = tabSheetList.addTab(layout, "IIRON-1986");
		tab1.setClosable(true);
    	
		VerticalLayout layout2 = new VerticalLayout();
		list2 = new ListManagerTreeMenu(1427,"IIRON-1987",1,1,false,null);
		layout2.addComponent(list2);
		Tab tab2 = tabSheetList.addTab(layout2, "IIRON-1987");
		tab2.setClosable(true);
		
		dropHandler = new DropHandlerComponent(null, 220);
		dropHandler.enableDropHandler();
		
		addComponent(listManagerTreeComponent, "top:55px; left:20px");
		addComponent(tabSheetList,"top:15px; left:250px");
		addComponent(dropHandler, "top:340px; left:20px");
		
		
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
}
