package org.nknsd.robotics.team.autonomous;

import org.nknsd.robotics.framework.NKNAutoStep;
import org.nknsd.robotics.framework.NKNComponent;
import org.nknsd.robotics.framework.NKNProgram;
import org.nknsd.robotics.team.autoSteps.AutoStepAbsoluteControl;
import org.nknsd.robotics.team.autoSteps.AutoStepChangeMaxSpeed;
import org.nknsd.robotics.team.autoSteps.AutoStepExtendArm;
import org.nknsd.robotics.team.autoSteps.AutoStepMove;
import org.nknsd.robotics.team.autoSteps.AutoStepMoveNRotate;
import org.nknsd.robotics.team.autoSteps.AutoStepRelativeMove;
import org.nknsd.robotics.team.autoSteps.AutoStepRotateArm;
import org.nknsd.robotics.team.autoSteps.AutoStepServo;
import org.nknsd.robotics.team.autoSteps.AutoStepSleep;
import org.nknsd.robotics.team.components.ExtensionHandler;
import org.nknsd.robotics.team.components.FlowSensorHandler;
import org.nknsd.robotics.team.components.IMUComponent;
import org.nknsd.robotics.team.components.IntakeSpinnerHandler;
import org.nknsd.robotics.team.components.PotentiometerHandler;
import org.nknsd.robotics.team.components.RotationHandler;
import org.nknsd.robotics.team.components.WheelHandler;
import org.nknsd.robotics.team.components.autonomous.AutoHeart;

import java.util.LinkedList;
import java.util.List;

public class BasketAuto extends NKNProgram {
    @Override
    public void createComponents(List<NKNComponent> components, List<NKNComponent> telemetryEnabled) {
        // Step List
        List<NKNAutoStep> stepList = new LinkedList<NKNAutoStep>();


        // Core mover
        AutoSkeleton autoSkeleton = new AutoSkeleton(0.8, 0.2, 1.5);

        AutoHeart autoHeart = new AutoHeart(stepList);
        components.add(autoHeart);
        telemetryEnabled.add(autoHeart);


        // Sensors
        FlowSensorHandler flowSensorHandler = new FlowSensorHandler();
        components.add(flowSensorHandler);
        telemetryEnabled.add(flowSensorHandler);

        IMUComponent imuComponent = new IMUComponent();
        components.add(imuComponent);
        //telemetryEnabled.add(imuComponent);

        PotentiometerHandler potentiometerHandler = new PotentiometerHandler();
        components.add(potentiometerHandler);


        // Wheel Handler
        WheelHandler wheelHandler = new WheelHandler();
        components.add(wheelHandler);


        // Arm Stuff
        RotationHandler rotationHandler = new RotationHandler ();
        components.add(rotationHandler);

        ExtensionHandler extensionHandler = new ExtensionHandler();
        components.add(extensionHandler);

        IntakeSpinnerHandler intakeSpinnerHandler = new IntakeSpinnerHandler();
        components.add(intakeSpinnerHandler);


        // Linking
        rotationHandler.link(potentiometerHandler, extensionHandler);
        extensionHandler.link(rotationHandler);

        autoSkeleton.link(wheelHandler, rotationHandler, extensionHandler, intakeSpinnerHandler, flowSensorHandler, imuComponent);
        assembleList(stepList, autoHeart, autoSkeleton);
    }

    private void assembleList(List<NKNAutoStep> stepList, AutoHeart autoHeart, AutoSkeleton autoSkeleton) {
        // Declare steps
        AutoStepSleep sleep = new AutoStepSleep(375);

        AutoStepMove moveSlightForward = new AutoStepMove(0, 0.2);
        AutoStepAbsoluteControl orientToBasket = new AutoStepAbsoluteControl(-0.79, 0.34, -135);
        AutoStepRelativeMove backAwayFromBasket = new AutoStepRelativeMove(0, -.35, 200);
        AutoStepRelativeMove slightlyBackAway = new AutoStepRelativeMove(0, -.3, 100);

        AutoStepAbsoluteControl pickUpFirstYellow = new AutoStepAbsoluteControl(0.4113, 1.28, -68.3);
        AutoStepRelativeMove moveToPickup = new AutoStepRelativeMove(0, 0.3, 400);

        AutoStepAbsoluteControl pickUpSecondYellow = new AutoStepAbsoluteControl(-0.0716, 1.6, -90);

        AutoStepAbsoluteControl alignToPark = new AutoStepAbsoluteControl(-0.05, 2.2, 90);
        AutoStepMove driveInToPark = new AutoStepMove(0.58, 0);


        AutoStepRotateArm rotateToHigh = new AutoStepRotateArm(RotationHandler.RotationPositions.HIGH);
        AutoStepRotateArm rotateToPickup = new AutoStepRotateArm(RotationHandler.RotationPositions.PICKUP);
        AutoStepRotateArm rotateToRest = new AutoStepRotateArm(RotationHandler.RotationPositions.RESTING);
        AutoStepRotateArm rotateToPrepickup = new AutoStepRotateArm(RotationHandler.RotationPositions.PREPICKUP);

        AutoStepExtendArm extendToHigh = new AutoStepExtendArm(ExtensionHandler.ExtensionPositions.HIGH_BASKET);
        AutoStepExtendArm retract = new AutoStepExtendArm(ExtensionHandler.ExtensionPositions.RESTING);

        AutoStepServo releaseBlock = new AutoStepServo(IntakeSpinnerHandler.HandStates.RELEASE, 1200);
        AutoStepServo gripBlock = new AutoStepServo(IntakeSpinnerHandler.HandStates.GRIP, 400);
        AutoStepServo neutralServo = new AutoStepServo(IntakeSpinnerHandler.HandStates.REST, 0);

        AutoStepChangeMaxSpeed slowSpeed = new AutoStepChangeMaxSpeed(0.6);
        AutoStepChangeMaxSpeed normalSpeed = new AutoStepChangeMaxSpeed(0.8);

        // Put away first block
        stepList.add(moveSlightForward);
        stepList.add(orientToBasket);
        stepList.add(backAwayFromBasket);
        stepList.add(rotateToHigh);
        stepList.add(extendToHigh);
        stepList.add(releaseBlock);
        stepList.add(slightlyBackAway);
        stepList.add(retract);

        // Get second block
        stepList.add(slowSpeed);
        stepList.add(pickUpFirstYellow);
        stepList.add(gripBlock);
        stepList.add(rotateToPickup);
        stepList.add(moveToPickup);
        stepList.add(sleep);
        stepList.add(rotateToRest);

        // Place second block
        stepList.add(normalSpeed);
        stepList.add(orientToBasket);
        stepList.add(rotateToHigh);
        stepList.add(extendToHigh);
        stepList.add(releaseBlock);
        stepList.add(backAwayFromBasket);
        stepList.add(retract);

        // Get third block
        stepList.add(slowSpeed);
        stepList.add(pickUpSecondYellow);
        stepList.add(gripBlock);
        stepList.add(rotateToPickup);
        stepList.add(moveToPickup);
        stepList.add(sleep);
        stepList.add(rotateToRest);

        // Place third block
        stepList.add(normalSpeed);
        stepList.add(orientToBasket);
        stepList.add(rotateToHigh);
        stepList.add(extendToHigh);
        stepList.add(releaseBlock);
        stepList.add(backAwayFromBasket);
        stepList.add(retract);


        autoHeart.linkSteps(stepList, autoSkeleton);
    }
}
