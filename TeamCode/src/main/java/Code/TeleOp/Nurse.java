package Code.TeleOp;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

// This is the "Nurse" OpMode, a diagnostic tool for testing drivetrain motors.
//
// --- HOW TO USE ---
// 1. Press and hold one of the following buttons to select a motor:
//    - Y Button: Front Right (FR)
//    - X Button: Front Left (FL)
//    - B Button: Back Right (BR)
//    - A Button: Back Left (BL)
//
// 2. While holding the selector button, use the triggers to control the motor:
//    - Right Trigger: Run motor FORWARD
//    - Left Trigger: Run motor BACKWARD
//
// 3. Check the telemetry on the Driver Station to confirm.

@TeleOp(name = "Nurse")
public class Nurse extends OpMode {

    DcMotor FR;
    DcMotor FL;
    DcMotor BR;
    DcMotor BL;

    @Override
    public void init() {
        // --- HARDWARE MAPPING ---
        FR = hardwareMap.dcMotor.get("FR");
        FL = hardwareMap.dcMotor.get("FL");
        BR = hardwareMap.dcMotor.get("BR");
        BL = hardwareMap.dcMotor.get("BL");

        // --- MOTOR CONFIGURATION ---
        FL.setDirection(DcMotorSimple.Direction.REVERSE);
        BL.setDirection(DcMotorSimple.Direction.REVERSE);
        FR.setZeroPowerBehavior(BRAKE);
        FL.setZeroPowerBehavior(BRAKE);
        BR.setZeroPowerBehavior(BRAKE);
        BL.setZeroPowerBehavior(BRAKE);

        // --- TELEMETRY ---
        telemetry.addLine("--- Nurse Diagnostic Ready ---");
        telemetry.addLine("Hold Y,X,B,A to select a motor.");
        telemetry.addLine("Use Triggers to run forward/backward.");
        telemetry.update();
    }

    @Override
    public void loop() {
        // --- 1. CALCULATE TEST POWER ---
        double testPower = gamepad1.right_trigger - gamepad1.left_trigger;

        // --- 2. SELECT WHICH MOTOR TO TEST ---
        double frPower = 0;
        double flPower = 0;
        double brPower = 0;
        double blPower = 0;

        String motorBeingTested = "None";

        if (gamepad1.y) {
            frPower = testPower;
            motorBeingTested = "Front Right (FR)";
        } else if (gamepad1.x) {
            flPower = testPower;
            motorBeingTested = "Front Left (FL)";
        } else if (gamepad1.b) {
            brPower = testPower;
            motorBeingTested = "Back Right (BR)";
        } else if (gamepad1.a) {
            blPower = testPower;
            motorBeingTested = "Back Left (BL)";
        }

        // --- 3. POWER THE MOTORS ---
        FR.setPower(frPower);
        FL.setPower(flPower);
        BR.setPower(brPower);
        BL.setPower(blPower);

        // --- 4. TELEMETRY ---
        telemetry.addLine("--- Motor Diagnostic ---");
        telemetry.addData("Motor Selected", motorBeingTested);
        telemetry.addData("Test Power", "%.2f", testPower);
        telemetry.addLine();
        telemetry.addData("FR Power", "%.2f", frPower);
        telemetry.addData("FL Power", "%.2f", flPower);
        telemetry.addData("BR Power", "%.2f", brPower);
        telemetry.addData("BL Power", "%.2f", blPower);
        telemetry.update();
    }
}