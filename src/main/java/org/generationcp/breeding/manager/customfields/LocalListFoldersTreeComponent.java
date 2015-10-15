
package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.SelectTreeItemOnSaveListener;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class LocalListFoldersTreeComponent extends ListTreeComponent {

	private static final long serialVersionUID = -3038021741795321888L;
	private final Boolean showFoldersOnly;
	private final Boolean refreshTreeOnAddEditOrDeleteAction;

	@Autowired
	protected SimpleResourceBundleMessageSource messageSource;

	public LocalListFoldersTreeComponent(final Integer folderId) {
		this(null, folderId, true, false);
	}

	public LocalListFoldersTreeComponent(final SelectTreeItemOnSaveListener selectTreeItemOnSaveListener, final Integer folderId,
			final Boolean showFoldersOnly, final Boolean refreshTreeOnAddEditOrDeleteAction) {
		super(selectTreeItemOnSaveListener, folderId);
		this.showFoldersOnly = showFoldersOnly;
		this.refreshTreeOnAddEditOrDeleteAction = refreshTreeOnAddEditOrDeleteAction;
	}

	@Override
	public void instantiateComponents() {
		super.instantiateComponents();

		if (this.folderTextField != null) {
			this.folderTextField.setWidth("140px");
		}
	}

	@Override
	protected boolean doIncludeActionsButtons() {
		return true;
	}

	@Override
	protected String getTreeHeading() {
		return this.messageSource.getMessage(Message.LIST_LOCATION);
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
	protected boolean doShowFoldersOnly() {
		return this.showFoldersOnly;
	}

	@Override
	public boolean usedInSubWindow() {
		return true;
	}

	@Override
	public boolean doIncludeTreeHeadingIcon() {
		return false;
	}

	@Override
	public String getTreeStyleName() {
		return "saveListTree";
	}

	@Override
	public void refreshRemoteTree() {

		if (!this.refreshTreeOnAddEditOrDeleteAction) {
			return;
		}

		final BreedingManagerApplication breedingManagerApplication = (BreedingManagerApplication) this.getApplication();
		if (breedingManagerApplication != null) {
			breedingManagerApplication.refreshListManagerTree();
			breedingManagerApplication.refreshCrossingManagerTree();
		}
	}

}
