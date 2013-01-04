/*triplet.java
 *Ankit Rastogi
 *Started: May 20, 2010
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