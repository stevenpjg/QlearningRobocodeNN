package currentrl; //change the package name as required
import java.io.File;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class NN {
	
	public static double NNtrain(double[][] Xtrain, double[] Ytrain,double[][] w_hx,double[][] w_yh,boolean switch1) {
		 
		      //variable declaration:
		      int no_h=19; //no. of hidden units
		      int r_b=no_h; //w_hx.length;
			  int c_b=6;// no. of inputs to neural network(4states+1action) + 1 (bias)=6;
		      double rho=0.00001;
		      double alpha=0.9;
		      int n_x=1;//Xtrain.length;
		      int d_x=Xtrain[0].length;
		      int n_y=Ytrain.length;
		      int d_y=1;//Ytrain[0].length;
	    	  //max and min for random number
		      double max=0.5;
		      double min=-0.5;
		      
		      
		      
		      double[][] prev_delw_y=new double[d_y][no_h+1];
		      double[][] prev_delw_h=new double[no_h][d_x];
		      double beta_2=0;
		      double curr_delw_y=0;
		      double[] tj=new double[no_h+1];
		      ArrayList<Double> error = new ArrayList<Double>();
		      
		   
		      for (int i=0;i < no_h; i++){
		    	  for (int j=0;j<d_x;j++){
		    		  prev_delw_h[i][j]=0;
		    	  }
		      }
		      
		      for (int i=0;i < d_y; i++){
		    	  for (int j=0;j<no_h+1;j++){
		    		  prev_delw_y[i][j]=0;
		    	  }
		      }
		      
		      //forward propogation:
		      double[] h=new double[no_h+1];
		      for (int i=0;i < no_h+1; i++){
		    	  
		    		  h[i]=0;
		    	  
		      }
		      //hardcoding bias term to 1:
		      h[no_h]=1;
		      double[] y_hat=new double[n_y];
		      for (int i=0;i < n_y; i++){
		    	  
		    		  y_hat[i]=0;
		    	 
		      }
		      int z=1;
		      
		      
		      MatrixMultiplication matrix=new MatrixMultiplication();
		      double[][] multiplier = new double[][] {
	                {2, -1, 1}
	        };
	        double[][] multiplier1 = new double[][] {
                {3},
                {2},
                {1}
	        };
                
                double[][] cc=matrix.Multiply(multiplier, multiplier1);
                
                double nn_qvalue=0;
                
                if(switch1==false){//return qvalue
                	 for (int i=0;i<no_h;i++)
		    		  {
		    			  
		    			  double s=0;
		    			  for(int j=0;j<d_x;j++){
		    				  s=s+w_hx[i][j]*Xtrain[0][j];
		    			  }
		    			  h[i]=matrix.sig(s);
		    		  }
		    		  
		    		  for(int i=0;i<d_y;i++)
		    		  {
		    			double s1=0;
		    			for(int j=0;j<no_h+1;j++)
		    			{
		    				s1=s1+w_yh[i][j]*h[j];
		    			}
		    			nn_qvalue = s1;
		    			
		    		  }
                	
		    		 
                	
                }
                else{//if true train weights
                	
                	  for (int i=0;i<no_h;i++)
		    		  {
		    			  
		    			  double s=0;
		    			  for(int j=0;j<d_x;j++){
		    				  s=s+w_hx[i][j]*Xtrain[0][j];
		    			  }
		    			  h[i]=matrix.sig(s);
		    		  }
		    		  
		    		  for(int i=0;i<d_y;i++)
		    		  {
		    			double s1=0;
		    			for(int j=0;j<no_h+1;j++)
		    			{
		    				s1=s1+w_yh[i][j]*h[j];
		    			}
		    			y_hat[0]=s1;
		    			
		    		  }
		    		  
		    		  for(int i=0;i<d_y;i++)
		    		  {
		    			double s1=0;
		    			for(int j=0;j<no_h+1;j++){
		    				s1=s1+w_yh[i][j]+h[j];
		    			}
		    		  }
		    		  
		    		  beta_2=(Ytrain[0]-y_hat[0]);
		    		  for (int i=0;i<w_yh[0].length;i++){
		    			 curr_delw_y=beta_2*h[i];
		    			 w_yh[0][i]=w_yh[0][i]+rho*curr_delw_y+alpha*prev_delw_y[0][i];
		    			 prev_delw_y[0][i]=rho*curr_delw_y+alpha*prev_delw_y[0][i];
		    			 
		    		  }
		    		  for (int i=0;i<no_h;i++){
		    			  double s=0;
		    			  for (int j=0;j<d_x;j++){
		    				  s=s+w_hx[i][j]*Xtrain[0][j];
		    			  }
		    			  tj[i]=s;
		    		  }
		    		  
		    		  
		    		  
		    		  double[][] delw_h=new double[r_b][c_b];
		    		  
		    		  for (int i=0;i<r_b;i++){
		    			  for (int j=0;j<c_b;j++){
		    				  delw_h[i][j]=beta_2*matrix.sigbi(tj[i])*(1-matrix.sigbi(tj[i]))*Xtrain[0][j];
		    				  w_hx[i][j]=w_hx[i][j]+rho*delw_h[i][j]+alpha*prev_delw_h[i][j];
		    				  prev_delw_h[i][j]=rho*delw_h[i][j]+alpha*prev_delw_h[i][j];
		    			  }
		    		  }
				      
				    
                }
				return nn_qvalue;
           	
		      
	}//public static void main
	
}//main class
