package me.xethh.libs.extension.resetPwd.core.test;

import me.xethh.libs.extension.resetPwd.core.EnablePasswordResetInterface;
import me.xethh.libs.extension.resetPwd.core.EnablePasswordResetInterfaceConfig;
import me.xethh.libs.extension.resetPwd.core.EnablePasswordResetInterfaceServiceTest;
import me.xethh.libs.extension.resetPwd.core.forgetPassword.EnableForgetPasswordInterface;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Base configuration for spring
 */
@Configuration
//@EnableWebMvc
//@ComponentScan("me.xethh.libs.extension.resetPwd.core")
//@Import(value=EnablePasswordResetInterfaceConfig.class)
@EnablePasswordResetInterface
@EnableForgetPasswordInterface
@EnablePasswordResetInterfaceServiceTest
public class AppSpringConfig
{
}
