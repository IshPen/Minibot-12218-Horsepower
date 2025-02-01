package MiniBot.DriveTrainControl;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp(name="single motor test", group=".")
public class singleMotorTest extends OpMode {
    DcMotorEx motor;
    @Override
    public void init() {
        motor = hardwareMap.get(DcMotorEx.class, "m");
    }

    @Override
    public void loop() {
        motor.setPower(gamepad1.right_trigger - gamepad1.left_trigger);
    }
}
