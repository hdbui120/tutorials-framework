package org.usfirst.frc.team2485.auto.SequencedItems;

import org.usfirst.frc.team2485.auto.SequencedItem;
import org.usfirst.frc.team2485.robot.Robot;

public class DisableClawPID implements SequencedItem {

	@Override
	public void run() {
		Robot.claw.setManual();
	}

	@Override
	public double duration() {
		return 0;
	}

}