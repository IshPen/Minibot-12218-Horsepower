package MiniBot.ClawControl;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name="claw control", group = "claw")
public class clawControl extends OpMode {

    Servo clawWrist, dummyWristForAllen, clawRotate, clawLeft, clawRight;
    double currentClawRotation = 0.5;
    @Override
    public void init() {
        clawWrist = hardwareMap.get(Servo.class, "servoCH1");
        dummyWristForAllen = hardwareMap.get(Servo.class, "servoCH2");
        clawRotate = hardwareMap.get(Servo.class, "servoCH3");
        clawLeft = hardwareMap.get(Servo.class, "servoCH4");
        clawRight = hardwareMap.get(Servo.class, "servoCH5");
    }

    @Override
    public void loop() {
        clawWrist.setPosition(gamepad2.left_stick_y+0.3);
        clawRotate.setPosition(currentClawRotation);
        currentClawRotation = Math.max(0, Math.min(1, currentClawRotation + (gamepad2.right_trigger - gamepad2.left_trigger) * 0.005));
        if (gamepad2.right_bumper) {
            closeClaw();
        }
        if (gamepad2.left_bumper) {
            openClaw();
        }
        telemetry.addData("Claw Wrist Position", clawWrist.getPosition());
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
