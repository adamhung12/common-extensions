package me.xethh.libs.extension.resetPwd.core

import me.xethh.libs.extension.resetPwd.core.forgetPassword.ForgetPasswordProcessService
import me.xethh.libs.extension.resetPwd.core.forgetPassword.ForgetPasswordRequest
import me.xethh.libs.toolkits.webDto.core.response.status.ResponseStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@Import(value=[EnablePasswordResetInterfaceServiceImplConfig::class])
annotation class EnablePasswordResetInterfaceServiceTest
open class EnablePasswordResetInterfaceServiceImplConfig{
    @Bean
    open fun passwordResetService():PasswordResetService{
        return object:PasswordResetService{
            override fun reset(passwordResetRequestRequest: PasswordResetRequest) : PasswordResetResponse {
                println("Password reset method called. \n Content:"+passwordResetRequestRequest)
                return PasswordResetResponse(ResponseStatus.OK, "The password reset successfully",BaseError(
                        "1",
                        "error",
                        arrayOf<String>()
                ))
            }
        }
    }

    @Bean
    open fun forgetPasswordProcessService(): ForgetPasswordProcessService{
        return object : ForgetPasswordProcessService{
            override fun process(forgetPasswordRequest: ForgetPasswordRequest): PasswordResetResponse {

                return PasswordResetResponse(ResponseStatus.OK, "", BaseError.NoBaseError())
            }
        }
    }
}
