/*World.java
 *Ankit Rastogi
 *Project DensityNorth
 *Started: May 6, 2010
 *Updated: May 10, 2010 
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
 import java.text.*;
 import java.util.*;
 
/**
 * A representation of all or part of the Northern Hemisphere, segmented into grids with a specified dimension. 
 * <p> The world spans a specified latitude and longitude relevant to the northern hemisphere and is represented by an array of <code>Grid</code> objects. 
 * The dimensions of the grid determines the resolution obtained in further data analyses.
 *@author Ankit Rastogi
 */
 public class World{
 	
 	/**
 	 *The width of each grid in terms of longitude.
 	 */
 	private final double lonDX;
 	
 	/**
 	 *The height of each grid in terms of latitude.
 	 */
 	private final double latDY;
 	
 	/**
 	 *The total longtitude of this World object.
 	 */
 	private int totalLon;
 	
 	/**
 	 *The total latitude of this World object
 	 */
 	private int totalLat;
 	
 	/**
 	 *The 2D array of Grid objects representing the world.
 	 */
 	private Grid nHem[][];
 	
 	/**
 	 *A 3D matrix containing a set of PSCF values (for the set of correlated data), 
 	 *for each grid in this World.
 	 */
 	private double PSCF[][][];
 	
 	/**
 	 *A 3D matrix containing a set of CWT values for each grid in this world, 
 	 *for each correlated variable
 	 */
 	private double CWT[][][];
 	
 	/**
 	 *A 3D matrix containing a set of CWT values for each grid in this world 
 	 *from a PREVIOUS iteration.
 	 */
 	 private double oldCWT[][][];
 	
 	/**
 	 *A 3D matrix containing the final set of CWT values to write to disk
 	 *for RTWC calculations. This accounts for the fact that different correlated variables
 	 *in <code>CMCRender.varList[]</code> will converge at different times in the calculation.
 	 */
 	 private double finalCWT[][][];
 	
 	/**
 	 *A 3D matrix containing a set of QTBA values for each grid in this world; NOTE that the QTBA
 	 *method can only be used if HYSPLIT files are being used.
 	 */
 	 private double QTBA[][][];
 	
 	/**
 	 *Formats numerical variables to a string in the format 0.000. For example, 4.1299 is formatted to 4.129.
 	 */
 	private DecimalFormat dfL = new DecimalFormat("0.000");
 	
 	/**
 	 *Contains PSCF weighting factors as an array of Triplets.
 	 */
 	Triplet[] PSCFWeights;
 	
 	/**
 	 *Contains the set of all unique/sourceIDs used for RTWC analysis
 	 */
 	 private HashSet<String> worldIds;
 	
 	//this will be constructor for GUI interface!
 	/**
 	 *Creates a new instance of a World object.
 	 *<p>
 	 *@param lon the total longitude of this World in degrees.
 	 *@param lat the total latitude of this World in degrees.
 	 *@param dX the width of each grid as latitude degrees. Note that <code>dX</code> cannot exceed <code>lat</code> and should divide it evenly. 
 	 *@param dY the height of each grid as longitude degrees. Note <code>dY</code>this cannot exceed <code>lon</code> and should divide it evenly.
 	 *If the conditions of <code>dX</code> and <code>dY</code> are not met, the default size of each grid is 1 x 1 degrees.
 	 */
 	public World(int lon, int lat, double dX, double dY){ //latitude, longitude dimensions (1x1, 0.5x0.5)
 		if((lon%dX == 0 && lat%dY == 0) || (dX < 1 && dY < 1 && dX > 0 && dY > 0)){
 			totalLon = lon;
 			totalLat = lat;
 			lonDX = dX;
 			latDY = dY;
 			nHem = new Grid[(int)(totalLon/dX)][(int)(totalLat/dY)];
 			PSCF = new double[(int)(totalLon/dX)][(int)(totalLat/dY)][];
 			CWT = new double[(int)(totalLon/dX)][(int)(totalLat/dY)][];
 			oldCWT = new double[(int)(totalLon/dX)][(int)(totalLat/dY)][];
 			finalCWT = new double[(int)(totalLon/dX)][(int)(totalLat/dY)][];
 			QTBA =  new double[(int)(totalLon/dX)][(int)(totalLat/dY)][];
 		}else{
 			lonDX = 1;
 			latDY = 1;
 			totalLon = lon;
 			totalLat = lat;
 			nHem = new Grid[totalLon][totalLat];
 			PSCF = new double[totalLon][totalLat][];
 			CWT = new double[totalLon][totalLat][];
 			oldCWT = new double[totalLon][totalLat][];
 			finalCWT = new double[totalLon][totalLat][];
 			QTBA = new double[totalLon][totalLat][];
 			
 		}
 		
 		//initialize the grid as well:
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				nHem[i][j] = new Grid(lonDX*i, latDY*j);
 			}
 		}
 		
 		//default PSCF weights should all be 1, with 5 classes.
 		PSCFWeights = new Triplet[5];
 		for(int i = 0; i < PSCFWeights.length; i++){
 			PSCFWeights[i] = new Triplet(1,1,1);
 		}
 		
 		//initialize the sourceID arrays:
 		worldIds = new HashSet<String>();
 	}
 	
 	
 	private String lastComma(String aux){
 		try{
 			return aux.substring(0, aux.lastIndexOf(","));
 		}catch(Exception e){
 			return aux;
 		}
 	}
 	
 	/**
 	 *Adds a point to the appropriate grid of this World object.
 	 *@return true The point was successfully added to this World.
 	 *@return false The point could not be added to this World.  
 	 */
 	public boolean addPointToWorld(Point x){
 		if(this.totalLat > 90 && (x.lon() >= 0 && x.lon() < this.totalLon)){
 			
 			worldIds.add(x.sourceID() + this.lastComma(x.thirdDim()));
 			
 			if(x.lat() < 0 && x.lat() >= (90-this.totalLat)){
 				int auxI = (int)(x.lon()/lonDX);
 				int auxJ = (int)((89-x.lat())/latDY);
 				nHem[auxI][auxJ].addPoint(x);
 				return true;
 			}
 			
 			if(x.lat() >= 0 && x.lat() < 90){
 				int auxI = (int)(x.lon()/lonDX);
 				int auxJ = (int)(x.lat()/latDY);
 				nHem[auxI][auxJ].addPoint(x);
 				return true;
 			}
 		}else{
 			
 			worldIds.add(x.sourceID() + this.lastComma(x.thirdDim()));
 			
	 		if((x.lon() >= 0 && x.lon() < this.totalLon) && (x.lat() >= 0 && x.lat() < this.totalLat)){
	 			int auxI = (int)(x.lon()/lonDX);
	 			int auxJ = (int)(x.lat()/latDY);
	 			nHem[auxI][auxJ].addPoint(x);
	 			return true;
	 		}
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 *An accessor which returns the total number of points in the world.
 	 */
 	public int hemPopulation(){
 		//returns the total number of points in the world
 		int counter = 0;
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				counter += nHem[i][j].population();
 			}
 		}
 		return counter;
 	}
 	
 	/**
 	 *A modifier which sets the weight of the PSCF function.
 	 *<p> The length of the <code>weights</code> array defines the number of classes. For all other population values which are not defined
 	 *in <code>weights</code>, the default PSCF value is set to 1. 
 	 *@param weights 	A new set of weighting factors to be used by the PSCF function. 
 	 *<p>REQUIRES: weights cannot be null or empty. each element of weights must be instantiated. Redundancy in values is allowed yet not resolved.
 	 */
 	public void changeWeights(Triplet[] weights){
 		if(weights != null && weights.length > 0) PSCFWeights = weights; //POINTER ONLY
 	}
 	
 	/**
 	 *Adds correlated data to points in the world with the appropriate source ID
 	 *@param targetIDW  the desired identifier of each point. Points with this identifier are tagged.
 	 *@param dataW the data with which to tag the point specified by <code>targetIDW</code>
 	 *<p>REQUIRES: dataW is not null or empty. If the identifier cannot be found, the data is not tagged with any point.
 	 */
 	//requires: dataW is not null or empty
 	public void tagWorld(String targetIDW, Pair[] dataW, String latR, String lonR){
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				nHem[i][j].tagPoints(targetIDW, dataW, latR, lonR);	
 			}
 		}
 	}
 	
 	/**
 	 *Returns the weight for the PSCF function. If no weight is found or specified, a default weight of 1 is returned.
 	 *@param nij the population of the points in a specific grid, used to determine the appropriate weighting function to return.
 	 */
 	private double getWeight(int nij){
 		for(int i = 0; i < PSCFWeights.length; i++){
 			if(nij <= PSCFWeights[i].high && nij >= PSCFWeights[i].low){
 				return PSCFWeights[i].weight;
 			}
 		}
 		return 1;
 	}
 	
 	/**
 	 *Returns the weight for CWT/QTBA/RTWC weighting functions; if no weight is found or specified,
 	 *a default weight of 1 is returned
 	 *@param value the floating point value of the weight
 	 *@param weights the weighting table
 	 */
 	private double getAdvancedWeight(double value, AdvancedTriplet[] weights){
 	 	for(int i = 0; i < weights.length; i++){
 	 		if(value >= weights[i].low && value <= weights[i].high){
 	 			return weights[i].weight;
 	 		}
 	 	}
 	 	return 1;
 	}
 	
 	/**
 	 *Weights the appropriate CWT/QTBA/RTWC weighting functions; if no weight is found or specified,
 	 *a default weight of 1 is returned. Should be run after at least 1 corresponding calculation, and 
 	 *also BEFORE matrix printing methods are called!
 	 *@param calcMethod - the calculation method to use
 	 *	<p> calcMethod = "CWT": the CWT matrix is weighted. must be done after at least 1 CWT matrix is calculated or smoothed
 	 *	<p> calcMethod = "RTWC": the final RTWC-CWT matrix is weighted after all iterations
 	 *	<p> calcMethod = "QTBA": the final QTBA matrix is weighted after at least 1 calculation is run
 	 *@param weights; the floating point weights to apply
 	 */
 	public void applyAdvancedWeight(String calcMethod, AdvancedTriplet[] weights){
 		
 		if(calcMethod.equalsIgnoreCase("CWT")){
 			//iterate through the entire matrix to apply the weights.
 			for(int i = 0; i < nHem.length; i++){
 				for(int j = 0; j < nHem[i].length; j++){
 					if(CWT[i][j] != null){
 						for(int k = 0; k < CWT[i][j].length; k++){
 							CWT[i][j][k] = this.getAdvancedWeight(nHem[i][j].taggedPop(), weights)*CWT[i][j][k];
 						}
 					}
 				}
 			}
 		}
 		
 		if(calcMethod.equalsIgnoreCase("RTWC")){
 			//iterate through the entire matrix to apply the weights.
 			for(int i = 0; i < nHem.length; i++){
 				for(int j = 0; j < nHem[i].length; j++){
 					if(finalCWT[i][j] != null){
 						for(int k = 0; k < finalCWT[i][j].length; k++){
 							finalCWT[i][j][k] = this.getAdvancedWeight(nHem[i][j].taggedPop(), weights)*finalCWT[i][j][k];
 						}
 					}
 				}
 			}
 		}
 		
 		if(calcMethod.equalsIgnoreCase("QTBA")){
 			//iterate through the entire matrix to apply the weights.
 			for(int i = 0; i < nHem.length; i++){
 				for(int j = 0; j < nHem[i].length; j++){
 					if(QTBA[i][j] != null){
 						for(int k = 0; k < QTBA[i][j].length; k++){
 							QTBA[i][j][k] = this.getAdvancedWeight(nHem[i][j].gridNatT[k], weights)*QTBA[i][j][k];
 						}
 					}
 				}
 			}
 		}
 		
 	}
 	
 	
 	/**
 	 *Computes PSCF values for points in the World with correlated data.
 	 *@param threshDataW the threshold data as a Pair of values
 	 */
 	public void calcPSCF(Pair[] threshDataW) throws Exception{
 		if(threshDataW == null || threshDataW.length == 0) return;
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				//DO ONLY FOR THOSE GRIDS THAT HAVE BEEN TAGGED; i.e., THE MIJ ARRAY IS NOT NULL.
 				//Grids containing a population of zero yet allocated data, get PSCF = 0. this is handled in the case below
 				//Grids containing points which have NO correlated data have a NULL PSCF value and are treated the same as empty grids; therefore, mij = null and PSCF = null
 				if(nHem[i][j].mij != null && threshDataW.length == nHem[i][j].mij.length){
 					PSCF[i][j] = new double[threshDataW.length];
					nHem[i][j].calcMIJ(threshDataW);
					for(int k = 0; k < PSCF[i][j].length; k++){
						int popTemp = nHem[i][j].taggedPop();
						PSCF[i][j][k] = getWeight(popTemp)*(((nHem[i][j]).mij[k])/((Integer)popTemp).doubleValue());
					}
 				}	
 			}
 		}
 	}
 	
 	/**
 	 *Computes a new matrix of "CWT" values for each grid.
 	 *@param varList; an array of correlated data variable names, from the correlated data file
 	 */
 	 public void calcCWT(String[] varList) throws Exception{
 	 	
 	 	if(varList == null || varList.length == 0) return;
 	 	for(int i =0; i < nHem.length; i++){
 	 		for(int j = 0; j < nHem[i].length; j++){
 	 			if(nHem[i][j].taggedPop() > 0){
 	 				CWT[i][j] = new double[varList.length];
 	 				CWT[i][j] = nHem[i][j].getCalcCWT(varList);
 	 			}
 	 			
 	 			//otherwise, CWT[i][j] remains null, just like the PSCF matrix.
 	 		}
 	 	}
 	 }
 	 
 	/**
 	 *Computes a new matrix of log-CWT values for each grid
 	 *@param varList an array of correlated data variable names, derived from the correlated data
 	 *file
 	 */ 	 
 	public void calcCWTLog(String[] varList) throws Exception{
 		if(varList == null || varList.length == 0) return;
 	 	for(int i =0; i < nHem.length; i++){
 	 		for(int j = 0; j < nHem[i].length; j++){
 	 			if(nHem[i][j].taggedPop() != 0){
 	 				CWT[i][j] = new double[varList.length];
 	 				CWT[i][j] = nHem[i][j].getCalcCWTLog(varList);
 	 			}
 	 			
 	 			//otherwise, CWT[i][j] remains null, just like the PSCF matrix.
 	 		}
 	 	}
 	}
 	
 	/**
 	 *Uses an implemented Savitzky-Golay filter with mean-value, left and right padding to smooth the CWT field.
 	 *@param varList an array of correlated data variable names
 	 *@param ndValue the value used for representing noData on the CWT grid. NOTE: ndValue should be the same as that 
 	 *specified for printing CWT-matrices to disk (usually, it is -1.000)
 	 *@param filterLength an odd-numbered length representing 2N+1 (the filter length), where N are the left and right
 	 *bounds used for data smoothing. Generally, the larger the length of the filter, the greater the smoothing effect.
 	 *@param confInt the confidence interval (1-probability) of the desired analysis, in decimal format (e.g. 0.95, 0.90, etc.)
 	 *@param polyDegree a user-defined parameter which specifies the degree of the polynomial to use smoothing with
 	 *@param logField true if the CWT-field was logarithmically computed, false if it was computed traditionally.
 	 *REQUIRES: the CWT-matrix MUST already be calculated, and its type (log vs. linear) must be known!
 	 */
 	//REVISION NOTE: preconditions for nullified CWT[i][j] NEEDS UPDATE: Oct. 16/2012. OK updated Dec. 25 2012.
 	public void smoothCWTField(String[] varList, double ndValue, int filterLength, int polyDegree, double confInt, boolean logField) throws Exception{
 		for(int x = 0; x < varList.length; x++){
 			double[][] smCWT = new double[(int)(totalLon/lonDX)][(int)(totalLat/latDY)];
 			ArrayList<Double> vectCWT = new ArrayList<Double>();
 			
 			//map the current CWT matrix to an array by iterating through all grids
 			for(int i = 0; i < nHem.length; i++){
 				for(int j = 0; j < nHem[i].length; j++){
 					if(CWT[i][j] != null && CWT[i][j][x] != ndValue){ //TEST
 						vectCWT.add(CWT[i][j][x]); //this still contains ndValues!
 					}
 				}
 			}
 			
 			//smooth the current CWT vector:
 			double[] vectCWTArray = new double[vectCWT.size()];
 			for(int n = 0; n < vectCWTArray.length; n++) vectCWTArray[n] = vectCWT.get(n);
 			double[] smVectCWT = (new Statistics()).smoothData(vectCWTArray, filterLength, polyDegree);
 			
 			//re-map the smVectCWT to the new smCWT matrix, and then to CWT[i][j][x] if eligible:
 			int vectorIndex = 0;
 			for(int i = 0; i < nHem.length; i++){
 				for(int j = 0; j < nHem[i].length; j++){
 					if(CWT[i][j] != null  && CWT[i][j][x] != ndValue){
 						double maxThresh = CWT[i][j][x] + nHem[i][j].getCWTUncertainty(confInt, x, varList[x], logField);
 						double minThresh = CWT[i][j][x] - nHem[i][j].getCWTUncertainty(confInt, x, varList[x], logField);
 						if(smVectCWT[vectorIndex] >= minThresh && smVectCWT[vectorIndex] <= maxThresh){
 							CWT[i][j][x] = smVectCWT[vectorIndex];
 							nHem[i][j].changeCWT(x, CWT[i][j][x]);
 						}else{
 							CWT[i][j][x] = ndValue;
 							nHem[i][j].changeCWT(x, ndValue);
 						}
 						vectorIndex++;
 					}			
 				}
 			}	
 		}
 	}
 	
 	/**
 	 *Uses an RTWC Iterative Method to recalculate and converge the CWT-field. Requires that 
 	 *the LINEAR CWT field has already been calculated for the added data. There is no smoothing
 	 *in this method and we rely on a linear convergence here.
 	 *@param varList an array of correlated data variable names
 	 */
 	public void reDistConc(String[] varList, int ndValue) throws Exception{
 		//NOTE: CWT or a smoothed CWT should already exist in CWT[i][j][x].
 		//FOR EACH SOURCE ID => FOR EACH POLLUTANT.
 		Iterator itrWorldIds = this.worldIds.iterator();
 		while(itrWorldIds.hasNext()){
 			String auxID = (String)itrWorldIds.next();
 			for(int x = 0; x < varList.length; x++){ //FOR EACH POLLUTANT
 				double avgCWTx = this.avgCWT(auxID, x, ndValue);
 				//System.out.println("auxID = " + auxID + ", avgCWTx = " + avgCWTx); //VERBOSE TESTING
 				//now, recalculate the concentration field!
 				if(avgCWTx >= 0){
					for(int i =0; i < nHem.length; i++){
						for(int j = 0; j < nHem[i].length; j++){
							if(nHem[i][j].containsID(auxID) && CWT[i][j][x] != ndValue){
								nHem[i][j].changeConcByMultiple(auxID, x, (CWT[i][j][x]/avgCWTx), varList[x]);
							}
						}
					}
 				}//safety check if avgCWT = 0
 			}//each pollutant
 		}//each trajectory	
 		
 	}
 	
 	private double avgCWT(String auxSourceID, int polIndex, int ndValue){
		double CWTAvgSUM = 0;
		double counter = 0; 
			
		for(int i =0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				if(nHem[i][j].containsID(auxSourceID)){
 					//SAFETY CHECK: should be excluded from precondition, however.
 					if(CWT[i][j] == null){
 						return ndValue;
 					}
 					
 					if(CWT[i][j][polIndex] != ndValue){
 						CWTAvgSUM += CWT[i][j][polIndex];
 						counter++;
 					}
 				}
 			}
 		}
 		
 		//RETURN FINAL
 		if(counter <= 0 || CWTAvgSUM <= 0){
 			return ndValue;
 		}else{
 			//System.out.println(auxSourceID + ", count = " + counter);
 			return CWTAvgSUM/counter;
 		}
 	}
 	
 	/**
 	 *RTWC method which uses redistributed concentration fields with CWT. NOTE: a PREVIOUS CWT
 	 *CALCULATION NEED NOT BE CONDUCTED; an iteration is counted as a single CWT calculation.
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
 	public void calcRTWC(String[] varList, int optionSelect, double convPercent, int maxIterations, int pointFilter, int polyDegree, double confInt, int ndValue) throws Exception{
    	double PDiff[] = new double[varList.length];
    	boolean alreadyConverged[] = new boolean[varList.length];
    	for(int i = 0; i < PDiff.length; i++){
    		PDiff[i] = -1;
    		alreadyConverged[i] = false;
    	}
    	
    	if(optionSelect == 1 || optionSelect == 4){
    		//no smoothing, fixed iterations (maxIterations)
    		this.calcCWT(varList);
    		for(int r = 0; r < maxIterations-1; r++){
    			this.reDistConc(varList, ndValue);
    			this.calcCWT(varList);
    			System.out.println("RTWC Iteration: " + (r+1)); //VERBOSE TESTING
    			if(optionSelect ==  4) this.smoothCWTField(varList, ndValue, pointFilter, polyDegree, confInt, false);
    		}
    		
    		//after the iterations, finalize the matrix
    		for(int k = 0; k < varList.length; k++){
    			this.finalizeCWT(k, varList);
    		}
    		
    		return;
    		
    	}else{
    		//linear CWT; no smoothing, use convergence criteria
    		this.calcCWT(varList);
    		int iterations = 0;
    		boolean converged = false;
    		
    		while(!converged){
    			this.copyToOldCWT(varList);
    			this.reDistConc(varList, ndValue);
    			this.calcCWT(varList);
    			if(optionSelect ==  3) this.smoothCWTField(varList, ndValue, pointFilter, polyDegree, confInt, false);
    			iterations++;
    			
    			PDiff = percentDiff(varList, ndValue);
    			
    			for(int k = 0; k < PDiff.length; k++){
    				System.out.println("PDiff(" + varList[k] + ") = " + PDiff[k]); //VERBOSE TESTING
    				if(PDiff[k] <= convPercent && PDiff[k] >= 0){
    					if(!alreadyConverged[k]){
    						this.finalizeCWT(k, varList);
    						alreadyConverged[k] = true;
    					}
    				}
    			}
    			
    			//iterate through the "already converged" variables to decide to stop
    			if(iterations >= maxIterations){
    				for(int k = 0; k < varList.length; k++){
    					this.finalizeCWT(k, varList);
    				}	
    				converged = true;
    			}else{
    				converged = true;
    				for(int k = 0; k < alreadyConverged.length; k++){
    					converged = (converged && alreadyConverged[k]);
    				}
    			}
    		}
 		}
    	
 	}
 	
 	/**
 	 *Determines the average percent difference between two CWT matrices for every pollutant.
 	 *the <code>finalCWT</code> 3D array will contain the converged results.
 	 *percentDiff will only count values != ndValue in the average
 	 */
 	private double[] percentDiff(String varList[], int ndValue){
 		if(varList == null) return null;
 		
 		double PD[] = new double[varList.length];
 		
 		for(int k = 0; k < varList.length; k++){
			
			double avgSumNew = 0; double avgSumNewCounter = 0;
			double avgSumOld = 0; double avgSumOldCounter = 0;
			
			for(int i = 0; i < nHem.length; i++){
 				for(int j = 0; j < nHem[i].length; j++){
 					if(CWT[i][j] != null && CWT[i][j][k] > 0){
 						avgSumNew += CWT[i][j][k];
 						avgSumNewCounter++;
 					}
 					
 					if(oldCWT[i][j] != null && oldCWT[i][j][k] > 0){
 						avgSumOld += oldCWT[i][j][k];
 						avgSumOldCounter++;
 					}
 				}
			}
			
			double newAvg = avgSumNew/avgSumNewCounter;
			double oldAvg = avgSumOld/avgSumOldCounter;
			//System.out.println("\tNewAvg,OldAvg = " + newAvg + "," + oldAvg); //VERBOSE TESTING
			
			PD[k] = Math.abs((newAvg - oldAvg)*100/(oldAvg));
			//alternative: PD[k] = Math.abs(((avgSumNew/avgSumNewCounter)/(avgSumOld/avgSumOldCounter) - 1))*100;
 		}
 		
 		return PD;
 	}
 	
 	private void finalizeCWT(int polIndex, String[] varList){
 		for(int i = 0; i < nHem.length; i++){
			for(int j = 0; j < nHem[i].length; j++){
				if(CWT[i][j] != null){
					finalCWT[i][j] = new double[varList.length];
					finalCWT[i][j][polIndex] = CWT[i][j][polIndex];
				}
			}
 		}
 	}
 	
 	private void copyToOldCWT(String[] varList){
 		for(int i = 0; i < nHem.length; i++){
			for(int j = 0; j < nHem[i].length; j++){
				if(CWT[i][j] != null){
					/*if(oldCWT[i][j] == null)*/ oldCWT[i][j] = new double[varList.length];
					for(int k = 0; k < varList.length; k++){
						oldCWT[i][j][k] = CWT[i][j][k];
					}
				}
			}
		}
 	}
 	
 	/**
 	 *Performs QTBA calculations on the entire world for every pollutant. ONLY HYSPLIT FILES CAN BE USED. 
 	 *The calculation methods are relatively simple.
 	 *@param varList an array of correlated data variable names, from the correlated data file 
 	 *@param a the atmospheric dispersion velocity (see the MetCor implementation note on QTBA); provide value in km/hr.
 	 */
 	 public void calcQTBA(String varList[], double a){
 	 	for(int i = 0; i < nHem.length; i++){
 	 		for(int j = 0; j < nHem[i].length; j++){
 	 			if(nHem[i][j].taggedPop() > 0){
 	 				QTBA[i][j] = new double[varList.length];
 	 				for(int k = 0; k < varList.length; k++){
 	 					try{
 	 						QTBA[i][j][k] = nHem[i][j].getQTBA(varList[k], a, k);
 	 					}catch(Exception e){
 	 						e.printStackTrace();
 	 						QTBA[i][j][k] = 0;
 	 					}
 	 				}
 	 			}
 	 		}
 	 	}
 	 }
 	 
 	 /**
 	  *Generates the average valueof the Natural Transport Potential Function; averaged
 	  *across all grids (with tagged endpoints). Returns a formatted block of text which
 	  *can be written to disk by CMCRender.
 	  *@param varList the list of correlated variables 
 	  */
 	 public ArrayList<String> avgGridNatT(String varList[]){
 	 	
 	 	ArrayList<String> output = new ArrayList<String>();
 	 	
 	 	output.add("POLLUTANT\t\tNATURAL TRANSPORT POT. FUNC");
 	 
 	 	for(int k = 0; k < varList.length; k++){
 	 		double noTagged = 0;
 	 		double sumQTBAk = 0;
 	 		
 	 		for(int i = 0; i < nHem.length; i++){
 	 			for(int j = 0; j < nHem[i].length; j++){
 	 				if(nHem[i][j].taggedPop() > 0){
 	 					sumQTBAk += nHem[i][j].getGridNatT(k);
 	 					noTagged++;
 	 				}		
 	 			}
 	 		}
 	 		
 	 		output.add(varList[k] + "\t\t" + (sumQTBAk/noTagged));
 	 		
 	 	}
 	 	
 	 	output.add("------------------------------");
 	 	
 	 	return output;
 	 }
 	 
 	  /**
 	  *Generates the average tagged Grid population across all grids (with tagged endpoints). 
 	  *Returns a formatted block of text which can be written to disk by CMCRender.
 	  *@param varList the list of correlated variables 
 	  */
 	 public ArrayList<String> avgTaggedNIJ(String varList[]){
 	 	ArrayList<String> output = new ArrayList<String>();
 	 	
 	 	output.add("POLLUTANT\t\tAVERAGE NIJ");
 	 
 	 	for(int k = 0; k < varList.length; k++){
 	 		double noTagged = 0;
 	 		double sumAvgNIJ = 0;
 	 		
 	 		for(int i = 0; i < nHem.length; i++){
 	 			for(int j = 0; j < nHem[i].length; j++){
 	 				if(nHem[i][j].taggedPop() > 0){
 	 					sumAvgNIJ += nHem[i][j].taggedPop();
 	 					noTagged++;
 	 				}		
 	 			}
 	 		}
 	 		
 	 		output.add(varList[k] + "\t\t" + (sumAvgNIJ/noTagged));	
 	 	}
 	 	
 	 	output.add("------------------------------");
 	 	
 	 	return output;
 	 }
 	
 	 public void calcPSCFBySourceID(Pair[] threshDataW) throws Exception{
 		if(threshDataW == null || threshDataW.length == 0) return;
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				//DO ONLY FOR THOSE GRIDS THAT HAVE BEEN TAGGED; i.e., THE MIJ ARRAY IS NOT NULL.
 				//Grids containing a population of zero yet allocated data, get PSCF = 0. this is handled in the case below
 				//Grids containing points which have NO correlated data have a NULL PSCF value and are treated the same as empty grids; therefore, mij = null and PSCF = null
 				if(nHem[i][j].mij != null && threshDataW.length == nHem[i][j].mij.length){
 					PSCF[i][j] = new double[threshDataW.length];
					nHem[i][j].calcMIJ(threshDataW);
					for(int k = 0; k < PSCF[i][j].length; k++){
						int popTemp = nHem[i][j].taggedPop();
						PSCF[i][j][k] = getWeight(nHem[i][j].taggedUniqueID())*(((nHem[i][j]).mij[k])/((Integer)popTemp).doubleValue());
					}
 				}	
 			}
 		}
 	}
 	
 	
 	/**
 	 *Returns the largest number of points found in any grid of this World. 
 	 *<p> All Points, including ones which have no correlated data, are included in the calculation. 
 	 */
 	public int getMaxNIJ(){ 
 		int maxNIJ = 0;
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				if(nHem[i][j].population() > maxNIJ) maxNIJ = nHem[i][j].population();
 			}
 		}
 		return maxNIJ;
 	}
 	
 	/**
 	 *Returns the largest number of points with correlated data found in any grid of this World.
 	 *<p> Only those points with correlated data are included in the calculation.
 	 */
 	private int getMaxTaggedNIJ(){
 		int maxNIJ = 0;
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				if(nHem[i][j].taggedPop() > maxNIJ) maxNIJ = nHem[i][j].taggedPop();
 			}
 		}
 		return maxNIJ;
 	}
 	
 	 private int getMaxTaggedSource(){
 		int maxUnique = 0;
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				if(nHem[i][j].taggedUniqueID() > maxUnique) maxUnique = nHem[i][j].taggedUniqueID();
 			}
 		}
 		return maxUnique;
 	}
 	
 	/**
 	 *Returns a tab-delimited histogram enumerating the population of each grid and the frequency at which this population is observed. Points with and without
 	 *correlated data are enumerated.
 	 *<p> The bin size of this histogram is determined by the range of populations observed in each grid and <code>intervals</code>.
 	 *@param intervals the number of classes to use in generating the histogram. A larger interval yields a more resolved histogram.
 	 *<p> REQUIRES: <code>intervals</code> must be greater or equal to 1.
 	 */
 	public String[] getHistData(int intervals){
 		if(intervals <= 0) return null;
 		int[] freq = new int[intervals];
 		String[] freqOut = new String[intervals];
 		double spacing = Math.ceil((double)getMaxNIJ()/intervals);
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				if(nHem[i][j].population() != 0){
 					freq[(int)(nHem[i][j].population()/spacing)] += nHem[i][j].population();
 				}
 			}
 		}
 		
 		for(int i = 0; i < freqOut.length; i++){
 			freqOut[i] = i*(spacing) + "\t" + freq[i]; 
 		}
 		return freqOut;
 	}
 	
 	/**
 	 *Returns a tab-delimited histogram enumerating the population of each grid and the frequency at which this population is observed. Only points with correlated data are enumerated.
 	 *<p> The bin size of this histogram is determined by the range of populations observed in each grid and <code>intervals</code>.
 	 *@param intervals the number of classes to use in generating the histogram. A larger interval yields a more resolved histogram.
 	 *<p> REQUIRES: <code>intervals</code> must be greater or equal to 1.
 	 */
 	 public String[] getTaggedHistData(int intervals){
 		if(intervals <= 0) return null;
 		int[] freq = new int[intervals];
 		String[] freqOut = new String[intervals];
 		double spacing = Math.ceil((double)getMaxTaggedNIJ()/intervals);
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				if(nHem[i][j].taggedPop() != 0){
 					freq[(int)(nHem[i][j].taggedPop()/spacing)] += nHem[i][j].taggedPop();
 				}
 			}
 		}
 		
 		for(int i = 0; i < freqOut.length; i++){
 			freqOut[i] = i*(spacing) + "\t" + freq[i]; 
 		}
 		return freqOut;
 	}
 	
 	/**
 	 *Returns a tab delimited scatter plot of grid population vs. PSCF for each grid in this world.
 	 *Grids with no points get a "no-data" PSCF value -1.0
 	 *@param threshData A pair of correlated data containing the name of the pollutants in the same order as the correlated data of all points.
 	 */
 	public ArrayList<String> getPSCFHist(Pair[] threshData){
 		//format PER POLLUTANT: (NIJ, PSCF[0]....PSCF[polLength]), tab delimited
 		if(threshData == null || threshData.length == 0) return null;
 		ArrayList<String> lines = new ArrayList<String>();
 		String temp = "";
 		
 		//print a header:
 		String headerLine = "NIJ";
 		for(int i = 0; i < threshData.length; i++){
 			headerLine += ("\t" + threshData[i].name);
 		}
 		
 		lines.add(headerLine);
 		
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				temp = "" + (nHem[i][j].taggedPop());
 				for(int k = 0; k < threshData.length; k++){
 					if(PSCF[i][j] == null){
 						temp += "\t-1.000"; //NO DATA (CHANGE)
 					}else{
 						temp += "\t" + PSCF[i][j][k];
 					}
 				}
 				lines.add(temp); 			
 			}
 		}
 		return lines;
 	}
 	
 	 public String[] getTaggedHistSource(int intervals){
 		if(intervals <= 0) return null;
 		int[] freq = new int[intervals];
 		String[] freqOut = new String[intervals];
 		double spacing = Math.ceil((double)getMaxTaggedSource()/intervals);
		System.out.println("SPACING = " + spacing + " , Max Unique Source IDs = " + getMaxTaggedSource());
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				if(nHem[i][j].taggedPop() != 0){
 					freq[(int)(nHem[i][j].taggedUniqueID()/spacing)] += 1;
 				}
 			}
 		}
 		
 		for(int i = 0; i < freqOut.length; i++){
 			freqOut[i] = i*(spacing) + "\t" + freq[i]; 
 		}
 		return freqOut;
 	}
 	
 	public ArrayList<String> getPSCFSourceHist(Pair[] threshData){
 		//format PER POLLUTANT: (NIJ, PSCF[0]....PSCF[polLength]), tab delimited
 		if(threshData == null || threshData.length == 0) return null;
 		ArrayList<String> lines = new ArrayList<String>();
 		String temp = "";
 		
 		//print a header:
 		String headerLine = "SourceID";
 		for(int i = 0; i < threshData.length; i++){
 			headerLine += ("\t" + threshData[i].name);
 		}
 		
 		lines.add(headerLine);
 		
 		
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				temp = "" + (nHem[i][j].taggedUniqueID());
 				for(int k = 0; k < threshData.length; k++){
 					if(PSCF[i][j] == null){
 						temp += "\t-1.000"; //NO DATA (CHANGE)
 					}else{
 						temp += "\t" + PSCF[i][j][k];
 					}
 				}
 				lines.add(temp); 			
 			}
 		}
 		return lines;
 	}
 	
 	
 	/**
 	 *Returns a 2D matrix for the set of PSCF values
 	 *@param index the 3rd-dimensional index of the PSCF matrix
 	 *@param nDValue no data value. Must be less than 0.
 	 */
 	public ArrayList<String> getPSCFMatrix(int index, int ndValue){
 		if(PSCF == null || index < 0 || ndValue >= 0) return null;
 		ArrayList<String> lines = new ArrayList<String>();
 		
 		//print a header:
 		String header = "ncols " + nHem.length + "\n";
 		header += "nrows " + nHem[0].length + "\nxllcorner 479130.000000 \nyllcorner 4594080.000000 \ncellsize 33000 \nnodata_value " + dfL.format(ndValue);
 		
 		lines.add(header);
 		
 		String oneLine  = "";
 		int latIndex = Math.min(PSCF[0].length, (int)(90.0/this.latDY));
 		
 		for(int i = 0; i < Math.min(nHem[0].length,(int)(90.0/this.latDY)); i++){
 			oneLine = "";
 			for(int j = 0; j < nHem.length; j++){
 				if(PSCF[j][latIndex-i-1] == null){
 					oneLine += dfL.format(ndValue) + "\t";
 				}else{
 					oneLine += dfL.format(PSCF[j][latIndex-i-1][index]) + "\t";
 				}
 			}
 			lines.add(oneLine);
 		}
 		
 		if(this.totalLat > 90){
	 		for(int i = (int)(90.0/this.latDY); i < nHem[0].length; i++){
	 			oneLine = "";
	 			for(int j = 0; j < nHem.length; j++){
	 				if(PSCF[j][i] == null){
	 					oneLine += dfL.format(ndValue) + "\t";
	 				}else{
	 					oneLine += dfL.format(PSCF[j][i][index]) + "\t";
	 				}
	 			}
	 			lines.add(oneLine);
	 		}
 		}
 		
 		return lines;
 	}
 	
 	/**
 	 *Returns a 2D matrix of the CWT values, in the same way as a printout of the PSCF matrix
 	 *@param index the 3rd-dimensional index of the CWT matrix
 	 *@param ndValue no data value. Must be less than 0.
 	 */
 	 public ArrayList<String> getCWTMatrix(int index, int ndValue){
 	 	if(CWT == null || index < 0 || ndValue >= 0) return null;
 		ArrayList<String> lines = new ArrayList<String>();
 		
 		//print a header:
 		String header = "ncols " + nHem.length + "\n";
 		header += "nrows " + nHem[0].length + "\nxllcorner 479130.000000 \nyllcorner 4594080.000000 \ncellsize 33000 \nnodata_value " + dfL.format(ndValue);
 		
 		lines.add(header);
 		
 		String oneLine  = "";
 		int latIndex = Math.min(CWT[0].length, (int)(90.0/this.latDY));
 		
 		for(int i = 0; i < Math.min(nHem[0].length,(int)(90.0/this.latDY)); i++){
 			oneLine = "";
 			for(int j = 0; j < nHem.length; j++){
 				if(CWT[j][latIndex-i-1] == null){
 					oneLine += dfL.format(ndValue) + "\t";
 				}else{
 					oneLine += dfL.format(CWT[j][latIndex-i-1][index]) + "\t";
 				}
 			}
 			lines.add(oneLine);
 		}
 		
 		if(this.totalLat > 90){
	 		for(int i = (int)(90.0/this.latDY); i < nHem[0].length; i++){
	 			oneLine = "";
	 			for(int j = 0; j < nHem.length; j++){
	 				if(CWT[j][i] == null){
	 					oneLine += dfL.format(ndValue) + "\t";
	 				}else{
	 					oneLine += dfL.format(CWT[j][i][index]) + "\t";
	 				}
	 			}
	 			lines.add(oneLine);
	 		}
 		}
 		
 		return lines;
 	 }
 	 
 	/**
 	 *Returns a 2D matrix of the Finalized RTWC values, in the same way as a printout of the PSCF matrix
 	 *@param index the 3rd-dimensional index of the CWT matrix
 	 *@param ndValue no data value. Must be less than 0.
 	 */
 	 public ArrayList<String> getFinalCWTMatrix(int index, int ndValue){
 	 	if(finalCWT == null || index < 0 || ndValue >= 0) return null;
 		ArrayList<String> lines = new ArrayList<String>();
 		
 		//print a header:
 		String header = "ncols " + nHem.length + "\n";
 		header += "nrows " + nHem[0].length + "\nxllcorner 479130.000000 \nyllcorner 4594080.000000 \ncellsize 33000 \nnodata_value " + dfL.format(ndValue);
 		
 		lines.add(header);
 		
 		String oneLine  = "";
 		int latIndex = Math.min(finalCWT[0].length, (int)(90.0/this.latDY));
 		
 		for(int i = 0; i < Math.min(nHem[0].length,(int)(90.0/this.latDY)); i++){
 			oneLine = "";
 			for(int j = 0; j < nHem.length; j++){
 				if(finalCWT[j][latIndex-i-1] == null){
 					oneLine += dfL.format(ndValue) + "\t";
 				}else{
 					oneLine += dfL.format(finalCWT[j][latIndex-i-1][index]) + "\t";
 				}
 			}
 			lines.add(oneLine);
 		}
 		
 		if(this.totalLat > 90){
	 		for(int i = (int)(90.0/this.latDY); i < nHem[0].length; i++){
	 			oneLine = "";
	 			for(int j = 0; j < nHem.length; j++){
	 				if(finalCWT[j][i] == null){
	 					oneLine += dfL.format(ndValue) + "\t";
	 				}else{
	 					oneLine += dfL.format(finalCWT[j][i][index]) + "\t";
	 				}
	 			}
	 			lines.add(oneLine);
	 		}
 		}
 		
 		return lines;
 	 }
 	 
 	 
 	 
 	 public ArrayList<String> getQTBAMatrix(int index, int ndValue){
 	 	if(QTBA == null || index < 0 || ndValue >= 0) return null;
 		ArrayList<String> lines = new ArrayList<String>();
 		
 		//print a header:
 		String header = "ncols " + nHem.length + "\n";
 		header += "nrows " + nHem[0].length + "\nxllcorner 479130.000000 \nyllcorner 4594080.000000 \ncellsize 33000 \nnodata_value " + dfL.format(ndValue);
 		
 		lines.add(header);
 		
 		String oneLine  = "";
 		int latIndex = Math.min(QTBA[0].length, (int)(90.0/this.latDY));
 		
 		for(int i = 0; i < Math.min(nHem[0].length,(int)(90.0/this.latDY)); i++){
 			oneLine = "";
 			for(int j = 0; j < nHem.length; j++){
 				if(QTBA[j][latIndex-i-1] == null){
 					oneLine += dfL.format(ndValue) + "\t";
 				}else{
 					oneLine += dfL.format(QTBA[j][latIndex-i-1][index]) + "\t";
 				}
 			}
 			lines.add(oneLine);
 		}
 		
 		if(this.totalLat > 90){
	 		for(int i = (int)(90.0/this.latDY); i < nHem[0].length; i++){
	 			oneLine = "";
	 			for(int j = 0; j < nHem.length; j++){
	 				if(QTBA[j][i] == null){
	 					oneLine += dfL.format(ndValue) + "\t";
	 				}else{
	 					oneLine += dfL.format(QTBA[j][i][index]) + "\t";
	 				}
	 			}
	 			lines.add(oneLine);
	 		}
 		}
 		
 		return lines;
 	 }
 	
 	/**
 	 *Prints the population of aach grid in this World and its corresponding PSCF values, if applicable.
 	 */
 	public void printData(){
 		//REQUIRES that PSCF is not null!
		for(int i = 0; i < nHem.length; i++){
			for(int j = 0; j < nHem[i].length; j++){
				if(nHem[i][j].population() > 0){
					System.out.print("(" + (lonDX*i) + " , " + (latDY*j) + ") ; Population = " + nHem[i][j].population());
					if(PSCF[i][j] != null){
						 System.out.print("...Contains concentration data with first PSCF values: = ");
						 for(int k = 0; k < PSCF[i][j].length; k++){
						 	System.out.print("\t\n1: PSCF = " + PSCF[i][j][k]);
						 }
					}
					System.out.print("\n");
				}
			}
		}
 	}
 	
 	/**
 	 *Prints the population of grids which contain concentration data. 
 	 */
 	public void printTaggedPoints(){
 		for(int i = 0; i < nHem.length; i++){
 			for(int j = 0; j < nHem[i].length; j++){
 				if(nHem[i][j].population() > 0 && nHem[i][j].mij != null){
 					System.out.print("(" + (lonDX*i) + " , " + (latDY*j) + ") ; Population = " + nHem[i][j].population() + "....contains conc. data\n");
 				}else{
 					if(nHem[i][j].population() > 0)
 					System.out.print("(" + (lonDX*i) + " , " + (latDY*j) + ") ; Population = " + nHem[i][j].population() + "\n");
 				}
 			}
 		}
 	}
 	
 	/**
 	 *Prints the state of this world. Basic Information Only!
 	 */
 	 public void printState(){
 	 	System.out.println("WORLD: dX = " + this.lonDX + " , dY = " + this.latDY);
 	 	System.out.println("Population: " + this.hemPopulation());
 	 	System.out.println("Grid Array Dimensions: " + nHem.length + " by " + nHem[0].length);
 	 }
 	 
 	 /**
 	  *Prints CWT values of tagged grids
 	  *purely for testing purposes
 	  */
 	 public void printCWTMatrix(int index){
 	 	for(int i = 0; i < nHem.length; i++){
 	 		for(int j = 0; j < nHem[i].length; j++){
 	 			if(CWT[i][j] != null){
 	 				System.out.println(nHem[i][j].gridLat() + "," + nHem[i][j].gridLon() + "," + CWT[i][j][index]);
 	 			}
 	 		}
 	 	}
 	 }
 	 
 	
 	/**
 	  *Prints QTBA values of tagged grids
 	  *purely for testing purposes
 	  */
 	 public void printQTBAMatrix(int index){
 	 	for(int i = 0; i < nHem.length; i++){
 	 		for(int j = 0; j < nHem[i].length; j++){
 	 			if(QTBA[i][j] != null){
 	 				System.out.println(nHem[i][j].gridLat() + "," + nHem[i][j].gridLon() + "," + QTBA[i][j][index]);
 	 			}
 	 		}
 	 	}
 	 }
 	 
 	 
 	  	 /**
 	  *Prints CWT values of tagged grids
 	  *purely for testing purposes
 	  */
 	 public void printRTWCMatrix(int index){
 	 	for(int i = 0; i < nHem.length; i++){
 	 		for(int j = 0; j < nHem[i].length; j++){
 	 			if(finalCWT[i][j] != null){
 	 				System.out.println(nHem[i][j].gridLat() + "," + nHem[i][j].gridLon() + "," + finalCWT[i][j][index]);
 	 			}
 	 		}
 	 	}
 	 }
 	
 	
 	//TESTING ONLY
 	/**
 	 *Testing method.
 	 */
 	public static void main(String[] args) throws Exception{
 		/*Pair [] data = new Pair[10];
 		Pair [] threshData = new Pair[10];
 		
  		for(int i = 0; i < data.length; i++){ 
  			data[i] = new Pair("Pollutant" + (char)(65+i), 0.345);
  			threshData[i] = new Pair("Pollutant" + (char)(65+i), 0.2343);
  		}
 		
 		World x = new World(360,90,0.5,0.5);
 		
 		for(int i = 0; i < 240000; i++){
 			if(i < 60000){ 
 				x.addPointToWorld(new Point(224.9,61.3, "BUBBLES"));
 			}else{ 
 				x.addPointToWorld(new Point(224.9,61.3, "BUBBLES2"));
 			}
 		}
 		
 		System.out.println("Dimensions: " + x.lonDX + " by " + x.latDY + ", total population = " + x.hemPopulation());
		x.printData();
 		x.tagWorld("BUBBLES", data);
 		x.tagWorld("BUBBLES2", threshData);
 		x.calcPSCF(data);
 		x.printData();*/
 		
 		String [] polList = {"Pollutant A", "Pollutant B"};
  		Pair data[] = new Pair[2];
  		data[0] = new Pair("Pollutant A", 2.41);
  		data[1] = new Pair("Pollutant B", 2.39);
  		
  		World x = new World(360,90,1,1);
  		x.addPointToWorld(new Point(1.5, 1.5, "x", ""));
  		x.addPointToWorld(new Point(1.5, 1.5, "x", ""));
  		x.addPointToWorld(new Point(1.5, 1.5, "y", ""));
  		x.addPointToWorld(new Point(1.5, 1.5, "z", ""));
  		x.addPointToWorld(new Point(1.5, 1.5, "z", ""));
  		x.addPointToWorld(new Point(1.5, 1.5, "m", ""));
  		x.addPointToWorld(new Point(2, 4, "x", ""));
  		x.addPointToWorld(new Point(2, 4, "x", ""));
  		x.addPointToWorld(new Point(2, 4, "y", ""));
  		x.addPointToWorld(new Point(2, 4, "z", ""));
  		x.addPointToWorld(new Point(2, 4, "z", ""));
  		x.addPointToWorld(new Point(2, 4, "m", ""));
  		
  		data[0] = new Pair("Pollutant A", 2.41);
  		data[1] = new Pair("Pollutant B", 2.39);
  		//x.tagWorld("x", data);
  		
  		data[0] = new Pair("Pollutant A", 1.50);
  		data[1] = new Pair("Pollutant B", 3.14);
  		//x.tagWorld("y", data);
  		
  		data[0] = new Pair("Pollutant A", 1.59);
  		data[1] = new Pair("Pollutant B", 3.14);
  	//	x.tagWorld("z", data);
  		
  		//before smoothing
  		x.calcCWT(polList);
  		x.printCWTMatrix(0);
  		x.printCWTMatrix(1);
  	
  		//after smoothing
  		System.out.println("SMOOTHED DATA ----------");
  		
  		x.smoothCWTField(polList, -1.00, 11, 4, 0.95, false);
  		x.printCWTMatrix(0);
  		x.printCWTMatrix(1);
 	}
 		
 }