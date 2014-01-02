package org.generationcp.browser.cross.study.adapted.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.impl.xb.xsdschema.impl.NumFacetImpl;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.adapted.main.listeners.AdaptedGermplasmButtonClickListener;
import org.generationcp.browser.cross.study.adapted.main.pojos.CategoricalTraitFilter;
import org.generationcp.browser.cross.study.adapted.main.pojos.CharacterTraitFilter;
import org.generationcp.browser.cross.study.adapted.main.pojos.NumericTraitFilter;
import org.generationcp.browser.cross.study.commons.trait.filter.CategoricalVariatesSection;
import org.generationcp.browser.cross.study.commons.trait.filter.CharacterTraitsSection;
import org.generationcp.browser.cross.study.commons.trait.filter.NumericTraitsSection;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class SetUpTraitFilter extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {
	
	public static final String NEXT_BUTTON_ID = "SetUpTraitFilter Next Button ID";
	   
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(SetUpTraitFilter.class);

	private static final int NUM_OF_SECTIONS = 3;
	private static final Message[] tabLabels = {Message.NUMERIC_TRAITS, Message.CHARACTER_TRAIT_FILTER_TAB_TITLE, Message.CATEGORICAL_VARIATES};

	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private QueryForAdaptedGermplasmMain mainScreen;
	private DisplayResults nextScreen;
	private CharacterTraitsSection characterSection;
	private NumericTraitsSection numericSection;
	private CategoricalVariatesSection categoricalVariatesSection;

	private TabSheet mainTabSheet;
	private Button nextButton;
	
	private List<EnvironmentForComparison> environmentsForComparisonList;
	private List<Integer> environmentIds;
	
	
	public SetUpTraitFilter(
			QueryForAdaptedGermplasmMain queryForAdaptedGermplasmMain,
			DisplayResults screenThree) {
		this.mainScreen = queryForAdaptedGermplasmMain;
		this.nextScreen = screenThree;
	}

	@Override
	public void updateLabels() {
		if (nextButton != null){
			messageSource.setCaption(nextButton, Message.NEXT);
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		setHeight("550px");
        setWidth("1000px");	 
	}
	
	public void createTraitsTabs() {		
		mainTabSheet = new TabSheet();
		mainTabSheet.setHeight("470px");
		
		
        for (int i = 0; i < NUM_OF_SECTIONS; i++){
        	VerticalLayout layout = new VerticalLayout();
        	
        	switch (i) {
				case 0:
					numericSection = new NumericTraitsSection(this.environmentIds, this.getWindow());
					numericSection.showEmptyTraitsMessage();
					layout = numericSection;
					break;
				
				case 1:
					characterSection = new CharacterTraitsSection(this.environmentIds, this.getWindow());
					layout = characterSection;
					break;
					
				case 2:
					categoricalVariatesSection = new CategoricalVariatesSection(this.environmentIds, this.getWindow());
					layout = categoricalVariatesSection;
					break;					
					
			}

        	
        	mainTabSheet.addTab(layout, messageSource.getMessage(tabLabels[i]));
        }
        
        mainTabSheet.addListener(new SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                Component selected = mainTabSheet.getSelectedTab();
                Tab tab = mainTabSheet.getTab(selected);

                if(tab!=null && tab.getCaption().equals(
                		messageSource.getMessage(tabLabels[0]))){
                	numericSection.showEmptyTraitsMessage();
                } else if(tab!=null && tab.getCaption().equals(
                		messageSource.getMessage(tabLabels[1]))){
                	characterSection.showEmptyTraitsMessage();
                } else if(tab!=null && tab.getCaption().equals(
                		messageSource.getMessage(tabLabels[2]))){
                	categoricalVariatesSection.showEmptyTraitsMessage();
                }
            }
        });
        
        addComponent(mainTabSheet, "top:20px");
	}

	public void populateTraitsTables(List<EnvironmentForComparison> environments) {
		this.environmentsForComparisonList = environments;
		this.environmentIds = new ArrayList<Integer>();
		for (EnvironmentForComparison envt : environments){
			this.environmentIds.add(envt.getEnvironmentNumber());
		}
		
		createTraitsTabs();	
		createButtonLayout();
	}
	
	
	private void createButtonLayout(){
		nextButton = new Button(messageSource.getMessage(Message.NEXT));
		nextButton.setWidth("80px");
		nextButton.setData(NEXT_BUTTON_ID);
		nextButton.addListener(new AdaptedGermplasmButtonClickListener(this));
		nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		addComponent(nextButton, "top:500px;left:900px");
		updateLabels();
	}
	
	// validate conditions before proceeding to next tab
	public void nextButtonClickAction(){
		if (numericSection != null){
			if (!numericSection.allFieldsValid()){
				return;
			}
			
			if (!categoricalVariatesSection.allFieldsValid()){
				return;
			}
		}
		
		List<NumericTraitFilter> numericFilters = numericSection.getFilters();
		List<CharacterTraitFilter> characterFilters = characterSection.getFilters();
		List<CategoricalTraitFilter> categoricalFilters = categoricalVariatesSection.getFilters();
		
		// Do not allow user to proceed if all traits dropped
		if (numericSection.allTraitsDropped() && characterSection.allTraitsDropped() 
				&& categoricalVariatesSection.allTraitsDropped()){
			MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.WARNING), 
					messageSource.getMessage(Message.ALL_TRAITS_DROPPED_WARNING));
			
		} else {
			this.mainScreen.selectThirdTab();
			this.nextScreen.populateResultsTable(environmentsForComparisonList,numericFilters,
					characterFilters,categoricalFilters);
		}
	}
	

	
	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}
	
	


}
