package org.usfirst.frc.team2485.auto.SequencedItems;

import org.usfirst.frc.team2485.auto.SequencedItem;
import org.usfirst.frc.team2485.robot.Robot;

public class SetFingerRollers implements SequencedItem {

	public static final int INTAKE = 0, REVERSE = 1, OFF = 2; 
	private int type; 
	private double timing; 
	
//	private static int numTotes = 0; 
	
	public SetFingerRollers(int type, double timing) {
		if (type == INTAKE || type == REVERSE || type == OFF)
			this.type = type;
		else
			throw new IllegalArgumentException("Must send rollers intake or reverse or off"); 
		
		this.timing = timing; 
		
//		numTotes++; 
	}
	@Override
	public void run() {
		
		if (type == INTAKE)
			Robot.fingers.dualIntake(0.5); // 0.5
		else if (type == REVERSE) 
			Robot.fingers.dualReverse(0.5);
		else if (type == OFF)
			Robot.fingers.dualIntake(0.5); 
		else
			throw new IllegalStateException("Finger rollers can only go intake or reverse");
	}

	@Override
	public double duration() {
		return timing;
	}

	
}