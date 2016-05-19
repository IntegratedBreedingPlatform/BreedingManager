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
import org.generationcp.breeding.manager.germplasm.containers.GermplasmSearchQuery;
import org.generationcp.breeding.manager.germplasm.containers.GermplasmSearchQueryFactory;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

/**
 * Lazy loading table component for the search germplasm results.
 *
 * @author Joyce Avestro
 *
 */
@Configurable
public class GermplasmSearchResultComponent extends Table implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	private final GermplasmDataManager germplasmDataManager;
	private final String searchChoice;
	private final String searchValue;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	public GermplasmSearchResultComponent(GermplasmDataManager germplasmDataManager, String searchChoice, String searchValue) {
		this.germplasmDataManager = germplasmDataManager;
		this.searchChoice = searchChoice;
		this.searchValue = searchValue;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		GermplasmSearchQueryFactory factory =
				new GermplasmSearchQueryFactory(this.germplasmDataManager, this.searchChoice, this.searchValue);
		LazyQueryContainer container = new LazyQueryContainer(factory, false, 50);

		// add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
		container.addContainerProperty(GermplasmSearchQuery.GID, String.class, null);
		container.addContainerProperty(GermplasmSearchQuery.NAMES, String.class, null);
		container.addContainerProperty(GermplasmSearchQuery.METHOD, String.class, null);
		container.addContainerProperty(GermplasmSearchQuery.LOCATION, String.class, null);

		this.setColumnHeader(GermplasmSearchQuery.GID, this.getTermNameFromOntology(ColumnLabels.GID));
		this.setColumnHeader(GermplasmSearchQuery.NAMES, this.getTermNameFromOntology(ColumnLabels.DESIGNATION));
		this.setColumnHeader(GermplasmSearchQuery.METHOD, this.getTermNameFromOntology(ColumnLabels.BREEDING_METHOD_NAME));
		this.setColumnHeader(GermplasmSearchQuery.LOCATION, this.getTermNameFromOntology(ColumnLabels.GERMPLASM_LOCATION));

		// initialize the first batch of data to be displayed
		container.getQueryView().getItem(0);

		this.setColumnWidth(GermplasmSearchQuery.GID, 100);
		this.setContainerDataSource(container);
		this.setWidth("100%");
		this.setHeight("200px");
		this.setSelectable(true);
		this.setMultiSelect(false);
		this.setSizeFull();
		// react at once when something is selected turn on column reordering and collapsing
		this.setImmediate(true);
		this.setColumnReorderingAllowed(true);
		this.setColumnCollapsingAllowed(true);

		this.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

			private static final long serialVersionUID = 1L;

			@Override
			public String generateDescription(Component source, Object itemId, Object propertyId) {
				return GermplasmSearchResultComponent.this.messageSource.getMessage(Message.CLICK_TO_VIEW_GERMPLASM_DETAILS);
			}
		});

	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	protected String getTermNameFromOntology(ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

}
