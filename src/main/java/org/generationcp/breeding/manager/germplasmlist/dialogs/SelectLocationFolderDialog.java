
package org.generationcp.breeding.manager.germplasmlist.dialogs;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.germplasmlist.ListManagerTreeComponent;
import org.generationcp.breeding.manager.util.CloseWindowAction;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SelectLocationFolderDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -5502264917037916149L;

	private static final Logger LOG = LoggerFactory.getLogger(SelectLocationFolderDialog.class);

	private final SelectLocationFolderDialogSource source;
	private ListManagerTreeComponent germplasmListTree;

	private Button cancelButton;
	private Button selectLocationButton;

	private final Integer folderId;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	public SelectLocationFolderDialog(SelectLocationFolderDialogSource source, Integer folderId) {
		this.source = source;
		this.folderId = folderId;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.initializeComponents();
		this.initializeLayout();
	}

	private void initializeComponents() {
		this.setCaption("Select Location Folder");
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setResizable(false);
		this.setModal(true);

		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.addListener(new CloseWindowAction());

		this.selectLocationButton = new Button("Select Location");
		this.selectLocationButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.selectLocationButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -4029658156820141206L;

			@Override
			public void buttonClick(ClickEvent event) {
				Integer folderId = null;
				if (SelectLocationFolderDialog.this.germplasmListTree.getSelectedListId() instanceof Integer) {
					folderId = (Integer) SelectLocationFolderDialog.this.germplasmListTree.getSelectedListId();
				}
				try {
					if (folderId != null) {
						GermplasmList folder = SelectLocationFolderDialog.this.germplasmListManager.getGermplasmListById(folderId);
						SelectLocationFolderDialog.this.source.setSelectedFolder(folder);
					} else {
						SelectLocationFolderDialog.this.source.setSelectedFolder(null);
					}

					Window window = event.getButton().getWindow();
					window.getParent().removeWindow(window);
				} catch (MiddlewareQueryException ex) {
					SelectLocationFolderDialog.LOG.error("Error with retrieving list with id: " + folderId, ex);
				}
			}
		});

		this.germplasmListTree = new ListManagerTreeComponent(true, this.folderId);
	}

	private void initializeLayout() {
		this.setHeight("380px");
		this.setWidth("250px");
		AbsoluteLayout mainLayout = new AbsoluteLayout();

		mainLayout.addComponent(this.germplasmListTree, "top:5px;left:15px");
		mainLayout.addComponent(this.cancelButton, "top:287px;left:25px");
		mainLayout.addComponent(this.selectLocationButton, "top:287px;left:100px");

		this.setContent(mainLayout);
	}

	@Override
	public void updateLabels() {
	}

}
