package ninja.controller;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ninja.service.VAS;
import ninja.util.Check;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class VASController extends GroupController<Set<String>> {
    @Autowired
    private VAS vas;

    @Override
    protected Object[] args() {
        return ArrayUtils.toArray( groups( vas ) );
    }

    @Override
    protected Stream<Map<String, String>> group( Entry<String, Set<String>> entry ) {
        return entry.getValue().stream().map( i -> option( i, Utils.spacer( entry.getKey(), i ) ) );
    }

    @PostMapping( "/vas" )
    @Async
    public void vas( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
        var params = Check.station( Check.params( text ) );

        String branch = params[ 0 ], division = params[ 1 ];

        var divisions = vas.data().get( branch );

        Check.nil( divisions, "查無院區: " + branch );

        Check.expr( divisions.contains( division ), "查無科別: " + division );

        var data = vas.call().stream().filter( i -> branch.equals( i.get( "Branch" ) ) && division.equals( i.get( "Division" ) ) ).collect( Collectors.toList() );

        Check.list( data, "查無看診資料: " + text );

        var attach = Slack.attachment( "三總看診進度查詢", "https://www2.ndmctsgh.edu.tw/PatientNum/" ).setText( tag( branch, division ) );

        data.forEach( i -> attach.addFields( field( i.get( "Room" ), i.get( "Doctor" ) ) ).addFields( field( "目前 / 下個", i.get( "Current" ) + " / " + i.get( "Next" ) ) ) );

        message( attach, command, text, url );
    }
}