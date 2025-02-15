package MiniBot.FullControl;

import static com.pedropathing.follower.FollowerConstants.*;

import com.pedropathing.util.Constants;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
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
@TeleOp(name = "Regionals Full TeleOP", group=".")
public class RegionalsFullTeleOP extends OpMode {
    private DcMotorEx leftFront;
    private DcMotorEx leftRear;
    private DcMotorEx rightFront;
    private DcMotorEx rightRear;
    private List<DcMotorEx> motors;

    DcMotorEx extensionMotor1, extensionMotor2;
    DcMotorEx armRotationMotor;

    Servo clawWrist, clawWristOld, clawRotate, clawLeft, clawRight;
    double currentClawRotation = 0.5;

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
        extensionMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        extensionMotor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armRotationMotor = hardwareMap.get(DcMotorEx.class,"ArmRotation");
        armRotationMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        clawWristOld = hardwareMap.get(Servo.class, "servoCH5");
        clawWrist = hardwareMap.get(Servo.class, "servoCH3");
        clawRotate = hardwareMap.get(Servo.class, "servoCH1");
        clawLeft = hardwareMap.get(Servo.class, "servoCH2");
        clawRight = hardwareMap.get(Servo.class, "servoCH4");

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
        /// CLAW CONTROL ///
        if (gamepad2ButtonHelper.isButtonJustPressed("x")) {
            if (clawPincersState == ClawPincersState.OPEN) {
                clawPincersState = ClawPincersState.CLOSE;
            }
            else if (clawPincersState == ClawPincersState.CLOSE) {
                clawPincersState = ClawPincersState.OPEN;
            }
        } // Make clawPincerState open or closed
        switch (clawPincersState) {
            case OPEN:
                openClaw();
                break;
            case CLOSE:
                closeClaw();
                break;
        }
        /// Switch Claw States
        if (gamepad2ButtonHelper.isButtonJustPressed("b")) {
            switch (clawState) {
                case GROUND:
                    clawState = ClawState.GRAB_SPEC_FROM_WALL;
                    break;
                case GRAB_SPEC_FROM_WALL:
                    clawState = ClawState.SCORING_CHAMBER;
                    break;
                case SCORING_CHAMBER:
                    clawState = ClawState.GROUND;
                    break;
            }
        }
        //TODO: Devons, fix the wrist servo position at each state
        switch (clawState) {
            case GROUND:
                clawWrist.setPosition(0.5);
                break;
            case GRAB_SPEC_FROM_WALL:
                clawWrist.setPosition(0.75);
                break;
            case SCORING_CHAMBER:
                clawWrist.setPosition(0.9);
                break;
        }
        currentClawRotation = Math.max(0, Math.min(1, clawRotate.getPosition() + (gamepad2.right_trigger - gamepad2.left_trigger) * 0.01));
        clawRotate.setPosition(currentClawRotation);


        /// ARM CONTROL ///
        // Extension Control //
        final double speed = 0.5;
        if (gamepad2.dpad_up || gamepad2.dpad_down) {
            runExtension(gamepad2.dpad_up, gamepad2.dpad_down, speed);
        }
        else {
            haltExtension();
        }
        // Rotation Control //
        if (gamepad2.a || gamepad2.y) {
            rotateArm(gamepad2.y, gamepad2.a, speed);
        }
        else {
            haltRotation();
        }
        /// ^Arm Control^ ///

        /// DRIVETRAIN CONTROL ///
        double linearSpeedControl = 0.6;
        double headingSpeedControl = 0.3;
        double y = -gamepad1.left_stick_y * linearSpeedControl; // Remember, this is reversed!
        double x = gamepad1.left_stick_x * linearSpeedControl; // this is strafing
        double rx = gamepad1.right_stick_x * headingSpeedControl;
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
        double leftFrontPower = (y + x + rx) / denominator;
        double leftRearPower = (y - x + rx) / denominator;
        double rightFrontPower = (y - x - rx) / denominator;
        double rightRearPower = (y + x - rx) / denominator;

        leftFront.setPower(leftFrontPower);
        leftRear.setPower(leftRearPower);
        rightFront.setPower(rightFrontPower);
        rightRear.setPower(rightRearPower);
        /// ^Drivetrain Control^ ///

        /// TELEMETRY FEEDBACK ///
        if (clawPincersState == ClawPincersState.OPEN) {telemetry.addData("Claw State", "\uD83D\uDFE9");} else {telemetry.addData("Claw State", "\uD83D\uDFE5");}
        telemetry.addData("\n-----------------------------\n", "");
        telemetry.addData("Extension Motor Position: ", extensionMotor1.getCurrentPosition());
        telemetry.addData("Extension Motor Power: ", extensionMotor1.getPower() + " " + extensionMotor2.getPower());
        telemetry.addData("Arm Rotation Position: ", armRotationMotor.getCurrentPosition());
        telemetry.addData("Claw Rotate: ", clawRotate.getPosition());
        telemetry.addData("Claw State: ", clawState);
        telemetry.addData("\n-----------------------------\n", "");
        telemetry.update();

        /// BUTTON HELPER UPDATE ///
        gamepad1ButtonHelper.update();
        gamepad2ButtonHelper.update();
    }

    private void haltRotation() {
        armRotationMotor.setPower(0);
    }

    private void haltExtension() {
        extensionMotor1.setPower(0);
        extensionMotor2.setPower(0);
    }

    private void rotateArm(boolean a, boolean b, double speed) {
        int m = 0;
        if (a) {
            m = 1;} else if (b) {
            m = -1;}
        armRotationMotor.setPower(m*speed);
    }
    public void runExtension(boolean up, boolean down, double speed) {
        int m = 0;
        if (up) {
            m = 1;} else if (down) {
            m = -1;}

        extensionMotor1.setPower(m*speed);
        extensionMotor2.setPower(m*speed);
    }


    void openClaw() {
        clawLeft.setPosition(0.84);
        clawRight.setPosition(0.3);
    }
    void closeClaw() {
        clawLeft.setPosition(1);
        clawRight.setPosition(0);
    }
}