package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.*;

/// To use this, connect an encoder to
@TeleOp(name = "FGC Accelerator")
public class Accelerator extends LinearOpMode {

	Hardware hardware;

	/// How many milliseconds to poll the encoder after
	long polling_time_ms = 200;

	/// How many milliseconds to update our logic after
	long update_time_ms = 50;

	/// How many RPMs we need to get an energized accelerator
	double revolutions_per_minute_needed = 250.0;

	/// How many encoder ticks for one rotation of our wheel
	double ticks_per_revolution = 8192.0;

	// ------

	/// The current speed in RPM
	double revolutions_per_minute = 0.0;

	/// When we started turning at >= 250 RPM
	long started_being_energized_time_ms = 0;

	/// When we last updated our rate
	long last_rate_update_time_ms = 0;

	/// Whether or not we are turning at >= 250 RPM
	boolean is_energized = false;

	/// If energized and false -> we are rotating clockwise
	/// If energized and true -> we are rotating counterclockwise
	boolean energized_counter_clockwise = false;

	static final byte DISPENSER_RATE_LOW = 0;
	static final byte DISPENSER_RATE_MEDIUM = 1;
	static final byte DISPENSER_RATE_HIGH = 2;

	byte dispenser_rate = DISPENSER_RATE_LOW;

	/// The highest rate we reached in this instance
	byte highest_dispenser_rate = DISPENSER_RATE_LOW;

	/// When we last read from the encoder
	long last_reading_time_ms = 0;

	/// The encoder's last position
	int last_position = 0;

	@Override
	public void runOpMode() {

		hardware = new Hardware(this);
		hardware.init();

		waitForStart();

		while (opModeIsActive()) {

			long current_time_ms = System.currentTimeMillis();

			long elapsed_ms = current_time_ms - last_reading_time_ms;

			if (elapsed_ms >= polling_time_ms) {
				int position_now = hardware.encoder.getCurrentPosition();

				int ticks_elapsed = position_now - last_position;

				double ticks_per_second = (double) ticks_elapsed / ((double) elapsed_ms / 1000.0);

				double revolutions_per_second = ticks_per_second / ticks_per_revolution;

				revolutions_per_minute = revolutions_per_second * 60.0;

				if (Math.abs(revolutions_per_minute) < revolutions_per_minute_needed && is_energized) {
					is_energized = false;
				}

				if (Math.abs(revolutions_per_minute) > revolutions_per_minute_needed) {
					if (!is_energized) {
						is_energized = true;
						started_being_energized_time_ms = current_time_ms;
						energized_counter_clockwise = revolutions_per_minute < 0.0;
					} else {
						// Check for the wrong way
						if (energized_counter_clockwise && revolutions_per_minute > 0.0) {
							started_being_energized_time_ms = current_time_ms;
							energized_counter_clockwise = false;
						}
						if (!energized_counter_clockwise && revolutions_per_minute < 0.0) {
							started_being_energized_time_ms = current_time_ms;
							energized_counter_clockwise = true;
						}
					}
				}

				last_reading_time_ms = current_time_ms;
				last_position = position_now;
			}

			if (elapsed_ms >= update_time_ms) {

				long time_energized_ms = current_time_ms - started_being_energized_time_ms;

				if (time_energized_ms < 11_000) {

					if (is_energized) {
						if (highest_dispenser_rate == DISPENSER_RATE_MEDIUM && time_energized_ms > 10_000) {
							dispenser_rate = DISPENSER_RATE_HIGH;
							highest_dispenser_rate = DISPENSER_RATE_HIGH;
							last_rate_update_time_ms = current_time_ms;
						} else if (highest_dispenser_rate == DISPENSER_RATE_LOW && time_energized_ms > 5000) {
							dispenser_rate = DISPENSER_RATE_MEDIUM;
							highest_dispenser_rate = DISPENSER_RATE_MEDIUM;
							last_rate_update_time_ms = current_time_ms;
						}
					}

				}

				else {
					if (dispenser_rate > DISPENSER_RATE_LOW) {

						long elapsed_ms_since_rate_update = current_time_ms - last_rate_update_time_ms;

						if (elapsed_ms_since_rate_update >= 15_000) {
							dispenser_rate -= 1;
							last_rate_update_time_ms = current_time_ms;
						}
					}
				}

				// If we've dropped back to low after being higher
				if (dispenser_rate == DISPENSER_RATE_LOW && highest_dispenser_rate > DISPENSER_RATE_LOW) {
					highest_dispenser_rate = DISPENSER_RATE_LOW;
					last_rate_update_time_ms = current_time_ms;
					started_being_energized_time_ms = current_time_ms;
				}

				telemetry.addData("Revolutions per minute: ", revolutions_per_minute);
				telemetry.addData("Dispenser rate: ", dispenser_rate);
				telemetry.addData("Highest Dispenser rate: ", highest_dispenser_rate);
				telemetry.addData("Energized: ", is_energized);
				telemetry.addData("Energized backwards: ", energized_counter_clockwise);
				telemetry.addData("Elapsed seconds since energized: ", (current_time_ms - started_being_energized_time_ms) / 1000.0);
				telemetry.addData("Elapsed seconds since rate update: ", (current_time_ms - last_rate_update_time_ms) / 1000.0);
				telemetry.update();
			}
		}
	}
}
