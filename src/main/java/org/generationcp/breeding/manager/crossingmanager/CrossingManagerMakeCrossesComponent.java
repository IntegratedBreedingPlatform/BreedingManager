package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class CrossingManagerMakeCrossesComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent{
    
	public static final String SELECT_FEMALE_PARENT_BUTTON_ID = "Female Parent Button";
	public static final String SELECT_MALE_PARENT_BUTTON_ID = "Male Parent Button";
	public static final String MAKE_CROSS_BUTTON_ID = "Make Cross";
	public static final String NEXT_BUTTON_ID = "next button";
	public static final String BACK_BUTTON_ID = "back button";
	public static final String PARENTS_DELIMITER = ",";
	
    private static final long serialVersionUID = 9097810121003895303L;
    private static final Logger LOG = LoggerFactory.getLogger(CrossingManagerMakeCrossesComponent.class);
    
    private static final Integer CROSS_OPTIONID_ONE = 1;
    private static final Integer CROSS_OPTIONID_TWO = 2;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private CrossingManagerMain source;
    private Accordion accordion;
    private Label lblFemaleParent;
    private Button btnSelectListFemaleParent;
    private Label lblMaleParent;
    private Button btnSelectListMaleParent;
    private OptionGroup optionGroupMakeCrosses;
    private CheckBox chkBoxMakeReciprocalCrosses;
    private Button btnMakeCross;
    private ListSelect listSelectFemale;
    private ListSelect listSelectMale;
    private Table tableCrossesMade;
    private Label lblCrossMade;
    private GridLayout gridLayoutSelectingParents;
    private VerticalLayout layoutCrossOption;
    private Button backButton;
    private Button nextButton;
    private HorizontalLayout layoutButtonArea;
    

    
    public CrossingManagerMakeCrossesComponent(CrossingManagerMain source, Accordion accordion){
    	this.source = source;
        this.accordion = accordion;
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.setMargin(true, true, true, true);

        lblFemaleParent= new Label();       
        
        btnSelectListFemaleParent= new Button();
        btnSelectListFemaleParent.setData(SELECT_FEMALE_PARENT_BUTTON_ID);
        btnSelectListFemaleParent.addListener(new CrossingManagerImportButtonClickListener(this));
        
        listSelectFemale = new ListSelect();
        listSelectFemale.setRows(10);
        listSelectFemale.setWidth("200px");
        listSelectFemale.setNullSelectionAllowed(true);
        listSelectFemale.setMultiSelect(true);
        listSelectFemale.setImmediate(true);
        populateFemaleList();

        optionGroupMakeCrosses = new OptionGroup();
        optionGroupMakeCrosses.setWidth("300px");
        optionGroupMakeCrosses.addStyleName("wrapOptionGroupText");
        optionGroupMakeCrosses.addItem(CROSS_OPTIONID_ONE);
        optionGroupMakeCrosses.setItemCaption(CROSS_OPTIONID_ONE, 
        		messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_ONE_LABEL));
        optionGroupMakeCrosses.addItem(CROSS_OPTIONID_TWO);
        optionGroupMakeCrosses.setItemCaption(CROSS_OPTIONID_TWO, 
        		messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_TWO_LABEL));
        
        chkBoxMakeReciprocalCrosses = new CheckBox();
	
        btnMakeCross= new Button();
        btnMakeCross.setData(MAKE_CROSS_BUTTON_ID);
        btnMakeCross.addListener(new CrossingManagerImportButtonClickListener(this));
        
        lblMaleParent=new Label();
        
        btnSelectListMaleParent= new Button();
        btnSelectListMaleParent.setData(SELECT_MALE_PARENT_BUTTON_ID);
        btnSelectListMaleParent.addListener(new CrossingManagerImportButtonClickListener(this));
        
        listSelectMale = new ListSelect();
        listSelectMale.setRows(10);
        listSelectMale.setWidth("200px");
        listSelectMale.setNullSelectionAllowed(true);
        listSelectMale.setMultiSelect(true);
        listSelectMale.setImmediate(true);
        populateMaleList();
        
        
        lblCrossMade=new Label();
        tableCrossesMade = new Table();
        tableCrossesMade.setSizeFull();
        tableCrossesMade.setMultiSelect(true);
        tableCrossesMade.addContainerProperty(1, String.class, null);
        tableCrossesMade.addContainerProperty(2, String.class, null);
        tableCrossesMade.addContainerProperty(3, String.class, null);
        tableCrossesMade.setColumnHeaders(new String[]{"Cross Name", "Female Parent", "Male Parent"});
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
       
        nextButton = new Button();
        nextButton.setData(NEXT_BUTTON_ID);

        //Widget Layout
        gridLayoutSelectingParents = new GridLayout(4,5);
        gridLayoutSelectingParents.setSpacing(true);
        
        gridLayoutSelectingParents.addComponent(lblFemaleParent,0,0);
        gridLayoutSelectingParents.addComponent(btnSelectListFemaleParent,0,1);
        gridLayoutSelectingParents.addComponent(listSelectFemale,0,2);
        gridLayoutSelectingParents.setComponentAlignment(lblFemaleParent,  Alignment.MIDDLE_CENTER);
        gridLayoutSelectingParents.setComponentAlignment(btnSelectListFemaleParent,  Alignment.MIDDLE_CENTER);
        
        gridLayoutSelectingParents.addComponent(lblMaleParent,2,0);
        gridLayoutSelectingParents.addComponent(btnSelectListMaleParent,2,1);
        gridLayoutSelectingParents.addComponent(listSelectMale,2,2);
        gridLayoutSelectingParents.setComponentAlignment(lblMaleParent,  Alignment.MIDDLE_CENTER);
        gridLayoutSelectingParents.setComponentAlignment(btnSelectListMaleParent,  Alignment.MIDDLE_CENTER);
       
        layoutCrossOption = new VerticalLayout();
        layoutCrossOption.setSpacing(true);
        layoutCrossOption.addComponent(optionGroupMakeCrosses);
        layoutCrossOption.addComponent(chkBoxMakeReciprocalCrosses);
        layoutCrossOption.addComponent(btnMakeCross);
        layoutCrossOption.setComponentAlignment(btnMakeCross,Alignment.MIDDLE_CENTER);
        
        gridLayoutSelectingParents.addComponent(layoutCrossOption,1,2);
        gridLayoutSelectingParents.setComponentAlignment(layoutCrossOption,  Alignment.TOP_LEFT);
        
        addComponent(gridLayoutSelectingParents);
        addComponent(lblCrossMade);
        addComponent(tableCrossesMade);
        
        layoutButtonArea = new HorizontalLayout();
        layoutButtonArea.setSpacing(true);
        layoutButtonArea.setMargin(true);
        layoutButtonArea.addComponent(backButton);
        layoutButtonArea.addComponent(nextButton);
        
        addComponent(layoutButtonArea);
        setComponentAlignment(layoutButtonArea, Alignment.BOTTOM_RIGHT);
        
    }
    
    private void populateMaleList() {
		//mock data to test multiselect
		String[] maleList = new String[] { "Male one", "Male two",
		        "Male three", "Male four", "Male five"};
		for (int i = 0; i < maleList.length; i++) {
			int j = -(i + 1);			
            listSelectMale.addItem(j);
            listSelectMale.setItemCaption(j, maleList[i]);
        }
	
    }
  
    //mock data to test multiselect
    private void populateFemaleList() {
		//mock data to test multiselect
		String[] femaleList = new String[] { "Female one", "Female two",
		        "Female three", "Female four", "Female five"};
		for (int i = 0; i < femaleList.length; i++) {
			int j = i + 1;
            listSelectFemale.addItem(j);
            listSelectFemale.setItemCaption(j, femaleList[i]);
        }
	
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
	messageSource.setCaption(lblFemaleParent, Message.LABEL_FEMALE_PARENT);
	messageSource.setCaption(lblMaleParent, Message.LABEL_MALE_PARENT);
	messageSource.setCaption(btnSelectListFemaleParent, Message.SELECT_LIST_BUTTON_LABEL);
	messageSource.setCaption(btnSelectListMaleParent, Message.SELECT_LIST_BUTTON_LABEL);
	messageSource.setCaption(chkBoxMakeReciprocalCrosses, Message.MAKE_CROSSES_CHECKBOX_LABEL);
	messageSource.setCaption(btnMakeCross, Message.MAKE_CROSSES_BUTTON_LABEL);
	messageSource.setCaption(lblCrossMade, Message.LABEL_CROSS_MADE);
        messageSource.setCaption(backButton, Message.BACK);
        messageSource.setCaption(nextButton, Message.NEXT);
    }
    
    /*
     * Action handler for Make Cross button
     */
    public void makeCrosses(){
    	Object selectedCrossOption = optionGroupMakeCrosses.getValue();
    	
    	List<Integer> femaleList = new ArrayList<Integer>();
    	femaleList.addAll((Collection<Integer>)listSelectFemale.getValue());
    	
    	List<Integer> maleList = new ArrayList<Integer>();
    	maleList.addAll((Collection<Integer>)listSelectMale.getValue());
    	//The selected males seem to be returned in reverse order
    	//so need to reverse list to have them ordered as they appear on screen
    	Collections.reverse(maleList); 
    	
    	
    	if (selectedCrossOption != null && !femaleList.isEmpty() && !maleList.isEmpty()){
    		Integer optionId = (Integer) selectedCrossOption;
    		tableCrossesMade.removeAllItems(); // clear the
    		
    		// Female - Male Multiplication
    		if (CROSS_OPTIONID_ONE.equals(optionId)){
    			multiplyParents(femaleList, maleList, true);
    			if (chkBoxMakeReciprocalCrosses.booleanValue()){
    				multiplyParents(maleList, femaleList, false);
    			}   			
    			
    		// Top to Bottom Crossing	
    		} else if (CROSS_OPTIONID_TWO.equals(optionId)){
    			
    		}
    	}
    }
    
    /**
     * Multiplies each item on first list with each item on second list.
     * The generated crossings are then added to tableCrossesMade Table.
     * 
     * @param parents1 - list of IDs of parents 1
     * @param parents2 - list of IDs of parents 2
     * @param isFemaleFirst - true if parents1 list is the female list
     */
    private void multiplyParents(List<Integer> parents1, List<Integer> parents2, boolean isFemaleFirst){
    	ListSelect select1 = isFemaleFirst ? listSelectFemale : listSelectMale;
    	ListSelect select2 = isFemaleFirst ? listSelectMale : listSelectFemale;
    	
    	for (Integer parent1 : parents1){
			String caption1 = select1.getItemCaption(parent1);
			
			for (Integer parent2 : parents2){
				String caption2 = select2.getItemCaption(parent2);
				tableCrossesMade.addItem(new Object[] {
							caption1 + "/" + caption2, caption1, caption2 
						}, 
						//table entry ID = the GIDs of parents separated by delimiter (eg. 1,2)
						parent1 + PARENTS_DELIMITER + parent2); 
			}
		}
    }

    public CrossingManagerMain getSource() {
    	return source;
    }
}
