package org.j8unit.maven.example.api;

public abstract interface IdByName {

    public abstract String name();

    public default String getId() {
        return name();
    }

    public static enum UniqueNames
    implements IdByName {
        XYZ,
        ABC,
        QWERTY;
    }

}
