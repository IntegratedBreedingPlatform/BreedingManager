
package org.generationcp.breeding.manager.customcomponent;


import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;

public class GermplasmListTree extends Tree implements GermplasmListSource {

	private static final long serialVersionUID = 366024615188199034L;

	public GermplasmListTree() {
		super();
	}

	@Override
	public void setDragMode(TreeDragMode treeDragMode, Table.TableDragMode treeTableDragMode) {
		super.setDragMode(treeDragMode);
	}

	@Override
	public Object addItem(Object[] cells, Object itemId) {
		final Monitor monitor = MonitorFactory.start("org.generationcp.breeding.manager."
				+ "customcomponent.GermplasmListTree.addItem(Object[], Object)");
		try {
			return super.addItem(itemId);
		} finally {
			monitor.stop();
		}
	}

	@Override
	public void setColumnExpandRatio(Object propertyId, float expandRatio) {
		// not needed to implement any code, just added the function so we would have override for the treetable specific function
	}

	@Override
	public Component getUIComponent() {
		return this;
	}

	@Override
	public void setColumnWidth(Object propertyId, int width) {
		// not needed to implement any code, just added the function so we would have override for the treetable specific function
	}

	@Override
	public void clearSelection() {
		super.select(null);
	}


}
