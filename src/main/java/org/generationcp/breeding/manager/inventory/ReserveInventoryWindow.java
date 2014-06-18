package org.generationcp.breeding.manager.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
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
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ReserveInventoryWindow extends Window implements InitializingBean, 
							InternationalizableComponent, BreedingManagerLayout {
	private static final long serialVersionUID = -5997291617886011653L;

	private VerticalLayout mainLayout;
	private Panel contentPanel;
	private VerticalLayout panelContentLayout;
	private Label singleScaleDescriptionLabel;
	private Label multiScaleDescriptionLabel;
	private Button cancelButton;
	private Button finishButton;
	
	private List<ReserveInventoryRowComponent> scaleRows;
	
	private Boolean isSingleScaled;
	
	private ReserveInventorySource source;
	
	//Inputs
	private Map<String, List<ListEntryLotDetails>> scaleGrouping;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	private ReserveInventoryAction reserveInventoryAction;
	
	public ReserveInventoryWindow(ReserveInventorySource source, Map<String, List<ListEntryLotDetails>> scaleGrouping, Boolean isSingleScaled) {
		super();
		this.source = source;
		this.isSingleScaled = isSingleScaled;
		this.scaleGrouping = scaleGrouping;
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
		this.setCaption(messageSource.getMessage(Message.RESERVE_INVENTORY));
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setModal(true);
		
		//components formatting
		singleScaleDescriptionLabel = new Label("Specify the amount of inventory to reserve from each selected lot.");
		
		multiScaleDescriptionLabel =  new Label("The lots you have selected are in different units. " +
				"Please specify the amount of inventory to reserve for each unit type.");
		
		contentPanel = new Panel();
		contentPanel.addStyleName("section_panel_layout");
		
		
		scaleRows = new ArrayList<ReserveInventoryRowComponent>();
		
		cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
		cancelButton.setWidth("80px");
		
		finishButton = new Button(messageSource.getMessage(Message.FINISH));
		finishButton.setWidth("80px");
		finishButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		initializeScaleRows();
	}

	private void initializeScaleRows() {
	
		for (Map.Entry<String, List<ListEntryLotDetails>> entry : scaleGrouping.entrySet()) {
		    String scale = entry.getKey();
		    List<ListEntryLotDetails> lotDetailList = entry.getValue();
		    scaleRows.add(new ReserveInventoryRowComponent(scale,lotDetailList.size()));
		}
	}

	@Override
	public void addListeners() {
		cancelButton.addListener(new CloseWindowAction());
		 
		reserveInventoryAction = new ReserveInventoryAction(source);
		
		finishButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				reserveInventoryAction.validateReservations(getReservations());
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {
		
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		panelContentLayout = new VerticalLayout();
		panelContentLayout.setMargin(true);
		panelContentLayout.setSpacing(true);
		
		if(isSingleScaled){
			setHeight("255px");
			setWidth("430px");
			
			contentPanel.setWidth("383px");
			contentPanel.setHeight("120px");
			
			panelContentLayout.addComponent(singleScaleDescriptionLabel);
			panelContentLayout.addComponent(scaleRows.get(0));
			
		}
		else{
			setHeight("325px");
			setWidth("430px");
			
			contentPanel.setWidth("383px");
			contentPanel.setHeight("190px");
			
			panelContentLayout.addComponent(multiScaleDescriptionLabel);
			
			VerticalLayout scaleLayout = new VerticalLayout();
			scaleLayout.setSpacing(true);
			scaleLayout.setHeight("90px");
			
			if(scaleRows.size() > 3){
				scaleLayout.addStyleName(AppConstants.CssStyles.SCALE_ROW);
			}
			
			for(ReserveInventoryRowComponent row : scaleRows){
				scaleLayout.addComponent(row);
			}
			
			panelContentLayout.addComponent(scaleLayout);
			
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

	protected Map<Double, List<ListEntryLotDetails>> getReservations() {
		Map<Double, List<ListEntryLotDetails>> reservations = new HashMap<Double,List<ListEntryLotDetails>>();
		
		for(ReserveInventoryRowComponent row : scaleRows){
			reservations.put(row.getReservationAmount(), scaleGrouping.get(row.getScale()));
		}
		
		return reservations;
	}

	// SETTERS AND GETTERS
	public Boolean getIsSingleScaled() {
		return isSingleScaled;
	}

	public void setIsSingleScaled(Boolean isSingleScaled) {
		this.isSingleScaled = isSingleScaled;
	}

	public List<ReserveInventoryRowComponent> getScaleRows() {
		return scaleRows;
	}

	public void setScaleRows(List<ReserveInventoryRowComponent> scaleRows) {
		this.scaleRows = scaleRows;
	}
	
}
