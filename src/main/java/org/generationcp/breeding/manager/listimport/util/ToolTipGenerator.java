
package org.generationcp.breeding.manager.listimport.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private ViewListHeader viewHeaderCreator;

	/**
	 * @param userNameMap map of user ids to user map
	 * @param germplasmListTypes list of all germplasm types
	 */
	public ToolTipGenerator(final Map<Integer, String> userNameMap, final List<UserDefinedField> germplasmListTypes) {
		this.userNameMap = userNameMap;
		this.listTypes = germplasmListTypes;
		this.viewHeaderCreator = new ViewListHeader() {

			@Override
			public ViewListHeaderWindow createViewListHeaderWindow(GermplasmList germplasmList) {
				return new ViewListHeaderWindow(germplasmList, ToolTipGenerator.this.userNameMap, ToolTipGenerator.this.listTypes);
			}
		};
	}

	/**
	 * Constructor only for testing
	 * 
	 * @param userNameMap map of user ids to user map
	 * @param germplasmListTypes list of all germplasm types
	 * @param viewHeaderCreator inteface that creates the {@link ViewListHeaderWindow} window. This is passed in so that we can call the
	 *        {@link ViewListHeaderWindow}.instantiateComponents. This prevents null pointers in the test. Note the instantiateComponents is
	 *        called by our dynamic class loader.
	 * 
	 */
	public ToolTipGenerator(final Map<Integer, String> userNameMap, final List<UserDefinedField> germplasmListTypes,
			final ViewListHeader viewHeaderCreator) {
		this.userNameMap = userNameMap;
		this.listTypes = germplasmListTypes;
		this.viewHeaderCreator = viewHeaderCreator;
	}

	public ItemDescriptionGenerator getItemDescriptionGenerator(final Set<GermplasmList> existingGermplasmList) {

		this.resultMap.putAll(this.convertListToMap(existingGermplasmList));

		return new AbstractSelect.ItemDescriptionGenerator() {

			private static final long serialVersionUID = -2669417630841097077L;

			@Override
			public String generateDescription(final Component source, final Object itemId, final Object propertyId) {
				final String itemValue = itemId.toString();
				if (StringUtils.isNumeric(itemValue)) {
					final GermplasmList germplasmList = ToolTipGenerator.this.resultMap.get(Integer.valueOf(itemValue));
					if (germplasmList != null) {
						final ViewListHeaderWindow viewListHeaderWindow = viewHeaderCreator.createViewListHeaderWindow(germplasmList);
						return viewListHeaderWindow.getListHeaderComponent().toString();
					}
				}
				return "";
			}

		};
	}

	/**
	 * Interface to enable testing. This is only there because of ones love for testing.
	 *
	 */
	interface ViewListHeader {

		ViewListHeaderWindow createViewListHeaderWindow(final GermplasmList germplasmList);
	}

	private ImmutableMap<Integer, GermplasmList> convertListToMap(final Set<GermplasmList> existingGermplasmList) {
		return Maps.uniqueIndex(existingGermplasmList, new Function<GermplasmList, Integer>() {

			@Override
			public Integer apply(GermplasmList from) {
				return from.getId();
			}
		});
	}
}
