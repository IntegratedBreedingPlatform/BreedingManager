
package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.customcomponent.GermplasmListTree;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.ThemeResource;

@Configurable
public abstract class ListTreeComponent extends ListSelectorComponent {

	private static final long serialVersionUID = -4025353842975688857L;

	private final ThemeResource folderResource = new ThemeResource("images/folder-icon.png");
	private final ThemeResource leafResource = new ThemeResource("images/document-icon.png");
	
	public ListTreeComponent(Integer selectListId) {
		this.listId = selectListId;
		this.selectListsFolderByDefault = false;
	}

	public ListTreeComponent(ListTreeActionsListener treeActionsListener) {
		this.treeActionsListener = treeActionsListener;
		this.selectListsFolderByDefault = false;
	}

	public ListTreeComponent(ListTreeActionsListener treeActionsListener, Integer selectedListId) {
		this.treeActionsListener = treeActionsListener;
		this.listId = selectedListId;
		this.selectListsFolderByDefault = true;
	}

	@Override
	public String getTreeStyleName() {
		return "listTree";
	}

	@Override
	public String getMainTreeStyleName() {
		return "listTree";
	}

	@Override
	public void instantiateGermplasmListSourceComponent() {
		this.setGermplasmListSource(new GermplasmListTree());
	}

	@Override
	public Object[] generateCellInfo(String name, String owner, String description, String listType, String numberOfEntries) {
		// just return an empty array, due to sonar inspection warning
		return new Object[1];
	}

	@Override
	public void setNodeItemIcon(Object itemId, boolean isFolder) {
		if (isFolder) {
			this.getGermplasmListSource().setItemIcon(itemId, this.folderResource);
		} else {
			this.getGermplasmListSource().setItemIcon(itemId, this.leafResource);
		}
	}
}
