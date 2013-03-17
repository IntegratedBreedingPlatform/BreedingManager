package org.generationcp.browser.germplasm.pedigree;

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
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.WorkbenchSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Window;

@Configurable
public class GraphVizUtility
{

	
	private static String TEMP_DIR =System.getProperty("user.dir");

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
	 * Constructor: creates a new GraphViz object that will contain
	 * a graph.
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
        String graphvizPath = "tools/graphviz/bin/dot.exe";

        File dotFile = new File(graphvizPath).getAbsoluteFile();
        try {
            // use the GraphViz dot executable included in the workbench if it is available.
            WorkbenchSetting workbenchSetting = workbenchDataManager.getWorkbenchSetting();
            if (workbenchSetting != null && !StringUtil.isEmpty(workbenchSetting.getInstallationDirectory())) {
                dotFile = new File(workbenchSetting.getInstallationDirectory(), graphvizPath).getAbsoluteFile();
            }
        }
        catch (MiddlewareQueryException e) {
            // intentionally empty
        }

        dotPath = dotFile.getAbsolutePath();
    }
    
	public String getImageOutputPath() {
        return imageOutputPath;
    }

    public void setImageOutputPath(String imageOutputPath) {
        this.imageOutputPath = imageOutputPath;
    }

    /**
	 * Returns the graph's source description in dot language.
	 * @return Source of the graph in dot language.
	 */
	public String getDotSource() {
		return graph.toString();
	}

	/**
	 * Adds a string to the graph's source (without newline).
	 */
	public void add(String line) {
		graph.append(line);
	}

	/**
	 * Adds a string to the graph's source (with newline).
	 */
	public void addln(String line) {
		graph.append(line + "\n");
	}

	/**
	 * Adds a newline to the graph's source.
	 */
	public void addln() {
		graph.append('\n');
	}

	/**
	 * Returns the graph as an image in binary format.
	 * @param dot_source Source of the graph to be drawn.
	 * @param type Type of the output image to be produced, e.g.: gif, dot, fig, pdf, ps, svg, png.
	 * @return A byte array containing the image of the graph.
	 */
	public byte[] getGraph(String dot_source, String type)
	{
		File dot;
		byte[] img_stream = null;

		try {
			dot = writeDotSourceToFile(dot_source);
			if (dot != null)
			{
				img_stream = get_img_stream(dot, type);
				if (dot.delete() == false) 
					System.err.println("Warning: " + dot.getAbsolutePath() + " could not be deleted!");
				return img_stream;
			}
			return null;
		} catch (java.io.IOException ioe) { return null; }
	}


	
	/**
	 * Writes the graph's image in a file.
	 * @param img   A byte array containing the image of the graph.
	 * @param file  Name of the file to where we want to write.
	 * @return Success: 1, Failure: -1
	 */
	public int writeGraphToFile(byte[] img, String file)
	{
		File to = new File(file);
		return writeGraphToFile(img, to);
	}

	/**
	 * Writes the graph's image in a file.
	 * @param img   A byte array containing the image of the graph.
	 * @param to    A File object to where we want to write.
	 * @return Success: 1, Failure: -1
	 */
	public int writeGraphToFile(byte[] img, File to)
	{
		try {
			FileOutputStream fos = new FileOutputStream(to);
			fos.write(img);
			fos.close();
		} catch (java.io.IOException ioe) { return -1; }
		return 1;
	}

	/**
	 * It will call the external dot program, and return the image in
	 * binary format.
	 * @param dot Source of the graph (in dot language).
	 * @param type Type of the output image to be produced, e.g.: gif, dot, fig, pdf, ps, svg, png.
	 * @return The image of the graph in .gif format.
	 */
	private byte[] get_img_stream(File dot, String type)
	{
		File img;
		byte[] img_stream = null;

		try {
			img = File.createTempFile("graph_", "."+type, new File(GraphVizUtility.TEMP_DIR));
			Runtime rt = Runtime.getRuntime();
			String[] args = {dotPath, "-T"+type, dot.getAbsolutePath(), "-o", img.getAbsolutePath()};
			Process p = rt.exec(args);
			p.waitFor();

			FileInputStream in = new FileInputStream(img.getAbsolutePath());
			img_stream = new byte[in.available()];
			in.read(img_stream);
			// Close it if we need to
			if( in != null ) in.close();

			if (img.delete() == false) 
				System.err.println("Warning: " + img.getAbsolutePath() + " could not be deleted!");
		}
		catch (java.io.IOException ioe) {
			System.err.println("Error:    in I/O processing of tempfile in dir " + GraphVizUtility.TEMP_DIR+"\n");
			System.err.println("       or in calling external command");
			ioe.printStackTrace();
		}
		catch (java.lang.InterruptedException ie) {
			System.err.println("Error: the execution of the external program was interrupted");
			ie.printStackTrace();
		}

		return img_stream;
	}

	/**
	 * Writes the source of the graph in a file, and returns the written file
	 * as a File object.
	 * @param str Source of the graph (in dot language).
	 * @return The file (as a File object) that contains the source of the graph.
	 */
	private File writeDotSourceToFile(String str) throws java.io.IOException
	{
		File temp;
		try {
			temp = File.createTempFile("graph_", ".dot.tmp", new File(GraphVizUtility.TEMP_DIR));
			FileWriter fout = new FileWriter(temp);
			fout.write(str);
			fout.close();
		}
		catch (Exception e) {
			System.err.println("Error: I/O error while writing the dot source to temp file!");
			return null;
		}
		return temp;
	}

	/**
	 * Returns a string that is used to start a graph.
	 * @return A string to open a graph.
	 */
	public String start_graph() {
		return "strict digraph G {";
	}

	/**
	 * Returns a string that is used to end a graph.
	 * @return A string to close a graph.
	 */
	public String end_graph() {
		return "}";
	}

	/**
	 * Read a DOT graph from a text file.
	 * 
	 * @param input Input text file containing the DOT graph
	 * source.
	 */
	public void readSource(String input)
	{
		StringBuilder sb = new StringBuilder();

		try
		{
			FileInputStream fis = new FileInputStream(input);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			dis.close();
		} 
		catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		this.graph = sb;
	}

    public static String createImageOutputPathForWindow(Window window) {
        String BSLASH = "\\";
        String FSLASH = "/";
        return window.getWindow().getApplication().getContext().getBaseDirectory().getAbsolutePath().replace(BSLASH, FSLASH) + "/WEB-INF/image";
    }
	
	public String graphVizOutputPath(String fileName) throws URISyntaxException{
		return imageOutputPath + File.separator +fileName;
	}

} // end of class GraphViz

