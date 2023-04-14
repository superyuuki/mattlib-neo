package mattlib.model.hardware;

/**
 * Represents something that can be driven
 */
public interface IActuator {

    /**
     * @param voltage The voltage that the actuator should be driven at, typically ranged -12 to 12 but not always
     */
    void driveAtVoltage(double voltage);

    /**
     * @param percent_zeroToOne The percent that
     */
    void driveAtPercent(double percent_zeroToOne);


}
