package org.firstinspires.ftc.teamcode;

import android.graphics.Path;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.drivetrain;
import org.firstinspires.ftc.teamcode.GamepadStates;

@TeleOp(name="Teleop", group = "Teleop")
public class teleop extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {

        double speed = .5;

         Limelight3A limelight;


        drivetrain Drive = new drivetrain();
        Limelight Lime = new Limelight();

        GamepadStates newGamePad1 = new GamepadStates(gamepad1);
        GamepadStates newGamePad2 = new GamepadStates(gamepad2);

        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        telemetry.setMsTransmissionInterval(11);


        limelight.pipelineSwitch(0);

        limelight.start();

        Drive.init(this);
        Lime.init();

        waitForStart();

        while(opModeIsActive()) {
            newGamePad1.updateState();
            newGamePad2.updateState();

            if (gamepad1.left_stick_x > .4) {
                Drive.strafeRight(speed);
            } else if (gamepad1.left_stick_x < -.4) {
                Drive.strafeLeft(speed);
            } else if (gamepad1.left_stick_y < -.4) {
                Drive.forward(speed);
            } else if (gamepad1.left_stick_y > .4) {
                Drive.backward(speed);
            } else if (gamepad1.right_stick_x > .4) {
                Drive.turnRight(speed);
            } else if (gamepad1.right_stick_x < -.4) {
                Drive.turnLeft(speed);
            }

            if (newGamePad1.right_bumper.released) {
                if (speed < 1) {
                    speed += .1;
                }
            } else if (newGamePad1.left_bumper.released) {
                if (speed > 0) {
                    speed -= .1;
                }
            }

            LLResult result = limelight.getLatestResult();
            if (result != null) {
                if (result.isValid()) {
                    Pose3D botpose = result.getBotpose();
                    telemetry.addData("tx", result.getTx());
                    telemetry.addData("ty", result.getTy());
                    telemetry.addData("Botpose", botpose.toString());
                    telemetry.update();
                }
            }
        }
    }
}