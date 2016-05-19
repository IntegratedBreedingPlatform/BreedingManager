/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.breeding.manager.germplasm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.search.StudyResultSet;
import org.generationcp.middleware.domain.search.filter.GidStudyQueryFilter;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Bibref;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.report.LotReportRow;
import org.generationcp.middleware.util.MaxPedigreeLevelReachedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Configurable
public class GermplasmQueries implements Serializable, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmQueries.class);

	public static final String SEARCH_OPTION_GID = "GID";
	public static final String SEARCH_OPTION_NAME = "Names";

	private GermplasmSearchResultModel germplasmResultByGID;
	private GermplasmDetailModel germplasmDetail;
	public static final String MAX_PEDIGREE_LABEL = PedigreeDataManager.MAX_PEDIGREE_LEVEL + "+ generations";

	private static final long serialVersionUID = 1L;

	@Resource
	private PlatformTransactionManager transactionManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	private PedigreeDataManager pedigreeDataManager;

	public GermplasmQueries() {

	}

	public List<GermplasmSearchResultModel> getGermplasmListResultByPrefStandardizedName(final String searchString) {
		try {
			List<Germplasm> germplasmList;
			long count;

			if (searchString.contains("%")) {
				count = this.germplasmDataManager.countGermplasmByName(searchString, Operation.LIKE);
				germplasmList = this.germplasmDataManager.getGermplasmByName(searchString, 0, (int) count, Operation.LIKE);
			} else {
				count = this.germplasmDataManager.countGermplasmByName(searchString, Operation.EQUAL);
				germplasmList = this.germplasmDataManager.getGermplasmByName(searchString, 0, (int) count, Operation.EQUAL);
			}
			final List<GermplasmSearchResultModel> toReturn = new ArrayList<GermplasmSearchResultModel>();
			for (final Germplasm g : germplasmList) {
				final Germplasm gData = g;
				final GermplasmSearchResultModel gResult = new GermplasmSearchResultModel();
				toReturn.add(this.setGermplasmSearchResult(gResult, gData));

			}
			return toReturn;
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE,
					Message.ERROR_IN_GETTING_GERMPLASM_LIST_RESULT_BY_PREFERRED_NAME);
		}
	}

	public GermplasmSearchResultModel getGermplasmResultByGID(final String gid) {
		try {
			final Germplasm gData = this.germplasmDataManager.getGermplasmByGID(new Integer(Integer.valueOf(gid)));
			final GermplasmSearchResultModel gResult = new GermplasmSearchResultModel();

			if (gData != null) {
				return this.germplasmResultByGID = this.setGermplasmSearchResult(gResult, gData);
			} else {
				return null;
			}
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SEARCH);
		}
	}

	private GermplasmSearchResultModel setGermplasmSearchResult(final GermplasmSearchResultModel gResult, final Germplasm gData) {
		try {
			gResult.setGid(gData.getGid());
			gResult.setNames(this.getGermplasmNames(gData.getGid()));

			final Method method = this.germplasmDataManager.getMethodByID(gData.getMethodId());
			if (method != null) {
				gResult.setMethod(method.getMname());
			} else {
				gResult.setMethod("");
			}

			final Location loc = this.germplasmDataManager.getLocationByID(gData.getLocationId());
			if (loc != null) {
				gResult.setLocation(loc.getLname());
			} else {
				gResult.setLocation("");
			}

			return gResult;
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SEARCH);
		}
	}

	public GermplasmDetailModel getGermplasmDetails(final int gid) {
		try {
			this.germplasmDetail = new GermplasmDetailModel();
			final Germplasm g = this.germplasmDataManager.getGermplasmByGID(new Integer(gid));
			final Name name = this.germplasmDataManager.getPreferredNameByGID(gid);

			if (g != null) {
				this.germplasmDetail.setGid(g.getGid());
				this.germplasmDetail.setGermplasmMethod(this.germplasmDataManager.getMethodByID(g.getMethodId()).getMname());
				this.germplasmDetail.setGermplasmPreferredName(name == null ? "" : name.getNval());
				this.germplasmDetail.setGermplasmCreationDate(String.valueOf(g.getGdate()));
				this.germplasmDetail.setPrefID(this.getGermplasmPrefID(g.getGid()));
				this.germplasmDetail.setGermplasmLocation(this.getLocation(g.getLocationId()));
				this.germplasmDetail.setReference(this.getReference(g.getReferenceId()));
				this.germplasmDetail.setmGid(g.getMgid());
			}
			return this.germplasmDetail;
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_GERMPLASM_DETAILS);
		}
	}

	public List<GermplasmDetailModel> getGenerationHistory(final Integer gid) {
		try {
			final List<GermplasmDetailModel> toreturn = new ArrayList<GermplasmDetailModel>();
			List<Germplasm> generationHistoryList = new ArrayList<Germplasm>();

			generationHistoryList = this.pedigreeDataManager.getGenerationHistory(new Integer(gid));
			for (final Germplasm g : generationHistoryList) {
				final GermplasmDetailModel genHistory = new GermplasmDetailModel();
				final String name = g.getPreferredName() != null ? g.getPreferredName().getNval() : "";
				genHistory.setGid(g.getGid());
				genHistory.setGermplasmPreferredName(name);
				toreturn.add(genHistory);
			}
			return toreturn;
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_GENERATION_HISTORY);
		}
	}

	public List<GermplasmNamesAttributesModel> getNames(final int gid) {
		try {
			final List<Name> names = this.germplasmDataManager.getNamesByGID(gid, null, null);
			final List<GermplasmNamesAttributesModel> germplasmNames = new ArrayList<GermplasmNamesAttributesModel>();

			for (final Name n : names) {
				final GermplasmNamesAttributesModel gNamesRow = new GermplasmNamesAttributesModel();
				gNamesRow.setName(n.getNval());
				gNamesRow.setLocation(this.getLocation(n.getLocationId()));

				final UserDefinedField type = this.germplasmDataManager.getUserDefinedFieldByID(n.getTypeId());
				if (type != null) {
					gNamesRow.setType(type.getFcode());
					gNamesRow.setTypeDesc(type.getFname());
				}

				gNamesRow.setDate(n.getNdate().toString());
				germplasmNames.add(gNamesRow);
			}
			return germplasmNames;
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_NAMES_BY_GERMPLASM_ID);
		}
	}

	public List<GermplasmNamesAttributesModel> getAttributes(final int gid) {
		try {
			final List<Attribute> attr = this.germplasmDataManager.getAttributesByGID(gid);
			final List<GermplasmNamesAttributesModel> germplasmAttributes = new ArrayList<GermplasmNamesAttributesModel>();

			for (final Attribute a : attr) {
				final GermplasmNamesAttributesModel gAttributeRow = new GermplasmNamesAttributesModel();
				gAttributeRow.setName(a.getAval());

				final Location location = this.germplasmDataManager.getLocationByID(a.getLocationId());
				if (location != null) {
					gAttributeRow.setLocation(location.getLname());
				}

				final UserDefinedField type = this.germplasmDataManager.getUserDefinedFieldByID(a.getTypeId());
				if (type != null) {
					gAttributeRow.setType(type.getFcode());
					gAttributeRow.setTypeDesc(type.getFname());
				}

				gAttributeRow.setDate(a.getAdate().toString());
				germplasmAttributes.add(gAttributeRow);
			}
			return germplasmAttributes;
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_ATTRIBUTES_BY_GERMPLASM_ID);
		}
	}

	private String getGermplasmPrefID(final int gid) {
		try {
			final List<Name> names = this.germplasmDataManager.getNamesByGID(gid, 8, null);
			String prefId = "";
			for (final Name n : names) {
				if (n.getNstat() == 8) {
					prefId = n.getNval();
					break;
				}

			}
			return prefId;
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_NAMES_BY_GERMPLASM_ID);
		}
	}


	public Integer getPedigreeLevelCount(final Integer gid, final Boolean includeDerivativeLines) throws MiddlewareQueryException {
		return this.getPedigreeLevelCount(gid, includeDerivativeLines, false);
	}

	public Integer getPedigreeLevelCount(final Integer gid, final Boolean includeDerivativeLines, final Boolean calculateFullPedigree)
			throws MiddlewareQueryException {
		return this.pedigreeDataManager.countPedigreeLevel(gid, includeDerivativeLines, calculateFullPedigree);
	}

	public String getPedigreeLevelCountLabel(final Integer gid, final Boolean includeDerivativeLines) throws MiddlewareQueryException {
		return this.getPedigreeLevelCountLabel(gid, includeDerivativeLines, false);
	}

	public String getPedigreeLevelCountLabel(final Integer gid, final Boolean includeDerivativeLines, final Boolean calculateFullPedigree)
			throws MiddlewareQueryException {
		String label;
		try {
			final Integer pedigreeLevelCount = this.getPedigreeLevelCount(gid, includeDerivativeLines, calculateFullPedigree);

			if (pedigreeLevelCount > 1) {
				label = pedigreeLevelCount + " generations";
			} else {
				label = pedigreeLevelCount + " generation";
			}
		} catch (final MaxPedigreeLevelReachedException e) {
			GermplasmQueries.LOG.debug("Max pedigree level reached", e);
			label = GermplasmQueries.MAX_PEDIGREE_LABEL;
		}

		return label;
	}

	private String getGermplasmNames(final int gid) {

		try {
			final List<Name> names = this.germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
			final StringBuilder germplasmNames = new StringBuilder("");
			int i = 0;
			for (final Name n : names) {
				if (i < names.size() - 1) {
					germplasmNames.append(n.getNval() + ",");
				} else {
					germplasmNames.append(n.getNval());
				}
				i++;
			}

			return germplasmNames.toString();
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_NAMES_BY_GERMPLASM_ID);
		}
	}


	private String getReference(final int refId) throws MiddlewareQueryException {
		final Bibref bibRef = this.germplasmDataManager.getBibliographicReferenceByID(refId);

		if (bibRef != null) {
			return bibRef.getAnalyt();
		} else {
			return "";
		}

	}

	private String getLocation(final int locId) {
		try {
			final Location x = this.germplasmDataManager.getLocationByID(locId);
			return x.getLname();
		} catch (final Exception e) {
			return "";
		}
	}

	public GermplasmPedigreeTree generatePedigreeTree(final Integer gid, final int i) {
		try {
			return this.pedigreeDataManager.generatePedigreeTree(gid, i);
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GENERATING_PEDIGREE_TREE);
		}
	}

	public GermplasmPedigreeTree generatePedigreeTree(final Integer gid, final int i, final Boolean includeDerivativeLines) {
		try {
			return this.pedigreeDataManager.generatePedigreeTree(gid, i, includeDerivativeLines);
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GENERATING_PEDIGREE_TREE);
		}
	}

	public GermplasmPedigreeTree getDerivativeNeighborhood(final Integer gid, final int numberOfStepsBackward,
			final int numberOfStepsForward) {
		try {
			return this.pedigreeDataManager.getDerivativeNeighborhood(gid, numberOfStepsBackward, numberOfStepsForward);
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_DERIVATIVE_NEIGHBORHOOD);
		}
	}

	public GermplasmPedigreeTree getMaintenanceNeighborhood(final Integer gid, final int numberOfStepsBackward,
			final int numberOfStepsForward) {
		try {
			return this.pedigreeDataManager.getMaintenanceNeighborhood(gid, numberOfStepsBackward, numberOfStepsForward);
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_DERIVATIVE_NEIGHBORHOOD);
		}
	}

	public List<LotReportRow> getReportOnLotsByEntityTypeAndEntityId(final String type, final Integer gid) {
		List<LotReportRow> result = new ArrayList<LotReportRow>();
		try {
			final long count = this.inventoryDataManager.countLotsByEntityTypeAndEntityId(type, gid);
			result = this.inventoryDataManager.generateReportOnLotsByEntityTypeAndEntityId(type, gid, 0, (int) count);
		} catch (final MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE,
					Message.ERROR_IN_GETTING_REPORT_ON_LOTS_BY_ENTITY_TYPE_AND_ENTITY_ID);
		}
		return result;
	}

	public List<StudyReference> getGermplasmStudyInfo(final int gid) {

		final List<StudyReference> results = new ArrayList<StudyReference>();
		try {

			final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					GidStudyQueryFilter gidFilter = new GidStudyQueryFilter(gid);
					StudyResultSet resultSet = GermplasmQueries.this.studyDataManager.searchStudies(gidFilter, 50);
					while (resultSet.hasMore()) {
						StudyReference reference = resultSet.next();
						results.add(reference);
					}
				}

			});

		} catch (final MiddlewareQueryException e) {

			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GERMPLASM_STUDY_INFORMATION_BY_GERMPLASM_ID);
		}
		return results;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Inherited abstract method
	}
}
