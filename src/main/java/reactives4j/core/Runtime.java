package reactives4j.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import reactives4j.maybe.Maybe;

import java.util.*;
import java.util.function.Supplier;

/**
 * The runtime for the reactive system.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Runtime {

    /**
     * Current reactive owner.
     */
    @Getter(AccessLevel.PACKAGE)
    final Maybe<BaseNode<?>> owner = Maybe.nothing();

    /**
     * Current reactive observer.
     */
    @Getter(AccessLevel.PACKAGE)
    final Maybe<BaseNode<?>> observer = Maybe.nothing();

    /**
     * List of all nodes in the reactive system.
     */
    @Getter(AccessLevel.PACKAGE)
    final Set<BaseNode<?>> nodes = new HashSet<>();

    /**
     * Map of nodes to their subscribers.
     */
    @Getter(AccessLevel.PACKAGE)
    final Map<BaseNode<?>, Set<BaseNode<?>>> subscribers = new HashMap<>();

    /**
     * Map of nodes to their sources.
     */
    @Getter(AccessLevel.PACKAGE)
    final Map<BaseNode<?>, Set<BaseNode<?>>> sources = new HashMap<>();

    /**
     * Map of nodes to their owners.
     */
    @Getter(AccessLevel.PACKAGE)
    final Map<BaseNode<?>, BaseNode<?>> owners = new HashMap<>();

    /**
     * Map of nodes to their properties.
     */
    @Getter(AccessLevel.PACKAGE)
    final Map<BaseNode<?>, Set<Property>> properties = new HashMap<>();

    /**
     * Map of nodes to their cleanup functions.
     */
    @Getter(AccessLevel.PACKAGE)
    final Map<BaseNode<?>, List<Runnable>> cleanups = new HashMap<>();

    /**
     * Set of nodes that need to be updated.
     */
    @Getter(AccessLevel.PACKAGE)
    final Set<BaseNode<?>> pending = new HashSet<>();

    /**
     * Creates a new runtime. This should only be called once per thread.
     *
     * @return the new runtime
     */
    @Contract(value = "_ -> new", pure = true)
    static Runtime create(Context cx) {
        var root = new Trigger(cx);
        var runtime = new Runtime();
        runtime.owner.set(root);
        runtime.nodes.add(root);
        return runtime;
    }

    void update(BaseNode<?> node) {
        // run the computation and get the result
        boolean result;
        if (node.getState().isObserver()) {
            result = withObserver(node, () -> node.run(this));
        } else {
            result = node.run(this);
        }

        // mark children dirty
        if (result && subscribers.containsKey(node)) {
            subscribers.get(node).forEach(BaseNode::setDirty);
        }

        // mark the node clean
        markClean(node);
    }

    void updateIfNecessary(BaseNode<?> node) {
        if (node.getStatus() == NodeStatus.Check) {
            for (BaseNode<?> source : sources.get(node)) {
                updateIfNecessary(source);
                if (node.isDirtyOrMarked()) break;
            }
        }
        if (node.isDirtyOrMarked()) {
            update(node);
        }
        markClean(node);
    }

    void subscribeObserver(BaseNode<?> node) {
        observer.ifPresent(observer -> {
            // add this observer to the node's dependencies
            subscribers.putIfAbsent(node, new HashSet<>());
            subscribers.get(node).add(observer);
            // add the node to this observer's sources
            sources.putIfAbsent(observer, new HashSet<>());
            sources.get(observer).add(node);
        });
    }

    void subscribeObserver(BaseNode<?> node, BaseNode<?> observer) {
        subscribers.putIfAbsent(node, new HashSet<>());
        subscribers.get(node).add(observer);
        sources.putIfAbsent(observer, new HashSet<>());
        sources.get(observer).add(node);
    }

    void addSubscriber(BaseNode<?> node, BaseNode<?> observer) {
        subscribers.putIfAbsent(node, new HashSet<>());
        subscribers.get(node).add(observer);
    }

    <U> U withObserver(BaseNode<?> node, Supplier<U> f) {
        var previous = observer.take();
        observer.set(node);
        U value = f.get();
        observer.swap(previous);
        return value;
    }

    void runEffects() {
        for (BaseNode<?> node : pending) {
            updateIfNecessary(node);
        }
    }

    void markClean(BaseNode<?> node) {
        node.setClean();
    }

    boolean isEffectLike(BaseNode<?> node) {
        return node.isType(NodeType.Effect) || node.isType(NodeType.Watch);
    }

    void markDirty(BaseNode<?> node) {
        if (node.isMarked()) return;
        mark(node, NodeStatus.Dirty);

        // Prepare the stack for DAG traversal
        Stack<Iterator<BaseNode<?>>> stack = new Stack<>();
        if (subscribers.containsKey(node)) {
            stack.push(subscribers.get(node).iterator());
        }

        outer:
        while (!stack.isEmpty()) {
            // if the iterator is empty, then we're done here
            var iter = stack.peek();
            if (!iter.hasNext()) {
                stack.pop();
                continue;
            }

            // get the first child
            var child = iter.next();
            while (true) {
                if (child.isCheck() || child.isMarked()) continue outer;
                mark(child, NodeStatus.Check);

                if (!subscribers.containsKey(child)) break;

                var children = subscribers.get(child);
                if (children.isEmpty()) break;

                // avoid going through a loop in a pseudo-recursive case
                if (children.size() == 1) {
                    child = children.iterator().next();
                    continue;
                }

                // iterate over the children
                stack.push(children.iterator());
                break;
            }
        }
    }

    void mark(BaseNode<?> node, NodeStatus level) {
        if (level.ordinal() > node.getStatus().ordinal())
            node.setStatus(level);

        if (node.getState().isSubscriber() && !observer.equals(Maybe.just(node)))
            pending.add(node);

        if (node.isDirty())
            node.setMarked();
    }

    void addCleanup(BaseNode<?> node, Runnable cleanup) {
        cleanups.putIfAbsent(node, new ArrayList<>());
        cleanups.get(node).add(cleanup);
    }

    Property pushScopeProperty(BaseNode<?> propertyNode, PropertyType propertyType) {
        var ownerNode = owner.expectGet("Reactive value outside of the reactive root");
        properties.putIfAbsent(ownerNode, new HashSet<>());
        var property = new Property(propertyNode, propertyType);
        properties.get(ownerNode).add(property);
        owners.put(propertyNode, ownerNode);
        return property;
    }

    void removeScopeProperty(BaseNode<?> owner, Property property) {
        properties.get(owner).remove(property);
        owners.remove(property.node());
    }

    void cleanupSources(BaseNode<?> node) {
        if (!sources.containsKey(node)) return;
        sources.get(node).forEach(source -> {
            if (subscribers.containsKey(source)) {
                subscribers.get(source).remove(node);
            }
        });
    }

    void addNode(BaseNode<?> node) {
        nodes.add(node);
    }

    void disposeNode(BaseNode<?> node) {
        subscribers.remove(node);
        sources.remove(node);
        nodes.remove(node);
    }

    void disposeRuntime() {
        observer.clear();
        subscribers.clear();
        sources.clear();
        pending.clear();
    }

}
