package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

@Autonomous(name="Auto Drive", group="Robot")
public class RobotAutoDriveByTime_Linear extends LinearOpMode {
    // Initialize all variables for the program
    // Hardware variables
    private DcMotor leftFrontDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightBackDrive = null;
    private Servo leftArm = null;
    private static final double ARM_DEFAULT = 0.3;
    private static final double ARM_MIN = 0.0;
    private static final double ARM_MAX = 1.0;

    // Software variables
    private final ElapsedTime     runtime = new ElapsedTime();
    static final double     DEFAULT_SPEED = 0.6;

    private IMU imu = null;
    static final double TURN_SPEED_ADJUSTMENT = 0.015;     // Larger is more responsive, but also less stable
    static final double HEADING_ERROR_TOLERANCE = 1.0;    // How close must the heading get to the target before moving to next step.
    static final double MAX_TURN_SPEED = 1.0;     // Max Turn speed to limit turn rate
    static final double MIN_TURN_SPEED = 0.15;     // Min Turn speed to limit turn rate
    private double turnSpeed = 0;
    private double degreesToTurn = 0;

    @Override
    public void runOpMode() {
        // Define all the hardware
        leftFrontDrive = hardwareMap.get(DcMotor.class, "left_front_drive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "left_back_drive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "right_front_drive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "right_back_drive");
        //leftArm = hardwareMap.get(Servo.class, "claw");
        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        //double arm_position = ARM_DEFAULT;
        //leftArm.setPosition(arm_position);

        RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.UP;
        RevHubOrientationOnRobot.UsbFacingDirection  usbDirection  = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);

        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(orientationOnRobot));
        imu.resetYaw();

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Ready");
        telemetry.update();

        telemetry.addData("Current Yaw", "%.0f", getHeading());
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        //NEHA From NEHA. Parameter Specify bean.java
        moveForward(2.3);
        strafeLeft(0.5);                        //strafing to 1st block
        moveBackward(2.1 );                     //moving first block backward
        turnRightToHeading(-20);                    //turning to the red line to angle the block
        moveBackward(1.1);                      //moving the block backward into zone
        turnLeftToHeading(20);                      //turning to 0 degrees
        strafeRight(1.4);                       //strafing before going forward
        moveForward(2.3);                       //moving to 2nd block
        strafeLeft(1.2);                        //strafing left to 2nd block
        moveBackward(2, 0.2);       //moving to red zone
        turnRightToHeading(20);                     //turning to face the red zone
        moveBackward(1.5, 0.2);     //placing block in red zone

        // End of autonomous program
        telemetry.addData("Path", "Complete");
        telemetry.addData("Current Yaw", "%.0f", getHeading());
        telemetry.update();
    }

    private void acceleration(double secondsToDrive, double speedToDrive,
                              double leftFrontDriveDirection, double rightFrontDriveDirection,
                              double leftBackDriveDirection, double rightBackDriveDirection){
        double targetSpeed = speedToDrive; // Store the original target speed
        double currentSpeed = 0.0;

        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < secondsToDrive)) {
            double elapsedTime = runtime.seconds();

            // Driving less than a second
            if(secondsToDrive < 1){
                currentSpeed = speedToDrive;
            }
            // Acceleration phase
            if (elapsedTime < 1 && currentSpeed < targetSpeed) {
                currentSpeed = currentSpeed + 0.01; // Increase the speed by 0.01 per second
            }
            // Deceleration phase
            else if (elapsedTime > secondsToDrive - 1 && elapsedTime < secondsToDrive && currentSpeed > 0) {
                currentSpeed = currentSpeed - 0.01; // Decrease the speed by 0.01 per second
            }

            leftFrontDrive.setPower(currentSpeed*leftFrontDriveDirection);
            rightFrontDrive.setPower(currentSpeed*rightFrontDriveDirection);
            leftBackDrive.setPower(currentSpeed*leftBackDriveDirection);
            rightBackDrive.setPower(currentSpeed*rightBackDriveDirection);

            telemetry.addData("Move forward: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        stopMoving();
    }

    private void stopMoving() {
        leftFrontDrive.setPower(0);
        rightFrontDrive.setPower(0);
        leftBackDrive.setPower(0);
        rightBackDrive.setPower(0);
    }

    private void moveForward(double secondsToDrive) {
        moveForward(secondsToDrive, DEFAULT_SPEED);
    }

    private void moveForward(double secondsToDrive, double speedToDrive) {
        acceleration(secondsToDrive, speedToDrive, 1, 1, 1, 1);
    }

    private void moveBackward(double secondsToDrive) {
        moveBackward(secondsToDrive, DEFAULT_SPEED);
    }

    private void moveBackward(double secondsToDrive, double speedToDrive) {
        acceleration(secondsToDrive, speedToDrive, -1, -1,-1,-1);
    }

    private void turnLeft(double secondsToDrive) {
        turnLeft(secondsToDrive, DEFAULT_SPEED);
    }

    private void turnLeft(double secondsToDrive, double speedToDrive) {
        acceleration(secondsToDrive, speedToDrive, -1, 1, -1, 1);
    }

    private void turnRight(double secondsToDrive) {
        turnRight(secondsToDrive, DEFAULT_SPEED);
    }

    private void turnRight(double secondsToDrive, double speedToDrive) {
        acceleration(secondsToDrive, speedToDrive, 1, -1, 1, -1);
    }

    private void strafeLeft(double secondsToDrive) {
        strafeLeft(secondsToDrive, DEFAULT_SPEED);
    }

    private void strafeLeft(double secondsToDrive, double speedToDrive) {
        acceleration(secondsToDrive, speedToDrive, -1, 1, 1, -1);
    }
    private void strafeRight(double secondsToDrive) {
        strafeRight(secondsToDrive, DEFAULT_SPEED);
    }

    private void strafeRight(double secondsToDrive, double speedToDrive) {
        acceleration(secondsToDrive, speedToDrive, 1, -1, -1, 1);
    }

    public double getHeading() {
        YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
        return orientation.getYaw(AngleUnit.DEGREES);
    }

    private void turnLeftToHeading(double targetYaw) {
        turnLeftToHeading(targetYaw, DEFAULT_SPEED);
    }

    // Turn left to desired heading.
    private void turnLeftToHeading(double heading, double speedToDrive) {
        degreesToTurn = heading - getHeading();

    while (opModeIsActive()
                && (Math.abs(degreesToTurn) > HEADING_ERROR_TOLERANCE)
            && (gamepad1.left_stick_y == 0) && (gamepad1.left_stick_x == 0) && (gamepad1.right_stick_x == 0)) {

        degreesToTurn = heading - getHeading();
        if(degreesToTurn < -180) degreesToTurn += 360;
        if(degreesToTurn > 180) degreesToTurn -= 360;

        // Clip the speed to the maximum permitted value
        turnSpeed = Range.clip(degreesToTurn*TURN_SPEED_ADJUSTMENT, -MAX_TURN_SPEED, MAX_TURN_SPEED);
        if(turnSpeed < MIN_TURN_SPEED && turnSpeed >= 0) turnSpeed = MIN_TURN_SPEED;
        if(turnSpeed > -MIN_TURN_SPEED && turnSpeed < 0) turnSpeed = -MIN_TURN_SPEED;

        leftFrontDrive.setPower(-turnSpeed);
        rightFrontDrive.setPower(turnSpeed);
        leftBackDrive.setPower(-turnSpeed);
        rightBackDrive.setPower(turnSpeed);

    }
}

    private void turnRightToHeading(double targetYaw) {
        turnRightToHeading(targetYaw, DEFAULT_SPEED);
    }

    // Turn right to desired heading.
    private void turnRightToHeading(double heading, double speedToDrive) {
        degreesToTurn = heading - getHeading();

        while (opModeIsActive()
                && (Math.abs(degreesToTurn) > HEADING_ERROR_TOLERANCE)
                && (gamepad1.left_stick_y == 0) && (gamepad1.left_stick_x == 0) && (gamepad1.right_stick_x == 0)) {

            degreesToTurn = heading - getHeading();
            if(degreesToTurn < -180) degreesToTurn += 360;
            if(degreesToTurn > 180) degreesToTurn -= 360;

            // Clip the speed to the maximum permitted value
            turnSpeed = Range.clip(degreesToTurn*TURN_SPEED_ADJUSTMENT, -MAX_TURN_SPEED, MAX_TURN_SPEED);
            if(turnSpeed < MIN_TURN_SPEED && turnSpeed >= 0) turnSpeed = MIN_TURN_SPEED;
            if(turnSpeed > -MIN_TURN_SPEED && turnSpeed < 0) turnSpeed = -MIN_TURN_SPEED;

            leftFrontDrive.setPower(turnSpeed);
            rightFrontDrive.setPower(-turnSpeed);
            leftBackDrive.setPower(turnSpeed);
            rightBackDrive.setPower(-turnSpeed);

        }
    }


}

//NC TELEMETRY TRANSFER