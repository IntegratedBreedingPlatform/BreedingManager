package org.generationcp.breeding.manager.customcomponent;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Reusable component that displays input steps as wizard type layout.
 * Has methods for traversing back and next steps, the UI is updated accordingly
 * to reflect the currently selected wizard step
 * 
 * @author Darla Ani
 *
 */
@Configurable
public class BreedingManagerWizardDisplay extends HorizontalLayout 
	implements InitializingBean, BreedingManagerLayout{
	
	
	private static final long serialVersionUID = -122768867922396461L;

	protected List<String> wizardSteps = new ArrayList<String>();
	protected List<Label> wizardLabels = new ArrayList<Label>();
	protected int currentIndex = 0;
	
	public BreedingManagerWizardDisplay(String... steps){
		super();
		for (String step : steps){
			wizardSteps.add(step);
		}
	}
	
	public int nextStep(){
		if (currentIndex < wizardSteps.size() - 1){
			currentIndex++;
			updateSelectedStep();
		} else {
			throw new Error("There is no more NEXT step left for this wizard.");
		}
		return currentIndex;
	}
	
	public int backStep(){
		if (currentIndex >= 1){
			currentIndex--;
			updateSelectedStep();
		} else {
			throw new Error("There is no more BACK step left for this wizard.");
		}
		return currentIndex;
	}
	
	protected void updateSelectedStep(){
		for (int i=0; i < wizardLabels.size(); i++){
			Label label = wizardLabels.get(i);
			label.setEnabled(i==currentIndex);
		}
	}
	
	protected String getSelectedStepStyle(){
		return "v-captiontext";
	}
	
	protected String getUnselectedStepStyle(){
		return "";
	}

	@Override
	public void instantiateComponents() {
		setHeight("40px");
		
		initializeLabelLayouts();
		updateSelectedStep();
	}

	private void initializeLabelLayouts() {
		for (int i=1; i<= wizardSteps.size(); i++){
			
			Label label = new Label();
			if (displayStepNumber()){
				label.setValue(i + "." + wizardSteps.get(i-1));
			} else {
				label.setValue( wizardSteps.get(i-1));
			}
			label.addStyleName(getLabelStyleName());
			label.setVisible(showAllSteps());
			
			label.setWidth(getLabelWidth());
			
			wizardLabels.add(label);
		}
	}
	
	protected String getLabelWidth(){
		return "180px";
	}
	
	protected String getLabelStyleName(){
		return Bootstrap.Typography.H3.styleName();
	}
	
	protected boolean displayStepNumber(){
		return true;
	}
 
	@Override
	public void initializeValues() {
		// only show first step
		Label firstStepLabel = wizardLabels.get(0);
		if (!showAllSteps() && firstStepLabel != null){
			firstStepLabel.setVisible(true);
		}
	}

	@Override
	public void addListeners() {
	}

	@Override
	public void layoutComponents() {
		for (Label label : wizardLabels){
			addComponent(label);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		layoutComponents();
	}
	
	protected boolean showAllSteps(){
		return true;
	}

	
	/**
	 * Updates the page according to inputs from
	 * last selected step of the wizard 
	 *
	 */
	public interface StepChangeListener {
		public void updatePage();
	}
}
