package org.generationcp.browser.germplasm;

import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmPedigreeTreeContainer extends VerticalLayout implements InitializingBean, InternationalizableComponent{

	private static final long serialVersionUID = 6008211670158416642L;

	private Button viewGraphButton;
	private CheckBox includeDerivativeLinesCheckbox;
	private Button refreshButton;
	private GermplasmPedigreeTreeComponent pedigreeTree;
	
	private Integer gid;
	private GermplasmQueries germplasmQueries;
	private GermplasmDetailsComponentTree parent;
	
	public GermplasmPedigreeTreeContainer(Integer gid, GermplasmQueries germplasmQueries, GermplasmDetailsComponentTree parent){
		this.gid = gid;
		this.germplasmQueries = germplasmQueries;
		this.parent = parent;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initializeComponents();
		addListeners();
		layoutComponents();
	}
	
	private void initializeComponents(){
		viewGraphButton = new Button("View Pedigree Graph");
		
		includeDerivativeLinesCheckbox = new CheckBox();
        includeDerivativeLinesCheckbox.setCaption("Include Derivative Lines");
        
        refreshButton = new Button("Apply");
        refreshButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        
        pedigreeTree = new GermplasmPedigreeTreeComponent(gid, germplasmQueries, new GermplasmIndexContainer(germplasmQueries), null, null, false);
	}

	private void addListeners(){
		refreshButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 5303561767433976952L;

			@Override
			public void buttonClick(ClickEvent event) {
				refreshPedigreeTree();
			}
		});
		
		viewGraphButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 2714058007154924277L;

			@Override
			public void buttonClick(ClickEvent event) {
				parent.showPedigreeGraphWindow();
			}
		});
	}
	
	private void layoutComponents(){
		HorizontalLayout includeDerivativeLinesOptionLayout = new HorizontalLayout();
        includeDerivativeLinesOptionLayout.setMargin(true); 
        includeDerivativeLinesOptionLayout.setSpacing(true);
        includeDerivativeLinesOptionLayout.addComponent(includeDerivativeLinesCheckbox);
        includeDerivativeLinesOptionLayout.addComponent(refreshButton);
        includeDerivativeLinesOptionLayout.addComponent(viewGraphButton);
        addComponent(includeDerivativeLinesOptionLayout);
        
		addComponent(pedigreeTree);
	}
	
	private void refreshPedigreeTree(){
		this.removeComponent(pedigreeTree);
		pedigreeTree = new GermplasmPedigreeTreeComponent(gid, germplasmQueries, new GermplasmIndexContainer(germplasmQueries), null, null, includeDerivativeLinesCheckbox.booleanValue());
		this.addComponent(pedigreeTree);
	}
	
	@Override
	public void updateLabels() {
		
	}
}
