package killrvideo.configuration;

import javax.inject.Inject;

import killrvideo.entity.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

/**
 * Setting up mapping between entities and Cassandra UDT.
 * 
 * @author DataStax evangelist team.
 */
@Configuration
public class DseMapperConfiguration {

    @Inject
    private MappingManager manager;

    @Bean
    public Mapper<User> userMapper() {
        return manager.mapper(User.class);
    }

    @Bean
    public Mapper<CommentsByUser> commentsByUserMapper() { 
        return manager.mapper(CommentsByUser.class); 
    }

    @Bean
    public Mapper< CommentsByVideo > commentsByVideoMapper() { 
        return manager.mapper(CommentsByVideo.class); 
    }

    @Bean
    public Mapper< LatestVideos > latestVideosMapper() { 
        return manager.mapper(LatestVideos.class); 
    }

    @Bean
    public Mapper< UserCredentials > userCredentialsMapper() {
        return manager.mapper(UserCredentials.class);
    }

    @Bean
    public Mapper< UserVideos > userVideosMapper() { 
        return manager.mapper(UserVideos.class);
    }

    @Bean
    public Mapper< Video > videoMapper() { 
        return manager.mapper(Video.class); 
    }

    @Bean
    public Mapper< VideoPlaybackStats > videoPlaybackStatsMapper() { 
        return manager.mapper(VideoPlaybackStats.class); 
    }

    @Bean
    public Mapper < VideoRating > videoRatingMapper() { 
        return manager.mapper(VideoRating.class); 
    }

    @Bean
    public Mapper< VideoRatingByUser > videoRatingByUserMapper() { 
        return manager.mapper(VideoRatingByUser.class); 
    }
}
