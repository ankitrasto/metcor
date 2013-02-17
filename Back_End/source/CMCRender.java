/*CMCRender.java
 *Ankit Rastogi
 *Project DensityNorth
 *Started: May 10, 2010
 **/

/*
 * Copyright [2013] [Ankit Rastogi]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */



package dnimp;
import java.io.*;
import java.util.*;
import java.text.*;

/**
 *A member of the DNIMP package which handles relevant operations performed on a set of data.
 *<p> CMCRender objects are optimized to read endpoints from CMC-formated back trajectory files, with each endpoint identified
 *by the back-trajectory date. CMCRender reads correlated data and proceeds to perform additional steps in the World in order to write PSCF matrices, histograms
 *and elevation plots to disk.
 */

public class CMCRender {

	/**
	 *Formats numerical text to the format "00". For example, 3 is formatted to "03", while 12 is formatted to "12".
	 */
	private DecimalFormat df = new DecimalFormat("00");

	/**
	 *The northern hemisphere.
	 */
	public World nh; //CHANGE TO PRIVATE

	/**
	 *Holds a list of back trajectory files in a specified directory.
	 */
	private File[] inputFiles;

	/**
	 *The abstract pathname describing a directory containing CMC-formatted back trajectories (or HYSPLIT trajectories)
	 */
	private File inputDirEP; //endpoints

	/**
	 *The abstract pathname denoting the directory of all file written by this instance of CMCRender
	 */
	private File outputDir; //all outputs!

	/**
	 *The representation of the file containing the correlated data to be tagged.
	 */
	private File concFile;

	/**
	 *A list of variable names corresponding to each component of the correlated data.
	 */
	private String[] varList; //obtained from inputFiles

	/**
	 *Not Found Exception - thrown if concentration data input file does not exist.
	 */
	private Exception notFound = new Exception("The concentration data input File does not exist");

	/**
	 *Holds the threshold data which is calculated in CMCRender or contained in the correlated data input file.
	 */
	private Pair[] threshData;

	/**
	 *Describes whether the correlated data input file contains pre-defined thresholds.
	 */
	private boolean containsThresh = false;

	/**
	 *Contains a dynamic array of correlated data used to calculate thresholds in CMCRender, if applicable.
	 */
	private ArrayList<ArrayList<Double>> concSet;


	/**
	 *Creates an instance of CMCRender with a World object of a given size and grid-dimensions.
	 *@param lon total longitude
	 *@param lat total latitude
	 *@param dX grid width in terms of latitude
	 *@param dY grid width in terms of longitude
	 *@param inPathEndPoints directory containing all CMC-formatted backtrajectories. NOTE: the directory must contain these files ONLY.
	 *@param concFilePath full path of the correlated data input file. The pathname must be absolute yet abstract.
	 *@param outDir program output directory. If the directory does not exist, it is created.
	 *@throws Exception IOExceptions and null pointer exceptions are thrown to higher levels.
	 *
	 *REQUIRES: Preconditions of lon,lat,dX and dY are given the <code>World</code> constructor. Directories and pathnames cannot be null and should exist.
	 */
    public CMCRender(int lon, int lat, double dX, double dY, String inPathEndPoints, String concFilePath, String outDir) throws Exception{
    	//requires: requires of World constructor + all other vars cannot be null
    	//the directories and input files MUST EXIST.
    	nh = new World(lon, lat, dX, dY);
    	inputDirEP = new File(inPathEndPoints);
    	concFile = new File(concFilePath);
    	outputDir = new File(outDir);

    	if(!outputDir.isDirectory()){
    		try{
    			outputDir.mkdir();
    		}catch(Exception e){
    			e.printStackTrace();
    			System.out.println("FAILED  TO CREATE DIRECTORY"); //VERBOSE TESTING
    		}
    	}

    	//windows path name syntax: C:/a/file.txt ...etc.
    	//unix path name syntax: /a/file.txt ...etc.
    	if(inputDirEP.isDirectory()){
    		inputFiles = inputDirEP.listFiles();
    	}else{
    		throw new Exception("The Input File Directory Could not be Read"); //this might never be accessible because of front-end error handling
    	}

    	if(!concFile.exists()){
    		throw notFound;
    	}

    }

    /**
     *Uses java.util.GregorianCalendar to correct a date by a time-zone shift and return it as a string in the format: "yyyymmddtt".
     *@param auxDate the input date which must be in the format: "yyyymmddtt"
     *@param an option to account for daylight savings time in addition to time zone correction (NOT IMPLEMENTED YET; by default, DST correction is automatic)
     *@param zoneOffset the hours to subtract from auxDate.
     *<p> If this function is used to convert any date directly into the UTC time zone, <code>zoneOffset</code> corresponds to the specific time-zone number
     *appropriate to the region.
     */
    private String timeCorrect(String auxDate, boolean correctDST, int zoneOffset){ //auxDate does not have a zero indexed month!
		//zoneOffset in hours (-8 for PST, example!), correctDST = true if DST applies
    	//date format: yyyymmddtt, yyyymddtt, yyyymmd
    	GregorianCalendar backDate = new GregorianCalendar(Integer.parseInt(auxDate.substring(0,4)), Integer.parseInt(auxDate.substring(4,6))-1, Integer.parseInt(auxDate.substring(6,8)), Integer.parseInt(auxDate.substring(8,10)), 0);
    	backDate.add(Calendar.HOUR_OF_DAY, (-1*zoneOffset));
    	String corrected = backDate.get(Calendar.YEAR) + df.format(backDate.get(Calendar.MONTH) + 1) + df.format(backDate.get(Calendar.DAY_OF_MONTH)) + df.format(backDate.get(Calendar.HOUR_OF_DAY));
    	return corrected;
    }


    private long mSecTime(String auxDate){
    	GregorianCalendar date = new GregorianCalendar(Integer.parseInt(auxDate.substring(0,4)), Integer.parseInt(auxDate.substring(4,6))-1, Integer.parseInt(auxDate.substring(6,8)), Integer.parseInt(auxDate.substring(8,10)), 0);
    	return date.getTimeInMillis();
    }

    /**
     *Reads the endpoints from the CMC-formatted backtrajectories and adds them to the World object.
     *@param numCol the number of columns in the CMC-formatted back trajectories associated with admissable data. For example, if numCol = 9, all admissable endpoints
     *in the back trajectories will contain 9 columns.
     *@param printElev if true, writes elevation data to disk while reading and loading endpoints.
     *@param progress A graphical progress bar object may be passed; pass a null object if not needed
     */
    public void readEP(int numCol, boolean printElev, javax.swing.JProgressBar progress) throws Exception{
    	System.out.print("--READING CMC...");
    	BufferedReader bR;
    	String dataHold;
    	String lineHold[];
    	String auxID;
    	String auxThirdDim;
    	double latY;
    	double lonX;
    	long counts = 0;

    	int errors = 0;
    	ArrayList<String> elevs;

    	for(int i = 0; i < inputFiles.length; i++){
    		elevs = new ArrayList<String>();
    		bR = new BufferedReader(new FileReader(inputFiles[i].getAbsoluteFile()));
    		for(int j = 0; j < 8; j++) bR.readLine();
    		lineHold = delimitLine(bR.readLine().trim()).split("\t"); //sourceID, line 9
   			auxID = lineHold[0] + df.format(Integer.parseInt(lineHold[1])) + df.format(Integer.parseInt(lineHold[2])) + df.format(Integer.parseInt(lineHold[3]));
    		auxThirdDim = "";
    		dataHold = bR.readLine(); //first header

    		while(dataHold != null){
    			lineHold = delimitLine(dataHold.trim()).split("\t"); //still the first to nth elevation header
    			auxThirdDim = lineHold[3];
    			if(lineHold.length < numCol){
    				if(printElev && elevs != null){
	       						elevs.add(lineHold[0] + "\t" + lineHold[3]);
	       			}
    				dataHold = bR.readLine();
       				lineHold = delimitLine(dataHold.trim()).split("\t");
       				do{
       					try{
       						latY = Double.parseDouble(lineHold[1]);
	       					lonX = Double.parseDouble(lineHold[2]);
	       					if(printElev && elevs != null){
	       						elevs.add(lineHold[0] + "\t" + lineHold[5]);
	       					}
	       					if(progress != null){
	       						progress.setString("Added " + (++counts) + " endpoints.");
	       					}
	       					nh.addPointToWorld(new Point(lonX, latY, auxID, auxThirdDim));
	       					//System.out.println("Added: (" + lonX + " , " + latY + ") to World with ID: " + auxID + " and ThirdDim = " + auxThirdDim); //VERBOSE TESTING
       					}catch(Exception e){
       						errors++;
       						//System.out.println("Inadmissable Point, not added"); //VERBOSE TESTING
       					}
       					dataHold = bR.readLine();
       					if(dataHold != null){
       						lineHold = delimitLine(dataHold.trim()).split("\t");
       					}
    				}while(dataHold != null && lineHold.length >= numCol);
    			}
    		}
    		bR.close();

    		if(progress != null){
    			double percentFin = ((double)i)/((double)inputFiles.length);
    			progress.setValue((int)(percentFin*100));
    		}
    		//if(i%100 == 0 && i > 100) System.out.println("Population of Points in World: " + nh.hemPopulation()); //VERBOSE TESTING

    		if(printElev) this.writeToFile(new File(this.outputDir + "/ELEV/" + inputFiles[i].getName() + ".txt"), elevs, false);

    	}
    	System.out.print("..done. Population of Points in World: " + nh.hemPopulation() +"\n"); //VERBOSE TESTING
    	//nh.printData(); //VERBOSE TESTING
    }
   	
   	private double timeDistance(String EPDate, String sourceDate){
   		
   		//GregorianCalendar epDate  = new GregorianCalendar(Integer.parseInt(EPDate.substring(0,4)), Integer.parseInt(EPDate.substring(4,6))-1, Integer.parseInt(EPDate.substring(6,8)), Integer.parseInt(EPDate.substring(8,10)), 0);
   		//GregorianCalendar recDate = new GregorianCalendar(Integer.parseInt(sourceDate.substring(0,4)), Integer.parseInt(sourceDate.substring(4,6))-1, Integer.parseInt(sourceDate.substring(6,8)), Integer.parseInt(sourceDate.substring(8,10)), 0);
   		
   		
   		return 0;
   	}


    public void readHSEP(int centuryStart, javax.swing.JProgressBar progress) throws Exception{
    	//centuryStart: 1900, 2000 are common examples.
    	System.out.print("--READING HYSPLIT...");
    	BufferedReader bR;
    	int counts = 0;
    	String dataHold;
    	String nextLine[];
    	String lineHold[];
    	ArrayList<String> auxID;
    	ArrayList<String> auxThirdDims;
    	double latY;
    	double lonX;
    	int blockCount;

    	for(int i = 0; i < inputFiles.length; i++){
    		blockCount = 1;
    		bR = new BufferedReader(new FileReader(inputFiles[i].getAbsoluteFile()));
    		auxID = new ArrayList<String>();
    		auxThirdDims = new ArrayList<String>();
    		dataHold = bR.readLine();
    		while(dataHold != null && !dataHold.equalsIgnoreCase("")){
    			//System.out.println("Block " + blockCount + " start text: " + dataHold); //VERBOSE
    			lineHold = delimitLine(dataHold.trim()).split("\t");

    			if(blockCount == 4){ //get sourceIDs
    				String auxSourceID = null;
    				String auxThirdDim = null;
    				try{
    					auxSourceID = "" + (centuryStart + Integer.parseInt(lineHold[0])) + df.format(Integer.parseInt(lineHold[1])) + df.format(Integer.parseInt(lineHold[2])) + df.format(Integer.parseInt(lineHold[3]));
    					auxThirdDim = lineHold[6] + "," + lineHold[4] + "," + lineHold[5]; //obtained receptor site as thirdDim
    				}catch(Exception e){
    					e.printStackTrace(); //VERBOSE
    				}
    				auxID.add(auxSourceID);
    				auxThirdDims.add(auxThirdDim);
    			}

    			if(blockCount == 6){ //addpoints!
    				try{
    					latY = Double.parseDouble(lineHold[9]);
    					lonX = Double.parseDouble(lineHold[10]);
    					
    					String EPDate = "" + (centuryStart + Integer.parseInt(lineHold[2])) + df.format(Integer.parseInt(lineHold[3])) + df.format(Integer.parseInt(lineHold[4])) + df.format(Integer.parseInt(lineHold[5]));
    					
    					int T = (int)(Math.abs(this.mSecTime(EPDate) - this.mSecTime(auxID.get(Integer.parseInt(lineHold[0])-1)))/3600000);
    					
    					if(lonX < 0) lonX += 360;
						if(nh.addPointToWorld(new Point(lonX, latY, auxID.get(Integer.parseInt(lineHold[0]) - 1), auxThirdDims.get(Integer.parseInt(lineHold[0]) - 1) + "," + T))){
	    						if(progress != null){
	    							progress.setString("Added " + (++counts) + " endpoints.");
	    						}
	    					//	System.out.println("Added: (" + lonX + " , " + latY + ") to World with ID: " + auxID.get(Integer.parseInt(lineHold[0]) - 1) + " and ThirdDim = " + auxThirdDims.get(Integer.parseInt(lineHold[0]) - 1)); //VERBOSE TESTING
	    				}
    				}catch(Exception e){
    					e.printStackTrace(); //VERBOSE
    				}
    			}


    			dataHold = bR.readLine(); //read next line, pre-emptively.
    			if(dataHold != null && !dataHold.equalsIgnoreCase("")){
    				nextLine = delimitLine(dataHold.trim()).split("\t");
    				//System.out.println("Block " + blockCount + " next line text: " + dataHold); //VERBOSE

    				while(lineHold.length == nextLine.length && dataHold != null && !dataHold.equalsIgnoreCase("")){
    					lineHold = nextLine;

    					if(blockCount == 4){ //get sourceIDs
		    				String auxSourceID = null;
		    				String auxThirdDim = null;
		    				try{
		    					auxSourceID = "" + (centuryStart + Integer.parseInt(lineHold[0])) + df.format(Integer.parseInt(lineHold[1])) + df.format(Integer.parseInt(lineHold[2])) + df.format(Integer.parseInt(lineHold[3]));
								auxThirdDim = lineHold[6] + "," +lineHold[4] + "," + lineHold[5]; //obtained receptor site as thirdDim
							}catch(Exception e){
		    					e.printStackTrace();
		    				}
		    				auxID.add(auxSourceID);
		    				auxThirdDims.add(auxThirdDim);
	    				}

	    				if(blockCount == 6){ //addpoints!
		    				try{
		    					latY = Double.parseDouble(lineHold[9]);
		    					lonX = Double.parseDouble(lineHold[10]);
		    					
		    					String EPDate = "" + (centuryStart + Integer.parseInt(lineHold[2])) + df.format(Integer.parseInt(lineHold[3])) + df.format(Integer.parseInt(lineHold[4])) + df.format(Integer.parseInt(lineHold[5]));
    					
    							int T = (int)(Math.abs(this.mSecTime(EPDate) - this.mSecTime(auxID.get(Integer.parseInt(lineHold[0])-1)))/3600000);
	
		    					if(lonX < 0) lonX += 360;
		    					//Point x = new Point(lonX, latY, auxID.get(Integer.parseInt(lineHold[0]) - 1)); //TEST
    							//x.printInfo(); //TEST
		    					if(nh.addPointToWorld(new Point(lonX, latY, auxID.get(Integer.parseInt(lineHold[0]) - 1), auxThirdDims.get(Integer.parseInt(lineHold[0]) - 1) + "," + T))){
		    						if(progress != null){
		    							progress.setString("Added " + (++counts) + " endpoints.");
		    						}
		    						//System.out.println("Added: (" + lonX + " , " + latY + ") to World with ID: " + auxID.get(Integer.parseInt(lineHold[0]) - 1) + " and ThirdDim = " + auxThirdDims.get(Integer.parseInt(lineHold[0]) - 1)); //VERBOSE TESTING
		    					}

		    				}catch(Exception e){
		    					e.printStackTrace(); //VERBOSE
		    				}
    					}


    					dataHold = bR.readLine();
    					if(dataHold != null && !dataHold.equalsIgnoreCase("")){
    						nextLine = delimitLine(dataHold.trim()).split("\t");
    					}

    					//System.out.println("Block " + blockCount + " repeating text: " + dataHold); //VERBOSE
    				}
    				blockCount++;
    			}
    		}
    		bR.close();

    		if(progress != null){
    			double percentFin = ((double)i)/((double)inputFiles.length);
    			progress.setValue((int)(percentFin*100));
    		}
    		//System.out.println("No. of Blocks in Text File: " + blockCount); //VERBOSE
    	}

    	System.out.print("...done. Population of Points in World: " + nh.hemPopulation() + "\n"); //VERBOSE TESTING
    	System.out.print("MAX NIJ in World: " + nh.getMaxNIJ()+"\n");
    }


    /**
     *Reads correlated data and associates them with the appropriate points (tagging) on the basis of their identifiers.
     *<p>Since the identifiers are dates, the input files contain a date range by which the tagging is conducted. The preconditions of the input file are checked
     *by a call to <code>checkInputFile()</code>
     *@param incr the temporal interval between back trajectories, in hours. REQUIRES: 24%incr = 0. (the increment evenly divides 24 hours of the day
     *so that there are a regular number of back trajectories per day.)
     *@param zone the time zone correction to make to the correlated data input file relative to the time zone of the back trajectories.
     *@param progress A graphical progress bar object may be passed; pass a null object if not needed
     */
    public void readConc(double incr, int zone, javax.swing.JProgressBar progress) throws Exception{
    	int numLines = checkInputFile();
    	if(numLines == 0){
    		throw new Exception("Error in Input File. Ensure that it is formatted correctly.");
    	}

    	System.out.println("Input File OK"); //VERBOSE TESTING
    	String lineHold[];
    	String dataHold;
    	BufferedReader bR = new BufferedReader(new FileReader(concFile.getAbsoluteFile()));
    	//variables for tagging
    	String startDate;
    	String endDate;
    	Pair[] concData;

    	//get appropriate variable names for headers!
    	lineHold = delimitLine(bR.readLine().trim()).split("\t");
    	this.varList = new String[lineHold.length - 6];
    	concData = new Pair[this.varList.length];
    	threshData = new Pair[this.varList.length];

    	if(!this.containsThresh){
    		concSet = new ArrayList<ArrayList<Double>>(this.varList.length);
    		for(int m = 0; m < this.varList.length; m++) concSet.add(new ArrayList<Double>());
    	}
    	for(int i = 6; i < lineHold.length; i++) varList[i-6] = lineHold[i];
    	dataHold = bR.readLine();
    	int j = 0;
    	while(dataHold != null){
    		if(progress != null){
    			progress.setValue((int)((++j)*100.0/numLines));
    		}
    		lineHold = delimitLine(dataHold.trim()).split("\t");
    		if(lineHold[0].equalsIgnoreCase("THRESH")){
    			for(int i = 0; i < threshData.length; i++){
    				this.threshData[i] = new Pair(varList[i], Double.parseDouble(lineHold[1+i]));
    			}
    		}else{
    			startDate = lineHold[0] + (lineHold[1].substring(0,2));

    			if(Integer.parseInt(lineHold[1].substring(2,4)) >= 30){
    				startDate = timeCorrect(lineHold[0] + lineHold[1].substring(0,2), false, -1);
    			}

    			endDate = lineHold[2] + (lineHold[3].substring(0,2));

    			if(Integer.parseInt(lineHold[3].substring(2,4)) >= 30){
    				endDate = timeCorrect(lineHold[2] + lineHold[3].substring(0,2), false, -1);
    			}
    			//System.out.println("Hour Rounded: " + startDate + " , " + endDate); //VERBOSE TESTING
    			for(int i = 0; i < concData.length; i++){
    				concData[i] = new Pair(varList[i], Double.parseDouble(lineHold[6+i]));
    				if(!this.containsThresh)((ArrayList<Double>)concSet.get(i)).add(Double.parseDouble(lineHold[6+i]));
    			//System.out.println(varList[i] + " = " + lineHold[6+i]); //VERBOSE TESTING
    			}
    			
    			//time-correct and tag the appropriate files...call another method in this class for it
    			tagPointsHS(timeCorrect(startDate, false, zone), timeCorrect(endDate, false, zone), incr, concData, progress, lineHold[4], lineHold[5]);
    		}
    		dataHold = bR.readLine();
    	}
    	bR.close();
    }

    /**
     *Verifies that the correlated data input file is formatted correctly.
     *Returns 0 if the input data file is formatted correctly; a value greater than 0 otherwise.
     */
    public int checkInputFile() throws Exception{
    	boolean ok = false;
    	BufferedReader bR = new BufferedReader(new FileReader(concFile.getAbsoluteFile()));
    	String lineHold[];
    	String dataHold = bR.readLine();
		int headerLength;
		int numLines = 0;

		if(dataHold == null) return numLines; //empty file.
    	numLines++;
    	lineHold = delimitLine(dataHold.trim()).split("\t");
    	//check to make sure that the first line contains the appropriate headings, and enumerate the headings for convenience.
    	if(lineHold[0].equalsIgnoreCase("IDATE") && lineHold[1].equalsIgnoreCase("ITIME") && lineHold[2].equalsIgnoreCase("FDATE") && lineHold[3].equalsIgnoreCase("FTIME") && lineHold[4].equalsIgnoreCase("LATR") && lineHold[5].equalsIgnoreCase("LONR")){
    		headerLength = lineHold.length;
    		if(headerLength <= 6){bR.close(); System.out.println("Header Error"); return 0;}
    		while(dataHold != null && !dataHold.equalsIgnoreCase("\n")){
    			numLines++;
    			lineHold = delimitLine(dataHold.trim()).split("\t"); //redundant but ok.
    			if(lineHold[0].equalsIgnoreCase("THRESH") && (lineHold.length != (headerLength-5))){bR.close(); return 0;}
    			if(!lineHold[0].equalsIgnoreCase("THRESH") && (lineHold.length != headerLength)){bR.close(); System.out.println("Missing Data: Line = " + numLines + ", Length: " + lineHold.length + ", Header = " + headerLength); return 0;}
    			if(lineHold[0].equalsIgnoreCase("THRESH") && (lineHold.length == (headerLength-5))){this.containsThresh = true;}
    			dataHold = bR.readLine();
    		}
    	}else{
    		bR.close();
    		return 0;
    	}
    	bR.close();
    	System.out.println("NUMBER OF LINES: " + numLines); //VERBOSE TESTING
    	return numLines;

    }

    /**
     *Tags the correlated data to the appropriate endpoints on the basis of date ranges as identifiers.
     *@param sDate the time-zone corrected start date in the format "yyyymmddtt"
     *@param fDate the time-zone corrected end date in the format "yyyymmddtt"
     *@param increment the interval between the back trajectory endpoints.
     *@param corData the pair of correlated data to tag.
     */
    private void tagPoints(String sDate, String fDate, double increment, Pair[] corData, javax.swing.JProgressBar progress){ //increment is in hours!
    	//round the time corrected sDate to the appropriate time with respect to the increment:
		String sTimeBT = df.format((int)(Double.parseDouble(sDate.substring(8,10))/increment)*increment);
		String fTimeBT = df.format((int)(Double.parseDouble(fDate.substring(8,10))/increment)*increment);
		String sDateBTL = sDate.substring(0,8) + sTimeBT;
		String fDateBTL = fDate.substring(0,8) + fTimeBT;

		if(Integer.parseInt(sDate.substring(8,10)) >= Integer.parseInt(timeCorrect(sDateBTL, false, (int)(-0.5*increment)).substring(8,10))){
			sDate = timeCorrect(sDateBTL, false, (int)(-1*increment));
			String sTimeRound = df.format((int)(Double.parseDouble(sDate.substring(8,10))/increment)*increment);
			sDate = sDate.substring(0,8) + sTimeRound;
		}else{
			sDate = sDateBTL;
		}

		if(Integer.parseInt(fDate.substring(8,10)) >= Integer.parseInt(timeCorrect(fDateBTL, false, (int)(-0.5*increment)).substring(8,10))){
			fDate = timeCorrect(fDateBTL, false, (int)(-1*increment));
			String fTimeRound = df.format((int)(Double.parseDouble(fDate.substring(8,10))/increment)*increment);
			fDate = fDate.substring(0,8) + fTimeRound;
		}else{
			fDate = fDateBTL;
		}

    	if(progress != null){
    		progress.setString("TAGGING: " + sDate + " TO " + fDate);
    	}
    	System.out.println("TAGGING: " + sDate + " TO " + fDate + " i.e. = " + this.mSecTime(sDate) + " TO " + this.mSecTime(fDate));


    	nh.tagWorld(sDate, corData, "", "");


    	//while(!sDate.equalsIgnoreCase(fDate)){
    	while(this.mSecTime(sDate) < this.mSecTime(fDate)){
    		sDate = increment(sDate);
    		/*sDate = timeCorrect(sDate, false, (int)(-1*increment));
			String sDateBTLow = sDate.substring(0,8) + df.format((int)(Double.parseDouble(sDate.substring(8,10))/increment)*increment);
			if(Integer.parseInt(sDate.substring(8,10)) >= Integer.parseInt(timeCorrect(sDateBTLow, false, (int)(-0.5*increment)).substring(8,10))){
				sDate = timeCorrect(sDateBTLow, false, (int)(-1*increment));
			}else{
				sDate = sDateBTLow;
			}*/
    		nh.tagWorld(sDate, corData, "", "");
    		//System.out.println("NEXT DATE: " + sDate);
    	}
    }
    
    private void tagPointsHS(String sDate, String fDate, double increment, Pair[] corData, javax.swing.JProgressBar progress, String latR, String lonR){
    	//simpler tagging function --> actually independent of increment
    	/*GregorianCalendar counterDate = new GregorianCalendar(Integer.parseInt(sDate.substring(0,4)), Integer.parseInt(sDate.substring(4,6))-1, Integer.parseInt(sDate.substring(6,8)), Integer.parseInt(sDate.substring(8,10)), 0);
    	String dateTag =  counterDate.get(Calendar.YEAR) + df.format(counterDate.get(Calendar.MONTH) + 1) + df.format(counterDate.get(Calendar.DAY_OF_MONTH)) + df.format(counterDate.get(Calendar.HOUR_OF_DAY));
    	
    	nh.tagWorld(dateTag, corData);
    	
    	counterDate.add()*/
    	
    	String dateCounter = sDate;
    	//System.out.println("Tagging: " + dateCounter + "with: " + corData[0].value); //VERBOSE
    	nh.tagWorld(dateCounter, corData, latR, lonR);
    	dateCounter = increment(dateCounter);
    	
    	if(progress != null){
    		progress.setString("TAGGING: " + sDate + " TO " + fDate);
    	}
    		
    	while(mSecTime(dateCounter) < mSecTime(fDate)){
    	//	System.out.println("Tagging: " + dateCounter + "with: " + corData[0].value); //VERBOSE
    		nh.tagWorld(dateCounter, corData, latR, lonR);		
    		dateCounter = increment(dateCounter);
    	}

    }
     


  	private String increment(String date){
  		GregorianCalendar sDate = new GregorianCalendar(Integer.parseInt(date.substring(0,4)), Integer.parseInt(date.substring(4,6))-1, Integer.parseInt(date.substring(6,8)), Integer.parseInt(date.substring(8,10)), 0);
		sDate.add(Calendar.HOUR_OF_DAY, 1);
		return sDate.get(Calendar.YEAR) + df.format(sDate.get(Calendar.MONTH) + 1) + df.format(sDate.get(Calendar.DAY_OF_MONTH)) + df.format(sDate.get(Calendar.HOUR_OF_DAY));
  	}

    /**
     *A line delimiter which eliminates white space from <code>input</code>
     *@param input the input string
     */
    private String delimitLine(String input){
    	//requires: input is not null; leading/trailing whitespaces are trimmed!
    	int i = 0;
    	String spaceSep = "";
    	while(i < input.length()){
    		if((int)input.charAt(i) != 9 && (int)input.charAt(i) != 32){ //mutually exclusive!
    			if(i > 0 && ((int)input.charAt(i-1) == 9 || (int)input.charAt(i-1) == 32)){
    				spaceSep += ("\t" + input.charAt(i));
    			}else{
    				spaceSep += input.charAt(i);
    			}
    		}
    		i++;
    	}
    	return spaceSep;
    }

    /**
     *calculates the PSCF once all endpoints are loaded and data is tagged.
     *@param threshMethod the method by which to calculate thresholds. This is used if the thresholds are not in the correlated data input file.
     *<p> threshMethod = 1: use the average of each pollutant as the threshold
     *<p> threshMethod = 2: use the average + 1 unit of standard deviation as the threshold
     *<p> threshMethod = 3: use a percentile-based method to determine the threshold concentrations
     *@param percentile: used only if threshMethod = 3: the percentile of the concentration data to be used as the threshold. REQUIRES: <code>0 < percentile <= 1.0.
     *@param weights the weights to be used for the PSCF function. For preconditions for <code>weights</code>, see the World.changeWeight method.
     *<p> if threshMethod is not 1, 2 or 3 and the input file does not contain threshold data, the option defaults to taking the average as a threshold
     *@param useSourceIDs true if the weights provided pertain to the number of unique sourceIDs in a grid. Otherwise, the weights are assumed to be NIJ limits.
     */
    public void calcPSCF(int threshMethod, double percentile, Triplet[] weights, boolean useSourceIDs) throws Exception{
    	//threshold method: if the inputConc file contains threshold data, then this value becomes irrelevant
    	//if a non-sensical number is entered and the input file contains no concentration data, the option defaults to 1
    	//METHOD 1: average of each pollutant
    	//METHOD 2: average + 1 SD of each pollutant
    	//METHOD 3: percentile cutoff.
    	//METHOD 4: already in file (default, any value!)
    	//percentile: enter zero if it method 3 is not used.

    	nh.changeWeights(weights);

    	double sum = 0;
    	if(containsThresh){
    		if(useSourceIDs){
  				nh.calcPSCFBySourceID(this.threshData);
  			}else{
  				nh.calcPSCF(this.threshData);
  			}
    	}else{
    		if(threshMethod <= 1 || threshMethod > 3){
    			for(int i = 0; i < threshData.length; i++){
    				sum = 0;
    				double auxSize = ((ArrayList<Double>)(concSet.get(i))).size();
    				for(int j = 0; j < ((ArrayList<Double>)(concSet.get(i))).size(); j++){
    					sum += ((ArrayList<Double>)(concSet.get(i))).get(j);
    				}
    				threshData[i] = new Pair(this.varList[i], sum/auxSize);
    				System.out.println(threshData[i].name + "\t" + threshData[i].value);
    			}
    		}else if (threshMethod == 2){ //mean + 1 SD
    			for(int i = 0; i < threshData.length; i++){
    				double preSum = 0;
    				sum = 0;
    				double auxSize = ((ArrayList<Double>)(concSet.get(i))).size();

    				//get average as sum
    				for(int j = 0; j < ((ArrayList<Double>)(concSet.get(i))).size(); j++){
    					sum += ((ArrayList<Double>)(concSet.get(i))).get(j);
    				}

    				//get SD as preSum
    				for(int j = 0; j < ((ArrayList<Double>)(concSet.get(i))).size(); j++){
    					preSum += Math.pow((((ArrayList<Double>)(concSet.get(i))).get(j)-(sum/auxSize)), 2);
    				}

    				threshData[i] = new Pair(this.varList[i], (sum/auxSize) + Math.sqrt(preSum/(auxSize-1)));
    				System.out.println(threshData[i].name + "\t" + threshData[i].value);
    			}
    		}else{ //i.e. threshMethod == 3
  				for(int i = 0; i < threshData.length; i++){
    				Object sortedList[] = ((concSet.get(i))).toArray();
    				Arrays.sort(sortedList);
    				if(percentile <= 1 && percentile > 0) threshData[i] = new Pair(this.varList[i], (Double)sortedList[(int)((percentile*sortedList.length)-1)]);
    				if(percentile <= 0 || percentile > 1) threshData[i] = new Pair(this.varList[i], (Double)sortedList[0]);
    				System.out.println(threshData[i].name + "\t" + threshData[i].value);
    			}
  			}

  			if(useSourceIDs){
  				nh.calcPSCFBySourceID(this.threshData);
  			}else{
  				nh.calcPSCF(this.threshData);
  			}
    	}
    }
    
    /**
     *Calculates a single CWT-field of this world with various options. Note: DOES NOT WRITE THE CWT-matrix to disk.
     *@param calcMethod an index for selecting the type of CWT-calculation to perform
     *	<p> calcMethod = 1: use a basic CWT model without any enhancements
     *	<p> calcMethod = 2: use a logarithmic CWT model without any enhancements
     *	<p> calcMethod = 3: use a basic CWT model with signal smoothing
     *	<p> calcMethod = 4: use a logarithmic CWT model with signal smoothing
     *@param pointFilter an odd-numbered length representing 2N+1 (the filter length), where N is the left and right bound
     *used for data smoothing. Generally, the larger the length of the filter, the greater the smoothing effect
     *@param ndValue the value used for representing "no Data" on the CWT grid. NOTE: ndValue should be the same as that 
     *which is specified for printing a null CWT matrices. Typically, -1.00 is used.
     *@param polyDegree a user-defined parameter which specifies the degree of the polynomial to use smoothing with
     *@param confInt the confidence interval (1-probability) of the desired analysis in decimal format (0.95, 0.999, etc.)
     */
    public void calcCWT(int calcMethod, double ndValue, int pointFilter, int polyDegree, double confInt, AdvancedTriplet[] weights) throws Exception{
    	/*CALCULATION METHOD NOTES:
    	calcMethod = 1: use a basic CWT model, without any enhancements
    	calcMethod = 2: use a logarithmic CWT averaging without any enhancements
    	calcMethod = 3: use a basic CWT model with signal smoothing
    	calcMethod = 4: use a logarithmic CWT model with signal smoothing
    	*/
    	if(varList == null) return; //aka the CD-file has not been read yet
    	if(calcMethod == 1){
    		nh.calcCWT(this.varList);
    	}
    	
    	if(calcMethod == 2){
    		nh.calcCWTLog(this.varList);
    	}
    	
    	if(calcMethod == 3){
    		nh.calcCWT(this.varList);
    		nh.smoothCWTField(this.varList, ndValue, pointFilter, polyDegree, confInt, false);
    	}
    	
    	if(calcMethod == 4){
    		nh.calcCWTLog(this.varList);
    		nh.smoothCWTField(this.varList, ndValue, pointFilter, polyDegree, confInt, true);
    	}
    	
    	//nh.printCWTMatrix(0); //VERBOSE TESTING
    	
    	if(weights != null){
    		nh.applyAdvancedWeight("CWT", weights);
    	}
    	
    	//nh.printCWTMatrix(0); //VERBOSE TESTING
    	
    }
    
    	/**
 	 *"Master" RTWC method which uses redistributed concentration fields based on previously
 	 *calculated CWT-fields. NOTE: at least one previously calculated CWT IS A REQUIREMENT
 	 *@param varList a list of correlated variables
 	 *@param optionSelect option selection for calculation methods:
 	 * 	<p>optionSelect = 1: linear CWT; no smoothing, fixed iterations (maxIterations)
 	 *	<p>optionSelect = 2: linear CWT; no smoothing, use convergence criteria % (up to a maxIteration if specified) 
 	 *	<p>optionSelect = 3: add smoothing to option 2.
 	 *	<p>optionSelect = 4; add smoothing to option 1
 	 *@param convPercent list the required convergence, IN PERCENT (x100). DEFAULT if <0: 1%
 	 *@param maxIterations the maximum number of iterations of CWT that should be conducted. 
 	 *If <code>convPercent >= 0</code>, then <code>maxIterations = 1000<code> by default.
 	 *@param pointFilter an odd-numbered length representing 2N+1 (the filter length), where N is the left and right bound
     *used for data smoothing. Generally, the larger the length of the filter, the greater the smoothing effect
     *@param ndValue the value used for representing "no Data" on the CWT grid. NOTE: ndValue should be the same as that 
     *which is specified for printing a null CWT matrices. MUST BE LESS THAN ZERO. Typically, -1.00 is used.
     *@param polyDegree a user-defined parameter which specifies the degree of the polynomial to use smoothing with
     *@param confInt the confidence interval (1-probability) of the desired analysis in decimal format (0.95, 0.999, etc.)
     */	
    public void calcRTWC(int optionSelect, double convPercent, int maxIterations, int pointFilter, int polyDegree, double confInt, int ndValue, AdvancedTriplet[] weights) throws Exception{
    	if(varList == null) return; //aka the CD-file has not been read yet
    	nh.calcRTWC(this.varList, optionSelect, convPercent, maxIterations, pointFilter, polyDegree, confInt, ndValue, this.outputDir);
    	
    	if(weights != null){
    		nh.applyAdvancedWeight("RTWC", weights);
    	}
    	
    	//nh.printRTWCMatrix(0);	
    }
    
    /**
     *Calculates the QTBA field of this Grid. Implementation Note: More options such as smoothing should be implemented.
     *@param a atmospheric dispersion velocity (km/hr)
     *@param weights triplet array for application of weights; can be null
     */
     public void calcQTBA(double a, AdvancedTriplet[] weights){
     	if(varList == null) return; //aka the CD-file has not been read yet
     	nh.calcQTBA(this.varList, a);
     	
     	//nh.printQTBAMatrix(0);
     	
     	if(weights != null){
     		nh.applyAdvancedWeight("QTBA", weights);
     	}
     	
     	//nh.printQTBAMatrix(0);
     } 

    //OUTPUT MODULE HISTOGRAMS, ELEVATION PLOTS
    /**
     *Writes a dynamic array of Strings (implemented as an ArrayList) to disk.
     *@param outName the abstract pathname of the desired output file
     *@param lines the dynamic ArrayList object to be written; each element gets one line
     *@param append whether or not to append to the file if it exists.
     *If the directory or file specified by <code>outName</code> does not exist, it is created.
     */
    private void writeToFile(File outName, ArrayList<String> lines, boolean append) throws IOException{
    	if(lines == null || outName == null || lines.size() == 0) return;
    	if(!new File(outName.getParent()).exists()){
    		new File(outName.getParent()).mkdir();
    	}

    	PrintWriter pW = new PrintWriter(new BufferedWriter(new FileWriter(outName.getAbsoluteFile(), append)));
    	for(int i = 0; i < lines.size(); i++) pW.println((String)(lines.get(i)));
    	pW.close();
    }

    /**
     *Writes an array of Strings to disk.
     *@param outName the abstract pathname of the desired output file
     *@param lines the array to be written; each element gets one line
     *@param append whether or not to append to the file if it exists.
     */
     private void writeToFile(File outName, String[] lines, boolean append) throws IOException{
    	if(lines == null || outName == null || lines.length == 0) return;
    	if(!new File(outName.getParent()).exists()){
    		new File(outName.getParent()).mkdir();
    	}

    	PrintWriter pW = new PrintWriter(new BufferedWriter(new FileWriter(outName.getAbsoluteFile(), append)));
    	for(int i = 0; i < lines.length; i++) pW.println(lines[i]);
    	pW.close();
    }

    //HISTOGRAM: Frequency vs. nij, current PSCF vs. NIJ
    /**
     *Prints the grid population histogram to disk; all endpoints are considered here. See World.getHistData.
     *@param intervals the number of classes to use for this histogram
     */
    public void histNIJ(int intervals) throws IOException{ //full filename and path required
    	writeToFile(new File(outputDir + "/HIST/Freq_nij.txt"), nh.getHistData(intervals), false);
    }

    /**
     *Prints the  grid population histogram to disk; only endpoints with correlated data are considered here. See World.getTaggedHistData
     *@param intervals the number of classes to use for this histogram
     */
    public void histTaggedNIJ(int intervals) throws IOException{ //full filename and path required
    	writeToFile(new File(outputDir + "/HIST/TaggedFreq_nij.txt"), nh.getTaggedHistData(intervals), false);
    }

    /**
     *Prints the  unique source ID histogram to disk; only endpoints with correlated data are considered here. See World.getTaggedHistSource
     *@param intervals the number of classes to use for this histogram
     */
    public void histTaggedSource(int intervals) throws IOException{ //full filename and path required
    	writeToFile(new File(outputDir + "/HIST/TaggedFreq_sourceID.txt"), nh.getTaggedHistSource(intervals), false);
    }

    /**
     *Prints the PSCF vs. grid population scatter data to disk.
     */
    public void histPSCF() throws IOException{ //REQUIRES THRESHDATA != null and PSCF must be already calculated!
 			writeToFile(new File(outputDir + "/HIST/PSCF_vs_NIJ"), nh.getPSCFHist(this.threshData), false);
    }

     /**
     *Prints the PSCF vs. number of unique source IDs data to disk.
     */
    public void histPSCFSource() throws IOException{ //REQUIRES THRESHDATA != null and PSCF must be already calculated!
 			writeToFile(new File(outputDir + "/HIST/PSCF_vs_SourceID"), nh.getPSCFSourceHist(this.threshData), false);
    }

    /**
     *Prints the PSCF matrices to disk. THis requires that at least endpoints are tagged with correlated data and PSCF values are already calculated.
     */
    public void PSCF() throws IOException{
    	for(int i = 0; i < threshData.length; i++){
    		writeToFile(new File(outputDir + "/PSCF_MATRICES/" + threshData[i].name + ".txt"), nh.getPSCFMatrix(i, -1), false);
    	}
    }
    
    /**
     *Prints the CWT matrices to disk. Must be called externally.
     */
    public void CWT() throws IOException{
    	for(int i = 0; i < varList.length; i++){
    		writeToFile(new File(outputDir + "/CWT_MATRICES/" + varList[i] + ".txt"), nh.getCWTMatrix(i, -1), false);
    	}
    }
    
    /**
     *Prints the Finalized CWT matrices (RTWC) to disk. Must be called externally.
     */
    public void RTWC() throws IOException{
    	for(int i = 0; i < varList.length; i++){
    		writeToFile(new File(outputDir + "/RTWC_MATRICES/" + varList[i] + ".txt"), nh.getFinalCWTMatrix(i, -1), false);
    	}
    }
    
    /**
     *Prints the QTBA matrices to disk. Must be called externally
     */
     public void QTBA() throws IOException{
    	for(int i = 0; i < varList.length; i++){
    		writeToFile(new File(outputDir + "/QTBA_MATRICES/" + varList[i] + ".txt"), nh.getQTBAMatrix(i, -1), false);
    	}
     }
     
     /**
      *Prints Grid Metrics specific to the type of calculation to disk.
      *@param optionSelect the type of calculation performed
      *	<p> optionSelect = 1: PSCF, CWT, RTWC. (For every correlated variable, the tagged population is averaged and printed)
      * <p> optionSelect != 1 (typically 2): QTBA (For every correlated variable, the avg. natural transport potential function (T) is averaged and printed)
      */
      public void GridMetrics(int optionSelect) throws IOException{
      	if(optionSelect == 1){
      		System.out.println("Writing GRID METRICS: average tagged population TO DISK ...");
      		writeToFile(new File(outputDir + "/METRICS_averageTaggedPop.txt"), nh.avgTaggedNIJ(this.varList), false);
      	}else{
      		System.out.println("Writing GRID METRICS: average natural transport potential TO DISK ...");
      		writeToFile(new File(outputDir + "/METRICS_averageNatT.txt"), nh.avgGridNatT(this.varList), false);
      	}
      }
     

    //TESTING ONLY!
	public static void main(String[]Args) throws Exception{
		/*BufferedReader bR = new BufferedReader(new FileReader("test.points"));

		String dataHold = bR.readLine().trim();
		while(dataHold != null){
			dataHold = dataHold.trim();
			String delimLine[] = delimitLine(dataHold).split("\t");
			System.out.println("Index\tString----------");
			for(int i = 0; i < delimLine.length; i++){
				System.out.println(i + "\t" + delimLine[i]);
			}
			dataHold = bR.readLine();
			//CMCRender(int lon, int lat, double dX, double dY, String inPathEndPoints, String concFilePath, String outPath
		}*/

		CMCRender tester = new CMCRender(360,90,5,5,"C:\\Users\\Brian McCarry II\\Desktop\\crop\\HS_TEST", "C:\\Users\\Brian McCarry II\\Desktop\\concentration_input_PAH.txt", "C:\\Users\\Brian McCarry II\\Desktop\\HS_TEST_OUT");

		Triplet[] wFac = new Triplet[2];

		wFac[0] = new Triplet(1,3,0.5);
		wFac[1] = new Triplet(4,6,0.75);
		/*wFac[2] = new Triplet(122,410,0.75);
		wFac[3] = new Triplet(411, 656, 0.95);*/



		System.out.print("Reading End-points from Files:");
	//	tester.readEP(9, false, null);
		tester.readHSEP(2000, null);
		System.out.print("....Done");

		tester.readConc(6, 0, null);

		System.out.print("Printing PSCF matrices...");
		tester.calcPSCF(2, 0.75, wFac, true);
		System.out.print("...Done\n");


		System.out.print("Generating Freq. vs. NIJ histogram data....");
		tester.histTaggedNIJ(500);
		System.out.println("Done\n");

		System.out.print("Generating histogram of unique source IDs....");
		tester.histTaggedSource(200);
		System.out.println("Done\n");

		System.out.print("Generating PSCF vs. NIJ scatter data....");
		tester.histPSCF();
		System.out.print("Done\n");

		System.out.print("Generating PSCF vs. source ID scatter data....");
		tester.histPSCFSource();
		System.out.print("Done\n");

		System.out.print("Printing PSCF matrices...");
		tester.PSCF();
		System.out.print("...Done\n");
	}
}

