package MiniBot.ArmControl.ColorSensorControl;

import static MiniBot.ArmControl.ColorSensorControl.armColorFunctions.detectColor;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;

@Autonomous(name = "Discrete Color Sensing ", group = "color")
public class returnDiscreteColors extends OpMode {
    private ColorSensor colorSensor;

    @Override
    public void init() {
        colorSensor = hardwareMap.get(RevColorSensorV3.class, "armColorSensor");
    }

    @Override
    public void loop() {
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
