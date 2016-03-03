package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;

public class LinkButton extends Button {
	private final ExternalResource url;

	public LinkButton(final ExternalResource url, final String caption) {
		super(caption);
		this.url = url;

		setImmediate(true);
		addListener(new Button.ClickListener() {
			public void buttonClick(final ClickEvent event) {
				if (LinkButton.this.url != null) {
					LinkButton.this.getWindow().open(new ExternalResource(LinkButton.this.url.getURL()), "_self");
				}
			}
		});
	}

	public ExternalResource getResource() {
		return this.url;
	}
}
