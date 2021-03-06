package pw.aru.libs.eventpipes.internal;

import pw.aru.libs.eventpipes.api.EventConsumer;
import pw.aru.libs.eventpipes.api.EventPublisher;
import pw.aru.libs.eventpipes.api.EventSubscriber;
import pw.aru.libs.eventpipes.api.EventSubscription;
import pw.aru.libs.eventpipes.api.keyed.KeyedEventPublisher;
import pw.aru.libs.eventpipes.api.keyed.KeyedEventSubscriber;
import pw.aru.libs.eventpipes.api.typed.TypedEventPublisher;
import pw.aru.libs.eventpipes.api.typed.TypedEventSubscriber;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class Wrapper {
    public static <T> EventSubscriber<T> wrapSubscriber(EventSubscriber<T> wrapped) {
        return new WrappedSubscriber<>(wrapped);
    }

    public static <K, V> KeyedEventSubscriber<K, V> wrapSubscriber(KeyedEventSubscriber<K, V> wrapped) {
        return new WrappedKeyedSubscriber<>(wrapped);
    }

    public static TypedEventSubscriber wrapSubscriber(TypedEventSubscriber wrapped) {
        return new WrappedTypedSubscriber(wrapped);
    }

    public static <T> EventPublisher<T> wrapPublisher(EventPublisher<T> wrapped) {
        return new WrappedPublisher<>(wrapped);
    }

    public static <K, V> KeyedEventPublisher<K, V> wrapPublisher(KeyedEventPublisher<K, V> wrapped) {
        return new WrappedKeyedPublisher<>(wrapped);
    }

    public static TypedEventPublisher wrapPublisher(TypedEventPublisher wrapped) {
        return new WrappedTypedPublisher(wrapped);
    }

    private static class WrappedSubscriber<T> implements EventSubscriber<T> {
        private final EventSubscriber<T> wrapped;

        WrappedSubscriber(EventSubscriber<T> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public EventSubscription<T> subscribe(EventConsumer<T> consumer) {
            return wrapped.subscribe(consumer);
        }

        @Override
        public CompletableFuture<T> first(Predicate<T> predicate) {
            return wrapped.first(predicate);
        }
    }

    private static class WrappedPublisher<T> implements EventPublisher<T> {
        private final EventPublisher<T> wrapped;

        WrappedPublisher(EventPublisher<T> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public CompletableFuture<Void> publish(T event) {
            return wrapped.publish(event);
        }
    }

    private static class WrappedKeyedSubscriber<K, V> implements KeyedEventSubscriber<K, V> {
        private final KeyedEventSubscriber<K, V> wrapped;

        WrappedKeyedSubscriber(KeyedEventSubscriber<K, V> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public EventSubscription<V> subscribe(K key, EventConsumer<V> consumer) {
            return wrapped.subscribe(key, consumer);
        }

        @Override
        public CompletableFuture<V> first(K key, Predicate<V> predicate) {
            return wrapped.first(key, predicate);
        }
    }

    private static class WrappedKeyedPublisher<K, V> implements KeyedEventPublisher<K, V> {
        private final KeyedEventPublisher<K, V> wrapped;

        WrappedKeyedPublisher(KeyedEventPublisher<K, V> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public CompletableFuture<Void> publish(K key, V value) {
            return wrapped.publish(key, value);
        }
    }

    private static class WrappedTypedSubscriber implements TypedEventSubscriber {
        private final TypedEventSubscriber wrapped;

        WrappedTypedSubscriber(TypedEventSubscriber wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public <T> EventSubscription<T> subscribe(Class<T> type, EventConsumer<T> consumer) {
            return wrapped.subscribe(type, consumer);
        }

        @Override
        public <T> CompletableFuture<T> first(Class<T> type, Predicate<T> predicate) {
            return wrapped.first(type, predicate);
        }
    }

    private static class WrappedTypedPublisher implements TypedEventPublisher {
        private final TypedEventPublisher wrapped;

        WrappedTypedPublisher(TypedEventPublisher wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public CompletableFuture<Void> publish(Object event) {
            return wrapped.publish(event);
        }
    }
}
