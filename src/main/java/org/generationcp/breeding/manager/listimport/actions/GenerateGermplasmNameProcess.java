package org.generationcp.breeding.manager.listimport.actions;

import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenerateGermplasmNameProcess implements Executable<GermplasmImportationContext> {

	NameBuilder nameBuilder;
	GermplasmBuilder germplasmBuilder;

	@Autowired
	public GenerateGermplasmNameProcess(NameBuilder nameBuilder, GermplasmBuilder germplasmBuilder) {
		this.nameBuilder = nameBuilder;
		this.germplasmBuilder = germplasmBuilder;
	}

	@Override
	public GermplasmImportationContext execute(GermplasmImportationContext context) throws BMSExecutionException {

		if(context.isMultipleMatches()){
			return context;
		}

		final Name name =  nameBuilder.build(context.getDataProvider());

		Map<String, Germplasm> map = context.getCreatedGermplasmMap();
		Germplasm germplasm;
		if (!map.containsKey(name.getNval())) {
			germplasm = germplasmBuilder.build(context.getDataProvider());
			map.put(name.getNval(), germplasm);
		} else {
			germplasm = map.get(name.getNval());
		}

		GermplasmName germplasmName = new GermplasmName(germplasm, name);
		context.setGermplasmNameObject(germplasmName);
		return context;
	}
}
