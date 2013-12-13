package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.germplasm.GermplasmIndexContainer;
import org.generationcp.breeding.manager.listmanager.util.germplasm.GermplasmQueries;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class GermplasmPedigreeComponent extends VerticalLayout implements
		InitializingBean, InternationalizableComponent {

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
	
	public GermplasmPedigreeComponent(Integer germplasmId){
		this.germplasmId = germplasmId;
	}
	
	@Override
	public void updateLabels() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setMargin(true, false, false, false);
		setSpacing(true);
		
		HorizontalLayout pedigreeHorizontalLayout = new HorizontalLayout();
		pedigreeHorizontalLayout.setWidth("400px");
		pedigreeHorizontalLayout.setMargin(false);
		
		HorizontalLayout includeDerivativeLayout = new HorizontalLayout();
		includeDerivativeLayout.setWidth("200px");
		includeDerivativeLayout.setSpacing(true);
        pedigreeDerivativeCheckbox = new CheckBox();
        pedigreeDerivativeCheckbox.setCaption(messageSource.getMessage(Message.INCLUDE_DERIVATIVE_LINES));
        pedigreeDerivativeCheckbox.setData(INCLUDE_DERIVATIVE_LINES);
        includeDerivativeLayout.addComponent(pedigreeDerivativeCheckbox);
        
        applyButton = new Button();
        applyButton.setData(APPLY);
        applyButton.setStyleName(BaseTheme.BUTTON_LINK);
        applyButton.setIcon(new ThemeResource("images/arrow_icon.png"));
        applyButton.addListener(new GermplasmButtonClickListener(this, this.germplasmId));
        includeDerivativeLayout.addComponent(applyButton);
        
        btnViewPedigreeGraph = new Button(messageSource.getMessage(Message.VIEW_PEDIGREE_GRAPH));
        btnViewPedigreeGraph.setData(VIEW_PEDIGREE_GRAPH_ID);
        btnViewPedigreeGraph.setStyleName(BaseTheme.BUTTON_LINK);
        btnViewPedigreeGraph.addStyleName("link_with_eye_icon");
        btnViewPedigreeGraph.addListener(new GermplasmButtonClickListener(this, this.germplasmId));
        
        pedigreeHorizontalLayout.addComponent(includeDerivativeLayout);
        pedigreeHorizontalLayout.addComponent(btnViewPedigreeGraph);
        pedigreeHorizontalLayout.setComponentAlignment(includeDerivativeLayout, Alignment.BOTTOM_LEFT);
        pedigreeHorizontalLayout.setComponentAlignment(btnViewPedigreeGraph, Alignment.BOTTOM_RIGHT);
                
        gQueries = new GermplasmQueries();
        container = new GermplasmIndexContainer(gQueries);
        addComponent(pedigreeHorizontalLayout);
        createTreeComponent();
    }

	private void createTreeComponent() {
		germplasmPedigreeTreeComponent = new GermplasmPedigreeTreeComponent(this.germplasmId, gQueries, 
        		container, this, null, (Boolean) pedigreeDerivativeCheckbox.getValue());
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
