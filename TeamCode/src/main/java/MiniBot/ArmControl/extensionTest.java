package MiniBot.ArmControl;

import static MiniBot.ArmControl.ColorSensorControl.armColorFunctions.detectColor;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp(name="Extension Test", group="ARM")
public class extensionTest extends OpMode {
    DcMotorEx extensionMotor1, extensionMotor2;
    private ColorSensor colorSensor;

    @Override
    public void init() {
        extensionMotor1 = hardwareMap.get(DcMotorEx.class,"ex1");
        extensionMotor2 = hardwareMap.get(DcMotorEx.class,"ex2");
        colorSensor = hardwareMap.get(RevColorSensorV3.class, "armColorSensor");

    }

    @Override
    public void loop() {
        telemetry.addData("ExtensionPosition1: ", extensionMotor1.getCurrentPosition());
        telemetry.addData("ExtensionPosition2: ", extensionMotor2.getCurrentPosition());
        float power = gamepad1.right_trigger - gamepad1.left_trigger;
        extensionMotor1.setPower(power);
        extensionMotor2.setPower(power);

        int red = colorSensor.red();
        int green = colorSensor.green();
        int blue = colorSensor.blue();

        String detectedColor = detectColor(red, green, blue);

        telemetry.addData("Red", red);
        telemetry.addData("Green", green);
        telemetry.addData("Blue", blue);
        telemetry.addData("Detected Color", detectedColor);
        telemetry.update();
    }
}