package MiniBot.DriveTrainControl;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp(name = "every motor test", group=".")
public class everyMotorTest extends OpMode {
    DcMotorEx c1, c2, c3, c4, e1, e2, e3, e4;
    @Override
    public void init() {
        c1 = hardwareMap.get(DcMotorEx.class, "c1");
        c2 = hardwareMap.get(DcMotorEx.class, "c2");
        c3 = hardwareMap.get(DcMotorEx.class, "c3");
        c4 = hardwareMap.get(DcMotorEx.class, "c4");

        e1 = hardwareMap.get(DcMotorEx.class, "e1");
        e2 = hardwareMap.get(DcMotorEx.class, "e2");
        e3 = hardwareMap.get(DcMotorEx.class, "e3");
        e4 = hardwareMap.get(DcMotorEx.class, "e4");
    }

    @Override
    public void loop() {
        double s = 0.3;
        if (gamepad1.x) {c1.setPower(s);}else {c1.setPower(0);}
        if (gamepad1.y) {c2.setPower(s);}else {c2.setPower(0);}
        if (gamepad1.b) {c3.setPower(s);}else {c3.setPower(0);}
        if (gamepad1.a) {c4.setPower(s);}else {c4.setPower(0);}

        if (gamepad2.x) {e1.setPower(s);}else {e1.setPower(0);}
        if (gamepad2.y) {e2.setPower(s);}else {e2.setPower(0);}
        if (gamepad2.b) {e3.setPower(s);}else {e3.setPower(0);}
        if (gamepad2.a) {e4.setPower(s);}else {e4.setPower(0);}
    }
}
