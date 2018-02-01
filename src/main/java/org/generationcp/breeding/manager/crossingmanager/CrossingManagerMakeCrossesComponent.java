
package org.generationcp.breeding.manager.crossingmanager;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.constants.CrossType;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.customcomponent.BreedingManagerWizardDisplay.StepChangeListener;
import org.generationcp.breeding.manager.customcomponent.LinkButton;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class CrossingManagerMakeCrossesComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
	BreedingManagerLayout, StepChangeListener {

	private static final int DEFAULT_BREEDING_METHOD_ID = 101;

	private static final Logger LOG = LoggerFactory.getLogger(CrossingManagerMakeCrossesComponent.class);

	private static final long serialVersionUID = 9097810121003895303L;
	public static final int BASED_ON_PARENTAGE = 0;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private final ManageCrossingSettingsMain source;

	private SelectParentsComponent selectParentsComponent;
	private MakeCrossesParentsComponent parentsComponent;
	private CrossingMethodComponent crossingMethodComponent;
	private MakeCrossesTableComponent crossesTableComponent;
	
	private LinkButton nurseryCancelButton;

	@Autowired
	private FieldbookService fieldbookMiddlewareService;

	private boolean isNavigatedFromNursery;
	private String nurseryId;
	private Workbook nurseryWorkbook = null;

	private Button nurseryBackButton;
	private final Button.ClickListener nurseryBackButtonDefaultClickListener = new Button.ClickListener() {

		/**
		 *
		 */
		private static final long serialVersionUID = -2946008623293356900L;

		@Override
		public void buttonClick(final Button.ClickEvent event) {
			Integer listId = CrossingManagerMakeCrossesComponent.this.crossesTableComponent.saveTemporaryList();
			CrossingManagerMakeCrossesComponent.this.sendToNurseryAction(listId);
		}
	};

	public CrossingManagerMakeCrossesComponent(final ManageCrossingSettingsMain manageCrossingSettingsMain) {
		this.source = manageCrossingSettingsMain;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.initializeNurseryContext(BreedingManagerUtil.getApplicationRequest());
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	void initializeNurseryContext(final HttpServletRequest currentRequest) {
		this.isNavigatedFromNursery = currentRequest.getPathInfo().contains(BreedingManagerApplication.NAVIGATION_FROM_NURSERY_PREFIX);
		if (this.isNavigatedFromNursery) {
			final String[] parameterValues = currentRequest.getParameterValues(BreedingManagerApplication.REQ_PARAM_NURSERY_ID);
			final String nurseryId = parameterValues != null && parameterValues.length > 0 ? parameterValues[0] : "";
			this.nurseryId = nurseryId;
			// Initialize the workbook.. this will be required later for seed source generation.
			if (!StringUtils.isBlank(this.nurseryId)) {
				this.nurseryWorkbook = this.fieldbookMiddlewareService.getNurseryDataSet(Integer.valueOf(this.nurseryId));
			}
		}
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		
	}

	/*
	 * Action handler for Make Cross button
	 */
	public void makeCrossButtonAction(
		final List<GermplasmListEntry> femaleList, final List<GermplasmListEntry> maleList,
		final String listnameFemaleParent, final String listnameMaleParent, final CrossType type, final boolean makeReciprocalCrosses,
		final boolean excludeSelf) {

		if (!femaleList.isEmpty() && !maleList.isEmpty()) {
			try {
				final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {

					@Override
					protected void doInTransactionWithoutResult(final TransactionStatus status) {
						// Female - Male Multiplication
						if (CrossType.MULTIPLY.equals(type)) {
							CrossingManagerMakeCrossesComponent.this.crossesTableComponent.multiplyParents(femaleList, maleList,
								listnameFemaleParent, listnameMaleParent, excludeSelf);
							if (makeReciprocalCrosses) {
								CrossingManagerMakeCrossesComponent.this.crossesTableComponent.multiplyParents(maleList, femaleList,
									listnameMaleParent, listnameFemaleParent, excludeSelf);
							}

							// Top to Bottom Crossing
						} else if (CrossType.TOP_TO_BOTTOM.equals(type)) {
							if (femaleList.size() == maleList.size()) {
								CrossingManagerMakeCrossesComponent.this.crossesTableComponent.makeTopToBottomCrosses(femaleList, maleList,
									listnameFemaleParent, listnameMaleParent, excludeSelf);
								if (makeReciprocalCrosses) {
									CrossingManagerMakeCrossesComponent.this.crossesTableComponent.makeTopToBottomCrosses(maleList,
										femaleList, listnameMaleParent, listnameFemaleParent, excludeSelf);
								}
							} else {
								MessageNotifier.showError(CrossingManagerMakeCrossesComponent.this.getWindow(),
									"Error with selecting parents.", CrossingManagerMakeCrossesComponent.this.messageSource
										.getMessage(Message.ERROR_MALE_AND_FEMALE_PARENTS_MUST_BE_EQUAL));
							}
						}
					}
				});

			} catch (final Throwable e) {
				CrossingManagerMakeCrossesComponent.LOG.error(e.getMessage(), e);
				MessageNotifier.showError(
					CrossingManagerMakeCrossesComponent.this.getWindow(),
					this.messageSource.getMessage(Message.ERROR),
					CrossingManagerMakeCrossesComponent.this.messageSource.getMessage(Message.ERROR_WITH_CROSSES_RETRIEVAL));
			}

			CrossingManagerMakeCrossesComponent.this
				.showNotificationAfterCrossing(CrossingManagerMakeCrossesComponent.this.crossesTableComponent.getTableCrossesMade()
					.size());

		} else {
			MessageNotifier.showError(CrossingManagerMakeCrossesComponent.this.getWindow(), "Error with selecting parents.",
				CrossingManagerMakeCrossesComponent.this.messageSource
					.getMessage(Message.AT_LEAST_ONE_FEMALE_AND_ONE_MALE_PARENT_MUST_BE_SELECTED));
		}

	}

	void showNotificationAfterCrossing(final int noOfCrosses) {
		if (noOfCrosses == 0) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
				this.messageSource.getMessage(Message.NO_CROSSES_GENERATED));
		}
	}

	public void toggleNurseryBackButton() {
		this.nurseryBackButton.setEnabled(this.isCrossListMade());
	}

	public void sendToNurseryAction(final Integer id) {
		// get the cancel button returning to nursery link as a root url
		final String urlToSpecificNurseryWithParams =
			CrossingManagerMakeCrossesComponent.this.nurseryCancelButton.getResource().getURL() + "?"
				+ BreedingManagerApplication.REQ_PARAM_CROSSES_LIST_ID + "=" + id;

		final ExternalResource urlToNursery = new ExternalResource(urlToSpecificNurseryWithParams);
		CrossingManagerMakeCrossesComponent.this.getWindow().open(urlToNursery, "_self");
	}

	private boolean isCrossListMade() {
		Table tableCrossesMade = this.crossesTableComponent.getTableCrossesMade();
		return tableCrossesMade != null && tableCrossesMade.size() > 0;
	}

	public void backButtonClickAction() {
		if (this.crossesTableComponent.getCrossList() != null) {
			MessageNotifier.showWarning(this.getWindow(), "Invalid Action", "Cannot change settings once crosses have been saved");
			return;
		}

	}

	@Override
	public void instantiateComponents() {
		this.selectParentsComponent = new SelectParentsComponent(this);
		this.selectParentsComponent.setDebugId("selectParentsComponent");
		this.parentsComponent = new MakeCrossesParentsComponent(this);
		this.parentsComponent.setDebugId("parentsComponent");
		this.crossingMethodComponent = new CrossingMethodComponent(this);
		this.crossingMethodComponent.setDebugId("crossingMethodComponent");
		this.crossesTableComponent = new MakeCrossesTableComponent(this);
		this.crossesTableComponent.setDebugId("crossesTableComponent");
		fieldbookMiddlewareService.loadAllObservations(nurseryWorkbook);
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
		this.setWidth("950px");

		final VerticalLayout sheetDesignCrosses = new VerticalLayout();
		sheetDesignCrosses.setSpacing(true);

		final HorizontalLayout layoutButtonArea = new HorizontalLayout();
		layoutButtonArea.setDebugId("layoutButtonArea");
		layoutButtonArea.setMargin(true, true, true, true);
		layoutButtonArea.setSpacing(true);

		this.nurseryCancelButton = this.constructNurseryCancelButton(BreedingManagerUtil.getApplicationRequest());
		this.nurseryBackButton = this.constructNurseryBackButton();
		this.nurseryBackButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.nurseryBackButton.setEnabled(false);
		layoutButtonArea.addComponent(this.nurseryCancelButton);
		layoutButtonArea.addComponent(this.nurseryBackButton);
	

		sheetDesignCrosses.addComponent(this.selectParentsComponent);
		sheetDesignCrosses.addComponent(this.parentsComponent);
		sheetDesignCrosses.addComponent(this.crossingMethodComponent);
		sheetDesignCrosses.addComponent(this.crossesTableComponent);
		sheetDesignCrosses.addComponent(layoutButtonArea);
		sheetDesignCrosses.setComponentAlignment(layoutButtonArea, Alignment.MIDDLE_CENTER);

		this.addComponent(sheetDesignCrosses);
		this.setStyleName("crosses-select-parents-tab");
	}

	protected Button constructNurseryBackButton() {
		final Button nurseryBackButton = new Button();
		nurseryBackButton.setDebugId("nurseryBackButton");
		nurseryBackButton.setCaption(this.messageSource.getMessage(Message.BACK_TO_NURSERY));
		nurseryBackButton.addListener(this.nurseryBackButtonDefaultClickListener);
		return nurseryBackButton;
	}

	LinkButton constructNurseryCancelButton(final HttpServletRequest currentRequest) {
		final ExternalResource urlToNursery;
		if (StringUtils.isBlank(this.nurseryId) || !NumberUtils.isDigits(this.nurseryId)) {
			urlToNursery = new ExternalResource(currentRequest.getScheme() + "://" + currentRequest.getServerName() + ":"
				+ currentRequest.getServerPort() + BreedingManagerApplication.PATH_TO_NURSERY);
		} else {
			urlToNursery = new ExternalResource(currentRequest.getScheme() + "://" + currentRequest.getServerName() + ":"
				+ currentRequest.getServerPort() + BreedingManagerApplication.PATH_TO_EDIT_NURSERY + this.nurseryId);
		}
		final LinkButton nurseryCancelButton = new LinkButton(urlToNursery, "");
		nurseryCancelButton.setDebugId("nurseryCancelButton");
		this.messageSource.setCaption(nurseryCancelButton, Message.CANCEL);
		return nurseryCancelButton;
	}

	boolean isNavigatedFromNursery() {
		return this.isNavigatedFromNursery;
	}

	public String getNurseryId() {
		return this.nurseryId;
	}

	// For test only
	void setNurseryId(final String nurseryId) {
		this.nurseryId = nurseryId;
	}

	public Workbook getNurseryWorkbook() {
		return this.nurseryWorkbook;
	}

	public void updateCrossesSeedSource(final String femaleListName, final String maleListName) {
		this.crossesTableComponent.updateSeedSource(femaleListName, maleListName);
	}

	private boolean doUpdateTable() {
		return !this.getSeparatorString().equals(this.crossesTableComponent.getSeparator());
	}

	@Override
	public void updatePage() {
		// only make updates to the page if separator was changed
		if (this.doUpdateTable() && this.crossesTableComponent.getCrossList() == null) {
			this.crossesTableComponent.updateSeparatorForCrossesMade();
		}
	}

	// SETTERS AND GETTERS
	public String getSeparatorString() {
		final CrossNameSetting crossNameSetting = this.getCurrentCrossingSetting().getCrossNameSetting();
		return crossNameSetting.getSeparator();
	}

	public CrossSetting getCurrentCrossingSetting() {
		return this.source.compileCurrentSetting();
	}

	public CrossesMadeContainer getCrossesMadeContainer() {
		return this.source;
	}

	public SelectParentsComponent getSelectParentsComponent() {
		return this.selectParentsComponent;
	}

	public void setSelectParentsComponent(final SelectParentsComponent selectParentsComponent) {
		this.selectParentsComponent = selectParentsComponent;
	}

	public MakeCrossesParentsComponent getParentsComponent() {
		return this.parentsComponent;
	}

	public void setParentsComponent(final MakeCrossesParentsComponent parentsComponent) {
		this.parentsComponent = parentsComponent;
	}

	public CrossingMethodComponent getCrossingMethodComponent() {
		return this.crossingMethodComponent;
	}

	public MakeCrossesTableComponent getCrossesTableComponent() {
		return this.crossesTableComponent;
	}

	public void setCrossesTableComponent(final MakeCrossesTableComponent crossesTableComponent) {
		this.crossesTableComponent = crossesTableComponent;
	}

	public Component getSource() {
		return this.source;
	}

	public void showNodeOnTree(final Integer listId) {
		final CrossingManagerListTreeComponent listTreeComponent = this.getSelectParentsComponent().getListTreeComponent();
		listTreeComponent.setListId(listId);
		listTreeComponent.createTree();
	}

	void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	public BreedingMethodSetting getCurrentBreedingMethodSetting() {
		//Return default breeding method id, the proper breeding method id will be set in Fieldbook 
		return new BreedingMethodSetting(DEFAULT_BREEDING_METHOD_ID, true, false);
	}

	void setNavigatedFromNursery(final boolean isNavigatedFromNursery) {
		this.isNavigatedFromNursery = isNavigatedFromNursery;
	}

	Button getNurseryBackButton() {
		return this.nurseryBackButton;
	}

	LinkButton getNurseryCancelButton() {
		return this.nurseryCancelButton;
	}
	
	void setNurseryCancelButton(LinkButton nurseryCancelButton) {
		this.nurseryCancelButton = nurseryCancelButton;
	}

	void setCrossingMethodComponent(final CrossingMethodComponent crossingMethodComponent) {
		this.crossingMethodComponent = crossingMethodComponent;
	}
}
