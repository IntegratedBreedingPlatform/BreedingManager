package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.customfields.ListTreeComponent;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class CrossingManagerListTreeComponent extends ListTreeComponent {

	private static final long serialVersionUID = 8112173851252075693L;
	
	public CrossingManagerListTreeComponent(
			ListTreeActionsListener treeActionsListener) {
		super(treeActionsListener);
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
	protected String getTreeStyleName() {
		return "crossingManagerTree";
	}
	
	@Override
	public void refreshRemoteTree(){

	}

}
