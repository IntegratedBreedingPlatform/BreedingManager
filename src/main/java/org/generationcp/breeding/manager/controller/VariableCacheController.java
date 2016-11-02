package org.generationcp.breeding.manager.controller;

import java.util.List;

import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/variableCache")
public class VariableCacheController {

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@ResponseBody
	@RequestMapping(value = "/deleteVariablesFromCache", method = RequestMethod.POST)
	public ResponseEntity<String> deleteVariablesFromCache(@RequestBody final List<Integer> variablesIds) {
		this.ontologyVariableDataManager.deleteVariablesFromCache(variablesIds);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
