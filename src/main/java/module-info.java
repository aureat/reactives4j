module reactives4j {
    requires java.desktop;
    requires java.base;
    requires java.compiler;
    requires java.xml;
    requires lombok;
    requires org.jetbrains.annotations;
    requires org.apache.logging.log4j;

    exports reactives4j.core;
    exports java.util.maybe;
}