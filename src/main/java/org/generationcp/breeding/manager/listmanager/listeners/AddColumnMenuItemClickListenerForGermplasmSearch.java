package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.FillWithAttributeWindow;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class AddColumnMenuItemClickListenerForGermplasmSearch extends AddColumnMenuItemClickListener {

	private static final long serialVersionUID = 1L;

	public AddColumnMenuItemClickListenerForGermplasmSearch(final AddColumnSource addColumnSource) {
		super(addColumnSource);
	}

	@Override
	void addImmediateSourcePreferredNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME);

		}
	}

	@Override
	void addImmediateSourceGidColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.IMMEDIATE_SOURCE_GID.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.IMMEDIATE_SOURCE_GID);

		}
	}

	@Override
	void addGroupPreferredNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME);

		}
	}

	@Override
	void addGroupSourceGidColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GROUP_SOURCE_GID.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.GROUP_SOURCE_GID);

		}
	}

	@Override
	void addPreferredIdColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.PREFERRED_ID.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.PREFERRED_ID);

		}
	}

	@Override
	void addPreferredNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.PREFERRED_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.PREFERRED_NAME);
		}
	}

	@Override
	void addGermplasmDateColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GERMPLASM_DATE.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.GERMPLASM_DATE);
		}
	}

	@Override
	void addLocationColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GERMPLASM_LOCATION.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.GERMPLASM_LOCATION);
		}
	}

	@Override
	void addMethodNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_NAME);
		}
	}

	@Override
	void addMethodAbbrevColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_ABBREVIATION);
		}
	}

	@Override
	void addMethodNumberColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_NUMBER);
		}
	}

	@Override
	void addMethodGroupColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_GROUP);
		}
	}

	@Override
	void addCrossMaleGIDColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_MALE_GID.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.CROSS_MALE_GID);
		}
	}

	@Override
	void addCrossMalePrefNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.CROSS_MALE_PREFERRED_NAME);
		}
	}

	@Override
	void addCrossFemaleGidColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_FEMALE_GID.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.CROSS_FEMALE_GID);
		}
	}

	@Override
	void addCrossFemalePrefNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME);
		}
	}

	@Override
	void displayFillWithAttributeWindow() {
		// Get the main window to make sure that the attribute window will be
		// added to the main window instead of the sub window
		final Window mainWindow = this.addColumnSource.getWindow().getApplication().getMainWindow();

		// 2nd parameter is null because user is yet to select the attribute
		// type, which will become column name
		final Window attributeWindow = new FillWithAttributeWindow(this.addColumnSource, null, true);
		attributeWindow.setStyleName(Reindeer.WINDOW_LIGHT);
		mainWindow.addWindow(attributeWindow);
	}

}
