package MiniBot.ArmControl;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name=".Complete Arm Control", group="ARM")
public class completeArmAndClawControl extends OpMode {
    DcMotorEx extensionMotor1, extensionMotor2;
    DcMotorEx armRotationMotor;

    Servo clawWrist, dummyWristForAllen, clawRotate, clawLeft, clawRight;
    double currentClawRotation = 0.5;

    @Override
    public void init() {
        extensionMotor1 = hardwareMap.get(DcMotorEx.class,"ex1");
        extensionMotor2 = hardwareMap.get(DcMotorEx.class,"ex2");
        armRotationMotor = hardwareMap.get(DcMotorEx.class,"ArmRotation");
        armRotationMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        clawWrist = hardwareMap.get(Servo.class, "servoCH1");
        dummyWristForAllen = hardwareMap.get(Servo.class, "servoCH2");
        clawRotate = hardwareMap.get(Servo.class, "servoCH3");
        clawLeft = hardwareMap.get(Servo.class, "servoCH4");
        clawRight = hardwareMap.get(Servo.class, "servoCH5");

    }

    @Override
    public void loop() {
        telemetry.addData("ExtensionPosition1: ", extensionMotor1.getCurrentPosition());
        telemetry.addData("ExtensionPosition2: ", extensionMotor2.getCurrentPosition());
        float power = gamepad2.right_trigger - gamepad2.left_trigger;
        if (power != 0) {
            armRotationMotor.setTargetPosition(armRotationMotor.getCurrentPosition());
            armRotationMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            armRotationMotor.setPower(1);

            extensionMotor1.setPower(power);
            extensionMotor2.setPower(power);
        }
        else {
            armRotationMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            armRotationMotor.setPower(gamepad2.left_stick_y);
        }
        telemetry.addData("Power != 0: ", power!=0);

        clawWrist.setPosition(gamepad2.left_stick_y);
        clawRotate.setPosition(currentClawRotation);
        currentClawRotation = Math.max(0, Math.min(1, currentClawRotation + (gamepad2.right_trigger - gamepad2.left_trigger) * 0.005));
        if (gamepad2.right_bumper) {
            closeClaw();
        }
        if (gamepad2.left_bumper) {
            openClaw();
        }
        telemetry.addData("Claw Wrist Position", clawWrist.getPosition());

        telemetry.update();
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