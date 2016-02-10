package org.usfirst.frc.team2485.robot;

import org.usfirst.frc.team2485.auto.*;
import org.usfirst.frc.team2485.subsystems.*;
import org.usfirst.frc.team2485.util.*; 

import edu.wpi.first.wpilibj.*;

public class Robot extends IterativeRobot {

	private SpeedController leftRoller;
	private SpeedController rightRoller;
	
	public static Rollers rollers;
	
	public void robotInit() {
		
		leftRoller = new VictorSP(6); // find port numbers
		rightRoller = new VictorSP(9); // find port numbers
		
		rollers = new Rollers(leftRoller, rightRoller);
		
		System.out.println("initialized");
	}

	public void autonomousInit() {
		
	}

	public void autonomousPeriodic() {
		
		updateDashboard();
	}

	public void teleopInit() {
		
	}

	public void teleopPeriodic() {
    
		if (Controllers.getOperatorLeftJoystickButton(1)) {
			rollers.intakeTote(0.5);
		} else if (!Controllers.getOperatorLeftJoystickButton(1)) {
			rollers.intakeTote(0);
		}
		
    	updateDashboard();
	}

	public void disabledInit() {
		
	}

	public void disabledPeriodic() {

		updateDashboard();
	}
	
	public void testInit() {
		
	}
	
	public void testPeriodic() {
		
	}

	public void updateDashboard() {
		 
	}
}
