package org.nknsd.robotics.team.autoSteps;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.nknsd.robotics.framework.NKNAutoStep;
import org.nknsd.robotics.team.autonomous.AutoSkeleton;
import org.nknsd.robotics.team.components.IntakeSpinnerHandler;

import java.util.concurrent.TimeUnit;

public class AutoStepChangeMaxSpeed implements NKNAutoStep {
    AutoSkeleton autoSkeleton;
    private final double speed;

    public AutoStepChangeMaxSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public void link(AutoSkeleton autoSkeleton) {
        this.autoSkeleton = autoSkeleton;

    }

    public void begin(ElapsedTime runtime, Telemetry telemetry) {
        autoSkeleton.setMaxSpeed(speed);
    }

    @Override
    public void run(Telemetry telemetry, ElapsedTime runtime) {}

    @Override
    public boolean isDone(ElapsedTime runtime) {
        return true;
    }

    @Override
    public String getName() {
        return "Set drivetrain to " + speed + " power. You should not see this message.";
    }
}
