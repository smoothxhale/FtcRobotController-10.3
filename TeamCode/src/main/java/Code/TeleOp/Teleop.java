package Code.TeleOp;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "TeleOp")
public class Teleop extends OpMode {

    // --- Drivetrain Motors ---
    DcMotor FR;
    DcMotor FL;
    DcMotor BR;
    DcMotor BL;

    // --- IMU (Gyro) Sensor ---
    IMU imu;

    // --- State Variables ---
    boolean isFieldCentric = true;
    boolean yButtonPreviouslyPressed = false;
    double headingOffset = 0.0;

    @Override
    public void init() {
        // --- HARDWARE MAPPING ---
        FR = hardwareMap.dcMotor.get("FR");
        FL = hardwareMap.dcMotor.get("FL");
        BR = hardwareMap.dcMotor.get("BR");
        BL = hardwareMap.dcMotor.get("BL");
        imu = hardwareMap.get(IMU.class, "imu");

        // --- MOTOR CONFIGURATION ---
        FL.setDirection(DcMotorSimple.Direction.REVERSE);
        BL.setDirection(DcMotorSimple.Direction.REVERSE);
        FR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        FL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // --- IMU INITIALIZATION ---
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        ));
        imu.initialize(parameters);
        imu.resetYaw();

        // --- INIT TELEMETRY ---
        telemetry.addLine("--- TeleOp Initialized ---");
        telemetry.addLine("Press Y to toggle between Field-Centric and Robot-Centric");
        telemetry.addLine("Press A to set 'Forward' as AWAY from the driver's current perspective.");
        telemetry.addLine("Hold Left+Right Bumpers for TURBO MODE (100% Speed)");
        telemetry.update();
    }

    @Override
    public void loop() {
        // --- 1. TOGGLE CONTROL MODE ---
        boolean yButtonCurrentlyPressed = gamepad1.y;
        if (yButtonCurrentlyPressed && !yButtonPreviouslyPressed) {
            isFieldCentric = !isFieldCentric;
        }
        yButtonPreviouslyPressed = yButtonCurrentlyPressed;

        // --- 2. GATHER INPUTS ---
        double y_raw = -gamepad1.left_stick_y;
        double x_raw = gamepad1.left_stick_x;
        double rx_raw = gamepad1.right_stick_x;

        // --- 3. APPLY SCALING ---
        boolean isTurboMode = gamepad1.left_bumper && gamepad1.right_bumper;
        double y_final, x_final, rx_final;
        double speedMultiplier;

        if (isTurboMode) {
            // --- TURBO MODE ---
            y_final = y_raw;
            x_final = x_raw;
            rx_final = rx_raw;
            speedMultiplier = 1.0;
        } else {
            // --- PRECISION MODE ---
            y_final = y_raw * y_raw * y_raw;
            x_final = x_raw * x_raw * x_raw;
            rx_final = rx_raw * rx_raw * rx_raw;
            speedMultiplier = 0.55;
        }

        // --- IMU RESET ---
        // When 'A' is pressed, we define the new "forward" for the controls
        // This makes "forward" on the joystick always point away from the driver's perspective.
        if (gamepad1.a) {
            double currentHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
            headingOffset = currentHeading - Math.PI;
        }

        // --- 4. FIELD-CENTRIC ADJUSTMENT ---
        if (isFieldCentric) {
            double absoluteHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
            double botHeading = absoluteHeading - headingOffset;

            double rotatedX = x_final * Math.cos(-botHeading) - y_final * Math.sin(-botHeading);
            double rotatedY = x_final * Math.sin(-botHeading) + y_final * Math.cos(-botHeading);
            x_final = rotatedX;
            y_final = rotatedY;
        }

        // --- 5. MECANUM DRIVE CALCULATION ---
        double frontLeftPower = y_final + x_final + rx_final;
        double backLeftPower = y_final - x_final + rx_final;
        double frontRightPower = y_final - x_final - rx_final;
        double backRightPower = y_final + x_final - rx_final;

        // --- 6. NORMALIZATION ---
        double max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
        max = Math.max(max, Math.abs(backLeftPower));
        max = Math.max(max, Math.abs(backRightPower));

        if (max > 1.0) {
            frontLeftPower /= max;
            backLeftPower /= max;
            frontRightPower /= max;
            backRightPower /= max;
        }

        // --- 7. APPLY FINAL SPEED MULTIPLIER ---
        frontLeftPower *= speedMultiplier;
        backLeftPower *= speedMultiplier;
        frontRightPower *= speedMultiplier;
        backRightPower *= speedMultiplier;

        // --- 8. POWER THE MOTORS ---
        FL.setPower(frontLeftPower);
        BL.setPower(backLeftPower);
        FR.setPower(frontRightPower);
        BR.setPower(backRightPower);

        // --- 9. TELEMETRY ---
        double relativeHeadingDegrees = AngleUnit.DEGREES.fromRadians(imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS) - headingOffset);

        telemetry.addData("Drive Mode", isFieldCentric ? "Field-Centric" : "Robot-Centric");
        telemetry.addData("Bot Heading", "%.2f (Deg)", relativeHeadingDegrees);
        telemetry.update();
    }
}