package Code.TeleOp; // Ensure this matches your package structure

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "Teleop")
public class Teleop extends OpMode {

    // --- Establishing Motors ---
    DcMotor FR;
    DcMotor FL;
    DcMotor BR;
    DcMotor BL;

    // --- IMU (Gyro) Sensor ---
    IMU imu;

    // --- State Variables ---
    boolean isFieldCentric = true; // Start in Field-Centric mode by default
    boolean yButtonPreviouslyPressed = false; // To toggle control mode

    @Override
    public void init() {
        // --- HARDWARE MAPPING ---
        FR = hardwareMap.dcMotor.get("FR");
        FL = hardwareMap.dcMotor.get("FL");
        BR = hardwareMap.dcMotor.get("BR");
        BL = hardwareMap.dcMotor.get("BL");
        imu = hardwareMap.get(IMU.class, "imu");

        // --- MOTOR CONFIGURATION ---
        // Reverse the motors on the left side to account for their mirrored mounting
        FL.setDirection(DcMotorSimple.Direction.REVERSE);
        BL.setDirection(DcMotorSimple.Direction.REVERSE);

        // Set motors to BRAKE mode to prevent drifting when idle
        FR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        FL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // --- IMU INITIALIZATION ---
        // Define the orientation of the Control Hub on your robot
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        ));
        imu.initialize(parameters);
        imu.resetYaw();

        // --- INIT TELEMETRY ---
        telemetry.addLine("--- TeleOp Initialized ---");
        telemetry.addLine("Press Y to toggle between Field-Centric and Robot-Centric");
        telemetry.addLine("Hold A to reset IMU heading");
        telemetry.addLine("Hold Left+Right Bumpers for TURBO MODE");
        telemetry.update();
    }

    @Override
    public void loop() {
        // --- TOGGLE CONTROL MODE ---
        boolean yButtonCurrentlyPressed = gamepad1.y;
        if (yButtonCurrentlyPressed && !yButtonPreviouslyPressed) {
            isFieldCentric = !isFieldCentric;
        }
        yButtonPreviouslyPressed = yButtonCurrentlyPressed;

        // --- GATHER INPUTS ---
        double y_raw = -gamepad1.left_stick_y;  // Forward/Backward
        double x_raw =  gamepad1.left_stick_x;  // Strafe Left/Right
        double rx_raw = gamepad1.right_stick_x; // Rotate Left/Right

        // --- APPLY DRIVER ENHANCEMENTS  ---
        boolean isTurboMode = gamepad1.left_bumper && gamepad1.right_bumper;
        double y_final, x_final, rx_final;

        if (isTurboMode) {
            // Use linear values for max speed
            y_final = y_raw;
            x_final = x_raw;
            rx_final = rx_raw;
        } else {
            // Use quadratically scaled values for precision control
            y_final = Math.signum(y_raw) * (y_raw * y_raw);
            x_final = Math.signum(x_raw) * (x_raw * x_raw);
            rx_final = Math.signum(rx_raw) * (rx_raw * rx_raw);
        }

        // --- FIELD-CENTRIC ADJUSTMENT ---
        // This section is skipped if we are in Robot-Centric mode
        if (isFieldCentric) {
            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
            // Rotate the movement direction by the robot's heading
            double rotatedX = x_final * Math.cos(-botHeading) - y_final * Math.sin(-botHeading);
            double rotatedY = x_final * Math.sin(-botHeading) + y_final * Math.cos(-botHeading);
            // Re-assign the final variables to the rotated values
            x_final = rotatedX;
            y_final = rotatedY;
        }

        // --- MECANUM DRIVE CALCULATION ---
        // This formula mixes the final joystick inputs to calculate the power for each wheel.
        double frontLeftPower  = y_final + x_final + rx_final;
        double backLeftPower   = y_final - x_final + rx_final;
        double frontRightPower = y_final - x_final - rx_final;
        double backRightPower  = y_final + x_final - rx_final;

        // --- NORMALIZATION ---
        double max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
        max = Math.max(max, Math.abs(backLeftPower));
        max = Math.max(max, Math.abs(backRightPower));

        if (max > 1.0) {
            frontLeftPower  /= max;
            backLeftPower   /= max;
            frontRightPower /= max;
            backRightPower  /= max;
        }

        // --- POWER THE MOTORS ---
        FL.setPower(frontLeftPower);
        BL.setPower(backLeftPower);
        FR.setPower(frontRightPower);
        BR.setPower(backRightPower);

        // --- MANUAL IMU RESET ---
        // Pressing 'A' will reset the IMU heading to zero.
        if (gamepad1.a) {
            imu.resetYaw();
        }

        // --- TELEMETRY (Debug Data) ---
        telemetry.addData("Drive Mode", isFieldCentric ? "Field-Centric" : "Robot-Centric");
        telemetry.addData("Speed Mode", isTurboMode ? "TURBO" : "Precision");
        telemetry.addData("Bot Heading", "%.2f (Deg)", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
        telemetry.update();
    }
}