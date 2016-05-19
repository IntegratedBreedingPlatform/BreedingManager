
package org.generationcp.breeding.manager.germplasm.pedigree;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.WorkbenchSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Window;

@Configurable
public class GraphVizUtility {

	private static String TEMP_DIR = System.getProperty("user.dir");
	private static final Logger LOG = LoggerFactory.getLogger(GraphVizUtility.class);

	private static final String BSLASH = "\\";
	private static final String FSLASH = "/";

	/**
	 * Where is your dot program located? It will be called externally.
	 */
	private String dotPath = null;

	/**
	 * The source of the graph written in dot language.
	 */
	private StringBuilder graph = new StringBuilder();

	private String imageOutputPath = null;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	/**
	 * Constructor: creates a new GraphViz object that will contain a graph.
	 * 
	 * @throws URISyntaxException
	 * @throws FileNotFoundException
	 */
	public GraphVizUtility() throws URISyntaxException, FileNotFoundException {
	}

	/**
	 * Initialize this GraphVizUtility instance.
	 * 
	 * This method should set the path of GraphViz dot executable.
	 */
	public void initialize() {
		// set the GraphViz' dot executable path
		final String graphvizPath = "infrastructure/graphviz/bin/dot.exe";

		File dotFile = new File(graphvizPath).getAbsoluteFile();

		// use the GraphViz dot executable included in the workbench if it is available.
		final WorkbenchSetting workbenchSetting = this.workbenchDataManager.getWorkbenchSetting();
		if (workbenchSetting != null && !StringUtil.isEmpty(workbenchSetting.getInstallationDirectory())) {
			dotFile = new File(workbenchSetting.getInstallationDirectory(), graphvizPath).getAbsoluteFile();
		}

		this.dotPath = dotFile.getAbsolutePath();
	}

	public String getImageOutputPath() {
		return this.imageOutputPath;
	}

	public void setImageOutputPath(final String imageOutputPath) {
		this.imageOutputPath = imageOutputPath;
	}

	/**
	 * Returns the graph's source description in dot language.
	 * 
	 * @return Source of the graph in dot language.
	 */
	public String getDotSource() {
		return this.graph.toString();
	}

	/**
	 * Adds a string to the graph's source (without newline).
	 */
	public void add(final String line) {
		this.graph.append(line);
	}

	/**
	 * Adds a string to the graph's source (with newline).
	 */
	public void addln(final String line) {
		this.graph.append(line + "\n");
	}

	/**
	 * Adds a newline to the graph's source.
	 */
	public void addln() {
		this.graph.append('\n');
	}

	/**
	 * Returns the graph as an image in binary format.
	 * 
	 * @param dotSource Source of the graph to be drawn.
	 * @param type Type of the output image to be produced, e.g.: gif, dot, fig, pdf, ps, svg, png.
	 * @return A byte array containing the image of the graph.
	 */
	public byte[] getGraph(final String dotSource, final String type) {
		File dot;
		byte[] imgStream = null;

		try {
			dot = this.writeDotSourceToFile(dotSource);
			if (dot != null) {
				imgStream = this.getImgStream(dot, type);
				if (!dot.delete()) {
					LOG.error("Warning: " + dot.getAbsolutePath() + " could not be deleted!");
				}
				return imgStream;
			}
			return new byte[0];
		} catch (final java.io.IOException ioe) {
			LOG.error(ioe.getMessage(), ioe);
			return new byte[0];
		}
	}

	/**
	 * Writes the graph's image in a file.
	 * 
	 * @param img A byte array containing the image of the graph.
	 * @param file Name of the file to where we want to write.
	 * @return Success: 1, Failure: -1
	 */
	public int writeGraphToFile(final byte[] img, final String file) {
		final File to = new File(file);
		return this.writeGraphToFile(img, to);
	}

	/**
	 * Writes the graph's image in a file.
	 * 
	 * @param img A byte array containing the image of the graph.
	 * @param to A File object to where we want to write.
	 * @return Success: 1, Failure: -1
	 */
	public int writeGraphToFile(final byte[] img, final File to) {
		try {
			final FileOutputStream fos = new FileOutputStream(to);
			fos.write(img);
			fos.close();
		} catch (final java.io.IOException ioe) {
			LOG.error(ioe.getMessage(), ioe);
			return -1;
		}
		return 1;
	}

	/**
	 * It will call the external dot program, and return the image in binary format.
	 * 
	 * @param dot Source of the graph (in dot language).
	 * @param type Type of the output image to be produced, e.g.: gif, dot, fig, pdf, ps, svg, png.
	 * @return The image of the graph in .gif format.
	 */
	private byte[] getImgStream(final File dot, final String type) {
		File img;
		byte[] imgStream = null;

		try {
			img = File.createTempFile("graph_", "." + type, new File(GraphVizUtility.TEMP_DIR));
			final Runtime rt = Runtime.getRuntime();
			final String[] args = {this.dotPath, "-T" + type, dot.getAbsolutePath(), "-o", img.getAbsolutePath()};
			final Process p = rt.exec(args);
			p.waitFor();

			final FileInputStream in = new FileInputStream(img.getAbsolutePath());
			imgStream = new byte[in.available()];
			in.read(imgStream);
			// Close it if we need to
			if (in != null) {
				in.close();
			}

			if (!img.delete()) {
				LOG.error("Warning: " + img.getAbsolutePath() + " could not be deleted!");
			}
		} catch (final java.io.IOException ioe) {
			LOG.error("Error: In I/O processing of tempfile in dir " + GraphVizUtility.TEMP_DIR + "\n or in calling external command", ioe);
		} catch (final java.lang.InterruptedException ie) {
			LOG.error("Error: the execution of the external program was interrupted", ie);
		}

		return imgStream;
	}

	/**
	 * Writes the source of the graph in a file, and returns the written file as a File object.
	 * 
	 * @param str Source of the graph (in dot language).
	 * @return The file (as a File object) that contains the source of the graph.
	 */
	private File writeDotSourceToFile(final String str) throws java.io.IOException {
		File temp;
		try {
			temp = File.createTempFile("graph_", ".dot.tmp", new File(GraphVizUtility.TEMP_DIR));
			final FileWriter fout = new FileWriter(temp);
			fout.write(str);
			fout.close();
		} catch (final Exception e) {
			LOG.error("Error: I/O error while writing the dot source to temp file!", e);
			return null;
		}
		return temp;
	}

	/**
	 * Returns a string that is used to start a graph.
	 * 
	 * @return A string to open a graph.
	 */
	public String startGraph() {
		return "strict digraph G {";
	}

	/**
	 * Returns a string that is used to end a graph.
	 * 
	 * @return A string to close a graph.
	 */
	public String endGraph() {
		return "}";
	}

	/**
	 * Read a DOT graph from a text file.
	 * 
	 * @param input Input text file containing the DOT graph source.
	 */
	public void readSource(final String input) {
		final StringBuilder sb = new StringBuilder();

		try {
			final FileInputStream fis = new FileInputStream(input);
			final DataInputStream dis = new DataInputStream(fis);
			final BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			dis.close();
		} catch (final Exception e) {
			LOG.error("Error: " + e.getMessage(), e);
		}

		this.graph = sb;
	}

	public static String createImageOutputPathForWindow(final Window window) {
		return window.getWindow().getApplication().getContext().getBaseDirectory().getAbsolutePath().replace(BSLASH, FSLASH)
				+ "/WEB-INF/image";
	}

	public String graphVizOutputPath(final String fileName) throws URISyntaxException {
		return this.imageOutputPath + File.separator + fileName;
	}

}
