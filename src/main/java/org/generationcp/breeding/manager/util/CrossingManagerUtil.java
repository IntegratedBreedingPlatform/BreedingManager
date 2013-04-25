package org.generationcp.breeding.manager.util;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;


public class CrossingManagerUtil{

    private GermplasmDataManager germplasmDataManager;


    public CrossingManagerUtil(GermplasmDataManager germplasmDataManager) {
	this.germplasmDataManager = germplasmDataManager;
    }


    public Germplasm setCrossingBreedingMethod(Germplasm gc,Integer femaleGid, Integer maleGid) throws MiddlewareQueryException{

	Germplasm gf = germplasmDataManager.getGermplasmByGID(femaleGid); // germplasm female
	Germplasm gm = germplasmDataManager.getGermplasmByGID(maleGid); // germplasm male
	Germplasm gff = germplasmDataManager.getGermplasmByGID(gf.getGpid2()); // maternal male grand parent (daddy of female parent)
	Germplasm gfm =  germplasmDataManager.getGermplasmByGID(gf.getGpid1()); // maternal female grand parent (mommy of female parent)
	Germplasm gmf = germplasmDataManager.getGermplasmByGID(gm.getGpid1()); //  paternal female grand parent (mommy of male parent)
	Germplasm gmm =  germplasmDataManager.getGermplasmByGID(gm.getGpid2()); // paternal male grand parent (daddy of male parent)

	if(gf.getGnpgs()<0)
	{
	    if(gm.getGnpgs()<0)
	    {
		gc.setMethodId(101);
	    }
	    else
	    {
		if(gm.getGnpgs()==1)
		{
		    gc.setMethodId(101);
		}
		else if(gm.getGnpgs()==2)
		{
		    if(gmf.getGid()==gf.getGid() || gmm.getGid()==gf.getGid())
		    {
			gc.setMethodId(107);
		    }
		    else
		    {
			gc.setMethodId(102);
		    }
		}
		else
		{
		    gc.setMethodId(106);
		}
	    }
	}
	else
	{
	    if(gm.getGnpgs()<0)
	    {
		if(gf.getGnpgs()==1)
		{
		    gc.setMethodId(101);
		}
		else if(gf.getGnpgs()==2)
		{
		    if(gff.getGid()==gm.getGid() || gfm.getGid()==gm.getGid())
		    {
			gc.setMethodId(107);
		    }
		    else
		    {
			gc.setMethodId(102);
		    }
		}
		else
		{
		    gc.setMethodId(106);
		}
	    }
	    else
	    {
		if(gf.getMethodId()==101 && gm.getMethodId()==101)
		{
		    gc.setMethodId(103);
		}
		else
		{
		    gc.setMethodId(106);
		}
	    }
	}

	return gc;

    }


}
