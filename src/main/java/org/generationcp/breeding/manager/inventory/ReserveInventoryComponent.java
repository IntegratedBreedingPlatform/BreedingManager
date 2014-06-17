package org.generationcp.breeding.manager.inventory;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ReserveInventoryComponent extends Window implements InitializingBean, 
							InternationalizableComponent, BreedingManagerLayout {
	private static final long serialVersionUID = -5997291617886011653L;

	private VerticalLayout mainLayout;
	private Panel contentPanel;
	private VerticalLayout panelContentLayout;
	private Label singleScaleDescriptionLabel;
	private Label multiScaleDescriptionLabel;
	private Button cancelButton;
	private Button finishButton;
	
	private Boolean isSingleScaled;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	public ReserveInventoryComponent(Boolean isSingleScaled) {
		super();
		this.isSingleScaled = isSingleScaled;
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
		//window formatting
		this.setCaption("Reserve Inventory");
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		
		
		//components formatting
		singleScaleDescriptionLabel = new Label("Specify the amount of inventory to reserve from each selected lot.");
		
		multiScaleDescriptionLabel =  new Label("The lots you have selected are in different units. " +
				"Please specify the amount of inventory to reserve for each unit type.");
		
		contentPanel = new Panel();
		contentPanel.addStyleName("section_panel_layout");
		
		cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
		cancelButton.setWidth("80px");
		
		finishButton = new Button(messageSource.getMessage(Message.FINISH));
		finishButton.setWidth("80px");
		finishButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {
		
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		panelContentLayout = new VerticalLayout();
		panelContentLayout.setMargin(true);
		
		if(isSingleScaled){
			setHeight("255px");
			setWidth("430px");
			
			contentPanel.setWidth("383px");
			contentPanel.setHeight("120px");
			
			panelContentLayout.addComponent(singleScaleDescriptionLabel);
			
		}
		else{
			setHeight("355px");
			setWidth("430px");
			
			contentPanel.setWidth("383px");
			contentPanel.setHeight("220px");
			
			panelContentLayout.addComponent(multiScaleDescriptionLabel);
		}
		
		contentPanel.setLayout(panelContentLayout);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(cancelButton);
		buttonLayout.addComponent(finishButton);
		buttonLayout.setComponentAlignment(cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(finishButton, Alignment.BOTTOM_LEFT);
		
		mainLayout.addComponent(contentPanel);
		mainLayout.addComponent(buttonLayout);
		
		addComponent(mainLayout);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	// SETTERS AND GETTERS
	public Boolean getIsSingleScaled() {
		return isSingleScaled;
	}

	public void setIsSingleScaled(Boolean isSingleScaled) {
		this.isSingleScaled = isSingleScaled;
	}
}
