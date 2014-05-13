package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.ListTreeComponent;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class ListManagerTreeComponent extends ListTreeComponent implements InitializingBean{

	private static final long serialVersionUID = -1013380483927558222L;
	
	public ListManagerTreeComponent(final ListTreeActionsListener treeActionsListener) {
		super(treeActionsListener);
	}
	
	public ListManagerTreeComponent(final ListTreeActionsListener treeActionListener, Integer listId){
		super(treeActionListener, listId);
	}
	
	@Override
	protected boolean doIncludeActionsButtons() {
		return true;
	}

	@Override
	protected boolean doIncludeRefreshButton() {
		return true;
	}

	@Override
	protected boolean isTreeItemsDraggable() {
		return true;
	}

	@Override
	protected boolean doIncludeCentralLists() {
		return true;
	}

	@Override
	protected boolean doShowFoldersOnly() {
		return false;
	}
	
	@Override
	public boolean usedInSubWindow() {
		return true;
	}
	
	@Override
	protected String getTreeHeading() {
		return messageSource.getMessage(Message.ALL_LISTS);
	}
}
