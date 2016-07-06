package killrvideo.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import killrvideo.configuration.Profiles;

@ComponentScan
@EnableAutoConfiguration
public class KillrVideoServer {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(KillrVideoServer.class);
        app.setWebEnvironment(false);
        app.setShowBanner(false);

        SimpleCommandLinePropertySource source = new SimpleCommandLinePropertySource(args);

        // Check if the selected profile has been set as argument.
        // if not the development profile will be added
        addDefaultProfile(app, source);
        app.run(args);
    }

    private static void addDefaultProfile(SpringApplication app, SimpleCommandLinePropertySource source) {
        if (!source.containsProperty("spring.profiles.active")) {
            app.setAdditionalProfiles(Profiles.SPRING_PROFILE_DEVELOPMENT);
        }
    }

}