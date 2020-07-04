/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Robot;
import frc.robot.subsystems.CannonSubsystem;
import frc.robot.subsystems.DriveSub;

public class AutoTrackTarget extends CommandBase {
  /**
   * Creates a new AutoTrackTarget.
   */
  private CannonSubsystem ssCannon;
  private double xCoord = -1;
  private double yCoord = -1;

  public AutoTrackTarget() {
    ssCannon = ssCannon.getInstance();
    //addRequirements(ssCannon);
    // Use addRequirements() here to declare subsystem dependencies.
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    xCoord = Robot.centerX;
    yCoord = Robot.centerY;
    ssCannon.TrackTurret(xCoord);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return (xCoord < 60+5 && xCoord > 60-5);
  }
}
