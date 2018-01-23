
package org.generationcp.breeding.manager.listmanager.util;

import org.generationcp.breeding.manager.listmanager.GermplasmColumnValuesGenerator;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.FillWithMenuItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.FillWithMenuTableHeaderClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Table;

@Configurable
public class FillWith implements InternationalizableComponent {

	private SimpleResourceBundleMessageSource messageSource;

	private AbstractLayout parentLayout;

	private ContextMenu fillWithMenu;
	private ContextMenuItem menuFillWithEmpty;
	private ContextMenuItem menuFillWithGermplasmDate;
	private ContextMenuItem menuFillWithPrefName;
	private ContextMenuItem menuFillWithPrefID;
	private ContextMenuItem menuFillWithAttribute;
	private ContextMenuItem menuFillWithLocationName;
	private ContextMenuItem menuFillWithBreedingMethodInfo;
	private ContextMenuItem menuFillWithBreedingMethodName;
	private ContextMenuItem menuFillWithBreedingMethodGroup;
	private ContextMenuItem menuFillWithBreedingMethodNumber;
	private ContextMenuItem menuFillWithBreedingMethodAbbreviation;
	private ContextMenuItem menuFillWithCrossFemaleInformation;
	private ContextMenuItem menuFillWithCrossFemaleGID;
	private ContextMenuItem menuFillWithCrossFemalePreferredName;
	private ContextMenuItem menuFillWithCrossMaleInformation;
	private ContextMenuItem menuFillWithCrossMaleGID;
	private ContextMenuItem menuFillWithCrossMalePreferredName;
	private ContextMenuItem menuFillWithCrossExpansion;
	private ContextMenuItem menuFillWithSequenceNumber;

	private Table.HeaderClickListener headerClickListener;

	// Though we are just filling up existing column(s), FillWithAttribute class requires AddColumnSource type
	private AddColumnSource addColumnSource;
	private GermplasmColumnValuesGenerator valuesGenerator;
	
	public FillWith(final AddColumnSource addColumnSource, final AbstractLayout parentLayout, final SimpleResourceBundleMessageSource messageSource) {
		this.addColumnSource = addColumnSource;
		this.valuesGenerator = new GermplasmColumnValuesGenerator(addColumnSource);
		this.parentLayout = parentLayout;
		this.messageSource = messageSource;
		this.setupContextMenu();
	}

	private void setupContextMenu() {
		this.fillWithMenu = new ContextMenu();
		this.fillWithMenu.setDebugId("fillWithMenu");
		this.fillWithMenu.setWidth("310px");

		this.menuFillWithEmpty = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_EMPTY);
		this.menuFillWithLocationName = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_LOCATION);
		this.menuFillWithPrefID = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_PREFERRED_ID);
		this.menuFillWithGermplasmDate = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_GERMPLASM_DATE);
		this.menuFillWithPrefName = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_PREFERRED_NAME);
		this.menuFillWithAttribute = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_ATTRIBUTE);

		this.menuFillWithBreedingMethodInfo = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO);
		this.menuFillWithBreedingMethodName = this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_BREEDING_METHOD_NAME, this.menuFillWithBreedingMethodInfo);
		this.menuFillWithBreedingMethodAbbreviation = this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV, this.menuFillWithBreedingMethodInfo);
		this.menuFillWithBreedingMethodNumber = this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER, this.menuFillWithBreedingMethodInfo);
		this.menuFillWithBreedingMethodGroup = this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP, this.menuFillWithBreedingMethodInfo);

		this.menuFillWithCrossFemaleInformation = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO);
		this.menuFillWithCrossFemaleGID = this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_CROSS_FEMALE_GID, this.menuFillWithCrossFemaleInformation);
		this.menuFillWithCrossFemalePreferredName = this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME, this.menuFillWithCrossFemaleInformation);

		this.menuFillWithCrossMaleInformation = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_CROSS_MALE_INFO);
		this.menuFillWithCrossMaleGID = this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_CROSS_MALE_GID, this.menuFillWithCrossMaleInformation);
		this.menuFillWithCrossMalePreferredName = this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_CROSS_MALE_NAME, this.menuFillWithCrossMaleInformation);

		this.menuFillWithCrossExpansion = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_CROSS_EXPANSION);
		this.menuFillWithSequenceNumber = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_SEQUENCE_NUMBER);

		this.fillWithMenu.addListener(new FillWithMenuItemClickListener(this, this.addColumnSource, this.fillWithMenu, this.valuesGenerator));

		if (this.parentLayout != null) {
			this.parentLayout.addComponent(this.fillWithMenu);
		}
		
		this.headerClickListener = new FillWithMenuTableHeaderClickListener(this, this.fillWithMenu, this.menuFillWithLocationName,
				this.menuFillWithCrossExpansion);
	}
	
	public void setTableHeaderListener(final Table table){
		table.addListener(this.headerClickListener);
	}

	private ContextMenuItem addFillWIthOptionToMenu(final FillWithOption option) {
		return this.fillWithMenu.addItem(this.messageSource.getMessage(option.getMessageKey()));
	}
	
	private ContextMenuItem addFillWithOptionToSubMenu(final FillWithOption option, final ContextMenuItem item) {
		return item.addItem(this.messageSource.getMessage(option.getMessageKey()));
	}

	public void setContextMenuEnabled(final Table table, final Boolean isEnabled) {
		table.removeListener(this.headerClickListener);
		if (isEnabled) {
			table.addListener(this.headerClickListener);
		}
	}
	
	public void fillWithSequence(final String columnName, final String prefix, final String suffix, final int startNumber, final int numOfZeros,
			final boolean spaceBetweenPrefixAndCode, final boolean spaceBetweenSuffixAndCode) {
		valuesGenerator.fillWithSequence(columnName, prefix, suffix, startNumber, numOfZeros, spaceBetweenPrefixAndCode, spaceBetweenSuffixAndCode);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public void setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(boolean visibility) {
		this.menuFillWithBreedingMethodAbbreviation.setVisible(visibility);
		this.menuFillWithBreedingMethodGroup.setVisible(visibility);
		this.menuFillWithBreedingMethodInfo.setVisible(visibility);
		this.menuFillWithBreedingMethodName.setVisible(visibility);
		this.menuFillWithBreedingMethodNumber.setVisible(visibility);
		this.menuFillWithCrossFemaleGID.setVisible(visibility);
		this.menuFillWithCrossFemaleInformation.setVisible(visibility);
		this.menuFillWithCrossFemalePreferredName.setVisible(visibility);
		this.menuFillWithCrossMaleGID.setVisible(visibility);
		this.menuFillWithCrossMaleInformation.setVisible(visibility);
		this.menuFillWithCrossMalePreferredName.setVisible(visibility);
		this.menuFillWithEmpty.setVisible(visibility);
		this.menuFillWithGermplasmDate.setVisible(visibility);
		this.menuFillWithPrefID.setVisible(visibility);
		this.menuFillWithPrefName.setVisible(visibility);
		this.menuFillWithAttribute.setVisible(visibility);
		this.menuFillWithSequenceNumber.setVisible(visibility);
	}
	
	public int getNumberOfEntries() {
		return this.addColumnSource.getItemIdsToProcess().size();
	}

	
	public ContextMenu getFillWithMenu() {
		return fillWithMenu;
	}

	
	public Table.HeaderClickListener getHeaderClickListener() {
		return headerClickListener;
	}
	
}
