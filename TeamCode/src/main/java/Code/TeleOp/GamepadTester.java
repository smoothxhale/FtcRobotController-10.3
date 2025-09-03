package Code.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Gamepad Tester")
public class GamepadTester extends OpMode {

    @Override
    public void init() {
        telemetry.addLine("--- DEBUG: Gamepad 1 Tester Initialized ---");
        telemetry.update();
    }

    @Override
    public void loop() {
        // --- Sticks ---
        telemetry.addLine("--- Sticks ---");
        telemetry.addData("Left Stick X", "%.3f", gamepad1.left_stick_x);
        telemetry.addData("Left Stick Y", "%.3f", gamepad1.left_stick_y);
        telemetry.addData("Right Stick X", "%.3f", gamepad1.right_stick_x);
        telemetry.addData("Right Stick Y", "%.3f", gamepad1.right_stick_y);

        // --- Triggers ---
        telemetry.addLine("--- Triggers ---");
        telemetry.addData("Left Trigger", "%.3f", gamepad1.left_trigger);
        telemetry.addData("Right Trigger", "%.3f", gamepad1.right_trigger);

        // --- Buttons ---
        telemetry.addLine("--- Buttons ---");
        telemetry.addData("A", gamepad1.a);
        telemetry.addData("B", gamepad1.b);
        telemetry.addData("X", gamepad1.x);
        telemetry.addData("Y", gamepad1.y);

        // --- Bumpers ---
        telemetry.addLine("--- Bumpers ---");
        telemetry.addData("Left Bumper", gamepad1.left_bumper);
        telemetry.addData("Right Bumper", gamepad1.right_bumper);

        // --- D-Pad ---
        telemetry.addLine("--- D-Pad ---");
        telemetry.addData("DPad Up", gamepad1.dpad_up);
        telemetry.addData("DPad Down", gamepad1.dpad_down);
        telemetry.addData("DPad Left", gamepad1.dpad_left);
        telemetry.addData("DPad Right", gamepad1.dpad_right);

        telemetry.update();
    }
}