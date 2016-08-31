
package org.generationcp.breeding.manager.customcomponent;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Reusable component that displays input steps as wizard type layout. Has methods for traversing back and next steps, the UI is updated
 * accordingly to reflect the currently selected wizard step
 * 
 * @author Darla Ani
 * 
 */
@Configurable
public class BreedingManagerWizardDisplay extends HorizontalLayout implements InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = -122768867922396461L;

	private static final Logger LOG = LoggerFactory.getLogger(BreedingManagerWizardDisplay.class);

	protected List<String> wizardSteps = new ArrayList<String>();
	protected List<Label> wizardLabels = new ArrayList<Label>();
	protected int currentIndex = 0;

	public BreedingManagerWizardDisplay(String... steps) {
		super();
		for (String step : steps) {
			this.wizardSteps.add(step);
		}
	}

	public int nextStep() {
		if (this.currentIndex < this.wizardSteps.size() - 1) {
			this.currentIndex++;
			this.updateSelectedStep();
		} else {
			LOG.error("There is no more NEXT step left for this wizard.");
		}
		return this.currentIndex;
	}

	public int backStep() {
		if (this.currentIndex >= 1) {
			this.currentIndex--;
			this.updateSelectedStep();
		} else {
			LOG.error("There is no more BACK step left for this wizard.");
		}
		return this.currentIndex;
	}

	protected void updateSelectedStep() {
		for (int i = 0; i < this.wizardLabels.size(); i++) {
			Label label = this.wizardLabels.get(i);
			label.setEnabled(i == this.currentIndex);
		}
	}

	protected String getSelectedStepStyle() {
		return "v-captiontext";
	}

	protected String getUnselectedStepStyle() {
		return "";
	}

	@Override
	public void instantiateComponents() {
		this.setHeight("40px");

		this.initializeLabelLayouts();
		this.updateSelectedStep();
	}

	private void initializeLabelLayouts() {
		for (int i = 1; i <= this.wizardSteps.size(); i++) {

			Label label = new Label();
			label.setDebugId("label");
			if (this.displayStepNumber()) {
				label.setValue(i + "." + this.wizardSteps.get(i - 1));
			} else {
				label.setValue(this.wizardSteps.get(i - 1));
			}
			label.addStyleName(this.getLabelStyleName());
			label.setVisible(this.showAllSteps());

			label.setWidth(this.getLabelWidth());

			this.wizardLabels.add(label);
		}
	}

	protected String getLabelWidth() {
		return "180px";
	}

	protected String getLabelStyleName() {
		return Bootstrap.Typography.H3.styleName();
	}

	protected boolean displayStepNumber() {
		return true;
	}

	@Override
	public void initializeValues() {
		// only show first step
		Label firstStepLabel = this.wizardLabels.get(0);
		if (!this.showAllSteps() && firstStepLabel != null) {
			firstStepLabel.setVisible(true);
		}
	}

	@Override
	public void addListeners() {
		// not implemented
	}

	@Override
	public void layoutComponents() {
		for (Label label : this.wizardLabels) {
			this.addComponent(label);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.layoutComponents();
	}

	protected boolean showAllSteps() {
		return true;
	}

	/**
	 * Updates the page according to inputs from last selected step of the wizard
	 * 
	 */
	public interface StepChangeListener {

		public void updatePage();
	}
}
