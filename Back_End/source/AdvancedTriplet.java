/*triplet.java
 *Ankit Rastogi
 *Started: May 20, 2010
 **/

package dnimp;
import java.text.*;

/**
 *A modifiable convenience data structure holding two integers and one floating point variable. Used exclusively for specifying weighting factors
 *for the CWT/QTBA/RTWC functions. To be semantically useful, low should be less than or equal to high.
 *@author Ankit Rastogi
 */
public class AdvancedTriplet{
	/**
	 *"Low" Integer Value of this Triplet.
	 */
	public double low;
	
	/**
	 *"High" Integer Value of this Triplet.
	 */
	public double high;
	
	/**
	 *The floating point value associated with this Triplet.
	 */
	public double weight;
	
	/**
	 *Creates a new instance of a triplet structure with two integers and an associated weight.
	 */
	public AdvancedTriplet(double low, double high, double weight){
		this.low = low;
		this.high = high;
		this.weight = weight;
	}
}