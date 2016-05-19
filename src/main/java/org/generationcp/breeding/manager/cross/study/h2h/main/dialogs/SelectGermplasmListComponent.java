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

package org.generationcp.breeding.manager.cross.study.h2h.main.dialogs;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class SelectGermplasmListComponent extends HorizontalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private GermplasmListTreeComponent treeComponent;
	private SelectGermplasmListInfoComponent listInfoComponent;
	private Label treeLabel;

	private HorizontalLayout mainLayout;

	private final Integer lastOpenedListId;
	private final Component source;

	public SelectGermplasmListComponent(Integer lastOpenedListId, Component source) {
		this.lastOpenedListId = lastOpenedListId;
		this.source = source;
	}

	@Override
	public void afterPropertiesSet() {
		this.assemble();
	}

	protected void assemble() {
		this.initializeComponents();
		this.initializeValues();
		this.initializeLayout();
		this.initializeActions();
	}

	protected void initializeComponents() {
		this.mainLayout = new HorizontalLayout();

		this.treeComponent = new GermplasmListTreeComponent(this);
		this.treeLabel = new Label(this.messageSource.getMessage(Message.PROJECT_LISTS));
		this.treeLabel.addStyleName(Bootstrap.Typography.H3.styleName());

		this.listInfoComponent = new SelectGermplasmListInfoComponent(this.lastOpenedListId, this.source);
	}

	protected void initializeValues() {

	}

	protected void initializeLayout() {
		// this.setSpacing(true);
		this.setMargin(true);

		this.mainLayout.setWidth("100%");
		this.mainLayout.setHeight("100%");
		this.mainLayout.setSpacing(true);

		this.listInfoComponent.setWidth("495px");
		this.listInfoComponent.setHeight("420px");

		VerticalLayout treeLayout = new VerticalLayout();
		treeLayout.setWidth("240px");
		treeLayout.setHeight("420px");
		treeLayout.addComponent(this.treeLabel);
		treeLayout.addComponent(this.treeComponent);

		this.mainLayout.addComponent(treeLayout);
		this.mainLayout.addComponent(this.listInfoComponent);
		this.mainLayout.setExpandRatio(this.listInfoComponent, 1.0f);

		this.addComponent(this.mainLayout);
	}

	protected void initializeActions() {

	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
	}

	public SelectGermplasmListInfoComponent getListInfoComponent() {
		return this.listInfoComponent;
	}
}
