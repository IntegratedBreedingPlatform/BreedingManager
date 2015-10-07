
package org.generationcp.breeding.manager.inventory;

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
		this.amountToReserveLbl = new Label(this.messageSource.getMessage(Message.AMOUNT_TO_RESERVE) + ":");
		this.amountToReserveLbl.addStyleName(AppConstants.CssStyles.BOLD);

		this.reservedAmtTxtField = new TextField();
		this.reservedAmtTxtField.setWidth("45px");
		this.reservedAmtTxtField.setRequired(true);

		this.reservedAmtTxtField.setRequiredError(this.DEFAULT_ERROR);
		this.reservedAmtTxtField.addValidator(new DoubleRangeValidator(this.DEFAULT_ERROR));

		String scaleFullText = this.scale + " (" + this.selectedLotPerScale + " selected)";
		this.scaleLabel = new Label(scaleFullText);
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

		this.addComponent(this.amountToReserveLbl);
		this.addComponent(this.reservedAmtTxtField);
		this.addComponent(this.scaleLabel);
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
