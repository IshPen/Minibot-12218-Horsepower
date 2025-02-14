package MiniBot.ArmControl.ColorSensorControl;

public class armColorFunctions {
    public static String detectColor(int red, int green, int blue) {
        if ((red + blue + green) > 5000) {
            return "White";
        } else if ((red + blue + green) > 600) {
            if (red > blue && red > green && red > 500 && red < 1000) {
                return "Red";
            } else if (blue > red && blue > green && blue > 500 && blue < 1000) {
                return "Blue";
            }
        }
        return "None";
    }
}
