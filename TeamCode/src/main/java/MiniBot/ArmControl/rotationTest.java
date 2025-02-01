package MiniBot.ArmControl;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp(name="Rotation Test", group="ARM")
public class rotationTest extends OpMode {
    DcMotorEx armRotationMotor;
    @Override
    public void init() {
        armRotationMotor = hardwareMap.get(DcMotorEx.class,"ArmRotation");
    }

    @Override
    public void loop() {
        armRotationMotor.setPower(gamepad2.right_trigger - gamepad2.left_trigger);
        telemetry.addData("Arm Set Power: ", gamepad2.right_trigger - gamepad2.left_trigger);
        telemetry.addData("Arm Rotation Position: ", armRotationMotor.getCurrentPosition());
        telemetry.update();
    }
}