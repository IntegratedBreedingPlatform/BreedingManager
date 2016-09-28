
package org.generationcp.breeding.manager.inventory;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.OptionGroup;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

@Configurable
public class ReserveInventoryRowComponent extends HorizontalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	private static final long serialVersionUID = -3514286698020753573L;

	private Label amountToReserveLbl;

	public OptionGroup getReserveOption() {
		return reserveOption;
	}

	private OptionGroup reserveOption;
	private TextField reservedAmtTxtField;
	private Label scaleLabel;
	private final int selectedLotPerScale;
	private String scale;
	private final String DEFAULT_ERROR = "Please specify a valid number.";;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public ReserveInventoryRowComponent(String scale, int selectedLotPerScale) {
		super();
		this.selectedLotPerScale = selectedLotPerScale;
		this.scale = scale;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.amountToReserveLbl = new Label(this.messageSource.getMessage(Message.AMOUNT_TO_RESERVE));
		this.amountToReserveLbl.setDebugId("amountToReserveLbl");
		this.amountToReserveLbl.addStyleName(AppConstants.CssStyles.BOLD);

		this.reserveOption = new OptionGroup();
		this.reserveOption.setDebugId("reserveOption");
		this.reserveOption.setWidth("180px");
		this.reserveOption.addItem(this.messageSource.getMessage(Message.SEED_MANUAL_AMOUNT));
		this.reserveOption.addItem(this.messageSource.getMessage(Message.SEED_ALL_AMOUNT));

		this.reservedAmtTxtField = new TextField();
		this.reservedAmtTxtField.setDebugId("reservedAmtTxtField");
		this.reservedAmtTxtField.setWidth("45px");
		this.reservedAmtTxtField.setRequired(true);

		this.reservedAmtTxtField.setRequiredError(this.DEFAULT_ERROR);
		this.reservedAmtTxtField.addValidator(new DoubleRangeValidator(this.DEFAULT_ERROR));
		String scaleFullText = this.scale;
		this.scaleLabel = new Label(scaleFullText);
		this.scaleLabel.setDebugId("scaleLabel");
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void layoutComponents() {
		this.setSpacing(true);

		final HorizontalLayout amount = new HorizontalLayout();
		amount.setDebugId("amount");
		amount.setWidth("100%");
		amount.setSpacing(true);
		amount.addComponent(this.amountToReserveLbl);
		amount.addComponent(this.reserveOption);
		amount.addComponent(this.scaleLabel);
		amount.addComponent(this.reservedAmtTxtField);
		amount.setComponentAlignment(this.amountToReserveLbl, Alignment.TOP_LEFT);
		amount.setComponentAlignment(this.reserveOption,Alignment.TOP_LEFT);
		amount.setComponentAlignment(this.scaleLabel,Alignment.TOP_LEFT);
		amount.setComponentAlignment(this.reservedAmtTxtField,Alignment.TOP_LEFT);
		this.addComponent(amount);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	/** Double validator that accepts empty values */
	public class DoubleRangeValidator extends DoubleValidator {

		private static final long serialVersionUID = -3795353195313914432L;

		public DoubleRangeValidator(String message) {
			super(message);
		}

		@Override
		protected boolean isValidString(String value) {

			Double doubleValue;

			if (value.trim().length() == 0) {
				return false;
			}

			try {
				doubleValue = Double.valueOf(value);
			} catch (NumberFormatException e) {
				return false;
			}

			if (doubleValue > 0) {
				return true;
			} else {
				return false;
			}
		}
	}

	// SETTERS AND GETTERS
	public String getScale() {
		return this.scale;
	}

	public void setScale(String scale) {
		this.scale = scale;
	}

	public Double getReservationAmount() {
		String amountTxt = this.reservedAmtTxtField.getValue().toString();
		Double amount = Double.valueOf("0");

		if (amountTxt.length() > 0) {
			amount = Double.valueOf(amountTxt);
		}
		return amount;
	}

	public TextField getReservedAmtTxtField() {
		return this.reservedAmtTxtField;
	}

	public void validate() {
		this.reservedAmtTxtField.validate();
	}
}
