
package org.generationcp.breeding.manager.listimport.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;

/**
 * Enables us to generate tools tips for a list of {@link GermplasmListEntry}
 *
 */
public class ToolTipGenerator {

	/**
	 * Note we are always adding to this map. The way Vaadin works correctly this is the only way to generated tool tips in different
	 * folders.
	 */
	final Map<Integer, GermplasmList> resultMap = new HashMap<Integer, GermplasmList>();

	/**
	 * Map of ids to user names.
	 */
	final private Map<Integer, String> userNameMap;

	/**
	 * Germplasm List types cached
	 */
	final private List<UserDefinedField> listTypes;

	public ToolTipGenerator(final Map<Integer, String> userNameMap, final List<UserDefinedField> germplasmListTypes) {
		this.userNameMap = userNameMap;
		this.listTypes = germplasmListTypes;

	}

	public ItemDescriptionGenerator getItemDescriptionGenerator(final List<GermplasmList> germplasmLists) {

		this.resultMap.putAll(this.convertListToMap(germplasmLists));

		return new AbstractSelect.ItemDescriptionGenerator() {

			private static final long serialVersionUID = -2669417630841097077L;

			@Override
			public String generateDescription(final Component source, final Object itemId, final Object propertyId) {
				final String itemValue = itemId.toString();
				if (StringUtils.isNumeric(itemValue)) {
					final GermplasmList germplasmList = ToolTipGenerator.this.resultMap.get(Integer.valueOf(itemValue));
					if (germplasmList != null) {
						final ViewListHeaderWindow viewListHeaderWindow =
								new ViewListHeaderWindow(germplasmList, ToolTipGenerator.this.userNameMap, ToolTipGenerator.this.listTypes);
						viewListHeaderWindow.instantiateComponents();
						return viewListHeaderWindow.getListHeaderComponent().toString();
					}
				}
				return "";
			}
		};
	}

	private ImmutableMap<Integer, GermplasmList> convertListToMap(final List<GermplasmList> germplasmLists) {
		return Maps.uniqueIndex(germplasmLists, new Function<GermplasmList, Integer>() {

			@Override
			public Integer apply(GermplasmList from) {
				return from.getId();
			}
		});
	}
}
