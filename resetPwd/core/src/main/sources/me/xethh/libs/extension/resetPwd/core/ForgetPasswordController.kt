package me.xethh.libs.extension.resetPwd.core.forgetPassword

import com.fasterxml.jackson.annotation.JsonInclude
import me.xethh.libs.extension.resetPwd.core.*
import me.xethh.libs.toolkits.webDto.core.request.Request
import me.xethh.libs.toolkits.webDto.core.response.Response
import me.xethh.libs.toolkits.webDto.core.response.status.ResponseStatus
import me.xethh.utils.wrapper.Tuple3
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.util.*

class ForgetPasswordResponse(status: ResponseStatus, result:String, err: BaseError)
    : Response<String, BaseError>(status, result, err)

class ForgetPasswordRequestData(val id:String?, val username:String, val newPassword:String?)
class ForgetPasswordRequest() : Request<ForgetPasswordRequestData>()

interface ForgetPasswordProcessService{
    fun process(forgetPasswordRequest: ForgetPasswordRequest) : PasswordResetResponse
}
interface ForgetPasswordService{
    fun getInfo(id: String) : ForgetPasswordRequest
    fun removeInfo(id: String) : ForgetPasswordRequest
    fun register(forgetPasswordRequest: ForgetPasswordRequest) : String
}

class RPSys:SystemMeta("RP","ResetPwd", "System for reset password")
class ForgetPassowrdModule : ModuleMeta("FP","Forgot Password", "Forget password module", RPSys()){
    companion object{
        val SELF = ForgetPassowrdModule()

        val REQUEST_ALREADY_CLAIM = "The request is already claimed"
        val REQUEST_ALREADY_TIMEOUT = "The request is already timeout"
        val REQUEST_NOT_FOUND = "The request does not found"
        val REQUEST_META_DATA_NOT_MATCH = "The request meta data not match"
        val REQUEST_RESET_FAIL = "The reset process is failed"
    }
    class RequestClaimedRequest(array:Array<String>) : BaseError(SELF, "00001", REQUEST_ALREADY_CLAIM,array)
    class RequestTimeoutRequest(array:Array<String>) : BaseError(SELF, "00002", REQUEST_ALREADY_TIMEOUT,array)
    class RequestNotFoundRequest(array:Array<String>) : BaseError(SELF, "00003", REQUEST_NOT_FOUND,array)
    class RequestMetaDataNotMatch(array:Array<String>) : BaseError(SELF, "00004", REQUEST_META_DATA_NOT_MATCH,array)
    class RequestResetFailed(array:Array<String>) : BaseError(SELF, "00005", REQUEST_RESET_FAIL,array)

}


class ForgetPasswordServiceImpl() : ForgetPasswordService{
    val map = HashMap<String, Tuple3<Date, Boolean, ForgetPasswordRequest>>()

    override fun getInfo(id: String): ForgetPasswordRequest {
        val rs = map[id]
        rs?.let {
            if(Date().time-rs.v1.time > Duration.ofMinutes(30).toMillis()){
                throw ForgetPassowrdModule.RequestTimeoutRequest(arrayOf()).toException()
            }
            else{
                return rs.v3
            }
        }
        throw ForgetPassowrdModule.RequestMetaDataNotMatch(arrayOf()).toException()
    }

    override fun removeInfo(id: String): ForgetPasswordRequest {
        val rs = map[id]
        rs?.let {
            if(rs.v2==true){
                throw ForgetPassowrdModule.RequestClaimedRequest(arrayOf()).toException();
            }
            else if(Date().time-rs.v1.time > Duration.ofMinutes(30).seconds){
                throw ForgetPassowrdModule.RequestTimeoutRequest(arrayOf()).toException()
            }
            else if(rs.v3==null){

            }
            else{
                map.replace(id, Tuple3.of(rs.v1, true, rs.v3))
                return rs.v3
            }
        }
        throw ForgetPassowrdModule.RequestMetaDataNotMatch(arrayOf()).toException()
    }

    override fun register(forgetPasswordRequest: ForgetPasswordRequest): String {
        var uuid = UUID.randomUUID()
        while(map.containsKey(uuid.toString()))
            uuid = UUID.randomUUID()

        forgetPasswordRequest.data = ForgetPasswordRequestData(uuid.toString(), forgetPasswordRequest.data.username, "")
        map.put(uuid.toString(), Tuple3.of(Date(),false,forgetPasswordRequest))
        return forgetPasswordRequest.data.id!!
    }

}

@RestController
@RequestMapping(value = ["/password/reset/forget"])
class ForgetPasswordController(
        @Autowired val forgetPasswordService: ForgetPasswordService,
        @Autowired val forgetPasswordProcessService: ForgetPasswordProcessService
) {

    @RequestMapping(value = [""], method = [RequestMethod.GET])
    fun  get(@RequestParam id:String): ForgetPasswordResponse {
        try{
            val data =  forgetPasswordService.getInfo(id)
            return ForgetPasswordResponse(ResponseStatus.OK, data.data.username, BaseError.NoBaseError())
        }
        catch (ex:BaseException){
            return ForgetPasswordResponse(ResponseStatus.BusinessError, "", ex.baseError);
        }
    }
    @RequestMapping(value = [""], method = [RequestMethod.POST])
    fun  post(@RequestBody forgetPasswordRequest: ForgetPasswordRequest): ForgetPasswordResponse {
        val id =  forgetPasswordService.register(forgetPasswordRequest)
        return ForgetPasswordResponse(ResponseStatus.OK, id, BaseError.NoBaseError())
    }
    @RequestMapping(value = [""], method = [RequestMethod.DELETE])
    fun  delete(@RequestBody forgetPasswordRequest: ForgetPasswordRequest): ForgetPasswordResponse {
        val temp = forgetPasswordService.getInfo(forgetPasswordRequest.data.id!!)
        if(temp.data.username==forgetPasswordRequest.data.username){
            val res = forgetPasswordProcessService.process(temp)
            if(res.status==ResponseStatus.OK){
                return ForgetPasswordResponse(ResponseStatus.OK, temp.data.id!!, BaseError.NoBaseError())
            }
            else{
                return ForgetPasswordResponse(ResponseStatus.BusinessError, temp.data.id!!, res.error)
            }
        }
        return ForgetPasswordResponse(ResponseStatus.BusinessError, temp.data.id!!, ForgetPassowrdModule.RequestMetaDataNotMatch(arrayOf()))
    }

}

@Import(value=[EnableForgetPasswordInterfaceConfig::class])
annotation class EnableForgetPasswordInterface
open class EnableForgetPasswordInterfaceConfig{
    @Bean
    open fun forgetPasswordController(
            @Autowired forgetPasswordService: ForgetPasswordService,
            @Autowired processService: ForgetPasswordProcessService
    ): ForgetPasswordController {
        return ForgetPasswordController(forgetPasswordService, processService)
    }

    @Bean
    open fun forgetPasswordService():ForgetPasswordService{
        return ForgetPasswordServiceImpl()
    }
}
