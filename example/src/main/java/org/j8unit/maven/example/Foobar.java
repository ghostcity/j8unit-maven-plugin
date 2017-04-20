package org.j8unit.maven.example;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Arrays;
import org.j8unit.maven.example.api.IdByName;

public enum Foobar
implements Serializable, Closeable, IdByName {

    FOO,

    BAR,

    FOOBAR;

    public static void main(final String[] args) {
        System.out.println("Hello world!");
        System.out.println(Arrays.toString(PrivateNames.values()));
    }

    @Override
    public void close() {
    }

    private static enum PrivateNames
    implements IdByName {
        XYZ,
        ABC,
        QWERTY;
    }

    private static class PrivateInnerClass {

        public static class PublicInnerInnerClass {
        }

    }

}
