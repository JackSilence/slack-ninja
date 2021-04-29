package ninja.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ninja.consts.Zone;
import ninja.controller.MovieController;

@Service
public class MovieTask extends Task {
    @Autowired
    private MovieController movie;

    @Scheduled( cron = "0 0 17 * * *", zone = Zone.TAIPEI )
    @Override
    public void exec() {
        movie.mnew( COMMAND, url );
    }
}