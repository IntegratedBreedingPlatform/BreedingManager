package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.breeding.manager.listimport.listeners.ImportGermplasmEntryActionListener;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class NewDesignationForGermplasmConfirmDialog extends Window implements BreedingManagerLayout, InitializingBean, ImportGermplasmEntryActionListener, Window.CloseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2742268286525936983L;
	public static final String WINDOW_NAME = "New Name";
	public static final String ADD_NAME_TO_GID = "Add name to GID";
	public static final String SEARCH_OR_CREATE_NEW = "Search/create another germplasm record";
	
	private  String germplasmName;
	private int germplasmIndex;
	private Integer ibdbUserId;
	private Integer dateIntValue;
	private Integer gid;
	private Integer nameMatchesCount;
	
	private VerticalLayout mainLayout;
	private Label confirmLabel;
	private Button searchCreateButton;
	private Button addNameButton;
	
	private ProcessImportedGermplasmAction source;
	
	
	
	public NewDesignationForGermplasmConfirmDialog(ProcessImportedGermplasmAction source, String germplasmName,int germplasmIndex, Integer gid, 
			Integer ibdbUserId, Integer dateIntValue, Integer nameMatchesCount) {
		super();
		this.germplasmName = germplasmName;
		this.germplasmIndex = germplasmIndex;
		this.ibdbUserId = ibdbUserId;
		this.dateIntValue = dateIntValue;
		this.gid = gid;
		this.nameMatchesCount = nameMatchesCount;
		this.source = source;
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
		setModal(true);
		setCaption(WINDOW_NAME);
		setStyleName(Reindeer.WINDOW_LIGHT);
		addStyleName("unsaved-changes-dialog");
		
		// define window size, set as not resizable
        setWidth("544px");
        setHeight("180px");
        setResizable(false);
        
        // center window within the browser
		center();
		confirmLabel = new Label("<center>" + getConfirmationMessage() + "</center>",Label.CONTENT_XHTML);
		
		searchCreateButton = new Button(SEARCH_OR_CREATE_NEW);
		
		addNameButton = new Button(ADD_NAME_TO_GID);
		addNameButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		
	}

	@SuppressWarnings("serial")
	@Override
	public void addListeners() {
		searchCreateButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				searchOrAddANewGermplasm();
			}
		});
		
		addNameButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				addGermplasmName();
			}
		});
		
	}
	
	private void searchOrAddANewGermplasm(){
		source.searchOrAddANewGermplasm(this);
		removeWindow(this);
	}

	@Override
	public void layoutComponents() {
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		mainLayout.addComponent(confirmLabel);
		
		Label forSpaceLabel = new Label();
		mainLayout.addComponent(forSpaceLabel);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(searchCreateButton);
		buttonLayout.addComponent(addNameButton);
		
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
		
		addComponent(mainLayout);
		
	}
	
	
	public Integer getGid() {
		return gid;
	}

	public Integer getIbdbUserId() {
		return ibdbUserId;
	}

	public Integer getDateIntValue() {
		return dateIntValue;
	}

	@Override
	public String getGermplasmName() {
		return germplasmName;
	}

	@Override
	public int getGermplasmIndex() {
		return germplasmIndex;
	}

	public Integer getNameMatchesCount() {
		return nameMatchesCount;
	}

	public void setNameMatchesCount(Integer nameMatchesCount) {
		this.nameMatchesCount = nameMatchesCount;
	}

	public void setGermplasmName(String germplasmName) {
		this.germplasmName = germplasmName;
	}

	public void setGermplasmIndex(int germplasmIndex) {
		this.germplasmIndex = germplasmIndex;
	}

	public void setIbdbUserId(Integer ibdbUserId) {
		this.ibdbUserId = ibdbUserId;
	}

	public void setDateIntValue(Integer dateIntValue) {
		this.dateIntValue = dateIntValue;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

    private void addGermplasmName(){
		Name name = source.createNameObject(ibdbUserId, dateIntValue, germplasmName);
		
		name.setNid(null);
		name.setNstat(Integer.valueOf(0));
		name.setGermplasmId(gid);
		
		source.addNameToGermplasm(name, gid);
		
		source.removeCurrentListenerAndProcessNextItem(this);
		removeWindow(this);
    }
    
    private String getConfirmationMessage(){
    	return "The name \"" + getGermplasmName() + "\" is not recorded as a name of GID " + getGid() + "."
	            + " Do you want to add the name to the GID or search/create another germplasm record?";
    }

	@Override
	public void windowClose(CloseEvent e) {
		super.close();
		source.closeAllImportEntryListeners();
	}

}
