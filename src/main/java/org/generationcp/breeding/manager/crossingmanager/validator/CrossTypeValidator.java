package org.generationcp.breeding.manager.crossingmanager.validator;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.MakeCrossesParentsComponent;
import org.generationcp.breeding.manager.crossingmanager.constants.CrossType;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator;
import com.vaadin.ui.Table;

@Configurable
public class CrossTypeValidator implements Validator {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	private MakeCrossesParentsComponent parentsComponent;
	

	public CrossTypeValidator(final MakeCrossesParentsComponent parentsComponent) {
		super();
		this.parentsComponent = parentsComponent;
	}

	@Override
	public void validate(Object value) {
		this.isValid(value);
	}

	@Override
	public boolean isValid(Object value) {
		final CrossType type = (CrossType) value;
		if (CrossType.PLEASE_CHOOSE.equals(type)) {
			MessageNotifier.showWarning(this.parentsComponent.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.PLEASE_CHOOSE_CROSSING_METHOD));
			return false;
		}
		
		final Table femaleParents = this.parentsComponent.getFemaleTable();
		final Table maleParents = this.parentsComponent.getMaleTable();

		List<GermplasmListEntry> femaleList = this.parentsComponent.getCorrectSortedValue(femaleParents);
		List<GermplasmListEntry> maleList = this.parentsComponent.getCorrectSortedValue(maleParents);
		
		if (femaleList.isEmpty()) {
			throw new InvalidValueException(this.messageSource.getMessage(Message.AT_LEAST_ONE_FEMALE_PARENT_MUST_BE_SELECTED));
		}
		
		final boolean isUnknownMaleCrossing = CrossType.UNKNOWN_MALE.equals(type);
		if (maleList.isEmpty() && !isUnknownMaleCrossing) {
			throw new InvalidValueException(this.messageSource.getMessage(Message.AT_LEAST_ONE_MALE_PARENT_MUST_BE_SELECTED));
		}
		
		if (CrossType.TOP_TO_BOTTOM.equals(type) && femaleList.size() != maleList.size()) {
			throw new InvalidValueException(this.messageSource
						.getMessage(Message.ERROR_MALE_AND_FEMALE_PARENTS_MUST_BE_EQUAL));
		}
		// Show warning that male items will be ignored for Unknown Male crossing method option, but crosses will proceed
		if (!maleList.isEmpty() && isUnknownMaleCrossing) {
			this.parentsComponent.getMaleParentTab().resetList();
			MessageNotifier.showWarning(this.parentsComponent.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.MALE_PARENTS_WILL_BE_IGNORED));
		}
		
		return true;
	}



}
