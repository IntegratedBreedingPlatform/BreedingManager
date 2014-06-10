package org.generationcp.breeding.manager.crossingmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class CrossingMethodComponent extends VerticalLayout implements BreedingManagerLayout,InitializingBean, 
								InternationalizableComponent, ListTreeActionsListener {

	private static final long serialVersionUID = -8847158352169444182L;
	
	public static final String MAKE_CROSS_BUTTON_ID = "Make Cross Button";
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
    private Panel crossingMethodPanel;
    private Label crossingMethodLabel;
    
    private ComboBox crossingMethodComboBox;
    private CheckBox chkBoxMakeReciprocalCrosses;
    private Button btnMakeCross;

    private CrossingManagerMakeCrossesComponent makeCrossesMain;
    private MakeCrossesParentsComponent parentsComponent;
    
	public CrossingMethodComponent(
			CrossingManagerMakeCrossesComponent makeCrossesMain) {
		super();
		this.makeCrossesMain = makeCrossesMain;
	}

	@Override
	public void updateUIForRenamedList(GermplasmList list, String newName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openListDetails(GermplasmList list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void folderClicked(GermplasmList list) {
		// TODO Auto-generated method stub
		
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
        crossingMethodLabel = new Label(messageSource.getMessage(Message.CROSSING_METHOD));
        crossingMethodLabel.setWidth("200px");
        crossingMethodLabel.setStyleName(Bootstrap.Typography.H4.styleName());
        crossingMethodLabel.addStyleName(AppConstants.CssStyles.BOLD);
        
        crossingMethodComboBox = new ComboBox();
        crossingMethodComboBox.setNewItemsAllowed(false);
        crossingMethodComboBox.setNullSelectionAllowed(false);
        crossingMethodComboBox.setWidth("400px");
        
        chkBoxMakeReciprocalCrosses = new CheckBox(messageSource.getMessage(Message.MAKE_CROSSES_CHECKBOX_LABEL));
    
        btnMakeCross= new Button(messageSource.getMessage(Message.MAKE_CROSSES_BUTTON_LABEL));
        btnMakeCross.setData(MAKE_CROSS_BUTTON_ID);
        btnMakeCross.addStyleName(Bootstrap.Buttons.INFO.styleName());
	}

	@Override
	public void initializeValues() {
		crossingMethodComboBox.addItem(CrossType.MULTIPLY);
        crossingMethodComboBox.setItemCaption(CrossType.MULTIPLY, messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_ONE_LABEL));
        crossingMethodComboBox.addItem(CrossType.TOP_TO_BOTTOM);
        crossingMethodComboBox.setItemCaption(CrossType.TOP_TO_BOTTOM, messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_TWO_LABEL));
        crossingMethodComboBox.select(CrossType.MULTIPLY);
	}

	@Override
	public void addListeners() {
		btnMakeCross.addListener(new CrossingManagerImportButtonClickListener(this));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {
		setSpacing(true);
		
        VerticalLayout layoutCrossOption = new VerticalLayout();
        layoutCrossOption.setWidth("460px");
        layoutCrossOption.setSpacing(true);
        layoutCrossOption.setMargin(true);
        layoutCrossOption.addComponent(crossingMethodComboBox);
        layoutCrossOption.addComponent(chkBoxMakeReciprocalCrosses);
        layoutCrossOption.addComponent(btnMakeCross);
        
        crossingMethodPanel = new Panel();
        crossingMethodPanel.setWidth("460px");
        crossingMethodPanel.setLayout(layoutCrossOption);
        crossingMethodPanel.addStyleName("section_panel_layout");
        
        //provides this slot for an icon 
        HeaderLabelLayout crossingMethodLayout = new HeaderLabelLayout(null,crossingMethodLabel);
        addComponent(crossingMethodLayout);
        addComponent(crossingMethodPanel);
	}
	
    public void makeCrossButtonAction(){
    	parentsComponent = makeCrossesMain.getParentsComponent();
    	
    	Table femaleParents = parentsComponent.getFemaleTable();
    	Table maleParents = parentsComponent.getMaleTable();
    	
    	List<GermplasmListEntry> femaleList = parentsComponent.getCorrectSortedValue(femaleParents);
    	List<GermplasmListEntry> maleList = parentsComponent.getCorrectSortedValue(maleParents);
      
    	CrossType type = (CrossType) crossingMethodComboBox.getValue();
    	
    	parentsComponent.updateFemaleListNameForCrosses();
    	parentsComponent.updateMaleListNameForCrosses();
    	
    	makeCrossesMain.makeCrossButtonAction(femaleList, maleList, 
    			parentsComponent.getFemaleListNameForCrosses(), parentsComponent.getMaleListNameForCrosses(), type, chkBoxMakeReciprocalCrosses.booleanValue());
    }

}