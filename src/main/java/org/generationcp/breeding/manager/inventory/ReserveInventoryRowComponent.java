package org.generationcp.breeding.manager.inventory;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

@Configurable
public class ReserveInventoryRowComponent extends HorizontalLayout  implements InitializingBean, 
					InternationalizableComponent, BreedingManagerLayout {
	private static final long serialVersionUID = -3514286698020753573L;
	
	private Label amountToReserveLbl;
	private TextField quantityTxtField;
	private Label scaleLabel;
	private int selectedLotPerScale;
	private String scale;
	
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
		amountToReserveLbl = new Label(messageSource.getMessage(Message.AMOUNT_TO_RESERVE));
		amountToReserveLbl.addStyleName(AppConstants.CssStyles.BOLD);
		
		quantityTxtField = new TextField();
		quantityTxtField.setWidth("45px");
		
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
		addComponent(quantityTxtField);
		addComponent(scaleLabel);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
}
