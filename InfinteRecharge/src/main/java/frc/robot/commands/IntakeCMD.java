/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Subsystem;
import frc.robot.subsystems.ServoSub;

public class IntakeCMD extends Command {
  /**
   * Creates a new IntakeCMD.
   */
  private final ServoSub ssServ;

  public IntakeCMD() {
    // Use addRequirements() here to declare subsystem dependencies.
    ssServ = ServoSub.getInstance();
    requires(ssServ);
  }

  private void requires(ServoSub ssServ2) {
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {

    ssServ.ServoPos(1);
  }

  // Called once the command ends or is interrupted.
  @Override
  protected void end() {
  }
  

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
