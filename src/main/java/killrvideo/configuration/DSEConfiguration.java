package killrvideo.configuration;

import javax.inject.Inject;

import killrvideo.entity.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

@Configuration
public class DSEConfiguration {

    @Inject
    MappingManager manager;

    @Bean
    public Mapper<User> userMapper() {
        return manager.mapper(User.class);
    }

    @Bean
    public Mapper<CommentsByUser> commentsByUserMapper() { return manager.mapper(CommentsByUser.class); }

    @Bean
    public Mapper<CommentsByVideo> commentsByVideoMapper() { return manager.mapper(CommentsByVideo.class); }

//
//    @Bean
//    public EncodingJobNotification_Manager encodingJobNotificationManager() {
//        return managerFactory.forEncodingJobNotification();
//    }
//
    @Bean
    public Mapper<LatestVideos> latestVideosMapper() { return manager.mapper(LatestVideos.class); }
//
//    @Bean
//    public TagsByLetter_Manager tagsByLetterManager() {
//        return managerFactory.forTagsByLetter();
//    }
//
//    @Bean
//    public UploadedVideoDestinations_Manager uploadedVideoDestinationsManager() {
//        return managerFactory.forUploadedVideoDestinations();
//    }
//
//    @Bean
//    public UploadedVideoJobByJobId_Manager uploadedVideoJobByJobIdManager() {
//        return managerFactory.forUploadedVideoJobByJobId();
//    }
//
//    @Bean
//    public UploadedVideoJobs_Manager uploadedVideoJobsManager() {
//        return managerFactory.forUploadedVideoJobs();
//    }
//
    @Bean
    public Mapper<UserCredentials> userCredentialsMapper() {
        return manager.mapper(UserCredentials.class);
    }

    @Bean
    public Mapper<UserVideos> userVideosMapper() { return manager.mapper(UserVideos.class); }

    @Bean
    public Mapper<Video> videoMapper() { return manager.mapper(Video.class); }

//    @Bean
//    public VideoByTag_Manager videoByTagManager() {
//        return managerFactory.forVideoByTag();
//    }
//
    @Bean
    public Mapper<VideoPlaybackStats> videoPlaybackStatsMapper() { return manager.mapper(VideoPlaybackStats.class); }

    @Bean
    public Mapper<VideoRating> videoRatingMapper() { return manager.mapper(VideoRating.class); }

    @Bean
    public Mapper<VideoRatingByUser> videoRatingByUserMapper() { return manager.mapper(VideoRatingByUser.class); }
//
//    @Bean
//    public VideoRecommandationsByVideo_Manager videoRecommandationsByVideoManager() {
//        return managerFactory.forVideoRecommandationsByVideo();
//    }
//
//    @Bean
//    public VideoRecommendations_Manager videoRecommendationsManager() {
//        return managerFactory.forVideoRecommendations();
//    }
}
