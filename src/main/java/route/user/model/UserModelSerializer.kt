package route.user.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class UserModelSerializer(t: Class<UserModel>?) : StdSerializer<UserModel>(t) {
    override fun serialize(model: UserModel, jgen: JsonGenerator, provider: SerializerProvider) {
        jgen.writeStartObject()
        jgen.writeStringField("id", model.id)
        jgen.writeStringField("name", model.name)
        jgen.writeStringField("oAuthId", model.oAuthId)
        jgen.writeStringField("oAuthProvider", model.oAuthProvider)
        jgen.writeEndObject()
    }
}