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
import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.breeding.manager.listimport.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;


/**
 * @author Dennis Billano
 *
 */
@Configurable
public class SelectGermplasmWindow extends Window implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -8113004135173349534L;
    
    public final static String CANCEL_BUTTON_ID = "SelectGermplasmWindow Cancel Button";
    public final static String DONE_BUTTON_ID = "SelectGermplasmWindow Done Button";
    
    private AbsoluteLayout mainLayout;
    private Button cancelButton;
    private Button doneButton;
    private HorizontalLayout buttonArea;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private LocationDataManager locationDataManager;
    
    private String germplasmName;
    private List<Germplasm> germplasms;
    private int germplasmCount;
    private Table germplasmTable;
    private int germplasmIndex;
    private Germplasm germplasm;
    private ProcessImportedGermplasmAction source;
    private Boolean viaToolURL;
    
    private Label selectGermplasmLabel;
    
    public SelectGermplasmWindow(ProcessImportedGermplasmAction source, String germplasmName, int index, Germplasm germplasm, Boolean viaToolURL) {
        this.germplasmName = germplasmName;
        this.germplasmIndex = index;
        this.germplasm = germplasm;
        this.source = source;
        this.viaToolURL = viaToolURL;
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
        germplasmTable.addContainerProperty("GID", Button.class, null);
        germplasmTable.addContainerProperty("Name", String.class, null);
        germplasmTable.addContainerProperty("Location", String.class, null);
        germplasmTable.addContainerProperty("Breeding Method", String.class, null);
        germplasmTable.addContainerProperty("Pedigree", String.class, null);
        germplasmTable.setHeight("200px");
        germplasmTable.setWidth("750px");
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
                Location location = locationDataManager.getLocationByID(germplasm.getLocationId());
                Method method = germplasmDataManager.getMethodByID(germplasm.getMethodId());

            	Button gidButton = new Button(String.format("%s", germplasm.getGid().toString()), new GidLinkClickListener(germplasm.getGid().toString(), viaToolURL));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK);                
                
                String crossExpansion = "";
                if(germplasm!=null){
                	try {
                		if(germplasmDataManager!=null)
                			crossExpansion = germplasmDataManager.getCrossExpansion(germplasm.getGid(), 1);
                	} catch(MiddlewareQueryException ex){
                        crossExpansion = "-";
                    }
            	}
                
                String locationName = "";
                if(location!=null && location.getLname()!=null)
                	locationName = location.getLname();

                String methodName = "";
                if(method!=null && method.getMname()!=null)
                	methodName = method.getMname();
                
                this.germplasmTable.addItem(new Object[]{gidButton, getShortenedGermplasmNames(germplasm.getGid()), locationName, methodName, crossExpansion}, germplasm.getGid());
            }
            
            germplasmTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
    			private static final long serialVersionUID = 1L;

    			public String generateDescription(Component source, Object itemId,
    					Object propertyId) {
    				if(propertyId=="Name"){
    					Item item = germplasmTable.getItem(itemId);
    					Integer gid = Integer.valueOf(((Button) item.getItemProperty("GID").getValue()).getCaption());
    					return getGermplasmNames(gid);
    				} else {
    					return null;
    				}
    			}
            });
            
        } catch (MiddlewareQueryException e) {
            //TODO add proper logging
        	e.printStackTrace();
        }        
    }

    protected void initializeLayout() {
        // set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("850px");
        setHeight("360px");
        setResizable(false);
        
        setClosable(false);
        
        // center window within the browser
        center();
        
        mainLayout.addComponent(selectGermplasmLabel, "top:40px; left:50px;");
        mainLayout.addComponent(germplasmTable, "top:55px; left:50px;");
        
        buttonArea.setMargin(false, true, false, true);
        buttonArea.setSpacing(true);
        buttonArea.addComponent(cancelButton);
        buttonArea.addComponent(doneButton);
        
        mainLayout.addComponent(buttonArea, "top:265px; left:505px;");
        
        this.setContent(mainLayout);
    }
    
    protected void initializeActions() {
        doneButton.addListener(new GermplasmImportButtonClickListener(this));
        doneButton.addListener(new CloseWindowAction(this));
        cancelButton.addListener(new CloseWindowAction(this));
    }

    public void doneAction(){
        Germplasm selectedGermplasm;
        try {
            selectedGermplasm = this.germplasmDataManager.getGermplasmByGID((Integer) germplasmTable.getValue());
            source.receiveGermplasmFromWindowAndUpdateGermplasmData(germplasmIndex, germplasm, selectedGermplasm);
            removeWindow(this);
        } catch (MiddlewareQueryException e) {
            // TODO Add proper logging
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
        messageSource.setCaption(doneButton, Message.SELECT_HIGHLIGHTED_GERMPLASM);
        messageSource.setCaption(cancelButton, Message.CANCEL);
    }

    private String getGermplasmNames(int gid) throws InternationalizableException {

        try {
            List<Name> names = germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
            StringBuffer germplasmNames = new StringBuffer("");
            int i = 0;
            for (Name n : names) {
                if (i < names.size() - 1) {
                    germplasmNames.append(n.getNval() + ", ");
                } else {
                    germplasmNames.append(n.getNval());
                }
                i++;
            }

            return germplasmNames.toString();
        } catch (MiddlewareQueryException e) {
            return null;
        }
    }

    
    private String getShortenedGermplasmNames(int gid) throws InternationalizableException {
        try {
            List<Name> names = germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
            StringBuffer germplasmNames = new StringBuffer("");
            int i = 0;
            for (Name n : names) {
                if (i < names.size() - 1) {
                    germplasmNames.append(n.getNval() + ", ");
                } else {
                    germplasmNames.append(n.getNval());
                }
                i++;
            }
            String n = germplasmNames.toString();
            if(n.length()>20){
            	n = n.substring(0, 20) + "...";
            }
            return n;
        } catch (MiddlewareQueryException e) {
            return null;
        }
    }
    

    public void cancelButtonClickAction(){
    	if(source instanceof ProcessImportedGermplasmAction){
	    	source.closeAllSelectGermplasmWindows();
    	}
    }
}
