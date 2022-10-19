package ninja.controller;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import ninja.service.Data;

public abstract class GroupController<T> extends DialogController {
    protected String groups( Data<T> data ) {
        return json( data.data().entrySet().stream().map( i -> {
            return Map.of( LABEL, i.getKey(), OPTIONS, list( group( i ) ) );
        } ) );
    }

    protected abstract Stream<Map<String, String>> group( Entry<String, T> entry );
}