package Code.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

// This is the "Encoder Test" OpMode, a diagnostic tool for testing wheel odometry.
//
// --- HOW TO USE ---
// 1. Hold the Right and Left Triggers to run all wheels at max power.
// 2. Observe the encoder counts for each wheel in the telemetry.
// 3. Press the 'X' button to reset all encoder counts to zero.

@TeleOp(name = "Encoder Test")
public class EncoderTest extends OpMode {

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

        FR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        FL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        BR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        BL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        FR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        FL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        BR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        BL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // --- TELEMETRY ---
        telemetry.addLine("--- DEBUG: Encoder Test Initialized ---");
        telemetry.addLine("Hold Right or Left Trigger to run wheels at max power.");
        telemetry.addLine("Press X to reset encoders.");
        telemetry.update();
    }

    @Override
    public void loop() {
        // --- 1. SET MOTOR POWER ---
        double testPower = gamepad1.right_trigger - gamepad1.left_trigger;

        FR.setPower(testPower);
        FL.setPower(testPower);
        BR.setPower(testPower);
        BL.setPower(testPower);

        // --- 2. RESET ENCODERS ---
        if (gamepad1.x) {
            FR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            FL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            BR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            BL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            FR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            FL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            BR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            BL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        // --- 3. TELEMETRY ---
        if (testPower != 0) {
            telemetry.addLine("--- Wheel Encoder Counts ---");
            telemetry.addData("Front Right (FR)", FR.getCurrentPosition());
            telemetry.addData("Front Left (FL)", FL.getCurrentPosition());
            telemetry.addData("Back Right (BR)", BR.getCurrentPosition());
            telemetry.addData("Back Left (BL)", BL.getCurrentPosition());
            telemetry.update();
        }
    }
}