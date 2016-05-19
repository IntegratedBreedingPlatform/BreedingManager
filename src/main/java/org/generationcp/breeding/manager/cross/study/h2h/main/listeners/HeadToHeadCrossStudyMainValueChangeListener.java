
package org.generationcp.breeding.manager.cross.study.h2h.main.listeners;

import org.generationcp.breeding.manager.cross.study.commons.EnvironmentFilter;
import org.generationcp.breeding.manager.cross.study.h2h.main.TraitsAvailableComponent;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.AddEnvironmentalConditionsDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.FilterLocationDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.FilterStudyDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.middleware.domain.dms.TrialEnvironmentProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Component;

public class HeadToHeadCrossStudyMainValueChangeListener implements ValueChangeListener {

	private static final long serialVersionUID = -3422805642974069212L;

	private static final Logger LOG = LoggerFactory.getLogger(HeadToHeadCrossStudyMainValueChangeListener.class);

	private final Component source;
	private Component sourceComboBox;
	private FilterLocationDto filterLocationDto;
	private TrialEnvironmentProperty environmentCondition;
	private String tableKey;
	private boolean isTagAll = false;
	private Component parentOfSource; // EnvironmentsAvailableCompoent or SpecifyAndWeighEnvironment

	public HeadToHeadCrossStudyMainValueChangeListener(Component source, boolean isTagAll) {
		this.source = source;
		this.isTagAll = isTagAll;
	}

	public HeadToHeadCrossStudyMainValueChangeListener(Component source, Component sourceComboBox) {
		this.source = source;
		this.sourceComboBox = sourceComboBox;
	}

	public HeadToHeadCrossStudyMainValueChangeListener(Component source, Component sourceComboBox, String tableKey) {
		this.source = source;
		this.sourceComboBox = sourceComboBox;
		this.tableKey = tableKey;
	}

	public HeadToHeadCrossStudyMainValueChangeListener(Component source, Component sourceComboBox, FilterLocationDto filterLocationDto) {
		this.source = source;
		this.filterLocationDto = filterLocationDto;
	}

	public HeadToHeadCrossStudyMainValueChangeListener(Component source, Component sourceComboBox,
			TrialEnvironmentProperty environmentCondition) {
		this.source = source;
		this.environmentCondition = environmentCondition;
	}

	public HeadToHeadCrossStudyMainValueChangeListener(Component source, Component parentOfSource, boolean isTagAll) {
		this.source = source;
		this.parentOfSource = parentOfSource;
		this.isTagAll = isTagAll;
	}

	@Override
	public void valueChange(ValueChangeEvent event) {

		String parentClass = "";
		if (this.parentOfSource instanceof EnvironmentFilter) {
			parentClass = "EnvironmentFilter";
		}

		if (this.source instanceof TraitsAvailableComponent) {
			if (this.isTagAll) {
				((TraitsAvailableComponent) this.source).clickTagAllCheckbox((Boolean) event.getProperty().getValue());
			} else {
				((TraitsAvailableComponent) this.source).clickCheckBox(this.sourceComboBox, (Boolean) event.getProperty().getValue());
			}
		} else if (this.source instanceof EnvironmentFilter) {
			((EnvironmentFilter) this.source).clickCheckBox(this.tableKey, this.sourceComboBox, (Boolean) event.getProperty().getValue());
		} else if (this.source instanceof FilterLocationDialog) {
			if (this.isTagAll) {
				((FilterLocationDialog) this.source).clickCheckBoxTag((Boolean) event.getProperty().getValue(), parentClass);
			} else {
				((FilterLocationDialog) this.source).clickCheckBox((Boolean) event.getProperty().getValue(), this.filterLocationDto);
			}
		} else if (this.source instanceof FilterStudyDialog) {
			if (this.isTagAll) {
				((FilterStudyDialog) this.source).clickCheckBoxTag((Boolean) event.getProperty().getValue(), parentClass);
			} else {
				((FilterStudyDialog) this.source).clickCheckBox((Boolean) event.getProperty().getValue(), this.filterLocationDto);
			}

		} else if (this.source instanceof AddEnvironmentalConditionsDialog) {
			if (this.isTagAll) {
				((AddEnvironmentalConditionsDialog) this.source).clickCheckBoxTag((Boolean) event.getProperty().getValue(), parentClass);
			} else {
				((AddEnvironmentalConditionsDialog) this.source).clickCheckBox((Boolean) event.getProperty().getValue(),
						this.environmentCondition);
			}
		} else {
			HeadToHeadCrossStudyMainValueChangeListener.LOG
					.error("HeadToHeadCrossStudyMainButtonClickListener: Error with buttonClick action. Source not identified.");
		}
	}

}
