package translator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONObject;

import resources.PositionParser;
import resources.RootClass;


/**
 * Not safe: do not check if the user list is really an ASCII file
 * @author michel
 *
 */
public class GoodiesIngestor extends RootClass {
	private final String workingDir;
	private final String filePrefix;
	private final String fileName;
	private final String jsonName;
	private final double defaultRadius;
	private int nbPositions = 0;
	private int nbLines = 0;
	private JSONObject report;

	/**
	 * @param workingDir 
	 * @param filePrefix: name of the uploaded file (before conversion in VOtable)
	 * @param fileName: Name of the final votable
	 * @param defaultRadius: Default radius in arcmin: used for positions without radius in user list
	 */
	public GoodiesIngestor(String workingDir, String filePrefix, String fileName, double defaultRadius) {
		this.workingDir = workingDir;
		this.filePrefix = filePrefix;
		this.fileName = fileName.replaceAll("[\\.\\(\\)]", "_");;
		this.jsonName = filePrefix + ".json";
		this.defaultRadius = defaultRadius/60.;
	}

	/**
	 * Constructor used for user lists. The name of the finbal votable is derived from the name of the uploaded file.
	 * @param workingDir 
	 * @param filePrefix: name of the uploaded file (before conversion in VOtable)
	 * @param defaultRadius: Default radius in arcmin: used for positions without radius in user list
	 */
	public GoodiesIngestor(String workingDir, String filePrefix, double defaultRadius) {
		this.workingDir = workingDir;
		this.filePrefix = filePrefix;
		this.fileName = (filePrefix + ".xml").replaceAll("[\\.\\(\\)]", "_");
		this.jsonName = filePrefix + ".json";
		this.defaultRadius = defaultRadius/60.;
	}

	/**
	 * Constructor used for user lists. The name of the final votable is derived from the name of the uploaded file.
	 * @param workingDir 
	 * @param filePrefix: name of the uploaded file (before conversion in VOtable)
	 */
	public GoodiesIngestor(String workingDir, String filePrefix) {
		this.workingDir = workingDir;
		this.filePrefix = filePrefix;
		this.fileName = (filePrefix + ".xml").replaceAll("[\\.\\(\\)]", "_");
		this.jsonName = filePrefix + ".json";
		this.defaultRadius = Double.NaN;
	}

	public void ingestUserList() throws IOException {
		try {
			convertUserList();
			writeJsonReport();
			logger.info("File " + this.filePrefix + " ingested as  " + this.fileName + " with " + this.nbPositions + " valid  positions" );

		} catch (Exception e) {
			logger.error("File " + this.filePrefix + " error" + e.getMessage() );
			FileWriter fw = new FileWriter(this.workingDir + File.separator + this.jsonName);
			fw.write(JsonUtils.getErrorMsg("Position List convertion failure\n" +e.getMessage()));
			fw.close();
		}
	}

	private void convertUserList() throws Exception{
		logger.info("Reading " + this.workingDir + File.separator + this.filePrefix );
		File inpf = new File(this.workingDir + File.separator + this.filePrefix);
		if( !inpf.exists() ){
			throw new IOException("File " + inpf.getAbsolutePath() + " does not exist");
		}
		BufferedReader br = new BufferedReader(new FileReader(inpf)) ;
		String boeuf;
		ArrayList<Double> ra = new ArrayList<Double>();
		ArrayList<Double> dec = new ArrayList<Double>();
		while( (boeuf = br.readLine()) != null ) {
			double[] v = {Double.NaN, Double.NaN, Double.NaN};
			boeuf = boeuf.trim();
			if( !boeuf.startsWith("#")) {
				try {
					PositionParser pp = new PositionParser(boeuf);
					v[0] = pp.getRa();
					v[1] = pp.getDec();
				} catch (Exception e) {
					v[0] = Double.NaN;
					v[1] = Double.NaN;
				}
			}
			nbLines++;
			if(! Double.isNaN(v[0]) && ! Double.isNaN(v[1]) ){
				ra.add(v[0]);
				dec.add(v[1]);
				nbPositions++;
			}
		}
		br.close();
		File outf = new File(this.workingDir + File.separator + this.fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outf)) ;
		bw.write("<?xml version='1.0' encoding='utf-8'?>\n");
		bw.write("<VOTABLE version=\"1.2\" xmlns=\"http://www.ivoa.net/xml/VOTable/v1.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.ivoa.net/xml/VOTable/v1.2 http://vo.ari.uni-heidelberg.de/docs/schemata/VOTable-1.2.xsd\"> \n");
		bw.write("  <RESOURCE type=\"results\"> \n");
		bw.write("		<INFO name=\"server\" value=\"http://saada.unistra.fr/taphandle\"></INFO> \n");
		bw.write("		<INFO name=\"query\" value=\"Upload a list of sources\"></INFO> \n");
		bw.write("		<DESCRIPTION> \n");
		bw.write("		Initial file name: " + this.fileName + " \n");
		bw.write("		Number of valid positions: " + this.nbPositions + " \n");
		bw.write("		</DESCRIPTION> \n");
		bw.write("      <TABLE name=\"UploadedValidPositions\">\n");
		bw.write("          <FIELD ID=\"pos_ra_csa\" datatype=\"double\" name=\"pos_ra_csa\" ucd=\"pos.eq.ra\" unit=\"deg\"/>\n"); 
		bw.write("          <FIELD ID=\"pos_dec_csa\" datatype=\"double\" name=\"pos_dec_csa\" ucd=\"pos.eq.dec\" unit=\"deg\"/>\n"); 
		bw.write("          <FIELD ID=\"pos_error_csa\" datatype=\"double\" name=\"pos_error_csa\" ucd=\"stat.error;pos\" unit=\"deg\"/>\n"); 
		bw.write("          <DATA>\n");
		bw.write("              <TABLEDATA>\n");
		for( int i=0 ; i<ra.size() ; i++ ){
			bw.write("                  <TR>");
			bw.write("<TD>" + ra.get(i) + "</TD>");
			bw.write("<TD>" + dec.get(i) + "</TD>");
			bw.write("<TD>" + this.defaultRadius + "</TD>");
			bw.write("</TR>\n");
		}
		bw.write("              </TABLEDATA>\n");
		bw.write("          </DATA>\n");
		bw.write("      </TABLE>\n");
		bw.write("  </RESOURCE>\n");
		bw.write("</VOTABLE>\n");
		bw.close();		
	}

	@SuppressWarnings("unchecked")
	private void writeJsonReport() throws IOException {
		this.report = new JSONObject();
		report.put("nameReport", this.jsonName);
		report.put("nameOrg", this.filePrefix);
		report.put("nameVot", this.fileName);
		report.put("positions", this.nbPositions);
		report.put("lines", this.nbLines);
		report.put("radius", this.defaultRadius);
		report.put("date", (new Date()).toString());
		FileWriter fw = new FileWriter(this.workingDir + File.separator + this.jsonName);
		fw.write(report.toJSONString());
		fw.close();
	}
	
	/**
	 * 
	 */
	public String getReport() {
		return ((report != null)? report.toJSONString(): "NULL");
	}
}
