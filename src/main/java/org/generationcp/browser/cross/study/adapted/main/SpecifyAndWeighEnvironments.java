package org.generationcp.browser.cross.study.adapted.main;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@Configurable
public class SpecifyAndWeighEnvironments extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {
	
	private QueryForAdaptedGermplasmMain mainScreen;
	private SetUpTraitFilter nextScreen;
	private ResultsComponent resultsScreen;
	
	private Label headerLabel;
	private Label headerValLabel;
	private Label chooseEnvLabel;
	private Label noOfEnvLabel;
	private Label noOfEnvValLabel;
	
	private Button filterByLocationBtn;
	private Button filterByStudyBtn;
	private Button addEnvConditionsBtn;
	private Button nextBtn;
	
	private Table entriesTable;
	
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
       
       headerLabel = new Label(messageSource.getMessage(Message.ENVIRONMENT_FILTER));
       headerLabel.setImmediate(true);
       addComponent(headerLabel, "top:20px;left:20px");
       
       headerValLabel = new Label(messageSource.getMessage(Message.ENVIRONMENT_FILTER_VAL));
       headerValLabel.setStyleName("gcp-bold-italic");
       headerValLabel.setContentMode(Label.CONTENT_XHTML);
       headerValLabel.setImmediate(true);
       addComponent(headerValLabel, "top:20px;left:150px");
       
       
       filterByLocationBtn = new Button(messageSource.getMessage(Message.FILTER_BY_LOCATION));
       filterByLocationBtn.setWidth("200px");
       addComponent(filterByLocationBtn, "top:50px;left:20px");
       
       filterByStudyBtn = new Button(messageSource.getMessage(Message.FILTER_BY_STUDY));
       filterByStudyBtn.setWidth("200px");
       addComponent(filterByStudyBtn, "top:50px;left:240px");
       
       addEnvConditionsBtn = new Button(messageSource.getMessage(Message.ADD_ENV_CONDITION));
       addEnvConditionsBtn.setWidth("400px");
       addComponent(addEnvConditionsBtn, "top:50px;left:580px");
       
       
       chooseEnvLabel = new Label(messageSource.getMessage(Message.CHOOSE_ENVIRONMENTS));
       chooseEnvLabel.setImmediate(true);
       addComponent(chooseEnvLabel, "top:90px;left:20px");
       
       entriesTable = new Table();
       entriesTable.setWidth("960px");
       entriesTable.setHeight("370px");
       entriesTable.setImmediate(true);
       entriesTable.setPageLength(-1);
       addComponent(entriesTable, "top:110px;left:20px");
       
       noOfEnvLabel = new Label(messageSource.getMessage(Message.NO_OF_SELECTED_ENVIRONMENT));
       noOfEnvLabel.setImmediate(true);
       addComponent(noOfEnvLabel, "top:500px;left:20px");
       
       noOfEnvValLabel = new Label("0");
       noOfEnvValLabel.setImmediate(true);
       addComponent(noOfEnvValLabel, "top:500px;left:230px");
       
       nextBtn = new Button("Next");
       nextBtn.setWidth("100px");
       nextBtn.setEnabled(false);
       addComponent(nextBtn, "top:490px;left:880px");
	}

}
