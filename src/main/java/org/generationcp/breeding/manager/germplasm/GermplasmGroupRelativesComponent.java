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
import org.generationcp.breeding.manager.germplasm.containers.GroupRelativesQuery;
import org.generationcp.breeding.manager.germplasm.containers.GroupRelativesQueryFactory;
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
public class GermplasmGroupRelativesComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;
	private final int gid;

	private Table groupRelativesTable;
	private Label noDataAvailableLabel;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmGroupRelativesComponent(int gid) {
		this.gid = gid;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.initializeComponents();
		this.layoutComponents();
	}

	private void initializeComponents() {
		GroupRelativesQueryFactory factory = new GroupRelativesQueryFactory(Integer.valueOf(this.gid));
		LazyQueryContainer container = new LazyQueryContainer(factory, false, 50);

		// add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
		container.addContainerProperty(GroupRelativesQuery.GID, String.class, null);
		container.addContainerProperty(GroupRelativesQuery.PREFERRED_NAME, String.class, null);

		if (container.size() > 0) {
			this.groupRelativesTable = new Table();

			this.groupRelativesTable.setColumnHeader(GroupRelativesQuery.GID, this.messageSource.getMessage(Message.GID_LABEL));
			this.groupRelativesTable.setColumnHeader(GroupRelativesQuery.PREFERRED_NAME,
					this.messageSource.getMessage(Message.PREFNAME_LABEL));

			container.getQueryView().getItem(0); // initialize the first batch of data to be displayed

			this.groupRelativesTable.setContainerDataSource(container);
			this.groupRelativesTable.setSelectable(true);
			this.groupRelativesTable.setMultiSelect(false);
			this.groupRelativesTable.setImmediate(true); // react at once when something is selected turn on column reordering and
															// collapsing
			this.groupRelativesTable.setColumnReorderingAllowed(true);
			this.groupRelativesTable.setColumnCollapsingAllowed(true);
		} else {
			this.noDataAvailableLabel = new Label("There is no Group Relatives information for this germplasm.");
		}
	}

	private void layoutComponents() {
		if (this.groupRelativesTable != null) {
			this.addComponent(this.groupRelativesTable);
		} else {
			this.addComponent(this.noDataAvailableLabel);
		}
	}

	@Override
	public void updateLabels() {

	}

}
