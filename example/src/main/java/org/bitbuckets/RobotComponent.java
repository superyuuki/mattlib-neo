package org.bitbuckets;

import mattlib.model.hardware.IComponent;
import mattlib.model.annotation.core.Conf;
import mattlib.model.annotation.core.Log;
import mattlib.model.annotation.core.Tune;

public interface RobotComponent extends IComponent {

    @Tune
    int myTune();
    @Conf
    int myConf();

    @Log
    void myLog(int data);

}
