package frc.robot.commands;

import java.net.CookieHandler;

import edu.wpi.first.wpilibj.PS4Controller.Button;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.subsystems.CANDriveSubsystem;

public class AdultMode extends Command {
    
    private final CANDriveSubsystem drive;
    private final int button;




    public AdultMode(CANDriveSubsystem drive, int button)
    {
        this.drive = drive;
        this.button = button;
        addRequirements(drive);
    }

    @Override
    public void initialize() {

        System.out.println("BUTTON: " + button);
        
        switch (button) {
            case 4:
                
                drive.setSpeed(Constants.kidSpeed);
                break;
        
            default:

                if (drive.checkButton(button) == false) {
                    drive.resetPassword();
                }
                else
                {
                    drive.addPassword(button);
                }

                if (drive.checkPassLength()) {
                    //max password length reached
                    if (drive.checkPassword()) {
                        //password is correct
                        drive.setSpeed(Constants.adultSpeed);
                    }
                    drive.resetPassword();

                }
                break;
        }
    }
    @Override
    public boolean isFinished() {
        return true;
    }

}