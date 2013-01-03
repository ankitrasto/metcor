/*Back-end master testing class for MetCor Implementation
 *Started: Dec. 28, 2012
 *Ankit Rastogi, metcor.googlecode.com
 **/
 
 
 public class backEndTest{
 
 	public static void main(String[]args) throws Exception{
 		dnimp.CMCRender test = new dnimp.CMCRender(360,90,1,0.75, "inputHysplit", "CorrData_Testing_sQTBA_III.txt", "HSOP");
 		
 		System.out.print("Reading End-points from Files:");
			test.readHSEP(2000, null);
		System.out.print("....Done");	
	
		test.readConc(0, 0, null);
		test.calcQTBA(5.4);	
		test.QTBA();
		test.GridMetrics(2);
 	}
 
 }