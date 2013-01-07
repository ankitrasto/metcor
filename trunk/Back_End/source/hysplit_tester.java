/**
 * @(#)hysplit_tester.java
 *
 *
 * @author
 * @version 1.00 2012/1/13
 */

public class hysplit_tester {

    public static void main(String[]args) throws Exception{
		dnimp.CMCRender tester = new dnimp.CMCRender(360,90,5,5,"hs_testing", "dummy.txt", "hs_out");

		System.out.print("Reading End-points from Files:");
		tester.readHSEP(2000, null);
		System.out.print("....Done");

		tester.readConc(1, 0, null);

		tester.calcPSCF(4, 1, null, false);

		System.out.print("Generated Native Freq. vs. NIJ Histogram data...");
		tester.histNIJ(100);
		System.out.println("Done\n");

		System.out.print("Generating Tagged Freq. vs. NIJ histogram data....");
		tester.histTaggedNIJ(100);
		System.out.println("Done\n");

		System.out.print("Generating tagged Freq histogram of unique source IDs....");
		tester.histTaggedSource(100);
		System.out.println("Done\n");

		System.out.print("Generating PSCF vs. source ID scatter data....");
		tester.histPSCFSource();
		System.out.print("Done\n");


		System.out.print("Printing PSCF matrices...");
		tester.PSCF();
		System.out.print("...Done\n");


    }

}