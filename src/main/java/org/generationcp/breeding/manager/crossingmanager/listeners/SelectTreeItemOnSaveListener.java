package org.generationcp.breeding.manager.crossingmanager.listeners;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customfields.ListNameField;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class SelectTreeItemOnSaveListener extends AbsoluteLayout
		implements InitializingBean, InternationalizableComponent, ListTreeActionsListener {

	private static final String FOLDER = "FOLDER";
	private static final long serialVersionUID = 1L;
	private final SaveListAsDialog saveListAsDialog;
	private final Component parentComponent;

	public SelectTreeItemOnSaveListener(final SaveListAsDialog saveListAsDialog, final Component parentComponent) {
		this.saveListAsDialog = saveListAsDialog;
		this.parentComponent = parentComponent;
	}

	@Override
	public void updateUIForRenamedList(final GermplasmList list, final String newName) {
		if (this.parentComponent instanceof ListManagerMain) {
			final ListManagerMain listManagerMain = (ListManagerMain) this.parentComponent;
			listManagerMain.getListSelectionComponent().updateUIForRenamedList(list, newName);
		}

		if (this.parentComponent instanceof ManageCrossingSettingsMain) {
			final ManageCrossingSettingsMain manageCrossingSettingsMain = (ManageCrossingSettingsMain) this.parentComponent;
			manageCrossingSettingsMain.getMakeCrossesComponent().getSelectParentsComponent().updateUIForRenamedList(list, newName);
		}

		final ListNameField listNameField = this.saveListAsDialog.getListDetailsComponent().getListNameField();
		listNameField.getListNameValidator().setCurrentListName(newName);
		listNameField.setValue(newName);
		listNameField.setListNameValidator(listNameField.getListNameValidator());

	}

	@Override
	public void studyClicked(final GermplasmList list) {
		if (this.saveListAsDialog != null && !AppConstants.DB.FOLDER.equals(list.getType())) {
			this.saveListAsDialog.getDetailsComponent().populateGermplasmListDetails(list);

			if (this.saveListAsDialog.getSource() instanceof ListBuilderComponent) {
				final ListBuilderComponent listBuilderComponent = (ListBuilderComponent) this.saveListAsDialog.getSource();
				listBuilderComponent.getSaveListButtonListener().setForceHasChanges(true);
			}

		}
	}

	@Override
	public void folderClicked(final GermplasmList list) {
		if (this.saveListAsDialog != null) {
			// Check also if folder is clicked (or list is null == central/local folders)
			if (list != null && AppConstants.DB.FOLDER.equals(list.getType()) || list == null) {
				if (!(this.saveListAsDialog.getDetailsComponent().getCurrentGermplasmList() != null
						&& this.saveListAsDialog.getDetailsComponent().getCurrentGermplasmList().getId() != null)) {
					this.saveListAsDialog.getDetailsComponent().populateGermplasmListDetails(list);
				}
			}
		}
	}

	@Override
	public void updateLabels() {
		// Do nothing

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Do nothing
	}

}
