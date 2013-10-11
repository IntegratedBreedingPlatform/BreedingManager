package org.generationcp.browser.cross.study.h2h.main.listeners;

import org.generationcp.browser.cross.study.adapted.main.SpecifyAndWeighEnvironments;
import org.generationcp.browser.cross.study.h2h.main.EnvironmentsAvailableComponent;
import org.generationcp.browser.cross.study.h2h.main.ResultsComponent;
import org.generationcp.browser.cross.study.h2h.main.SpecifyGermplasmsComponent;
import org.generationcp.browser.cross.study.h2h.main.TraitsAvailableComponent;
import org.generationcp.browser.cross.study.h2h.main.dialogs.AddEnvironmentalConditionsDialog;
import org.generationcp.browser.cross.study.h2h.main.dialogs.FilterLocationDialog;
import org.generationcp.browser.cross.study.h2h.main.dialogs.FilterStudyDialog;
import org.generationcp.browser.cross.study.h2h.main.dialogs.SelectGermplasmEntryDialog;
import org.generationcp.browser.cross.study.h2h.main.dialogs.SelectGermplasmListDialog;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.browser.germplasm.dialogs.SelectAGermplasmDialog;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.TrialEnvironmentProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

public class HeadToHeadCrossStudyMainValueChangeListener implements ValueChangeListener {

    private static final long serialVersionUID = -3422805642974069212L;

    private static final Logger LOG = LoggerFactory.getLogger(HeadToHeadCrossStudyMainValueChangeListener.class);

    private Component source;
    private Component sourceComboBox;
    private FilterLocationDto filterLocationDto;
    private TrialEnvironmentProperty environmentCondition;
    private String tableKey;
    private boolean isTagAll = false;
    private Component parentOfSource; // EnvironmentsAvailableCompoent or SpecifyAndWeighEnvironment
 
    public HeadToHeadCrossStudyMainValueChangeListener(Component source, boolean isTagAll){
        this.source = source;
        this.isTagAll = isTagAll;
    }
    
    public HeadToHeadCrossStudyMainValueChangeListener(Component source, Component sourceComboBox){
        this.source = source;
        this.sourceComboBox = sourceComboBox;
    }
    public HeadToHeadCrossStudyMainValueChangeListener(Component source, Component sourceComboBox, String tableKey){
        this.source = source;
        this.sourceComboBox = sourceComboBox;
        this.tableKey = tableKey;
    }
    public HeadToHeadCrossStudyMainValueChangeListener(Component source, Component sourceComboBox, FilterLocationDto filterLocationDto){
        this.source = source;
        this.filterLocationDto = filterLocationDto;
    }
    public HeadToHeadCrossStudyMainValueChangeListener(Component source, Component sourceComboBox, TrialEnvironmentProperty environmentCondition){
        this.source = source;
        this.environmentCondition = environmentCondition;
    }
    public HeadToHeadCrossStudyMainValueChangeListener(Component source, Component parentOfSource, boolean isTagAll){
        this.source = source;
        this.parentOfSource = parentOfSource;
        this.isTagAll = isTagAll;
    }
    
    @Override
    public void valueChange(ValueChangeEvent event) {
    	
		String parentClass = "";
    	if( parentOfSource instanceof SpecifyAndWeighEnvironments ){
    		parentClass = "SpecifyAndWeighEnvironments";
    	}
    	else if( parentOfSource instanceof EnvironmentsAvailableComponent ){
    		parentClass = "EnvironmentsAvailableComponent";
    	}
    	
        if (source instanceof TraitsAvailableComponent) {
            ((TraitsAvailableComponent) source).clickCheckBox(sourceComboBox, (Boolean)event.getProperty().getValue());
        } else if (source instanceof EnvironmentsAvailableComponent) {
            ((EnvironmentsAvailableComponent) source).clickCheckBox(tableKey, sourceComboBox, (Boolean)event.getProperty().getValue());
        } else if (source instanceof SpecifyAndWeighEnvironments) {
            ((SpecifyAndWeighEnvironments) source).clickCheckBox(tableKey, sourceComboBox, (Boolean)event.getProperty().getValue());
        } else if (source instanceof FilterLocationDialog) {  
        	if(isTagAll){   	
        		((FilterLocationDialog) source).clickCheckBoxTag((Boolean)event.getProperty().getValue(), parentClass);
        	}else
        		((FilterLocationDialog) source).clickCheckBox((Boolean)event.getProperty().getValue(), filterLocationDto);
        }else if (source instanceof FilterStudyDialog) {
        	if(isTagAll){
        		((FilterStudyDialog) source).clickCheckBoxTag((Boolean)event.getProperty().getValue(), parentClass);
        	}else
        		((FilterStudyDialog) source).clickCheckBox((Boolean)event.getProperty().getValue(), filterLocationDto);
        
        } else if (source instanceof AddEnvironmentalConditionsDialog) {
        	if(isTagAll){
        		((AddEnvironmentalConditionsDialog) source).clickCheckBoxTag((Boolean)event.getProperty().getValue());
        	}else
        		((AddEnvironmentalConditionsDialog) source).clickCheckBox((Boolean)event.getProperty().getValue(), environmentCondition);
        }   
        else {
            LOG.error("HeadToHeadCrossStudyMainButtonClickListener: Error with buttonClick action. Source not identified.");
        }
    }

}
