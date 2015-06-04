
package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class CrossingManagerSummaryComponent extends VerticalLayout implements BreedingManagerLayout, InitializingBean {

	private static final long serialVersionUID = 5812462719216001161L;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private Label summaryLabel;

	private TabSheet tabSheet;
	private VerticalLayout tabContentLayout;
	private CrossesSummaryListDataComponent crossListComponent;
	private SummaryListHeaderComponent femaleDetailsComponent;
	private SummaryListHeaderComponent maleDetailsComponent;

	private HorizontalLayout settingsComponent;

	private Button doneButton;

	private final ManageCrossingSettingsMain crossingManagerMain;

	private final GermplasmList crossList;
	private final GermplasmList maleList;
	private final GermplasmList femaleList;
	private final CrossingManagerSetting setting;

	public CrossingManagerSummaryComponent(ManageCrossingSettingsMain crossingManagerMain, GermplasmList crossList,
			GermplasmList femaleList, GermplasmList maleList, CrossingManagerSetting setting) {
		this.crossingManagerMain = crossingManagerMain;
		this.crossList = crossList;
		this.maleList = maleList;
		this.femaleList = femaleList;
		this.setting = setting;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();

		this.crossListComponent.focus();
	}

	@Override
	public void instantiateComponents() {
		this.summaryLabel = new Label(this.messageSource.getMessage(Message.SUMMARY));
		this.summaryLabel.addStyleName(Bootstrap.Typography.H4.styleName());
		this.summaryLabel.addStyleName(AppConstants.CssStyles.BOLD);

		// use tabsheet for styling purposes only
		this.tabSheet = new TabSheet();
		this.tabSheet.hideTabs(true);
		this.tabContentLayout = new VerticalLayout();

		this.crossListComponent = new CrossesSummaryListDataComponent(this.crossList);
		this.femaleDetailsComponent =
				new SummaryListHeaderComponent(this.femaleList, this.messageSource.getMessage(Message.FEMALE_PARENT_LIST_DETAILS));
		this.maleDetailsComponent =
				new SummaryListHeaderComponent(this.maleList, this.messageSource.getMessage(Message.MALE_PARENT_LIST_DETAILS));
		this.settingsComponent = new CrossesSummarySettingsComponent(this.setting);

		this.doneButton = new Button(this.messageSource.getMessage(Message.DONE));
		this.doneButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());

	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		this.doneButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -3416641468145860085L;

			@Override
			public void buttonClick(ClickEvent event) {
				CrossingManagerSummaryComponent.this.doneButtonClickAction();

			}
		});
	}

	@Override
	public void layoutComponents() {
		this.setSpacing(true);

		this.layoutSummaryPageContent();

		this.addComponent(this.summaryLabel);
		this.addComponent(this.tabSheet);
	}

	private void layoutSummaryPageContent() {
		HorizontalLayout parentsLayout = new HorizontalLayout();
		parentsLayout.setSpacing(true);
		parentsLayout.setHeight("130px");
		parentsLayout.setWidth("100%");
		parentsLayout.addComponent(this.femaleDetailsComponent);
		parentsLayout.addComponent(this.maleDetailsComponent);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");
		buttonLayout.addComponent(this.doneButton);
		buttonLayout.setComponentAlignment(this.doneButton, Alignment.MIDDLE_CENTER);

		VerticalLayout spacingLayout = new VerticalLayout();
		spacingLayout.setHeight("5px");

		this.tabContentLayout.setSpacing(true);
		this.tabContentLayout.setMargin(true);
		this.tabContentLayout.addComponent(this.crossListComponent);

		// for spacing only
		this.tabContentLayout.addComponent(spacingLayout);
		this.tabContentLayout.addComponent(parentsLayout);
		this.tabContentLayout.addComponent(this.settingsComponent);
		this.tabContentLayout.addComponent(buttonLayout);

		this.tabSheet.setHeight("620px");
		this.tabSheet.addTab(this.tabContentLayout);
	}

	private void doneButtonClickAction() {
		this.crossingManagerMain.reset();
	}

}
