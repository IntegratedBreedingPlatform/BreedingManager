package org.generationcp.browser.cross.study.adapted.main;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.commons.trait.filter.CharacterTraitsSection;
import org.generationcp.browser.cross.study.commons.trait.filter.NumericTraitsSection;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class SetUpTraitFilter extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {
	   
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(SetUpTraitFilter.class);

	private static final int NUM_OF_SECTIONS = 3;
	private static final Message[] tabLabels = {Message.NUMERIC_TRAITS, Message.CHARACTER_TRAIT_FILTER_TAB_TITLE, Message.THIRD_SECTION};

	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private VerticalLayout[] tabLayouts = new VerticalLayout[NUM_OF_SECTIONS];

	private TabSheet mainTabSheet;
	
	private List<EnvironmentForComparison> environmentsForComparisonList;
	private List<Integer> environmentIds;
	
	
	public SetUpTraitFilter(
			QueryForAdaptedGermplasmMain queryForAdaptedGermplasmMain,
			ResultsComponent screenThree) {
	}

	@Override
	public void updateLabels() {
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		setHeight("550px");
        setWidth("1000px");	 
	}
	
	public void createTraitsTabs() {		
		mainTabSheet = new TabSheet();
		
        for (int i = 0; i < NUM_OF_SECTIONS; i++){
        	VerticalLayout layout = new VerticalLayout();
        	
        	switch (i) {
				case 0:
					layout = new NumericTraitsSection(this.environmentIds, this.getWindow());
					break;
				
				case 1:
					layout = new CharacterTraitsSection(this.environmentIds, this.getWindow());
					break;
					
			}

        	
        	mainTabSheet.addTab(layout, messageSource.getMessage(tabLabels[i]));
        	tabLayouts[i] = layout;
        }
        
        addComponent(mainTabSheet, "top:20px");
	}

	public void populateTraitsTables(List<EnvironmentForComparison> environments) {
		this.environmentsForComparisonList = environments;
		this.environmentIds = new ArrayList<Integer>();
		for (EnvironmentForComparison envt : environments){
			this.environmentIds.add(envt.getEnvironmentNumber());
		}
		
//		this.fieldsToValidate = new ArrayList<Field>();
		
		createTraitsTabs();	
	}
	
	
	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}
	

}
