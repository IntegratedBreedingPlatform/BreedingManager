
package org.generationcp.breeding.manager.crossingmanager.listeners;

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

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;

@Configurable
public class SelectTreeItemOnSaveListener extends AbsoluteLayout implements InitializingBean, InternationalizableComponent,
		ListTreeActionsListener {

	private static final long serialVersionUID = 1L;
	private final SaveListAsDialog saveListAsDialog;
	private final Component parentComponent;

	public SelectTreeItemOnSaveListener(SaveListAsDialog saveListAsDialog, Component parentComponent) {
		this.saveListAsDialog = saveListAsDialog;
		this.parentComponent = parentComponent;
	}

	@Override
	public void updateUIForRenamedList(GermplasmList list, String newName) {
		if (this.parentComponent instanceof ListManagerMain) {
			ListManagerMain listManagerMain = (ListManagerMain) this.parentComponent;
			listManagerMain.getListSelectionComponent().updateUIForRenamedList(list, newName);
		}

		if (this.parentComponent instanceof ManageCrossingSettingsMain) {
			ManageCrossingSettingsMain manageCrossingSettingsMain = (ManageCrossingSettingsMain) this.parentComponent;
			manageCrossingSettingsMain.getMakeCrossesComponent().getSelectParentsComponent().updateUIForRenamedList(list, newName);
		}

		ListNameField listNameField = this.saveListAsDialog.getListDetailsComponent().getListNameField();
		listNameField.getListNameValidator().setCurrentListName(newName);
		listNameField.setValue(newName);
		listNameField.setListNameValidator(listNameField.getListNameValidator());

	}

	@Override
	public void studyClicked(GermplasmList list) {
		if (this.saveListAsDialog != null && !list.getType().equals("FOLDER")) {
			this.saveListAsDialog.getDetailsComponent().setGermplasmListDetails(list);

			if (this.saveListAsDialog.getSource() instanceof ListBuilderComponent) {
				ListBuilderComponent LBC = (ListBuilderComponent) this.saveListAsDialog.getSource();
				LBC.getSaveListButtonListener().setForceHasChanges(true);
			}

		}
	}

	@Override
	public void folderClicked(GermplasmList list) {
		if (this.saveListAsDialog != null) {
			// Check also if folder is clicked (or list is null == central/local folders)
			if (list != null && list.getType().equals("FOLDER") || list == null) {
				// Check if list old (with ID), if so, remove list details
				if (this.saveListAsDialog.getDetailsComponent().getCurrentGermplasmList() != null
						&& this.saveListAsDialog.getDetailsComponent().getCurrentGermplasmList().getId() != null) {
					this.saveListAsDialog.getDetailsComponent().setGermplasmListDetails(null);
					this.saveListAsDialog.setGermplasmList(null);// reset also the current list to save
				}
			} else {
				this.saveListAsDialog.getDetailsComponent().setGermplasmListDetails(list);
			}
		}
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub

	}

}
