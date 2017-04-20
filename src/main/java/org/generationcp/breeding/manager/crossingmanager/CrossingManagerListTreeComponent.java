
package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerTreeActionsListener;
import org.generationcp.breeding.manager.customfields.ListTreeTableComponent;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

@Configurable
public class CrossingManagerListTreeComponent extends ListTreeTableComponent {

	private static final long serialVersionUID = 8112173851252075693L;
	public static final String DEFAULT_HEIGHT = "580px";

	private Button addToFemaleListButton;
	private Button cancelButton;
	private Button addToMaleListButton;
	private Button openForReviewButton;
	private final CrossingManagerTreeActionsListener crossingTreeActionsListener;

	public CrossingManagerListTreeComponent(final CrossingManagerTreeActionsListener treeActionsListener) {
		super(treeActionsListener);
		this.crossingTreeActionsListener = treeActionsListener;
	}

	@Override
	public void addListeners() {

		super.addListeners();

		this.addToFemaleListButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -3383724866291655410L;

			@Override
			public void buttonClick(final ClickEvent event) {

				final Integer germplasmListId = (Integer) CrossingManagerListTreeComponent.this.getGermplasmListSource().getValue();
				
				CrossingManagerListTreeComponent.this.crossingTreeActionsListener.addListToFemaleList(germplasmListId);
				CrossingManagerListTreeComponent.this.closeTreeWindow(event);
			}

		});

		this.addToMaleListButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -7685621731871659880L;

			@Override
			public void buttonClick(final ClickEvent event) {

				final Integer germplasmListId = (Integer) CrossingManagerListTreeComponent.this.getGermplasmListSource().getValue();

				CrossingManagerListTreeComponent.this.crossingTreeActionsListener.addListToMaleList(germplasmListId);
				CrossingManagerListTreeComponent.this.closeTreeWindow(event);
			}

		});

		this.openForReviewButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 2103866815084444657L;

			@Override
			public void buttonClick(final ClickEvent event) {
				if (CrossingManagerListTreeComponent.this.germplasmList != null) {
					CrossingManagerListTreeComponent.this.getTreeActionsListener().studyClicked(
							CrossingManagerListTreeComponent.this.germplasmList);
					CrossingManagerListTreeComponent.this.closeTreeWindow(event);
				}

			}

		});

		this.cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -3708969669687499248L;

			@Override
			public void buttonClick(final ClickEvent event) {
				CrossingManagerListTreeComponent.this.closeTreeWindow(event);
			}

		});

	}

	protected void closeTreeWindow(final ClickEvent event) {
		final Window dialog = event.getComponent().getParent().getWindow();
		dialog.getParent().getWindow().removeWindow(dialog);
	}

	public void showWarningInInventoryView() {
		final String message = "Please switch to list view first before adding entries to parent lists.";
		MessageNotifier.showError(this.getWindow(), "Warning!", message);
	}

	@Override
	public void layoutComponents() {

		super.layoutComponents();

		final HorizontalLayout actionButtonsLayout = new HorizontalLayout();
		actionButtonsLayout.setDebugId("actionButtonsLayout");
		actionButtonsLayout.setSpacing(true);
		actionButtonsLayout.setStyleName("align-center");
		actionButtonsLayout.setMargin(true, false, false, false);

		actionButtonsLayout.addComponent(this.cancelButton);
		actionButtonsLayout.addComponent(this.addToFemaleListButton);
		actionButtonsLayout.addComponent(this.addToMaleListButton);
		actionButtonsLayout.addComponent(this.openForReviewButton);

		this.addComponent(actionButtonsLayout);

	}

	@Override
	public void instantiateComponents() {

		super.instantiateComponents();

		// Override the height for this component to add space for the buttons below.
		this.setHeight(DEFAULT_HEIGHT);

		this.addToFemaleListButton = new Button();
		this.addToFemaleListButton.setDebugId("addToFemaleListButton");
		this.addToFemaleListButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.addToFemaleListButton.setCaption(this.messageSource.getMessage(Message.DIALOG_ADD_TO_FEMALE_LABEL));
		this.addToFemaleListButton.setEnabled(false);

		this.addToMaleListButton = new Button();
		this.addToMaleListButton.setDebugId("addToMaleListButton");
		this.addToMaleListButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.addToMaleListButton.setCaption(this.messageSource.getMessage(Message.DIALOG_ADD_TO_MALE_LABEL));
		this.addToMaleListButton.setEnabled(false);

		this.openForReviewButton = new Button();
		this.openForReviewButton.setDebugId("openForReviewButton");
		this.openForReviewButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.openForReviewButton.setCaption(this.messageSource.getMessage(Message.DIALOG_OPEN_FOR_REVIEW_LABEL));
		this.openForReviewButton.setEnabled(false);

		this.cancelButton = new Button();
		this.cancelButton.setDebugId("cancelButton");
		this.cancelButton.setStyleName(Bootstrap.Buttons.DEFAULT.styleName());
		this.cancelButton.setCaption(this.messageSource.getMessage(Message.CANCEL));

	}

	@Override
	protected boolean doIncludeActionsButtons() {
		return true;
	}

	@Override
	protected boolean doIncludeRefreshButton() {
		return false;
	}

	@Override
	protected boolean isTreeItemsDraggable() {
		return true;
	}

	@Override
	public boolean doShowFoldersOnly() {
		return false;
	}

	@Override
	public String getTreeStyleName() {
		return "crossingManagerTree";
	}

	@Override
	public void refreshRemoteTree() {
		// current does not do anything, since there is no remote tree in the screen to be refresh
	}

	@Override
	public void studyClickedAction(final GermplasmList germplasmList) {
		this.toggleListSelectionButtons(true);
	}

	@Override
	public void folderClickedAction(final GermplasmList germplasmList) {
		this.toggleListSelectionButtons(false);
	}

	private void toggleListSelectionButtons(final boolean enabled) {
		this.addToFemaleListButton.setEnabled(enabled);
		this.addToMaleListButton.setEnabled(enabled);
		this.openForReviewButton.setEnabled(enabled);
	}

	Button getAddToFemaleListButton() {
		return addToFemaleListButton;
	}

	Button getCancelButton() {
		return cancelButton;
	}

	Button getAddToMaleListButton() {
		return addToMaleListButton;
	}

	Button getOpenForReviewButton() {
		return openForReviewButton;
	}

}
