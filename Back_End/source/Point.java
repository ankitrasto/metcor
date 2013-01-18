/*Point.java
 *Ankit Rastogi
 *Project DensityNorth
 *Started: May 5, 2010
 *Updated: -
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

/**
 *A modifiable representation of a DensityNorth Point object containing
 *geospatial information and any one-dimensional set of correlated data.
 *<p> A point describes an object with latitude, longitude and a set of one-dimensional data. Additional information
 *by which to indentify the point is required, especially for higher level tagging functions.
 *
 *@author Ankit Rastogi
 */
 
 public class Point{ 
 	
 	/**
 	 *point latitude (y-axis coordinate)
 	 */
 	private double latY;
 	
 	/**
 	 *point longitude (x-axis coordinate)
 	 */
 	private double lonX;
 	
 	/**
 	 *Additional variable that can be used to describe the point.
 	 */
 	private String sourceID;
 	
 	/**
 	 *Another Additional variable that can be used to describe the point.
 	 */
 	private String thirdDim;
 	
 	/**
 	 *1 dimensional set of correlated data represented as pairs.
 	 */
 	private Pair values[];
 	
 	/**
 	 *1-D set of original pollutants assigned to this grid. For memory optimization
 	 *the use of this array is only for RTWC/concentration redistribution methods 
 	 *and folows the same index as the Pair[] values;
 	 */
 	private double originalConc[];
 	
 	private boolean firstTime = true;
 	
 	/**
 	 *length mismatch exception: The size of the input data is either inadmissable or does not match the required size.
 	 */
 	private Exception lengthMismatchException = new Exception("The size of the input data is either inadmissable or does not match the required size.");
 	
 	/**
 	 *notFound exception: The name specified was not found in this set of correlated data.
 	 */
 	 private Exception notFoundException = new Exception("The name specified was not found in this set of correlated data.");
 	
 	
 	/**	
 	 *Create an instance of a Point with only latitude and longitude.
 	 *<p> REQUIRES: <code>auxLat, auxLon</code> are not null.
 	 */ 
 	public Point(double auxLon, double auxLat){
 		this.lonX = auxLon;
 		this.latY = auxLat;
 	}
 	
 	/**
 	 *Create an instance of a Point with latitude, longitude, an identifier and a known number of correlated data elements.
 	 *<p> REQUIRES: <code>auxSourceID != null </code>
 	 */
 	 public Point(double auxLon, double auxLat, String auxSourceID){
 	 	this.lonX = auxLon;
 	 	this.latY = auxLat;
 	 	this.sourceID = auxSourceID;
 	 }
 	 
 	 /**
 	  *Create an instance of a Point with latitude, longitude and two identifiers
 	  *<p> REQUIRES: <code>auxSourceID != null ^ auxThirdDim != null</code>
 	  */
 	  public Point(double auxLon, double auxLat, String auxSourceID, String auxThirdDim){
 	  	this.lonX = auxLon;
 	  	this.latY = auxLat;
 	  	this.sourceID = auxSourceID;
 	  	this.thirdDim = auxThirdDim;
 	  }
 	 
 	 
 	 //ACCESSORS
 	 
 	 /**
 	  *returns the latitude of this point
 	  */
 	 public double lat(){
 	 	return this.latY;
 	 }
 	 
 	 /**
 	  *returns the longitude of this point
 	  */
 	  public double lon(){
 	  	return this.lonX;
 	  }
 	  
 	  /**
 	   *returns the unique identifier specific to this point.
 	   */
 	  public String sourceID(){
 	  	return this.sourceID;
 	  }
 	  
 	  /**
 	   *returns the second identifier specific to this point
 	   */
 	   public String thirdDim(){
 	   		return this.thirdDim;
 	   }
 	  
 	  /**
 	   *finds the value corresponding to a name in the set of data correlated to this point.
 	   *Note that <code>name</code> is case specific
 	   */
 	  
 	  public double getValue(String name) throws Exception{
 	  	int i = 0;
 	  	while(this.hasData() && i < values.length){
 	  		if(name.equals(values[i].name)) return values[i].value;
 	  		i++;
 	  	}
 	  	throw notFoundException;
 	  }
 	  
 	  public double getOriginalValue(int index) throws Exception{
 	  	if(this.originalConc == null || index < 0 || index >= this.originalConc.length){
 	  		throw notFoundException;
 	  	}
 	  	return this.originalConc[index];
 	  }
 	   	  
 	  /**
 	   *adds an initial set of correlated data to this point. If there was correlated data before,
 	   *it is overwritten. Implementation Note: auxPair is copied into the point, there is no reference to it.
 	   *@exception this.lengthMismatchException Thrown if <code>auxPair</code> is empty or null
 	   */
 	  public void addData(Pair[] auxPair) throws Exception{
 	  	//System.out.println("ADDED DATA TO POINT: " + this.lat() + "," + this.lon() +
 	  	//"ID = " + this.sourceID() + "," + this.thirdDim() + ", conc = " + auxPair[0].value); //VERBOSE TESTING
 	  	if(auxPair == null || auxPair.length <= 0){
 	  		throw this.lengthMismatchException;
 	  	}else{
 	  		this.values = new Pair[auxPair.length];
 	  		if(this.firstTime){
 	  			this.originalConc = new double[auxPair.length];
 	  		}
 	  		for(int i = 0; i < values.length; i++){
 	  			values[i] = new Pair(auxPair[i].name, auxPair[i].value);
 	  			if(this.firstTime) originalConc[i] = auxPair[i].value;
 	  			values[i].threshold = auxPair[i].threshold;
 	  		}
 	  		
 	  		firstTime = false;
 	  	}
 	  }
 	  
 	  /**
 	   *For a point which has data, assigns a new value to an existing data variable.
 	   *@param auxIndex the index of the correlated variable to use (if multiple data variables are used)
 	   *@param auxNewValue the new value to assign 
 	   */
 	  public void setNewConc(int auxIndex, double auxNewValue){
 	  	if(values == null) return;
 	  	if(auxIndex >= values.length || auxIndex < 0) return;
 	  	values[auxIndex] = new Pair(values[auxIndex].name, auxNewValue);	
 	  }
 	  
 	  /**
 	   *Assigns a new (or overwrites the existing) identifier to this point.
 	   */
 	  public void setID(String auxSourceID){
 	  	this.sourceID = auxSourceID;
 	  }
 	  
 	  /**
 	   *An accessor which determines whether there exists correlated data in this point.
 	   */
 	  public boolean hasData(){
 	  	return (values != null);
 	  }
 	  
 	  public void printInfo(){
 	  	System.out.println("<--POINT: " + this.lonX + " , " + this.latY + ".-->");
 	  	System.out.println("SourceID: " + this.sourceID);
 	  	System.out.println("Correlated Data:");
 	  	System.out.println("\t Name\tValue");
 	  	if(values == null){
 	  		System.out.println("<----EMPTY POLLUTANT DATA---->");
 	  	}else{
 	  		for(int i = 0; i < values.length; i++){
 	  			System.out.println("\t" + values[i].name + "\t" + values[i].value);
 	  		}
 	  	}
 	  	System.out.println("------------------------------");
 	  }
 	  

 	  /**
 	   *TESTING ONLY.
 	   */
 	  public static void main(String []args){
 	  	Pair x[] = new Pair[3];
 	  	x[0] = new Pair("polA", 3.392);
 	  	x[1] = new Pair("polB", 5.493);
 	  	x[2] = new Pair("polC", 4.583);
 	  	
 	  	Point auxPt = new Point(224.3, 61.35, "Feb 10, 2010");
 	  	
 	  	auxPt.printInfo();
 	  	auxPt.setNewConc(0, 34);
 	  	
 	  	try{
 	  		auxPt.addData(x);
 	  		System.out.println("FINDING polA: " + auxPt.getValue("polA"));
 	  		System.out.println("FINDING polB: " + auxPt.getValue("polB"));
 	  		System.out.println("FINDING polB: " + auxPt.getValue("polC"));
 	  		System.out.println("FINDING polX: " + auxPt.getValue("polc")); 
 	  	}catch(Exception e){
 	  		System.out.println(e);
 	  	}
 	  	
 	  	auxPt.printInfo();
 	  	
 	  	Pair y[] = new Pair[4];
 	  	y[0] = new Pair("polA", 312313.392);
 	  	y[1] = new Pair("polB", 5123.493);
 	  	y[2] = new Pair("polC", 4123.583);
 	  	y[3] = new Pair("polD", 20.402);
 	  	
 	  	try{
 	  		auxPt.addData(y); 
 	  	}catch(Exception e){
 	  		System.out.println(e);
 	  	}
 	  	
 	  	
 	  	
 	  	auxPt.printInfo();
 	  	
 	  	auxPt.setNewConc(0, 3123);
 	  	auxPt.setNewConc(44, 32);
 	  	auxPt.setNewConc(-1, 3);
 	  	auxPt.setNewConc(3, -1.00);
 	  	
 	  	auxPt.printInfo();
 	  	
 	  }
 	  
 	
 }