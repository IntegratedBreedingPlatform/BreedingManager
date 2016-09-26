
package org.generationcp.breeding.manager.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ReserveInventoryWindow extends BaseSubWindow implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = -5997291617886011653L;

	private VerticalLayout mainLayout;
	private Panel contentPanel;
	private VerticalLayout panelContentLayout;
	private Label singleScaleDescriptionLabel;
	private Label multiScaleDescriptionLabel;
	private Label differentUnits;
	private Label selectedCountLabel;
	private OptionGroup commitOption;
	private Button cancelButton;
	private Button finishButton;
	public int selectdCounts;
	private Label notesLabel;
	private TextField notes;
	private List<ReserveInventoryRowComponent> scaleRows;

	private Boolean isSingleScaled;

	private final ReserveInventorySource source;

	// Inputs
	private final Map<String, List<ListEntryLotDetails>> scaleGrouping;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private ReserveInventoryAction reserveInventoryAction;

	public ReserveInventoryWindow(final ReserveInventorySource source, final Map<String, List<ListEntryLotDetails>> scaleGrouping,
			final Boolean isSingleScaled) {
		super();
		this.source = source;
		this.isSingleScaled = isSingleScaled;
		this.scaleGrouping = scaleGrouping;
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
		// window formatting
		this.setCaption(this.messageSource.getMessage(Message.SEED_PREPARATION));
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setModal(true);

		// components formatting
		this.singleScaleDescriptionLabel = new Label("Specify the amount of seed you would like to prepare for each selected lot.");
		this.singleScaleDescriptionLabel.setDebugId("singleScaleDescriptionLabel");

		this.multiScaleDescriptionLabel = new Label("Specify the amount of seed you would like to prepare for each selected lot.");
		this.multiScaleDescriptionLabel.setDebugId("multiScaleDescriptionLabel");

		this.differentUnits = new Label("The lots you have selected are in different units."
				+ "Please specify the amount to reserve for each unit type.");
		this.differentUnits.setDebugId("differentUnits");

		this.contentPanel = new Panel();
		this.contentPanel.setDebugId("contentPanel");
		this.contentPanel.addStyleName("section_panel_layout");

		this.scaleRows = new ArrayList<ReserveInventoryRowComponent>();


		this.notesLabel = new Label(this.messageSource.getMessage(Message.NOTES));
		this.notesLabel.setDebugId("notesLabel");
		this.notesLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.notes = new TextField();
		this.notes.setDebugId("notes");
		this.notes.setWidth("400px");
		this.notes.setHeight("60px");


		this.commitOption = new OptionGroup();
		this.commitOption.setDebugId("commitOption");
		this.commitOption.setMultiSelect(true);
		this.commitOption.setWidth("300px");
		this.commitOption.addItem(this.messageSource.getMessage(Message.COMMIT_SEEDS));

		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.setDebugId("cancelButton");
		this.cancelButton.setWidth("80px");

		this.finishButton = new Button(this.messageSource.getMessage(Message.FINISH));
		this.finishButton.setDebugId("finishButton");
		this.finishButton.setWidth("80px");
		this.finishButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		this.initializeScaleRows();
	}

	private void initializeScaleRows() {

		for (final Map.Entry<String, List<ListEntryLotDetails>> entry : this.scaleGrouping.entrySet()) {
			final String scale = entry.getKey();
			final List<ListEntryLotDetails> lotDetailList = entry.getValue();
			this.selectdCounts=lotDetailList.size();
			this.scaleRows.add(new ReserveInventoryRowComponent(scale, lotDetailList.size()));
		}
	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new CloseWindowAction());

		this.reserveInventoryAction = new ReserveInventoryAction(this.source);

		this.finishButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				if (ReserveInventoryWindow.this.validateReserveAmount()) {
					ReserveInventoryWindow.this.reserveInventoryAction.validateReservations(ReserveInventoryWindow.this.getReservations());
				}
			}
		});
	}

	protected boolean validateReserveAmount() {
		try {

			for (final ReserveInventoryRowComponent row : this.scaleRows) {
				row.validate();
			}

			return true;

		} catch (final InvalidValueException e) {
			MessageNotifier.showRequiredFieldError(this.getWindow(), e.getMessage());
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {

		this.mainLayout = new VerticalLayout();
		this.mainLayout.setDebugId("reserveInventoryMainLayout");
		this.mainLayout.setSpacing(true);

		this.panelContentLayout = new VerticalLayout();
		this.panelContentLayout.setDebugId("panelContentLayout");
		this.panelContentLayout.setMargin(true);
		this.panelContentLayout.setSpacing(true);

		String selectedCountsText = " (" + this.selectdCounts + " selected)";
		this.selectedCountLabel = new Label(selectedCountsText);
		this.selectedCountLabel.setDebugId("selectedCountLabel");

		if (this.isSingleScaled) {
			this.setHeight("370px");
			this.setWidth("600px");

			this.contentPanel.setWidth("540px");
			this.contentPanel.setHeight("230px");

			String scaleFullText = this.singleScaleDescriptionLabel.toString() + this.selectedCountLabel.toString();
			Label scaleLabel = new Label(scaleFullText);
			scaleLabel.setDebugId("scaleLabel");
			this.panelContentLayout.addComponent(scaleLabel);
			this.panelContentLayout.addComponent(this.scaleRows.get(0));

		} else {
			this.setHeight("470px");
			this.setWidth("700px");

			this.contentPanel.setWidth("640px");
			this.contentPanel.setHeight("330px");

			String scaleFullTextForMultipleUnits = this.multiScaleDescriptionLabel.toString() + this.selectedCountLabel.toString();
			Label scaleLabelForMultipleUnits = new Label(scaleFullTextForMultipleUnits);
			scaleLabelForMultipleUnits.setDebugId("scaleLabelForMultipleUnits");
			this.panelContentLayout.addComponent(scaleLabelForMultipleUnits);
			this.panelContentLayout.addComponent(this.differentUnits);
			final VerticalLayout scaleLayout = new VerticalLayout();
			scaleLayout.setDebugId("scaleLayout");
			scaleLayout.setSpacing(true);
			scaleLayout.setHeight("90px");

			if (this.scaleRows.size() > 3) {
				scaleLayout.addStyleName(AppConstants.CssStyles.SCALE_ROW);
			}

			for (final ReserveInventoryRowComponent row : this.scaleRows) {
				scaleLayout.addComponent(row);
			}

			this.panelContentLayout.addComponent(scaleLayout);

		}

		this.panelContentLayout.addComponent(this.notesLabel);
		final HorizontalLayout notesTextArea = new HorizontalLayout();
		notesTextArea.setDebugId("notesTextArea");
		notesTextArea.setWidth("100%");
		notesTextArea.setSpacing(true);
		notesTextArea.addComponent(this.notesLabel);
		notesTextArea.addComponent(this.notes);
		notesTextArea.setComponentAlignment(this.notesLabel,Alignment.TOP_LEFT);
		notesTextArea.setComponentAlignment(this.notes,Alignment.TOP_CENTER);
		this.panelContentLayout.addComponent(notesTextArea);

		final HorizontalLayout commitCheck = new HorizontalLayout();
		commitCheck.setDebugId("commitCheck");
		commitCheck.setWidth("90%");
		commitCheck.setSpacing(true);
		commitCheck.addComponent(this.commitOption);
		commitCheck.setComponentAlignment(this.commitOption,Alignment.TOP_CENTER);
		this.panelContentLayout.addComponent(commitCheck);
		this.contentPanel.setLayout(this.panelContentLayout);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setWidth("100%");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.finishButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.finishButton, Alignment.BOTTOM_LEFT);

		this.mainLayout.addComponent(this.contentPanel);
		this.mainLayout.addComponent(buttonLayout);

		this.addComponent(this.mainLayout);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	protected Map<ReservationRowKey, List<ListEntryLotDetails>> getReservations() {
		final Map<ReservationRowKey, List<ListEntryLotDetails>> reservations = new HashMap<ReservationRowKey, List<ListEntryLotDetails>>();

		for (final ReserveInventoryRowComponent row : this.scaleRows) {
			reservations.put(new ReservationRowKey(row.getScale(), row.getReservationAmount()), this.scaleGrouping.get(row.getScale()));
		}

		return reservations;
	}

	// SETTERS AND GETTERS
	public Boolean getIsSingleScaled() {
		return this.isSingleScaled;
	}

	public void setIsSingleScaled(final Boolean isSingleScaled) {
		this.isSingleScaled = isSingleScaled;
	}

	public List<ReserveInventoryRowComponent> getScaleRows() {
		return this.scaleRows;
	}

	public void setScaleRows(final List<ReserveInventoryRowComponent> scaleRows) {
		this.scaleRows = scaleRows;
	}

}
