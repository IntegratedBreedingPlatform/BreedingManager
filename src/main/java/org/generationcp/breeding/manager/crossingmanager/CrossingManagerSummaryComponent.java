package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
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
public class CrossingManagerSummaryComponent extends VerticalLayout implements
		BreedingManagerLayout, InitializingBean {

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
	
	private ManageCrossingSettingsMain crossingManagerMain;
	
	private GermplasmList crossList;
	private GermplasmList maleList;
	private GermplasmList femaleList;
	private CrossingManagerSetting setting;
	
	public CrossingManagerSummaryComponent(ManageCrossingSettingsMain crossingManagerMain, GermplasmList crossList, 
			GermplasmList femaleList, GermplasmList maleList, CrossingManagerSetting setting){
		this.crossingManagerMain = crossingManagerMain;
		this.crossList = crossList;
		this.maleList = maleList;
		this.femaleList = femaleList;
		this.setting = setting;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		summaryLabel = new Label(messageSource.getMessage(Message.SUMMARY));
		summaryLabel.addStyleName(Bootstrap.Typography.H4.styleName());
		summaryLabel.addStyleName(AppConstants.CssStyles.BOLD);
		
		//use tabsheet for styling purposes only
		tabSheet = new TabSheet();
		tabSheet.hideTabs(true);
		tabContentLayout = new VerticalLayout();
		
		crossListComponent = new CrossesSummaryListDataComponent(tabContentLayout, crossList);
		femaleDetailsComponent = new SummaryListHeaderComponent(femaleList, 
				messageSource.getMessage(Message.FEMALE_PARENT_LIST_DETAILS));
		maleDetailsComponent = new SummaryListHeaderComponent(maleList, 
				messageSource.getMessage(Message.MALE_PARENT_LIST_DETAILS));
		settingsComponent = new CrossesSummarySettingsComponent(setting);
		
		doneButton = new Button(messageSource.getMessage(Message.DONE));
		doneButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}


	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
		doneButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -3416641468145860085L;

			@Override
			public void buttonClick(ClickEvent event) {
				doneButtonClickAction();
				
			}
		});
	}

	@Override
	public void layoutComponents() {
		setSpacing(true);

		layoutSummaryPageContent();

		addComponent(summaryLabel);
		addComponent(tabSheet);
	}

	private void layoutSummaryPageContent() {
		HorizontalLayout parentsLayout = new HorizontalLayout();
		parentsLayout.setHeight("130px");
		parentsLayout.setWidth("100%");
		parentsLayout.addComponent(femaleDetailsComponent);
		parentsLayout.addComponent(maleDetailsComponent);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");
		buttonLayout.addComponent(doneButton);
		buttonLayout.setComponentAlignment(doneButton, Alignment.MIDDLE_CENTER);
		
		VerticalLayout spacingLayout = new VerticalLayout();
		spacingLayout.setHeight("25px");
		
		tabContentLayout.setSpacing(true);
		tabContentLayout.setMargin(true);
		tabContentLayout.addComponent(crossListComponent);
		tabContentLayout.addComponent(spacingLayout); // for spacing only
		tabContentLayout.addComponent(parentsLayout);
		tabContentLayout.addComponent(settingsComponent);
		tabContentLayout.addComponent(buttonLayout);
		
		tabSheet.setHeight("590px");
		tabSheet.addTab(tabContentLayout);
	}

	
	private void doneButtonClickAction(){
		ConfirmDialog.show(this.getWindow(), messageSource.getMessage(Message.MAKE_NEW_CROSSES), 
	            messageSource.getMessage(Message.CONFIRM_REDIRECT_TO_MAKE_CROSSES_WIZARD), 
	            messageSource.getMessage(Message.OK), messageSource.getMessage(Message.CANCEL_LABEL), 
	            new ConfirmDialog.Listener() {
					private static final long serialVersionUID = 1L;

					public void onClose(ConfirmDialog dialog) {
	                    if (dialog.isConfirmed()) {
	                        crossingManagerMain.reset();
	                    }
	                }
	            }
	        );
		
	}


}
