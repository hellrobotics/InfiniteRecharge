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
import frc.robot.subsystems.DriveSub;

public class DriveCMD extends Command {
  /**
   * Creates a new DriveCMD.
   */


  PathWriter pw;
  boolean isRecording = false;
  public String filename = "tmpName";

  private DriveSub ssDrive;
  private OI oi;
  private double driveDir;


  public DriveCMD() {
    // Use addRequirements() here to declare subsystem dependencies.
    ssDrive = DriveSub.getInstance();
    requires(ssDrive);
    oi = OI.getInstance();
  }
  
  private void requires(DriveSub ssDrive2) {
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    SmartDashboard.putString("Path name", filename);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    //xCoord = Robot.centerX;

    //make sure turning is always the correct direction.
    if(oi.stick.getRawAxis(3) < 0 ){
      driveDir = -1;
    }
    else{
      driveDir = 1;
    }


      //standard drivetrain code with adjustable speed
      ssDrive.Arcade(oi.stick.getRawAxis(1)*(oi.stick.getRawAxis(3)), oi.stick.getRawAxis(0)*(oi.stick.getRawAxis(3))*driveDir*0.75)  ;
 
    //}
    if(oi.stick.getRawButtonPressed(10) && !isRecording){
      filename = SmartDashboard.getString("Path name", filename);
      pw = new PathWriter(filename+".csv");
      pw.start();
    }
    if(pw != null && pw.isFinished()){
      isRecording = false;
    }
 
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
