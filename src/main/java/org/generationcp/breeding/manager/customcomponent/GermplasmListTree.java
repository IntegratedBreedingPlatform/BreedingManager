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
	public void setDragMode(final TreeDragMode treeDragMode, final Table.TableDragMode treeTableDragMode) {
		super.setDragMode(treeDragMode);
	}

	@Override
	public Object addItem(final Object[] cells, final Object itemId) {
		final Monitor monitor =
				MonitorFactory.start("org.generationcp.breeding.manager." + "customcomponent.GermplasmListTree.addItem(Object[], Object)");
		try {
			return super.addItem(itemId);
		} finally {
			monitor.stop();
		}
	}

	@Override
	public void setColumnExpandRatio(final Object propertyId, final float expandRatio) {
		// not needed to implement any code, just added the function so we would have override for the treetable specific function
	}

	@Override
	public Component getUIComponent() {
		return this;
	}

	@Override
	public void setColumnWidth(final Object propertyId, final int width) {
		// not needed to implement any code, just added the function so we would have override for the treetable specific function
	}

	@Override
	public void clearSelection() {
		super.select(null);
	}

}
