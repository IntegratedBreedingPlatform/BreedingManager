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

package org.generationcp.browser.study;

import java.util.Date;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Country;
import org.generationcp.middleware.pojos.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickListener;

/**
 * 
 * @author Joyce Avestro
 * 
 */
@Configurable
public class StudySearchInputComponent extends GridLayout implements InitializingBean, InternationalizableComponent{

    private static final Logger LOG = LoggerFactory.getLogger(StudySearchInputComponent.class);
    private static final long serialVersionUID = 1L;

    List<Study> studies; 

    Label dateLabel;
    Label nameLabel;
    Label countryLabel;
    Label seasonLabel;

    private GridLayout gridLayout;
    private DateField dateField;
    private TextField nameField;
    private ComboBox countryCombo;
    private ComboBox seasonCombo;

    private Button searchButton;
    private Button clearButton;
    private Component buttonArea;

    StudySearchMainComponent parentComponent;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    

    public StudySearchInputComponent(StudySearchMainComponent parentComponent) {
        this.parentComponent = parentComponent;
    }
    
    @Override
    public void afterPropertiesSet() {

        setSpacing(true);

        dateField = new DateField();
        nameField = new TextField();
        nameField.setDescription(messageSource.getMessage(Message.EXACT_STUDY_NAME_TEXT));
        countryCombo = createCountryComboBox();
        seasonCombo = createSeasonComboBox();

        dateLabel = new Label(messageSource.getMessage(Message.DATE_LABEL));
        nameLabel = new Label(messageSource.getMessage(Message.NAME_LABEL));
        countryLabel = new Label(messageSource.getMessage(Message.COUNTRY_LABEL));
        seasonLabel = new Label(messageSource.getMessage(Message.SEASON_LABEL));        

        gridLayout = new GridLayout();
        gridLayout.setRows(5);
        gridLayout.setColumns(3);
        gridLayout.setSpacing(true);
        
        gridLayout.addComponent(dateLabel, 1, 1);
        gridLayout.addComponent(dateField, 2, 1);
        gridLayout.addComponent(nameLabel, 1, 2);
        gridLayout.addComponent(nameField, 2, 2);
        gridLayout.addComponent(countryLabel, 1, 3);
        gridLayout.addComponent(countryCombo, 2, 3);
        gridLayout.addComponent(seasonLabel, 1, 4);
        gridLayout.addComponent(seasonCombo, 2, 4);
        addComponent(gridLayout);
        
        buttonArea = layoutButtonArea();
        addComponent(buttonArea);
        setComponentAlignment(buttonArea, Alignment.TOP_RIGHT);
        
    }
    

    protected Component layoutButtonArea() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setMargin(true, false, false, false);

        searchButton = new Button();
        messageSource.setCaption(searchButton, Message.SEARCH_LABEL);
        
        clearButton = new Button();
        messageSource.setCaption(clearButton, Message.CLEAR_LABEL);

        ButtonClickListener buttonClickListener = new ButtonClickListener();
        searchButton.addListener(buttonClickListener);
        clearButton.addListener(buttonClickListener);
        
        buttonLayout.addComponent(searchButton);
        buttonLayout.addComponent(clearButton);
        return buttonLayout;
    }
    
    

    private ComboBox createCountryComboBox(){
        List<Country> countries = null;
        try {
            countries = germplasmDataManager.getAllCountry();
        }
        catch (MiddlewareQueryException e) {
            LOG.error("Error encountered while getting countries", e);
            throw new InternationalizableException(e, Message.ERROR_DATABASE, 
                    Message.ERROR_PLEASE_CONTACT_ADMINISTRATOR);
        }
        
        BeanItemContainer<Country> beanItemContainer = new BeanItemContainer<Country>(Country.class);
        for (Country country : countries) {
            beanItemContainer.addBean(country);
        }

        ComboBox comboBox = new ComboBox();
        comboBox.setContainerDataSource(beanItemContainer);
        comboBox.setItemCaptionPropertyId("isoabbr");
        comboBox.setImmediate(true);

        return comboBox;
    }

    private ComboBox createSeasonComboBox(){
        Season[] seasons = Season.values();

        ComboBox comboBox = new ComboBox();
        for (Season season : seasons) {
            comboBox.addItem(season);
        }
        comboBox.setImmediate(true);

        return comboBox;
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
    }


    private class ButtonClickListener implements ClickListener{
        private static final long serialVersionUID = 1L;

        @Override
        public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
            
            Integer dateValue = null;
            String nameValue = null;
            String countryValue = null;
            Season seasonValue = null;

            if (event.getButton() == searchButton){
                if ((dateField != null) && (dateField.getValue() != null)){
                    dateValue = new Integer((int) ((Date) dateField.getValue()).getTime());
                }
                if ((nameField != null) && (nameField.getValue() != null)) {
                    nameValue = nameField.getValue().toString();
                }
                if ((countryCombo != null) && (countryCombo.getValue() != null)) {
                    countryValue = ((Country) countryCombo.getValue()).getIsofull();
                }
                if ((seasonCombo != null) && (seasonCombo.getValue() != null)) {
                    seasonValue = (Season) seasonCombo.getValue();
                }
                
                parentComponent.searchStudy(nameValue, countryValue, seasonValue, dateValue);

            } else if (event.getButton() == clearButton){
                dateField.setValue(null);
                nameField.setValue("");
                countryCombo.setValue(null);
                seasonCombo.setValue(null);
                requestRepaintAll();
            }
        }
    }
    


}
