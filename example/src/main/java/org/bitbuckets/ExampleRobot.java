package org.bitbuckets;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.motorcontrol.Talon;
import mattlib.MattLog;
import mattlib.ProcessPath;

public class ExampleRobot extends TimedRobot {

    static final RobotComponent robotComponent = MattLog.instance().makeComponent(
            new ProcessPath(new String[]{"robot"}),
            RobotComponent.class
    );

    @Override
    public void robotInit() {
        robotComponent.myLog(20);

        int canID = robotComponent.myConf();
        new Talon(canID);

        System.out.println("The conf value is: " + canID);
    }
}
