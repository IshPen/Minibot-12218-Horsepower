package MiniBot.ArmControl;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp(name="Extension Test", group="ARM")
public class extensionTest extends OpMode {
    DcMotorEx extensionMotor1, extensionMotor2;
    @Override
    public void init() {
        extensionMotor1 = hardwareMap.get(DcMotorEx.class,"ex1");
        extensionMotor2 = hardwareMap.get(DcMotorEx.class,"ex2");
    }

    @Override
    public void loop() {
        telemetry.addData("ExtensionPosition1: ", extensionMotor1.getCurrentPosition());
        telemetry.addData("ExtensionPosition2: ", extensionMotor2.getCurrentPosition());
        float power = gamepad1.right_trigger - gamepad1.left_trigger;
        extensionMotor1.setPower(power);
        extensionMotor2.setPower(power);
        telemetry.update();
    }
}