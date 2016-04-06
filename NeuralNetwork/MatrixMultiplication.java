package currentrl;

public class MatrixMultiplication {

	  public static double[][] Multiply(double[][] m1, double[][] m2) {
	        int m1ColLength = m1[0].length; // m1 columns length
	        int m2RowLength = m2.length;    // m2 rows length
	        if(m1ColLength != m2RowLength) return null; // matrix multiplication is not possible
	        int mRRowLength = m1.length;    // m result rows length
	        int mRColLength = m2[0].length; // m result columns length
	        double[][] mResult = new double[mRRowLength][mRColLength];
	        for(int i = 0; i < mRRowLength; i++) {         // rows from m1
	            for(int j = 0; j < mRColLength; j++) {     // columns from m2
	                for(int k = 0; k < m1ColLength; k++) { // columns from m1
	                    mResult[i][j] += m1[i][k] * m2[k][j];
	                }
	            }
	        }
	        return mResult;
	    }
	  
	  public double sig(double x){
		double ysig;
		
		  ysig=-1+(2/(1+Math.exp(-x)));
		
		  return ysig;
		  
		  
	  }
	  
	  public double sigbi(double x1){
			double ysigbi;
			
			  ysigbi=1/(1+Math.exp(-x1));
			
			  return ysigbi;
			  
			  
		  }
	  
	  
	
}
