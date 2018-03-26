package server;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import config.SwaggerConfig;
import database.DatabaseCollection;
import database.mongomodel.MongoDatabaseCollection;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import route.AuthInterceptor;
import route.card.CardController;
import route.search.SearchController;
import route.user.UserController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
@SpringBootApplication @ComponentScan(basePackageClasses = { UserController.class, CardController.class, SearchController.class, SwaggerConfig.class })
public class Main extends WebMvcConfigurerAdapter {
    public static void main(String[] args) throws UnknownHostException {
        SpringApplication.run(Main.class, args);
    }

    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor());
    }

    @Bean
    DatabaseCollection getDatabase(ApplicationArguments applicationArguments) throws UnknownHostException {
        Args args = new Args(applicationArguments.getSourceArgs());
        InetAddress address = InetAddress.getByName(args.getMongoHost());
        String databaseName = args.getMongoDatabase();
        MongoDatabase db = new MongoClient(new ServerAddress(address, args.getMongoPort())).getDatabase(databaseName);
        return new MongoDatabaseCollection(db);
    }

    private class Args {
        private String mongoHost;
        private int mongoPort;
        private String mongoDatabase;
        private String elasticsearchHost;
        private int elasticsearchPort;

        public Args(String[] args) {
            Map<String, Object> parsedArgs = parseArgs(args);
            this.mongoHost = (String) parsedArgs.get("MONGO_HOST");
            this.mongoPort = (int) parsedArgs.get("MONGO_PORT");
            this.mongoDatabase = (String) parsedArgs.get("MONGO_DATABASE");
            this.elasticsearchHost = (String) parsedArgs.get("ELASTICSEARCH_HOST");
            this.elasticsearchPort = (int) parsedArgs.get("ELASTICSEARCH_PORT");
        }

        public String getMongoHost() {
            return this.mongoHost;
        }
        public int getMongoPort() {
            return this.mongoPort;
        }
        public String getMongoDatabase() {
            return this.mongoDatabase;
        }
        public String getElasticsearchHost() {
            return this.elasticsearchHost;
        }
        public int getElasticsearchPort() {
            return this.elasticsearchPort;
        }

        private Map<String, Object> parseArgs(String[] args) {
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
}