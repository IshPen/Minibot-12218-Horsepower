package MiniBot.FullControl;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp(name="Reset All Subsystem Encoders", group=".")
public class resetAllSubsystemEncoders extends OpMode {

    DcMotorEx extensionMotor1, extensionMotor2;
    DcMotorEx armRotationMotor;

    @Override
    public void init() {
        extensionMotor1 = hardwareMap.get(DcMotorEx.class,"ex1");
        extensionMotor2 = hardwareMap.get(DcMotorEx.class,"ex2");
        armRotationMotor = hardwareMap.get(DcMotorEx.class,"ArmRotation");
    }

    @Override
    public void loop() {
        if (gamepad1.a) {
            extensionMotor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            extensionMotor2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            armRotationMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        telemetry.addData("Ex1 Position: ", extensionMotor1.getCurrentPosition());
        telemetry.addData("Ex2 Position: ", extensionMotor2.getCurrentPosition());
        telemetry.addData("Rotation Position: ", armRotationMotor.getCurrentPosition());
        telemetry.addData("", "Press A to reset encoders");
        telemetry.update();
    }
}
