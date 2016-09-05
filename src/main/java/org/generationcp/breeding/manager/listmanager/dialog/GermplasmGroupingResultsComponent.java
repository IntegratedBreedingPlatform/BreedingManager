
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GermplasmGroupingResultsComponent extends BaseSubWindow implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout, Window.CloseListener {

	private Map<Integer, GermplasmGroup> groupingResults = new HashMap<Integer, GermplasmGroup>();

	private Table groupingResultsTable;
	private Button okButton;

	static final int MAX_MEMBERS_TO_DISPLAY = 15;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmGroupingResultsComponent(final Map<Integer, GermplasmGroup> groupingResults) {
		this.groupingResults = groupingResults;
	}

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
	public void windowClose(final CloseEvent e) {
		super.close();
	}

	@Override
	public void instantiateComponents() {
		this.okButton = new Button();
		this.okButton.setDebugId("okButton");
		this.okButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.groupingResultsTable = new Table();
		this.groupingResultsTable.setDebugId("groupingResultsTable");
		this.groupingResultsTable.setWidth("100%");
		this.groupingResultsTable.addContainerProperty("GID", Integer.class, null);
		this.groupingResultsTable.addContainerProperty("Group Id (MGID)", Integer.class, null);
		this.groupingResultsTable.addContainerProperty("Total Members", Integer.class, null);
		this.groupingResultsTable.addContainerProperty("Group Members", String.class, null);
		this.groupingResultsTable.addContainerProperty("Notes", String.class, null);
	}

	@Override
	public void initializeValues() {
		int rowId = 1;
		for (final Map.Entry<Integer, GermplasmGroup> mapEntry : this.groupingResults.entrySet()) {
			final GermplasmGroup groupingResult = mapEntry.getValue();

			final StringBuffer memberString = new StringBuffer();
			int memberNumber = 1;

			for (final Germplasm member : groupingResult.getGroupMembers()) {
				memberString.append(member.getGid());
				final Name preferredName = member.findPreferredName();
				if (preferredName != null) {
					memberString.append(" [");
					memberString.append(preferredName.getNval());
					memberString.append("]");
				}
				if (memberNumber == groupingResult.getGroupMembers().size()) {
					memberString.append(".");
				} else {
					memberString.append(",");
				}
				if (memberNumber == MAX_MEMBERS_TO_DISPLAY) {
					break;
				}
				memberNumber++;
			}

			if (groupingResult.getGroupMembers().size() > MAX_MEMBERS_TO_DISPLAY) {
				memberString.append("....");
			}

			final String notes =
					groupingResult.getFounder().getMethod().isGenerative() ? this.messageSource
							.getMessage(Message.GENERATIVE_GERMPLASM_NOT_GROUPED) : "";
			this.groupingResultsTable.addItem(new Object[] {groupingResult.getFounder().getGid(), groupingResult.getGroupId(),
					groupingResult.getGroupMembers().size(), memberString.toString(), notes}, rowId++);
		}
		this.groupingResultsTable.setPageLength(rowId);
	}

	@Override
	public void addListeners() {
		this.okButton.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				GermplasmGroupingResultsComponent.super.close();
			}
		});

	}

	@Override
	public void layoutComponents() {
		this.setModal(true);
		this.setWidth("800px");
		this.setHeight("400px");
		this.setResizable(false);
		this.addStyleName(Reindeer.WINDOW_LIGHT);

		this.center();

		final VerticalLayout verticleLayout = new VerticalLayout();
		verticleLayout.setDebugId("verticleLayout");
		verticleLayout.setMargin(true);
		verticleLayout.setSpacing(true);

		verticleLayout.addComponent(this.groupingResultsTable);
		verticleLayout.addComponent(this.okButton);

		verticleLayout.setComponentAlignment(this.okButton, Alignment.BOTTOM_CENTER);
		this.setContent(verticleLayout);
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this, Message.GROUPING_RESULTS_CAPTION);
		this.messageSource.setCaption(this.okButton, Message.OK);
	}

	Table getGroupingResultsTable() {
		return this.groupingResultsTable;
	}

	void setGroupingResultsTable(final Table groupingResultsTable) {
		this.groupingResultsTable = groupingResultsTable;
	}

	Button getOkButton() {
		return this.okButton;
	}

	void setOkButton(final Button okButton) {
		this.okButton = okButton;
	}

	SimpleResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

	void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
