
package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.breeding.manager.listimport.listeners.ImportGermplasmEntryActionListener;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
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
public class NewDesignationForGermplasmConfirmDialog extends BaseSubWindow implements BreedingManagerLayout, InitializingBean,
		ImportGermplasmEntryActionListener, Window.CloseListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -2742268286525936983L;
	public static final String WINDOW_NAME = "New Name";
	public static final String ADD_NAME_TO_GID = "Add name to GID";
	public static final String SEARCH_OR_CREATE_NEW = "Search/create another germplasm record";

	private String germplasmName;
	private int germplasmIndex;
	private Integer ibdbUserId;
	private Integer dateIntValue;
	private Integer gid;
	private Integer nameMatchesCount;

	private VerticalLayout mainLayout;
	private Label confirmLabel;
	private Button searchCreateButton;
	private Button addNameButton;

	private final ProcessImportedGermplasmAction source;

	public NewDesignationForGermplasmConfirmDialog(ProcessImportedGermplasmAction source, String germplasmName, int germplasmIndex,
			Integer gid, Integer ibdbUserId, Integer dateIntValue, Integer nameMatchesCount) {
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
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.setModal(true);
		this.setCaption(NewDesignationForGermplasmConfirmDialog.WINDOW_NAME);
		this.setStyleName(Reindeer.WINDOW_LIGHT);
		this.addStyleName("unsaved-changes-dialog");

		// define window size, set as not resizable
		this.setWidth("544px");
		this.setHeight("180px");
		this.setResizable(false);

		// center window within the browser
		this.center();
		this.confirmLabel = new Label("<center>" + this.getConfirmationMessage() + "</center>", Label.CONTENT_XHTML);

		this.searchCreateButton = new Button(NewDesignationForGermplasmConfirmDialog.SEARCH_OR_CREATE_NEW);

		this.addNameButton = new Button(NewDesignationForGermplasmConfirmDialog.ADD_NAME_TO_GID);
		this.addNameButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		// not implemented
	}

	@SuppressWarnings("serial")
	@Override
	public void addListeners() {
		this.searchCreateButton.addListener(new Button.ClickListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = -373965708464005849L;

			@Override
			public void buttonClick(ClickEvent event) {
				NewDesignationForGermplasmConfirmDialog.this.searchOrAddANewGermplasm();
			}
		});

		this.addNameButton.addListener(new Button.ClickListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = -8698652015248607854L;

			@Override
			public void buttonClick(ClickEvent event) {
				NewDesignationForGermplasmConfirmDialog.this.addGermplasmName();
			}
		});

	}

	private void searchOrAddANewGermplasm() {
		this.source.searchOrAddANewGermplasm(this);
		this.getParent().removeWindow(this);
	}

	@Override
	public void layoutComponents() {
		this.mainLayout = new VerticalLayout();
		this.mainLayout.setSpacing(true);

		this.mainLayout.addComponent(this.confirmLabel);

		Label forSpaceLabel = new Label();
		this.mainLayout.addComponent(forSpaceLabel);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(this.searchCreateButton);
		buttonLayout.addComponent(this.addNameButton);

		this.mainLayout.addComponent(buttonLayout);
		this.mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);

		this.addComponent(this.mainLayout);

	}

	public Integer getGid() {
		return this.gid;
	}

	public Integer getIbdbUserId() {
		return this.ibdbUserId;
	}

	public Integer getDateIntValue() {
		return this.dateIntValue;
	}

	@Override
	public String getGermplasmName() {
		return this.germplasmName;
	}

	@Override
	public int getGermplasmIndex() {
		return this.germplasmIndex;
	}

	public Integer getNameMatchesCount() {
		return this.nameMatchesCount;
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

	private void addGermplasmName() {
		Name name = this.source.createNameObject(this.ibdbUserId, this.dateIntValue, this.germplasmName);

		name.setNid(null);
		name.setNstat(Integer.valueOf(0));
		name.setGermplasmId(this.gid);

		this.source.addNameToGermplasm(name, this.gid);

		this.source.removeCurrentListenerAndProcessNextItem(this);
		this.getParent().removeWindow(this);
	}

	private String getConfirmationMessage() {
		return "The name \"" + this.getGermplasmName() + "\" is not recorded as a name of GID " + this.getGid() + "."
				+ " Do you want to add the name to the GID or search/create another germplasm record?";
	}

	@Override
	public void windowClose(CloseEvent e) {
		super.close();
		this.source.closeAllImportEntryListeners();
	}

}
