import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.servlet.ResultActions
import java.util.ArrayList
import java.util.HashMap

@Throws(Exception::class)
fun toMap(result: ResultActions): Map<String, Any> {
    return ObjectMapper().readValue(result.andReturn().response.contentAsString, HashMap::class.java) as Map<String, Any>
}

@Throws(Exception::class)
fun toList(result: ResultActions): List<Any> {
    return ObjectMapper().readValue(result.andReturn().response.contentAsString, ArrayList::class.java)
}

@Throws(Exception::class)
fun resEquals(result: ResultActions, obj: Any): Boolean {
    return obj == toMap(result)
}