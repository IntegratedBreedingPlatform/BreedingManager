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
import org.generationcp.breeding.manager.germplasm.inventory.InventoryViewComponent;
import org.generationcp.breeding.manager.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.breeding.manager.germplasm.listeners.GermplasmSelectedTabChangeListener;
import org.generationcp.breeding.manager.germplasm.pedigree.GermplasmPedigreeGraphComponent;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GermplasmDetail extends Accordion implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	private static final int FIRST_TAB = 1;
	private static final int SECOND_TAB = 2;
	private static final int THIRD_TAB = 3;
	private static final int FOURTH_TAB = 4;
	private static final int FIFTH_TAB = 5;
	private static final int SIX_TAB = 6;
	private static final int SEVEN_TAB = 7;
	private static final int EIGHT_TAB = 8;
	private static final int NINE_TAB = 9;
	private static final int TEN_TAB = 10;
	private static final int ELEVEN_TAB = 11;
	private static final int TWELVE_TAB = 12;
	public final static String VIEW_PEDIGREE_GRAPH_ID = "View Pedigree Graph";
	public final static String INCLUDE_DERIVATIVE_LINES = "Include Derivative Lines";
	public final static String REFRESH_BUTTON_ID = "Refresh";
	private final GermplasmIndexContainer dataIndexContainer;
	private final GermplasmQueries qQuery;
	private GermplasmDetailModel gDetailModel;
	private VerticalLayout layoutDetails;
	private VerticalLayout layoutNames;
	private VerticalLayout layoutAttributes;
	private VerticalLayout layoutGenerationHistory;
	private VerticalLayout layoutPedigreeTree;
	private VerticalLayout layoutGermplasmList;
	private VerticalLayout layoutGroupRelatives;
	private VerticalLayout layoutManagementNeighbors;
	private VerticalLayout layoutInventoryInformation;
	private VerticalLayout layoutDerivativeNeighborhood;
	private VerticalLayout layoutMaintenanceNeighborhood;
	private VerticalLayout layoutGermplasmStudyInformation;
	private final VerticalLayout mainLayout;
	private GridLayout treeDisplayLayout;
	private Label levelLabel;
	private final int gid;
	private final TabSheet tabSheet;
	private final GermplasmIndexContainer dataResultIndexContainer;
	private final boolean fromUrl; // this is true if this component is created by accessing the Germplasm Details page directly from the
									// URL

	private GermplasmPedigreeTreeComponent germplasmPedigreeTreeComponent;
	private CheckBox pedigreeDerivativeCheckbox;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmDataManager germplasmManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private Button btnViewPedigreeGraph;

	public GermplasmDetail(final int gid, final GermplasmQueries qQuery, final GermplasmIndexContainer dataResultIndexContainer,
			final VerticalLayout mainLayout, final TabSheet tabSheet, final boolean fromUrl) throws InternationalizableException {
		this.qQuery = qQuery;
		this.mainLayout = mainLayout;
		this.gid = gid;
		this.tabSheet = tabSheet;
		this.dataResultIndexContainer = dataResultIndexContainer;
		this.dataIndexContainer = dataResultIndexContainer;
		this.fromUrl = fromUrl;
	}

	public GermplasmDetailModel getGermplasmDetailModel() {
		return this.gDetailModel;
	}

	public void selectedTabChangeAction() throws InternationalizableException {
		final Component selected = this.getSelectedTab();
		final Tab tab = this.getTab(selected);
		if (tab.getComponent() instanceof VerticalLayout) {

			if (((VerticalLayout) tab.getComponent()).getData().equals(GermplasmDetail.SECOND_TAB)) {
				if (this.layoutNames.getComponentCount() == 0) {
					this.layoutNames.addComponent(new GermplasmNamesComponent(this.dataIndexContainer, this.gDetailModel));
					this.layoutNames.setMargin(true);
				}
			} else if (((VerticalLayout) tab.getComponent()).getData().equals(GermplasmDetail.THIRD_TAB)) {
				if (this.layoutAttributes.getComponentCount() == 0) {
					this.layoutAttributes.addComponent(new GermplasmAttributesComponent(this.dataIndexContainer, this.gDetailModel));
					this.layoutAttributes.setMargin(true);
				}
			} else if (((VerticalLayout) tab.getComponent()).getData().equals(GermplasmDetail.FOURTH_TAB)) {
				if (this.layoutGenerationHistory.getComponentCount() == 0) {
					this.layoutGenerationHistory.addComponent(new GermplasmGenerationHistoryComponent(this.dataIndexContainer,
							this.gDetailModel));
					this.layoutGenerationHistory.setMargin(true);
				}
			} else if (((VerticalLayout) tab.getComponent()).getData().equals(GermplasmDetail.FIFTH_TAB)) {
				if (this.layoutPedigreeTree.getComponentCount() == 0) {

					this.btnViewPedigreeGraph = new Button("View Pedigree Graph");
					this.btnViewPedigreeGraph.setData(GermplasmDetail.VIEW_PEDIGREE_GRAPH_ID);
					this.btnViewPedigreeGraph.addListener(new GermplasmButtonClickListener(this));
					this.layoutPedigreeTree.addComponent(this.btnViewPedigreeGraph);

					final HorizontalLayout derivativeHorizontalLayout = new HorizontalLayout();
					derivativeHorizontalLayout.setMargin(true);
					derivativeHorizontalLayout.setSpacing(true);
					this.pedigreeDerivativeCheckbox = new CheckBox();
					this.pedigreeDerivativeCheckbox.setCaption("Include Derivative Lines");
					this.pedigreeDerivativeCheckbox.setData(GermplasmDetail.INCLUDE_DERIVATIVE_LINES);
					derivativeHorizontalLayout.addComponent(this.pedigreeDerivativeCheckbox);

					final Button refreshButton = new Button("Apply");
					refreshButton.setData(GermplasmDetail.REFRESH_BUTTON_ID);
					refreshButton.addListener(new GermplasmButtonClickListener(this));
					refreshButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
					derivativeHorizontalLayout.addComponent(refreshButton);

					this.layoutPedigreeTree.addComponent(derivativeHorizontalLayout);
					this.initializeTree();

					this.germplasmPedigreeTreeComponent =
							new GermplasmPedigreeTreeComponent(this.gid, this.qQuery, this.dataResultIndexContainer, this.mainLayout,
									this.tabSheet, (Boolean) this.pedigreeDerivativeCheckbox.getValue());

					this.treeDisplayLayout.addComponent(this.germplasmPedigreeTreeComponent);

					try {
						final String label =
								this.qQuery.getPedigreeLevelCountLabel(this.gid, (Boolean) this.pedigreeDerivativeCheckbox.getValue());

						this.levelLabel = new Label(label);
						this.treeDisplayLayout.addComponent(this.levelLabel);
					} catch (final MiddlewareQueryException e) {
						e.printStackTrace();
					}
					this.layoutPedigreeTree.addComponent(this.treeDisplayLayout);
				}
			} else if (((VerticalLayout) tab.getComponent()).getData().equals(GermplasmDetail.SIX_TAB)) {
				if (this.layoutGermplasmList.getComponentCount() == 0) {
					this.layoutGermplasmList.addComponent(new GermplasmListComponent(this.gid, this.fromUrl));
					this.layoutGermplasmList.setMargin(true);
				}
			} else if (((VerticalLayout) tab.getComponent()).getData().equals(GermplasmDetail.SEVEN_TAB)) {
				if (this.layoutGroupRelatives.getComponentCount() == 0) {
					this.layoutGroupRelatives.addComponent(new GermplasmGroupRelativesComponent(this.gid));
					this.layoutGroupRelatives.setMargin(true);
				}
			} else if (((VerticalLayout) tab.getComponent()).getData().equals(GermplasmDetail.EIGHT_TAB)) {
				if (this.layoutManagementNeighbors.getComponentCount() == 0) {
					this.layoutManagementNeighbors.addComponent(new GermplasmManagementNeighborsComponent(this.gid));
					this.layoutManagementNeighbors.setMargin(true);
				}
			} else if (((VerticalLayout) tab.getComponent()).getData().equals(GermplasmDetail.NINE_TAB)) {
				if (this.layoutDerivativeNeighborhood.getComponentCount() == 0) {
					this.layoutDerivativeNeighborhood.addComponent(new GermplasmDerivativeNeighborhoodComponent(this.gid, this.qQuery,
							this.dataResultIndexContainer, this.mainLayout, this.tabSheet));
				}
			} else if (((VerticalLayout) tab.getComponent()).getData().equals(GermplasmDetail.TEN_TAB)) {
				if (this.layoutInventoryInformation.getComponentCount() == 0) {
					this.layoutInventoryInformation.addComponent(new InventoryViewComponent(null, null, this.gid));
					this.layoutInventoryInformation.setMargin(true);
				}
			} else if (((VerticalLayout) tab.getComponent()).getData().equals(GermplasmDetail.ELEVEN_TAB)) {
				if (this.layoutGermplasmStudyInformation.getComponentCount() == 0) {
					this.layoutGermplasmStudyInformation.addComponent(new GermplasmStudyInfoComponent(this.dataIndexContainer,
							this.gDetailModel, this.fromUrl));
					this.layoutGermplasmStudyInformation.setMargin(true);
				}
			} else if (((VerticalLayout) tab.getComponent()).getData().equals(GermplasmDetail.TWELVE_TAB)) {
				if (this.layoutMaintenanceNeighborhood.getComponentCount() == 0) {
					this.layoutMaintenanceNeighborhood.addComponent(new GermplasmMaintenanceNeighborhoodComponent(this.gid, this.qQuery,
							this.dataResultIndexContainer, this.mainLayout, this.tabSheet));
				}
			}
		}
	}

	private void initializeTree() {
		this.treeDisplayLayout = new GridLayout(2, 1);
		this.treeDisplayLayout.setMargin(true);
		this.treeDisplayLayout.setSpacing(true);
		this.treeDisplayLayout.setHeight("100%");
	}

	@Override
	public void afterPropertiesSet() {

		try {
			if (this.germplasmManager.getGermplasmByGID(this.gid) != null) {
				this.renderGermplasmDetailsAccordion();
			} else {
				final VerticalLayout layout = new VerticalLayout();
				layout.setMargin(true);
				final Label label = new Label(this.messageSource.getMessage(Message.NULL_GERMPLASM_DETAILS) + " " + this.gid);
				layout.addComponent(label);

				this.addTab(layout, this.messageSource.getMessage(Message.CHARACTERISTICS_LABEL));
			}

		} catch (final MiddlewareQueryException e) {
			e.printStackTrace();
			MessageNotifier.showError(this.getWindow(), "Error with viewing Germplasm",
					e.getMessage() + ". " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}
	}

	private void renderGermplasmDetailsAccordion() {
		this.layoutDetails = new VerticalLayout();
		this.layoutDetails.setData(GermplasmDetail.FIRST_TAB);
		this.gDetailModel = this.qQuery.getGermplasmDetails(this.gid);
		this.layoutDetails.addComponent(new GermplasmCharacteristicsComponent(this.gDetailModel));

		this.layoutDetails.setMargin(true);

		this.layoutNames = new VerticalLayout();
		this.layoutNames.setData(GermplasmDetail.SECOND_TAB);

		this.layoutAttributes = new VerticalLayout();
		this.layoutAttributes.setData(GermplasmDetail.THIRD_TAB);

		this.layoutGenerationHistory = new VerticalLayout();
		this.layoutGenerationHistory.setData(GermplasmDetail.FOURTH_TAB);

		this.layoutPedigreeTree = new VerticalLayout();
		this.layoutPedigreeTree.setData(GermplasmDetail.FIFTH_TAB);

		this.layoutGermplasmList = new VerticalLayout();
		this.layoutGermplasmList.setData(GermplasmDetail.SIX_TAB);

		this.layoutGroupRelatives = new VerticalLayout();
		this.layoutGroupRelatives.setData(GermplasmDetail.SEVEN_TAB);

		this.layoutManagementNeighbors = new VerticalLayout();
		this.layoutManagementNeighbors.setData(GermplasmDetail.EIGHT_TAB);

		this.layoutDerivativeNeighborhood = new VerticalLayout();
		this.layoutDerivativeNeighborhood.setData(GermplasmDetail.NINE_TAB);

		this.layoutInventoryInformation = new VerticalLayout();
		this.layoutInventoryInformation.setData(GermplasmDetail.TEN_TAB);

		this.layoutGermplasmStudyInformation = new VerticalLayout();
		this.layoutGermplasmStudyInformation.setData(GermplasmDetail.ELEVEN_TAB);

		this.layoutMaintenanceNeighborhood = new VerticalLayout();
		this.layoutMaintenanceNeighborhood.setData(GermplasmDetail.TWELVE_TAB);

		this.layoutPedigreeTree.setMargin(true);

		this.addTab(this.layoutDetails, this.messageSource.getMessage(Message.CHARACTERISTICS_LABEL));
		this.addTab(this.layoutNames, this.messageSource.getMessage(Message.NAMES_LABEL));
		this.addTab(this.layoutAttributes, this.messageSource.getMessage(Message.ATTRIBUTES_LABEL));
		this.addTab(this.layoutGenerationHistory, this.messageSource.getMessage(Message.GENERATION_HISTORY_LABEL));
		this.addTab(this.layoutPedigreeTree, this.messageSource.getMessage(Message.PEDIGREE_TREE_LABEL));
		this.addTab(this.layoutGermplasmList, this.messageSource.getMessage(Message.LISTS_LABEL));
		this.addTab(this.layoutGroupRelatives, this.messageSource.getMessage(Message.GROUP_RELATIVES_LABEL));
		this.addTab(this.layoutManagementNeighbors, this.messageSource.getMessage(Message.MANAGEMENT_NEIGHBORS_LABEL));
		this.addTab(this.layoutDerivativeNeighborhood, this.messageSource.getMessage(Message.DERIVATIVE_NEIGHBORHOOD_LABEL));
		this.addTab(this.layoutMaintenanceNeighborhood, this.messageSource.getMessage(Message.MAINTENANCE_NEIGHBORHOOD_LABEL));
		this.addTab(this.layoutInventoryInformation, this.messageSource.getMessage(Message.INVENTORY_INFORMATION_LABEL));
		this.addTab(this.layoutGermplasmStudyInformation, this.messageSource.getMessage(Message.GERMPLASM_STUDY_INFORMATION_LABEL));

		this.addListener(new GermplasmSelectedTabChangeListener(this));
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		// currently does nothing, place code here if we want to update some labels
	}

	public void viewPedigreeGraphClickAction() throws InternationalizableException {

		try {
			final Window pedigreeGraphWindow = new BaseSubWindow("Pedigree Graph");
			pedigreeGraphWindow.setModal(true);
			pedigreeGraphWindow.setWidth("100%");
			pedigreeGraphWindow.setHeight("620px");
			pedigreeGraphWindow.setName("Pedigree Graph");
			pedigreeGraphWindow.addStyleName(Reindeer.WINDOW_LIGHT);
			pedigreeGraphWindow.addComponent(new GermplasmPedigreeGraphComponent(this.gid, this.qQuery));
			this.getWindow().addWindow(pedigreeGraphWindow);

		} catch (final Exception e) {
			throw new InternationalizableException(e, Message.ERROR_IN_SEARCH, Message.EMPTY_STRING);
		}
	}

	public void refreshPedigreeTree() {
		this.layoutPedigreeTree.removeComponent(this.treeDisplayLayout);

		if (this.germplasmPedigreeTreeComponent != null) {
			this.treeDisplayLayout.removeComponent(this.germplasmPedigreeTreeComponent);
			this.treeDisplayLayout.removeComponent(this.levelLabel);
		}
		this.initializeTree();
		this.germplasmPedigreeTreeComponent =
				new GermplasmPedigreeTreeComponent(this.gid, this.qQuery, this.dataResultIndexContainer, this.mainLayout, this.tabSheet,
						(Boolean) this.pedigreeDerivativeCheckbox.getValue());
		this.treeDisplayLayout.addComponent(this.germplasmPedigreeTreeComponent);

		try {

			final String label = this.qQuery.getPedigreeLevelCountLabel(this.gid, (Boolean) this.pedigreeDerivativeCheckbox.getValue());

			this.levelLabel = new Label(label);
			this.treeDisplayLayout.addComponent(this.levelLabel);
		} catch (final MiddlewareQueryException e) {
			e.printStackTrace();
		}

		this.layoutPedigreeTree.addComponent(this.treeDisplayLayout);
	}

}
