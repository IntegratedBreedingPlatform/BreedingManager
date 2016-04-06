package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.GermplasmGroup;
import org.generationcp.middleware.service.api.GermplasmGroupNamingResult;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class AssignCodesResultsDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout, Window.CloseListener {

	private final Map<Integer, GermplasmGroupNamingResult> assignCodesResults;
	private Table assignCodesResultsTable;

	static final int MAX_MESSAGES_TO_DISPLAY = 15;

	AssignCodesResultsDialog() {
		this.assignCodesResults = new HashMap<>();
	}

	AssignCodesResultsDialog( final Map<Integer, GermplasmGroupNamingResult> assignCodesResults) {
		this.assignCodesResults = assignCodesResults;
	}

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private Button okButton;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void instantiateComponents() {
		this.okButton = new Button();
		this.okButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.assignCodesResultsTable = new Table();
		this.assignCodesResultsTable.setWidth("100%");
		this.assignCodesResultsTable.addContainerProperty("GID", Integer.class, null);
		this.assignCodesResultsTable.addContainerProperty("Results", Label.class, null);
		this.assignCodesResultsTable.setColumnWidth("GID", 40);
	}

	@Override
	public void initializeValues() {
		int rowId = 1;
		for (final Map.Entry<Integer, GermplasmGroupNamingResult> mapEntry : this.assignCodesResults.entrySet()) {
			final GermplasmGroupNamingResult groupNamingResult = mapEntry.getValue();

			final StringBuffer messageString = new StringBuffer();
			final Label resultsList = new Label();
			resultsList.setContentMode(Label.CONTENT_XHTML);
			resultsList.setStyleName("lst-assign-codes-results-message");

			int messageNumber = 1;

			for (final String message : groupNamingResult.getMessages()) {
				messageString.append("<li>").append(message).append("</li>");
				if (messageNumber == MAX_MESSAGES_TO_DISPLAY) {
					break;
				}
				messageNumber++;
			}
			if (groupNamingResult.getMessages().size() > MAX_MESSAGES_TO_DISPLAY) {
				messageString.append("<li>").append("....").append("</li>");
			}

			resultsList.setValue(messageString);
			this.assignCodesResultsTable.addItem(new Object[] {mapEntry.getKey(), resultsList}, rowId++);
		}
		this.assignCodesResultsTable.setPageLength(rowId);

	}

	@Override
	public void addListeners() {
		this.okButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(final Button.ClickEvent event) {
				AssignCodesResultsDialog.super.close();
			}
		});

	}

	@Override
	public void layoutComponents() {
		this.setModal(true);
		this.setWidth("900px");
		this.setHeight("400px");
		this.setResizable(false);
		this.addStyleName(Reindeer.WINDOW_LIGHT);

		this.center();

		final VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);

		verticalLayout.addComponent(this.assignCodesResultsTable);
		verticalLayout.addComponent(this.okButton);

		verticalLayout.setComponentAlignment(this.okButton, Alignment.BOTTOM_CENTER);
		this.setContent(verticalLayout);
	}

	@Override
	public void windowClose(final CloseEvent e) {
		super.close();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this, Message.ASSIGN_CODES_RESULTS_HEADER);
		this.messageSource.setCaption(this.okButton, Message.OK);
	}
}
