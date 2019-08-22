package me.xethh.libs.extension.resetPwd.core

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import me.xethh.libs.toolkits.exceptions.CommonException
import me.xethh.libs.toolkits.webDto.core.category.Confidential
import me.xethh.libs.toolkits.webDto.core.request.Request
import me.xethh.libs.toolkits.webDto.core.response.Response
import me.xethh.libs.toolkits.webDto.core.response.err.ResponseError
import me.xethh.libs.toolkits.webDto.core.response.status.ResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

open class BaseException : CommonException{
    val baseError:BaseError
    constructor(baseError: BaseError):super() { this.baseError = baseError}
    constructor(baseError: BaseError, message: String):super(message) { this.baseError = baseError}
    constructor(baseError: BaseError, message: String, cause: Throwable) : super(message, cause) { this.baseError = baseError}
    constructor(baseError: BaseError, cause: Throwable) : super(cause) { this.baseError = baseError}
    constructor(baseError: BaseError, message: String, cause: Throwable, enableSuppression: Boolean, writableStackTrace: Boolean)
            :super(message, cause, enableSuppression, writableStackTrace){
        this.baseError = baseError
    }

}
open class BaseError(errCode:String, errMessage:String, inputs:Array<String>) : ResponseError(){
    constructor(moduleMeta: ModuleMeta, custCode:String, errMessage: String, inputs: Array<String>)
            : this(moduleMeta.systemMeta.systemCode+"-"+moduleMeta.moudleCode+"-"+custCode, errMessage, inputs) {
    }
    init {
        this.errCode = errCode
        message = when(inputs.size){
            0 -> errMessage
            1 -> String.format(errMessage, inputs[0])
            2 -> String.format(errMessage, inputs[0], inputs[1])
            3 -> String.format(errMessage, inputs[0], inputs[1], inputs[2])
            4 -> String.format(errMessage, inputs[0], inputs[1], inputs[2], inputs[3])
            5 -> String.format(errMessage, inputs[0], inputs[1], inputs[2], inputs[3], inputs[4])
            6 -> String.format(errMessage, inputs[0], inputs[1], inputs[2], inputs[3], inputs[4], inputs[5])
            7 -> String.format(errMessage, inputs[0], inputs[1], inputs[2], inputs[3], inputs[4], inputs[5], inputs[6])
            8 -> String.format(errMessage, inputs[0], inputs[1], inputs[2], inputs[3], inputs[4], inputs[5], inputs[6], inputs[7])
            9 -> String.format(errMessage, inputs[0], inputs[1], inputs[2], inputs[3], inputs[4], inputs[5], inputs[6], inputs[7], inputs[8])
            10 -> String.format(errMessage, inputs[0], inputs[1], inputs[2], inputs[3], inputs[4], inputs[5], inputs[6], inputs[7], inputs[8], inputs[9])
            else -> throw RuntimeException("PasswordResetError fail to be created, parameter input[${inputs.size} - $inputs] not supported")
        }
    }

    fun toException() : BaseException{
        return BaseException(this)
    }

    class NoBaseError():BaseError("","",arrayOf())
}

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(value = [
    JsonSubTypes.Type(value = RequestPasswordNormal::class, name = "normal"),
    JsonSubTypes.Type(value = PassworResetRequestdAdvanced::class, name = "advanced")
])
abstract class PasswordResetData(val username:String, val existingPwd:String, val newPwd:String, val type:String) : Confidential.ConfidentialAbs()
class RequestPasswordNormal(username: String, existingPwd: String, newPwd: String) : PasswordResetData(username, existingPwd, newPwd, "noraml"){
    override fun confidentialToString(): String {
        return "RequestPassword(username='$username', existingPwd='${existingPwd.map { '*' }.joinToString(separator = "") }', newPwd='${newPwd.map { '*' }.joinToString(separator = "")}')"
    }
}
class PassworResetRequestdAdvanced(val adminUsername:String, val adminPassword:String, username: String, existingPwd: String, newPwd: String) : PasswordResetData(username, existingPwd, newPwd, "advanced"){
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
@RequestMapping(value = ["/password/reset/request"])
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



