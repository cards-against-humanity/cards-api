package server;

import config.SwaggerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import route.AuthInterceptor;
import route.card.CardController;
import route.search.SearchController;
import route.user.UserController;
import java.net.UnknownHostException;
import java.util.*;

@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
@SpringBootApplication @ComponentScan(basePackageClasses = { UserController.class, CardController.class, SearchController.class, SwaggerConfig.class })
public class Main extends WebMvcConfigurerAdapter {
    public static void main(String[] args) throws UnknownHostException {
        Map<String, Object> argMap = parseArgs(args);
        SpringApplication.run(Main.class, args);
    }

    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor());
    }

    private static Map<String, Object> parseArgs(String[] args) {
        Map<String, Object> argMap = new HashMap<>();
        argMap.put("MONGO_HOST", "localhost");
        argMap.put("MONGO_PORT", 27017);
        argMap.put("MONGO_DATABASE", "appName");
        argMap.put("ELASTICSEARCH_HOST", "localhost");
        argMap.put("ELASTICSEARCH_PORT", 9200);

        Set<String> argTypes = new HashSet<>(Arrays.asList("MONGO_HOST", "MONGO_PORT", "MONGO_DATABASE", "ELASTICSEARCH_HOST", "ELASTICSEARCH_PORT"));
        for (String arg : args) {
            String key = arg.split("=")[0];
            String value = arg.split("=")[1];
            if (!argTypes.contains(key)) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            argMap.replace(key, value);
            if (key.equals("MONGO_PORT") || key.equals("ELASTICSEARCH_PORT")) {
                argMap.replace(key, Integer.parseInt((String) argMap.get(key)));
            }
        }
        return argMap;
    }
}