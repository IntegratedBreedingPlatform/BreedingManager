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
import org.generationcp.breeding.manager.germplasm.containers.ManagementNeighborsQuery;
import org.generationcp.breeding.manager.germplasm.containers.ManagementNeighborsQueryFactory;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmManagementNeighborsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	private final Integer gid;

	private Table managementNeighborsTable;
	private Label noDataAvailableLabel;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmManagementNeighborsComponent(Integer gid) {
		this.gid = gid;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.initializeComponents();
		this.layoutComponents();
	}

	private void initializeComponents() {
		ManagementNeighborsQueryFactory factory = new ManagementNeighborsQueryFactory(this.gid);
		LazyQueryContainer container = new LazyQueryContainer(factory, false, 50);

		if (container.size() > 0) {
			this.managementNeighborsTable = new Table();
			// add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
			container.addContainerProperty(ManagementNeighborsQuery.GID, String.class, null);
			container.addContainerProperty(ManagementNeighborsQuery.PREFERRED_NAME, String.class, null);

			container.getQueryView().getItem(0); // initialize the first batch of data to be displayed

			this.managementNeighborsTable.setContainerDataSource(container);
			if (container.size() < 10) {
				this.managementNeighborsTable.setPageLength(container.size());
			} else {
				this.managementNeighborsTable.setPageLength(10);
			}
			this.managementNeighborsTable.setSelectable(true);
			this.managementNeighborsTable.setMultiSelect(false);
			this.managementNeighborsTable.setImmediate(true); // react at once when something is selected turn on column reordering and
																// collapsing
			this.managementNeighborsTable.setColumnReorderingAllowed(true);
			this.managementNeighborsTable.setColumnCollapsingAllowed(true);

			this.managementNeighborsTable.setColumnHeader(ManagementNeighborsQuery.GID, this.messageSource.getMessage(Message.GID_LABEL));
			this.managementNeighborsTable.setColumnHeader(ManagementNeighborsQuery.PREFERRED_NAME,
					this.messageSource.getMessage(Message.PREFNAME_LABEL));
		} else {
			this.noDataAvailableLabel = new Label("There is no Management Neighbors Information for this germplasm.");
		}
	}

	private void layoutComponents() {
		if (this.managementNeighborsTable != null) {
			this.addComponent(this.managementNeighborsTable);
		} else {
			this.addComponent(this.noDataAvailableLabel);
		}
	}

	@Override
	public void updateLabels() {
	}

}
