
package org.generationcp.breeding.manager.germplasm;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.util.MaxPedigreeLevelReachedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmPedigreeTreeContainer extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 6008211670158416642L;

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmPedigreeTreeContainer.class);

	private Button viewGraphButton;
	private Button displayFullPedigreeButton;
	private CheckBox includeDerivativeLinesCheckbox;
	private Button refreshButton;
	private GermplasmPedigreeTreeComponent pedigreeTree;

	private final Integer gid;
	private final GermplasmQueries germplasmQueries;
	private final GermplasmDetailsComponentTree parent;
	private Integer pedigreeLevelCount;
	boolean maxReached = false;
	private GridLayout treeLayout;
	private Label levelLabel;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmPedigreeTreeContainer(final Integer gid, final GermplasmQueries germplasmQueries,
			final GermplasmDetailsComponentTree parent) {
		this.gid = gid;
		this.germplasmQueries = germplasmQueries;
		this.parent = parent;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.initializeComponents();
		this.addListeners();
		this.layoutComponents();
	}

	private void initializeComponents() {
		this.viewGraphButton = new Button("View Pedigree Graph");
		this.displayFullPedigreeButton = new Button("Display Full Pedigree");

		this.includeDerivativeLinesCheckbox = new CheckBox();
		this.includeDerivativeLinesCheckbox.setCaption("Include Derivative Lines");

		this.refreshButton = new Button("Apply");
		this.refreshButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.pedigreeTree = new GermplasmPedigreeTreeComponent(this.gid, this.germplasmQueries,
				new GermplasmIndexContainer(this.germplasmQueries), null, null, false);

		this.updatePedigreeLevelCount(false);
	}

	private void updatePedigreeLevelCount(final Boolean includeDerivativeLines) {
		this.maxReached = false;
		try {
			this.pedigreeLevelCount = this.germplasmQueries.getPedigreeLevelCount(this.gid, includeDerivativeLines);
			this.displayFullPedigreeButton.setVisible(false);
		} catch (final MiddlewareQueryException e) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_PEDIGREE_LEVEL));
			GermplasmPedigreeTreeContainer.LOG.error(e.getMessage());
		} catch (final MaxPedigreeLevelReachedException e) {
			this.pedigreeLevelCount = PedigreeDataManager.MAX_PEDIGREE_LEVEL;
			this.displayFullPedigreeButton.setVisible(true);
			this.maxReached = true;
		}
	}

	private void addListeners() {
		this.refreshButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 5303561767433976952L;

			@Override
			public void buttonClick(final ClickEvent event) {
				GermplasmPedigreeTreeContainer.this.refreshPedigreeTree();
			}
		});

		this.viewGraphButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 2714058007154924277L;

			@Override
			public void buttonClick(final ClickEvent event) {
				GermplasmPedigreeTreeContainer.this.parent.showPedigreeGraphWindow();
			}
		});

		this.displayFullPedigreeButton.addListener(new Button.ClickListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = -3484890478816054068L;

			@Override
			public void buttonClick(final ClickEvent clickEvent) {
				GermplasmPedigreeTreeContainer.this.updatePedigreeCountLabel();
			}
		});
	}

	protected void updatePedigreeCountLabel() {
		this.treeLayout.removeComponent(this.pedigreeTree);
		this.treeLayout.removeComponent(this.levelLabel);
		this.removeComponent(this.treeLayout);

		this.initializeTree();
		try {
			this.pedigreeTree = new GermplasmPedigreeTreeComponent(this.gid, this.germplasmQueries,
					new GermplasmIndexContainer(this.germplasmQueries), null, null, this.includeDerivativeLinesCheckbox.booleanValue());
			this.treeLayout.addComponent(this.pedigreeTree);
			
			final String labelValue =
					this.germplasmQueries.getPedigreeLevelCountLabel(this.gid, this.includeDerivativeLinesCheckbox.booleanValue(), true);
			
			this.levelLabel = new Label(labelValue);
			this.treeLayout.addComponent(this.levelLabel);

			this.addComponent(this.treeLayout);
			this.displayFullPedigreeButton.setVisible(false);
		} catch (final MiddlewareQueryException e) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_PEDIGREE_LEVEL));
			GermplasmPedigreeTreeContainer.LOG.error(e.getMessage());
		}
	}

	private void layoutComponents() {
		final HorizontalLayout includeDerivativeLinesOptionLayout = new HorizontalLayout();
		includeDerivativeLinesOptionLayout.setMargin(true);
		includeDerivativeLinesOptionLayout.setSpacing(true);
		includeDerivativeLinesOptionLayout.addComponent(this.includeDerivativeLinesCheckbox);
		includeDerivativeLinesOptionLayout.addComponent(this.refreshButton);
		includeDerivativeLinesOptionLayout.addComponent(this.viewGraphButton);
		includeDerivativeLinesOptionLayout.addComponent(this.displayFullPedigreeButton);
		this.addComponent(includeDerivativeLinesOptionLayout);

		this.initializeTree();

		this.treeLayout.addComponent(this.pedigreeTree);

		this.initLevelLabel();

		this.addComponent(this.treeLayout);
	}

	private void initLevelLabel() {
		String label = "";

		if (this.maxReached) {
			label = this.pedigreeLevelCount + "+ generations";
		} else if (this.pedigreeLevelCount > 1) {
			label = this.pedigreeLevelCount + " generations";
		} else {
			label = this.pedigreeLevelCount + " generation";
		}

		this.levelLabel = new Label(label);
		this.treeLayout.addComponent(this.levelLabel);
	}

	private void initializeTree() {
		this.treeLayout = new GridLayout(2, 1);
		this.treeLayout.setMargin(true);
		this.treeLayout.setSpacing(true);
		this.treeLayout.setHeight("100%");
	}

	void refreshPedigreeTree() {
		this.treeLayout.removeComponent(this.pedigreeTree);
		this.treeLayout.removeComponent(this.levelLabel);
		this.removeComponent(this.treeLayout);

		this.initializeTree();

		this.pedigreeTree = new GermplasmPedigreeTreeComponent(this.gid, this.germplasmQueries,
				new GermplasmIndexContainer(this.germplasmQueries), null, null, this.includeDerivativeLinesCheckbox.booleanValue());
		this.treeLayout.addComponent(this.pedigreeTree);

		try {
			this.updatePedigreeLevelCount(this.includeDerivativeLinesCheckbox.booleanValue());
			this.initLevelLabel();
			this.addComponent(this.treeLayout);
		} catch (final MiddlewareQueryException e) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_PEDIGREE_LEVEL));
			GermplasmPedigreeTreeContainer.LOG.error(e.getMessage());
		}
	}

	@Override
	public void updateLabels() {

	}

	/* For Test Purposes */
	public Button getDisplayFullPedigreeButton() {
		return this.displayFullPedigreeButton;
	}
}
