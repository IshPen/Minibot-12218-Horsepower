package MiniBot.ClawControl;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name="claw limits", group = "claw")
public class clawLimits extends OpMode {
    Servo controlledServo;
    @Override
    public void init() {
        controlledServo = hardwareMap.get(Servo.class, "servoCH2");
    }

    @Override
    public void loop() {
        controlledServo.setPosition(gamepad1.left_stick_y);
        telemetry.addData("G1 Left Stick Y: ", gamepad1.left_stick_y);
        telemetry.update();
    }
}
