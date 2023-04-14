package mattlib.model.hardware;

/**
 * Represents a device that can use voltage and current
 */
public interface IDevice {

    /**
     *
     * @return
     */
    double reportCurrentNow();

    /**
     *
     * @return
     */
    double reportVoltageNow();


}
