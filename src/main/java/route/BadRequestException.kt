package route

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
@ResponseBody
class BadRequestException(reason: String) : Exception(reason)