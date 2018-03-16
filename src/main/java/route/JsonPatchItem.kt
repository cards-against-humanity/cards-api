package route

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

class JsonPatchItem {
    @JsonProperty("op")    var op:    String? = null @JsonIgnore private set
    @JsonProperty("path")  var path:  String? = null @JsonIgnore private set
    @JsonProperty("value") var value: Object? = null @JsonIgnore private set
}