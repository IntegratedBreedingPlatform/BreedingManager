package org.generationcp.breeding.manager.application;

import java.util.Iterator;
import java.util.Map;

import com.vaadin.terminal.ParameterHandler;

public class CrossingManagerParameterHandler implements ParameterHandler {

	private static final long serialVersionUID = 1L;

	private String nurseryId;

	@Override
	public void handleParameters(Map<String, String[]> parameters) {
		if (parameters.get("nurseryid") != null && parameters.get("nurseryid").length > 0) {
			this.nurseryId = parameters.get("nurseryid")[0];
		} else {
			this.nurseryId = "";
		}
	}

	public String getNurseryId() {
		return nurseryId;
	}
}
