package ninja.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import ninja.util.Gson;
import ninja.util.Utils;

@Service
public class VAS extends Data<Set<String>> {
    private static final String VAS_URL = "https://patno.ndmctsgh.edu.tw/wcm/vas";

    public Stream<Map<String, String>> call() {
        List<Map<String, String>> data = Gson.list( Utils.call( VAS_URL ) );

        return data.stream().filter( i -> "內湖".equals( i.get( "Branch" ) ) );
    }

    @Override
    void init( Map<String, Set<String>> data ) {
        data.put( VAS_URL, call().map( i -> i.get( "Division" ) ).collect( Collectors.toSet() ) );
    }
}