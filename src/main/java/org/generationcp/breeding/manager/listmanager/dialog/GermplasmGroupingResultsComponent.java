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
import org.generationcp.middleware.service.impl.GermplasmGroupingResult;
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

	private Map<Integer, GermplasmGroupingResult> groupingResults = new HashMap<Integer, GermplasmGroupingResult>();
	
	private Table groupingResultsTable;
	private Button okButton;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmGroupingResultsComponent(Map<Integer, GermplasmGroupingResult> groupingResults) {
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
	public void windowClose(CloseEvent e) {
		super.close();
	}

	@Override
	public void instantiateComponents() {
		this.okButton = new Button();
		this.okButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.groupingResultsTable = new Table();
		this.groupingResultsTable.setWidth("100%");
		this.groupingResultsTable.addContainerProperty("GID", Integer.class, null);
		this.groupingResultsTable.addContainerProperty("Group Id (MGID)", Integer.class, null);
		this.groupingResultsTable.addContainerProperty("Total Members", Integer.class, null);
		this.groupingResultsTable.addContainerProperty("Group Members", String.class, null);
	}

	@Override
	public void initializeValues() {
		int rowId = 1;
		for (Map.Entry<Integer, GermplasmGroupingResult> mapEntry : this.groupingResults.entrySet()) {
			GermplasmGroupingResult groupingResult = mapEntry.getValue();

			StringBuffer memberString = new StringBuffer();
			int memberCounter = 1;
			for (Germplasm member : groupingResult.getGroupMembers()) {
				memberString.append(member.getGid());
				if (member.getPreferredName() != null) {
					memberString.append(" [");
					memberString.append(member.getPreferredName().getNval());
					if (memberCounter == groupingResult.getGroupMembers().size()) {
						memberString.append("]. ");
					} else {
						memberString.append("], ");
					}
				}
				memberCounter++;
			}
				
			this.groupingResultsTable.addItem(new Object[] {groupingResult.getFounderGid(), groupingResult.getGroupMgid(),
					groupingResult.getGroupMembers().size(), memberString.toString()}, rowId++);
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
		this.setWidth("600px");
		this.setHeight("400px");
		this.setResizable(false);
		this.addStyleName(Reindeer.WINDOW_LIGHT);

		this.center();

		VerticalLayout verticleLayout = new VerticalLayout();
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

}
