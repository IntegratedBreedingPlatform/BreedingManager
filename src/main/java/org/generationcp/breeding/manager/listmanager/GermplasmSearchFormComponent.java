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

package org.generationcp.breeding.manager.listmanager;

import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
@Configurable
public class GermplasmSearchFormComponent extends VerticalLayout implements Property.ValueChangeListener, InitializingBean,
        InternationalizableComponent{

    private String choice;
    private String searchValue;
    private final TextField txtSearchValue = new TextField();
    private OptionGroup searchSelect;
    public static final String SEARCH_OPTION_GID = "GID";
    public static final String SEARCH_OPTION_NAME = "Names";

    private static final List<String> SEARCH_OPTION = Arrays.asList(new String[] { 
    						SEARCH_OPTION_GID
                                                            ,SEARCH_OPTION_NAME 
                                                            });

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmSearchFormComponent() {

    }

    @Override
    public void valueChange(ValueChangeEvent event) {
        choice = searchSelect.getValue().toString();
        searchValue = txtSearchValue.getValue().toString();
    }

    public String getChoice() {
        return choice;
    }

    public String getSearchValue() {
        return searchValue;
    }

    @Override
    public void afterPropertiesSet() {

        GridLayout grid = new GridLayout(4, 4);
        setSpacing(true);

        searchSelect = new OptionGroup("Search for", SEARCH_OPTION);
        searchSelect.select(SEARCH_OPTION_NAME);
        searchSelect.setImmediate(true);
        searchSelect.addListener(this);
        searchSelect.addStyleName("horizontal");
        grid.addComponent(searchSelect, 1, 1);

        txtSearchValue.setMaxLength(-1);
        txtSearchValue.addListener(this);
        txtSearchValue.setImmediate(true);
        txtSearchValue.addStyleName("addTopSpace");
        grid.addComponent(txtSearchValue, 2, 1);
        
        addComponent(grid);

        this.choice = SEARCH_OPTION_NAME;
        this.searchValue = "";
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setCaption(searchSelect, Message.SEARCH_FOR_LABEL);
    }

}
