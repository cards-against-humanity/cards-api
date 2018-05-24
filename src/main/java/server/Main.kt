package server

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import config.SwaggerConfig
import database.mongomodel.MongoDatabaseCollection
import elasticsearch.ElasticSearchableDatabaseCollection
import elasticsearch.SearchableDatabaseCollection
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import route.AuthInterceptor
import route.card.CardController
import route.search.SearchController
import route.user.UserController
import java.net.InetAddress
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
    open fun getDatabase(args: Args): SearchableDatabaseCollection {
        val address = InetAddress.getByName(args.mongoHost)
        val databaseName = args.mongoDatabase
        val db = MongoClient(ServerAddress(address, args.mongoPort)).getDatabase(databaseName)
        return ElasticSearchableDatabaseCollection(MongoDatabaseCollection(db), InetAddress.getByName(args.elasticsearchHost), args.elasticsearchPort, 30000)
    }

    @Bean
    open fun corsConfigurer(args: Args): WebMvcConfigurer {
        return object : WebMvcConfigurerAdapter() {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE").allowedOrigins(args.allowedCorsOrigin).allowedHeaders("*")
            }
        }
    }

    @Bean
    open fun getArgs(): Args {
        return Args()
    }

    @Component
    class Query(private val db: SearchableDatabaseCollection) : GraphQLQueryResolver {
        fun getUser(id: String) = db.getUser(id)
    }
}