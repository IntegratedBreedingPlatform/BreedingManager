package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.ListTreeComponent;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class CrossingManagerListTreeComponent extends ListTreeComponent {

	private static final long serialVersionUID = 8112173851252075693L;
	
    @Autowired
    protected SimpleResourceBundleMessageSource messageSource;

	public CrossingManagerListTreeComponent(
			ListTreeActionsListener treeActionsListener) {
		super(treeActionsListener);
	}

	@Override
	protected boolean doIncludeActionsButtons() {
		return true;
	}

	@Override
	protected String getTreeHeading() {
		return messageSource.getMessage(Message.LISTS);
	}

	@Override
	protected boolean doIncludeRefreshButton() {
		return false;
	}

	@Override
	protected boolean isTreeItemsDraggable() {
		return true;
	}

	@Override
	protected boolean doIncludeCentralLists() {
		return true;
	}

	@Override
	protected boolean doShowFoldersOnly() {
		return false;
	}
	
	@Override
	protected String getTreeHeadingStyleName() {
		return Bootstrap.Typography.H4.styleName();
	}

	@Override
	protected boolean doIncludeToogleButton() {
		return false;
	}

	@Override
	protected void toogleListTreePane() {
		// TODO Auto-generated method stub
		
	}

}
