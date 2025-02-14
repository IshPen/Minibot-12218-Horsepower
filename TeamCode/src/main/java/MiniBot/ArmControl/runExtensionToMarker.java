package MiniBot.ArmControl;

import static MiniBot.ArmControl.ColorSensorControl.armColorFunctions.detectColor;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import java.util.*;

import MiniBot.ButtonHelper;

@TeleOp(name="Run Extension To Marker", group="ARM")
public class runExtensionToMarker extends OpMode {
    DcMotorEx extensionMotor1, extensionMotor2;
    private ColorSensor colorSensor;

    private final String[] colorOrder = {"Blue", "White", "Red"};
    private String currentColor = "Blue";
    private String targetColor = "None";
    private boolean moving = false;
    ButtonHelper gamepad1Helper;

    @Override
    public void init() {
        extensionMotor1 = hardwareMap.get(DcMotorEx.class, "ex1");
        extensionMotor2 = hardwareMap.get(DcMotorEx.class, "ex2");
        colorSensor = hardwareMap.get(RevColorSensorV3.class, "armColorSensor");
        gamepad1Helper = new ButtonHelper(gamepad1);
    }

    @Override
    public void loop() {
        int red = colorSensor.red();
        int green = colorSensor.green();
        int blue = colorSensor.blue();

        String detectedColor = detectColor(red, green, blue);

        if (!detectedColor.equals("None")) {
            currentColor = detectedColor;
        }


        telemetry.addData("Detected Color", detectedColor);
        telemetry.addData("Current Color", currentColor);
        telemetry.addData("Target Color", targetColor);
        telemetry.addData("\nRed", red);
        telemetry.addData("Green", green);
        telemetry.addData("Blue", blue);

        telemetry.addData("\nIndex of Current: ", findIndex(colorOrder, currentColor));

        // Check if we need to move
        if (moving && currentColor.equals(targetColor)) {
            stopMotors();
            moving = false;
        }

        if (gamepad1Helper.isButtonJustPressed("y")) {
            moveToColor("Blue");
        }
        else if (gamepad1Helper.isButtonJustPressed("b")) {
            moveToColor("White");
        }
        else if (gamepad1Helper.isButtonJustPressed("a")) {
            moveToColor("Red");
        }

        gamepad1Helper.update();
        telemetry.update();
    }

    public void moveToColor(String target) {
        targetColor = target;
        int currentIndex = getColorIndex(currentColor);
        int targetIndex = getColorIndex(targetColor);

        if (currentIndex == -1 || targetIndex == -1) return;

        double mp = -0.5;
        double power = -((targetIndex > currentIndex) ? mp : -mp);
        extensionMotor1.setPower(power);
        extensionMotor2.setPower(power);
        moving = true;
    }

    private void stopMotors() {
        extensionMotor1.setPower(0);
        extensionMotor2.setPower(0);
    }

    private int getColorIndex(String color) {
        for (int i = 0; i < colorOrder.length; i++) {
            if (colorOrder[i].equals(color)) return i;
        }
        return -1;
    }

    public static int findIndex(String a[], String t)
    {
        if (a == null)
            return -1;

        int len = a.length;
        int i = 0;

        // traverse in the array
        while (i < len) {

            // if the i-th element is t
            // then return the index
            if (a[i] == t) {
                return i;
            }
            else {
                i = i + 1;
            }
        }

        return -1;
    }
}
