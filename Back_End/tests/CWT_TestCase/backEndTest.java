/*Back-end master testing class for MetCor Implementation - CWT
 *Started: Dec. 28, 2012
 *Ankit Rastogi, metcor.googlecode.com
 **/
 
 // (int optionSelect, double convPercent, int maxIterations, 
 //int pointFilter, int polyDegree, double confInt, int ndValue)
 public class backEndTest{
 
 	public static void main(String[]args) throws Exception{
 		dnimp.CMCRender test = new dnimp.CMCRender(360,90,1,0.75, "inputHysplit", "CorrData_Testing_sQTBA_III.txt", "HSOP");
 		
 		System.out.print("Reading End-points from Files:");
			test.readHSEP(2000, null);
		System.out.print("....Done");	
		
		test.readConc(0, 0, null);
		
		
		dnimp.AdvancedTriplet[] testWeights = new dnimp.AdvancedTriplet[2];
		testWeights[0] = new dnimp.AdvancedTriplet(0.0,10.5,5);
		testWeights[1] = new dnimp.AdvancedTriplet(10.6, 20, 3.5);
		
		test.calcQTBA(5.4, null);
		test.QTBA();
		test.GridMetrics(2);
		
		//test.calcRTWC(1, 0.5, 3, 7,3,0.95,-1, null);
		
		//test.RTWC();
		
		//test.calcCWT(1, -1, 5,3,0.95, testWeights);
		
		//test.CWT();
		
		//	test.GridMetrics(1);
 	}
 
 }