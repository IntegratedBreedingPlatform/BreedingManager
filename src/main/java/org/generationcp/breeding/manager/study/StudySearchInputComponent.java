/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.study;

import java.util.List;

import org.generationcp.breeding.manager.application.GermplasmStudyBrowserLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.commons.vaadin.validator.RegexValidator;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author Joyce Avestro
 *
 */
@Configurable
public class StudySearchInputComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		GermplasmStudyBrowserLayout {

	private static final Logger LOG = LoggerFactory.getLogger(StudySearchInputComponent.class);
	private static final long serialVersionUID = 1L;

	List<Study> studies;

	private Panel searchPanel;
	private Label dateLabel;
	private Label nameLabel;
	private Label countryLabel;
	private Label seasonLabel;

	private GridLayout searchFieldsLayout;
	private TextField dateYearField;
	private TextField dateMonthField;
	private TextField dateDayField;
	private TextField nameField;
	private ComboBox countryCombo;
	private ComboBox seasonCombo;

	private Button searchButton;
	private Button clearButton;
	private Component buttonArea;

	private final StudySearchMainComponent parentComponent;
	private Label searchCriteriaLabel;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private PlatformTransactionManager transactionManager;


	public StudySearchInputComponent(StudySearchMainComponent parentComponent) {
		this.parentComponent = parentComponent;
	}

	@Override
	public void afterPropertiesSet() {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.dateYearField = new TextField();
		this.dateYearField.setDescription(this.messageSource.getMessage(Message.DATE_YEAR_FIELD_DESCRIPTION)); // "Input at least the year for the search date."
		this.dateYearField.setWidth(1, Sizeable.UNITS_CM);
		this.dateYearField.addValidator(new RegexValidator(this.messageSource.getMessage(Message.ERROR_YEAR_FORMAT),
				"[1-3][0-9][0-9][0-9]", true)); // "Year must be in format YYYY"

		this.dateMonthField = new TextField();
		this.dateMonthField.setWidth(1, Sizeable.UNITS_CM);
		this.dateMonthField.addValidator(new RegexValidator(this.messageSource.getMessage(Message.ERROR_MONTH_FORMAT), "[1-9]|[0-1][0-9]",
				true)); // "Month must be in format MM"

		this.dateDayField = new TextField();
		this.dateDayField.setWidth(1, Sizeable.UNITS_CM);
		this.dateDayField
				.addValidator(new RegexValidator(this.messageSource.getMessage(Message.ERROR_DAY_FORMAT), "[1-9]|[0-3][0-9]", true)); // "Day must be in format DD"

		this.nameField = new TextField();
		this.nameField.setDescription(this.messageSource.getMessage(Message.EXACT_STUDY_NAME_TEXT));
		this.countryCombo = this.createCountryComboBox();
		this.seasonCombo = this.createSeasonComboBox();

		this.dateLabel = new Label(this.messageSource.getMessage(Message.START_DATE_LABEL));
		this.nameLabel = new Label(this.messageSource.getMessage(Message.NAME_LABEL));
		this.countryLabel = new Label(this.messageSource.getMessage(Message.COUNTRY_LABEL));
		this.seasonLabel = new Label(this.messageSource.getMessage(Message.SEASON_LABEL));

		// Buttons
		this.searchButton = new Button(this.messageSource.getMessage(Message.SEARCH_LABEL));
		this.searchButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
		this.clearButton = new Button(this.messageSource.getMessage(Message.CLEAR_LABEL));

		this.searchCriteriaLabel = new Label("<b>" + this.messageSource.getMessage(Message.SEARCH_CRITERIA) + "</b>", Label.CONTENT_XHTML);
		this.searchCriteriaLabel.setWidth("120px");
	}

	@Override
	public void initializeValues() {
		// empty function for now
	}

	@Override
	public void addListeners() {
		ButtonClickListener buttonClickListener = new ButtonClickListener();
		this.searchButton.addListener(buttonClickListener);
		this.clearButton.addListener(buttonClickListener);
		this.searchButton.setClickShortcut(KeyCode.ENTER);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {
		this.setSpacing(true);
		this.setWidth("300px");

		GridLayout dateLayout = new GridLayout();
		dateLayout.setRows(3);
		dateLayout.setColumns(4);
		dateLayout.addComponent(this.dateYearField, 1, 1);
		dateLayout.addComponent(this.dateMonthField, 2, 1);
		dateLayout.addComponent(this.dateDayField, 3, 1);
		dateLayout.addComponent(new Label("Year"), 1, 2);
		dateLayout.addComponent(new Label("Month"), 2, 2);
		dateLayout.addComponent(new Label("Day"), 3, 2);

		this.searchFieldsLayout = new GridLayout();
		this.searchFieldsLayout.setRows(5);
		this.searchFieldsLayout.setColumns(3);
		this.searchFieldsLayout.setSpacing(true);
		this.searchFieldsLayout.addComponent(this.dateLabel, 1, 1);
		this.searchFieldsLayout.addComponent(dateLayout, 2, 1);
		this.searchFieldsLayout.addComponent(this.nameLabel, 1, 2);
		this.searchFieldsLayout.addComponent(this.nameField, 2, 2);
		this.searchFieldsLayout.addComponent(this.countryLabel, 1, 3);
		this.searchFieldsLayout.addComponent(this.countryCombo, 2, 3);
		this.searchFieldsLayout.addComponent(this.seasonLabel, 1, 4);
		this.searchFieldsLayout.addComponent(this.seasonCombo, 2, 4);

		this.buttonArea = this.layoutButtonArea();

		VerticalLayout searchLayout = new VerticalLayout();
		searchLayout.addComponent(this.searchFieldsLayout);
		searchLayout.addComponent(this.buttonArea);
		searchLayout.setComponentAlignment(this.buttonArea, Alignment.BOTTOM_CENTER);

		this.searchPanel = new Panel();
		this.searchPanel.setWidth("300px");
		this.searchPanel.setHeight("250px");
		this.searchPanel.setLayout(searchLayout);

		this.addComponent(this.searchCriteriaLabel);
		this.addComponent(this.searchPanel);
	}

	protected Component layoutButtonArea() {
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true, false, false, false);

		buttonLayout.addComponent(this.clearButton);
		buttonLayout.addComponent(this.searchButton);
		return buttonLayout;
	}

	@SuppressWarnings("deprecation")
	private ComboBox createCountryComboBox() {
		List<Country> countries = null;
		try {
			countries = this.germplasmDataManager.getAllCountry();
		} catch (MiddlewareQueryException e) {
			StudySearchInputComponent.LOG.error("Error encountered while getting countries", e);
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_PLEASE_CONTACT_ADMINISTRATOR);
		}

		BeanItemContainer<Country> beanItemContainer = new BeanItemContainer<Country>(Country.class);
		for (Country country : countries) {
			beanItemContainer.addBean(country);
		}

		ComboBox comboBox = new ComboBox();
		comboBox.setContainerDataSource(beanItemContainer);
		comboBox.setItemCaptionPropertyId("isoabbr");
		comboBox.setImmediate(true);

		return comboBox;
	}

	private ComboBox createSeasonComboBox() {
		Season[] seasons = Season.values();

		ComboBox comboBox = new ComboBox();
		for (Season season : seasons) {
			comboBox.addItem(season);
		}
		comboBox.setImmediate(true);

		return comboBox;
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
	}

	private class ButtonClickListener implements ClickListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					Integer dateValue = null;
					String nameValue = null;
					String countryValue = null;
					Season seasonValue = null;

					if (event.getButton() == StudySearchInputComponent.this.searchButton) {
						if (StudySearchInputComponent.this.areDateFieldsValid()) {
							dateValue = StudySearchInputComponent.this.getDateValue();

							if (StudySearchInputComponent.this.nameField != null && StudySearchInputComponent.this.nameField.getValue() != null) {
								nameValue = StudySearchInputComponent.this.nameField.getValue().toString();
							}
							if (StudySearchInputComponent.this.countryCombo != null
									&& StudySearchInputComponent.this.countryCombo.getValue() != null) {
								countryValue = ((Country) StudySearchInputComponent.this.countryCombo.getValue()).getIsofull();
							}
							if (StudySearchInputComponent.this.seasonCombo != null && StudySearchInputComponent.this.seasonCombo.getValue() != null) {
								seasonValue = (Season) StudySearchInputComponent.this.seasonCombo.getValue();
							}
							StudySearchInputComponent.this.parentComponent.getSearchResultComponent().searchStudy(nameValue, countryValue,
									seasonValue, dateValue);
						}

					} else if (event.getButton() == StudySearchInputComponent.this.clearButton) {
						StudySearchInputComponent.this.dateYearField.setValue("");
						StudySearchInputComponent.this.dateMonthField.setValue("");
						StudySearchInputComponent.this.dateDayField.setValue("");
						StudySearchInputComponent.this.nameField.setValue("");
						StudySearchInputComponent.this.countryCombo.setValue(null);
						StudySearchInputComponent.this.seasonCombo.setValue(null);
						StudySearchInputComponent.this.requestRepaint();
					}
				}
			});
		}
	}

	// TODO soon to be moved in a Date Utility Class in IBPCommons
	public boolean areDateFieldsValid() {
		try {
			this.dateYearField.validate();
			this.dateMonthField.validate();
			this.dateDayField.validate();

			Integer yearValue = this.getYearValue();
			Integer monthValue = this.getMonthValue();
			Integer dayValue = this.getDayValue();

			if (monthValue > 0 && yearValue == 0) {
				MessageNotifier.showRequiredFieldError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_MONTH_WITHOUT_YEAR)); // "Month cannot be specified without the year."
				return false;
			}

			if (dayValue > 0 && (yearValue == 0 || monthValue == 0)) {
				MessageNotifier.showRequiredFieldError(this.getWindow(),
						this.messageSource.getMessage(Message.ERROR_DAY_WITHOUT_MONTH_YEAR)); // "Day cannot be specified without the year or month."
				return false;
			}

			return true;

		} catch (InvalidValueException e) {
			MessageNotifier.showRequiredFieldError(this.getWindow(), e.getMessage());
			return false;
		} catch (NumberFormatException e) {
			MessageNotifier.showRequiredFieldError(this.getWindow(), e.getMessage());
			return false;
		}
	}

	// TODO soon to be moved in a Date Utility Class in IBPCommons
	public Integer getDateValue() {
		Integer dateValue = null;
		Integer yearValue = this.getYearValue();
		Integer monthValue = this.getMonthValue();
		Integer dayValue = this.getDayValue();

		dateValue = DateUtil.getIBPDateNoZeroes(yearValue, monthValue, dayValue);

		if (dateValue == 0) {
			dateValue = null;
		}

		return null;
	}

	// TODO soon to be moved in a Date Utility Class in IBPCommons
	private Integer getDayValue() {
		if (this.dateDayField != null && this.dateDayField.getValue() != null) {
			String value = this.dateDayField.getValue().toString();

			if (value.length() > 0) {
				return Integer.valueOf(this.dateDayField.getValue().toString());
			}
		}
		return 0;
	}

	private Integer getYearValue() {
		if (this.dateYearField != null && this.dateYearField.getValue() != null) {
			String value = this.dateYearField.getValue().toString();
			if (value.length() > 0) {
				return Integer.valueOf(value);
			}
		}
		return 0;
	}

	private Integer getMonthValue() {
		if (this.dateMonthField != null && this.dateMonthField.getValue() != null) {
			String value = this.dateMonthField.getValue().toString();

			if (value.length() > 0) {
				return Integer.valueOf(this.dateMonthField.getValue().toString());
			}
		}

		return 0;
	}
}
