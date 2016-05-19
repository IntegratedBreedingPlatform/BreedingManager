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
import org.generationcp.breeding.manager.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.breeding.manager.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmDerivativeNeighborhoodComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;
	private GermplasmPedigreeTree germplasmDerivativeNeighborhood;
	private final GermplasmQueries qQuery;
	private final VerticalLayout mainLayout;
	private final TabSheet tabSheet;
	private final GermplasmIndexContainer dataIndexContainer;
	private Tree derivativeNeighborhoodTree;
	private final int gid;
	private Label labelNumberOfStepsBackward;
	private Label labelNumberOfStepsForward;
	private Button btnDisplay;
	private HorizontalLayout hLayout1;
	private HorizontalLayout hLayout2;
	private HorizontalLayout hLayout3;
	private Select selectNumberOfStepBackward;
	private Select selectNumberOfStepForward;
	public static final String DISPLAY_BUTTON_ID = "Display Derivative Neighborhood";

	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(GermplasmDerivativeNeighborhoodComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmDerivativeNeighborhoodComponent(int gid, GermplasmQueries qQuery, GermplasmIndexContainer dataResultIndexContainer,
			VerticalLayout mainLayout, TabSheet tabSheet) throws InternationalizableException {

		super();

		this.mainLayout = mainLayout;
		this.tabSheet = tabSheet;
		this.qQuery = qQuery;
		this.dataIndexContainer = dataResultIndexContainer;
		this.gid = gid;
	}

	private void addNode(GermplasmPedigreeTreeNode node, int level) {

		if (level == 1) {
			String name = node.getGermplasm().getPreferredName() != null ? node.getGermplasm().getPreferredName().getNval() : null;
			String rootNodeLabel = name + "(" + node.getGermplasm().getGid() + ")";
			int rootNodeId = node.getGermplasm().getGid();
			this.derivativeNeighborhoodTree.addItem(rootNodeId);
			this.derivativeNeighborhoodTree.setItemCaption(rootNodeId, rootNodeLabel);
			this.derivativeNeighborhoodTree.setParent(rootNodeId, rootNodeId);
			this.derivativeNeighborhoodTree.setChildrenAllowed(rootNodeId, true);
			this.derivativeNeighborhoodTree.expandItemsRecursively(rootNodeId);
		}
		for (GermplasmPedigreeTreeNode child : node.getLinkedNodes()) {
			String name = child.getGermplasm().getPreferredName() != null ? child.getGermplasm().getPreferredName().getNval() : null;
			int parentNodeId = node.getGermplasm().getGid();
			String childNodeLabel = name + "(" + child.getGermplasm().getGid() + ")";
			int childNodeId = child.getGermplasm().getGid();
			this.derivativeNeighborhoodTree.addItem(childNodeId);
			this.derivativeNeighborhoodTree.setItemCaption(childNodeId, childNodeLabel);
			this.derivativeNeighborhoodTree.setParent(childNodeId, parentNodeId);
			this.derivativeNeighborhoodTree.setChildrenAllowed(childNodeId, true);
			this.derivativeNeighborhoodTree.expandItemsRecursively(childNodeId);

			if (child.getGermplasm().getGid() == this.gid) {
				this.derivativeNeighborhoodTree.setValue(childNodeId);
				this.derivativeNeighborhoodTree.setImmediate(true);
			}

			this.addNode(child, level + 1);
		}
	}

	@Override
	public void afterPropertiesSet() {
		this.setSpacing(true);
		this.setMargin(true);

		this.setStyleName("gsb-component-wrap");

		this.hLayout1 = new HorizontalLayout();
		this.hLayout1.setSpacing(true);
		this.hLayout1.setMargin(false, true, false, false);
		this.hLayout1.addStyleName("gsb-component-wrap");

		this.hLayout2 = new HorizontalLayout();
		this.hLayout2.setSpacing(true);
		this.hLayout2.setMargin(false, true, false, false);
		this.hLayout2.addStyleName("gsb-component-wrap");

		this.hLayout3 = new HorizontalLayout();
		this.hLayout3.setSpacing(true);
		this.hLayout3.addStyleName("gsb-component-wrap");

		this.labelNumberOfStepsBackward = new Label();
		this.labelNumberOfStepsBackward.setWidth("170px");
		this.selectNumberOfStepBackward = new Select();
		this.selectNumberOfStepBackward.setWidth("50px");
		this.populateSelectSteps(this.selectNumberOfStepBackward);
		this.selectNumberOfStepBackward.setNullSelectionAllowed(false);
		this.selectNumberOfStepBackward.select("2");

		this.labelNumberOfStepsForward = new Label();
		this.labelNumberOfStepsForward.setWidth("170px");
		this.selectNumberOfStepForward = new Select();
		this.selectNumberOfStepForward.setWidth("50px");
		this.populateSelectSteps(this.selectNumberOfStepForward);
		this.selectNumberOfStepForward.setNullSelectionAllowed(false);
		this.selectNumberOfStepForward.select("3");

		this.btnDisplay = new Button();
		this.btnDisplay.setData(GermplasmDerivativeNeighborhoodComponent.DISPLAY_BUTTON_ID);
		this.btnDisplay.setDescription("Display Germplasm Derivative Neighborhood ");
		this.btnDisplay.addListener(new GermplasmButtonClickListener(this));
		this.btnDisplay.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.hLayout1.addComponent(this.labelNumberOfStepsBackward);
		this.hLayout1.addComponent(this.selectNumberOfStepBackward);
		this.hLayout2.addComponent(this.labelNumberOfStepsForward);
		this.hLayout2.addComponent(this.selectNumberOfStepForward);
		this.hLayout3.addComponent(this.btnDisplay);

		CssLayout cssLayout = new CssLayout();
		cssLayout.setWidth("100%");
		cssLayout.addComponent(this.hLayout1);
		cssLayout.addComponent(this.hLayout2);
		cssLayout.addComponent(this.hLayout3);

		this.addComponent(cssLayout);

		this.derivativeNeighborhoodTree = new Tree();
		this.addComponent(this.derivativeNeighborhoodTree);
		this.derivativeNeighborhoodTree.addListener(new GermplasmItemClickListener(this));

		if (this.mainLayout != null) {
			this.derivativeNeighborhoodTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

				private static final long serialVersionUID = 3442425534732855473L;

				@Override
				public String generateDescription(Component source, Object itemId, Object propertyId) {
					return GermplasmDerivativeNeighborhoodComponent.this.messageSource.getMessage(Message.CLICK_TO_VIEW_GERMPLASM_DETAILS);
				}
			});
		}

		this.displayButtonClickAction();
	}

	private void populateSelectSteps(Select select) {

		for (int i = 1; i <= 10; i++) {
			select.addItem(String.valueOf(i));
		}
	}

	@Override
	public void attach() {

		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {

		this.messageSource.setValue(this.labelNumberOfStepsBackward, Message.NUMBER_OF_STEPS_BACKWARD_LABEL);
		this.messageSource.setValue(this.labelNumberOfStepsForward, Message.NUMBER_OF_STEPS_FORWARD_LABEL);
		this.messageSource.setCaption(this.btnDisplay, Message.DISPLAY_BUTTON_LABEL);

	}

	public void displayButtonClickAction() {

		this.removeComponent(this.derivativeNeighborhoodTree);
		this.derivativeNeighborhoodTree.removeAllItems();
		int numberOfStepsBackward = Integer.valueOf(this.selectNumberOfStepBackward.getValue().toString());
		int numberOfStepsForward = Integer.valueOf(this.selectNumberOfStepForward.getValue().toString());

		this.germplasmDerivativeNeighborhood =
				this.qQuery.getDerivativeNeighborhood(Integer.valueOf(this.gid), numberOfStepsBackward, numberOfStepsForward); // throws
																																// QueryException
		if (this.germplasmDerivativeNeighborhood != null) {
			this.addNode(this.germplasmDerivativeNeighborhood.getRoot(), 1);
		}

		this.addComponent(this.derivativeNeighborhoodTree);

	}

	public void displayNewGermplasmDetailTab(int gid) throws InternationalizableException {
		if (this.mainLayout != null && this.tabSheet != null) {
			VerticalLayout detailLayout = new VerticalLayout();
			detailLayout.setSpacing(true);

			if (!Util.isTabExist(this.tabSheet, String.valueOf(gid))) {
				detailLayout.addComponent(new GermplasmDetail(gid, this.qQuery, this.dataIndexContainer, this.mainLayout, this.tabSheet,
						false));
				Tab tab = this.tabSheet.addTab(detailLayout, String.valueOf(gid), null);
				tab.setClosable(true);
				this.tabSheet.setSelectedTab(detailLayout);
				this.mainLayout.addComponent(this.tabSheet);

			} else {
				Tab tab = Util.getTabAlreadyExist(this.tabSheet, String.valueOf(gid));
				this.tabSheet.setSelectedTab(tab.getComponent());
			}

		}
	}
}
