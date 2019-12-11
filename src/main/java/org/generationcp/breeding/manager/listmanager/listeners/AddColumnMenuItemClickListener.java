
package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.FillWithAttributeWindow;
import org.generationcp.breeding.manager.listmanager.FillWithGermplasmNameWindow;
import org.generationcp.breeding.manager.listmanager.GermplasmColumnValuesGenerator;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class AddColumnMenuItemClickListener implements ContextMenu.ClickListener {

	private static final long serialVersionUID = 1L;

	@Autowired
	protected SimpleResourceBundleMessageSource messageSource;

	protected GermplasmColumnValuesGenerator valuesGenerator;
	protected final AddColumnSource addColumnSource;

	public AddColumnMenuItemClickListener(final AddColumnSource addColumnSource) {
		this.addColumnSource = addColumnSource;
		this.valuesGenerator = new GermplasmColumnValuesGenerator(addColumnSource);
	}

	@Override
	public void contextItemClick(final ClickEvent event) {
		final ContextMenuItem clickedItem = event.getClickedItem();
		final String clickedOptionName = clickedItem.getName();
		if (this.messageSource.getMessage(FillWithOption.FILL_WITH_PREFERRED_ID.getMessageKey()).equals(clickedOptionName)) {
			this.addPreferredIdColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_PREFERRED_NAME.getMessageKey()).equals(clickedOptionName)) {
			this.addPreferredNameColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_GERMPLASM_DATE.getMessageKey()).equals(clickedOptionName)) {
			this.addGermplasmDateColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_LOCATION.getMessageKey()).equals(clickedOptionName)) {
			this.addLocationColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NAME.getMessageKey()).equals(clickedOptionName)) {
			this.addMethodNameColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV.getMessageKey())
				.equals(clickedOptionName)) {
			this.addMethodAbbrevColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER.getMessageKey())
				.equals(clickedOptionName)) {
			this.addMethodNumberColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP.getMessageKey())
				.equals(clickedOptionName)) {
			this.addMethodGroupColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_GID.getMessageKey()).equals(clickedOptionName)) {
			this.addCrossFemaleGidColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME.getMessageKey()).equals(clickedOptionName)) {
			this.addCrossFemalePrefNameColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_GID.getMessageKey()).equals(clickedOptionName)) {
			this.addCrossMaleGIDColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_NAME.getMessageKey()).equals(clickedOptionName)) {
			this.addCrossMalePrefNameColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_ATTRIBUTE.getMessageKey()).equals(clickedOptionName)) {
			this.displayFillWithAttributeWindow();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_GROUP_SOURCE_GID.getMessageKey()).equals(clickedOptionName)) {
			this.addGroupSourceGidColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_GROUP_SOURCE_PREFERRED_NAME.getMessageKey())
				.equals(clickedOptionName)) {
			this.addGroupPreferredNameColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_GID.getMessageKey()).equals(clickedOptionName)) {
			this.addImmediateSourceGidColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_PREFERRED_NAME.getMessageKey())
				.equals(clickedOptionName)) {
			this.addImmediateSourcePreferredNameColumn();
		} else if (this.messageSource.getMessage(FillWithOption.FILL_WITH_GERMPLASM_NAME.getMessageKey()).equals(clickedOptionName)) {
			this.displayFillWithGermplasmNameWindow();
		}

	}

	void addImmediateSourcePreferredNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME);
			this.valuesGenerator.setImmediateSourcePreferredNameColumnValues(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName());
		}
	}

	void addImmediateSourceGidColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.IMMEDIATE_SOURCE_GID.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.IMMEDIATE_SOURCE_GID);
			this.valuesGenerator.setImmediateSourceGidColumnValues(ColumnLabels.IMMEDIATE_SOURCE_GID.getName());
		}
	}

	void addGroupPreferredNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME);
			this.valuesGenerator.setGroupSourcePreferredNameColumnValues(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName());
		}
	}

	void addGroupSourceGidColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GROUP_SOURCE_GID.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.GROUP_SOURCE_GID);
			this.valuesGenerator.setGroupSourceGidColumnValues(ColumnLabels.GROUP_SOURCE_GID.getName());
		}
	}

	void addPreferredIdColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.PREFERRED_ID.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.PREFERRED_ID);
			this.valuesGenerator.setPreferredIdColumnValues(ColumnLabels.PREFERRED_ID.getName());
		}
	}

	void addPreferredNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.PREFERRED_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.PREFERRED_NAME);
			this.valuesGenerator.setPreferredNameColumnValues(ColumnLabels.PREFERRED_NAME.getName());
		}
	}

	void addGermplasmDateColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GERMPLASM_DATE.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.GERMPLASM_DATE);
			this.valuesGenerator.setGermplasmDateColumnValues(ColumnLabels.GERMPLASM_DATE.getName());
		}
	}

	void addLocationColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GERMPLASM_LOCATION.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.GERMPLASM_LOCATION);
			this.valuesGenerator.setLocationNameColumnValues(ColumnLabels.GERMPLASM_LOCATION.getName());
		}
	}

	void addMethodNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_NAME);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NAME.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
		}
	}

	void addMethodAbbrevColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_ABBREVIATION);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV);
		}
	}

	void addMethodNumberColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_NUMBER);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NUMBER.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER);
		}
	}

	void addMethodGroupColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_GROUP);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_GROUP.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
		}
	}

	void addCrossMaleGIDColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.MGID.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.MGID);
			this.valuesGenerator.setCrossMaleGIDColumnValues(ColumnLabels.MGID.getName());
		}
	}

	void addCrossMalePrefNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.CROSS_MALE_PREFERRED_NAME);
			this.valuesGenerator.setCrossMalePrefNameColumnValues(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());
		}
	}

	void addCrossFemaleGidColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.FGID.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.FGID);
			this.valuesGenerator.setCrossFemaleInfoColumnValues(ColumnLabels.FGID.getName(),
					FillWithOption.FILL_WITH_CROSS_FEMALE_GID);
		}
	}

	void addCrossFemalePrefNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
			this.addColumnSource.addColumn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME);
			this.valuesGenerator.setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(),
					FillWithOption.FILL_WITH_CROSS_FEMALE_NAME);
		}
	}

	void displayFillWithAttributeWindow() {
		final Window mainWindow = this.addColumnSource.getWindow();
		// 2nd parameter is null because user is yet to select the attribute type, which will become column name
		final Window attributeWindow = new FillWithAttributeWindow(this.addColumnSource, null, this.isFromGermplasmSearchWindow());
		attributeWindow.setStyleName(Reindeer.WINDOW_LIGHT);
		mainWindow.addWindow(attributeWindow);
	}

	void displayFillWithGermplasmNameWindow() {
		final Window mainWindow = this.addColumnSource.getWindow();
		// 2nd parameter is null because user is yet to select the name type, which will become column name
		final Window attributeWindow = new FillWithGermplasmNameWindow(this.addColumnSource, null, this.isFromGermplasmSearchWindow());
		attributeWindow.setStyleName(Reindeer.WINDOW_LIGHT);
		mainWindow.addWindow(attributeWindow);
	}

	Boolean isFromGermplasmSearchWindow() {
		return false;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setValuesGenerator(final GermplasmColumnValuesGenerator valuesGenerator) {
		this.valuesGenerator = valuesGenerator;
	}

}
