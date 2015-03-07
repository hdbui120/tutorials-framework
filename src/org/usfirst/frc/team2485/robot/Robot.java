
package org.usfirst.frc.team2485.robot;

import org.usfirst.frc.com.kauailabs.nav6.frc.IMUAdvanced;
import org.usfirst.frc.team2485.auto.Sequencer;
import org.usfirst.frc.team2485.auto.SequencerFactory;
import org.usfirst.frc.team2485.subsystems.*;
import org.usfirst.frc.team2485.util.CombinedVictorSP;
import org.usfirst.frc.team2485.util.Controllers;
import org.usfirst.frc.team2485.util.DualEncoder;
import org.usfirst.frc.team2485.util.ToteCount;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Relay.Direction;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.VictorSP;

/**
 * @author Anoushka Bose
 * @author Aidan Fay
 * @author Ben Clark
 * @author Patrick Wamsley
 * @author Camille Considine
 * @author Mike Maunu
 */ 
public class Robot extends IterativeRobot {
	
	//subsystems 
	public static DriveTrain drive;
	public static Strongback strongback; 
	public static Clapper clapper;
	public static Fingers fingers;
	public static RatchetSystem ratchet;
	public static Claw claw;
	public static ToteCount toteCounter;

	
	private long timeLastToteCountProcessed;
	private long TOTE_COUNT_MIN_DELAY = 500;
	
	private VictorSP left, left2, right, right2, strongbackMotor, leftFingerBelt, rightFingerBelt, clapperLifter1, clapperLifter2;
	private CombinedVictorSP center; 
	 
	private Encoder leftEnc, rightEnc, centerEnc;
	private DualEncoder dualEncoder;
	
	private Solenoid centerWheelSuspension, longFingerActuators, shortFingerActuators, latchActuator;
	private Compressor compressor;
	private Relay compressorSpike;	
	private DigitalInput pressureSwitch;
	private DoubleSolenoid clapperActuator, clawSolenoid;
	private IMUAdvanced imu;
	private SerialPort ser;
//	private CameraServer camServer; 
	
	private Sequencer autoSequence;
	private AnalogPotentiometer clapperPot;
	private CombinedVictorSP combinedVictorSP;
	private Sequencer teleopSequence;
	
	int degrees;
	private double curPos;
	private double lastPos;
	private double lastVelocity;
	private static double currVelocity;
	public static ContainerCommandeerer containerCommandeerer;
	private static Solenoid commandeererSolenoidR, commandeererSolenoidL;
	
	private boolean toteCounterButtonIsReset = true; 
    private boolean done = false;
	private VictorSP clawMotor;
	private AnalogPotentiometer clawPot;
	private DigitalInput clapperSafetyLimitSwitch; 
	private DigitalInput toteDetectorLimitSwitch; 
	
//	boolean fingersOn = true;
	
    public void robotInit() {
    	
    	left     				= new VictorSP(14); //left: 14,15
    	left2 	    			= new VictorSP(15);
    	right       			= new VictorSP(0); //right: 0, 1
    	right2  				= new VictorSP(1);
    	leftFingerBelt    		= new VictorSP(9); // could be 6
    	rightFingerBelt   		= new VictorSP(6); //could be 9
    	clawMotor				= new VictorSP(12);
    	clapperLifter1 			= new VictorSP(13); 
    	clapperLifter2 			= new VictorSP(3); 
    	strongbackMotor 		= new VictorSP(2); 
    	center		 			= new CombinedVictorSP(new VictorSP(11), new VictorSP(7)); 
    	
    	longFingerActuators  	= new Solenoid(5);
    	shortFingerActuators 	= new Solenoid(6);
    	latchActuator 			= new Solenoid(2);
    	centerWheelSuspension	= new Solenoid(3); 
    	clawSolenoid			= new DoubleSolenoid(0,4); 
    	clapperActuator 		= new DoubleSolenoid(1,7);
    	
    	clawPot		    		= new AnalogPotentiometer(0);
    	clapperPot		   		= new AnalogPotentiometer(1);  
    	
    	clapperSafetyLimitSwitch = new DigitalInput(16); 
    	toteDetectorLimitSwitch  = new DigitalInput(17);
    	
    	commandeererSolenoidL = new Solenoid(1, 2);
    	commandeererSolenoidR = new Solenoid(1, 0);
    	
    	leftEnc = new Encoder(0, 1);
    	rightEnc = new Encoder(4, 5);
    	dualEncoder = new DualEncoder(leftEnc, rightEnc);
    	
    	leftEnc .setDistancePerPulse(.0414221608);
    	rightEnc.setDistancePerPulse(.0414221608); 
    	
    	toteCounter = new ToteCount(); 

//    	compressor = new Compressor();
    	compressorSpike = new Relay(0);
    	pressureSwitch = new DigitalInput(10); //TODO: find port
    	
    	try{
    		ser = new SerialPort(57600, SerialPort.Port.kUSB);
    		byte update_rate_hz = 50;
    		imu = new IMUAdvanced(ser, update_rate_hz);
    	
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
    	
    	
    	if(imu != null) {
    		LiveWindow.addSensor("IMU", "Gyro", imu);
    	}
    	
    	drive = new DriveTrain(left, left2, right, right2, center, centerWheelSuspension, imu, leftEnc, rightEnc, centerEnc);
       	clapper = new Clapper(clapperLifter1, clapperLifter2, clapperActuator, clapperPot, toteDetectorLimitSwitch, clapperSafetyLimitSwitch);
    	claw    = new Claw(clawMotor, clawSolenoid, clawPot);
    	fingers = new Fingers(leftFingerBelt,rightFingerBelt,longFingerActuators,shortFingerActuators);
    	ratchet = new RatchetSystem(latchActuator);    	
    	strongback = new Strongback(strongbackMotor, imu); 
    	containerCommandeerer = new ContainerCommandeerer(commandeererSolenoidL, commandeererSolenoidR);
    	
    	
//    	camServer = CameraServer.getInstance();
        //camServer.setQuality(50);
        //the camera name (ex "cam0") can be found through the roborio web interface
//        camServer.startAutomaticCapture("cam1");
    	
        Controllers.set(new Joystick(0), new Joystick(1), new Joystick(2));
    	
    	System.out.println("initialized");
    }

    public void autonomousInit() {
    	imu.zeroYaw();
//    	strongback.enablePid();
//    	leftEnc.reset();
//    	rightEnc.reset();
//    	dualEncoder.reset();
//    	strongback.disablePid(); 
//    	
//        int autonomousType = (int) SmartDashboard.getNumber("autoMode", SequencerFactory.DRIVE_TO_AUTO_ZONE);
//        autoSequence = SequencerFactory.createAuto(autonomousType);
        autoSequence = SequencerFactory.createAuto(SequencerFactory.DRIVE_TO_AUTO_ZONE);
    	
    }
  
    public void autonomousPeriodic() {
////    	System.out.println("left/right " + leftEnc.getDistance() + "\t\t" + rightEnc.getDistance());
////    	System.out.println("dualEnc " + dualEncoder.getDistance());
//    	
////    	drive.setLeftRight(-.7, -.7);
////    	 autoSequence.run();
////    	 
    	 if (autoSequence != null) {
//    		System.out.println("running teleop sequence");
    		if (autoSequence.run()) {
    			autoSequence = null;
//    			clapper.setManual(); 
    		}
    	}
    	 
    }
    
    public void teleopInit() {
    	System.out.println("teleop init");
    	containerCommandeerer.resetSol();
//    	imu.zeroYaw();
    	
    	drive.setMaintainHeading(false);
    	drive.dropCenterWheel(false);
    	
    	drive.disableDriveStraightPID();
    	
    	
    	
    	drive.disableIMUPID();
//    	drive.disableStrafePID(); 	//keep this commented out as long as there is no center encoder
    	
    	leftEnc.reset();
    	rightEnc.reset();
    	
    	clapper.setManual();
    	
//		strongback.enablePid();
		claw.setManual();
		
		teleopSequence = null; 
    	strongback.setSetpoint(0);
    	strongback.disablePid();
    	claw.liftManually(0);
  
    	fingers.dualIntake(0);
    	clapper.liftManually(0);
    	
//		System.out.println(clapper.getPotValue());
    }	

    public void teleopPeriodic() {
    	
    	if (Controllers.getButton(Controllers.XBOX_BTN_A)) 
    		strongback.enablePid();
    	else if (Controllers.getButton(Controllers.XBOX_BTN_B))
    		strongback.disablePid();
    	
    	strongback.checkSafety();
    	
       	updateDashboard();
	
       	  ////////////////////////////////////////////
       	 ///////////	 DRIVE CODE	   //////////////
       	////////////////////////////////////////////
     
       	//controls changed 2/16/15 per driver request
       	if (Controllers.getAxis(Controllers.XBOX_AXIS_RTRIGGER, .2f) > 0) 
       		drive.setForcedNoStrafeMode(true);
       	else 
       		drive.setForcedNoStrafeMode(false);
       	
       	if (Controllers.getAxis(Controllers.XBOX_AXIS_LTRIGGER, .2f) > 0) 
       		drive.setSlowStrafeOnlyMode(true);
       	else 
       		drive.setSlowStrafeOnlyMode(false);
      
       	if (Controllers.getButton(Controllers.XBOX_BTN_RBUMP)) 
       		drive.setQuickTurn(true);
       	else
       		drive.setQuickTurn(false);		
       	
        if (Controllers.getButton(Controllers.XBOX_BTN_LBUMP))
        	drive.setNormalSpeed(); 	
        else 
        	drive.setLowSpeed();
        
        if (Controllers.getButton(Controllers.XBOX_BTN_BACK))
        	drive.dropCenterWheel(false); 
        if (Controllers.getButton(Controllers.XBOX_BTN_START))
        	drive.dropCenterWheel(true);
        
        drive.warlordDrive(Controllers.getAxis(Controllers.XBOX_AXIS_LX, 0),
				Controllers.getAxis(Controllers.XBOX_AXIS_LY, 0),
    			Controllers.getAxis(Controllers.XBOX_AXIS_RX, 0));

		/////////////////////////////////////////////
		//////////		TOTE COUNTER
		/////////////////////////////////////////////

        
        long currTime = System.currentTimeMillis();
        if (Controllers.getButton(Controllers.XBOX_BTN_Y) && currTime - timeLastToteCountProcessed > TOTE_COUNT_MIN_DELAY) {
        	toteCounter.addTote(); 
        	timeLastToteCountProcessed = currTime;
        } else if (Controllers.getButton(Controllers.XBOX_BTN_X) && currTime - timeLastToteCountProcessed > TOTE_COUNT_MIN_DELAY) {
//        	toteCounter.reset(); 
        	toteCounter.subtractTote(); //this is stupid
        	timeLastToteCountProcessed = currTime;
        } 
           
		/////////////////////////////////////////////
		//////////		PSEUDO-VELOCITY CALCULATIONS
		/////////////////////////////////////////////

       	double curPos = dualEncoder.getDistance();
       	currVelocity = curPos-lastPos;
//       	System.out.println(imu.getWorldLinearAccelX() +"," + imu.getWorldLinearAccelY() + "," + imu.getWorldLinearAccelZ() + "," + imu.getPitch() + "," + imu.getRoll() + "," + imu.getYaw() + "," + curPos + "," + curVelocity + "," + (curVelocity - lastVelocity));
       
       	lastPos = curPos;
       	lastVelocity = currVelocity;
       	
       	
		/////////////////////////////////////////////
		///////////////CLAPPER LOGIC////////////////
		///////////////////////////////////////////
//       	if (clapperSafetyLimitSwitch.get()) {
////    		System.out.println("CLAPPERS TOO LOW");
//    		if (clapper.isAutomatic())
//    			clapper.setManual(); 
//    	}
       	if (Controllers.getJoystickAxis(Controllers.JOYSTICK_AXIS_Y,(float) 0.1) != 0) {//if the joystick is moved
    		clapper.liftManually((Controllers.getJoystickAxis(Controllers.JOYSTICK_AXIS_Y,(float) 0.1))); //back is up
    	}
    	else if (clapper.isManual()){
    		//System.out.println("enabling clapper PID after manual operation");
    		clapper.setSetpoint(clapper.getPotValue());//set the setpoint to where ever it left off
    	}
//    	else if (clapper.isBelowLowestSetPoint()) {
//    		clapper.clapperPID.disable();
//    	}
//       	
       	clapper.updateToteCount(toteCounter.getCount());
       	
       	if (Controllers.getJoystickButton(1) && teleopSequence == null) {
       		teleopSequence = SequencerFactory.createTestPickupWithStrongbackTilt();
       	}
       	if (Controllers.getJoystickButton(2) && teleopSequence == null) {
    		teleopSequence = SequencerFactory.createToteIntakeNoHang();
    	}
       	
       	if(Controllers.getJoystickButton(3))
       		clapper.openClapper();
       	if(Controllers.getJoystickButton(4))
       		clapper.closeClapper();
       	

		/////////////////////////////////////////////
		//////////		FINGERS, RATCHET, AND ONE CLAPPER SETPOINT 
       	//////////		(ONLY ENABLING A SINGLE CLAPPER SETPOINT)
		/////////////////////////////////////////////

       	//FINGERS, RATCHET, AND ONE CLAPPER SETPOINT (ONLY ENABLING A CLAPPER SETPOINT)
       	if(Controllers.getJoystickAxis(Controllers.JOYSTICK_AXIS_Z) > .7){
       		fingers.rotateToteRight(.7);
       	} else if(Controllers.getJoystickAxis(Controllers.JOYSTICK_AXIS_Z) < -.7){
       		fingers.rotateToteLeft(.7);
       	} else if(Controllers.getJoystickButton(5)){
       		fingers.dualIntake(1);
    	} else if(Controllers.getJoystickButton(6)){
       		fingers.dualReverse(.75);
    	} else {
       		fingers.dualIntake(0); 
    	}
       	
       	if (Controllers.getJoystickButton(7)) {
       		//System.out.println("fingers should close now");
       		fingers.setFingerPosition(Fingers.CLOSED);
       	}
       	if (Controllers.getJoystickButton(8)) {
//       		clapper.setSetpoint(Clapper.COOP_THREE_TOTES_SETPOINT); 
       		clapper.setSetpoint(Clapper.COOP_THREE_TOTES_SETPOINT);
       	}
       	if (Controllers.getJoystickButton(9)) {
//       		System.out.println("fingers should go parallel");
//       		fingers.setFingerPosition(Fingers.PARALLEL);
       		fingers.setFingerPosition(Fingers.PARALLEL);
       	}
       	if (Controllers.getJoystickButton(10)) {
//       		System.out.println("hook should go back to normal");
       		ratchet.extendRatchet();
       	}
       	if (Controllers.getJoystickButton(11)) {
//       		System.out.println("fingers should open");
       		fingers.setFingerPosition(Fingers.OPEN);
       	}
       	if (Controllers.getJoystickButton(12)) {
//       		System.out.println("hook should release");
       		ratchet.retractRatchet();
       	}

       	
       	///////////////////////////////////////////////////////////////////////
       	///////////////////////////////////////////////////////////////////////
       	//
       	//	SECONDARY JOYSTICK CONTROLS...ALL CONTAINER/CLAW RELATED
       	//
       	///////////////////////////////////////////////////////////////////////
       	///////////////////////////////////////////////////////////////////////
       	
       	
       	if (Controllers.getSecondaryJoystickAxis(Controllers.JOYSTICK_AXIS_Y, .1f) != 0) {
       		claw.liftManually(Controllers.getSecondaryJoystickAxis(Controllers.JOYSTICK_AXIS_Y));
       	} else if (claw.isManual()) {
       		claw.setPID(Claw.kP_LOCK_POSITION_IN_PLACE, 0, 0);
    		claw.setSetpoint(claw.getPotValue());
       	}
       	
       	if(Controllers.getSecondaryJoystickButton(1) && teleopSequence == null)
       		teleopSequence = SequencerFactory.createContainerPickupRoutine();
       	if(Controllers.getSecondaryJoystickButton(2) && teleopSequence == null)
       		teleopSequence = SequencerFactory.createPrepareForContainerLiftRoutine();
       	
    	if(Controllers.getSecondaryJoystickButton(3))
       		claw.open();
       	if(Controllers.getSecondaryJoystickButton(4))
       		claw.close();
       	
       	if (Controllers.getSecondaryJoystickButton(5) && teleopSequence == null) {
       		teleopSequence = SequencerFactory.createContainerRightingRoutine();
       	}
       	
       	if (Controllers.getSecondaryJoystickButton(6)) {
//       		null so far
       	}
       	
       	if(Controllers.getSecondaryJoystickButton(7)) {
       		claw.setPID(Claw.kP_LESS_POWER_ALLOWS_MORE_ERROR, Claw.kI, Claw.kD);
       		claw.setSetpoint(Claw.PLACE_ON_EXISTING_STACK_SIX_TOTES);
       	}
       	
       	//////////////////////////////////////////////
		//////////////////////////////////////////////
		//////////////////////////////////////////////
       	if(Controllers.getSecondaryJoystickButton(8) && teleopSequence == null) {
//       		teleopSequence = SequencerFactory.createTestPickupWithStrongbackTilt();
       	}
       	//////////////////////////////////////////////
		//////////////////////////////////////////////
		//////////////////////////////////////////////
       	
       	if(Controllers.getSecondaryJoystickButton(9)) {
       		claw.setPID(Claw.kP_LESS_POWER_ALLOWS_MORE_ERROR, Claw.kI, Claw.kD);
       		claw.setSetpoint(Claw.PLACE_ON_EXISTING_STACK_FIVE_TOTES);
       	}
       	
       	if(Controllers.getSecondaryJoystickButton(10) && teleopSequence == null) {
       		teleopSequence = SequencerFactory.createDropToteStackRoutine(true);//totes on the ratchet and one underneath
       	}
       	
       	if(Controllers.getSecondaryJoystickButton(11)) {
       		claw.setPID(Claw.kP_LESS_POWER_ALLOWS_MORE_ERROR, Claw.kI, Claw.kD);
       		claw.setSetpoint(Claw.PLACE_ON_EXISTING_STACK_FOUR_TOTES);
//      		claw.setSetpoint(Claw.ONE_TOTE_RESTING);
       	}
       	
       	if(Controllers.getSecondaryJoystickButton(12) && teleopSequence == null) {
       		teleopSequence = SequencerFactory.createDropToteStackRoutine(false);//only totes on the ratchet
       	}
       		
   		if ((Controllers.getJoystickAxis(Controllers.JOYSTICK_AXIS_THROTTLE) > 0) ||
   				Controllers.getSecondaryJoystickAxis(Controllers.JOYSTICK_AXIS_THROTTLE) > 0) {
   			//kill ALL THE THINGS@!#@#!!@@
   			if(teleopSequence != null) {
   				teleopSequence.clear();
   				teleopSequence = null;
   			}
   			System.out.println("Killing all the things");
   		}
   	
       	
       	if (teleopSequence != null) {
//       		System.out.println("running sequence here in teleopPeriodic");
       		if (teleopSequence.run()) {
       			teleopSequence = null;
       		}
       	}
       	
       	claw.updateWinchPeriodic();
    	clapper.updateLastHeight(); 

    	
//    	System.out.println("Strongback: isEnabled " + strongback.leadScrewImuPID.isEnable() + "\t\tSetpoint" + strongback.leadScrewImuPID.getSetpoint());
    }
    
    public static double getCurrVelocity() {
		return currVelocity;
	}

    public void disabledInit() {
    	if(teleopSequence != null) {
    		System.out.println("teleopSequence not null here in disabledInit");
    		teleopSequence = null;
    	}
    	
    	
    }
    
	public void disabledPeriodic() {
//    	System.out.println(clapper.getPotValue());
//    	int counter = 0;
//    	
//    	if (Controllers.getButton(Controllers.XBOX_BTN_A)) {
//    		counter++;
//    	}
//    	
//    	if(counter > 50) {
//    		degrees += 30;
////    		System.out.println("degrees is now " + degrees);
//    		counter = 0;
//    	}
		if(teleopSequence != null) {
			System.out.println("teleopSequence not null in disabledPeriodic");
			teleopSequence.clear();
			teleopSequence = null;
		}
    	updateDashboard();
    }
    
    public void testInit() {
    	clapper.setManual();
    	leftEnc.reset();
    	rightEnc.reset();
    	drive.disableDriveStraightPID();
    	drive.disableIMUPID();
    	
    	done = false;
    }
        
    public void testPeriodic() {
    	
    	if (!pressureSwitch.get()) {
    		compressorSpike.set(Relay.Value.kForward);
    	}
    	else
    	{
    		compressorSpike.set(Relay.Value.kOff);
    	}
//    	compressor.start();
    	
    	
    	
    	
    	
//    	if(toteDetectorLimitSwitch.get())
//    		System.out.println("get returned true");
//    	else
//    		System.out.println("get returned false");
    	
    	//    	
//    	clapper.setSetpoint(Clapper.ON_RATCHET_SETPOINT);
//    	claw.close(); 
//    	claw.setSetpoint(Claw.ONE_TOTE_LOADING);
//    	claw.setSetpoint(Claw.ONE_TOTE_RESTING);
    	
 //   	clapper.liftManually(.4);
    	
//    	leftFingerBelt.set(.2);
    	
//clawMotor.set(.2);

    	

//    	drive.setLeftRight(.2, -.2);
//    	drive.driveTo(60);
    	
//    	degrees = 30;
    	
//    	drive.dropCenterWheel(false);
//    	if(!done && drive.rotateTo(30)) {
//    		done = true;
//    		System.out.println("just finished rotateTo inside of testPeriodic");
//    	}
    	
//    	System.out.println(imu.getYaw());
    	
//    	
//    	  if (Controllers.getButton(Controllers.XBOX_BTN_START))
//          	drive.tuneDriveKp(.005);
//          if (Controllers.getButton(Controllers.XBOX_BTN_BACK))
//          	drive.tuneDriveKp(-.005);
//          if (Controllers.getButton(Controllers.XBOX_BTN_Y)) 
//          	drive.resetButtonClicked(); 
          
          
//    	System.out.println("Imu yaw: " + imu.getYaw());
//    	System.out.println("Imu pitch: " + imu.getPitch());
//    	
//    	left.set(-.5);
//    	left2.set(-.5); 
//    	right.set(.5);
//    	right2.set(.5);
    	
//    	leadScrewMotor.set(-.05);
//    	System.out.println(strongback.leadScrewImuPID.isEnable());

//    	strongback.enablePid(); 
//		System.out.println(strongback.getError() + " output " + .leadScrewImuPID.get());
    	
//    	SmartDashboard.putString("Clapper and Container", clapper.getPercentHeight() +"," + 0 + "," + imu.getRoll());
       	
//       	SmartDashboard.putInt("IPS",    (int) drive.getAbsoluteRate());
       	

    }
    
    public void updateDashboard() {
     	SmartDashboard.putString("Clapper and Container", clapper.getPercentHeight() + "," + (int)claw.getPotValue() + ","+ claw.getPercentHeight()+ "," + (int)clapper.getPotValue() + "," + -1 * strongback.getIMURoll());  
//    	SmartDashboard.putString("Clapper and Container", clapper.getPercentHeight() + ","+ claw.getPercentHeight() + "," + strongback.getIMURoll());  	
       	SmartDashboard.putNumber("IPS", (int) drive.getAbsoluteRate());
       	SmartDashboard.putNumber("Battery", DriverStation.getInstance().getBatteryVoltage());
        SmartDashboard.putBoolean("Disabled", DriverStation.getInstance().isDisabled());
        SmartDashboard.putNumber("Claw Pot", claw.getPotValue());
//        System.out.println(claw.getPotValue());
        SmartDashboard.putNumber("Clapper Pot", clapper.getPotValue());
        SmartDashboard.putNumber("Tote Count", toteCounter.getCount());
        SmartDashboard.putNumber("Error from Claw", claw.getError());
        SmartDashboard.putNumber("Claw kP", claw.getP());
        //SmartDashboard.putBoolean("Clapper is manual: ", clapper.isManual());
        SmartDashboard.putNumber("Clapper Inches", clapper.getInchHeight());
        SmartDashboard.putNumber("Claw Inches", claw.getInchHeight());
        SmartDashboard.putNumber("Clapper change in height" ,  (float)Robot.clapper.getChangeInHeightInInches());
        SmartDashboard.putNumber("Encoder Distance", leftEnc.getDistance());
        SmartDashboard.putBoolean("Tote detected by limit switch", clapper.toteDetected());
    }
}
