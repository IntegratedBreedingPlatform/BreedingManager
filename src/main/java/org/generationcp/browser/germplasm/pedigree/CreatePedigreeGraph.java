package org.generationcp.browser.germplasm.pedigree;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.generationcp.browser.germplasm.GermplasmDetail;
import org.generationcp.browser.germplasm.GermplasmQueries;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;

import com.vaadin.ui.Window;

public class CreatePedigreeGraph
{

	/**
	 * 
	 */

	private GermplasmQueries qQuery;
	private GraphVizUtility gv;
	private int gid;
	private int level;
	private Window window;


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

		// used for testing
		/*
		gv.addln("50544 [shape=box];");
		gv.addln("50547 [shape=box];");
		gv.addln("50547 [URL=\"http://www.google.com\"];");
		gv.addln("50544 [label=\"A 64(50544)\"];");
		gv.addln("50547 [label=\"A 87(50594)\"];");
		gv.addln("50544->50547;");
		gv.addln("50566 [shape=box];");
		gv.addln("50566 [label=\"A 89(50594)\",URL=\"http://www.google.com\"];");
		gv.addln("50544->50566;");
		gv.addln("nodeB [shape=none];");
		gv.addln("nodeB [label=\"\"];");
		gv.addln("50566->nodeB [style=\"dashed\"];");
		*/

		createDiGraphNode();
		gv.addln(gv.end_graph());

		System.out.println(gv.toString());


		//		System.out.println(gv.getDotSource());

		//		String type = "gif";
		//      String type = "dot";
		//      String type = "fig";    // open with xfig
		//      String type = "pdf";
		//      String type = "ps";
		//      String type = "svg";    // open with inkscape
		String type = "png";


		// Load the directory as a resource

		File out = new File(gv.graphVizOutputPath(graphName+"."+type));    // Windows
		//create graph
		gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );

	}

	private void createDiGraphNode() throws MiddlewareQueryException {
		GermplasmPedigreeTree germplasmPedigreeTree = this.qQuery.generatePedigreeTree(Integer.valueOf(gid), level);

		if(level==1){
			String leafNodeGIDRoot=germplasmPedigreeTree.getRoot().getGermplasm().getGid().toString();
			String leafNodeLabelRoot=germplasmPedigreeTree.getRoot().getGermplasm().getPreferredName().getNval()+ "("+germplasmPedigreeTree.getRoot().getGermplasm().getGid().toString()+")";
			gv.addln(leafNodeGIDRoot+" [shape=box];");
			gv.addln(leafNodeGIDRoot+" [label=\""+leafNodeLabelRoot+"\"];");
			gv.addln(leafNodeGIDRoot+";");
		}else{
			addNode(germplasmPedigreeTree.getRoot(), 1);
		}
	}

	private void addNode(GermplasmPedigreeTreeNode node, int level) {

		

		for (GermplasmPedigreeTreeNode parent : node.getLinkedNodes()) {
			try{

				if(!parent.getGermplasm().getGid().toString().equals("0")){
					String leafNodeGID=parent.getGermplasm().getGid().toString();
					String leafNodeLabel=parent.getGermplasm().getPreferredName().getNval()+ "("+parent.getGermplasm().getGid().toString()+")";

					gv.addln(leafNodeGID+" [shape=box];");
					gv.addln(leafNodeGID+" [label=\""+leafNodeLabel+"\"];");
					gv.addln(leafNodeGID+" [URL=http://google.com];");

					String parentNodeGID=node.getGermplasm().getGid().toString();
					String parentNodeLabel=node.getGermplasm().getPreferredName().getNval()+"("+node.getGermplasm().getGid().toString()+")";

					gv.addln(parentNodeGID+" [shape=box];");
					gv.addln(parentNodeGID+" [label=\""+parentNodeLabel+"\"];");
					

					if(level==1){
						String leafNodeGIDRoot=node.getGermplasm().getGid().toString();
						String leafNodeLabelRoot=node.getGermplasm().getPreferredName().getNval()+ "("+node.getGermplasm().getGid().toString()+")";
						gv.addln(leafNodeGIDRoot+" [shape=box];");
						gv.addln(leafNodeGIDRoot+" [label=\""+leafNodeLabelRoot+"\"];");
//						gv.addln(leafNodeGIDRoot+" [URL=http://google.com];");
						gv.addln(leafNodeGIDRoot+"->"+leafNodeGID+";");
					}
					gv.addln(parentNodeGID+"->"+leafNodeGID+";");
					
					if(parent.getLinkedNodes().isEmpty()){
						
						gv.addln(leafNodeGID+level+"[shape=none];");
						gv.addln(leafNodeGID+level+"[label=\"\"];");
//						gv.addln(leafNodeGID+level+"[URL=\"http://google.com\"];");
						gv.addln(leafNodeGID+"->"+leafNodeGID+level +" [style=\"dashed\"];");
					}
				}
				addNode(parent, level + 1);
			}catch(Exception e){
				System.out.println("Error Graph");
				addNode(parent, level + 1);
			}
		}
	}


}
