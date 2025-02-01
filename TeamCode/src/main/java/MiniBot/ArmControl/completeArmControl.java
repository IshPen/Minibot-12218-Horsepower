package MiniBot.ArmControl;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp(name="Arm Test", group="ARM")
public class completeArmControl extends OpMode {
    DcMotorEx extensionMotor1, extensionMotor2;
    DcMotorEx armRotationMotor;

    @Override
    public void init() {
        extensionMotor1 = hardwareMap.get(DcMotorEx.class,"ex1");
        extensionMotor2 = hardwareMap.get(DcMotorEx.class,"ex2");
        armRotationMotor = hardwareMap.get(DcMotorEx.class,"ArmRotation");
        armRotationMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    public void loop() {
        telemetry.addData("ExtensionPosition1: ", extensionMotor1.getCurrentPosition());
        telemetry.addData("ExtensionPosition2: ", extensionMotor2.getCurrentPosition());
        float power = gamepad1.right_trigger - gamepad1.left_trigger;
        if (power != 0) {
            armRotationMotor.setTargetPosition(armRotationMotor.getCurrentPosition());
            armRotationMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            armRotationMotor.setPower(1);

            extensionMotor1.setPower(power);
            extensionMotor2.setPower(power);
        }
        else {
            armRotationMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            armRotationMotor.setPower(gamepad1.left_stick_y);
        }
        telemetry.addData("Power != 0: ", power!=0);

        telemetry.update();
    }

}