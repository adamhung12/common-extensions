package me.xethh.libs.extension.resetPwd.core

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import me.xethh.libs.toolkits.webDto.core.category.Confidential
import me.xethh.libs.toolkits.webDto.core.request.Request
import me.xethh.libs.toolkits.webDto.core.response.Response
import me.xethh.libs.toolkits.webDto.core.response.err.BaseError
import me.xethh.libs.toolkits.webDto.core.response.status.ResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

//open class BaseException : CommonException{
//    val baseError:BaseError
//    constructor(baseError: BaseError):super() { this.baseError = baseError}
//    constructor(baseError: BaseError, message: String):super(message) { this.baseError = baseError}
//    constructor(baseError: BaseError, message: String, cause: Throwable) : super(message, cause) { this.baseError = baseError}
//    constructor(baseError: BaseError, cause: Throwable) : super(cause) { this.baseError = baseError}
//    constructor(baseError: BaseError, message: String, cause: Throwable, enableSuppression: Boolean, writableStackTrace: Boolean)
//            :super(message, cause, enableSuppression, writableStackTrace){
//        this.baseError = baseError
//    }
//
//}

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(value = [
    JsonSubTypes.Type(value = RequestPasswordNormal::class, name = "normal"),
    JsonSubTypes.Type(value = PasswordResetRequestAdvanced::class, name = "advanced")
])
abstract class PasswordResetData(val username:String, val existingPwd:String, val newPwd:String, val type:String) : Confidential.ConfidentialAbs()
class RequestPasswordNormal(username: String, existingPwd: String, newPwd: String) : PasswordResetData(username, existingPwd, newPwd, "noraml"){
    override fun confidentialToString(): String {
        return "RequestPassword(username='$username', existingPwd='${existingPwd.map { '*' }.joinToString(separator = "") }', newPwd='${newPwd.map { '*' }.joinToString(separator = "")}')"
    }
}
class PasswordResetRequestAdvanced(val adminUsername:String, val adminPassword:String, username: String, existingPwd: String, newPwd: String) : PasswordResetData(username, existingPwd, newPwd, "advanced"){
    override fun confidentialToString(): String {
        return "RequestPasswordAdvanced(adminUsername='$adminUsername', adminPassword='$adminPassword', username='$username', existingPwd='${existingPwd.map { '*' }.joinToString(separator = "") }', newPwd='${newPwd.map { '*' }.joinToString(separator = "")}')"
    }
}
class PasswordResetRequest() : Request<PasswordResetData>()
class PasswordResetResponse(status: ResponseStatus, result:String, err: BaseError)
    : Response<String, BaseError>(status, result, err)


interface PasswordResetService{
    fun reset(passwordResetRequest: PasswordResetRequest):PasswordResetResponse
}

@RestController
@RequestMapping(value = ["/password/reset"])
class PasswordResetController(@Autowired val pwdResetService:PasswordResetService) {
    @RequestMapping(value = [""], method = [RequestMethod.POST])
    fun  post(@RequestBody passwordResetRequest: PasswordResetRequest): PasswordResetResponse {
        return pwdResetService.reset(passwordResetRequest)
    }

}

@Import(value=[EnablePasswordResetInterfaceConfig::class])
annotation class EnablePasswordResetInterface
open class EnablePasswordResetInterfaceConfig{
    @Bean
    open fun passwordResetController(@Autowired pwdResetService: PasswordResetService): PasswordResetController {
        return PasswordResetController(pwdResetService)
    }
}



