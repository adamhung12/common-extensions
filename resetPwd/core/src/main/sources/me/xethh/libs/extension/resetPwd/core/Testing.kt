package me.xethh.libs.extension.resetPwd.core

import me.xethh.libs.extension.resetPwd.core.forgetPassword.*
import me.xethh.libs.toolkits.logging.WithLogger
import me.xethh.libs.toolkits.webDto.core.response.status.ResponseStatus
import me.xethh.utils.wrapper.Tuple4
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import java.time.Duration

import java.util.Timer
import kotlin.concurrent.schedule
@Import(value=[EnablePasswordResetInterfaceServiceImplConfig::class])
annotation class EnablePasswordResetInterfaceServiceTest
open class EnablePasswordResetInterfaceServiceImplConfig{
    @Bean
    open fun passwordResetService():PasswordResetService{
        return object:PasswordResetService{
            override fun reset(passwordResetRequestRequest: PasswordResetRequest) : PasswordResetResponse {
                println("Password reset method called. \n Content:"+passwordResetRequestRequest)
                return PasswordResetResponse(ResponseStatus.OK, "The password reset successfully",PasswordResetError.NoBaseError())
            }
        }
    }

    @Bean
    open fun forgetPasswordProcessService(): ForgetPasswordProcessService{
        return object : ForgetPasswordProcessService{
            override fun process(forgetPasswordRequest: ForgetPasswordRequest): PasswordResetResponse {

                return PasswordResetResponse(ResponseStatus.OK, "", PasswordResetError.NoBaseError())
            }
        }
    }

    @Bean
    open fun passwordResetScheduleService(
            @Autowired forgetPasswordService: ForgetPasswordServiceImpl,
            @Autowired passwordResetNotificationService: PasswordResetNotificationService
    ):PasswordResetScheduleService{
        return object: PasswordResetScheduleService, WithLogger{

            init {
                Timer("Send notification", false).schedule(500, Duration.ofMinutes(1).toMillis()) {
                    logger().info("Start send notification");
                    val toBeSent = forgetPasswordService.map.filter { it.value.v2==false }
                    toBeSent.forEach{
                        logger().info("Send notification for request[${it.value.v4.data.id}]")
                        passwordResetNotificationService.notify(it.value.v4)
                        forgetPasswordService.map.replace(it.key, Tuple4.of(it.value.v1, true, it.value.v3, it.value.v4))
                    }
                }
            }
        }
    }

    @Bean
    open fun passwordResetNotificationService():PasswordResetNotificationService{
        return object: PasswordResetNotificationService, WithLogger{
            override fun notify(forgetPasswordRequest: ForgetPasswordRequest) {
                logger().info("Sending notification for request[${forgetPasswordRequest.data.id}]")
            }
        }
    }
}
