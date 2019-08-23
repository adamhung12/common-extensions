package me.xethh.libs.extension.resetPwd.core

import me.xethh.libs.toolkits.sysMeta.ModuleMeta
import me.xethh.libs.toolkits.sysMeta.SystemMeta
import me.xethh.libs.toolkits.webDto.core.response.err.BaseError
import me.xethh.libs.toolkits.webDto.core.response.err.BaseErrorException

class RPSys: SystemMeta("RP","ResetPwd", "System for reset password")
open class PasswordResetError(errorCode: String, errMessage: String, inputs: Array<String>) : BaseError(ForgetPasswordModule.SELF, errorCode, errMessage, inputs){
    fun toException() : BaseErrorException {
        return BaseErrorException(this)
    }
    class NoBaseError(): PasswordResetError("","",arrayOf())
}
class ForgetPasswordModule : ModuleMeta("FP","Forgot Password", "Forget password module", RPSys()){
    companion object{
        val SELF = ForgetPasswordModule()
    }
    class RequestClaimedRequest(array:Array<String>) : PasswordResetError("00001", "The request is already claimed",array)
    class RequestTimeoutRequest(array:Array<String>) : PasswordResetError("00002", "The request is already timeout",array)
    class RequestNotFoundRequest(array:Array<String>) : PasswordResetError( "00003", "The request does not found",array)
    class RequestMetaDataNotMatch(array:Array<String>) : PasswordResetError( "00004", "The request meta data not match",array)
    class RequestResetFailed(array:Array<String>) : PasswordResetError( "00005", "The reset process is failed",array)

}

