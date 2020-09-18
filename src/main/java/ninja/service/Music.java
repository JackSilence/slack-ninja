package ninja.service;

import java.util.List;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;

import org.springframework.stereotype.Service;

import ninja.util.Cast;
import ninja.util.Gson;
import ninja.util.Utils;

@Service
public class Music extends Data<List<List<String>>> {
    @Override
    void init( Map<String, List<List<String>>> data ) {
        try {
            ApiResponse response = new Cloudinary().search().expression( "music AND resource_type:raw" ).execute();

            String url = Cast.map( Cast.list( response, "resources" ).get( 0 ) ).get( "secure_url" ).toString();

            data.put( url, Gson.list( Utils.call( url ) ) );

        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }
}