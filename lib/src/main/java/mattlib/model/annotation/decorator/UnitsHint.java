package mattlib.model.annotation.decorator;

public @interface UnitsHint {

    enum Unit {
        RADIANS,
        DEGREES,
        ROTATIONS,
        VOLTS,
        AMPERES,
        NEWTONS,
        METERS //idk any others
    }

}
