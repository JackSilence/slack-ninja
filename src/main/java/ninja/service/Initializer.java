package ninja.service;

import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class Initializer {
    @Async
    public <T> void init( Data<T> data, Map<String, T> map ) {
        data.init( map );
    }
}