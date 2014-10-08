package org.generationcp.breeding.manager.listmanager.util.germplasm;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;

import com.vaadin.ui.Window;

public class CreatePedigreeGraph
{
    private GermplasmQueries qQuery;
    private GraphVizUtility gv;
    private int gid;
    private int level;
    private Window window;
    private boolean includeDerivativeLines;


    public CreatePedigreeGraph(int gid, int level, Boolean includeDerivativeLines, Window window,GermplasmQueries qQuery){
        this.qQuery=qQuery;
        this.gid=gid;
        this.level=level;
        this.window=window;
        this.includeDerivativeLines=includeDerivativeLines;
    }    
    
    public CreatePedigreeGraph(int gid, int level,Window window,GermplasmQueries qQuery){
        this.qQuery=qQuery;
        this.gid=gid;
        this.level=level;
        this.window=window;
    }

    /**
     * Construct a DOT graph in memory, convert it
     * to image and store the image in the file system.
     * @throws MalformedURLException 
     * @throws URISyntaxException 
     * @throws FileNotFoundException 
     * @throws MiddlewareQueryException 
     */
    public void create(String graphName) throws FileNotFoundException, URISyntaxException, MiddlewareQueryException 
    {
        gv = new GraphVizUtility();
        gv.initialize();
        gv.setImageOutputPath(GraphVizUtility.createImageOutputPathForWindow(window));
        gv.addln(gv.start_graph());

        createDiGraphNode();
        gv.addln(gv.end_graph());

        String type = "png";

        // Load the directory as a resource
        File out = new File(gv.graphVizOutputPath(graphName+"."+type));    // Windows
        
        //create graph
        gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );

    }

    private void createDiGraphNode() throws MiddlewareQueryException {
        GermplasmPedigreeTree germplasmPedigreeTree = this.qQuery.generatePedigreeTree(Integer.valueOf(gid), level, includeDerivativeLines);

        if(level==1){
            String leafNodeGIDRoot=germplasmPedigreeTree.getRoot().getGermplasm().getGid().toString();
            String leafNodeLabelRoot= germplasmPedigreeTree.getRoot().getGermplasm().getPreferredName().getNval() + "\n" + 
           		 "GID: " + germplasmPedigreeTree.getRoot().getGermplasm().getGid().toString();
            gv.addln(leafNodeGIDRoot+" [shape=box];");
            gv.addln(leafNodeGIDRoot+" [label=\""+leafNodeLabelRoot+"\", fontname=\"Helvetica\", fontsize=12.0];");
            gv.addln(leafNodeGIDRoot+";");
        }else{
            addNode(germplasmPedigreeTree.getRoot(), 1);
        }
    }

    private void addNode(GermplasmPedigreeTreeNode node, int level) {

        if(node.getLinkedNodes().size()==0){
            String leafNodeGIDRoot=node.getGermplasm().getGid().toString();
            String leafNodeLabelRoot= node.getGermplasm().getPreferredName().getNval() + "\n" + 
            		"GID: " + node.getGermplasm().getGid().toString();
            gv.addln(leafNodeGIDRoot+" [shape=box];");
            gv.addln(leafNodeGIDRoot+" [label=\""+leafNodeLabelRoot+"\", fontname=\"Helvetica\", fontsize=12.0];");
            gv.addln(leafNodeGIDRoot+";");        
        }
        
        for (GermplasmPedigreeTreeNode parent : node.getLinkedNodes()) {
            
            try{

                if(!parent.getGermplasm().getGid().toString().equals("0")){
                    String leafNodeGID=parent.getGermplasm().getGid().toString();
                    String leafNodeLabel= parent.getGermplasm().getPreferredName().getNval() + "\n" +
                    		"GID: " + parent.getGermplasm().getGid().toString();

                    gv.addln(leafNodeGID+" [shape=box];");
                    gv.addln(leafNodeGID+" [label=\""+leafNodeLabel+"\", fontname=\"Helvetica\", fontsize=12.0];");

                    String parentNodeGID=node.getGermplasm().getGid().toString();
                    String parentNodeLabel= node.getGermplasm().getPreferredName().getNval() + "\n" + 
                    		"GID: " + node.getGermplasm().getGid().toString();

                    gv.addln(parentNodeGID+" [shape=box];");
                    gv.addln(parentNodeGID+" [label=\""+parentNodeLabel+"\", fontname=\"Helvetica\", fontsize=12.0];");
                    

                    if(level==1){
                        String leafNodeGIDRoot=node.getGermplasm().getGid().toString();
                        String leafNodeLabelRoot= node.getGermplasm().getPreferredName().getNval() + "\n" +
                        		"GID: " + node.getGermplasm().getGid().toString();
                        gv.addln(leafNodeGIDRoot+" [shape=box];");
                        gv.addln(leafNodeGIDRoot+" [label=\""+leafNodeLabelRoot+"\", fontname=\"Helvetica\", fontsize=12.0];");
                        gv.addln(leafNodeGID+"->"+ leafNodeGIDRoot +";");
                    }
                    gv.addln(leafNodeGID+"->"+ parentNodeGID+";");
                }
                addNode(parent, level + 1);
            }catch(Exception e){
                addNode(parent, level + 1);
            }
        }
    }
}

