/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package org.generationcp.breeding.manager.listimport;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


/**
 * @author Dennis Billano
 *
 */
@Configurable
public class SelectGermplasmWindow extends Window implements InitializingBean, InternationalizableComponent {

    /**
     * 
     */
    private static final long serialVersionUID = -8113004135173349534L;
    
    public final static String CANCEL_BUTTON_ID = "SelectGermplasmWindow Cancel Button";
    public final static String DONE_BUTTON_ID = "SelectGermplasmWindow Done Button";
    
    private AbsoluteLayout mainLayout;
    private Button cancelButton;
    private Button doneButton;
    private HorizontalLayout buttonArea;
    
    private ListSelect parentList;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    private String germplasmName;
    private List<Germplasm> germplasms;
    private int germplasmCount;
    private Table germplasmTable;
    private int germplasmIndex;
    private Germplasm germplasm;
    private SpecifyGermplasmDetailsComponent source;
    
    private Label selectGermplasmLabel;
    
    public SelectGermplasmWindow() {
        this.parentList = new ListSelect();
    }
    
    public SelectGermplasmWindow(String germplasmName) {
        this.germplasmName = germplasmName;
    }    
    
    public SelectGermplasmWindow(SpecifyGermplasmDetailsComponent source, String germplasmName, int index, Germplasm germplasm) {
        this.germplasmName = germplasmName;
        this.germplasmIndex = index;
        this.germplasm = germplasm;
        this.source = source;
    }
    
    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
        mainLayout = new AbsoluteLayout();
        selectGermplasmLabel = new Label();
        selectGermplasmLabel.setStyleName("bold");
        buttonArea = new HorizontalLayout();
        cancelButton = new Button(); 
        cancelButton.setData(CANCEL_BUTTON_ID);
        doneButton = new Button();
        doneButton.setEnabled(false);
        doneButton.setData(DONE_BUTTON_ID);
        germplasmTable = new Table();
        germplasmTable.addContainerProperty("GID", Integer.class, null);
        germplasmTable.addContainerProperty("Name", String.class, null);
        germplasmTable.setHeight("200px");
        germplasmTable.setWidth("550px");
        germplasmTable.setSelectable(true);
        germplasmTable.setMultiSelect(false);
        germplasmTable.setNullSelectionAllowed(false);
        germplasmTable.setImmediate(true);
        germplasmTable.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;
            public void valueChange(ValueChangeEvent event) {
                if(germplasmTable.getValue()!=null){
                    doneButton.setEnabled(true);
                } else {
                    doneButton.setEnabled(false);
                }
            }
        });
    }
    
    protected void initializeValues() {
        try {
            this.germplasmCount = (int) this.germplasmDataManager.countGermplasmByName(germplasmName, Operation.EQUAL);
            this.germplasms = this.germplasmDataManager.getGermplasmByName(germplasmName, 0, germplasmCount, Operation.EQUAL);
            for (int i=0; i<this.germplasms.size(); i++){
                Germplasm germplasm = germplasms.get(i);
                this.germplasmTable.addItem(new Object[]{germplasm.getGid(), germplasmName}, germplasm.getGid());
            }
        } catch (MiddlewareQueryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
    }

    protected void initializeLayout() {
        // set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("650px");
        setHeight("360px");
        setResizable(false);
        
        setClosable(false);
        
        // center window within the browser
        center();
        
        mainLayout.addComponent(selectGermplasmLabel, "top:40px; left:50px;");
        mainLayout.addComponent(germplasmTable, "top:55px; left:50px;");
        
        buttonArea.setMargin(false, true, false, true);
        buttonArea.setSpacing(true);
        buttonArea.addComponent(doneButton);
        
        mainLayout.addComponent(buttonArea, "top:265px; left:525px;");
        
        this.setContent(mainLayout);
    }
    
    protected void initializeActions() {
        doneButton.addListener(new GermplasmImportButtonClickListener(this));
        doneButton.addListener(new CloseWindowAction());
    }

    public void doneAction(){
        Germplasm selectedGermplasm;
        try {
            selectedGermplasm = this.germplasmDataManager.getGermplasmByGID((Integer) germplasmTable.getValue());
            ((SpecifyGermplasmDetailsComponent) source).receiveGermplasmFromWindowAndUpdateGermplasmData(germplasmIndex, germplasm, selectedGermplasm);
            removeWindow(this);
        } catch (MiddlewareQueryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        assemble();
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        selectGermplasmLabel.setCaption("Multiple Germplasm Records found with the name " + this.germplasmName);
        messageSource.setCaption(this, Message.PLEASE_SELECT_A_GERMPLASM_FROM_THE_TABLE);
        messageSource.setCaption(doneButton, Message.DONE_LABEL);
    }


}
