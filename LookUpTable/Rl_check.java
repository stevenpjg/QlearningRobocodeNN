package currentrl; //change it into your package name

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
public class Rl_check extends AdvancedRobot {

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
    int qrl_x=0;
    int qrl_y=0;
    int qenemy_x=0;
    int qenemy_y=0;
    private RobotStatus robotStatus;
    int qdistancetoenemy=0;
    //-------------Explore or greedy----------------------//
    
    boolean explore=false;
    boolean greedy=true;
    //----------------------------------------------------//
    double absbearing=0;
    int q_absbearing=0;
    //initialize reward
    double reward=0;
    String state_action_combi=null;
    String state_action_combi_greedy=null;
    double robot_energy=0;
    int sa_combi_inLUT=0;
    //Run command-Robocode
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
	
	
public void onRoundEnded(RoundEndedEvent e) {
	System.out.println("cumulative reward of one full battle is ");	
	System.out.println(cum_reward_while);
	System.out.println("index number ");	
	System.out.println(getRoundNum());
	cum_reward_array[getRoundNum()]=cum_reward_while;
	for(int i=0;i<cum_reward_array.length;i++){
		System.out.println(cum_reward_array[i]);
		System.out.println();
	}
	
	
	index1=index1+1;
	save1();
	   }


public void save1() {

	PrintStream w = null;
	try {
		w = new PrintStream(new RobocodeFileOutputStream(getDataFile("cum.txt")));
		for (int i=0;i<cum_reward_array.length;i++) {
			w.println(cum_reward_array[i]);
		}
	} catch (IOException e) {
		e.printStackTrace();
	}finally {
		w.flush();
		w.close();
	}

}//save

   
public void onBattleEnded(BattleEndedEvent e) {
	save1();
}
	
public void run(){
	
	if(count==0){
		
		//For initializing text file in the first run use the three lines of code. once the text file is generated in \Rl_check comment this out
				//initialiseLUT();
				//save();
				//comment this
		try {
			load();
			} 
		catch (IOException e) {
			e.printStackTrace();
			
			}
  }//to initialize some parameter in the first run
    	count=count+1;
    setColors(null, new Color(192,192,192), new Color(192,192,192), Color.black, new Color(150, 0, 150));
    setBodyColor(new java.awt.Color(192,192,192,100));
    while(true){
    	if(explore){ //Explore event--------------------------------------------------//
     		save();
    		//load command
    		try {
    			load();
    		} 
    		catch (IOException e) {
    			e.printStackTrace();
    		}
    		//load command
    //predict current state:
	turnGunRight(360);
	random_action=randInt(1,total_actions.length);
	state_action_combi=qrl_x+""+qrl_y+""+qdistancetoenemy+""+q_absbearing+""+random_action;
	
	for(int i=0;i<LUT.length;i++){
		if(LUT[i][0].equals(state_action_combi))
		{
			sa_combi_inLUT=i;
			break;
		}
	}
	 q_present = LUT[sa_combi_inLUT][1];
	 q_present_double=Double.parseDouble(q_present);
	 reward=0;
	 
	 //performing next state and scanning
	 
	 my_energy_pres=robot_energy;
	 enemy_energy_pres=enemy_energy;		 
	 rl_action(random_action);
	 
	 turnGunRight(360);
	 my_energy_next=robot_energy;
	 enemy_energy_next=enemy_energy;
	 reward1=0;
	 reward1=(my_energy_next-my_energy_pres)-(enemy_energy_next-enemy_energy_pres);
	 
	 
	 
	 
	 state_action_combi_next = qrl_x+""+qrl_y+""+qdistancetoenemy+""+q_absbearing+""+random_action;
	 for(int i=0;i<LUT.length;i++){
			if(LUT[i][0].equals(state_action_combi_next))
			{
				sa_combi_inLUT_next=i;
				break;
			}
	 }
		 q_next = LUT[sa_combi_inLUT_next][1];
		 q_next_double=Double.parseDouble(q_next);
	 
		
	 //performing update
	 q_present_double=q_present_double+alpha*(reward+gamma*q_next_double-q_present_double);
	 LUT[sa_combi_inLUT][1]=Double.toString(q_present_double);
	 cum_reward_while+=reward;
	
}//explore loop ends
    	
//Greedy Moves//    	
    	
    	if(greedy){
     		save();
    		//load command
    		try {
    			load();
    		} 
    		catch (IOException e) {
    			e.printStackTrace();
    		}
    		//load command
    //predict current state:
	turnGunRight(360);
	// finding action that produces maximum Q value
	
	for(int j=1;j<=total_actions.length;j++)
	{
		state_action_combi=qrl_x+""+qrl_y+""+qdistancetoenemy+""+q_absbearing+""+j;
	
		for(int i=0;i<LUT.length;i++){
			if(LUT[i][0].equals(state_action_combi))
			{
				actions_indices[j-1]=i;
				break;
			
			}
	}
	 
	}
	//converting table to double
	for(int i=0;i<total_states_actions.length;i++){
		for(int j=0;j<2;j++){		
	LUT_double[i][j]= Double.valueOf(LUT[i][j]).doubleValue();
		}
	}
	//converting table to double
	for(int k=0;k<total_actions.length;k++){
		q_possible[k]=LUT_double[actions_indices[k]][1];
	}
	
	Qmax_action=getMax(q_possible)+1;
	int jj=0;
	
	//find position of actions
	for(int i=0;i<4;i++){
	if(actions_indices[i]==Qmax_action)
	{Qmax_actual_action=i+1;}
	}
	//find position of actions
	
	//finding action that produces maximum q
	state_action_combi_greedy=qrl_x+""+qrl_y+""+qdistancetoenemy+""+q_absbearing+""+Qmax_action;
	
	for(int i=0;i<LUT.length;i++){
		if(LUT[i][0].equals(state_action_combi_greedy))
		{
			sa_combi_inLUT=i;
			break;
		}
	}
	
	
	
	q_present = LUT[sa_combi_inLUT][1];
	 q_present_double=Double.parseDouble(q_present);
	 reward=0;

	 //performing next state and scanning
	 reward1=0;
	 my_energy_pres=robot_energy;
	 enemy_energy_pres=enemy_energy;
	 
	 rl_action(Qmax_action);
	 
	 
	 turnGunRight(360);
	 
	 my_energy_next=robot_energy;
	 enemy_energy_next=enemy_energy;
	 reward1=0;
	 reward1=(my_energy_next-my_energy_pres)-(enemy_energy_next-enemy_energy_pres);
	 
	 state_action_combi_next = qrl_x+""+qrl_y+""+qdistancetoenemy+""+q_absbearing+""+Qmax_action;
	 for(int i=0;i<LUT.length;i++){
			if(LUT[i][0].equals(state_action_combi_next))
			{
				sa_combi_inLUT_next=i;
				break;
			}
	 }
		 q_next = LUT[sa_combi_inLUT_next][1];
		 q_next_double=Double.parseDouble(q_next);
	 
		
	 //performing update
	 q_present_double=q_present_double+alpha*(reward+gamma*q_next_double-q_present_double);
	 LUT[sa_combi_inLUT][1]=Double.toString(q_present_double);
	 cum_reward_while+=reward;
	 
}//greedy loop ends
    		
          
    }//while loop ends
    //System.out.println(cum_reward_while);
    
    
    }//run function ends

   
//function definitions for RL robot:
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
	if(qdistancetoenemy==1){fire(3);
	
	}
	if(qdistancetoenemy==2){fire(2);}
	if(qdistancetoenemy==3){fire(1);}
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
	qenemy_x=quantize_position(enemyX); //enemy x-position 
	qenemy_y=quantize_position(enemyY); //enemy y-position

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

private int quantize_angle(double absbearing2) {
	
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
	return q_absbearing;
}

private int quantize_distance(double distance2) {
	
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
//onHitWall()


private int quantize_position(double rl_x2) {
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
	return qrl_x;

	}

public void rl_action(int x)
			{
	switch(x){
		case 1: //action 1 of the RL robot
			int moveDirection=+1;  //moves in anticlockwise direction
			if (getVelocity == 0)
				moveDirection *= 1;

			// circle our enemy
			setTurnRight(getBearing + 90);
			setAhead(150 * moveDirection);
			break;
		case 2: //action 2 of the RL robot
			int moveDirection1=-1;  //moves in clockwise direction
			if (getVelocity == 0)
				moveDirection1 *= 1;

			// circle our enemy
			setTurnRight(getBearing + 90);
			setAhead(150 * moveDirection1);
			break;
		case 3: //action 3 of the RL robot
			turnGunRight(gunTurnAmt); // Try changing these to setTurnGunRight,
			turnRight(getBearing-25); // and see how much Tracker improves...
			// (you'll have to make Tracker an AdvancedRobot)
			ahead(150);
			break;
		case 4: //action 4 of the RL robot
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

//Look up table (lut) initialization:

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

//get max (Max function)
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
	reward-=3.5;//earlier it was -2
	double xPos=this.getX();
	double yPos=this.getY();
	double width=this.getBattleFieldWidth();
	double height=this.getBattleFieldHeight();
	if(yPos<80)//too close to the bottom
	{
		
		turnLeft(getHeading() % 90);
		//System.out.println("Get heading");
		//System.out.println(getHeading());
		if(getHeading()==0){turnLeft(0);}
		if(getHeading()==90){turnLeft(90);}
		if(getHeading()==180){turnLeft(180);}
		if(getHeading()==270){turnRight(90);}
		ahead(150);
		//System.out.println("Too close to the bottom");
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
		//System.out.println("Too close to the Top");
		if((this.getHeading()<90)&&(this.getHeading()>0)){this.setTurnRight(90);}
		else if((this.getHeading()<360)&&(this.getHeading()>270)){this.setTurnLeft(90);}
		turnLeft(getHeading() % 90);
		//System.out.println("Get heading");
		//System.out.println(getHeading());
		if(getHeading()==0){turnRight(180);}
		if(getHeading()==90){turnRight(90);}
		if(getHeading()==180){turnLeft(0);}
		if(getHeading()==270){turnLeft(90);}
		ahead(150);
		
	}
	else if(xPos<80){
		turnLeft(getHeading() % 90);
		//System.out.println("Get heading");
		//System.out.println(getHeading());
		if(getHeading()==0){turnRight(90);}
		if(getHeading()==90){turnLeft(0);}
		if(getHeading()==180){turnLeft(90);}
		if(getHeading()==270){turnRight(180);}
		ahead(150);
	}
	else if(xPos>width-80){
		turnLeft(getHeading() % 90);
		//System.out.println("Get heading");
		//System.out.println(getHeading());
		if(getHeading()==0){turnLeft(90);}
		if(getHeading()==90){turnLeft(180);}
		if(getHeading()==180){turnRight(90);}
		if(getHeading()==270){turnRight(0);}
		ahead(150);
	}
	
}

//wall smoothing

}//Rl_check class
