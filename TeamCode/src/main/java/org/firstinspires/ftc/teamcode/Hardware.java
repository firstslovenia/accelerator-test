package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Hardware {

	private LinearOpMode calling_opmode = null;

	// There is no only encoder class..
	public DcMotor encoder = null;

	public Hardware(LinearOpMode opmode) {
		calling_opmode = opmode;
	}

	public void init() {
		encoder = calling_opmode.hardwareMap.get(DcMotor.class, "encoder");
	}
}
