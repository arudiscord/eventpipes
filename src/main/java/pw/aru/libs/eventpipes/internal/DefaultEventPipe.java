package pw.aru.libs.eventpipes.internal;

import pw.aru.libs.eventpipes.api.*;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static pw.aru.libs.eventpipes.internal.Wrapper.wrapPublisher;
import static pw.aru.libs.eventpipes.internal.Wrapper.wrapSubscriber;

public class DefaultEventPipe<T> implements EventPipe<T> {
    private final EventExecutor executor;
    private final Set<EventConsumer<T>> consumers;

    public DefaultEventPipe(EventExecutor executor) {
        this.executor = executor;
        this.consumers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public CompletableFuture<Void> publish(T event) {
        return CompletableFuture.allOf(
            consumers.stream()
                .map(consumer -> onExecute(event, consumer))
                .toArray(CompletableFuture[]::new)
        );
    }

    @Override
    public EventSubscription<T> subscribe(EventConsumer<T> consumer) {
        return new Subscription(consumer);
    }

    @Override
    public CompletableFuture<T> first(Predicate<T> predicate) {
        return new FirstConsumer(predicate).first;
    }

    @Override
    public void close() {
        consumers.clear();
        onEmpty();
    }

    private void unsubscribe(EventConsumer<T> consumer) {
        consumers.remove(consumer);
        if (consumers.isEmpty()) {
            onEmpty();
        }
    }

    protected CompletableFuture<?> onExecute(T event, EventConsumer<T> consumer) {
        return executor.execute(new EventRunnable(event, consumer));
    }

    protected void onEmpty() {
        //noop
    }

    @Override
    public EventSubscriber<T> subscriber() {
        return wrapSubscriber(this);
    }

    @Override
    public EventPublisher<T> publisher() {
        return wrapPublisher(this);
    }

    class Subscription implements EventSubscription<T>, EventConsumer<T> {
        private final EventConsumer<T> consumer;

        Subscription(EventConsumer<T> consumer) {
            this.consumer = Objects.requireNonNull(consumer);
            consumers.add(this);
        }

        @Override
        public void onEvent(T event) {
            consumer.onEvent(event);
        }

        @Override
        public void close() {
            unsubscribe(this);
        }

        @Override
        public EventPipe<T> pipe() {
            return DefaultEventPipe.this;
        }
    }

    class FirstConsumer implements EventConsumer<T> {
        private final Predicate<T> predicate;
        private final CompletableFuture<T> first = new CompletableFuture<>();

        FirstConsumer(Predicate<T> predicate) {
            this.predicate = Objects.requireNonNull(predicate);
            consumers.add(this);
        }

        @Override
        public void onEvent(T event) {
            if (predicate.test(event)) {
                unsubscribe(this);
                first.complete(event);
            }
        }
    }

    class EventRunnable implements Runnable {
        private final T event;
        private final EventConsumer<T> consumer;

        EventRunnable(T event, EventConsumer<T> consumer) {
            this.event = event;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            consumer.onEvent(event);
        }
    }

}
