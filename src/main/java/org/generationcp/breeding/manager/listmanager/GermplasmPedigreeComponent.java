package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.germplasm.GermplasmIndexContainer;
import org.generationcp.breeding.manager.listmanager.util.germplasm.GermplasmQueries;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class GermplasmPedigreeComponent extends GridLayout implements
		InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	public static final String VIEW_PEDIGREE_GRAPH_ID = "List Manager Pedigree - View Pedigree Graph";
	public static final String INCLUDE_DERIVATIVE_LINES = "List Manager Pedigree - Include Derivative Lines";
	public static final String APPLY = "List Manager Pedigree - Apply";

	private static final long serialVersionUID = 1L;

	@Autowired
    private SimpleResourceBundleMessageSource messageSource;

	private GermplasmPedigreeTreeComponent germplasmPedigreeTreeComponent;
	private GermplasmIndexContainer container;
	private GermplasmQueries gQueries;
	
	private Button btnViewPedigreeGraph;
	private CheckBox pedigreeDerivativeCheckbox;
	private Button applyButton;
	
	private Integer germplasmId;
	private VerticalLayout pedigreeTreeLayout;
	
	public GermplasmPedigreeComponent(Integer germplasmId){
		this.germplasmId = germplasmId;
	}
	
	@Override
	public void updateLabels() {
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
		pedigreeDerivativeCheckbox = new CheckBox();
        pedigreeDerivativeCheckbox.setCaption(messageSource.getMessage(Message.INCLUDE_DERIVATIVE_LINES));
        pedigreeDerivativeCheckbox.setData(INCLUDE_DERIVATIVE_LINES);
		
        applyButton = new Button();
        applyButton.setData(APPLY);
        applyButton.setStyleName(BaseTheme.BUTTON_LINK);
        applyButton.setIcon(AppConstants.Icons.ICON_ARROW);
        
        btnViewPedigreeGraph = new Button(messageSource.getMessage(Message.VIEW_PEDIGREE_GRAPH));
        btnViewPedigreeGraph.setData(VIEW_PEDIGREE_GRAPH_ID);
        btnViewPedigreeGraph.setStyleName(BaseTheme.BUTTON_LINK);
        btnViewPedigreeGraph.addStyleName("link_with_eye_icon");
        
        gQueries = new GermplasmQueries();
        container = new GermplasmIndexContainer(gQueries);
        
        pedigreeTreeLayout = new VerticalLayout();
        pedigreeTreeLayout.setSizeFull();
        germplasmPedigreeTreeComponent = new GermplasmPedigreeTreeComponent(this.germplasmId, gQueries, 
        		container, pedigreeTreeLayout, null, (Boolean) pedigreeDerivativeCheckbox.getValue());
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		applyButton.addListener(new GermplasmButtonClickListener(this, this.germplasmId));
		btnViewPedigreeGraph.addListener(new GermplasmButtonClickListener(this, this.germplasmId));
	}

	@Override
	public void layoutComponents() {
		addStyleName("overflow_x_auto");
		
		setRows(2);
        setColumns(3);
        setMargin(true, false, false, false);
		setSpacing(true);
		
		HorizontalLayout includeDerivativeLayout = new HorizontalLayout();
		includeDerivativeLayout.setWidth("200px");
		includeDerivativeLayout.setSpacing(true);
        includeDerivativeLayout.addComponent(pedigreeDerivativeCheckbox);
        includeDerivativeLayout.addComponent(applyButton);
        
        addComponent(includeDerivativeLayout, 0, 0);
        addComponent(btnViewPedigreeGraph, 2, 0);
        addComponent(germplasmPedigreeTreeComponent, 0, 1, 2, 1);
	}
	
	private void createTreeComponent() {
		germplasmPedigreeTreeComponent = new GermplasmPedigreeTreeComponent(this.germplasmId, gQueries, 
        		container, this.pedigreeTreeLayout, null, (Boolean) pedigreeDerivativeCheckbox.getValue());
		 addComponent(germplasmPedigreeTreeComponent);
	}
	
    public void refreshPedigreeTree() {
        if(germplasmPedigreeTreeComponent != null){
            removeComponent(germplasmPedigreeTreeComponent);
        }
        createTreeComponent();
    }
	
    public GermplasmQueries getGermplasmQueries(){
    	return this.gQueries;
    }


}
