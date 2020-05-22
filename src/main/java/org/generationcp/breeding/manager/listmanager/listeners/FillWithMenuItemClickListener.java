
package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.AdditionalDetailsCrossNameComponent;
import org.generationcp.breeding.manager.listmanager.FillWithAttributeWindow;
import org.generationcp.breeding.manager.listmanager.GermplasmColumnValuesGenerator;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ClickListener;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class FillWithMenuItemClickListener implements ClickListener {

	public static final String SPECIFY_EXPANSION_LEVEL = "Specify Expansion Level";

	public static final String SPECIFY_SEQUENCE_NUMBER = "Specify Sequence Number";

	private static final long serialVersionUID = -2384037190598803030L;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private final FillWith fillWith;
	private final AddColumnSource addColumnSource;
	private final ContextMenu fillWithMenu;
	private final GermplasmColumnValuesGenerator valuesGenerator;

	public FillWithMenuItemClickListener(final FillWith fillWith, final AddColumnSource addColumnSource,
			final ContextMenu fillWithMenu, final GermplasmColumnValuesGenerator valuesGenerator) {
		super();
		this.fillWith = fillWith;
		this.addColumnSource = addColumnSource;
		this.fillWithMenu = fillWithMenu;
		this.valuesGenerator = valuesGenerator;
	}

	@Override
	public void contextItemClick(final ClickEvent event) {
		// Get reference to clicked item
		final ContextMenuItem clickedItem = event.getClickedItem();
		final String clickedOptionName = clickedItem.getName();
		final String columnName = (String) this.fillWithMenu.getData();

		if (clickedOptionName.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_EMPTY.getMessageKey()))) {
			this.valuesGenerator.fillWithEmpty(columnName);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_LOCATION.getMessageKey()))) {
			this.valuesGenerator.setLocationNameColumnValues(columnName);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_GERMPLASM_DATE.getMessageKey()))) {
			this.valuesGenerator.setGermplasmDateColumnValues(columnName);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_PREFERRED_NAME.getMessageKey()))) {
			this.valuesGenerator.setPreferredNameColumnValues(columnName);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_PREFERRED_ID.getMessageKey()))) {
			this.valuesGenerator.setPreferredIdColumnValues(columnName);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_ATTRIBUTE.getMessageKey()))) {
			this.displayFillWithAttributeWindow(columnName);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NAME.getMessageKey()))) {
			this.valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
		} else if (clickedOptionName.equals(
				this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV.getMessageKey()))) {
			this.valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV);
		} else if (clickedOptionName.equals(
				this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER.getMessageKey()))) {
			this.valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER);
		} else if (clickedOptionName.equals(
				this.messageSource.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP.getMessageKey()))) {
			this.valuesGenerator.setMethodInfoColumnValues(columnName, FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_GID.getMessageKey()))) {
			this.valuesGenerator.setCrossFemaleInfoColumnValues(columnName, FillWithOption.FILL_WITH_CROSS_FEMALE_GID);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME.getMessageKey()))) {
			this.valuesGenerator.setCrossFemaleInfoColumnValues(columnName, FillWithOption.FILL_WITH_CROSS_FEMALE_NAME);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_GID.getMessageKey()))) {
			this.valuesGenerator.setCrossMaleGIDColumnValues(columnName);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_NAME.getMessageKey()))) {
			this.valuesGenerator.setCrossMalePrefNameColumnValues(columnName);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_CROSS_EXPANSION.getMessageKey()))) {
			this.displayExpansionLevelPopupWindow(columnName);
		} else if (clickedOptionName
				.equals(this.messageSource.getMessage(FillWithOption.FILL_WITH_SEQUENCE_NUMBER.getMessageKey()))) {
			this.displaySequenceNumberPopupWindow(columnName);
		}
	}

	private void displayFillWithAttributeWindow(final String columnName) {
		final Window mainWindow = this.addColumnSource.getWindow();
		final Window attributeWindow = new FillWithAttributeWindow(this.addColumnSource, columnName, false);
		attributeWindow.setStyleName(Reindeer.WINDOW_LIGHT);
		mainWindow.addWindow(attributeWindow);
	}

	private void displayExpansionLevelPopupWindow(final String columnName) {
		final Window specifyCrossExpansionLevelWindow = new BaseSubWindow(
				FillWithMenuItemClickListener.SPECIFY_EXPANSION_LEVEL);
		specifyCrossExpansionLevelWindow.setHeight("135px");
		specifyCrossExpansionLevelWindow.setWidth("250px");
		specifyCrossExpansionLevelWindow.setModal(true);
		specifyCrossExpansionLevelWindow.setResizable(false);
		specifyCrossExpansionLevelWindow.setStyleName(Reindeer.WINDOW_LIGHT);

		final AbsoluteLayout layout = new AbsoluteLayout();
		layout.setDebugId("layout");
		final ComboBox levelComboBox = new ComboBox();
		levelComboBox.setDebugId("levelComboBox");
		levelComboBox.setDebugId("levelComboBox");
		for (int ctr = 1; ctr <= 5; ctr++) {
			levelComboBox.addItem(ctr);
		}
		levelComboBox.setValue(1);
		levelComboBox.setNullSelectionAllowed(false);
		layout.addComponent(levelComboBox, "top:10px;left:10px");

		final Button okButton = new Button(this.messageSource.getMessage(Message.OK));
		okButton.setDebugId("okButton");
		okButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -3519880320817778816L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				final Integer crossExpansionLevel = (Integer) levelComboBox.getValue();
				try{
					FillWithMenuItemClickListener.this.valuesGenerator.fillWithCrossExpansion(crossExpansionLevel,
							columnName);
				}catch (final MiddlewareException ex){
					final String gid = ex.getMessage().replaceAll("\\D", "");
					MessageNotifier.showError(FillWithMenuItemClickListener.this.addColumnSource.getWindow(), "Error with Cross Expansion", String.format("There is a data problem that prevents the generation of the cross expansion for GID '%s'. Please contact your administrator", gid));
				}

				FillWithMenuItemClickListener.this.addColumnSource.getWindow()
						.removeWindow(specifyCrossExpansionLevelWindow);
			}
		});
		okButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		layout.addComponent(okButton, "top:50px;left:10px");

		final Button cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		cancelButton.setDebugId("cancelButton");
		cancelButton.setStyleName(Bootstrap.Buttons.DEFAULT.styleName());
		cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -3519880320817778816L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				FillWithMenuItemClickListener.this.addColumnSource.getWindow()
						.removeWindow(specifyCrossExpansionLevelWindow);
			}
		});
		layout.addComponent(cancelButton, "top:50px;left:60px");

		specifyCrossExpansionLevelWindow.setContent(layout);

		this.addColumnSource.getWindow().addWindow(specifyCrossExpansionLevelWindow);
	}

	private void displaySequenceNumberPopupWindow(final String propertyId) {
		final Window specifySequenceNumberWindow = new BaseSubWindow(
				FillWithMenuItemClickListener.SPECIFY_SEQUENCE_NUMBER);
		specifySequenceNumberWindow.setHeight("320px");
		specifySequenceNumberWindow.setWidth("530px");
		specifySequenceNumberWindow.setModal(true);
		specifySequenceNumberWindow.setResizable(false);
		specifySequenceNumberWindow.setContent(
				new AdditionalDetailsCrossNameComponent(this.fillWith, propertyId, specifySequenceNumberWindow));
		specifySequenceNumberWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		this.addColumnSource.getWindow().addWindow(specifySequenceNumberWindow);
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

}
