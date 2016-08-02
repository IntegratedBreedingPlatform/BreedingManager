
package org.generationcp.breeding.manager.listimport.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;

public class ToolTipGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(ToolTipGenerator.class);

	final Map<Integer, GermplasmList> resultMap = new HashMap<Integer, GermplasmList>();

	final private Map<Integer, String> userNameMap;

	final private List<UserDefinedField> listTypes;

	public ToolTipGenerator(final Map<Integer, String> userNameMap, final List<UserDefinedField> listTypes) {
		this.userNameMap = userNameMap;
		this.listTypes = listTypes;

	}

	public ItemDescriptionGenerator getItemDescriptionGenerator(final List<GermplasmList> germplasmLists) {

		this.resultMap.putAll(this.convertListToMap(germplasmLists));

		return new AbstractSelect.ItemDescriptionGenerator() {

			private static final long serialVersionUID = -2669417630841097077L;

			@Override
			public String generateDescription(final Component source, final Object itemId, final Object propertyId) {
				System.out.println(source.getCaption() + "-" + itemId.toString());
				try {
					final String itemValue = itemId.toString();
					if (StringUtils.isNumeric(itemValue)) {
						final GermplasmList germplasmList = ToolTipGenerator.this.resultMap.get(Integer.valueOf(itemValue));
						if (germplasmList != null) {
							final ViewListHeaderWindow viewListHeaderWindow = new ViewListHeaderWindow(germplasmList,
									ToolTipGenerator.this.userNameMap, ToolTipGenerator.this.listTypes);
							return viewListHeaderWindow.getListHeaderComponent().toString();
						}
					}
				} catch (NumberFormatException e) {
					ToolTipGenerator.LOG.error(e.getMessage(), e);
				}
				return "";
			}
		};
	}

	private ImmutableMap<Integer, GermplasmList> convertListToMap(final List<GermplasmList> germplasmLists) {
		return Maps.uniqueIndex(germplasmLists, new Function<GermplasmList, Integer>() {

			@Override
			public Integer apply(GermplasmList from) {
				// do stuff here
				return from.getId();
			}
		});
	}
}
