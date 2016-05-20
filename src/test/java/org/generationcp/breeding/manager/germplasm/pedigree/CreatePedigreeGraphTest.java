
package org.generationcp.breeding.manager.germplasm.pedigree;

import java.io.File;
import java.net.URISyntaxException;

import org.generationcp.breeding.manager.germplasm.GermplasmQueries;
import org.generationcp.middleware.data.initializer.GermplasmPedigreeTreeTestDataInitializer;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.ui.Window;

public class CreatePedigreeGraphTest {

	private static final String PATH = "C:\\SamplePath";
	private static final String FILE_TYPE = "png";
	private static final String SAMPLE_GRAPH = "SampleGraph";
	private static final int LEVEL = 3;
	private static final int GID = 1000;
	@Mock
	private GermplasmQueries qQuery;
	@Mock
	private GraphVizUtility gv;
	@Mock
	private Window window;
	@Mock
	private Application application;
	@Mock
	private ApplicationContext applicationContext;

	private CreatePedigreeGraph createPedigreeGraph;

	private final File sampleFile = new File("Sample File");
	private GermplasmPedigreeTree germplasmPedigreeTree;
	private GermplasmPedigreeTreeTestDataInitializer germplasmPedigreeTreeTDI;

	@Before
	public void setUp() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);
		this.createPedigreeGraph = new CreatePedigreeGraph(GID, LEVEL, this.window, this.qQuery);

		Mockito.doReturn(this.window).when(this.window).getWindow();
		Mockito.doReturn(this.application).when(this.window).getApplication();
		Mockito.doReturn(this.applicationContext).when(this.application).getContext();
		Mockito.doReturn(this.sampleFile).when(this.applicationContext).getBaseDirectory();

		this.germplasmPedigreeTreeTDI = new GermplasmPedigreeTreeTestDataInitializer();
		this.germplasmPedigreeTree = this.germplasmPedigreeTreeTDI.createGermplasmPedigreeTree(GID, LEVEL);
		Mockito.doReturn(this.germplasmPedigreeTree).when(this.qQuery).generatePedigreeTree(Integer.valueOf(GID), LEVEL, false);

		Mockito.doReturn(PATH).when(this.gv).graphVizOutputPath(SAMPLE_GRAPH + "." + FILE_TYPE);
	}

	@Test
	public void testCreate() {
		this.createPedigreeGraph.create(SAMPLE_GRAPH, this.gv);
		this.verifyIfEveryLinkedNodeHasBeenAdded(this.germplasmPedigreeTree.getRoot());
	}

	private void verifyIfEveryLinkedNodeHasBeenAdded(final GermplasmPedigreeTreeNode node) {
		if (node.getLinkedNodes().isEmpty()) {
			Mockito.verify(this.gv, Mockito.atLeast(1)).addln(node.getGermplasm().getGid() + ";");
		} else {
			for (final GermplasmPedigreeTreeNode parentNode : node.getLinkedNodes()) {
				Mockito.verify(this.gv, Mockito.atLeast(1)).addln(
						parentNode.getGermplasm().getGid() + "->" + node.getGermplasm().getGid() + ";");
				this.verifyIfEveryLinkedNodeHasBeenAdded(parentNode);
			}
		}
	}

}
