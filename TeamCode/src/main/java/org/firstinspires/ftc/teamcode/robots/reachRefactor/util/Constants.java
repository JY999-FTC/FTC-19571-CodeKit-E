package org.firstinspires.ftc.teamcode.robots.reachRefactor.util;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

@Config(value = "FFConstants")
public class Constants {

    //----------------------------------------------------------------------------------------------
    // Physical Constants
    //----------------------------------------------------------------------------------------------

    // distance measurements
    public static final double MIN_CHASSIS_LENGTH = 14.330708976377952;
    public static final double MAX_CHASSIS_LENGTH = 35.4331;
    public static final double TRACK_WIDTH = 12.132362205;
    public static final double DISTANCE_SENSOR_TO_FRONT_AXLE = 2.755906;
    public static final double DISTANCE_TARGET_TO_BACK_WHEEL = 8.75;
    public static final double SHOULDER_TO_ELBOW = 14.031496;
    public static final double ELBOW_TO_WRIST = 11.0236;
    public static final double SHIPPING_HUB_HEIGHT = 20.25;
    public static final double ROBOT_HEIGHT = 8;

    // constraints
    public static double SWIVEL_TICKS_PER_REVOLUTION = 1740;
    public static double TICKS_PER_INCH = 31;
    public static double SWERVE_TICKS_PER_INCH = 31;

    //----------------------------------------------------------------------------------------------
    // Control Constants
    //----------------------------------------------------------------------------------------------

    public static double EPSILON = 1e-6; // small value used for the approximately equal calculation in MathUtils
    public static double TRIGGER_DEADZONE = 0.2; // gamepad trigger values below this threshold will be ignored
    public static double JOYSTICK_DEADZONE = 0.05;

    public static double MAX_VEL = 30;
    public static double MAX_ACCEL = 10;
    public static double MAX_ANG_VEL = Math.toRadians(120);
    public static double MAX_ANG_ACCEL = Math.toRadians(60);

    public static final double TICKS_PER_REV = 1120;
    public static final double MAX_RPM = 150;
    public static boolean USE_CUSTOM_VELOCITY_PID = true;
    public static PIDFCoefficients MOTOR_VELOCITY_PID = new PIDFCoefficients(10, 3, 0,
            getMotorVelocityF(MAX_RPM / 60 * TICKS_PER_REV));
    public static PIDFCoefficients SWERVE_VELOCITY_PID = new PIDFCoefficients(10, 3, 0,
            getMotorVelocityF(MAX_RPM / 60 * TICKS_PER_REV));

    //----------------------------------------------------------------------------------------------
    // Simulation
    //----------------------------------------------------------------------------------------------
    public static boolean USE_MOTOR_SMOOTHING = false;

    //----------------------------------------------------------------------------------------------
    // Enums
    //----------------------------------------------------------------------------------------------
    public enum Alliance {
        RED(1), BLUE(-1);

        private final int mod;

        Alliance(int mod) {
            this.mod = mod;
        }

        public int getMod() {
            return mod;
        }
    }

    public enum Position {
        START_RED_UP(new Pose2d(12, -72, Math.toRadians(270))),
        START_RED_DOWN(new Pose2d(-36, -72, Math.toRadians(270))),
        START_BLUE_UP(new Pose2d(12, 72, Math.toRadians(90))),
        START_BLUE_DOWN(new Pose2d(-36, 72, Math.toRadians(90))),

        RED_SHIPPING_HUB(new Pose2d(-12, -24)),
        BLUE_SHIPPING_HUB(new Pose2d(-12, 24));

        private final Pose2d pose;

        Position(Pose2d pose) {
            this.pose = pose;
        }

        public Pose2d getPose() {
            return pose;
        }
    }

    public static double encoderTicksToInches(double ticks) {
        return ticks / TICKS_PER_INCH;
    }

    public static double inchesToEncoderTicks(double inches) {
        return inches * TICKS_PER_INCH;
    }

    public static double inchesToSwerveEncoderTicks(double inches) {
        return inches * SWERVE_TICKS_PER_INCH;
    }

    public static double getMotorVelocityF(double ticksPerSecond) {
        return 32767 / ticksPerSecond;
    }
}
