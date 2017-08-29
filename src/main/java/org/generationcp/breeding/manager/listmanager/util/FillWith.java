
package org.generationcp.breeding.manager.listmanager.util;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.AdditionalDetailsCrossNameComponent;
import org.generationcp.breeding.manager.listmanager.FillWithAttributeWindow;
import org.generationcp.breeding.manager.listmanager.GermplasmColumnValuesGenerator;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.HeaderClickEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class FillWith implements InternationalizableComponent {

	private final class TableHeaderClickListener implements Table.HeaderClickListener {

		private static final long serialVersionUID = 4792602001489368804L;

		@Override
		public void headerClick(HeaderClickEvent event) {
			if (event.getButton() == com.vaadin.event.MouseEvents.ClickEvent.BUTTON_RIGHT) {
				String column = (String) event.getPropertyId();
				FillWith.this.fillWithMenu.setData(column);
				if (column.equals(ColumnLabels.ENTRY_CODE.getName())) {
					FillWith.this.menuFillWithLocationName.setVisible(false);
					FillWith.this.menuFillWithCrossExpansion.setVisible(false);
					FillWith.this.setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(true);
					FillWith.this.fillWithMenu.show(event.getClientX(), event.getClientY());
				} else if (column.equals(ColumnLabels.SEED_SOURCE.getName())) {
					FillWith.this.menuFillWithLocationName.setVisible(true);
					FillWith.this.menuFillWithCrossExpansion.setVisible(false);
					FillWith.this.setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(true);
					FillWith.this.fillWithMenu.show(event.getClientX(), event.getClientY());
				} else if (column.equals(ColumnLabels.PARENTAGE.getName())) {
					FillWith.this.setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(false);
					FillWith.this.menuFillWithLocationName.setVisible(false);
					FillWith.this.menuFillWithCrossExpansion.setVisible(true);
					FillWith.this.fillWithMenu.show(event.getClientX(), event.getClientY());
				}
			}
		}
	}

	private final class FillWithMenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2384037190598803030L;

		@Override
		public void contextItemClick(ClickEvent event) {
			// Get reference to clicked item
			ContextMenuItem clickedItem = event.getClickedItem();
			final String clickedOptionName = clickedItem.getName();

			final String columnName = (String) FillWith.this.fillWithMenu.getData();

			if (clickedOptionName.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_EMPTY.getMessageKey()))) {
				valuesGenerator.fillWithEmpty(columnName);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_LOCATION.getMessageKey()))) {
				valuesGenerator.setLocationNameColumnValues(columnName);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_GERMPLASM_DATE.getMessageKey()))) {
				valuesGenerator.setGermplasmDateColumnValues(columnName);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_PREFERRED_NAME.getMessageKey()))) {
				valuesGenerator.setPreferredNameColumnValues(columnName);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_PREFERRED_ID.getMessageKey()))) {
				valuesGenerator.setPreferredIdColumnValues(columnName);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_ATTRIBUTE.getMessageKey()))) {
				FillWith.this.displayFillWithAttributeWindow(columnName);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NAME.getMessageKey()))) {
				valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV.getMessageKey()))) {
				valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER.getMessageKey()))) {
				valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP.getMessageKey()))) {
				valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_GID.getMessageKey()))) {
				valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_CROSS_FEMALE_GID);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME.getMessageKey()))) {
				valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_CROSS_FEMALE_NAME);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_GID.getMessageKey()))) {
				valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_CROSS_MALE_GID);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_NAME.getMessageKey()))) {
				valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_CROSS_MALE_NAME);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_EXPANSION.getMessageKey()))) {
				FillWith.this.displayExpansionLevelPopupWindow(columnName);
			} else if (clickedOptionName
					.equals(FillWith.this.messageSource.getMessage(FillWithOption.FILL_WITH_SEQUENCE_NUMBMER.getMessageKey()))) {
				FillWith.this.displaySequenceNumberPopupWindow(columnName);
			}
		}
	}

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
		this.headerClickListener = new TableHeaderClickListener();

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
		this.menuFillWithCrossMalePreferredName = this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_CROSS_MALE_GID, this.menuFillWithCrossMaleInformation);

		this.menuFillWithCrossExpansion = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_CROSS_EXPANSION);
		this.menuFillWithSequenceNumber = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_SEQUENCE_NUMBMER);

		this.fillWithMenu.addListener(new FillWithMenuClickListener());

		if (this.parentLayout != null) {
			this.parentLayout.addComponent(this.fillWithMenu);
		}
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


	private void displayFillWithAttributeWindow(final String columnName) {
		final Window mainWindow = this.addColumnSource.getWindow();
		Window attributeWindow = new FillWithAttributeWindow(this.addColumnSource, columnName);
		attributeWindow.setStyleName(Reindeer.WINDOW_LIGHT);
		mainWindow.addWindow(attributeWindow);
	}

	private void displayExpansionLevelPopupWindow(final String columnName) {
		final Window specifyCrossExpansionLevelWindow = new BaseSubWindow("Specify Expansion Level");
		specifyCrossExpansionLevelWindow.setHeight("135px");
		specifyCrossExpansionLevelWindow.setWidth("250px");
		specifyCrossExpansionLevelWindow.setModal(true);
		specifyCrossExpansionLevelWindow.setResizable(false);
		specifyCrossExpansionLevelWindow.setStyleName(Reindeer.WINDOW_LIGHT);

		AbsoluteLayout layout = new AbsoluteLayout();
		layout.setDebugId("layout");
		final ComboBox levelComboBox = new ComboBox();
		levelComboBox.setDebugId("levelComboBox");
		levelComboBox.setDebugId("levelComboBox");
		for (int ctr = 1; ctr <= 5; ctr++) {
			levelComboBox.addItem(Integer.valueOf(ctr));
		}
		levelComboBox.setValue(Integer.valueOf(1));
		levelComboBox.setNullSelectionAllowed(false);
		layout.addComponent(levelComboBox, "top:10px;left:10px");

		Button okButton = new Button(this.messageSource.getMessage(Message.OK));
		okButton.setDebugId("okButton");
		okButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -3519880320817778816L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				final Integer crossExpansionLevel = (Integer) levelComboBox.getValue();
				FillWith.this.valuesGenerator.fillWithCrossExpansion(crossExpansionLevel, columnName);
				FillWith.this.addColumnSource.getWindow().removeWindow(specifyCrossExpansionLevelWindow);
			}
		});
		okButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		layout.addComponent(okButton, "top:50px;left:10px");

		Button cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		cancelButton.setDebugId("cancelButton");
		cancelButton.setStyleName(Bootstrap.Buttons.DEFAULT.styleName());
		cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -3519880320817778816L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				FillWith.this.addColumnSource.getWindow().removeWindow(specifyCrossExpansionLevelWindow);
			}
		});
		layout.addComponent(cancelButton, "top:50px;left:60px");

		specifyCrossExpansionLevelWindow.setContent(layout);

		this.addColumnSource.getWindow().addWindow(specifyCrossExpansionLevelWindow);
	}

	private void displaySequenceNumberPopupWindow(String propertyId) {
		Window specifySequenceNumberWindow = new BaseSubWindow("Specify Sequence Number");
		specifySequenceNumberWindow.setHeight("320px");
		specifySequenceNumberWindow.setWidth("530px");
		specifySequenceNumberWindow.setModal(true);
		specifySequenceNumberWindow.setResizable(false);
		specifySequenceNumberWindow.setContent(new AdditionalDetailsCrossNameComponent(this, propertyId, specifySequenceNumberWindow));
		specifySequenceNumberWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		this.addColumnSource.getWindow().addWindow(specifySequenceNumberWindow);
	}
	
	public void fillWithSequence(final String columnName, final String prefix, final String suffix, final int startNumber, final int numOfZeros,
			final boolean spaceBetweenPrefixAndCode, final boolean spaceBetweenSuffixAndCode) {
		valuesGenerator.fillWithSequence(columnName, prefix, suffix, startNumber, numOfZeros, spaceBetweenPrefixAndCode, spaceBetweenSuffixAndCode);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	private void setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(boolean visibility) {
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
}
