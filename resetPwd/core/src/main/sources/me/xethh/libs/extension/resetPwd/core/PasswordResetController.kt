package me.xethh.libs.extension.resetPwd.core

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

open class PasswordResetError(errCode:String, errMessage:String, inputs:Array<String>) : ResponseError(){
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

    class NoPasswordResetError():PasswordResetError("","",arrayOf())
}

data class RequestPassword(val username:String, val existingPwd:String, val newPwd:String) : Confidential.ConfidentialAbs() {
    override fun confidentialToString(): String {
        return "RequestPassword(username='$username', existingPwd='${existingPwd.map { '*' }.joinToString(separator = "") }}', newPwd='${newPwd.map { '*' }.joinToString(separator = "")}')"
    }

    override fun toString(): String {
        return super.toString()
    }
}

class PasswordResetRequest() : Request<RequestPassword>() {

}

class PasswordResetResponse(status: ResponseStatus, result:String, err: PasswordResetError)
    : Response<String, PasswordResetError>(status, result, err)


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


@Import(value=[EnablePasswordResetInterfaceServiceImplConfig::class])
annotation class EnablePasswordResetInterfaceServiceTest
open class EnablePasswordResetInterfaceServiceImplConfig{
    @Bean open fun passwordResetService():PasswordResetService{
        return object:PasswordResetService{
            override fun reset(passwordResetRequestRequest: PasswordResetRequest) : PasswordResetResponse {
               println("Password reset method called")
               return PasswordResetResponse(ResponseStatus.OK, "The password reset successfully",PasswordResetError(
                       "1",
                       "error",
                       arrayOf<String>()
               ))
            }
        }
    }
}

