// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPLTVController;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.studica.frc.AHRS;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry3d;
import edu.wpi.first.units.DistanceUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

import static edu.wpi.first.units.Units.Inch;
import static edu.wpi.first.units.Units.Meter;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Radian;
import static frc.robot.Constants.DriveConstants.*;

import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.objdetect.FaceDetectorYN;

public class CANDriveSubsystem extends SubsystemBase {

  public Pose2d pose;

  private final ChassisSpeeds chassisSpeeds;
 
  private final SparkMax leftLeader;
  private final SparkMax leftFollower;
  private final SparkMax rightLeader;
  private final SparkMax rightFollower;

  private final DifferentialDrive drive;

  private final RelativeEncoder leftEncoder, rightEncoder;
  private DifferentialDriveOdometry odom;
  
  public AHRS gyro;

  public float speed = Constants.adultSpeed;

  public CANDriveSubsystem() {



    chassisSpeeds = new ChassisSpeeds();

    // create Brushless motors for drive
    leftLeader = new SparkMax(LEFT_LEADER_ID, MotorType.kBrushless);
    leftFollower = new SparkMax(LEFT_FOLLOWER_ID, MotorType.kBrushless);
    rightLeader = new SparkMax(RIGHT_LEADER_ID, MotorType.kBrushless);
    rightFollower = new SparkMax(RIGHT_FOLLOWER_ID, MotorType.kBrushless);

                        leftEncoder = leftLeader.getEncoder();
    rightEncoder = rightLeader.getEncoder();

    //odom = new DifferentialDriveOdometry(gyro.getRotation2d(), getDistance(leftEncoder), getDistance(rightEncoder));

    // set up differential drive class
    drive = new DifferentialDrive(leftLeader, rightLeader);

    // Set can timeout. Because this project only sets parameters once on
    // construction, the timeout can be long without blocking robot operation. Code
    // which sets or gets parameters during operation may need a shorter timeout.
    leftLeader.setCANTimeout(250);
    rightLeader.setCANTimeout(250);
    leftFollower.setCANTimeout(250);
    rightFollower.setCANTimeout(250);

    // Create the configuration to apply to motors. Voltage compensation
    // helps the robot perform more similarly on different
    // battery voltages (at the cost of a little bit of top speed on a fully charged
    // battery). The current limit helps prevent tripping
    // breakers.
    SparkMaxConfig config = new SparkMaxConfig();
    config.voltageCompensation(12);
    config.smartCurrentLimit(DRIVE_MOTOR_CURRENT_LIMIT);
    config.idleMode(IdleMode.kBrake);

    // Set configuration to follow each leader and then apply it to corresponding
    // follower. Resetting in case a new controller is swapped
    // in and persisting in case of a controller reset due to breaker trip
    config.follow(leftLeader);
    leftFollower.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    config.follow(rightLeader);
    rightFollower.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    

    // Remove following, then apply config to right leader
    config.disableFollowerMode();
    rightLeader.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    // Set config to inverted and then apply to left leader. Set Left side inverted
    // so that postive values drive both sides forward
    config.inverted(true);
    leftLeader.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    RobotConfig robotConfig = null;
    try{
      robotConfig = RobotConfig.fromGUISettings();
    } catch (Exception e) {
      e.printStackTrace();
    }

    correctPassword.add(1);
    correctPassword.add(1);
    correctPassword.add(3);
    correctPassword.add(2);
    correctPassword.add(3);


    // Configure AutoBuilder last

  }

  public List<Integer> correctPassword = new ArrayList<>();
  public List<Integer> inputPassword = new ArrayList<>();

  public void resetPassword() {
    inputPassword.clear();
    System.out.println("PASSWORD CLEARED");
  }

  public void addPassword(int button) {
      inputPassword.add(button);

      System.out.println("ADDED BUTTON: " + button);
      System.out.println("NEW PASSWORD: " + inputPassword.toString());

  }

  public boolean checkPassLength() {
      if (inputPassword.size() == 5) {
          System.out.println("PASS AT MAX LENGTH");
          return true;
      }
      System.out.println("PASS IS NOT AT MAX LENGTH");

      return false;
  }

  public boolean checkPassword() {
      if (correctPassword.equals(inputPassword))
      {
          System.out.println("PASS IS CORRECT");
          return true;
      }
      System.out.println("PASS IS INCORRECT");
      System.out.println(Arrays.asList(correctPassword).toString());
      System.out.println(inputPassword.toString());
      return false;
  }

  public boolean checkButton(int button) {
      if (correctPassword.get(inputPassword.size()) == button) {
          System.out.println("BUTTON IS CORRECT");
          return true;
      }
      System.out.println("BUTTON IS INCORRECT. CORECT BUTTON IS " + correctPassword.get(inputPassword.size()));
      return false;
      
  }

  public void changeSpeed(float amount) {
      float finalSpeed = speed + amount;
      speed = Math.min(
          Math.max(finalSpeed, 0f),
          Constants.adultSpeed);  
  }

  public void setSpeed(float amount) {
      speed = amount;
  }

  @Override
  public void periodic() {
    //odom.update(gyro.getRotation2d(), getDistance(leftEncoder).baseUnitMagnitude(), getDistance(rightEncoder).baseUnitMagnitude());
  }

  public void driveArcade(double xSpeed, double zRotation) {
    drive.arcadeDrive(xSpeed, zRotation);
  }

  public void driveRobotRelative(ChassisSpeeds speeds) {
    drive.arcadeDrive(speeds.vxMetersPerSecond, speeds.vyMetersPerSecond);
  }

  public Distance getDistance(RelativeEncoder encoder)
  {
    return Distance.ofBaseUnits(encoder.getPosition() * Constants.DriveConstants.WHEEL_CIRCUMFERENCE, Meters);
  }

  public ChassisSpeeds getCurrentSpeeds()
  {
    return chassisSpeeds;
  }

  public Pose2d getPose()
  {
      return pose;
  }

  public void resetPose(Pose2d poseOdom)
  {
    odom.resetPosition(gyro.getRotation2d(), getDistance(leftEncoder).baseUnitMagnitude(), getDistance(rightEncoder).baseUnitMagnitude(), new Pose2d());
  }

  


}
