package org.usfirst.frc.team2485.auto;

import org.usfirst.frc.team2485.auto.SequencedItems.*;
import org.usfirst.frc.team2485.robot.Robot;
import org.usfirst.frc.team2485.subsystems.*;

/**
 * 
 * Contains methods which return Sequencer objects in order to run the robot autonomously, 
 * during both the auto and teleop modes of the game. 
 * 
 */

public class SequencerFactory {

	// auto types
	public enum AutoType {
		ROLLERS
	}
	
	public static Sequencer createAuto(AutoType autoType) {

		switch (autoType) {
		
			case ROLLERS:
				return new Sequencer(new SequencedItem[] {
						new RunRollers(0.5, 3.0)
				});
			
		}
		return new Sequencer();
	}
	

	/**
	 * @return Sequence used to run rollers two times, for 3 then 5 seconds. 
	 */
	public static Sequencer createRunRollersTwice() {
		return new Sequencer(
			new SequencedItem[] {
				new RunRollers(0.5, 3.0),
				new RunRollers(0.5, 5.0)
			});
	}
	
}
