package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.List;
import java.util.Locale;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.inventory.exception.CloseLotException;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import com.google.common.collect.Lists;

@Configurable
public class CloseLotDiscardInventoryConfirmDialog extends BaseSubWindow implements BreedingManagerLayout, InitializingBean,
		CloseLotDiscardInventoryListener, Window.CloseListener {

	public static final String WINDOW_NAME = "Close lot";

	private ListEntryLotDetails listEntryLotDetails;

	private VerticalLayout mainLayout;
	private Label confirmLabel;
	private Button yesButton;
	private Button noButton;
	private CheckBox applyAllCheckBox;
	private ListComponent source;
	private CloseLotDiscardInventoryAction closeLotDiscardInventoryAction;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public CloseLotDiscardInventoryConfirmDialog(ListComponent source, CloseLotDiscardInventoryAction closeLotDiscardInventoryAction,
			ListEntryLotDetails listEntryLotDetails) {
		this.listEntryLotDetails = listEntryLotDetails;
		this.source = source;
		this.closeLotDiscardInventoryAction = closeLotDiscardInventoryAction;
	}

	@Override
	public void windowClose(CloseEvent closeEvent) {
		super.close();
		this.closeLotDiscardInventoryAction.closeAllLotCloseListeners();
	}

	@Override
	protected void closeWindow() {
		super.closeWindow();
		this.closeLotDiscardInventoryAction.closeAllLotCloseListeners();
	}

	@Override
	protected void close() {
		super.close();
		this.closeLotDiscardInventoryAction.closeAllLotCloseListeners();
	}

	@Override
	public void instantiateComponents() {
		this.setModal(true);
		this.setCaption(CloseLotDiscardInventoryConfirmDialog.WINDOW_NAME);
		this.setStyleName(Reindeer.WINDOW_LIGHT);
		this.addStyleName("unsaved-changes-dialog");

		this.setWidth("544px");
		this.setHeight("180px");
		this.setResizable(false);

		this.center();

		this.yesButton = new Button(this.messageSource.getMessage(Message.YES));
		this.yesButton.setDebugId("yesButton");
		this.yesButton.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
		this.yesButton.setStyleName(Reindeer.BUTTON_DEFAULT);

		this.noButton = new Button(this.messageSource.getMessage(Message.NO));
		this.noButton.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
		this.noButton.setDebugId("noButton");

		String confirmText = this.messageSource
				.getMessage(Message.LOTS_HAVE_AVAILABLE_BALANCE_NO_UNCOMMITTED_RESERVATION_ERROR, this.listEntryLotDetails.getLotId());

		this.confirmLabel = new Label(confirmText, Label.CONTENT_RAW);
		this.confirmLabel.setDebugId("confirmLabelId");

		String applyToAllText = this.messageSource.getMessage(Message.APPLY_TO_ALL);
		this.applyAllCheckBox = new CheckBox(applyToAllText, false);
		this.applyAllCheckBox.setDebugId("applyToAllTextCheckBox");
		this.applyAllCheckBox.setData(this.listEntryLotDetails.getId());
		this.applyAllCheckBox.setImmediate(true);
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
		this.yesButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -8698652015248607854L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				CloseLotDiscardInventoryConfirmDialog.this.yesActionListener();
			}
		});

		this.noButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -8698652015248607854L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				CloseLotDiscardInventoryConfirmDialog.this.noActionListener();
			}
		});

	}

	@Override
	public void layoutComponents() {
		this.mainLayout = new VerticalLayout();
		this.mainLayout.setDebugId("closeLotDiscardInventoryMainLayout");
		this.mainLayout.setSpacing(true);

		this.mainLayout.addComponent(this.confirmLabel);

		final Label forSpaceLabel = new Label();
		forSpaceLabel.setDebugId("forSpaceLabel");
		this.mainLayout.addComponent(forSpaceLabel);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(this.applyAllCheckBox);
		buttonLayout.setComponentAlignment(this.applyAllCheckBox, Alignment.MIDDLE_LEFT);
		buttonLayout.addComponent(this.yesButton);
		buttonLayout.addComponent(this.noButton);

		this.mainLayout.addComponent(buttonLayout);
		this.mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);

		this.addComponent(this.mainLayout);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public void yesActionListener() {
		if (this.applyAllCheckBox.getValue().equals(Boolean.FALSE)) {
			try {
				this.source.processCloseLots(Lists.newArrayList(this.listEntryLotDetails));
			} catch (CloseLotException e) {
				final String errorMessage = this.messageSource.getMessage(e.getMessage());
				MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR), errorMessage);
				this.closeLotDiscardInventoryAction.closeAllLotCloseListeners();
				return;
			}

			this.closeLotDiscardInventoryAction.removeCurrentCloseLotListenerAndProcessNextItem(this);

		} else {
			List<CloseLotDiscardInventoryListener> closeLotListener = this.closeLotDiscardInventoryAction.getCloseLotListener();

			List<ListEntryLotDetails> listEntryLotDetails = Lists.newArrayList();

			for (CloseLotDiscardInventoryListener closeLotDiscardInventoryListener : closeLotListener) {
				listEntryLotDetails.add(closeLotDiscardInventoryListener.getEntryLotDetails());
			}

			try {
				this.source.processCloseLots(Lists.newArrayList(listEntryLotDetails));
			} catch (CloseLotException e) {
				final String errorMessage = this.messageSource.getMessage(e.getMessage());
				MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR), errorMessage);
				this.closeLotDiscardInventoryAction.closeAllLotCloseListeners();
				return;
			}

			this.closeLotDiscardInventoryAction.closeAllLotCloseListeners();

		}

		MessageNotifier.showMessage(this.source.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
				this.messageSource.getMessage(Message.LOTS_CLOSED_SUCCESSFULLY));

		this.source.resetListInventoryTableValues();
		this.source.resetListDataTableValues();
		this.source.getWindow().removeWindow(this);

	}

	public void noActionListener() {
		if (this.applyAllCheckBox.getValue().equals(Boolean.FALSE)) {
			this.closeLotDiscardInventoryAction.removeCurrentCloseLotListenerAndProcessNextItem(this);
			this.getParent().removeWindow(this);
		} else {
			this.closeLotDiscardInventoryAction.closeAllLotCloseListeners();
			this.source.getWindow().removeWindow(this);
		}

	}

	@Override
	public ListEntryLotDetails getEntryLotDetails() {
		return this.listEntryLotDetails;
	}

	public Label getConfirmLabel() {
		return confirmLabel;
	}

	public Button getYesButton() {
		return yesButton;
	}

	public Button getNoButton() {
		return noButton;
	}

	public CheckBox getApplyAllCheckBox() {
		return applyAllCheckBox;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public VerticalLayout getMainLayout() {
		return mainLayout;
	}
}
