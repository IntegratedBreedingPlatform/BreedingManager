package org.generationcp.browser.cross.study.traitdonors.main;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.commons.EnvironmentFilter;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.traitdonors.main.SelectTraitsSection.TraitItem;
import org.generationcp.browser.cross.study.traitdonors.main.listeners.TraitDonorButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractSelect.MultiSelectMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;

// FIXME : Rebecca align the screen please
@Configurable
public class PreselectTraitFilter extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{

	// FIXME Rebecca decent serial
	private static final long serialVersionUID = 1L;
	
	public static final String NEXT_BUTTON_ID = "PreselectTraitFilter Next Button ID";

	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private TraitDonorsQueryMain mainScreen;
	private EnvironmentFilter nextScreen;
	
	private Button nextButton;
	
	private Label lblSectionTitle;
	private TreeTable traitTreeTable;
	private Table traitSelectTable;
	
	public PreselectTraitFilter(TraitDonorsQueryMain mainScreen, EnvironmentFilter nextScreen) {
		this.mainScreen = mainScreen;
		this.nextScreen = nextScreen;
	}

	@Override
	public void updateLabels() {
//		if (nextButton != null){
//			messageSource.setCaption(nextButton, Message.NEXT);
//		}		
	}

	@Override
	public void afterPropertiesSet() throws Exception {

    	setHeight("450px");
    	setWidth("980px");
    	
    	//initializeComponents();
    	//populateTable();		
	}
	
	private void initializeComponents() {
		lblSectionTitle = new Label(messageSource.getMessage(Message.GET_NUMERIC_VARIATES));
		
		traitTreeTable = new TreeTable();
		traitTreeTable.setImmediate(true);
		traitTreeTable.setHeight("360px");
		traitTreeTable.setWidth("440px");

		
		traitTreeTable.addContainerProperty("Trait", String.class, "");
		
		Object root = traitTreeTable.addItem(new Object[] { "All Traits"}, null);
		Object agronomic = traitTreeTable.addItem(new Object[] {"Agronomic"}, null);
		Object metronomic = traitTreeTable.addItem(new Object[] {"Metronomic"}, null);
		Object vegenomic = traitTreeTable.addItem(new Object[] {"Vegenomic"}, null);
		Object plantHeight = traitTreeTable.addItem(new Object[] {"Plant Height"}, null);
		Object plantFat = traitTreeTable.addItem(new Object[] {"Plant Fat"}, null);
		Object plantSway = traitTreeTable.addItem(new Object[] {"Plant Sway"}, null);
		Object plantSong = traitTreeTable.addItem(new Object[] {"Plant Song"}, null);
		Object plantGreen = traitTreeTable.addItem(new Object[] {"Plant Green"}, null);
		Object plantLeaves = traitTreeTable.addItem(new Object[] {"Plant Leaves"}, null);
		
		traitTreeTable.setParent(agronomic, root);
		traitTreeTable.setParent(metronomic, root);
		traitTreeTable.setParent(vegenomic, root);
		traitTreeTable.setParent(plantHeight, agronomic);
		traitTreeTable.setParent(plantFat, agronomic);
		traitTreeTable.setParent(plantSway, metronomic);
		traitTreeTable.setParent(plantSong, metronomic);
		traitTreeTable.setParent(plantGreen, vegenomic);
		traitTreeTable.setParent(plantLeaves, vegenomic);
		
		traitTreeTable.setCollapsed(root, false);
		traitTreeTable.setCollapsed(agronomic, true);
		traitTreeTable.setCollapsed(metronomic, true);
		traitTreeTable.setCollapsed(vegenomic, true);
		
		traitTreeTable.setChildrenAllowed(plantHeight, false);
		traitTreeTable.setChildrenAllowed(plantFat, false);
		traitTreeTable.setChildrenAllowed(plantSway, false);
		traitTreeTable.setChildrenAllowed(plantSong, false);
		traitTreeTable.setChildrenAllowed(plantGreen, false);
		traitTreeTable.setChildrenAllowed(plantLeaves, false);
		
		traitTreeTable.setSelectable(true);
		traitTreeTable.setMultiSelect(true);
		traitTreeTable.setMultiSelectMode(MultiSelectMode.DEFAULT);
		
		traitSelectTable = new Table();
		traitSelectTable.setImmediate(true);
		traitSelectTable.setHeight("360px");
		traitSelectTable.setWidth("440px");
		
		final BeanItemContainer<TraitItem> tableContainer = new BeanItemContainer<SelectTraitsSection.TraitItem>(TraitItem.class);
		traitSelectTable.setContainerDataSource(tableContainer);
		traitSelectTable.setVisibleColumns(new Object[] { "traitName", "stdVarName" });
		
		addComponent(lblSectionTitle, "top:10px;left:20px");
		addComponent(traitTreeTable, "top:50px;left:20px");
		addComponent(traitSelectTable, "top:50px;left:500px");

	}
	
	public void populateTraitsTables(List<EnvironmentForComparison> environments) {
//		this.environmentsForComparisonList = environments;
//		this.environmentIds = new ArrayList<Integer>();
//		for (EnvironmentForComparison envt : environments){
//			this.environmentIds.add(envt.getEnvironmentNumber());
//		}
//		
//		createTraitsTabs();
		initializeComponents();
		createButtonLayout();
	}
	
	private void createButtonLayout(){
		nextButton = new Button(messageSource.getMessage(Message.NEXT));
		nextButton.setWidth("80px");
		nextButton.setData(NEXT_BUTTON_ID);
		nextButton.addListener(new TraitDonorButtonClickListener(this));
		nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		addComponent(nextButton, "top:420px;left:900px");
		updateLabels();
	}
	
	public void nextButtonClickAction(){
		
		this.mainScreen.selectSecondTab();
		this.nextScreen.populateEnvironmentsTable();
		//this.nextScreen.populateEnvironmentsTable(traitForComparisonsListTemp, traitEnvMapTemp, trialEnvMapTemp, germplasmIds, germplasmPairsTemp, germplasmIdNameMap);
		
	}
	
	

}
