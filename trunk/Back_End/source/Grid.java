/*Grid.java
 *Ankit Rastogi
 *Project DensityNorth
 *Started: May 5, 2010
 *Updated: - 
 */
 
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
 import java.text.*;
 import java.util.*;
 
 
 /**
  *A representation of a grid with a specified dimension capable of containing a collection of points
  *<p> A grid describes an object with a collection of points (of type <code>Point</code>), defined by a corner latitude
  *corner longitude as well as characteristic MIJ values. A grid is capable of searching its array of points for specific values on the basis of 
  *a point's unique identifier (<code>sourceID</code>) or named points in the set of data correlated to a point. The correlated set of data of the points in the grid
  *is mutable on the basis of its identifier.
  *@author Ankit Rastogi
  *
  */
  public class Grid{
  	 
  	/**
  	 *the defining longitude of this grid (represents the point at the top left corner of this grid)
  	 */
  	private double lonXC;
  
  	/**
  	 *the defining latitude of this grid (represents the point at the top left corner of this grid)
  	 */
  	private double latYC;
  	
  	/**
  	 *The set of points in the grid. This set is initially empty.
  	 */
  	private Vector<Point> points;
  	
  	/**
  	 *The set of mij values specific to this grid. Initially null and accessible to members.
  	 */
  	public int[] mij;
  	
  	/**
  	 *The set of unique sourceIDs of a grid cell
  	 */
  	private HashSet<String> ids;
  	
  	/**
  	 *An array of CWT values specific to this grid. Initially null, size assigned when MIJ is calculated.
  	 */
  	private double[] CWT;
  	
  	private HashSet<String> receptors;
  	
  	private boolean firstTime;
  	
  	private boolean CWTCalc;
  	
  	public double[] gridNatT;
  	
  	/**
  	 *Create a grid of a defined latitude and longitude. The grid initially contains an empty set of points.
  	 *<p> REQUIRES: <code>auxLonXC, auxLatYC > 0</code>
  	 *@param auxLonXC the corner longitude of this grid
  	 *@param auxLatYC the corner latitude of this grid
  	 */
  	public Grid(double auxLonXC, double auxLatYC){
  		this.lonXC = auxLonXC;
  		this.latYC = auxLatYC;
  		points = new Vector<Point>();
  		firstTime = true;
  		CWTCalc = false;
  	}
  	
  	
  	/**
  	 *Adds a point to this Grid.
  	 *@param x the <code>Point</code> to be added
  	 */
  	public void addPoint(Point x){
  		//the points added are likely to contain an empty set of correlated data. this would be "filled" afterwards on the basis of sourceID
  		points.add(x);
  	}
  	
  	
  	/**
  	 *Returns the number of points in this grid. 0 if empty.
  	 */
  	public int population(){
  		return points.size(); 
  	}
  	
  	/**
  	 *Returns the number of points in the grid which have correlated data.
  	 */
  	public int taggedPop(){
  		int counter = 0;
  		for(int i = 0; i < points.size(); i++){
  			if(((Point)(points.get(i))).hasData()) counter++;
  		}
  		return counter;
  	}
  	
  	/**
  	 *Adds correlated data to points belonging to the Grid on the basis of its identifier. 
  	 *Multiple points can be tagged as the entire set of point data is searched.
  	 *@param targetID the identifier
  	 *@param auxData the set of data to be correlated to the point with identifier <code>targetID</code>
  	 */
  	public void tagPoints(String targetID, Pair[] auxData, String latR, String lonR){
  		//search the vector of points for the desired targetID; then fill the point(s) with data
  		//if no points were found with the appropriate sourceID, the state of the program has not changed
  		int i = 0;
  		while(i < points.size()){
  			if(((Point)(points.get(i))).sourceID() != null && ((Point)(points.get(i))).sourceID().equals(targetID) && 
  				latR.equalsIgnoreCase(thirdDimLat(((Point)(points.get(i))).thirdDim())) && lonR.equalsIgnoreCase(thirdDimLon(((Point)(points.get(i))).thirdDim()))){
  				try{
  					((Point)(points.get(i))).addData(auxData);
  					if(firstTime && auxData.length > 0){
  						//initialize the MIJ values, not by higher level
  						this.mij = new int[auxData.length];
  						this.CWT = new double[auxData.length];
  						this.gridNatT = new double[auxData.length];
  						firstTime = false;
  					}
  				}catch(Exception e){
  					System.out.println(e); //thrown if auxData is null
  				}
  			}
  			i++;
  		}
  	}
  	
  	private String thirdDimLat(String auxThirdDim){
  		return (auxThirdDim.split(","))[1];
  	}
  	
  	private String thirdDimLon(String auxThirdDim){
  		return (auxThirdDim.split(","))[2];
  	}
  	
	/**
	 *Calculates the set of mij values for this grid
	 *@param threshData the set of threshold data in the same order as the indices of the mij array.
	 *@throws Exception occurs if a name sought after in correlated data sets is not found
	 */
  	public void calcMIJ(Pair[] threshData) throws Exception{ //threshold data as a pair.
		if(mij != null && threshData.length == mij.length){
			for(int i = 0; i < mij.length; i++){
				mij[i] =  0; //initially set mij to zero.
				for(int j = 0; j < points.size(); j++){
					if(((Point)points.get(j)).hasData() && threshData[i].value <= ((Point)points.get(j)).getValue(threshData[i].name)){
						mij[i]++;
					}
				}
			}
		}
  	}
  	
  	/**
  	 *The number of unique sourceIDs (excluding receptor sites and times) in this grid. All points are considered.
  	 */
  	public int uniqueID(){
  		if(this.population() == 0) return 0;
  		HashSet<String> auxIds = new HashSet<String>();
  		for(int i = 0; i < points.size(); i++){
  			if(((Point)points.get(i)).sourceID() != null && ((Point)points.get(i)).thirdDim() != null) 
  				auxIds.add(((Point)points.get(i)).sourceID() + this.lastComma(((Point)points.get(i)).thirdDim()));
  		}
  		return auxIds.size();
  	}
  	
  	private int numReceptors(){
  		if(this.taggedUniqueID() == 0) return 0;
  		receptors = new HashSet<String>();
  		for(int i = 0; i < points.size(); i++){
  			if(((Point)points.get(i)).hasData() && ((Point)points.get(i)).sourceID() != null && ((Point)points.get(i)).thirdDim() != null){
  				String receptorOfPoint = thirdDimLat(((Point)points.get(i)).thirdDim()) + "," + thirdDimLon(((Point)points.get(i)).thirdDim());
  				receptors.add(receptorOfPoint);
  			}
  		}
  		return receptors.size();
  	}
  	
  	
  	/**
  	 *The number of unique sourceIDs in this grid. Only points with tagged data are considered.  	 
  	 */
  	public int taggedUniqueID(){
  		if(this.population() == 0) return 0;
  		ids = new HashSet<String>();
  		for(int i = 0; i < points.size(); i++){
  			if(((Point)points.get(i)).hasData() && ((Point)points.get(i)).sourceID() != null && ((Point)points.get(i)).thirdDim() != null){
  				String uniqueID = ((Point)points.get(i)).sourceID() + this.lastComma(((Point)points.get(i)).thirdDim());
  				ids.add(uniqueID);
  			}
  		}
  		
  		//VERBOSE:
  		
  		/*System.out.println("-----CONTENTS:------");
  		String x[] = new String[ids.size()];
  		ids.toArray(x);
  		for(int i = 0; i < x.length; i++){
  			System.out.println(x[i]);
  		}
  		System.out.println("--------------\n\n");*/
  		
  		return ids.size();
  	}
  	
  	/**
  	 *A method to modify a single CWT value in the Grid-cell. Important
  	 *for synchronization with RTWC; important use in World.smoothCWTField
  	 *@param polIndex pollutant index
  	 *@param ndValue new value of the CWT[polIndex]: this method was intended to be used to 
  	 *replace a given CWT value with a no-data value in order for recalculations of RTWC to be
  	 *feasible. 
  	 */
  	public void changeCWT(int polIndex, double ndValue){
  	 	if(this.CWTCalc) CWT[polIndex] = ndValue;
  	}
  	
  	public Pair[] nijmTable(){
  		Pair[] auxTable = new Pair[this.taggedUniqueID()];
  		Iterator itrUniqueID = this.ids.iterator();
  		String idsTemp = "";
  		for(int i = 0; i < ids.size(); i++){
  			idsTemp = (String)itrUniqueID.next();
  			auxTable[i] = new Pair(idsTemp, countNIJM(idsTemp));
  		}
  		return auxTable;
  	}
  	
  	private int countNIJM(String auxUID){
  		int auxNIJM = 0;
  		for(int i = 0; i < points.size(); i++){	
  			if(((Point)points.get(i)).hasData() && ((Point)points.get(i)).sourceID() != null && ((Point)points.get(i)).thirdDim() != null){
  				String uniqueID = ((Point)points.get(i)).sourceID() + this.lastComma(((Point)points.get(i)).thirdDim());
  				if(uniqueID.equalsIgnoreCase(auxUID)) auxNIJM++;
  			}
  		}
  		return auxNIJM;
  	}
  	
  	private double getConcByUniqueID(String auxUID, String polName) throws Exception{
  		for(int i = 0; i < points.size(); i++){
  			if(((Point)points.get(i)).hasData() && (((Point)points.get(i)).sourceID() + this.lastComma(((Point)points.get(i)).thirdDim())).equalsIgnoreCase(auxUID)){
  				return ((Point)points.get(i)).getValue(polName);
  			}
  		}
  		throw new Exception("The name specified was not found in this set of correlated data.");
  	}
  	
  	//NOTE: ONLY CALL THIS METHOD IF taggedPop > 0. Will return NULL OTHERWISE!
  	/**
  	 *For all tagged data in this cell, returns an array of CWT values for every pollutant of this grid. A
  	 *NO DATA VALUE FOR THIS METHOD MUST BE LESS THAN 0 AND NOT EQUAL TO IT. <0)
  	 *@ param varList names of each pollutant
  	 *RETURN: CWT[0 ... K], where K is the number of pollutants in all cells
  	 */
  	public double[] getCalcCWT(String[] varList) throws Exception{
  		//calculate the CWT for this grid!
  		//iteration levels: for each pollutant, through each uniqueID!
  		
		int j = this.taggedUniqueID();
  		for(int i = 0; i < varList.length; i++){
  			Iterator itrUniqueID = this.ids.iterator();
  			double auxSum = 0;
  			
  			for(j = 0; j < this.taggedUniqueID(); j++){
  				String auxUniqueID = (String)itrUniqueID.next();
  				//System.out.println(auxUniqueID); //VERBOSE
  				auxSum += getConcByUniqueID(auxUniqueID, varList[i])*countNIJM(auxUniqueID);
  			}
  			
  			
  			if(this.taggedPop() > 0 && ((!CWTCalc) || (CWTCalc && CWT[i] >= 0))){
  				CWT[i] = auxSum/this.taggedPop(); //do this if this is the first time calculating CWT or if CWT[i] != ndValue
  				//System.out.println(this.latYC + "," + (this.lonXC-360) + "," + CWT[i] + "," + this.taggedPop()); //VERBOSE
  			}
  		}
  		CWTCalc = true;
  		return CWT;
  	}
  	
  	/**
  	 *For all tagged data in this cell, returns an array of logarithmic CWT values for every pollutant of this grid.
  	 *The only difference between this method and getCalcCWT is the use of a logarithmic concentration field, whose use
  	 *can be user defined.
  	 *@ param varList names of each pollutant
  	 *RETURN: CWT[0 ... K], where K is the number of pollutants in all cells
  	 *
  	 */
  	public double[] getCalcCWTLog(String[] varList) throws Exception{
  		//calculate the CWT for this grid!
  		//iteration levels: for each pollutant, through each uniqueID!
		int j = this.taggedUniqueID();
  		for(int i = 0; i < varList.length; i++){
  			Iterator itrUniqueID = this.ids.iterator();
  			double auxSum = 0;
  			
  			for(j = 0; j < this.taggedUniqueID(); j++){
  				String auxUniqueID = (String)itrUniqueID.next();
  				auxSum += Math.log10(getConcByUniqueID(auxUniqueID, varList[i]))*countNIJM(auxUniqueID);
  			}
  			
  			if(this.taggedPop() > 0 && ((!CWTCalc) || (CWTCalc && CWT[i] >= 0))){
  				CWT[i] = auxSum/this.taggedPop();
  			}
  		}
  		CWTCalc = true;
  		return CWT;
  	}
  	
  	
  	/**
  	 *Returns the uncertainty in CWT values at a specified confidence interval.
  	 *@param confInt the confidence interval (1-probability) of the desired analysis; in decimal format (e.g. 0.95, 0.999, 0.90)
  	 *@param varList an array representing the ordered names of all pollutants in this grid 
  	 *@param logUsed true if the concentration field is logarithmic, false if it is not (regular)
  	 */
  	public double[] getCWTUncertainty(double confInt, String[] varList, boolean logUsed) throws Exception{
  		/*REQUIREMENT: CWT[] must already be calculated for this grid
  		 *This ensures that the standard-dev calculations can be made for CI interval to be obtained.
  		 *The invoking class should have CWT[i] +/- this uncertainty returned
  		 **/
  		if(!this.CWTCalc) return null;
  		
  		double CWTUnc[] = new double[CWT.length];
  		
  		for(int x = 0; x < varList.length; x++){ //for every pollutant in varList
  			double sdSquareSum = 0;
  			for(int i = 0; i < points.size(); i++){
  				if(((Point)points.get(i)).hasData()){
  					if(!logUsed){
  						sdSquareSum += Math.pow(Math.abs(CWT[x] - ((Point)points.get(i)).getValue(varList[x])), 2.0);
  					}else{
  						sdSquareSum += Math.pow(Math.abs(CWT[x] - Math.log10(((Point)points.get(i)).getValue(varList[x]))), 2.0);
  					}
  					
  				}
  			}
  			
  			CWTUnc[x] = Math.sqrt(sdSquareSum/((double)this.taggedPop() - 1))*(new Statistics()).AStudT(1-confInt, this.taggedPop()-1)/Math.sqrt((double)this.taggedPop());
  			
  		}
  		return CWTUnc;
  	}
  	
  	/**
  	 *Returns the uncertainty in a CWT value at a specified confidence interval, for a specific pollutant!
  	 *@param confInt the confidence interval (1-probability) of the desired analysis; in decimal format (e.g. 0.95, 0.999, 0.90)
  	 *@param index an array-index representing the ordered names of all pollutants in this grid
  	 *@param polName pollutant name
  	 *@param logUsed true if the concentration field is logarithmic, false if it is not (regular)
  	 */
  	public double getCWTUncertainty(double confInt, int index, String polName, boolean logUsed) throws Exception{
  		/*REQUIREMENT: CWT[] must already be calculated for this grid
  		 *This ensures that the standard-dev calculations can be made for CI interval to be obtained.
  		 *The invoking class should have CWT[i] +/- this uncertainty returned
  		 **/
  		if(!this.CWTCalc) return 0.0;
  		int x = index;
  		double CWTUnc = 0;
  		
		double sdSquareSum = 0;
		for(int i = 0; i < points.size(); i++){
			if(((Point)points.get(i)).hasData()){
				if(!logUsed){
					sdSquareSum += Math.pow(Math.abs(CWT[x] - ((Point)points.get(i)).getValue(polName)), 2.0);
				}else{
					sdSquareSum += Math.pow(Math.abs(CWT[x] - Math.log10(((Point)points.get(i)).getValue(polName))), 2.0);
				}	
			}
		}
  			
  		CWTUnc = Math.sqrt(sdSquareSum/((double)this.taggedPop() - 1))*(new Statistics()).AStudT(1-confInt, this.taggedPop()-1)/Math.sqrt((double)this.taggedPop());
  		
  		return CWTUnc;
  	}
  	
  	/**
  	 *Iterates through all endpoints of this grid: if the matching **UNIQUEID** is found, then the appropriate value is changed.
  	 *SAFETY: state is left unchanged if sourceID is not found, or if other parameters are invalid
  	 *@param sourceID - the unique (trajectory) identifier to search for in this grid
  	 *@param auxIndex - index of the variable list
  	 *@param auxNewValue - the new value to assign the data to
  	 */
  	public void changeConc(String auxUID, int auxIndex, double auxNewValue){ //CHANGE!
  		if(this.taggedUniqueID() <= 0) return;
  		for(int i = 0; i < points.size(); i++){
  			if(((Point)points.get(i)).hasData() && ((Point)points.get(i)).sourceID() != null && ((Point)points.get(i)).thirdDim() != null){
  				if((((Point)points.get(i)).sourceID() + this.lastComma(((Point)points.get(i)).thirdDim())).equalsIgnoreCase(auxUID)){ //changed
  					((Point)points.get(i)).setNewConc(auxIndex, auxNewValue);
  					//((Point)points.get(i)).printInfo(); //VERBOSE TESTING
  				}
  			}
  		}
  	}
  	
  	public void changeConcByMultiple(String auxUID, int auxIndex, double multiple, String polName) throws Exception{ //CHANGE
  		if(this.taggedUniqueID() <= 0) return;
  		for(int i = 0; i < points.size(); i++){
  			if(((Point)points.get(i)).hasData() && ((Point)points.get(i)).sourceID() != null && ((Point)points.get(i)).thirdDim() != null){
  				if((((Point)points.get(i)).sourceID() + this.lastComma(((Point)points.get(i)).thirdDim())).equalsIgnoreCase(auxUID)){
  					double existingConc = ((Point)points.get(i)).getValue(polName);
  					//System.out.println(auxUID + "," + (((Point)points.get(i))).lat() + "," + ((((Point)points.get(i))).lon()-360) + "," + existingConc + "," + multiple); //VERBOSE TESTING 
  					((Point)points.get(i)).setNewConc(auxIndex, multiple*existingConc);
  					//((Point)points.get(i)).printInfo(); //VERBOSE TESTING
  				}
  			}
  		}
  	}
  	
  	/**
  	 *Determines if a TAGGED point with a specific sourceID lies here
  	 *@param sourceID - the unique identifier that we look for
  	 */
  	public boolean containsID(String uniqueID){
  		if(this.taggedUniqueID() <= 0) return false;
  		Iterator itrUniqueID = this.ids.iterator();
  		while(itrUniqueID.hasNext()){
  			if(uniqueID.equals((String)itrUniqueID.next())){
  				return true;
  			}
  		}
  		return false;
  	}
  	
  	/**
  	 *
  	 */
  	public double getQTBA(String polName, double a, int polIndex, int worldRecCount) throws Exception{
  		//Receptor endpoints are NOT counted in the QTBA calculations!
  		
  		if(this.taggedUniqueID() <= 0) return -1;
  		
  		double gridQTBAs[] = new double[this.numReceptors()];
  		double gridNatTs[] = new double[this.numReceptors()];
  		double concWeightedQTBA = 0;
  		double bareGridQTBA = 0; 
  		Statistics statPackage = new Statistics();
  		
  		Iterator itrReceptors = this.receptors.iterator();
  		
  		//calculate the QTBAs FOR EACH receptor in this grid:
		for(int r = 0; r < gridQTBAs.length; r++){
	  		concWeightedQTBA = 0;
			bareGridQTBA = 0; 
			String receptor = (String)itrReceptors.next();
			
	  		for(int i = 0; i < points.size(); i++){
	  			if(((Point)points.get(i)).hasData() && ((Point)points.get(i)).sourceID() != null && ((Point)points.get(i)).thirdDim() != null){
	  				Point auxP = (Point)points.get(i);
	  				String[] auxThirdDim = auxP.thirdDim().split(","); 
	  				
	  				if(auxThirdDim[1].equalsIgnoreCase(receptor.split(",")[0]) && auxThirdDim[2].equalsIgnoreCase(receptor.split(",")[1])){				
		  				double v = statPackage.haversineV(auxP.lat(), auxP.lon()-360, Double.parseDouble(auxThirdDim[1]), Double.parseDouble(auxThirdDim[2]));
		  				
		  				if(Math.abs(v) >= 1E-9){ //explicitly exclude receptor !
		  					double natTxy = statPackage.naturalTransPot(Math.abs(Double.parseDouble(auxThirdDim[3])), v, a);
		  					bareGridQTBA += natTxy;
		  					concWeightedQTBA += (natTxy*auxP.getValue(polName));
		  				}
	  				}
	  				
	  				//System.out.println(auxP.lat() + "," + (auxP.lon()-360) + "," + auxP.sourceID() + "," + auxP.getValue(polName));
	  				
	  				//System.out.println(auxP.lat() + "," + (auxP.lon()-360) + "," + statPackage.naturalTransPot(Math.abs(Double.parseDouble(auxThirdDim[3])), v, a)
	  				//	+ "(T,v,a) = " + Double.parseDouble(auxThirdDim[3]) + "," + v + "," + a); //VERBOSE
	  			}
	  		}
	  		
	  		gridQTBAs[r] = (concWeightedQTBA/bareGridQTBA);
	  		gridNatTs[r] = bareGridQTBA;
		}
		
		//now, process and return the multiple QTBAs!
		if(gridQTBAs.length == 0){
			return -1;
		}else{
			double counts = 0;
			double sum = 0;
			double sumT = 0;
			
			for(int i = 0; i < gridQTBAs.length; i++){
				if(gridQTBAs[i] >= 0){
					sum += gridQTBAs[i];
					sumT += gridNatTs[i];
				}
				counts++;
			}
			
			if(counts == 0) return -1;
			
			if(gridQTBAs.length == 1 && worldRecCount == 1){
				this.gridNatT[polIndex] = gridNatTs[0];
				return gridQTBAs[0];
			}
			
			if(worldRecCount > 1 && gridQTBAs.length > 1){
				this.gridNatT[polIndex] = (sumT/counts);
				return (sum/counts);
			}
		}
		
		return -1;
  	}
  	
  	public double getGridNatT(int polIndex){
  		return this.gridNatT[polIndex];
  	}
  	
  	public double gridLat(){
  		return this.latYC;
  	}	
  		
  	public double gridLon(){
  		return this.lonXC;
  	}
  	 	
 	private String lastComma(String aux){
 		try{
 			return aux.substring(0, aux.lastIndexOf(","));
 		}catch(Exception e){
 			return aux;
 		}
 	}
  	
  	
  	
  	
  	public void printState(){
  		System.out.println("GRID, Corner = (" + this.lonXC + " , " + this.latYC + " ) ");
  		System.out.println("Total Pop = " + this.population());
  		System.out.println("Tagged Pop = " + this.taggedPop());
  		System.out.println("Unique IDs = " + this.uniqueID());
  		System.out.println("Tagged Uniqud IDs = " + this.taggedUniqueID());
  	}
  	
  	/**
  	 *testing only.
  	 */
  	public static void main(String[] args) throws Exception{
  		/*Grid auxG = new Grid(1,1);
  		int numPoints = 240000;
  		Pair [] data = new Pair[10];
  		
  		for(int i = 0; i < 10; i++){ 
  			data[i] = new Pair("Pollutant" + (char)(65+i), 0.2354);
  		}
  		
  		
  		for(int i = 0; i < numPoints; i++){
  			auxG.addPoint(new Point(1.5,1.5, "Point"));
  		}
  		
  		System.out.println(auxG.population() + " points added to this Grid");
  		
  		auxG.tagPoints("Point", data);
  		
  		auxG.calcMIJ(data);
  			
  		for(int i = 0; i < auxG.mij.length; i++){
  			System.out.println(data[i].name + "\t\t MIJ = " + auxG.mij[i]);
  		}
  		
  		auxG.printState();
  		
  		*/
  		
  		Grid auxG = new Grid(1,1);
  		
  		auxG.addPoint(new Point(1.5, 1.5, "x", ""));
  		auxG.addPoint(new Point(1.5, 1.5, "x", ""));
  		auxG.addPoint(new Point(1.5, 1.5, "y", ""));
  		auxG.addPoint(new Point(1.5, 1.5, "z", ""));
  		auxG.addPoint(new Point(1.5, 1.5, "z", ""));
  		auxG.addPoint(new Point(1.5, 1.5, "m", ""));
  		
		System.out.println("Contains x,y,z,m? " + auxG.containsID("x") + "," + 
		auxG.containsID("y") + "," + auxG.containsID("z") + "," + auxG.containsID("m"));

  		
  		
  		Pair [] data = new Pair[2];
  		String [] polList = {"Pollutant A", "Pollutant B"};
  		data[0] = new Pair("Pollutant A", 2.41);
  		data[1] = new Pair("Pollutant B", 2.39);
//  		auxG.tagPoints("x", data);
  		System.out.println("Contains x,y,z,m? " + auxG.containsID("x") + "," + 
  			auxG.containsID("y") + "," + auxG.containsID("z") + "," + auxG.containsID("m"));
  		
  		data[0] = new Pair("Pollutant A", 1.50);
  		data[1] = new Pair("Pollutant B", 3.14);
//  		auxG.tagPoints("y", data);
  		System.out.println("Contains x,y,z,m? " + auxG.containsID("x") + "," + 
  			auxG.containsID("y") + "," + auxG.containsID("z") + "," + auxG.containsID("m"));
  		
  		data[0] = new Pair("Pollutant A", 1.59);
  		data[1] = new Pair("Pollutant B", 3.14);
//  		auxG.tagPoints("z", data);
  		
  		System.out.println("Contains x,y,z,m? " + auxG.containsID("x") + "," + 
  			auxG.containsID("y") + "," + auxG.containsID("z") + "," + auxG.containsID("m"));
  		
//  		auxG.tagPoints("m", data);
  		System.out.println("Contains x,y,z,m? " + auxG.containsID("x") + "," + 
  			auxG.containsID("y") + "," + auxG.containsID("z") + "," + auxG.containsID("m"));
  		
  		/*Pair table[] = auxG.nijmTable();
  		
  		for(int i = 0; i < table.length; i++){
  			System.out.println(table[i].name + "\t\t" + table[i].value);
  		}*/
  		
  	//	auxG.printState();
  		
  		
  		
  		/*if(auxG.taggedPop() > 0){
  			
	  		double[] gridCWT = auxG.getCalcCWTLog(polList);
	  		double [] gridCWTUnc = auxG.getCWTUncertainty(0.95, polList, true);
	  		System.out.println((new Statistics()).AStudT(0.05, 4));
	  		
	  		for(int i = 0; i < gridCWT.length; i++){
	  			System.out.println(polList[i] + " CWT=\t" + gridCWT[i] + ", Unc:=\t " + gridCWTUnc[i]);
	  		}
	  		
	  		
	  		
	  		gridCWT = auxG.getCalcCWT(polList);
	  		gridCWTUnc = auxG.getCWTUncertainty(0.95, polList, false);
	  		
	  		for(int i = 0; i < gridCWT.length; i++){
	  			System.out.println(polList[i] + " CWT=\t" + gridCWT[i] + ", Unc:=\t " + gridCWTUnc[i]);
	  		}
	  		
	  		
	  		System.out.println("Unc in Pollutant A = " + auxG.getCWTUncertainty(0.95, 1, polList[1], false));
  		}*/
  		
  		

  	}
  	
  	
  	
  }
  
  