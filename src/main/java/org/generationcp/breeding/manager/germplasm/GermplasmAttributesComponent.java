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
public class GermplasmAttributesComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	GermplasmIndexContainer dataIndexContainer;

	GermplasmDetailModel gDetailModel;

	private Table attributesTable;
	private Label noDataAvailableLabel;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmAttributesComponent(GermplasmIndexContainer dataIndexContainer, GermplasmDetailModel gDetailModel) {
		this.dataIndexContainer = dataIndexContainer;
		this.gDetailModel = gDetailModel;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.initializeComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public void initializeComponents() {
		IndexedContainer attributes = this.dataIndexContainer.getGermplasmAttribute(this.gDetailModel);

		if (attributes.getItemIds().isEmpty()) {
			this.noDataAvailableLabel = new Label("There is no Attributes information for this germplasm.");
		} else {
			this.attributesTable = new Table();
			this.attributesTable.setWidth("90%");
			this.attributesTable.setContainerDataSource(attributes);
			if (attributes.getItemIds().size() < 10) {
				this.attributesTable.setPageLength(attributes.getItemIds().size());
			} else {
				this.attributesTable.setPageLength(10);
			}
			this.attributesTable.setSelectable(true);
			this.attributesTable.setMultiSelect(false);
			this.attributesTable.setImmediate(true); // react at once when something is
			this.attributesTable.setColumnReorderingAllowed(true);
			this.attributesTable.setColumnCollapsingAllowed(true);
			this.attributesTable.setColumnHeaders(new String[] {this.messageSource.getMessage(Message.TYPE_LABEL),
					this.messageSource.getMessage(Message.TYPEDESC_LABEL), this.messageSource.getMessage(Message.NAME_LABEL),
					this.messageSource.getMessage(Message.DATE_LABEL), this.messageSource.getMessage(Message.LOCATION_LABEL)});
		}
	}

	public void initializeValues() {

	}

	public void addListeners() {

	}

	public void layoutComponents() {
		if (this.attributesTable != null) {
			this.addComponent(this.attributesTable);
		} else {
			this.addComponent(this.noDataAvailableLabel);
		}
	}

	@Override
	public void updateLabels() {

	}

}
