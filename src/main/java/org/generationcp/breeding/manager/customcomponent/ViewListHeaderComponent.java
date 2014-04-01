package org.generationcp.breeding.manager.customcomponent;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

@Configurable
public class ViewListHeaderComponent extends GridLayout implements BreedingManagerLayout, InitializingBean, InternationalizableComponent{
	private static final Logger LOG = LoggerFactory.getLogger(ViewListHeaderComponent.class);
	
	private static final long serialVersionUID = 4690756426750044929L;
	
	private GermplasmList germplasmList;
	
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
	
	@Autowired
    private UserDataManager userDataManager;
	
	@Autowired
    private GermplasmListManager germplasmListManager;
	
	public ViewListHeaderComponent(GermplasmList germplasmList){
		super(2, 7);
		this.germplasmList = germplasmList;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	@Override
	public void instantiateComponents() {
		nameLabel = new Label(messageSource.getMessage(Message.LIST_NAME) + ":");
		nameLabel.addStyleName("bold");
		
		nameValueLabel = new Label(germplasmList.getName());
		nameValueLabel.setDescription(germplasmList.getName());
		nameValueLabel.setWidth("200px");
		
		ownerLabel = new Label(messageSource.getMessage(Message.LIST_OWNER_LABEL) + ":");
		ownerLabel.addStyleName("bold");
		
		ownerValueLabel = new Label(getOwnerListName(germplasmList.getUserId()));
		ownerValueLabel.setDescription(getOwnerListName(germplasmList.getUserId()));
		ownerValueLabel.setWidth("200px");
		
		statusLabel = new Label(messageSource.getMessage(Message.STATUS_LABEL) + ":");
		statusLabel.addStyleName("bold");
		
		String statusValue = "Unlocked List";
		if(germplasmList.getStatus() >= 100){
			statusValue = "Locked List";
		}
		statusValueLabel = new Label(statusValue);
		statusValueLabel.setWidth("200px");
		
		descriptionLabel = new Label(messageSource.getMessage(Message.DESCRIPTION_LABEL) + ":");
		descriptionLabel.addStyleName("bold");
		
		descriptionValueLabel = new Label(germplasmList.getDescription());
		descriptionValueLabel.setDescription(germplasmList.getDescription());
		descriptionValueLabel.setWidth("200px");
		
		typeLabel = new Label(messageSource.getMessage(Message.TYPE_LABEL) + ":");
		typeLabel.addStyleName("bold");
		
		String typeValue = getTypeString(germplasmList.getType());
		typeValueLabel = new Label(typeValue);
		typeValueLabel.setDescription(typeValue);
		typeValueLabel.setWidth("200px");
		
		dateLabel = new Label(messageSource.getMessage(Message.DATE_LABEL) + ":");
		dateLabel.addStyleName("bold");
		
		dateValueLabel = new Label(germplasmList.getDate().toString());
		dateValueLabel.setWidth("200px");
		
		notesLabel = new Label(messageSource.getMessage(Message.NOTES) + ":");
		notesLabel.addStyleName("bold");
		
		notesValueLabel = new Label(germplasmList.getNotes());
		notesValueLabel.setDescription(germplasmList.getNotes());
	}

	@Override
	public void initializeValues() {
		
	}

	@Override
	public void addListeners() {
		
	}

	@Override
	public void layoutComponents() {
		setSpacing(true);
		
		addComponent(nameLabel, 0, 0);
		addComponent(nameValueLabel, 1, 0);
		
		addComponent(ownerLabel, 0, 1);
		addComponent(ownerValueLabel, 1, 1);
		
		addComponent(statusLabel, 0, 2);
		addComponent(statusValueLabel, 1, 2);
		
		addComponent(descriptionLabel, 0, 3);
		addComponent(descriptionValueLabel, 1, 3);
		
		addComponent(typeLabel, 0, 4);
		addComponent(typeValueLabel, 1, 4);
		
		addComponent(dateLabel, 0, 5);
		addComponent(dateValueLabel, 1, 5);
		
		addComponent(notesLabel, 0, 6);
		addComponent(notesValueLabel, 1, 6);
	}

	private String getOwnerListName(Integer userId) {
		try{
	        User user=userDataManager.getUserById(userId);
	        if(user != null){
	            int personId=user.getPersonid();
	            Person p =userDataManager.getPersonById(personId);
	    
	            if(p!=null){
	                return p.getFirstName()+" "+p.getMiddleName() + " "+p.getLastName();
	            }else{
	                return user.getName();
	            }
	        } else {
	            return "";
	        }
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting list owner name of user with id: " + userId, ex);
			return "";
		}
    }
	
	private String getTypeString(String typeCode) {
		try{
	        List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();
	        
	        for (UserDefinedField listType : listTypes) {
	            if(typeCode.equals(listType.getFcode())){
	            	return listType.getFname();
	            }
	        }
		}catch(MiddlewareQueryException ex){
			LOG.error("Error in getting list types.", ex);
			return "Error in getting list types.";
		}
        
        return "Germplasm List";
    }

	@Override
	public void updateLabels() {
		
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<table border=\"0\">\n");
		builder.append("<tr>\n");
		builder.append("<td><b>List Name:</b></td>\n");
		builder.append("<td>" + germplasmList.getName() + "</td>\n");
		builder.append("</tr>\n");
		builder.append("<tr>\n");
		
		builder.append("<td><b>List Owner:</b></td>\n");
		builder.append("<td>" + getOwnerListName(germplasmList.getUserId()) + "</td>\n");
		builder.append("</tr>\n");
		
		builder.append("<tr>\n");
		String statusValue = "Unlocked List";
		if(germplasmList.getStatus() >= 100){
			statusValue = "Locked List";
		}
		builder.append("<td><b>Status:</b></td>\n");
		builder.append("<td>" + statusValue + "</td>\n");
		builder.append("</tr>\n");
		
		builder.append("<tr>\n");
		builder.append("<td><b>Description:</b></td>\n");
		builder.append("<td>" + germplasmList.getDescription() + "</td>\n");
		builder.append("</tr>\n");
		
		builder.append("<tr>\n");
		builder.append("<td><b>Type:</b></td>\n");
		builder.append("<td>" + getTypeString(germplasmList.getType()) + "</td>\n");
		builder.append("</tr>\n");
		
		builder.append("<tr>\n");
		builder.append("<td><b>Creation Date:</b></td>\n");
		builder.append("<td>" + germplasmList.getDate() + "</td>\n");
		builder.append("</tr>\n");
		
		builder.append("<tr>\n");
		builder.append("<td><b>Notes:</b></td>\n");
		if(germplasmList.getNotes() != null){
			builder.append("<td>" + germplasmList.getNotes() + "</td>\n");
		} else{
			builder.append("<td>-</td>\n");
		}
		builder.append("</tr>\n");
		
		builder.append("</table>");
		
		return builder.toString();
	}
}
