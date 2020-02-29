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
import frc.robot.Robot;
import frc.robot.subsystems.CannonSubsystem;

public class CannonCMD extends Command {

  private CannonSubsystem ssCannon;
  private OI oi;
  public boolean isRunning2 = false;
  public boolean isShooting = false;
  private double ServPos = 0.5;
  private double yCoord = -1;
  private double xCoord = -1;

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
    ssCannon.ConfigPID();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    yCoord = Robot.centerY;
    xCoord = Robot.centerX;
    SmartDashboard.putNumber("Target X", xCoord);

    double distance = ((3.30-0.54) / Math.tan(Math.toRadians((ServPos-0.50)* 360)));
    //Distanse kalkulering.
    SmartDashboard.putNumber("Calculated Cannon Power", ssCannon.calculateWheelSpeed(distance));
    SmartDashboard.putNumber("Calculated distance", distance);
    Robot.distance = distance;

    if (oi.stick.getRawButtonPressed(2)) {
      isRunning2 = !isRunning2;
    }
    //if (oi.stick.getRawButtonPressed(9)) {
    //  Robot.isTracking = !Robot.isTracking;
    //}

    double cannonPower = ((oi.stick.getThrottle()+1)/2)*(-0.3)-0.7;
    SmartDashboard.putNumber("Cannon power", cannonPower);
    SmartDashboard.putBoolean("Is running =", isRunning2);
    SmartDashboard.putNumber("Cannon Speed", ssCannon.getWheelSpeed());
  if(oi.stick.getRawButton(1)){
    //ssCannon.RunShootWheelPID(ssCannon.calculateWheelSpeed(distance));
    ssCannon.RunShootWheel(cannonPower);
    isShooting = true;
  } else if(isRunning2 == true){
    //ssCannon.RunShootWheelPID(ssCannon.calculateWheelSpeed(distance));
    ssCannon.RunShootWheel(cannonPower);
    isShooting = false;
  } else{
    ssCannon.RunShootWheel(0);
    isShooting = false;
  }
  
  if(isRunning2 && yCoord != -1 && !isShooting) {
    ssCannon.TrackServo(yCoord);
    ServPos = ssCannon.GetVissionServo();
    ssCannon.TrackTurret(xCoord);
    SmartDashboard.putNumber("Servo pos", ServPos);
    System.out.println("Tracking: " + xCoord + ", " + yCoord);
  } else {
    if(ServPos <= 0.8 && oi.controller.getRawAxis(5) >= 0.1) {
      ServPos += (0.01*oi.controller.getRawAxis(5));
    } else if(0.3 <= ServPos && oi.controller.getRawAxis(5) <= -0.1) {
      ServPos += (0.01*oi.controller.getRawAxis(5));
    }
    ssCannon.SetVissionServo(ServPos);
    SmartDashboard.putNumber("Servo pos", ServPos);
    ssCannon.SpinTurret(oi.controller.getRawAxis(0));
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
