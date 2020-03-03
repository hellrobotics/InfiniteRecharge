package frc.robot;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commandgroups.AutoCMDGRP;
import frc.robot.commands.CannonCMD;
import frc.robot.commands.DriveCMD;
import frc.robot.commands.ElevatorCMD;
import frc.robot.commands.IntakeCMD;
import frc.robot.commands.StorageCMD;


public class Robot extends TimedRobot {
  public static OI m_oi;

  Command m_autonomousCommand;
  SendableChooser<Command> m_chooser = new SendableChooser<>();
  /*
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  */


  Command driveRun = new DriveCMD();
  Command cannonRun = new CannonCMD();
  Command intakeRun = new IntakeCMD();
  Command storageRun = new StorageCMD();
  Command elevatorRun = new ElevatorCMD();
  SequentialCommandGroup autoCMD = new AutoCMDGRP();

  private static final int IMG_WIDTH = 320;
  
  public static double centerX = 0;
  public static double centerY = 0;
  public static boolean isTracking = false;
  public static double distance = 0;
  public static boolean SensorA = false;

  private final Object imgLock = new Object();

  NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
  private static NetworkTableEntry centerXEntry;
  private static NetworkTableEntry centerYEntry;

  public static double visionError = 0.0;
  public static double visionErrorY = 0.0;
  public static double CenteX = 0.0;
  public static double CenteY = 0.0;
  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_oi = new OI();


    //m_chooser.addDefault("Nothing", new ExampleCommand());
		//m_chooser.addObject("Auto Mid Switch", new AutoCMDGRP());


    final NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
    final NetworkTable table = ntinst.getTable("visionTable");
    centerXEntry = table.getEntry("centerX"); 
    centerYEntry = table.getEntry("centerY");
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
 
    centerX = centerXEntry.getDouble(-1);
    centerY = centerYEntry.getDouble(-1);

    
    CenteX = ((centerX/255) -0.5);
    CenteY = ((centerY/255) -0.5);
    
    
    //System.out.println(CenteY);
    double centerXp;
    double centerYp;
    synchronized (imgLock) {
      
      centerXp = this.centerX;
      centerYp = this.centerY;
  
    }
    if (centerXp != -1) {
      visionError = centerXp - (IMG_WIDTH / 2.0*0.25);
      //System.out.println(centerXp + " " + visionError);

    } else {
     //System.out.println("No targets X-axis");
      visionError = 0.0;
    }
    if (centerYp != -1){
      visionErrorY =centerYp - (IMG_WIDTH / 2.0*0.25);
      //System.out.println(centerYp + " " + visionError);

    } else{
      //System.out.println("No targets Y-axis");
      visionErrorY = 0.0;
    }

  }

  /**
   * This function is called once each time the robot enters Disabled mode.
   * You can use it to reset any subsystem information you want to clear when
   * the robot is disabled.
   */
  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
    Scheduler.getInstance().run();
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString code to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional commands to the
   * chooser code above (like the commented example) or additional comparisons
   * to the switch structure below with additional strings & commands.
   */
  @Override
  public void autonomousInit() {
    //m_autonomousCommand = m_chooser.getSelected();

    /*
     * String autoSelected = SmartDashboard.getString("Auto Selector",
     * "Default"); switch(autoSelected) { case "My Auto": autonomousCommand
     * = new MyAutoCommand(); break; case "Default Auto": default:
     * autonomousCommand = new ExampleCommand(); break; }
     */

    // schedule the autonomous command (example)
    /*if (m_autonomousCommand != null) {
      m_autonomousCommand.start();
    }*/
    autoCMD.schedule();
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    Scheduler.getInstance().run();
  }

  @Override
  public void teleopInit() {
    autoCMD.cancel();
    Scheduler.getInstance().run();
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    System.out.println("TELEOPINIT");
    
    driveRun.start();
    storageRun.start();
    intakeRun.start();
    cannonRun.start();
    elevatorRun.start();
    
    System.out.println("TELEOP DONE");

    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
  }
  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    Scheduler.getInstance().run();
    centerX = centerXEntry.getDouble(-1);
    centerY = centerYEntry.getDouble(-1);

  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
