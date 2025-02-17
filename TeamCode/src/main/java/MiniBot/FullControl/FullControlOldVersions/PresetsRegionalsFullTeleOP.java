package MiniBot.FullControl.FullControlOldVersions;

import static com.pedropathing.follower.FollowerConstants.leftFrontMotorDirection;
import static com.pedropathing.follower.FollowerConstants.leftFrontMotorName;
import static com.pedropathing.follower.FollowerConstants.leftRearMotorDirection;
import static com.pedropathing.follower.FollowerConstants.leftRearMotorName;
import static com.pedropathing.follower.FollowerConstants.rightFrontMotorDirection;
import static com.pedropathing.follower.FollowerConstants.rightFrontMotorName;
import static com.pedropathing.follower.FollowerConstants.rightRearMotorDirection;
import static com.pedropathing.follower.FollowerConstants.rightRearMotorName;

import static MiniBot.ArmControl.ColorSensorControl.armColorFunctions.detectColor;

import com.pedropathing.util.Constants;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import java.util.Arrays;
import java.util.List;

import MiniBot.ButtonHelper;
import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;

//"Presets -- Regionals Full TeleOP"
public class PresetsRegionalsFullTeleOP extends OpMode {
    private DcMotorEx leftFront;
    private DcMotorEx leftRear;
    private DcMotorEx rightFront;
    private DcMotorEx rightRear;
    private List<DcMotorEx> motors;

    DcMotorEx extensionMotor1, extensionMotor2;
    DcMotorEx armRotationMotor;

    Servo clawWrist, clawWristOld, clawRotate, clawLeft, clawRight;
    double currentClawRotation = 0.5;

    /// FSM STATES ///
    enum DriverControlState {
        FORWARD_FACING,
        BACKWARD_FACING
    }
    enum RobotState {
        GROUND,
        GRAB_SPEC,
        SPEC_SCORING,
        BASKET_SCORING,
        BASE
    }

    // Arm Stuffs
    enum ArmState {
        GROUND_BASE,
        GROUND_CONTROL,
        GRAB_SPEC_FROM_WALL,
        SET_UP_SCORE_HIGH_CHAMBER,
        SCORING_HIGH_CHAMBER,
        SET_UP_SCORE_LOW_CHAMBER,
        SCORING_LOW_CHAMBER,
        SCORE_HIGH_BASKET,
        SCORE_LOW_BASKET
    }
    private ColorSensor colorSensor;

    private final String[] colorOrder = {"Blue", "White", "Red"};
    private String currentColor = "Blue";
    private String targetColor = "None";
    private boolean armMoving = false;

    // ClawStuffs
    enum ClawState {
        GROUND,
        GRAB_SPEC_FROM_WALL,
        SCORING_CHAMBER,
        SCORING_BASKET
    }
    enum ClawPincersState {
        OPEN,
        CLOSE
    }

    /// FSM INITIALIZATIONS ///
    DriverControlState driverControlState = DriverControlState.FORWARD_FACING;
    RobotState robotState = RobotState.GROUND;
    ArmState armState = ArmState.GROUND_BASE;
    ClawState clawState = ClawState.GROUND;
    ClawPincersState clawPincersState = ClawPincersState.CLOSE;

    ButtonHelper gamepad1ButtonHelper, gamepad2ButtonHelper;
    @Override
    public void init() {
        Constants.setConstants(FConstants.class, LConstants.class);

        leftFront = hardwareMap.get(DcMotorEx.class, leftFrontMotorName);
        leftRear = hardwareMap.get(DcMotorEx.class, leftRearMotorName);
        rightRear = hardwareMap.get(DcMotorEx.class, rightRearMotorName);
        rightFront = hardwareMap.get(DcMotorEx.class, rightFrontMotorName);
        leftFront.setDirection(leftFrontMotorDirection);
        leftRear.setDirection(leftRearMotorDirection);
        rightFront.setDirection(rightFrontMotorDirection);
        rightRear.setDirection(rightRearMotorDirection);

        motors = Arrays.asList(leftFront, leftRear, rightFront, rightRear);

        for (DcMotorEx motor : motors) {
            MotorConfigurationType motorConfigurationType = motor.getMotorType().clone();
            motorConfigurationType.setAchieveableMaxRPMFraction(1.0);
            motor.setMotorType(motorConfigurationType);
        }


        extensionMotor1 = hardwareMap.get(DcMotorEx.class,"ex1");
        extensionMotor2 = hardwareMap.get(DcMotorEx.class,"ex2");
        colorSensor = hardwareMap.get(RevColorSensorV3.class, "armColorSensor");
        armRotationMotor = hardwareMap.get(DcMotorEx.class,"ArmRotation");
        armRotationMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        clawWristOld = hardwareMap.get(Servo.class, "servoCH1");
        clawWrist = hardwareMap.get(Servo.class, "servoCH2");
        clawRotate = hardwareMap.get(Servo.class, "servoCH3");
        clawLeft = hardwareMap.get(Servo.class, "servoCH4");
        clawRight = hardwareMap.get(Servo.class, "servoCH5");

        gamepad1ButtonHelper = new ButtonHelper(gamepad1);
        gamepad2ButtonHelper = new ButtonHelper(gamepad2);
    }

    @Override
    public void init_loop() {
        clawPincersState = ClawPincersState.CLOSE;
        closeClaw();
    }
    @Override
    public void loop() {
        /// Update Arm Extension Color State ///
        int red = colorSensor.red();
        int green = colorSensor.green();
        int blue = colorSensor.blue();
        String detectedColor = detectColor(red, green, blue);
        if (!detectedColor.equals("None")) {currentColor = detectedColor;}

        if (armMoving && currentColor.equals(targetColor)) {
            stopMotors();
            armMoving = false;
        }

        /// ROBOT STATE FSM ///
        switch (robotState) {
            case GROUND:
                // arm to base
                rotateArmToEncoderPosition(40);
                armState = ArmState.GROUND_BASE;
                clawState = ClawState.GROUND;
                break;
            case GRAB_SPEC:
                // arm to grab height
                rotateArmToEncoderPosition(400);
                armState = ArmState.GRAB_SPEC_FROM_WALL;
                clawState = ClawState.GRAB_SPEC_FROM_WALL;
                break;
            case SPEC_SCORING:
                // arm to spec high scoring height as default
                rotateArmToEncoderPosition(650);
                armState = ArmState.SET_UP_SCORE_HIGH_CHAMBER;
                clawState = ClawState.SCORING_CHAMBER;
                break;
            case BASKET_SCORING:
                rotateArmToEncoderPosition(1100);
                // arm to high basket scoring height as default
                armState = ArmState.SCORE_HIGH_BASKET;
                clawState = ClawState.SCORING_BASKET;
                break;
            case BASE:
                /// /// Escape Preset Arm States /// ///
                /// Left Stick Controls Rotation ///
                if (Math.abs(gamepad2.left_stick_y) >= 0.1) {
                //    armRotationMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                //    armRotationMotor.setPower(gamepad1.left_stick_y);
                }
                /// Right Stick Controls Extension ///
                if (Math.abs(gamepad2.right_stick_y) >= 0.1) {
                //    extensionMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                //    extensionMotor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

                //    extensionMotor1.setPower(gamepad1.right_stick_y);
                //    extensionMotor2.setPower(gamepad1.right_stick_y);
                }
                break;
        }


        /// CLAW CONTROL ///
        if (gamepad2ButtonHelper.isButtonJustPressed("x")) {
            if (clawPincersState == ClawPincersState.OPEN) {
                clawPincersState = ClawPincersState.CLOSE;
            }
            else if (clawPincersState == ClawPincersState.CLOSE) {
                clawPincersState = ClawPincersState.OPEN;
            }
        } /// Make clawPincerState open or closed
        switch (clawState) {
            case GROUND:
                clawWrist.setPosition(0.15);
                currentClawRotation = Math.max(0, Math.min(1, clawRotate.getPosition() + (gamepad2.right_trigger - gamepad2.left_trigger) * 0.01));
                clawRotate.setPosition(currentClawRotation);
                break;
            case GRAB_SPEC_FROM_WALL:
                clawWrist.setPosition(0.5);
                clawRotate.setPosition(0.5);
                break;
            case SCORING_CHAMBER:
                clawWrist.setPosition(0.0);
                clawRotate.setPosition(0.5);
                break;
            case SCORING_BASKET:
                clawWrist.setPosition(0.0);
                clawRotate.setPosition(0.5);
                break;
        } /// Controlled by the RobotState
        switch (clawPincersState) {
            case OPEN:
                openClaw();
                break;
            case CLOSE:
                closeClaw();
                break;
        }


        /// ARM CONTROL ///
        //if (batteryVoltage > 9.0) {
        //double armExtensionPower = gamepad2.left_stick_y * 0.5;
        //extensionMotor1.setPower(armExtensionPower);
        //extensionMotor2.setPower(armExtensionPower);
        //}
        //else {extensionMotor1.setPower(0);extensionMotor2.setPower(0);}
        if (gamepad2ButtonHelper.isButtonJustPressed("dpad_up")) {
            switch (robotState) {
                case GROUND: case BASE:
                    robotState = RobotState.GRAB_SPEC;
                    moveToColor("Blue");
                    break;
                case GRAB_SPEC:
                    robotState = RobotState.SPEC_SCORING;
                    moveToColor("White");
                    break;
                case SPEC_SCORING:
                    robotState = RobotState.BASKET_SCORING;
                    moveToColor("Red");
                    break;
            }
        }
        else if (gamepad2ButtonHelper.isButtonJustPressed("dpad_down")) {
            switch (robotState) {
                case BASKET_SCORING:
                    robotState = RobotState.SPEC_SCORING;
                    moveToColor("White");
                    break;
                case SPEC_SCORING:
                    robotState = RobotState.GRAB_SPEC;
                    moveToColor("Blue");
                    break;
                case GRAB_SPEC:
                    robotState = RobotState.GROUND;
                    moveToColor("Blue");
            }
        }

        if (gamepad2ButtonHelper.isButtonJustPressed("a")) {
            switch (armState) {
                case SET_UP_SCORE_HIGH_CHAMBER:
                    armState = ArmState.SET_UP_SCORE_LOW_CHAMBER;
                    break;
                case SET_UP_SCORE_LOW_CHAMBER:
                    armState = ArmState.SET_UP_SCORE_HIGH_CHAMBER;
                    break;
                case SCORE_HIGH_BASKET:
                    armState = ArmState.SCORE_LOW_BASKET;
                    break;
                case SCORE_LOW_BASKET:
                    armState = ArmState.SCORE_HIGH_BASKET;
                    break;
            }
        }

        if (gamepad2ButtonHelper.isButtonJustPressed("x")) {/*switch (armState) {
                case SET_UP_SCORE_HIGH_CHAMBER:
                    armState = ArmState.SCORING_HIGH_CHAMBER;
                    break;
                case SET_UP_SCORE_LOW_CHAMBER:
                    armState = ArmState.SCORING_LOW_CHAMBER;
                    break;
            }*/}

        /// DRIVETRAIN CONTROL ///
        if (gamepad1ButtonHelper.isButtonJustPressed("right_bumper")) {
            if (driverControlState == DriverControlState.FORWARD_FACING) {
                driverControlState = DriverControlState.BACKWARD_FACING;
            }
            else {
                driverControlState = DriverControlState.FORWARD_FACING;
            }
        }
        double linearSpeedControl = 0.6;
        double headingSpeedControl = 0.3;
        double y = -gamepad1.left_stick_y * linearSpeedControl; // Remember, this is reversed!
        double x = gamepad1.left_stick_x * linearSpeedControl; // this is strafing
        double rx = gamepad1.right_stick_x * headingSpeedControl;

        switch (driverControlState) {
            case FORWARD_FACING:
                x=x;
                y=y;
                break;
            case BACKWARD_FACING:
                x*=-1;
                y*=-1;
                break;
        }
        // Denominator is the largest motor power (absolute value) or 1
        // This ensures all the powers maintain the same ratio, but only when
        // at least one is out of the range [-1, 1]

        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
        double leftFrontPower = (y + x + rx) / denominator;
        double leftRearPower = (y - x + rx) / denominator;
        double rightFrontPower = (y - x - rx) / denominator;
        double rightRearPower = (y + x - rx) / denominator;

        leftFront.setPower(leftFrontPower);
        leftRear.setPower(leftRearPower);
        rightFront.setPower(rightFrontPower);
        rightRear.setPower(rightRearPower);

        /// TELEMETRY FEEDBACK ///
        telemetry.addData("Driving Direction: ", driverControlState);
        telemetry.addData("ROBOT State: ", robotState);
        telemetry.addData("ARM State: ", armState);
        telemetry.addData("CLAW State: ", clawState);
        if (clawPincersState == ClawPincersState.OPEN) {telemetry.addData("Claw State", "\uD83D\uDFE9");} else {telemetry.addData("Claw State", "\uD83D\uDFE5");}
        telemetry.addData("\n-----------------------------\n", "");
        telemetry.addData("Extension Motor Position: ", extensionMotor1.getCurrentPosition());
        telemetry.addData("Extension Motor Power: ", extensionMotor1.getPower() + " " + extensionMotor2.getPower());
        telemetry.addData("Arm Rotation Position: ", armRotationMotor.getCurrentPosition());
        telemetry.addData("Claw Rotate: ", clawRotate.getPosition());
        telemetry.addData("\n-----------------------------\n", "");
        telemetry.addData("Detected Color", detectedColor);
        telemetry.addData("Current Color", currentColor);
        telemetry.addData("Target Color", targetColor);
        telemetry.addData("\nRed", red);
        telemetry.addData("Green", green);
        telemetry.addData("Blue", blue);
        telemetry.addData("Arm Moving: ", armMoving);
        telemetry.update();

        /// BUTTON HELPER UPDATE ///
        gamepad1ButtonHelper.update();
        gamepad2ButtonHelper.update();
    }

    void openClaw() {
        clawLeft.setPosition(0.84);
        clawRight.setPosition(0.3);
    }
    void closeClaw() {
        clawLeft.setPosition(1);
        clawRight.setPosition(0);
    }
    void rotateArmToEncoderPosition(int encoderLocation) {
        armRotationMotor.setTargetPosition(encoderLocation);
        armRotationMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armRotationMotor.setPower(0.7);
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
        armMoving = true;
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

}
