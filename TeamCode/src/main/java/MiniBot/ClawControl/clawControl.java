package MiniBot.ClawControl;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name="claw control", group = "claw")
public class clawControl extends OpMode {

    Servo clawWrist, clawWristOld, clawRotate, clawLeft, clawRight;
    double currentClawRotation = 0.5;
    @Override
    public void init() {
        clawWristOld = hardwareMap.get(Servo.class, "servoCH5");
        clawWrist = hardwareMap.get(Servo.class, "servoCH3");
        clawRotate = hardwareMap.get(Servo.class, "servoCH1");
        clawLeft = hardwareMap.get(Servo.class, "servoCH2");
        clawRight = hardwareMap.get(Servo.class, "servoCH4");
    }

    @Override
    public void loop() {
        clawWrist.setPosition(gamepad2.left_stick_y+0.5);
        currentClawRotation = Math.max(0, Math.min(1, currentClawRotation + (gamepad2.right_trigger - gamepad2.left_trigger) * 0.005));
        clawRotate.setPosition(currentClawRotation);
        if (gamepad2.right_bumper) {
            closeClaw();
        }
        if (gamepad2.left_bumper) {
            openClaw();
        }
        telemetry.addData("Claw Rotate Position", clawRotate.getPosition());
        telemetry.addData("Sim Rotate Position", currentClawRotation);
        telemetry.addData("Actual Wrist Position", clawWrist.getPosition());
    }
    void openClaw() {
        clawLeft.setPosition(0.84);
        clawRight.setPosition(0.16);
    }
    void closeClaw() {
        clawLeft.setPosition(1);
        clawRight.setPosition(0);
    }
}
