package org.generationcp.breeding.manager.pojos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImportedGermplasmList {

    private String filename;
    private String name;
    private String title;
    private String type;
    private Date date;
    private List<ImportedCondition> importedConditions;
    private List<ImportedFactor> importedFactors;
    private List<ImportedConstant> importedConstants;
    private List<ImportedVariate> importedVariates;
    private List<ImportedGermplasm> importedGermplasms;

    public ImportedGermplasmList (String filename, String name, String title, String type, Date date){
        this.filename = filename;
        this.name = name;
        this.title = title;
        this.type = type;
        this.date = date;
        this.importedFactors = new ArrayList<ImportedFactor>();
        this.importedConstants = new ArrayList<ImportedConstant>();
        this.importedVariates = new ArrayList<ImportedVariate>();
        this.importedGermplasms = new ArrayList<ImportedGermplasm>();
    }
    
    public ImportedGermplasmList (String filename, String name, String title, String type, Date date
            , List<ImportedCondition> importedConditions, List<ImportedFactor> importedFactors
            , List<ImportedConstant> importedConstants, List<ImportedVariate> importedVariates
            , List<ImportedGermplasm> importedGermplasms){
        this.filename = filename;
        this.name = name;
        this.title = title;
        this.type = type;
        this.date = date;
        this.importedConditions = importedConditions;
        this.importedFactors = importedFactors;
        this.importedConstants = importedConstants;
        this.importedVariates = importedVariates;
        this.importedGermplasms = importedGermplasms;
    }
    
    public String getFilename(){
        return filename;
    }
    
    public void setFilename(String filename){
        this.filename = filename;
    }
    
    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getTitle(){
        return title;
    }
    
    public void setTitle(String title){
        this.title = title;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type){
        this.type = type;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date){
        this.date = date;
    }

    public List<ImportedCondition> getImportedConditions(){
        return importedConditions;
    }
    
    public void setImportedConditions(List<ImportedCondition> importedConditions){
        this.importedConditions = importedConditions;
    }
    
    public void addImportedCondition(ImportedCondition importedCondition){
        this.importedConditions.add(importedCondition);
    }

    public List<ImportedFactor> getImportedFactors(){
        return importedFactors;
    }
    
    public void setImportedFactors(List<ImportedFactor> importedFactors){
        this.importedFactors = importedFactors;
    }

    public void addImportedFactor(ImportedFactor importedFactor){
        this.importedFactors.add(importedFactor);
    }    
    
    public List<ImportedConstant> getImportedConstants(){
        return importedConstants;
    }
    
    public void setImportedConstants(List<ImportedConstant> importedConstants){
        this.importedConstants = importedConstants;
    }

    public void addImportedConstant(ImportedConstant importedConstant){
        this.importedConstants.add(importedConstant);
    }
    
    public List<ImportedVariate> getImportedVariates(){
        return importedVariates;
    }
    
    public void setImportedVariates(List<ImportedVariate> importedVariates){
        this.importedVariates = importedVariates;
    }

    public void addImportedVariate(ImportedVariate importedVariate){
        this.importedVariates.add(importedVariate);
    }    

    public List<ImportedGermplasm> getImportedGermplasms(){
        return importedGermplasms;
    }
    
    public void setImportedGermplasms(List<ImportedGermplasm> importedGermplasms){
        this.importedGermplasms = importedGermplasms;
    }

    public void addImportedGermplasm(ImportedGermplasm importedGermplasm){
        this.importedGermplasms.add(importedGermplasm);
    }
    
    
};