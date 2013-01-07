/*Statistics.java: A repository of functions/methods for TSMs other than PSCF
 *Package: dnimp, Dependencies: sgfilter, commons math libraries
 *Ankit Rastogi
 *Started: August 22, 2012
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
 import mr.go.sgfilter.*;
 import org.apache.commons.math.special.*;
 
 
 /**
  *A statistics module for the MetCor implementation of different
  *trajectory statistics models. Functions include returning varying t-values,
  *signal smoothing and matrix to vector (+ vice versa) transformations. To be used
  *by CMCRender, World and Grid classes of the dnimp package. 
  *
  *The SG-filter implementation was developed by Marcin Rzeznicki and is taken
  *verbatim from http://code.google.com/p/savitzky-golay-filter/.
  *
  *@author Ankit Rastogi 
  *
  */
 public class Statistics{
 	
 	private double rawData[];
 	private double smoothData[];
 	
 	/** An empty constructor for the Statistics module. 
 	 */
 	public Statistics(){}
 	
 	private double studT(double t, int n){
 		t = Math.abs(t);
 		double th = Math.atan(t/Math.sqrt(n));
 		double sth = Math.sin(th);
 		double cth = Math.cos(th);
 		
 		if (n == 1) return 1-th/(Math.PI/2.0);
 		
 		if(n%2 == 1){
 			return 1 - (th+sth*cth*statCom(cth*cth, 2, n-3, -1))/(Math.PI/2.0);
 		}else{
 			return 1-sth*statCom(cth*cth,1,n-3,-1);
 		}
 	}
 	
 	private double statCom(double q, double i, int j, int b){
 		double zz = 1;
 		double z = zz;
 		double k = i;
 		while(k <= j){
 			zz = zz*q*k/(k-b);
 			z = zz + z;
 			k += 2;
 		}
 		return z;
 	}
 	
 	
 	/**
 	 *The main smoothing algorithm used by MetCor.
 	 *@param rawData the raw data to be passed as a vector, to be mapped back to CWT-matrices by the invoking class
 	 *@param filterLength an odd-numbered length representing 2N+1 (the filter length), where N are the left and right
 	 *bounds used for data smoothing. Generally, the larger the filter, the greater the smoothing effect.
 	 *@param polyDegree the degree of the polynomial to fit into the data, should also be user controlled.
 	 *
 	 *REQUIRES: filterLength%2 = 1, i.e. filterLength is odd.
 	 *
 	 */
 	public double[] smoothData(double[] rawData, int filterLength, int polyDegree){
		int nLR = 0;
		if(filterLength%2 == 1){ nLR = (filterLength-1)/2; }else{ nLR = filterLength/2; }
		
		SGFilter sgF = new SGFilter(nLR,nLR);
		double[] sgCoeff = SGFilter.computeSGCoefficients(nLR, nLR, polyDegree);
		sgF.appendPreprocessor(new MeanValuePadder(2*nLR, true, true));
		
		return sgF.smooth(rawData, sgCoeff);
 	}

 	/**
 	 *Computes a given t-value for a specific probability (p) and degrees of freedom (n).
 	 *Note that p represents a two-tailed distribution area. Conversion to a one-tailed distribution
 	 *area is left to the user's intepretation
 	 *@param p the two tailed distribution area: 1 - CI, where CI is the confidence interval.
 	 *@param n degrees of freedom
 	 *
 	 */
 	public double AStudT(double p, int n){
 		double v = 0.5;
 		double dv = 0.5;
 		double t = 0;
 		while(dv > 1E-6){
 			t = 1/v-1; //1/v-1 verbatim, brackets required?
 			dv = dv/2.0;
 			if(this.studT(t,n) > p){
 				v = v - dv;
 			}else{
 				v += dv;
 			}
 		}
 		
 		return t;
 	}
 	
 	/**
 	 *Returns the linear distance between 2 geographical coordinates; for sQTBA
 	 *@param x Latitude
 	 *@param y Longitude
 	 *@param xR receptor site latitude
 	 *@param yR receptor site longitude
 	 */
 	public double haversineV(double x, double y, double xR, double yR){
 		double re = 6378.1369; //equatorial earth's radius
 		double pi360 = Math.PI/360.0;
 		double pi180 = Math.PI/180.0;
 		
 		double term1 = Math.sin(pi360*(x - xR));
 		double term2 = Math.cos(pi180*xR);
 		double term3 = Math.cos(pi180*x);
 		double term4 = Math.sin(pi360*(y-yR));
 		double sqrtauxV = 2*re*Math.asin(Math.sqrt(Math.pow(term1,2.0) + (term2*term3*Math.pow(term4, 2.0))));
 		
 		return sqrtauxV;
 	}
 	
 	/**
 	 *Returns the natural transport potential function for a given back trajectory.
 	 *@param T temporal distance from receptor site
 	 *@param sqrtV spatial distance away from receptor site, see this.haversineV(...)
 	 *@param a	atmospheric dispersion factor
 	 */
 	 public double naturalTransPot(double Taux, double sqrtV, double a) throws Exception{
 	 	double T = Math.abs(Taux);
 	 	if(T == 0) return 1000000000;
 	 	double term1 = 1.0/((2*a*T)*Math.sqrt(2*Math.PI)*sqrtV);
 	 	double erfcParam = (sqrtV/(T*a*Math.sqrt(2)));
 	 	return term1*(1.0-this.erf(erfcParam));
 	 }
 	 
 	 
 	 //Alternative implementation for the ERF:
 	  private double erf(double z) {
        
        if(z == 0) return 0.0;
        
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

        // use Horner's method
        double ans = 1 - t * Math.exp( -z*z   -   1.26551223 +
                                            t * ( 1.00002368 +
                                            t * ( 0.37409196 + 
                                            t * ( 0.09678418 + 
                                            t * (-0.18628806 + 
                                            t * ( 0.27886807 + 
                                            t * (-1.13520398 + 
                                            t * ( 1.48851587 + 
                                            t * (-0.82215223 + 
                                            t * ( 0.17087277))))))))));
        if (z >= 0) return  ans;
        else        return -ans;
 	  }
 	  
 	
 	/**
 	 *Testing only.
 	 */
 	 
 	public static void main(String[] Args) throws Exception{
 		Statistics statTest = new Statistics();
 		/*DecimalFormat df = new DecimalFormat("0.000");
 		
 		for(int n = 1; n < 120; n++){
 			System.out.println(n + "DF, 0.05 = " + df.format(statTest.AStudT(0.05, n)));
 		}
 		
 		for(int n = 1; n < 120; n++){
 			System.out.println(n + "DF, 0.10 = " + df.format(statTest.AStudT(0.10, n)));
 		}*/
 		
 		double data[] = new double[]{4.978490485030721, 4.05673060356413, 5.941319246987078, 5.135073542221775, 2.3821760757477803, 4.9665775727267425, 4.099392527861313, 5.112018750129706, 6.1237568467180115, 5.514909013146264, 3.6836372699618005, 6.418886002854732, 5.068643971666669, 6.665624647202592, 4.885241222549593, 6.391504742331859, 4.725848210742279, 4.124877762584695, 5.415932410597626, 5.780109353846543, 5.951478455582691, 3.6346290082067414, 5.101851874068355, 5.676516955217815, 5.157264183867851, 4.543521101268323, 3.9005734796210723, 4.56955196144615, 6.765863836667339, 4.879630612612463, 5.531754726170732, 5.054745577976829, 4.0515713149527475, 4.689488030303826, 5.278468698124456, 6.5202918591139, 4.520664012051461, 4.682189004825869, 4.569746344071519, 5.111975567298497, 5.359019063689495, 5.439199127537561, 5.2967440872459175, 7.215551599893209, 5.115138527208011, 3.2417867767123094, 3.851638134454772, 4.716478082443418, 5.930270972970176, 5.104329808993688, 3.8405889454400857, 4.791644068589577, 3.704789507455379, 4.37967023337454, 5.220638537699736, 5.298510263871911, 6.08748441033322, 5.939887814749121, 4.950159895771174, 4.5097825640062545, 4.312221680711184, 5.054449728823288, 5.448361837947443, 4.055926400034352, 4.332194283488786, 5.578482775129309, 4.083500827740135, 4.919983295886383, 4.311108840458667, 3.8088614752290026, 5.492708622771914, 3.763264663989375, 5.902909023869145, 4.344170665722308, 3.6347625011916165, 5.695233515389685, 6.538556909522802, 5.3596335221279015, 5.541757990420989, 5.001368246099333, 4.0313188013430885, 6.621042034393342, 4.002511307579848, 5.391980139171496, 3.9094013907272003, 4.2683445193435965, 5.192742774460883, 5.87326745446328, 3.6868396855343617, 5.622740416311509, 6.172362498027332, 4.897070726771359, 5.6909721816385215, 5.7905095886712985, 4.209125772107139, 4.385931003284767, 5.669393267970842, 5.912365914141542, 5.6811667726764625, 5.278211662399071, 5.16349792855808, 5.662158780318315, 4.711117858871507, 6.9306425501832525, 5.433403924759384, 3.7886892048258245, 5.137775796526945, 5.880773390302156, 6.715594274195913, 4.804286111163124, 5.680297702072726, 6.204795245772571, 5.458314348745064, 6.015605277983744, 3.9220788424165516, 6.233927683096608, 3.5025736586625116, 5.602000383230644, 6.330410303214216, 4.7473855974264465};
 		double sgData[] = statTest.smoothData(data, 11, 4);
 		
 		System.out.println("Smoothed Data:");
 		
 		for(int i = 0; i < sgData.length; i++){
 			System.out.print(sgData[i] + ", ");
 		}

 		//double v = statTest.haversineV(44.499,-79.614, 45.434, -75.676);
 		double v = statTest.haversineV(45.434,-75.676, 45.434, -75.676);
 		System.out.println("\n v = " + v);
 		System.out.println(statTest.naturalTransPot(0, v, 5.4));
 		
 		
 		/*double[][] x = new double[10][];
 		for(int i = 0; i < x.length; i+=2){
 			x[i] = new double[2];
 			x[i][0] = -1.0;
 			x[i][1] = i+1;
 		}
 		System.out.println("\n");
 		
 		for(int i = 0; i < x.length; i++){
 			for(int j = 0; j < 2; j++){
 				if(x[i] != null && x[i][j] != -1.0){
 					System.out.println("(" + i + ", " + j + ") = " + x[i][j]);
 				}
 			}
 		}*/
 				
 	}
 		
 		
 		
 	
 }
