package org.generationcp.browser.cross.study.adapted.main;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;

@Configurable
public class SetUpTraitFilter extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

	public SetUpTraitFilter(
			QueryForAdaptedGermplasmMain queryForAdaptedGermplasmMain,
			ResultsComponent screenThree) {
		// TODO Auto-generated constructor stub
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
