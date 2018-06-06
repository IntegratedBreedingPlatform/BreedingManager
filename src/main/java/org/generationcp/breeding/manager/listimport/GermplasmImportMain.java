package org.generationcp.breeding.manager.listimport;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.BreedingManagerWizardDisplay.StepChangeListener;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.help.document.HelpButton;
import org.generationcp.commons.help.document.HelpModule;
import org.generationcp.commons.security.AuthorizationUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmImportMain extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = -6656072296236475385L;
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final int NUMBER_OF_STEPS = 2;

	private final String[] wizardStepNames = new String[GermplasmImportMain.NUMBER_OF_STEPS];
	private final String[] tabHeights = new String[GermplasmImportMain.NUMBER_OF_STEPS];

	private GermplasmImportFileComponent importFileComponent;
	private SpecifyGermplasmDetailsComponent germplasmDetailsComponent;
	private GermplasmListImportWizardDisplay wizardDisplay;

	private final ComponentContainer parent;
	private GermplasmImportPopupSource popupSource;

	private TabSheet tabSheet;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private HorizontalLayout titleLayout;
	private Label toolTitle;

	private final Boolean viaToolURL;
	private boolean viaPopup;

	public GermplasmImportMain(final ComponentContainer parent, final boolean viaToolURL) {
		this.parent = parent;
		this.viaToolURL = viaToolURL;
	}

	public GermplasmImportMain(final ComponentContainer parent, final boolean viaToolURL, final boolean viaPopup) {
		this.parent = parent;
		this.viaToolURL = viaToolURL;
		this.viaPopup = viaPopup;
	}

	public GermplasmImportMain(final ComponentContainer parent, final boolean viaToolURL, final GermplasmImportPopupSource popupSource) {
		this.parent = parent;
		this.viaToolURL = viaToolURL;
		this.viaPopup = false;
		this.popupSource = popupSource;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public GermplasmImportFileComponent getWizardScreenOne() {
		return this.importFileComponent;
	}

	public SpecifyGermplasmDetailsComponent getWizardScreenTwo() {
		return this.germplasmDetailsComponent;
	}

	private void setTitleContent() {
		this.titleLayout = new HorizontalLayout();
		this.titleLayout.setDebugId("titleLayout");
		this.titleLayout.setSpacing(true);

		this.toolTitle = new Label(this.messageSource.getMessage(Message.IMPORT_GERMPLASM_LIST_TAB_LABEL));
		this.toolTitle.setDebugId("toolTitle");
		this.toolTitle.setStyleName(Bootstrap.Typography.H1.styleName());
		this.toolTitle.setContentMode(Label.CONTENT_XHTML);
		this.toolTitle.setWidth("268px");

		this.titleLayout.addComponent(this.toolTitle);
		this.titleLayout.addComponent(new HelpButton(HelpModule.IMPORT_GERMPLASM, "View Import Germplasm Tutorial"));
	}

	@Override
	public void instantiateComponents() {
		this.setMargin(false, false, true, true);
		this.setWidth("730px");
		this.addStyleName("lm-germplasm-import-main");

		this.setTitleContent();

		this.instantiateWizardDisplay();

		// use tab approach to display which step to display
		this.initializeWizardSteps();
	}

	protected void initializeWizardSteps() {
		this.tabSheet = new TabSheet();
		this.tabSheet.setDebugId("tabSheet");

		// tab names are not actually shown
		this.tabSheet.hideTabs(true);

		this.tabSheet.setHeight(this.tabHeights[0]);
		this.tabSheet.setWidth("100%");

		this.tabSheet.addStyleName(AppConstants.CssStyles.TABSHEET_WHITE);

		this.importFileComponent = new GermplasmImportFileComponent(this);
		this.importFileComponent.setDebugId("importFileComponent");
		this.germplasmDetailsComponent = new SpecifyGermplasmDetailsComponent(this, this.viaToolURL);
		this.germplasmDetailsComponent.setDebugId("germplasmDetailsComponent");

		this.tabSheet.addTab(this.importFileComponent, this.wizardStepNames[0]);
		this.tabSheet.addTab(this.germplasmDetailsComponent, this.wizardStepNames[1]);
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		// do nothing
	}

	@Override
	public void layoutComponents() {
		this.addComponent(this.titleLayout);

		this.addComponent(this.wizardDisplay);
		this.addComponent(this.tabSheet);
	}

	private void instantiateWizardDisplay() {
		this.wizardStepNames[0] = this.messageSource.getMessage(Message.CHOOSE_IMPORT_FILE);
		this.wizardStepNames[1] = this.messageSource.getMessage(Message.SPECIFY_GERMPLASM_DETAILS);

		this.tabHeights[0] = "300px";
		this.tabHeights[1] = "860px";

		this.wizardDisplay = new GermplasmListImportWizardDisplay(this.wizardStepNames);
		this.wizardDisplay.setDebugId("wizardDisplay");
	}

	private void showWizardStep(final int step) {
		final Tab tab = Util.getTabAlreadyExist(this.tabSheet, this.wizardStepNames[step]);
		if (tab != null) {
			final Component tabComponent = tab.getComponent();
			this.tabSheet.setSelectedTab(tabComponent);
			if (tabComponent instanceof StepChangeListener) {
				final StepChangeListener listener = (StepChangeListener) tabComponent;
				listener.updatePage();
			}
			this.tabSheet.setHeight(this.tabHeights[step]);

			if (this.getWindow() != null) {
				this.getWindow().setScrollTop(0);
			}
		}
	}

	public void nextStep() {
		final int step = this.wizardDisplay.nextStep();
		// if from upload to specify Germplasm Details step
		if (step == 1) {
			this.initializeSpecifyGermplasmDetailsPage();
		}
		this.showWizardStep(step);
	}

	public void backStep() {
		final int step = this.wizardDisplay.backStep();
		this.showWizardStep(step);
	}

	public void reset() {
		this.parent.removeAllComponents();
		this.parent.addComponent(new GermplasmImportMain(this.parent, this.viaToolURL, this.viaPopup));
	}

	private void initializeSpecifyGermplasmDetailsPage() {
		final GermplasmListUploader germplasmListUploader = this.importFileComponent.getGermplasmListUploader();
		if (this.germplasmDetailsComponent != null && germplasmListUploader != null
				&& germplasmListUploader.getImportedGermplasmList() != null) {

			final ImportedGermplasmList importedGermplasmList = germplasmListUploader.getImportedGermplasmList();
			final List<ImportedGermplasm> importedGermplasms = importedGermplasmList.getImportedGermplasm();

			this.germplasmDetailsComponent.setImportedGermplasms(importedGermplasms);
			this.germplasmDetailsComponent.setGermplasmListUploader(germplasmListUploader);

			this.germplasmDetailsComponent.initializeFromImportFile(importedGermplasmList);
		}
	}

	public boolean isViaPopup() {
		return this.viaPopup;
	}

	public GermplasmImportPopupSource getGermplasmImportPopupSource() {
		return this.popupSource;
	}

	public ComponentContainer getComponentContainer() {
		return this.parent;
	}

	public TabSheet getTabSheet() {
		return this.tabSheet;
	}

}
