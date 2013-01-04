/**
 *Pair data structure: holds a name and its corresponding value
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