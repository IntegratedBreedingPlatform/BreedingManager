package org.generationcp.browser.cross.study.adapted.main;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;

@Configurable
public class SpecifyAndWeighEnvironments extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {
	
	private QueryForAdaptedGermplasmMain mainScreen;
	private SetUpTraitFilter nextScreen;
	private ResultsComponent resultsScreen;
	
	private Label headerLabel;
	
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public SpecifyAndWeighEnvironments(QueryForAdaptedGermplasmMain mainScreen, SetUpTraitFilter nextScreen
			, ResultsComponent resultScreen) {
		 this.mainScreen = mainScreen;
		 this.nextScreen = nextScreen;
		 this.resultsScreen = resultScreen;
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	   setHeight("550px");
       setWidth("1000px");	
	}

}
