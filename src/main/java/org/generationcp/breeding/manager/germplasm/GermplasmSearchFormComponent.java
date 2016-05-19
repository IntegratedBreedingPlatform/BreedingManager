/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.germplasm;

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
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Configurable
public class GermplasmSearchFormComponent extends VerticalLayout implements Property.ValueChangeListener, InitializingBean,
		InternationalizableComponent {

	/**
	 *
	 */
	private static final long serialVersionUID = 3354382691759103836L;
	private String choice;
	private String searchValue;
	private final TextField txtSearchValue = new TextField();
	private OptionGroup searchSelect;

	private static final List<String> SEARCH_OPTION = Arrays.asList(new String[] {GermplasmQueries.SEARCH_OPTION_GID,
			GermplasmQueries.SEARCH_OPTION_NAME});

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmSearchFormComponent() {

	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		this.choice = this.searchSelect.getValue().toString();
		this.searchValue = this.txtSearchValue.getValue().toString();
	}

	public String getChoice() {
		return this.choice;
	}

	public String getSearchValue() {
		return this.searchValue;
	}

	@Override
	public void afterPropertiesSet() {

		GridLayout grid = new GridLayout(4, 4);
		this.setSpacing(true);

		this.searchSelect = new OptionGroup("Search for", GermplasmSearchFormComponent.SEARCH_OPTION);
		this.searchSelect.select(GermplasmQueries.SEARCH_OPTION_NAME);
		this.searchSelect.setImmediate(true);
		this.searchSelect.addListener(this);
		this.searchSelect.addStyleName("horizontal");
		grid.addComponent(this.searchSelect, 1, 1);

		this.txtSearchValue.setMaxLength(-1);
		this.txtSearchValue.addListener(this);
		this.txtSearchValue.setImmediate(true);
		this.txtSearchValue.addStyleName("addTopSpace");
		grid.addComponent(this.txtSearchValue, 2, 1);
		this.addComponent(grid);

		this.choice = GermplasmQueries.SEARCH_OPTION_NAME;
		this.searchValue = "";
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.searchSelect, Message.SEARCH_FOR_LABEL);
	}

}
