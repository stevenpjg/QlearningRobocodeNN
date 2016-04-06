package currentrl; //change the package name as required

import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.Color;

import java.io.BufferedReader;

import java.io.FileReader;

import java.io.IOException;

import java.io.PrintStream;

import com.sun.javafx.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;

import robocode.RobocodeFileOutputStream;
import robocode.RobotStatus;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import java.util.Random;
import robocode.control.RobotSetup;
public class Rl_nn extends AdvancedRobot {
	final double alpha = 0.1;
    final double gamma = 0.9;
    double distance=0;
    double rl_x=0;
    double rl_y=0;
    double rl_x_q=0;
    //declaring states
    int[] your_x=new int[8];
    int[] your_y=new int[6];
    int[] distance_to_enemy=new int[4];
    int[] gear_angle=new int[4];
    //declaring actions
    int[] action=new int[4];
    //LUT table initialization
    int[] total_states_actions=new int[8*6*4*4*action.length];
    int[] total_actions=new int[4];
    String[][] LUT=new String[total_states_actions.length][2];
    String[][] CUM=new String[10][2];
    double[][] LUT_double=new double[total_states_actions.length][2];
    //quantized parameters
    double qrl_x=0;
    double qrl_y=0;
    double qenemy_x=0;
    double qenemy_y=0;
    private RobotStatus robotStatus;
    double qdistancetoenemy=0;
    //-------------Explore or greedy----------------------//
    boolean explore=false; //set this true while training
    boolean greedy=true;
    //----------------------------------------------------//
    double absbearing=0;
    double q_absbearing=0;
    //initialize reward
    double reward=0;
    String state_action_combi=null;
    String state_action_combi_greedy=null;
    double robot_energy=0;
    int sa_combi_inLUT=0;
    
    String q_present=null;
    double q_present_double=0;
    int random_action=0;
    String state_action_combi_next=null;
	int sa_combi_inLUT_next=0;
	String q_next=null;
	double q_next_double=0;
	int count=0;
	int Qmax_action=0;
	int[] actions_indices=new int[total_actions.length];
	double[] q_possible=new double[total_actions.length];
	int Qmax_actual_action=0;
	double enemy_energy=0;
	double reward1=0;
	 double my_energy_pres=0;
	 double enemy_energy_pres=0;
	 double my_energy_next=0;
	 double enemy_energy_next=0;
	 double gunTurnAmt;
	 double bearing;
	 int rlaction;
	 int store_action;
	private double getHeadingRadians;
	private double getVelocity;
	private double absBearing;
	private double getBearing;
	private double getTime;
	private double normalizeBearing;
	
	int count_battles;
	double cum_reward;
	double cum_reward_while=0;
	static double[] cum_reward_array=new double[1000];
	double cum_reward_hun=0; 
	static int index1=0;
	public int getRoundNum;
	//nn
	double[][] Xtrain=new double[1][6];
	double[][] Xtrain_next=new double[1][6];
	double[] Ytrain=new double[1];
	static int iter=0;
	double dummy=0;
	
	double ypredict = 0;
	static double[][] w_hx=new double[19][6];
	static double[][] w_yh=new double[1][19+1];	
	String[][] w_hxs=new String[19][6];
	String[][] w_yhs=new String[1][19+1];
	
	
	//methods
	NN NN_obj=new NN(); //Neural Network Function
	Weights weights_obj=new Weights();
	//
public void run(){
	setColors(null, Color.PINK, Color.PINK, new Color(255,165,0,100), new Color(150, 0, 150));
    setBodyColor(Color.PINK);
    while(true){
    	if(explore){ //Explore event--------------------------------------------------//
     		
    if(iter==0){
    	
    	
    	try {
			load2();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		//load command
    	try {
			load22();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
    	//the loaded variable is in string converting it into double
    	for(int i=0;i<19;i++){
    		for(int j=0;j<6;j++){		
    	w_hx[i][j]= Double.valueOf(w_hxs[i][j]).doubleValue();
    		}
    	}
    	for(int i=0;i<1;i++){
    		for(int j=0;j<20;j++){		
    	w_yh[i][j]= Double.valueOf(w_yhs[i][j]).doubleValue();
    		}
    	}
    	
    	iter=iter+1;
    	
    }		
    turnGunRight(360);
	random_action=randInt(1,total_actions.length);
	state_action_combi=qrl_x+""+qrl_y+""+qdistancetoenemy+""+q_absbearing+""+random_action;
	Xtrain[0][0]=qrl_x;
	Xtrain[0][1]=qrl_y;
	Xtrain[0][2]=qdistancetoenemy;
	Xtrain[0][3]=q_absbearing;
	Xtrain[0][4]=random_action;
	Xtrain[0][5]=1;
	
	q_present_double=NN.NNtrain(Xtrain, Ytrain, w_hx, w_yh, false);
	//NN.NNtrain(Xtrain, Ytrain, w_hx, w_yh,true);
	
		System.out.println(w_hx[0][0]);
	
	
	 reward=0;
	 //performing next state and scanning
			 
	 rl_action(random_action);
	 
	 turnGunRight(360);
	 
	 Xtrain_next[0][0]=qrl_x;
		Xtrain_next[0][1]=qrl_y;
		Xtrain_next[0][2]=qdistancetoenemy;
		Xtrain_next[0][3]=q_absbearing;
		Xtrain_next[0][4]=random_action;
		Xtrain_next[0][5]=1;
	
	
		 q_next_double=NN.NNtrain(Xtrain_next, Ytrain, w_hx, w_yh, false);
	 
		
	 //performing update
	 q_present_double=q_present_double+alpha*(reward+gamma*q_next_double-q_present_double);
	 Ytrain[0]=q_present_double;
	 dummy=NN.NNtrain(Xtrain, Ytrain, w_hx, w_yh,true);
	 cum_reward_while+=reward;
	 save2();
	 save22();
	 
	
}//explore loop ends
    	
//Greedy Moves//    	
    	
    	if(greedy){

    
    		if(iter==0){
    	    	
    	    	
    	    	try {
    				load2();
    			} 
    			catch (IOException e) {
    				e.printStackTrace();
    			}
    			//load command
    	    	try {
    				load22();
    			} 
    			catch (IOException e) {
    				e.printStackTrace();
    			}
    	    	//the loaded variable is in string converting it into double
    	    	for(int i=0;i<19;i++){
    	    		for(int j=0;j<6;j++){		
    	    	w_hx[i][j]= Double.valueOf(w_hxs[i][j]).doubleValue();
    	    		}
    	    	}
    	    	for(int i=0;i<1;i++){
    	    		for(int j=0;j<20;j++){		
    	    	w_yh[i][j]= Double.valueOf(w_yhs[i][j]).doubleValue();
    	    		}
    	    	}
    	    	
    	    	iter=iter+1;
    	    	
    	    }		
     	
    //predict current state:
	turnGunRight(360);
	
	// finding action that produces maximum Q value
	
	
	for(int j=1;j<=total_actions.length;j++)
	{
		Xtrain[0][0]=qrl_x;
		Xtrain[0][1]=qrl_y;
		Xtrain[0][2]=qdistancetoenemy;
		Xtrain[0][3]=q_absbearing;
		Xtrain[0][4]=j;
		Xtrain[0][5]=1;
		q_possible[j-1]=NN.NNtrain(Xtrain, Ytrain, w_hx, w_yh, false);
	}
	//converting table to double
	for(int i=0;i<4;i++){
		System.out.println(q_possible[i]);
	}
	
	Qmax_action=getMax(q_possible)+1;
	int jj=0;
	
	Xtrain[0][0]=qrl_x;
	Xtrain[0][1]=qrl_y;
	Xtrain[0][2]=qdistancetoenemy;
	Xtrain[0][3]=q_absbearing;
	Xtrain[0][4]=Qmax_action;
	Xtrain[0][5]=1;
	
	 q_present_double=NN.NNtrain(Xtrain, Ytrain, w_hx, w_yh, false);
	 reward=0;
	 //performing next state and scanning
	
	 
	 rl_action(Qmax_action);
	 
	 
	 turnGunRight(360);
	 
	 Xtrain_next[0][0]=qrl_x;
		Xtrain_next[0][1]=qrl_y;
		Xtrain_next[0][2]=qdistancetoenemy;
		Xtrain_next[0][3]=q_absbearing;
		Xtrain_next[0][4]=random_action;
		Xtrain_next[0][5]=Qmax_action;;
		
		 q_next_double=NN.NNtrain(Xtrain_next, Ytrain, w_hx, w_yh, false);
	 
		
	 //performing update
	 q_present_double=q_present_double+alpha*(reward+gamma*q_next_double-q_present_double);
	 Ytrain[0]=q_present_double;
	 dummy=NN.NNtrain(Xtrain, Ytrain, w_hx, w_yh,true);
	 cum_reward_while+=reward;
	 
}//greedy loop ends
    		
          
    }//while loop ends
    
    
    
    }//run function ends

   
//function definitions:
public void onScannedRobot(ScannedRobotEvent e)
	{
	double absBearing=e.getBearingRadians()+getHeadingRadians();
	this.absBearing=absBearing;
	double getVelocity=e.getVelocity();
	double getHeadingRadians=e.getHeadingRadians();
	this.getHeadingRadians=getHeadingRadians;
	this.getVelocity=getVelocity;
	
	double getBearing=e.getBearing();
	this.getBearing=getBearing;
	double getTime=getTime();
	this.getTime=getTime;
	gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
	this.gunTurnAmt=gunTurnAmt;
	
	double normalizeBearing=normalizeBearing(getBearing + 90 - (15 * 1));
	this.normalizeBearing=normalizeBearing;
	robot_energy=getEnergy();
	enemy_energy=e.getEnergy();
	distance = e.getDistance(); //distance to the enemy
	qdistancetoenemy=quantize_distance(distance); //distance to enemy state number 3
	
	//fire
	if(qdistancetoenemy<=2.50){fire(3);
	
	}
	if(qdistancetoenemy>2.50&&qdistancetoenemy<5.00){fire(3);}
	if(qdistancetoenemy>5.00&&qdistancetoenemy<7.50){fire(1);}
	//fire
	
	//your robot
	
	qrl_x=quantize_position(getX()); //your x position -state number 1
	qrl_y=quantize_position(getY()); //your y position -state number 2
	//Calculating Enemy X & Y:
	double angleToEnemy = e.getBearing();
	// Calculate the angle to the scanned robot
	double angle = Math.toRadians((getHeading() + angleToEnemy % 360));
	// Calculate the coordinates of the robot
	double enemyX = (getX() + Math.sin(angle) * e.getDistance());
	double enemyY = (getY() + Math.cos(angle) * e.getDistance());
	qenemy_x=quantize_position(enemyX);
	qenemy_y=quantize_position(enemyY);
	//distance to enemy
	//absolute angle to enemy
	absbearing=absoluteBearing((float) getX(),(float) getY(),(float) enemyX,(float) enemyY);
	q_absbearing=quantize_angle(absbearing); //state number 4
	
	}

public double normalizeBearing(double angle) {
	while (angle >  180) angle -= 360;
	while (angle < -180) angle += 360;
	return angle;
	
}




//reward functions:


public void onHitRobot(HitRobotEvent event){reward-=2;} //our robot hit by enemy robot
public void onBulletHit(BulletHitEvent event){reward+=3;} //one of our bullet hits enemy robot
public void onHitByBullet(HitByBulletEvent event){reward-=3;} //when our robot is hit by a bullet
//public void BulletMissedEvent(Bullet bullet){reward-=3;} 

private double quantize_angle(double absbearing2) {
	
	if((absbearing2 > 0) && (absbearing2<=90)){
		q_absbearing=1;
		}
	else if((absbearing2 > 90) && (absbearing2<=180)){
		q_absbearing=2;
		}
	else if((absbearing2 > 180) && (absbearing2<=270)){
		q_absbearing=3;
		}
	else if((absbearing2 > 270) && (absbearing2<=360)){
		q_absbearing=4;
		}
	return absbearing2/90;
}

private double quantize_distance(double distance2) {

	if((distance2 > 0) && (distance2<=250)){
		qdistancetoenemy=1;
		}
	else if((distance2 > 250) && (distance2<=500)){
		qdistancetoenemy=2;
		}
	else if((distance2 > 500) && (distance2<=750)){
		qdistancetoenemy=3;
		}
	else if((distance2 > 750) && (distance2<=1000)){
		qdistancetoenemy=4;
		}
	qdistancetoenemy=distance2/100;
	return qdistancetoenemy;
}

//absolute bearing
double absoluteBearing(float x1, float y1, float x2, float y2) {
	double xo = x2-x1;
	double yo = y2-y1;
	double hyp = Point2D.distance(x1, y1, x2, y2);
	double arcSin = Math.toDegrees(Math.asin(xo / hyp));
	double bearing = 0;

	if (xo > 0 && yo > 0) { // both pos: lower-Left
		bearing = arcSin;
	} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
		bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
	} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
		bearing = 180 - arcSin;
	} else if (xo < 0 && yo < 0) { // both neg: upper-right
		bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
	}

	return bearing;
}




private double quantize_position(double rl_x2) {
		// TODO Auto-generated method stub
		
	if((rl_x2 > 0) && (rl_x2<=100)){
		qrl_x=1;
		}
	else if((rl_x2 > 100) && (rl_x2<=200)){
		qrl_x=2;
		}
	else if((rl_x2 > 200) && (rl_x2<=300)){
		qrl_x=3;
		}
	else if((rl_x2 > 300) && (rl_x2<=400)){
		qrl_x=4;
		}
	else if((rl_x2 > 400) && (rl_x2<=500)){
		qrl_x=5;
		}
	else if((rl_x2 > 500) && (rl_x2<=600)){
		qrl_x=6;
		}
	else if((rl_x2 > 600) && (rl_x2<=700)){
		qrl_x=7;
		}
	else if((rl_x2 > 700) && (rl_x2<=800)){
		qrl_x=8;
		}
	return rl_x2/100;

	}

public void rl_action(int x)
			{
	switch(x){
		case 1:
			int moveDirection=+1;  //moves in anticlockwise direction
			if (getVelocity == 0)
				moveDirection *= 1;

			// circle our enemy
			setTurnRight(getBearing + 90);
			setAhead(150 * moveDirection);
			break;
		case 2:
			int moveDirection1=-1;  //moves in clockwise direction
			if (getVelocity == 0)
				moveDirection1 *= 1;

			// circle our enemy
			setTurnRight(getBearing + 90);
			setAhead(150 * moveDirection1);
			break;
		case 3:
			turnGunRight(gunTurnAmt); // Try changing these to setTurnGunRight,
			turnRight(getBearing-25); // and see how much Tracker improves...
			// (you'll have to make Tracker an AdvancedRobot)
			ahead(150);
			break;
		case 4:
			turnGunRight(gunTurnAmt); // Try changing these to setTurnGunRight,
			turnRight(getBearing-25); // and see how much Tracker improves...
			// (you'll have to make Tracker an AdvancedRobot)
			back(150);
			break;
			
	
		
	}
			}//rl_action()
//randomint
public static int randInt(int min, int max) {

    // Usually this can be a field rather than a method variable
    Random rand = new Random();

    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
}

//lut initialization

public void initialiseLUT() {
    int[] total_states_actions=new int[8*6*4*4*action.length];
    LUT=new String[total_states_actions.length][2];
    int z=0;
    for(int i=1;i<=8;i++){
    	for(int j=1;j<=6;j++){
    		for(int k=1;k<=4;k++){
    			for(int l=1;l<=4;l++){
    				for(int m=1;m<=action.length;m++){
    					LUT[z][0]=i+""+j+""+k+""+l+""+m;
    					LUT[z][1]="0";
    					z=z+1;
    				}
    			}
    		}
    	}
    }

   } //Initialize LUT

public void save() {

	PrintStream w = null;
	try {
		w = new PrintStream(new RobocodeFileOutputStream(getDataFile("LookUpTable.txt")));
		for (int i=0;i<LUT.length;i++) {
			w.println(LUT[i][0]+"    "+LUT[i][1]);
		}
	} catch (IOException e) {
		e.printStackTrace();
	}finally {
		w.flush();
		w.close();
	}

}//save
public void save2() {

	PrintStream w1 = null;
	try {
		w1 = new PrintStream(new RobocodeFileOutputStream(getDataFile("weights_hidden.txt")));
		for (int i=0;i<w_hx.length;i++) {
			w1.println(w_hx[i][0]+"    "+w_hx[i][1]+"    "+w_hx[i][2]+"    "+w_hx[i][3]+"    "+w_hx[i][4]+"    "+w_hx[i][5]);
		}
	} catch (IOException e) {
		e.printStackTrace();
	}finally {
		w1.flush();
		w1.close();
	}

}//save

public void save22() {

	PrintStream w2 = null;
	try {
		w2 = new PrintStream(new RobocodeFileOutputStream(getDataFile("weights_output.txt")));
		for (int i=0;i<1;i++) {
			w2.println(w_yh[i][0]+"    "+w_yh[i][1]+"    "+w_yh[i][2]+"    "+w_yh[i][3]+"    "+w_yh[i][4]+"    "+w_yh[i][5]+"    "+
					w_yh[i][6]+"    "+w_yh[i][7]+"    "+w_yh[i][8]+"    "+w_yh[i][9]+"    "+w_yh[i][10]+"    "+w_yh[i][11]+"    "+
					w_yh[i][12]+"    "+w_yh[i][13]+"    "+w_yh[i][14]+"    "+w_yh[i][15]+"    "+w_yh[i][16]+"    "+w_yh[i][17]+"    "+
					w_yh[i][18]+"    "+w_yh[i][19]
					
					
					);
					
		}
	} catch (IOException e) {
		e.printStackTrace();
	}finally {
		w2.flush();
		w2.close();
	}

}//save

public void load() throws IOException {
	BufferedReader reader = new BufferedReader(new FileReader(getDataFile("LookUpTable.txt")));
	String line = reader.readLine();
	try {
        int zz=0;
        while (line != null) {
        	String splitLine[] = line.split("    ");
        	LUT[zz][0]=splitLine[0]; 
        	LUT[zz][1]=splitLine[1];
        	zz=zz+1;
        	line= reader.readLine();
        }
	} catch (IOException e) {
		e.printStackTrace();
	}finally {
		reader.close();
	}
}//load

//load 2:
public void load2() throws IOException {
	BufferedReader reader = new BufferedReader(new FileReader(getDataFile("weights_hidden.txt")));
	String line = reader.readLine();
	try {
        int zz=0;
        while (line != null) {
        	String splitLine[] = line.split("    ");
        	w_hxs[zz][0]=splitLine[0]; 
        	w_hxs[zz][1]=splitLine[1];
        	w_hxs[zz][2]=splitLine[2];
        	w_hxs[zz][3]=splitLine[3];
        	w_hxs[zz][4]=splitLine[4];
        	w_hxs[zz][5]=splitLine[5];
        	
        	zz=zz+1;
        	line= reader.readLine();
        }
	} catch (IOException e) {
		e.printStackTrace();
	}finally {
		reader.close();
	}
}//load
//load 2:
public void load22() throws IOException {
	BufferedReader reader = new BufferedReader(new FileReader(getDataFile("weights_output.txt")));
	String line = reader.readLine();
	try {
        int zz=0;
        while (line != null) {
        	String splitLine[] = line.split("    ");
        	w_yhs[zz][0]=splitLine[0]; 
        	w_yhs[zz][1]=splitLine[1];
        	w_yhs[zz][2]=splitLine[2];
        	w_yhs[zz][3]=splitLine[3];
        	w_yhs[zz][4]=splitLine[4];
        	w_yhs[zz][5]=splitLine[5];
        	w_yhs[zz][6]=splitLine[6]; 
        	w_yhs[zz][7]=splitLine[7];
        	w_yhs[zz][8]=splitLine[8];
        	w_yhs[zz][9]=splitLine[9];
        	w_yhs[zz][10]=splitLine[10];
        	w_yhs[zz][11]=splitLine[11];
        	w_yhs[zz][12]=splitLine[12]; 
        	w_yhs[zz][13]=splitLine[13];
        	w_yhs[zz][14]=splitLine[14];
        	w_yhs[zz][15]=splitLine[15];
        	w_yhs[zz][16]=splitLine[16];
        	w_yhs[zz][17]=splitLine[17];
        	w_yhs[zz][18]=splitLine[18]; 
        	w_yhs[zz][19]=splitLine[19];
        	
        	zz=0;
        	line= reader.readLine();
        }
	} catch (IOException e) {
		e.printStackTrace();
	}finally {
		reader.close();
	}
}//load

//get max
public static int getMax(double[] array){ 

	double largest = array[0];int index = 0;
	for (int i = 1; i < array.length; i++) {
	  if ( array[i] >= largest ) {
	      largest = array[i];
	      index = i;
	   }
	}
	return index;
  }//end of getMax



//wall smoothing (To make sure RL robot does not get stuck in the wall)
public void onHitWall(HitWallEvent e){
	reward-=3.5;
	double xPos=this.getX();
	double yPos=this.getY();
	double width=this.getBattleFieldWidth();
	double height=this.getBattleFieldHeight();
	if(yPos<80)
	{
		
		turnLeft(getHeading() % 90);
		
		if(getHeading()==0){turnLeft(0);}
		if(getHeading()==90){turnLeft(90);}
		if(getHeading()==180){turnLeft(180);}
		if(getHeading()==270){turnRight(90);}
		ahead(150);
		
		if ((this.getHeading()<180)&&(this.getHeading()>90))
		{
			this.setTurnLeft(90);
		}
		else if((this.getHeading()<270)&&(this.getHeading()>180))
		{
			this.setTurnRight(90);
		}
		
		
	}
	else if(yPos>height-80){ //to close to the top
		
		if((this.getHeading()<90)&&(this.getHeading()>0)){this.setTurnRight(90);}
		else if((this.getHeading()<360)&&(this.getHeading()>270)){this.setTurnLeft(90);}
		turnLeft(getHeading() % 90);
		if(getHeading()==0){turnRight(180);}
		if(getHeading()==90){turnRight(90);}
		if(getHeading()==180){turnLeft(0);}
		if(getHeading()==270){turnLeft(90);}
		ahead(150);
		
	}
	else if(xPos<80){
		turnLeft(getHeading() % 90);
		if(getHeading()==0){turnRight(90);}
		if(getHeading()==90){turnLeft(0);}
		if(getHeading()==180){turnLeft(90);}
		if(getHeading()==270){turnRight(180);}
		ahead(150);
	}
	else if(xPos>width-80){
		turnLeft(getHeading() % 90);
		if(getHeading()==0){turnLeft(90);}
		if(getHeading()==90){turnLeft(180);}
		if(getHeading()==180){turnRight(90);}
		if(getHeading()==270){turnRight(0);}
		ahead(150);
	}
	
}

//wall smoothing

}//Rl_nn class
