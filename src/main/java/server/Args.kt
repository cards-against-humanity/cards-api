package server

open class Args {
    val mongoHost: String = System.getenv("MONGO_HOST") ?: "localhost"
    val mongoPort: Int = try { System.getenv("MONGO_PORT").toInt() } catch (e: Exception) { 27017 }
    val mongoDatabase: String = System.getenv("MONGO_DATABASE") ?: "cardsOnline"
    val elasticsearchHost: String = System.getenv("ELASTICSEARCH_HOST") ?: "localhost"
    val elasticsearchPort: Int = try { System.getenv("ELASTICSEARCH_PORT").toInt() } catch (e: Exception) { 9200 }
    val allowedCorsOrigin: String = System.getenv("ALLOWED_CORS_ORIGIN") ?: "http://localhost"
}