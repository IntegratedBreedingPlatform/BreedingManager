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

import java.util.HashMap;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.breeding.manager.listimport.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listimport.listeners.ImportGermplasmEntryActionListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
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
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Dennis Billano
 *
 */
@Configurable
public class SelectGermplasmWindow extends Window implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, 
		Window.CloseListener, ImportGermplasmEntryActionListener{

    private static final String PARENTAGE = "Parentage";

	private static final String BREEDING_METHOD = "Breeding Method";

	private static final String LOCATION = "Location";

	private static final String GID = "GID";

	private static final String DESIGNATION = "Designation";

	private static final long serialVersionUID = -8113004135173349534L;
    
    public final static String CANCEL_BUTTON_ID = "SelectGermplasmWindow Cancel Button";
    public final static String DONE_BUTTON_ID = "SelectGermplasmWindow Done Button";
    
    private VerticalLayout mainLayout;
    private Button cancelButton;
    private Button doneButton;
    
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
    
    private CheckBox useSameGidCheckbox;
    private CheckBox ignoreMatchesCheckbox;
    private CheckBox ignoreRemainingMatchesCheckbox;
    
    public SelectGermplasmWindow(ProcessImportedGermplasmAction source, String germplasmName, int index, Germplasm germplasm, Boolean viaToolURL) {
        this.germplasmName = germplasmName;
        this.germplasmIndex = index;
        this.germplasm = germplasm;
        this.source = source;
        this.viaToolURL = viaToolURL;
    }
    
    protected void assemble() {
        instantiateComponents();
        initializeValues();
        addListeners();
        layoutComponents();
    }
 
    public void doneAction(){
        try {
        	if(useSameGidCheckbox.booleanValue()) {
        		if(source.getNameGermplasmMap()==null) {
        			source.setNameGermplasmMap(new HashMap<String, Germplasm>());
        		}
        		source.mapGermplasmNamesToGermplasm(germplasmName,germplasm);
        	}
        	if(!ignoreMatchesCheckbox.booleanValue()) {
        		Germplasm selectedGermplasm = this.germplasmDataManager.getGermplasmByGID((Integer) germplasmTable.getValue());
            	source.receiveGermplasmFromWindowAndUpdateGermplasmData(germplasmIndex, germplasm, selectedGermplasm);
        	}
        	source.removeListener(this);
        	if(ignoreRemainingMatchesCheckbox.booleanValue()) {
        		source.ignoreRemainingMatches();
        	} else {
        		source.processNextItems();
        	}
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
        messageSource.setCaption(this, Message.SELECT_MATCHING_GERMPLASM_OR_ADD_NEW_ENTRY);
        messageSource.setCaption(doneButton, Message.CONTINUE);
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

    public void cancelButtonClickAction(){
    	if(source instanceof ProcessImportedGermplasmAction){
	    	source.closeAllImportEntryListeners();
    	}
    }

	@Override
	public void instantiateComponents() {
        selectGermplasmLabel = new Label("", Label.CONTENT_XHTML);
        selectGermplasmLabel.setWidth("100%");
        
        cancelButton = new Button(); 
        cancelButton.setData(CANCEL_BUTTON_ID);
        
        doneButton = new Button();
        doneButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        doneButton.setEnabled(false);
        doneButton.setData(DONE_BUTTON_ID);
        
        germplasmTable = new Table();
        germplasmTable.addContainerProperty(DESIGNATION, Button.class, null);
        germplasmTable.addContainerProperty(GID, Button.class, null);
        germplasmTable.addContainerProperty(LOCATION, String.class, null);
        germplasmTable.addContainerProperty(BREEDING_METHOD, String.class, null);
        germplasmTable.addContainerProperty(PARENTAGE, String.class, null);
        germplasmTable.setHeight("200px");
        germplasmTable.setWidth("750px");
        germplasmTable.setSelectable(true);
        germplasmTable.setMultiSelect(false);
        germplasmTable.setNullSelectionAllowed(false);
        germplasmTable.setImmediate(true);
        
        useSameGidCheckbox = new CheckBox("Use this match for other instances of this name in the import list");
        useSameGidCheckbox.setImmediate(true);
        ignoreMatchesCheckbox = new CheckBox("Ignore matches and add a new entry");
        ignoreMatchesCheckbox.setImmediate(true);
        ignoreRemainingMatchesCheckbox = new CheckBox("Ignore remaining matches and add new entries for all");
        ignoreRemainingMatchesCheckbox.setImmediate(true);
	}

	@Override
	public void addListeners() {
		germplasmTable.addListener(new Property.ValueChangeListener() {
	        private static final long serialVersionUID = 1L;
	        @Override
			public void valueChange(ValueChangeEvent event) {
				toggleContinueButton();
			}
	    });
		
		useSameGidCheckbox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				toggleGermplasmTable();
			}
		});
		
		ignoreMatchesCheckbox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				toggleContinueButton();
				toggleGermplasmTable();
			}
		});
		
		doneButton.addListener(new GermplasmImportButtonClickListener(this));
        doneButton.addListener(new CloseWindowAction(this));
	        
        cancelButton.addListener(new CloseWindowAction(this));
	}

	protected void toggleGermplasmTable() {
		boolean disableSelection = ignoreMatchesCheckbox.booleanValue() &&
				!useSameGidCheckbox.booleanValue();
		if(disableSelection) {
			germplasmTable.setSelectable(false);
			germplasmTable.setNullSelectionAllowed(true);
			germplasmTable.unselect(germplasmTable.getValue());
			germplasmTable.select(null);
			germplasmTable.refreshRowCache();
			germplasmTable.requestRepaint();
			germplasmTable.setImmediate(true);
		} else {
			germplasmTable.setSelectable(true);
			germplasmTable.setNullSelectionAllowed(false);
			germplasmTable.setMultiSelect(false);
			germplasmTable.refreshRowCache();
			germplasmTable.requestRepaint();
			germplasmTable.setImmediate(true);
		}
	}

	protected void toggleContinueButton() {
		boolean enableButton = (germplasmTable.getValue()!=null) ||
							ignoreMatchesCheckbox.booleanValue();
		if(enableButton) {
			doneButton.setEnabled(true);
		} else {
			doneButton.setEnabled(false);
		}				
	}

	@Override
	public void initializeValues() {
		initializeGuideMessage();
		initializeTableValues();
	}

	@Override
	public void layoutComponents() {
		// set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("800px");
        setHeight("460px");
        setResizable(false);
        addStyleName(Reindeer.WINDOW_LIGHT);
        
        // center window within the browser
        center();
        mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.addComponent(selectGermplasmLabel);
        mainLayout.addComponent(germplasmTable);
        
        // Buttons Layout
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");
		buttonLayout.setHeight("40px");
		buttonLayout.setSpacing(true);
	     
		buttonLayout.addComponent(cancelButton);
		buttonLayout.addComponent(doneButton);
		buttonLayout.setComponentAlignment(cancelButton, Alignment.BOTTOM_RIGHT);
	    buttonLayout.setComponentAlignment(doneButton, Alignment.BOTTOM_LEFT);
        
        mainLayout.addComponent(useSameGidCheckbox);
        mainLayout.addComponent(ignoreMatchesCheckbox);
        mainLayout.addComponent(ignoreRemainingMatchesCheckbox);
        
        mainLayout.addComponent(buttonLayout);
        
        this.setContent(mainLayout);
	}
	
	private void initializeGuideMessage(){
		selectGermplasmLabel.setValue("Multiple matches were found with the name <b>" + this.germplasmName + "</b>. Click on an entry below to choose it as a match. "
				+ "You can also choose to ignore the match and add a new entry.");
	}
	
	protected void initializeTableValues() {
		try {
            this.germplasmCount = (int) this.germplasmDataManager.countGermplasmByName(germplasmName, Operation.EQUAL);
            this.germplasms = this.germplasmDataManager.getGermplasmByName(germplasmName, 0, germplasmCount);
            for (int i=0; i<this.germplasms.size(); i++){
            	
                Germplasm germplasm = germplasms.get(i);
                Location location = locationDataManager.getLocationByID(germplasm.getLocationId());
                Method method = germplasmDataManager.getMethodByID(germplasm.getMethodId());
            	Name preferredName = germplasmDataManager.getPreferredNameByGID(germplasm.getGid());
            	
            	Button gidButton = new Button(String.format("%s", germplasm.getGid().toString()), new GidLinkClickListener(germplasm.getGid().toString(), viaToolURL));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK); 
                
                
                Button desigButton = new Button(preferredName.getNval(), new GidLinkClickListener(germplasm.getGid().toString(), viaToolURL));
                desigButton.setStyleName(BaseTheme.BUTTON_LINK); 
                
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
                
                this.germplasmTable.addItem(new Object[]{desigButton, gidButton, locationName, methodName, crossExpansion}, germplasm.getGid());
            }
            
            germplasmTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
    			private static final long serialVersionUID = 1L;

    			public String generateDescription(Component source, Object itemId,
    					Object propertyId) {
    				if(propertyId==DESIGNATION){
    					Item item = germplasmTable.getItem(itemId);
    					Integer gid = Integer.valueOf(((Button) item.getItemProperty(GID).getValue()).getCaption());
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

	public String getGermplasmName() {
		return germplasmName;
	}

	public void setGermplasmName(String germplasmName) {
		this.germplasmName = germplasmName;
	}

	public Germplasm getGermplasm() {
		return germplasm;
	}

	public void setGermplasm(Germplasm germplasm) {
		this.germplasm = germplasm;
	}

	public int getGermplasmIndex() {
		return germplasmIndex;
	}

	public void setGermplasmIndex(int germplasmIndex) {
		this.germplasmIndex = germplasmIndex;
	}

	@Override
	public void windowClose(CloseEvent e) {
		super.close();
		source.closeAllImportEntryListeners();
	}
	
	
	
	
}
