package org.generationcp.browser.util;


import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;

public class SelectedTabCloseHandler implements TabSheet.CloseHandler{

	private static final long serialVersionUID = 1L;

	
	@Override
	public void onTabClose(TabSheet tabsheet, Component tabContent) {
		if(tabsheet.getComponentCount() > 1){
			String tabCaption=tabsheet.getTab(tabContent).getCaption();
			Tab tab = Util.getTabBefore(tabsheet, tabCaption);
			tabsheet.removeTab(tabsheet.getTab(tabContent));
			tabsheet.setSelectedTab(tab.getComponent());
		}else{
			tabsheet.removeTab(tabsheet.getTab(tabContent));
		}
		tabsheet.requestRepaintAll();
		
	}
}
