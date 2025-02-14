package pedroPathing.constants;

import com.pedropathing.localization.*;
import com.pedropathing.localization.constants.*;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class LConstants {
    static {
        OTOSConstants.useCorrectedOTOSClass = true;
        OTOSConstants.hardwareMapName = "sensor_otos";
        OTOSConstants.linearUnit = DistanceUnit.INCH;
        OTOSConstants.angleUnit = AngleUnit.RADIANS;
        OTOSConstants.offset = new SparkFunOTOS.Pose2D(4.375, .25, Math.PI / 2);
        OTOSConstants.linearScalar = 1.00;
        OTOSConstants.angularScalar = 1.00;
    }
}

//I will never forget what you did to me
//my blood is like sand and my heart is the water that shreds it
//hash maps are fucking useless,so fuck you
//also i hid a greek question mark :
//here is some amazing code that your new partners know nothing about
//for heart in ("kaiden", "devin"):
//  heart = broken
//this is a function nga



