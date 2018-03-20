package route.user.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class UserModelSerializer(t: Class<UserModel>?) : StdSerializer<UserModel>(t) {
    override fun serialize(model: UserModel, jgen: JsonGenerator, provider: SerializerProvider) {
        jgen.writeStartObject()
        jgen.writeStringField("id", model.getId())
        jgen.writeStringField("name", model.getName())
        jgen.writeStringField("oAuthId", model.getOAuthId())
        jgen.writeStringField("oAuthProvider", model.getOAuthProvider())
        jgen.writeEndObject()
    }
}