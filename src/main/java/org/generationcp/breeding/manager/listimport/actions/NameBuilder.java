package org.generationcp.breeding.manager.listimport.actions;

import org.generationcp.middleware.pojos.Name;
import org.springframework.stereotype.Component;

@Component
public class NameBuilder implements Builder<Name,NameDataProvider> {

	@Override
	public Name build(NameDataProvider provider) {
		final Name name = new Name();

		name.setTypeId(provider.getTypeId());
		name.setUserId(provider.getUserId());
		name.setNval(provider.getName());
		name.setLocationId(provider.getLocationId());
		name.setNdate(provider.getDateValue());
		name.setReferenceId(provider.getReferenceId());
		name.setNstat(provider.getNstat());
		return name;
	}
}
