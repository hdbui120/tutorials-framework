package org.usfirst.frc.team2485.auto.SequencedItems;

import org.usfirst.frc.team2485.auto.SequencedItem;
import org.usfirst.frc.team2485.robot.Robot;

public class RunRollers implements SequencedItem {
	
	double speed;
	double time;
	
	public RunRollers(double speed, double time) {
		if (speed < 0.0) {
			this.speed = 0.0; //fail silently
		} else if (speed < 1.0) {
			this.speed = speed;
		} else {
			this.speed = 1.0; //fail silently
		}
	}

	@Override
	public void run() {
		Robot.rollers.intakeTote(speed);
	}

	@Override
	public double duration() {
		return time;
	}
}
