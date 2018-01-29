
package org.generationcp.breeding.manager.customcomponent;

import java.util.Collection;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;

/**
 * Created by EfficioDaniel on 9/26/2014.
 */
public interface GermplasmListSource {

	boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed);

	boolean setParent(Object itemId, Object newParentId);

	boolean expandItem(Object itemId);

	void select(Object itemId);

	void setItemCaption(Object itemId, String caption);

	void setDropHandler(DropHandler dropHandler);

	void setValue(Object newValue);

	String getItemCaption(Object itemId);

	boolean removeAllItems();

	void setStyleName(String style);

	void addStyleName(String style);

	void setItemStyleGenerator(Tree.ItemStyleGenerator itemStyleGenerator);

	void setItemDescriptionGenerator(AbstractSelect.ItemDescriptionGenerator generator);

	void setDragMode(Tree.TreeDragMode treeDragMode, Table.TableDragMode treeTableDragMode);

	void setImmediate(boolean immediate);

	void requestRepaint();

	void addListener(ItemClickEvent.ItemClickListener listener);

	void addListener(Tree.CollapseListener listener);

	void addListener(Tree.ExpandListener listener);

	void setNullSelectionAllowed(boolean nullSelectionAllowed);

	Item addItem(Object itemId);

	Object addItem(Object[] cells, Object itemId);

	Item getItem(Object itemId);

	boolean removeItem(Object itemId);

	boolean isExpanded(Object itemId);

	boolean collapseItem(Object itemId);

	boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue);

	void setSizeFull();

	void setColumnExpandRatio(Object propertyId, float expandRatio);

	void setSelectable(boolean selectable);

	void setItemIcon(Object itemId, Resource icon);

	Object getValue();

	Component getUIComponent();

	void setColumnWidth(Object propertyId, int width);

	void clearSelection();

	Object getParent(Object itemId);
	
	/**
	 * Added so that we get the items in a tree 
	 * @return a list of current items in the tree
	 */
	Collection<?> getItemIds();
}
