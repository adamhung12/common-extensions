package me.xethh.libs.extension.resetPwd.core.forgetPassword

import me.xethh.libs.extension.resetPwd.core.ForgetPasswordModule
import me.xethh.libs.extension.resetPwd.core.PasswordResetError
import me.xethh.libs.extension.resetPwd.core.PasswordResetResponse
import me.xethh.libs.toolkits.webDto.core.request.Request
import me.xethh.libs.toolkits.webDto.core.response.Response
import me.xethh.libs.toolkits.webDto.core.response.err.BaseError
import me.xethh.libs.toolkits.webDto.core.response.err.BaseErrorException
import me.xethh.libs.toolkits.webDto.core.response.status.ResponseStatus
import me.xethh.utils.wrapper.Tuple3
import me.xethh.utils.wrapper.Tuple4
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.util.*

class ForgetPasswordResponse(status: ResponseStatus, result:ForgetPasswordRequest?, err: BaseError)
    : Response<ForgetPasswordRequest, BaseError>(status, result, err)

class ForgetPasswordRequestData(val id:String?, val username:String, val newPassword:String?)
class ForgetPasswordRequest() : Request<ForgetPasswordRequestData>()

interface PasswordResetScheduleService{
}
interface PasswordResetNotificationService{
    fun notify(forgetPasswordRequest: ForgetPasswordRequest) : Unit
}
interface ForgetPasswordProcessService{
    fun process(forgetPasswordRequest: ForgetPasswordRequest) : PasswordResetResponse
}
interface ForgetPasswordService{
    fun getInfo(id: String) : ForgetPasswordRequest
    fun removeInfo(id: String) : ForgetPasswordRequest
    fun register(forgetPasswordRequest: ForgetPasswordRequest) : ForgetPasswordRequest
}


class ForgetPasswordServiceImpl() : ForgetPasswordService{
    val map = HashMap<String, Tuple4<Date, Boolean, Boolean, ForgetPasswordRequest>>()

    override fun getInfo(id: String): ForgetPasswordRequest {
        val rs = map[id]
        rs?.let {
            if(Date().time-rs.v1.time > Duration.ofMinutes(30).toMillis()){
                throw ForgetPasswordModule.RequestTimeoutRequest(arrayOf()).toException()
            }
            else{
                return rs.v4
            }
        }
        throw ForgetPasswordModule.RequestMetaDataNotMatch(arrayOf()).toException()
    }

    override fun removeInfo(id: String): ForgetPasswordRequest {
        val rs = map[id]
        rs?.let {
            if(rs.v2==true){
                throw ForgetPasswordModule.RequestClaimedRequest(arrayOf()).toException();
            }
            else if(Date().time-rs.v1.time > Duration.ofMinutes(30).seconds){
                throw ForgetPasswordModule.RequestTimeoutRequest(arrayOf()).toException()
            }
            else if(rs.v3==null){

            }
            else{
                map.replace(id, Tuple4.of(rs.v1, rs.v2, true, rs.v4))
                return rs.v4
            }
        }
        throw ForgetPasswordModule.RequestMetaDataNotMatch(arrayOf()).toException()
    }

    override fun register(forgetPasswordRequest: ForgetPasswordRequest): ForgetPasswordRequest {
        var uuid = UUID.randomUUID()
        while(map.containsKey(uuid.toString()))
            uuid = UUID.randomUUID()

        forgetPasswordRequest.data = ForgetPasswordRequestData(uuid.toString(), forgetPasswordRequest.data.username, "")
        map.put(uuid.toString(), Tuple4.of(Date(), false,false,forgetPasswordRequest))
        return forgetPasswordRequest
    }

}

@RestController
@RequestMapping(value = ["/password/forget/claim"])
class ForgetPasswordClaimController(
        @Autowired val forgetPasswordService: ForgetPasswordService,
        @Autowired val forgetPasswordProcessService: ForgetPasswordProcessService
){
    @RequestMapping(value = [""], method = [RequestMethod.POST])
    fun  post(@RequestBody forgetPasswordRequest: ForgetPasswordRequest): ForgetPasswordResponse {
        val temp = forgetPasswordService.getInfo(forgetPasswordRequest.data.id!!)
        if(temp.data.username==forgetPasswordRequest.data.username){
            val res = forgetPasswordProcessService.process(temp)
            if(res.status==ResponseStatus.OK){
                return ForgetPasswordResponse(ResponseStatus.OK, temp, PasswordResetError.NoBaseError())
            }
            else{
                return ForgetPasswordResponse(ResponseStatus.BusinessError, temp, res.error)
            }
        }
        return ForgetPasswordResponse(ResponseStatus.BusinessError, temp, ForgetPasswordModule.RequestMetaDataNotMatch(arrayOf()))
    }
}
@RestController
@RequestMapping(value = ["/password/forget"])
class ForgetPasswordController(
        @Autowired val forgetPasswordService: ForgetPasswordService,
        @Autowired val forgetPasswordProcessService: ForgetPasswordProcessService,
        @Autowired val passwordResetScheduleService: PasswordResetScheduleService,
        @Autowired val passwordResetNotificationService: PasswordResetNotificationService
) {

    @RequestMapping(value = [""], method = [RequestMethod.GET])
    fun  get(@RequestParam id:String): ForgetPasswordResponse {
        try{
            val data =  forgetPasswordService.getInfo(id)
            return ForgetPasswordResponse(ResponseStatus.OK, data, PasswordResetError.NoBaseError())
        }
        catch (ex:BaseErrorException){
            return ForgetPasswordResponse(ResponseStatus.BusinessError, null, ex.baseError);
        }
    }
    @RequestMapping(value = [""], method = [RequestMethod.POST])
    fun  post(@RequestBody forgetPasswordRequest: ForgetPasswordRequest): ForgetPasswordResponse {
        val id =  forgetPasswordService.register(forgetPasswordRequest)
        return ForgetPasswordResponse(ResponseStatus.OK, forgetPasswordRequest, PasswordResetError.NoBaseError())
    }

}

@Import(value=[EnableForgetPasswordInterfaceConfig::class])
annotation class EnableForgetPasswordInterface
open class EnableForgetPasswordInterfaceConfig{
    @Bean
    open fun forgetPasswordController(
            @Autowired forgetPasswordService: ForgetPasswordService,
            @Autowired processService: ForgetPasswordProcessService,
            @Autowired passwordResetScheduleService: PasswordResetScheduleService,
            @Autowired passwordResetNotificationService: PasswordResetNotificationService
    ): ForgetPasswordController {
        return ForgetPasswordController(forgetPasswordService, processService, passwordResetScheduleService, passwordResetNotificationService)
    }

    @Bean
    open fun forgetPasswordClaim(
            @Autowired forgetPasswordService: ForgetPasswordService,
            @Autowired processService: ForgetPasswordProcessService
    ):ForgetPasswordClaimController{
        return ForgetPasswordClaimController(forgetPasswordService, processService)
    }

    @Bean
    open fun forgetPasswordService():ForgetPasswordService{
        return ForgetPasswordServiceImpl()
    }
}
