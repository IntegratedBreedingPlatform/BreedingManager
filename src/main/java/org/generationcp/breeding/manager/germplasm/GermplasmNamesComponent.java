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

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmNamesComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	private final GermplasmIndexContainer dataIndexContainer;
	private final GermplasmDetailModel gDetailModel;

	private Table namesTable;
	private Label noDataAvailableLabel;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmNamesComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel) {
		this.dataIndexContainer = dataIndexContainer;
		this.gDetailModel = gDetailModel;
	}

	@Override
	public void afterPropertiesSet() {
		this.initializeComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	private void initializeComponents() {
		IndexedContainer names = this.dataIndexContainer.getGermplasmNames(this.gDetailModel);

		if (names.getItemIds().isEmpty()) {
			this.noDataAvailableLabel = new Label("There is no Names information for this germplasm.");
		} else {
			this.namesTable = new Table();
			this.namesTable.setWidth("90%");
			this.namesTable.setContainerDataSource(names);
			if (names.getItemIds().size() < 10) {
				this.namesTable.setPageLength(names.getItemIds().size());
			} else {
				this.namesTable.setPageLength(10);
			}
			this.namesTable.setSelectable(true);
			this.namesTable.setMultiSelect(false);
			this.namesTable.setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
			this.namesTable.setColumnReorderingAllowed(true);
			this.namesTable.setColumnCollapsingAllowed(true);
			this.namesTable.setColumnHeaders(new String[] {this.messageSource.getMessage(Message.NAME_LABEL),
					this.messageSource.getMessage(Message.DATE_LABEL), this.messageSource.getMessage(Message.LOCATION_LABEL),
					this.messageSource.getMessage(Message.TYPE_LABEL), this.messageSource.getMessage(Message.TYPEDESC_LABEL)});
		}
	}

	private void initializeValues() {

	}

	private void addListeners() {

	}

	private void layoutComponents() {
		if (this.namesTable != null) {
			this.addComponent(this.namesTable);
		} else {
			this.addComponent(this.noDataAvailableLabel);
		}
	}

	@Override
	public void updateLabels() {

	}

}
