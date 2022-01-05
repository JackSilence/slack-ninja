package ninja.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import ninja.util.Gson;
import ninja.util.Utils;

@Service
public class VAS extends Data<Set<String>> {
    private static final String VAS_URL = "https://patnonew.ndmctsgh.edu.tw/api/history/visit_record";

    public List<Map<String, String>> call() {
        return Gson.list( Utils.call( VAS_URL ) );
    }

    @Override
    void init( Map<String, Set<String>> data ) {
        data.putAll( call().stream().collect( Collectors.groupingBy( i -> i.get( "Branch" ), Collectors.mapping( i -> i.get( "Division" ), Collectors.toSet() ) ) ) );
    }
}
