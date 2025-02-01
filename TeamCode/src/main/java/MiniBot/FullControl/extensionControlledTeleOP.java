package MiniBot.FullControl;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import MiniBot.ButtonHelper;

@Config
public class extensionControlledTeleOP extends OpMode {
    class asdf {

    }
    DcMotorEx frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor;
    DcMotorEx extensionMotor1, extensionMotor2;
    DcMotorEx armRotationMotor;

    Servo clawWrist, dummyWristForAllen, clawRotate, clawLeft, clawRight;
    double currentClawRotation = 0.3;

    enum RobotState {
        GROUND,
        GRAB_SPEC,
        SPEC_SCORING,
        BASKET_SCORING,
        BASE
    }
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
    enum ClawState {
        GROUND,
        GRAB_SPEC_FROM_WALL,
        SCORING_CHAMBER,
        SCORING_BASKET
    }

    RobotState robotState = RobotState.GROUND;
    ArmState armState = ArmState.GROUND_BASE;
    ClawState clawState = ClawState.GROUND;

    ButtonHelper gamepad1ButtonHelper, gamepad2ButtonHelper;
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

        gamepad1ButtonHelper = new ButtonHelper(gamepad1);
        gamepad2ButtonHelper = new ButtonHelper(gamepad2);
    }

    @Override
    public void init_loop() {
        closeClaw();
    }
    @Override
    public void loop() {
        /// ROBOT STATE FSM ///
        switch (robotState) {
            case GROUND:
                // arm to base
                extendArmToEncoderPosition(0);
                rotateArmToEncoderPosition(0);
                armState = ArmState.GROUND_BASE;
                clawState = ClawState.GROUND;
                break;
            case GRAB_SPEC:
                // arm to grab height
                extendArmToEncoderPosition(0);
                rotateArmToEncoderPosition(307);
                armState = ArmState.GRAB_SPEC_FROM_WALL;
                clawState = ClawState.GRAB_SPEC_FROM_WALL;
                break;
            case SPEC_SCORING:
                // arm to spec high scoring height as default
                extendArmToEncoderPosition(875);
                rotateArmToEncoderPosition(1100);
                armState = ArmState.SET_UP_SCORE_HIGH_CHAMBER;
                clawState = ClawState.SCORING_CHAMBER;
                break;
            case BASKET_SCORING:
                extendArmToEncoderPosition(1400);
                rotateArmToEncoderPosition(1000);
                // arm to high basket scoring height as default
                armState = ArmState.SCORE_HIGH_BASKET;
                clawState = ClawState.SCORING_BASKET;
                break;
            case BASE:
                /// /// Escape Preset Arm States /// ///
                /// Left Stick Controls Rotation ///
                if (Math.abs(gamepad2.left_stick_y) >= 0.1) {
                    armRotationMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    armRotationMotor.setPower(gamepad1.left_stick_y);
                }
                /// Right Stick Controls Extension ///
                if (Math.abs(gamepad2.right_stick_y) >= 0.1) {
                    extensionMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    extensionMotor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

                    extensionMotor1.setPower(gamepad1.right_stick_y);
                    extensionMotor2.setPower(gamepad1.right_stick_y);
                }
                break;

        }
        if (gamepad2.right_bumper) {closeClaw();}
        if (gamepad2.left_bumper) {openClaw();}

        if (gamepad2ButtonHelper.isButtonJustPressed("dpad_up")) {
            switch (robotState) {
                case GROUND: case BASE:
                    robotState = RobotState.GRAB_SPEC;
                    break;
                case GRAB_SPEC:
                    robotState = RobotState.SPEC_SCORING;
                    break;
                case SPEC_SCORING:
                    robotState = RobotState.BASKET_SCORING;
                    break;
            }
        }
        else if (gamepad2ButtonHelper.isButtonJustPressed("dpad_down")) {
            switch (robotState) {
                case BASKET_SCORING:
                    robotState = RobotState.SPEC_SCORING;
                    break;
                case SPEC_SCORING:
                    robotState = RobotState.GRAB_SPEC;
                    break;
                case GRAB_SPEC:
                    robotState = RobotState.GROUND;
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
        if (gamepad2ButtonHelper.isButtonJustPressed("x")) {
            switch (armState) {
                case SET_UP_SCORE_HIGH_CHAMBER:
                    armState = ArmState.SCORING_HIGH_CHAMBER;
                    break;
                case SET_UP_SCORE_LOW_CHAMBER:
                    armState = ArmState.SCORING_LOW_CHAMBER;
                    break;
            }
        }

        telemetry.addData("Robot State", robotState);
        telemetry.addData("Arm State", armState);
        telemetry.addData("Claw State", clawState);
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
    void extendArmToEncoderPosition(int encoderLocation) {
        //
        extensionMotor1.setTargetPosition(encoderLocation);
        extensionMotor2.setTargetPosition(encoderLocation);
        extensionMotor1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        extensionMotor2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        extensionMotor1.setPower(0.7);
        extensionMotor2.setPower(0.7);


    }
    void rotateArmToEncoderPosition(int encoderLocation) {
        armRotationMotor.setTargetPosition(encoderLocation);
        armRotationMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armRotationMotor.setPower(0.7);
    }

}
