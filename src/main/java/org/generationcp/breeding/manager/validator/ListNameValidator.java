
package org.generationcp.breeding.manager.validator;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator;

@Configurable
public class ListNameValidator implements Validator {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(ListNameValidator.class);

	private static final String DEFAULT_ERROR = "Please specify the name and/or location of the list";
	private static final String SAME_PARENT_FOLDER_LIST_NAME_ERROR = "List Name and its Parent Folder must not have the same name";
	private static final String INVALID_LIST_NAME_PATTERN = "[\\\\/:*?|<>\"\\\\.]";
	private static final Pattern invalidListNamePattern = Pattern.compile(ListNameValidator.INVALID_LIST_NAME_PATTERN);

	private String errorDetails;
	private String parentFolder;

	private String currentListName;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private ContextUtil contextUtil;

	public ListNameValidator() {
	}

	public ListNameValidator(final String parentFolder) {
		this.parentFolder = parentFolder;
		this.errorDetails = ListNameValidator.DEFAULT_ERROR;
	}

	public ListNameValidator(final String parentFolder, final String currentListName) {
		this.parentFolder = parentFolder;
		this.errorDetails = ListNameValidator.DEFAULT_ERROR;
		this.currentListName = currentListName;
	}

	@Override
	public void validate(final Object value) {
		if (!this.isValid(value)) {
			throw new InvalidValueException(this.errorDetails);
		}
	}

	@Override
	public boolean isValid(final Object value) {
		if (this.parentFolder != null) {

			if (this.parentFolder.trim().length() == 0) {
				this.errorDetails = ListNameValidator.DEFAULT_ERROR;
				return false;
			}

			if (this.parentFolder.trim().endsWith(value.toString() + " >")) {
				this.errorDetails = ListNameValidator.SAME_PARENT_FOLDER_LIST_NAME_ERROR;
				return false;
			}
		}

		if (!this.validateListName(value.toString().trim())) {
			return false;
		}

		return true;
	}

	protected boolean validateListName(final String listName) {
		final String newName = listName.trim();
		boolean isValid = true;
		if (StringUtils.isEmpty(newName)) {
			this.errorDetails = this.messageSource.getMessage(Message.INVALID_ITEM_NAME);
			isValid = false;

		} else if (ListSelectorComponent.LISTS.equalsIgnoreCase(newName)) {
			this.errorDetails = "Cannot use \"" + ListSelectorComponent.LISTS + "\" as item name.";
			isValid = false;
		} else if (ListNameValidator.invalidListNamePattern.matcher(newName).find()) {
			this.errorDetails = this.messageSource.getMessage(Message.INVALID_LIST_NAME);
			isValid = false;

		} else {
			// Check if given list name is already taken by other lists for new list or only when list name changed from old value
			if (StringUtils.isEmpty(this.currentListName) || !newName.equals(this.currentListName.trim())) {

				try {
					final List<GermplasmList> lists =
							this.germplasmListManager.getGermplasmListByName(newName, this.getCurrentProgramUUID(), 0, 1, Operation.EQUAL);

					if (!lists.isEmpty()) {
						this.errorDetails = this.messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE);
						isValid = false;
					}

				} catch (final MiddlewareQueryException ex) {
					ListNameValidator.LOG.error("Error with getting germplasm list by list name - " + listName, ex);
					this.errorDetails = this.messageSource.getMessage(Message.ERROR_VALIDATING_LIST);
					isValid = false;
				}
			}
		}
		return isValid;
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	public String getCurrentListName() {
		return this.currentListName;
	}

	public void setCurrentListName(final String currentListName) {
		this.currentListName = currentListName;
	}

	public String getParentFolder() {
		return this.parentFolder;
	}

	public void setParentFolder(final String parentFolder) {
		this.parentFolder = parentFolder;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

}
