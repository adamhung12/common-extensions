package me.xethh.libs.extension.resetPwd.core

import me.xethh.libs.extension.resetPwd.core.utils.PasswordType
import me.xethh.libs.extension.resetPwd.core.utils.PasswordVerifier
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordVerifierTest{
    @Test
    fun base(){
        assertTrue(PasswordVerifier.verify("A").contains(PasswordType.UPPER_CASE))
        assertTrue(PasswordVerifier.verify("Z").contains(PasswordType.UPPER_CASE))

        assertTrue(PasswordVerifier.verify("a").contains(PasswordType.LOWER_CASE))
        assertTrue(PasswordVerifier.verify("z").contains(PasswordType.LOWER_CASE))

        assertTrue(PasswordVerifier.verify("0").contains(PasswordType.DIGITS))
        assertTrue(PasswordVerifier.verify("9").contains(PasswordType.DIGITS))
        assertTrue(PasswordVerifier.verify("1").contains(PasswordType.DIGITS))

        assertTrue(PasswordVerifier.verify("!").contains(PasswordType.SYMBOL))
        assertTrue(PasswordVerifier.verify("/").contains(PasswordType.SYMBOL))
        assertTrue(PasswordVerifier.verify(":").contains(PasswordType.SYMBOL))
        assertTrue(PasswordVerifier.verify("@").contains(PasswordType.SYMBOL))
        assertTrue(PasswordVerifier.verify("[").contains(PasswordType.SYMBOL))
        assertTrue(PasswordVerifier.verify("`").contains(PasswordType.SYMBOL))
        assertTrue(PasswordVerifier.verify("{").contains(PasswordType.SYMBOL))
        assertTrue(PasswordVerifier.verify("~").contains(PasswordType.SYMBOL))

        assertTrue(PasswordVerifier.verify("Abcd$%").containsAll(listOf(PasswordType.SYMBOL,PasswordType.UPPER_CASE,PasswordType.LOWER_CASE)))
        assertTrue(PasswordVerifier.verify("A123$%").containsAll(listOf(PasswordType.SYMBOL,PasswordType.UPPER_CASE,PasswordType.DIGITS)))
        assertFalse(PasswordVerifier.verify("Abcd$%").containsAll(listOf(PasswordType.SYMBOL,PasswordType.UPPER_CASE,PasswordType.LOWER_CASE,PasswordType.DIGITS)))

    }

}