/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.breeding.manager.germplasm;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.GermplasmStudyBrowserLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

@Configurable
public class GermplasmCharacteristicsComponent extends GridLayout implements InitializingBean, InternationalizableComponent,
		GermplasmStudyBrowserLayout {

	private static final long serialVersionUID = 1L;
	private Label lblGID;
	private Label lblPrefName;
	private Label lblLocation;
	private Label lblGermplasmMethod;
	private Label lblCreationDate;
	private Label lblReference;
	private CheckBox chkFixedLines;
	private Label lblGroupId;

	private final GermplasmDetailModel gDetailModel;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmCharacteristicsComponent(final GermplasmDetailModel gDetailModel) {
		this.gDetailModel = gDetailModel;
	}

	@Override
	public void afterPropertiesSet() {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.lblPrefName =
				new Label("<b>" + this.messageSource.getMessage(Message.PREFNAME_LABEL) + ":</b> "
						+ this.gDetailModel.getGermplasmPreferredName()); // "Preferred Name"
		this.lblPrefName.setContentMode(Label.CONTENT_XHTML);
		this.lblGermplasmMethod =
				new Label("<b>" + this.messageSource.getMessage(Message.CREATION_METHOD_LABEL) + ":</b> "
						+ this.gDetailModel.getGermplasmMethod()); // "Creation Method"
		this.lblGermplasmMethod.setContentMode(Label.CONTENT_XHTML);
		this.lblGermplasmMethod.setDescription(this.gDetailModel.getGermplasmMethod());
		this.lblCreationDate =
				new Label("<b>" + this.messageSource.getMessage(Message.CREATION_DATE_LABEL) + ":</b> "
						+ String.valueOf(this.gDetailModel.getGermplasmCreationDate())); // "Creation Date"
		this.lblCreationDate.setContentMode(Label.CONTENT_XHTML);
		String locationName = this.gDetailModel.getGermplasmLocation();
		if (locationName != null && locationName.length() > 40) {
			locationName = locationName.substring(0, 40) + "...";
		} else if (StringUtils.isEmpty(locationName)) {
			locationName = "-";
		}
		this.lblLocation = new Label("<b>" + this.messageSource.getMessage(Message.LOCATION_LABEL) + ":</b> " + locationName); // "Location"
		this.lblLocation.setContentMode(Label.CONTENT_XHTML);
		this.lblLocation.setDescription(this.gDetailModel.getGermplasmLocation());
		this.lblGID =
				new Label("<b>" + this.messageSource.getMessage(Message.GID_LABEL) + ":</b> " + String.valueOf(this.gDetailModel.getGid())); // "GID"
		this.lblGID.setContentMode(Label.CONTENT_XHTML);
		String reference = "-";
		String referenceFullString = null;
		if (this.gDetailModel.getReference() != null) {
			referenceFullString = String.valueOf(this.gDetailModel.getReference());
			if (referenceFullString.length() > 40) {
				reference = referenceFullString.substring(0, 40) + "...";
			} else {
				reference = referenceFullString;
			}
		}
		this.lblReference = new Label("<b>" + this.messageSource.getMessage(Message.REFERENCE_LABEL) + ":</b> " + reference); // "Reference"
		this.lblReference.setContentMode(Label.CONTENT_XHTML);
		this.lblReference.setDescription(referenceFullString);

		this.chkFixedLines = new CheckBox(this.messageSource.getMessage(Message.FIXED_LINE_LABEL));

		this.lblGroupId =
				new Label("<b>" + this.messageSource.getMessage(Message.GROUP_ID_LABEL) + ":</b> "
						+ String.valueOf(this.gDetailModel.getMGid())); // "MGID"
		this.lblGroupId.setContentMode(Label.CONTENT_XHTML);
	}

	@Override
	public void initializeValues() {
		this.initializeFixedLine(this.chkFixedLines, this.gDetailModel.getMGid());
	}

	void initializeFixedLine(final CheckBox chkFixedLines, final Integer mGid) {
		if (mGid.intValue() > 0) {
			chkFixedLines.setValue(true);
		}
		chkFixedLines.setReadOnly(true);
	}

	@Override
	public void addListeners() {
		// do nothing
	}

	@Override
	public void layoutComponents() {
		this.setRows(3);
		this.setColumns(3);
		this.setSpacing(true);
		this.setWidth("90%");

		this.addComponent(this.lblPrefName, 0, 0);
		this.addComponent(this.lblGermplasmMethod, 0, 1);
		this.addComponent(this.lblCreationDate, 1, 0);
		this.addComponent(this.lblLocation, 1, 1);
		this.addComponent(this.lblGID, 2, 0);
		this.addComponent(this.lblReference, 2, 1);
		this.addComponent(this.chkFixedLines, 0, 2);
		this.addComponent(this.lblGroupId, 1, 2);
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	/**
	 * FOR TEST ONLY
	 * 
	 * @param messageSource
	 */
	void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

}
