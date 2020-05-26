
package org.generationcp.breeding.manager.crossingmanager.settings;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.CrossesMadeContainer;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMakeCrossesComponent;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.commons.help.document.HelpButton;
import org.generationcp.commons.help.document.HelpModule;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.HeaderLabelLayout;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ManageCrossingSettingsMain extends VerticalLayout
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, CrossesMadeContainer {

	private static final long serialVersionUID = 1L;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private StudyDataManager studyDataManager;

	private Label toolTitle;
	private Label designCrossesHeaderLabel;

	private CrossingManagerMakeCrossesComponent makeCrossesComponent;
	private TabSheet tabSheet;

	private CrossesMade crossesMade = new CrossesMade();
	private final ComponentContainer parent;

	private GermplasmList germplasmList = null;
	private Integer studyId = null;

	public ManageCrossingSettingsMain(final ComponentContainer parent) {
		this.parent = parent;
	}

	public ManageCrossingSettingsMain(final ComponentContainer parent, final Integer studyId) {
		this(parent);
		this.studyId = studyId;
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.toolTitle = new Label(this.messageSource.getMessage(Message.DESIGN_CROSSES));
		this.toolTitle.setDebugId("toolTitle");
		this.toolTitle.setStyleName(Bootstrap.Typography.H1.styleName());
		this.toolTitle.setWidth("230px");

		this.designCrossesHeaderLabel = new Label(this.messageSource.getMessage(Message.DESIGN_CROSSES_HEADER));
		this.designCrossesHeaderLabel.setDebugId("designCrossesHeaderLabel");
		this.designCrossesHeaderLabel.setWidth("900px");

		this.tabSheet = new TabSheet();
		this.tabSheet.setDebugId("tabSheet");
		this.tabSheet.hideTabs(true);
		this.tabSheet.setHeight("1600px");
		this.tabSheet.setWidth("100%");
		this.tabSheet.addStyleName(AppConstants.CssStyles.TABSHEET_WHITE);

		this.makeCrossesComponent = new CrossingManagerMakeCrossesComponent(this);
		this.makeCrossesComponent.setDebugId("makeCrossesComponent");
		if (this.studyId != null) {
			// If the germplasm list is coming from a study, set the list name value to study name.
			final String studyName = this.studyDataManager.getStudyDetails(studyId).getStudyName();
			this.makeCrossesComponent.getSelectParentsComponent().createListDetailsTab(this.studyId, null,
				studyName);
		}

		this.tabSheet.addTab(this.makeCrossesComponent);
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
		this.setMargin(false, false, false, true);

		final HorizontalLayout headingLayout = new HorizontalLayout();
		headingLayout.setDebugId("headingLayout");
		headingLayout.setSpacing(true);
		headingLayout.setHeight("40px");
		headingLayout.addComponent(this.toolTitle);
		headingLayout.addComponent(new HelpButton(HelpModule.DESIGN_CROSSES, "View Design Crosses tutorial"));

		final HeaderLabelLayout subHeaderLabel = new HeaderLabelLayout(null, this.designCrossesHeaderLabel);
		subHeaderLabel.setDebugId("subHeaderLabel");

		this.addComponent(headingLayout);
		this.addComponent(this.designCrossesHeaderLabel);
		this.addComponent(this.tabSheet);
	}

	public void reset() {

		if (this.parent.getWindow() != null) {
			this.parent.getWindow().setScrollTop(0);
		}

		this.parent.removeAllComponents();

		final ManageCrossingSettingsMain crossingManagerMain = new ManageCrossingSettingsMain(this.parent);
		crossingManagerMain.setDebugId("crossingManagerMain");

		// remove the redundant left margin after reloading the choose setting page
		crossingManagerMain.setMargin(false);

		this.parent.addComponent(crossingManagerMain);
	}

	@Override
	public CrossesMade getCrossesMade() {
		return this.crossesMade;
	}

	@Override
	public void setCrossesMade(final CrossesMade crossesMade) {
		this.crossesMade = crossesMade;
	}

	public CrossingManagerMakeCrossesComponent getMakeCrossesComponent() {
		return this.makeCrossesComponent;
	}

	public CrossSetting compileCurrentSetting() {
		final CrossSetting setting = new CrossSetting();
		setting.setCrossNameSetting(new CrossNameSetting());
		//Set the additional details values to 0 and empty string, proper values will be set in Fieldbook
		final AdditionalDetailsSetting additionalDetails = new AdditionalDetailsSetting(0, "");
		setting.setAdditionalDetailsSetting(additionalDetails);
		setting.setBreedingMethodSetting(this.makeCrossesComponent.getCurrentBreedingMethodSetting());

		return setting;
	}

}
