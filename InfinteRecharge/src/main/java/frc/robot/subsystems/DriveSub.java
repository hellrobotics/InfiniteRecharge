/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import frc.robot.RobotMap;

public class DriveSub extends SubsystemBase {
  /**
   * Creates a new DriveSub.
   */
  public WPI_VictorSPX TopLeft = new WPI_VictorSPX(RobotMap.TOPLEFT);
  public WPI_VictorSPX BottomLeft = new WPI_VictorSPX(RobotMap.BOTTOMLEFT);
  SpeedControllerGroup leftMotors = new SpeedControllerGroup(TopLeft, BottomLeft);


  public WPI_VictorSPX TopRigth = new WPI_VictorSPX(RobotMap.TOPRIGTH);
  public WPI_VictorSPX BottomRigth = new WPI_VictorSPX(RobotMap.BOTTOMRIGTH);
  SpeedControllerGroup rigthMotors = new SpeedControllerGroup(TopRigth, BottomRigth);

  DifferentialDrive allDrive = new DifferentialDrive(leftMotors, rigthMotors);

  private double integral_prior = 0;
  private double last_time = 0;
  
  public DriveSub() {

  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
  public void Arcade (double moveValue, double rotateValue) {
    allDrive.arcadeDrive(moveValue, rotateValue);
  }
  public void Lock(){
    TopLeft.set(0.1);
    BottomLeft.set(-0.1);
    TopRigth.set(0.1);
    BottomRigth.set(-0.1);
  
  }

  public double TrackTargetTurning (double target) {
    if (target >= 0) {
      double error = ((80.0+(40/Robot.distance)) - target);
      /* DEN HER VILLA VÆRT BEST, MEN FÅR IKKE T Å TUNE DEN BRA
      double iteration_time = Timer.getFPGATimestamp() - last_time;
      double integral = Math.max(0.1, Math.min(-0.1, integral_prior + error * iteration_time));
      double kp = 0.02*0.45;
      double ki = (1.2*kp)/0.2;
      integral_prior = integral;
      last_time = Timer.getFPGATimestamp();
      return (error*kp + integral*ki)*-1;  
      
      /* BACKUP*/
      double pk = 0.5/1.0;
      double flatness = 0.08;
      double magnitude = 1.2;
      System.out.println("tracking, object found " + error);
      double power = Math.min(0.4,(Math.log(magnitude*Math.abs(error)+(1/Math.E)) * -flatness - flatness));
      System.out.println("Power: "+power);
      if (error > 0) {
        return power;
      } else if (error < 0) {
        return -power;
      } else {
        return 0;
      }
    } else {
      return 0;
    }
  }

  private static DriveSub m_instance;
	public static synchronized DriveSub getInstance() {
		if (m_instance == null){
			m_instance = new DriveSub();
		}
		return m_instance;
  }
}
