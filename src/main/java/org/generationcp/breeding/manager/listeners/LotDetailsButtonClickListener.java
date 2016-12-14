package org.generationcp.breeding.manager.listeners;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.inventory.LotDetailsMainComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class LotDetailsButtonClickListener implements Button.ClickListener {

	private final Integer gid;
	private final String germplasmName;
	private final Component source;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public LotDetailsButtonClickListener(Integer gid, String germplasmName, Component source) {
		super();
		this.gid = gid;
		this.germplasmName = germplasmName;
		this.source = source;
	}

	@Override
	public void buttonClick(Button.ClickEvent event) {
		LotDetailsMainComponent lotDetailsMainComponent = new LotDetailsMainComponent(this.gid);

		Window lotDetailsMainWindow = new BaseSubWindow(this.messageSource.getMessage(Message.LOT_DETAIL_POPUP_HEADER_TITLE, this.germplasmName, this.gid));
		lotDetailsMainWindow.setWidth("810px");
		lotDetailsMainWindow.setHeight("420px");
		lotDetailsMainWindow.addComponent(lotDetailsMainComponent);
		this.source.getWindow().addWindow(lotDetailsMainWindow);
		lotDetailsMainWindow.center();
	}
}
