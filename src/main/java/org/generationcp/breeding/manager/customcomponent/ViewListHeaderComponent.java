
package org.generationcp.breeding.manager.customcomponent;

import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

@Configurable
public class ViewListHeaderComponent extends GridLayout implements BreedingManagerLayout, InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 4690756426750044929L;

	private final GermplasmList germplasmList;

	private Label nameLabel;
	private Label nameValueLabel;
	private Label ownerLabel;
	private Label ownerValueLabel;
	private Label statusLabel;
	private Label statusValueLabel;
	private Label descriptionLabel;
	private Label descriptionValueLabel;
	private Label typeLabel;
	private Label typeValueLabel;
	private Label dateLabel;
	private Label dateValueLabel;
	private Label notesLabel;
	private Label notesValueLabel;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private Map<Integer, String> userNameMap;

	private List<UserDefinedField> listTypes;

	public ViewListHeaderComponent(final GermplasmList germplasmList, final Map<Integer, String> userNameMap,
			final List<UserDefinedField> listTypes) {
		super(2, 7);
		this.germplasmList = germplasmList;
		this.userNameMap = userNameMap;
		this.listTypes = listTypes;
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


		this.nameLabel = new Label(this.messageSource.getMessage(Message.LIST_NAME) + ":");
		this.nameLabel.setDebugId("nameLabel");
		this.nameLabel.addStyleName("bold");

		this.nameValueLabel = new Label(this.germplasmList.getName());
		this.nameValueLabel.setDebugId("nameValueLabel");
		this.nameValueLabel.setDescription(this.germplasmList.getName());
		this.nameValueLabel.setWidth("200px");

		this.ownerLabel = new Label(this.messageSource.getMessage(Message.LIST_OWNER_LABEL) + ":");
		this.ownerLabel.setDebugId("ownerLabel");
		this.ownerLabel.addStyleName("bold");

		final String ownerName = userNameMap.get(this.germplasmList.getUserId());
		this.ownerValueLabel = new Label(ownerName);
		this.ownerValueLabel.setDebugId("ownerValueLabel");
		this.ownerValueLabel.setDescription(ownerName);
		this.ownerValueLabel.setWidth("200px");

		this.statusLabel = new Label(this.messageSource.getMessage(Message.STATUS_LABEL) + ":");
		this.statusLabel.setDebugId("statusLabel");
		this.statusLabel.addStyleName("bold");

		this.statusValueLabel = new Label(this.getStatusValue(this.germplasmList.getStatus()));
		this.statusValueLabel.setDebugId("statusValueLabel");
		this.statusValueLabel.setWidth("200px");

		this.descriptionLabel = new Label(this.messageSource.getMessage(Message.DESCRIPTION_LABEL) + ":");
		this.descriptionLabel.setDebugId("descriptionLabel");
		this.descriptionLabel.addStyleName("bold");

		final String description = BreedingManagerUtil.getDescriptionForDisplay(this.germplasmList);
		this.descriptionValueLabel = new Label(description);
		this.descriptionValueLabel.setDebugId("descriptionValueLabel");
		this.descriptionValueLabel.setDescription(this.germplasmList.getDescription());
		this.descriptionValueLabel.setWidth("200px");

		this.typeLabel = new Label(this.messageSource.getMessage(Message.TYPE_LABEL) + ":");
		this.typeLabel.setDebugId("typeLabel");
		this.typeLabel.addStyleName("bold");

		final String typeValue = BreedingManagerUtil.getTypeString(this.germplasmList.getType(), listTypes);
		this.typeValueLabel = new Label(typeValue);
		this.typeValueLabel.setDebugId("typeValueLabel");
		this.typeValueLabel.setDescription(typeValue);
		this.typeValueLabel.setWidth("200px");

		this.dateLabel = new Label(this.messageSource.getMessage(Message.DATE_LABEL) + ":");
		this.dateLabel.setDebugId("dateLabel");
		this.dateLabel.addStyleName("bold");

		this.dateValueLabel = new Label(this.germplasmList.getDate() != null ? this.germplasmList.getDate().toString() : "");
		this.dateValueLabel.setDebugId("dateValueLabel");
		this.dateValueLabel.setWidth("200px");

		this.notesLabel = new Label(this.messageSource.getMessage(Message.NOTES) + ":");
		this.notesLabel.setDebugId("notesLabel");
		this.notesLabel.addStyleName("bold");

		String notes = "-";
		if (this.germplasmList.getNotes() != null && this.germplasmList.getNotes().length() != 0) {
			notes = this.germplasmList.getNotes();
			if (notes.length() > 27) {
				notes = notes.substring(0, 27) + "...";
			}
		}
		this.notesValueLabel = new Label(notes);
		this.notesValueLabel.setDebugId("notesValueLabel");
		this.notesValueLabel.setDescription(this.germplasmList.getNotes());

	}

	public GermplasmList getGermplasmList() {
		return germplasmList;
	}

	public void setStatus(final int status) {
		if (this.statusValueLabel == null) {
			this.statusValueLabel = new Label();
			this.statusValueLabel.setDebugId("statusValueLabel");
		}
		this.statusValueLabel.setValue(this.getStatusValue(status));
		this.getGermplasmList().setStatus(status);
	}

	private String getStatusValue(final Integer status) {
		String statusValue = "Unlocked List";
		if (status != null && this.germplasmList.getStatus() >= 100) {
			statusValue = "Locked List";
		}
		return statusValue;
	}

	public Label getStatusValueLabel() {
		return statusValueLabel;
	}

	@Override
	public void initializeValues() {
		// not implemented
	}

	@Override
	public void addListeners() {
		// not implemented
	}

	@Override
	public void layoutComponents() {
		this.setSpacing(true);

		this.addComponent(this.nameLabel, 0, 0);
		this.addComponent(this.nameValueLabel, 1, 0);

		this.addComponent(this.ownerLabel, 0, 1);
		this.addComponent(this.ownerValueLabel, 1, 1);

		this.addComponent(this.statusLabel, 0, 2);
		this.addComponent(this.statusValueLabel, 1, 2);

		this.addComponent(this.descriptionLabel, 0, 3);
		this.addComponent(this.descriptionValueLabel, 1, 3);

		this.addComponent(this.typeLabel, 0, 4);
		this.addComponent(this.typeValueLabel, 1, 4);

		this.addComponent(this.dateLabel, 0, 5);
		this.addComponent(this.dateValueLabel, 1, 5);

		this.addComponent(this.notesLabel, 0, 6);
		this.addComponent(this.notesValueLabel, 1, 6);
	}

	@Override
	public void updateLabels() {
		// not implemented
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("<table border=\"0\">\n");
		builder.append("<tr>\n");
		builder.append("<td><b>List Name:</b></td>\n");
		builder.append("<td>" + this.germplasmList.getName() + "</td>\n");
		builder.append("</tr>\n");
		builder.append("<tr>\n");

		builder.append("<td><b>List Owner:</b></td>\n");
		builder.append("<td>" + userNameMap.get(this.germplasmList.getUserId()) + "</td>\n");
		builder.append("</tr>\n");

		builder.append("<tr>\n");
		String statusValue = "Unlocked List";
		if (this.germplasmList.getStatus() != null && this.germplasmList.getStatus() >= 100) {
			statusValue = "Locked List";
		}
		builder.append("<td><b>Status:</b></td>\n");
		builder.append("<td>" + statusValue + "</td>\n");
		builder.append("</tr>\n");

		builder.append("<tr>\n");
		builder.append("<td><b>Description:</b></td>\n");
		final String description = BreedingManagerUtil.getDescriptionForDisplay(this.germplasmList);

		builder.append("<td>" + description + "</td>\n");
		builder.append("</tr>\n");

		builder.append("<tr>\n");
		builder.append("<td><b>Type:</b></td>\n");
		builder.append("<td>" + BreedingManagerUtil.getTypeString(this.germplasmList.getType(), listTypes) + "</td>\n");
		builder.append("</tr>\n");

		builder.append("<tr>\n");
		builder.append("<td><b>Creation Date:</b></td>\n");
		builder.append("<td>" + this.germplasmList.getDate() + "</td>\n");
		builder.append("</tr>\n");

		builder.append("<tr>\n");
		builder.append("<td><b>Notes:</b></td>\n");
		if (this.germplasmList.getNotes() != null) {
			String notes = this.germplasmList.getNotes().replaceAll("<", "&lt;");
			notes = notes.replaceAll(">", "&gt;");
			if (notes.length() > 27) {
				notes = notes.substring(0, 27) + "...";
			}
			if (notes.length() == 0) {
				notes = "-";
			}
			builder.append("<td>" + notes + "</td>\n");
		} else {
			builder.append("<td>-</td>\n");
		}
		builder.append("</tr>\n");

		builder.append("</table>");

		return builder.toString();
	}
}
