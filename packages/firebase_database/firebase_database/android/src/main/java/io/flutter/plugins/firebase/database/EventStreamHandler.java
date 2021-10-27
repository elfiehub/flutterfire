package io.flutter.plugins.firebase.database;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import java.util.Map;

interface OnDispose {
  void run();
}

public class EventStreamHandler implements StreamHandler {
  private final Query query;
  private ValueEventListener valueEventListener;
  private ChildEventListener childEventListener;
  private final OnDispose onDispose;

  public EventStreamHandler(Query query, OnDispose onDispose) {
    this.query = query;
    this.onDispose = onDispose;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {
    final Map<String, Object> args = (Map<String, Object>) arguments;
    final String eventType = (String) args.get(Constants.EVENT_TYPE);

    if (Constants.EVENT_TYPE_VALUE.equals(eventType)) {
      valueEventListener = new ValueEventsProxy(events);
      query.addValueEventListener(valueEventListener);
    } else {
      childEventListener = new ChildEventsProxy(events, eventType);
      query.addChildEventListener(childEventListener);
    }
  }

  @Override
  public void onCancel(Object arguments) {
    this.onDispose.run();

    if (valueEventListener != null) {
      query.removeEventListener(valueEventListener);
      valueEventListener = null;
    }

    if (childEventListener != null) {
      query.removeEventListener(childEventListener);
      childEventListener = null;
    }
  }
}
