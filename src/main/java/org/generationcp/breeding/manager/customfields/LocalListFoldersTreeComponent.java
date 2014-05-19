package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.SelectTreeItemOnSaveListener;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class LocalListFoldersTreeComponent extends ListTreeComponent {

	private static final long serialVersionUID = -3038021741795321888L;
	private Boolean showFoldersOnly;
	
    @Autowired
    protected SimpleResourceBundleMessageSource messageSource;

	
	public LocalListFoldersTreeComponent(Integer folderId) {
		super(folderId);
		this.showFoldersOnly = true;
	}

	public LocalListFoldersTreeComponent(Integer folderId, Boolean showFoldersOnly) {
		super(folderId);
		this.showFoldersOnly = showFoldersOnly;
	}

	public LocalListFoldersTreeComponent(SelectTreeItemOnSaveListener selectTreeItemOnSaveListener, Integer folderId, Boolean showFoldersOnly) {
		super(selectTreeItemOnSaveListener, folderId);
		this.showFoldersOnly = showFoldersOnly;
	}
	
	
	@Override
	protected boolean doIncludeActionsButtons() {
		return true;
	}

	@Override
	protected String getTreeHeading() {
		return messageSource.getMessage(Message.LIST_LOCATION);
	}
	
	@Override
	protected String getTreeHeadingStyleName() {
		return Bootstrap.Typography.H4.styleName();
	}

	@Override
	protected boolean doIncludeRefreshButton() {
		return false;
	}

	@Override
	protected boolean isTreeItemsDraggable() {
		return true;
	}

	@Override
	protected boolean doIncludeCentralLists() {
		return false;
	}

	@Override
	protected boolean doShowFoldersOnly() {
		return showFoldersOnly;
	}
	
	@Override
	public boolean usedInSubWindow() {
		return true;
	}
	
	@Override
	protected boolean doIncludeTreeHeadingIcon() {
		return false;
	}
	
	
	@Override
	protected String getTreeStyleName() {
		return "saveListTree";
	}

}
