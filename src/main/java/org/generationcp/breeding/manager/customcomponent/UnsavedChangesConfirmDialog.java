
package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class UnsavedChangesConfirmDialog extends BaseSubWindow
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = -7800270790767272974L;

	private Label descriptionLabel;
	private Button cancelButton;
	private Button discardButton;
	private Button saveButton;

	private final String description;

	private VerticalLayout mainLayout;
	private final UnsavedChangesConfirmDialogSource source;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public UnsavedChangesConfirmDialog(final UnsavedChangesConfirmDialogSource source, final String description) {
		super();
		this.source = source;
		this.description = description;
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
		this.setModal(true);
		this.setCaption(this.messageSource.getMessage(Message.UNSAVED_CHANGES));
		this.setStyleName(Reindeer.WINDOW_LIGHT);
		this.addStyleName("unsaved-changes-dialog");
		// define window size, set as not resizable
		this.setWidth("544px");
		this.setHeight("180px");
		this.setResizable(false);
		// center window within the browser
		this.center();

		// content variables
		this.descriptionLabel = new Label("<center>" + this.description + "</center>", Label.CONTENT_XHTML);
		this.descriptionLabel.setDebugId("descriptionLabel");

		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.setDebugId("cancelButton");
		this.discardButton = new Button(this.messageSource.getMessage(Message.DISCARD_CHANGES));
		this.discardButton.setDebugId("discardButton");

		this.saveButton = new Button(this.messageSource.getMessage(Message.SAVE_CHANGES));
		this.saveButton.setDebugId("saveButton");
		this.saveButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 2688256898854358066L;

			@Override
			public void buttonClick(final ClickEvent event) {
				UnsavedChangesConfirmDialog.this.cancelAction();
			}
		});

		this.saveButton.addListener(new ClickListener() {

			private static final long serialVersionUID = -941792327552845606L;

			@Override
			public void buttonClick(final ClickEvent event) {
				UnsavedChangesConfirmDialog.this.saveAction();
			}
		});

		this.discardButton.addListener(new ClickListener() {

			private static final long serialVersionUID = -5985668025701325303L;

			@Override
			public void buttonClick(final ClickEvent event) {
				UnsavedChangesConfirmDialog.this.discardAction();
			}
		});
	}

	public void discardAction() {
		this.source.discardAllListChangesAction();
	}

	public void saveAction() {
		this.source.saveAllListChangesAction();
	}

	public void cancelAction() {
		this.source.cancelAllListChangesAction();
	}

	@Override
	public void layoutComponents() {
		this.mainLayout = new VerticalLayout();
		this.mainLayout.setDebugId("unsavedChangesMainLayout");
		this.mainLayout.setSpacing(true);

		this.mainLayout.addComponent(this.descriptionLabel);

		final Label forSpaceLabel = new Label();
		forSpaceLabel.setDebugId("forSpaceLabel");
		this.mainLayout.addComponent(forSpaceLabel);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.discardButton);
		buttonLayout.addComponent(this.saveButton);

		this.mainLayout.addComponent(buttonLayout);
		this.mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);

		this.addComponent(this.mainLayout);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}
}
