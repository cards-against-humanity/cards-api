package server

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import config.SwaggerConfig
import database.mongomodel.MongoDatabaseCollection
import elasticsearch.ElasticSearchableDatabaseCollection
import elasticsearch.SearchableDatabaseCollection
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import route.AuthInterceptor
import route.card.CardController
import route.search.SearchController
import route.user.UserController
import java.net.InetAddress
import java.util.*
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableAutoConfiguration(exclude = [MongoAutoConfiguration::class])
@SpringBootApplication
@ComponentScan(basePackageClasses = [UserController::class, CardController::class, SearchController::class, SwaggerConfig::class])
open class Main : WebMvcConfigurerAdapter() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Main::class.java, *args)
        }
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(AuthInterceptor())
    }

    @Bean
    open fun getDatabase(applicationArguments: ApplicationArguments): SearchableDatabaseCollection {
        val args = Args(applicationArguments.sourceArgs)
        val address = InetAddress.getByName(args.mongoHost)
        val databaseName = args.mongoDatabase
        val db = MongoClient(ServerAddress(address, args.mongoPort)).getDatabase(databaseName)
        return ElasticSearchableDatabaseCollection(MongoDatabaseCollection(db), InetAddress.getByName(args.elasticsearchHost), args.elasticsearchPort)
    }

    @Bean
    open fun corsConfigurer(applicationArguments: ApplicationArguments): WebMvcConfigurer {
        val args = Args(applicationArguments.sourceArgs)
        return object : WebMvcConfigurerAdapter() {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE").allowedOrigins(args.allowedCorsOrigin).allowedHeaders("*")
            }
        }
    }

    private inner class Args(args: Array<String>) {
        val mongoHost: String
        val mongoPort: Int
        val mongoDatabase: String
        val elasticsearchHost: String
        val elasticsearchPort: Int
        val allowedCorsOrigin: String

        init {
            val parsedArgs = parseArgs(args)
            this.mongoHost = parsedArgs["MONGO_HOST"] as String
            this.mongoPort = parsedArgs["MONGO_PORT"] as Int
            this.mongoDatabase = parsedArgs["MONGO_DATABASE"] as String
            this.elasticsearchHost = parsedArgs["ELASTICSEARCH_HOST"] as String
            this.elasticsearchPort = parsedArgs["ELASTICSEARCH_PORT"] as Int
            this.allowedCorsOrigin = parsedArgs["ALLOWED_CORS_ORIGIN"] as String
        }

        private fun parseArgs(args: Array<String>): Map<String, Any> {
            val argMap = HashMap<String, Any>()
            argMap["MONGO_HOST"] = "mongodb"
            argMap["MONGO_PORT"] = 27017
            argMap["MONGO_DATABASE"] = "appName"
            argMap["ELASTICSEARCH_HOST"] = "elasticsearch"
            argMap["ELASTICSEARCH_PORT"] = 9200
            argMap["ALLOWED_CORS_ORIGIN"] = "http://localhost"

            val argTypes = HashSet(Arrays.asList("MONGO_HOST", "MONGO_PORT", "MONGO_DATABASE", "ELASTICSEARCH_HOST", "ELASTICSEARCH_PORT", "ALLOWED_CORS_ORIGIN"))
            for (arg in args) {
                val key = arg.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                val value = arg.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                if (!argTypes.contains(key)) {
                    throw IllegalArgumentException("Invalid argument: $arg")
                }
                argMap.replace(key, value)
                if (key == "MONGO_PORT" || key == "ELASTICSEARCH_PORT") {
                    argMap.replace(key, Integer.parseInt(argMap[key] as String))
                }
            }
            return argMap
        }
    }
}