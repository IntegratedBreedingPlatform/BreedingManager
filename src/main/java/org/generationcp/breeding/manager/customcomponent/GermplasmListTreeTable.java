package org.generationcp.breeding.manager.customcomponent;

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
	public boolean expandItem(final Object itemId) {
		super.setCollapsed(itemId, false);
		return true;
	}

	@Override
	public void setItemStyleGenerator(final Tree.ItemStyleGenerator itemStyleGenerator) {
		// not needed to implement any code, just added to override tree specific function
	}

	@Override
	public void setDragMode(final Tree.TreeDragMode treeDragMode, final Table.TableDragMode treeTableDragMode) {
		super.setDragMode(treeTableDragMode);
	}

	@Override
	public boolean isExpanded(final Object itemId) {
		return !super.isCollapsed(itemId);
	}

	@Override
	public boolean collapseItem(final Object itemId) {
		super.setCollapsed(itemId, true);
		return true;
	}

	@Override
	public Component getUIComponent() {
		return this;
	}

	@Override
	public String getItemCaption(final Object itemId) {
		if (this.getItem(itemId) != null && this.getItem(itemId).getItemProperty(GermplasmListTreeTable.NAME_COL) != null
				&& this.getItem(itemId).getItemProperty(GermplasmListTreeTable.NAME_COL).getValue() != null) {
			return (String) this.getItem(itemId).getItemProperty(GermplasmListTreeTable.NAME_COL).getValue();
		}
		return "";
	}

	@Override
	public void setItemCaption(final Object itemId, final String caption) {
		this.getItem(itemId).getItemProperty(GermplasmListTreeTable.NAME_COL).setValue(" " + caption);
	}

	@Override
	public void clearSelection() {
		super.select(null);
	}

}
