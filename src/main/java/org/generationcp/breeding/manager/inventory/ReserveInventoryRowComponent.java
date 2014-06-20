package org.generationcp.breeding.manager.inventory;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

@Configurable
public class ReserveInventoryRowComponent extends HorizontalLayout  implements InitializingBean, 
					InternationalizableComponent, BreedingManagerLayout {
	private static final long serialVersionUID = -3514286698020753573L;
	
	private Label amountToReserveLbl;
	private TextField reservedAmtTxtField;
	private Label scaleLabel;
	private int selectedLotPerScale;
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
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	@Override
	public void instantiateComponents() {
		amountToReserveLbl = new Label(messageSource.getMessage(Message.AMOUNT_TO_RESERVE) + ":");
		amountToReserveLbl.addStyleName(AppConstants.CssStyles.BOLD);
		
		reservedAmtTxtField = new TextField();
		reservedAmtTxtField.setWidth("45px");
		reservedAmtTxtField.setRequired(true);
		
		reservedAmtTxtField.setRequiredError(DEFAULT_ERROR);
		reservedAmtTxtField.addValidator(new DoubleRangeValidator(DEFAULT_ERROR));
		
		String scaleFullText = scale + " (" + selectedLotPerScale + " selected)";
		scaleLabel = new Label(scaleFullText);
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
		setSpacing(true);
		
		addComponent(amountToReserveLbl);
		addComponent(reservedAmtTxtField);
		addComponent(scaleLabel);
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
        	
        	if(value.trim().length() == 0){
        		return false;
        	}
        	
        	try {
        		doubleValue = Double.valueOf(value);
            } catch (NumberFormatException e) {
            	return false;
            }
        	
            if (doubleValue > 0){
                return true;
            }
            else{
                return false;
            }
        }
    }
	
	// SETTERS AND GETTERS
	public String getScale() {
		return scale;
	}

	public void setScale(String scale) {
		this.scale = scale;
	}
	
	public Double getReservationAmount(){
		String amountTxt = reservedAmtTxtField.getValue().toString();
		Double amount = Double.valueOf("0");
		
		if(amountTxt.length() > 0){
			amount = Double.valueOf(amountTxt);
		}
		return amount;
	}

	public TextField getReservedAmtTxtField() {
		return reservedAmtTxtField;
	}
	
	public void validate() throws InvalidValueException {
		reservedAmtTxtField.validate();
	}
}
