/**
 *Pair data structure: holds a name and its corresponding value
 */
 package dnimp;
public class Pair{
 	String name;
 	public double value;
 	/**
 	 *specifies whether the value exceeds a predefined threshold. Default value is false.
 	 */
 	public boolean threshold;
 	/**
 	 *Basic constructor. <code>threshold</code> is set as false by default
 	 *@param auxName pair element name
 	 *@param val pair element value
 	 */
 	public Pair(String auxName, double val){
 		this.value = val;
 		this.name = auxName;	
 		threshold = false;	
 	}
 }