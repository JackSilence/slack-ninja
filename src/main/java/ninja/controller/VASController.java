package ninja.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.gpedro.integrations.slack.SlackAttachment;
import ninja.service.VAS;
import ninja.util.Check;
import ninja.util.Slack;

@RestController
public class VASController extends DialogController {
    @Autowired
    private VAS vas;

    @Override
    protected Object[] args() {
        return ArrayUtils.toArray( options( Iterables.getOnlyElement( vas.data().values() ) ) );
    }

    @PostMapping( "/vas" )
    @Async
    public void vas( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
        List<Map<String, String>> data = vas.call().filter( i -> text.equals( i.get( "Division" ) ) ).collect( Collectors.toList() );

        Check.list( data, "查無科別: " + text );

        SlackAttachment attach = Slack.attachment( "三總看診進度查詢", "https://www2.ndmctsgh.edu.tw/PatientNum/" ).setText( tag( text ) );

        data.forEach( i -> attach.addFields( field( i.get( "Room" ), i.get( "Doctor" ) ) ).addFields( field( "目前 / 下個", i.get( "Current" ) + " / " + i.get( "Next" ) ) ) );

        message( attach, command, text, url );
    }
}