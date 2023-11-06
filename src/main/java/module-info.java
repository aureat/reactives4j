/**
 * <h3>Reactives4j</h3>
 * <p>
 * Composable reactive primitives with a non-blocking reactive runtime and dirty-checking.
 * The library provides API for creating and managing reactive data flows, supporting both synchronous and asynchronous operations.
 * </p>
 * <ul>
 *     <b>Constructs:</b>
 *     <li>{@link reactives4j.core.Reactive} - a reactive value that can be observed and updated</li>
 *     <li>{@link reactives4j.core.Memo} - a reactive value that is computed from other reactive values</li>
 *     <li>{@link reactives4j.core.Trigger} - a reactive value that can be triggered to update its subscribers</li>
 *     <li>{@link reactives4j.core.Resource} - a value store whose value is computed asynchronously</li>
 * </ul>
 */
module reactives4j {
    requires java.desktop;
    requires java.base;
    requires java.compiler;
    requires java.xml;
    requires lombok;
    requires org.jetbrains.annotations;
    requires org.apache.logging.log4j;

    exports reactives4j.core;
    exports reactives4j.maybe;
}