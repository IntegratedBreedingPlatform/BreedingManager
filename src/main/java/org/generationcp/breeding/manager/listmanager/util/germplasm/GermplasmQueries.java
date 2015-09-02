
package org.generationcp.breeding.manager.listmanager.util.germplasm;

import java.io.Serializable;
import java.util.ArrayList;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.util.GermplasmDetailModel;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Bibref;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class GermplasmQueries implements Serializable, InitializingBean {

	private GermplasmDetailModel germplasmDetail;

	private static final long serialVersionUID = 1L;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PedigreeDataManager pedigreeDataManager;

	public GermplasmQueries() {

	}

	public GermplasmDetailModel getGermplasmDetails(int gid) {
		try {
			this.germplasmDetail = new GermplasmDetailModel();
			Germplasm g = this.germplasmDataManager.getGermplasmByGID(new Integer(gid));
			Name name = this.germplasmDataManager.getPreferredNameByGID(gid);

			if (g != null) {
				this.germplasmDetail.setGid(g.getGid());
				this.germplasmDetail.setGermplasmMethod(this.germplasmDataManager.getMethodByID(g.getMethodId()).getMname());
				this.germplasmDetail.setGermplasmPreferredName(name == null ? "" : name.getNval());
				this.germplasmDetail.setGermplasmCreationDate(name == null ? "" : String.valueOf(g.getGdate()));
				this.germplasmDetail.setGermplasmLocation(this.getLocation(g.getLocationId()));
				this.germplasmDetail.setReference(this.getReference(g.getReferenceId()));
			}
			return this.germplasmDetail;
		} catch (MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_GERMPLASM_DETAILS);
		}
	}

	@SuppressWarnings("deprecation")
	public ArrayList<GermplasmAttributeModel> getAttributes(int gid) {
		try {
			ArrayList<Attribute> attr = (ArrayList<Attribute>) this.germplasmDataManager.getAttributesByGID(gid);
			ArrayList<GermplasmAttributeModel> germplasmAttributes = new ArrayList<GermplasmAttributeModel>();

			for (Attribute a : attr) {
				GermplasmAttributeModel gAttributeRow = new GermplasmAttributeModel();
				gAttributeRow.setName(a.getAval());

				Location location = this.germplasmDataManager.getLocationByID(a.getLocationId());
				if (location != null) {
					gAttributeRow.setLocation(location.getLname());
				}

				UserDefinedField type = this.germplasmDataManager.getUserDefinedFieldByID(a.getTypeId());
				if (type != null) {
					gAttributeRow.setType(type.getFcode());
					gAttributeRow.setTypeDesc(type.getFname());
				}

				gAttributeRow.setDate(a.getAdate().toString());
				germplasmAttributes.add(gAttributeRow);
			}
			return germplasmAttributes;
		} catch (MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_ATTRIBUTES_BY_GERMPLASM_ID);
		}
	}

	private String getReference(int refId) {
		Bibref bibRef = this.germplasmDataManager.getBibliographicReferenceByID(refId);
		if (bibRef != null) {
			return bibRef.getAnalyt();
		} else {
			return "";
		}

	}

	@SuppressWarnings("deprecation")
	private String getLocation(int locId) {
		try {
			Location x = this.germplasmDataManager.getLocationByID(locId);
			return x.getLname();
		} catch (Exception e) {
			return ""; // TODO: Verify that this doesn't need ui error notification and really just returns ""
		}
	}

	public GermplasmPedigreeTree generatePedigreeTree(Integer gid, int i) {
		try {
			return this.pedigreeDataManager.generatePedigreeTree(gid, i);
		} catch (MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GENERATING_PEDIGREE_TREE);
		}
	}

	public GermplasmPedigreeTree generatePedigreeTree(Integer gid, int i, Boolean includeDerivativeLines) {
		try {
			return this.pedigreeDataManager.generatePedigreeTree(gid, i, includeDerivativeLines);
		} catch (MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GENERATING_PEDIGREE_TREE);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// not implemented
	}

}
