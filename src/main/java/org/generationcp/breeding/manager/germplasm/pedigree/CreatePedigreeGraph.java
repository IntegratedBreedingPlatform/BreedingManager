
package org.generationcp.breeding.manager.germplasm.pedigree;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.generationcp.breeding.manager.germplasm.GermplasmQueries;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Window;

public class CreatePedigreeGraph {

	private static final Logger LOG = LoggerFactory.getLogger(CreatePedigreeGraph.class);

	private final GermplasmQueries qQuery;
	private GraphVizUtility gv;
	private final int gid;
	private final int level;
	private final Window window;
	private boolean includeDerivativeLines;

	public CreatePedigreeGraph(final int gid, final int level, final Boolean includeDerivativeLines, final Window window,
			final GermplasmQueries qQuery) {
		this.qQuery = qQuery;
		this.gid = gid;
		this.level = level;
		this.window = window;
		this.includeDerivativeLines = includeDerivativeLines;
	}

	public CreatePedigreeGraph(final int gid, final int level, final Window window, final GermplasmQueries qQuery) {
		this.qQuery = qQuery;
		this.gid = gid;
		this.level = level;
		this.window = window;
	}

	/**
	 * Construct a DOT graph in memory, convert it to image and store the image in the file system.
	 * 
	 * @param graphName
	 */
	public void create(final String graphName) {
		try {
			this.gv = new GraphVizUtility();
			this.create(graphName, this.gv);
		} catch (FileNotFoundException | URISyntaxException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Construct a DOT graph in memory, convert it to image and store the image in the file system.
	 * 
	 * @param graphName
	 * @param gv
	 */
	public void create(final String graphName, final GraphVizUtility gv) {
		this.gv = gv;
		this.gv.initialize();
		this.gv.setImageOutputPath(GraphVizUtility.createImageOutputPathForWindow(this.window));
		this.gv.addln(this.gv.startGraph());

		this.createDiGraphNode();
		this.gv.addln(this.gv.endGraph());

		final String type = "png";

		// Load the directory as a resource
		File out;
		try {

			out = new File(this.gv.graphVizOutputPath(graphName + "." + type));
			// create graph
			this.gv.writeGraphToFile(this.gv.getGraph(this.gv.getDotSource(), type), out);

		} catch (final URISyntaxException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private void createDiGraphNode() {
		final GermplasmPedigreeTree germplasmPedigreeTree =
				this.qQuery.generatePedigreeTree(Integer.valueOf(this.gid), this.level, this.includeDerivativeLines);

		if (this.level == 1) {
			final String leafNodeGIDRoot = this.createNodeTextWithFormatting(germplasmPedigreeTree.getRoot());
			this.gv.addln(leafNodeGIDRoot + ";");
		} else {
			this.addNode(germplasmPedigreeTree.getRoot(), 1);
		}
	}

	private String createNodeTextWithFormatting(final GermplasmPedigreeTreeNode node) {
		final String leafNodeGIDRoot = node.getGermplasm().getGid().toString();
		final String leafNodeLabelRoot =
				node.getGermplasm().getPreferredName().getNval() + "\n" + "GID: " + node.getGermplasm().getGid().toString();
		this.gv.addln(leafNodeGIDRoot + " [shape=box];");
		this.gv.addln(leafNodeGIDRoot + " [label=\"" + leafNodeLabelRoot + "\", fontname=\"Helvetica\", fontsize=12.0, ordering=\"in\"];");
		return leafNodeGIDRoot;
	}

	private void addNode(final GermplasmPedigreeTreeNode node, final int level) {

		if (node.getLinkedNodes().isEmpty()) {
			final String leafNodeGIDRoot = this.createNodeTextWithFormatting(node);
			this.gv.addln(leafNodeGIDRoot + ";");
		}

		for (final GermplasmPedigreeTreeNode parent : node.getLinkedNodes()) {

			if (!"0".equalsIgnoreCase(parent.getGermplasm().getGid().toString())) {

				final String leafNodeGID = this.createNodeTextWithFormatting(parent);
				final String parentNodeGID = this.createNodeTextWithFormatting(node);

				if (level == 1) {
					final String leafNodeGIDRoot = this.createNodeTextWithFormatting(node);
					this.gv.addln(leafNodeGID + "->" + leafNodeGIDRoot + ";");
				} else {
					this.gv.addln(leafNodeGID + "->" + parentNodeGID + ";");
				}
			}

			this.addNode(parent, level + 1);
		}
	}

}
