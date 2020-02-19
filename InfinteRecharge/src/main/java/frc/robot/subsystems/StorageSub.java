/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotMap;

public class StorageSub extends SubsystemBase {
  /**
   * Creates a new StorageSub.
   */
 public WPI_TalonSRX pizzaWheel = new WPI_TalonSRX(RobotMap.PIZZAWHEEL);
  public StorageSub() {

  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
  private static StorageSub m_instance;
	public static synchronized StorageSub getInstance() {
		if (m_instance == null){
			m_instance = new StorageSub();
    }
    return m_instance;
  }
  public void RunPizza(double speed) {
  pizzaWheel.set(speed);
  }

  public int getPizzaPos(){
    return pizzaWheel.getSelectedSensorPosition(0);
  }
}
