package org.firstinspires.ftc.teamcode.FancyTeleop;

import static org.firstinspires.ftc.teamcode.FancyTeleop.HobbesConstants.CLAW_CLOSED;
import static org.firstinspires.ftc.teamcode.FancyTeleop.HobbesConstants.CLAW_OPEN;
import static org.firstinspires.ftc.teamcode.FancyTeleop.HobbesConstants.EXTENDO_OFFSET;
import static org.firstinspires.ftc.teamcode.FancyTeleop.HobbesConstants.INFINITY;
import static org.firstinspires.ftc.teamcode.FancyTeleop.HobbesConstants.INTAKE_POWER;
import static org.firstinspires.ftc.teamcode.FancyTeleop.HobbesConstants.SLIDES_KP;
import static org.firstinspires.ftc.teamcode.FancyTeleop.HobbesConstants.SLIDES_MAX;
import static org.firstinspires.ftc.teamcode.FancyTeleop.HobbesConstants.SLIDES_MIN;
import static org.firstinspires.ftc.teamcode.FancyTeleop.HobbesConstants.SLIDES_SIGMOID_SCALER;
import static java.lang.Math.E;
import static java.lang.Math.abs;
import static java.lang.Math.pow;

import android.content.Context;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.hardware.lynx.commands.standard.LynxSetDebugLogLevelCommand;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.chassis.Meccanum.Meccanum;
import org.firstinspires.ftc.teamcode.helpers.PID;
import org.firstinspires.ftc.teamcode.robot.Robot;

import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;

@Config
public class Hobbes extends Meccanum implements Robot {
    protected HardwareMap hw = null;

    public MotorSlideThread slidesController = new MotorSlideThread();
    public ServosThread servosController = new ServosThread();

    //public SampleMecanumDrive rr = null;
    DcMotor slides;
    private Servo extendoLeft, extendoRight, extendoArm, extendoWrist, slidesArm, slidesWrist, claw;
    private CRServo intakeRight, intakeLeft;

    // all relative to robot's reference frame with deposit as front


    Telemetry tele = FtcDashboard.getInstance().getTelemetry();

    public void resetImu() {
        this.offset = -imu.getAngularOrientation().firstAngle;
    }
    @Override
    public void init(HardwareMap hardwareMap) {
        super.init(hardwareMap);
        // imu = hardwareMap.get(BNO055IMU.class, "imu");
        // imu.initialize(parameters);
        // angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS);

        // Meccanum Motors Definition and setting prefs
        motorFrontLeft = (DcMotorEx) hardwareMap.dcMotor.get("motorFrontLeft"); // EH1
        motorBackLeft = (DcMotorEx) hardwareMap.dcMotor.get("motorBackLeft"); // EH4
        motorFrontRight = (DcMotorEx) hardwareMap.dcMotor.get("motorFrontRight"); // CH2
        motorBackRight = (DcMotorEx) hardwareMap.dcMotor.get("motorBackRight"); // CH0

        motorBackLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        motorFrontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        // Reverse the left side motors and set behaviors to stop instead of coast
        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        slides = (DcMotorEx) hardwareMap.dcMotor.get("slides"); // EH3

        // LIMITED
        claw = hardwareMap.servo.get("claw");
        extendoLeft = hardwareMap.servo.get("extendoLeft");
        extendoRight = hardwareMap.servo.get("extendoRight");
        extendoArm = hardwareMap.servo.get("extendoArm");
        extendoWrist = hardwareMap.servo.get("extendoWrist");
        slidesArm = hardwareMap.servo.get("slidesArm");
        slidesWrist = hardwareMap.servo.get("slidesWrist");
        // CONTINUOUS
        intakeLeft = hardwareMap.crservo.get("intakeLeft");
        intakeRight = hardwareMap.crservo.get("intakeRight");


        slides.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slides.setDirection(DcMotorSimple.Direction.REVERSE);
        slides.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slides.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


        slidesController.start();

        // define hw as the hardware map for possible access later in this class
        hw = hardwareMap;



        runtime.reset();
    }
    public void motorDriveXYVectors(double xvec, double yvec, double spinvec){
        // this class drives the robot in the direction of vectors from a joystick and a spin value
        // used for teleop mode driving wheels with joysticks


        double y = pow(yvec,1); // Remember, this is reversed!
        double x = pow(xvec * 1.1,1); // Counteract imperfect strafing
        double rx = pow(spinvec,1);


        //denominator is the largest motor power (absolute value) or 1
        //this ensures all the powers maintain the same ratio, but only when
        //at least one is out of the range [-1, 1]
        double denominator = Math.max(abs(y) + abs(x) + abs(rx), 1);
        double frontLeftPower = (y + x + rx) / denominator;
        double backLeftPower = (y - x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backRightPower = (y + x - rx) / denominator;

        motorDrive(frontLeftPower,backLeftPower, frontRightPower, backRightPower);

    }

    public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior zeroPowerBehavior) {
        motorBackLeft.setZeroPowerBehavior(zeroPowerBehavior);
        motorBackRight.setZeroPowerBehavior(zeroPowerBehavior);
        motorFrontRight.setZeroPowerBehavior(zeroPowerBehavior);
        motorFrontLeft.setZeroPowerBehavior(zeroPowerBehavior);
    }

    // macro manager stuff
    public String MACRO_ID = null;
    public boolean MACROING = false;
    public Map<String, HobbesState> macros;
    public ElapsedTime macroTimer = new ElapsedTime();
    public int macroTimeout = INFINITY;
    public void setMacros(Map<String, HobbesState> macros) {
        this.macros = macros;
    }
    public void runMacro(String id) {
        if (macroTimer.milliseconds() < macroTimeout) macroTimeout = INFINITY; // cancel ongoing macro
        MACRO_ID = id;
        MACROING = true;
    }
    public void cancelMacros() {
        MACROING = false;
        macroTimeout = INFINITY;
        //slidesController.setTargeting(false);
    }
    public void tickMacros() {
        if (macroTimer.milliseconds() > macroTimeout) {
            macroTimeout = INFINITY;
            MACROING = true;
        }
        if (MACROING) {
            if (macros.get(MACRO_ID) == null) {
                MACROING = false;
                Objects.requireNonNull(System.console()).printf("ERROR: unknown macro " + MACRO_ID + ".");
                return;
            }
            HobbesState m = macros.get(MACRO_ID);
            if (m.slidesPos != null) slidesController.setTarget(m.slidesPos);
            if (m.extendoPos != null) servosController.setExtendo(m.extendoPos);
            if (m.extendoArmPos != null) servosController.extendoArmPos = m.extendoArmPos;
            if (m.extendoWristPos != null) servosController.extendoWristPos = m.extendoWristPos;
            if (m.slidesArmPos != null) servosController.slidesArmPos = m.slidesArmPos;
            if (m.slidesWristPos != null) servosController.slidesWristPos = m.slidesWristPos;
            if (m.intakeSpeed != null) servosController.intakeSpeed = m.intakeSpeed;
            if (m.clawPos != null) servosController.clawPos = m.clawPos;
            if (m.linkedState != null) {
                macroTimer.reset();
                macroTimeout = m.linkedState.timeout;
                MACRO_ID = m.linkedState.nextState;
            }
            MACROING = false;
        }
    }
    public void tick() {
        failsafeCheck(); // empty
        tickMacros();
        slidesController.slidesTick(); // update slides
        servosController.servosTick(); // update servos
    }
    public void failsafeCheck() {
        /*
        if () { // arm at unsafe rotation relative to other arm

        }
        if () { // claw closed and arm trying to go down to transfer

        }
        if () { // slides wrist at unsafe rotation relative to arm

        }
        if () { // extendo wrist at unsafe rotation relative to arm

        }
        if () { // slides pos at unsafe low height

        }
        if () { // extendo pos at unsafe in place

        }
        if () { // slides pos at unsafe height and extendo pos at unsafe place

        }
         */
    }


    public double slideTar = 0; // target of slide (duh)
    public void resetSlides() {
        slidesController.resetSlideBasePos();
    }

    public class ServosThread {
        public volatile double extendoPos = 0;
        public volatile double intakeSpeed = 0;
        public volatile double slidesArmPos = 0;
        public volatile double slidesWristPos = 0;
        public volatile double extendoArmPos = 0;
        public volatile double extendoWristPos = 0;
        public volatile double clawPos = 0;

        public void servosTick() {
            tele.addData("extendoPos", extendoPos);
            tele.addData("extendoArmPos", extendoArmPos);
            tele.addData("extendoWristPos", extendoWristPos);
            tele.addData("intakeSpeed", intakeSpeed);
            tele.addData("clawPos", clawPos);
            tele.addData("slidesArmPos", slidesArmPos);
            tele.addData("slidesWristPos", slidesWristPos);
            // use for failsafe eventually (have positions in queue and check that each works before setting)
            // check if these get position checks are redundant. (Will a servo try and set position if its already running to position or does it not matter?)
            if (slidesArm.getPosition() != slidesArmPos) slidesArm.setPosition(slidesArmPos);
            if (slidesWrist.getPosition() != slidesWristPos) slidesWrist.setPosition(slidesWristPos);


            //if (extendoArm.getPosition() != extendoArmPos) extendoArm.setPosition(extendoArmPos);
            //if (extendoWrist.getPosition() != extendoWristPos) extendoWrist.setPosition(extendoWristPos);
            extendoArm.setPosition(extendoArmPos);
            extendoWrist.setPosition(extendoWristPos);

            if (claw.getPosition() != clawPos) claw.setPosition(clawPos);

            if (extendoLeft.getPosition() != extendoPos) {
                extendoLeft.setPosition(extendoPos);
                extendoRight.setPosition(extendoLeftToRight(extendoPos));
            }

            if (intakeLeft.getPower() != intakeSpeed) {
                intakeLeft.setPower(intakeSpeed);
                intakeRight.setPower(-intakeSpeed);
            }


        }
        public void spintake(boolean on) {
            intakeSpeed = on ? INTAKE_POWER : 0;
        }
        public void intake(double power) {
            intakeSpeed = power;
        }
        public void setSlidesArmWrist(double armPosition, double wristPosition) {
            slidesArmPos = armPosition;
            slidesWristPos = wristPosition;
        }
        public void setClaw(boolean open) {
            clawPos = open ? CLAW_OPEN : CLAW_CLOSED;
        }
        public void setExtendoArmWrist(double armPosition, double wristPosition) {
            extendoArmPos = armPosition;
            extendoWristPos = wristPosition;
        }
        public void incrementArmWrist(double incrementArm, double incrementWrist) {
            extendoArmPos += incrementArm;
            extendoWristPos += incrementWrist;
        }
        public void setExtendo(double position) { // extendo positions based on left value
            extendoPos = position;
        }
        public void setClawPrecise(double position) {
            clawPos = position;
        }
        public void incrementExtendo(double increment) {
            if ((extendoPos + increment) < 0.58 && (extendoPos + increment) > 0.1) {
                extendoPos += increment;
            }
        }
        public double extendoLeftToRight(double leftPosition) {
            return EXTENDO_OFFSET-leftPosition; // NOT SURE IF THIS IS RIGHT, TEST WITH UNLINKED SERVOS
        }
    }
    public class MotorSlideThread {
        public boolean runToBottom = false;
        public boolean SLIDE_TARGETING = false;
        public double basePos = 0;
        public double pos = 0;
        public double errorThreshold = 20;
        public double derivativeThreshold = 1;

        public double power = 0;

        //public double slideTar = 0;
        public PID slidePID;

        //public double maxHeight = 1000;
        //public double minHeight = 0;

        //public double differenceScalar = 0.0001;
        //public double scaler = 50;
        Telemetry tele = FtcDashboard.getInstance().getTelemetry();
        public void setTele(Telemetry t) {
            tele = t;
        }

        public void start() {
            basePos = slides.getCurrentPosition();

            slidePID = new PID(SLIDES_KP, 0, 0, false);
            tele = FtcDashboard.getInstance().getTelemetry();
        }

        public void slidesTick() {

            slidePID.setConsts(SLIDES_KP, 0, 0);
            slidePID.setTarget(slideTar);
            pos = -(slides.getCurrentPosition() - basePos);

            tele.addData("pos", pos);
            tele.addData("targeting", SLIDE_TARGETING);
            tele.addData("slidetar", slideTar);
            tele.addData("slidep", SLIDES_KP);

            if (pos < SLIDES_MIN-100  && power < 0) {
                SLIDE_TARGETING = true;
                slideTar = SLIDES_MIN-100;
            }
            if (pos > SLIDES_MAX && power > 0) {
                SLIDE_TARGETING = true;
                slideTar = SLIDES_MAX;
            }
            if (SLIDE_TARGETING) {
                power = -slidePID.tick(pos);
                tele.addData("pidpower", power);
            }

            tele.addData("drivingPower", !runToBottom ? minMaxScaler(pos, power) : 0.4);
            tele.update();

            if (!runToBottom) slides.setPower(minMaxScaler(pos, power));
            else slides.setPower(0.4);
        }

        // REWRITE EVENTUALLY AND CLEAN UP PLEASE
        public double minMaxScaler(double x, double power) {
            double p = power * (power > 0 ? ((1.3 * 1/(1+Math.pow(E, -SLIDES_SIGMOID_SCALER*(x-300+SLIDES_MIN)))) - 0.1) : ((1.3 * 1/(1+Math.pow(E, SLIDES_SIGMOID_SCALER*(x+300-SLIDES_MAX)))) - 0.1));
            // uuuuuh
            return p;
        }

        public void driveSlides(double p) {
            //if (p == 0) setTarget(pos); // untested
            tele.addData("ipower", p);
            tele.addData("cpower", power);
            tele.update();
            SLIDE_TARGETING = false;
            power = -p;
        }
        public void setTargeting(boolean targeting) {
            SLIDE_TARGETING = targeting;
        }

        public void setTarget(double tar) {
            slideTar = tar;
            SLIDE_TARGETING = true;
        }
        public void resetSlideBasePos() {
            slides.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            slides.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            basePos = slides.getCurrentPosition();
        }
        public boolean isBusy() {

            return slidePID.getDerivative() < derivativeThreshold && abs(pos - slideTar) < errorThreshold;
            //                                                       could get proportion (^) from pid but dont want to
        }

    }

    public void playSound(String filename){
        // play a sound
        // doesnt work but would be really fun :(

        int startupID = hw.appContext.getResources().getIdentifier(filename, "raw", hw.appContext.getPackageName());
        Context appContext = hw.appContext;
        SoundPlayer.getInstance().startPlaying(appContext, startupID);
    }
}
