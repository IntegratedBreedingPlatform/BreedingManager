
package org.generationcp.breeding.manager.customcomponent;

import java.util.Collection;

import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.TreeTable;

/**
 * Created by EfficioDaniel on 9/26/2014.
 */
public class GermplasmListTreeTable extends TreeTable implements GermplasmListSource {

	private static final long serialVersionUID = 3171881413482637854L;

	public static final String NAME_COL = "Name";
	public static final String OWNER_COL = "Owner";
	public static final String DESCRIPTION_COL = "Description";
	public static final String LIST_TYPE_COL = "Type";
	public static final String NUMBER_OF_ENTRIES_COL = "# of entries";

	public GermplasmListTreeTable() {
		super();
	}

	@Override
	public boolean expandItem(Object itemId) {
		super.setCollapsed(itemId, false);
		return true;
	}

	@Override
	public void setItemStyleGenerator(Tree.ItemStyleGenerator itemStyleGenerator) {
		// not needed to implement any code, just added to override tree specific function
	}

	@Override
	public void setDragMode(Tree.TreeDragMode treeDragMode, Table.TableDragMode treeTableDragMode) {
		super.setDragMode(treeTableDragMode);
	}

	@Override
	public boolean isExpanded(Object itemId) {
		return !super.isCollapsed(itemId);
	}

	@Override
	public boolean collapseItem(Object itemId) {
		super.setCollapsed(itemId, true);
		return true;
	}

	@Override
	public Component getUIComponent() {
		return this;
	}

	@Override
	public String getItemCaption(Object itemId) {
		if (this.getItem(itemId) != null && this.getItem(itemId).getItemProperty(GermplasmListTreeTable.NAME_COL) != null
				&& this.getItem(itemId).getItemProperty(GermplasmListTreeTable.NAME_COL).getValue() != null) {
			return (String) this.getItem(itemId).getItemProperty(GermplasmListTreeTable.NAME_COL).getValue();
		}
		return "";
	}

	@Override
	public void setItemCaption(Object itemId, String caption) {
		this.getItem(itemId).getItemProperty(GermplasmListTreeTable.NAME_COL).setValue(" " + caption);
	}

	@Override
	public void clearSelection() {
		super.select(null);
	}
	
	/* (non-Javadoc)
	 * @see com.vaadin.ui.TreeTable#getItemIds()
	 */
	@Override
	public Collection<?> getItemIds() {
		return super.getItemIds();
	}

	@Override
	public Object getParent(Object itemId) {
		return super.getParent(itemId);
	}
}
