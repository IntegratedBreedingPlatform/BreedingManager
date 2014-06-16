package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerListTreeComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMakeCrossesComponent;
import org.generationcp.breeding.manager.crossingmanager.SelectParentsComponent;
import org.generationcp.breeding.manager.crossingmanager.listeners.SelectTreeItemOnSaveListener;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class LocalListFoldersTreeComponent extends ListTreeComponent {

	private static final long serialVersionUID = -3038021741795321888L;
	private Boolean showFoldersOnly;
	private Boolean refreshTreeOnAddEditOrDeleteAction;
	
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
		refreshTreeOnAddEditOrDeleteAction = false;
	}

	public LocalListFoldersTreeComponent(SelectTreeItemOnSaveListener selectTreeItemOnSaveListener, Integer folderId, Boolean showFoldersOnly, Boolean refreshTreeOnAddEditOrDeleteAction) {
		super(selectTreeItemOnSaveListener, folderId);
		this.showFoldersOnly = showFoldersOnly;
		this.refreshTreeOnAddEditOrDeleteAction = refreshTreeOnAddEditOrDeleteAction;
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
	
	@Override
	public void refreshRemoteTree(){

		if(!refreshTreeOnAddEditOrDeleteAction)
			return;
		
		BreedingManagerApplication breedingManagerApplication = (BreedingManagerApplication) getApplication();
		if(breedingManagerApplication==null){
			System.out.println("BM application is null");
			return;
		}

		ListManagerMain listManagerMain = breedingManagerApplication.getListManagerMain();

		if(listManagerMain!=null){
			listManagerMain.getListSelectionComponent().getListTreeComponent().refreshTree();
		}
		
		ManageCrossingSettingsMain manageCrossSettingsMain = breedingManagerApplication.getManageCrossingSettingsMain();
		
		if(manageCrossSettingsMain!=null){
			manageCrossSettingsMain.getMakeCrossesComponent().getSelectParentsComponent().getListTreeComponent().refreshTree();
		}
		
	}

}
