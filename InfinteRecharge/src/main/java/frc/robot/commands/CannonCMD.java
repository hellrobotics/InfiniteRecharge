/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.OI;
import frc.robot.subsystems.CannonSubsystem;

public class CannonCMD extends Command {

  private CannonSubsystem ssCannon;
  private OI oi;
  public boolean isRunning2 = false;
  private double ServPos = 0.0;


  /**
   * Creates a new CannonCMD.
   */
  public CannonCMD() {
    // Use addRequirements() here to declare subsystem dependencies.
    ssCannon = CannonSubsystem.getInstance();
    requires(ssCannon);
    oi = OI.getInstance();
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    System.out.println("CAnnoncmd");
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {

    if(oi.stick.getRawButtonPressed(12)){
      if(isRunning2 == true){
        isRunning2 = false;
      }
      else if(isRunning2 == false){
        isRunning2 = true;
      }
    }
 
    SmartDashboard.putBoolean("Is running =", isRunning2);

    if (oi.stick.getRawButton(2)) {
        ssCannon.RunShootWheel(-1);
        
      }
      else if(isRunning2 == true){
        ssCannon.RunShootWheel(-0.8);
      }
      
       else {
        ssCannon.RunShootWheel(oi.controller.getRawAxis(3)*-1);
      } 
      
      if(oi.controller.getRawAxis(5) < -0.1){
      ServPos += (0.01*oi.controller.getRawAxis(5));

  } else if(oi.controller.getRawAxis(5) > 0.1){
    ServPos += (0.01*oi.controller.getRawAxis(5));
  }
  ssCannon.SetVissionServo(ServPos);
  SmartDashboard.putNumber("Servo pos", ServPos);

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
