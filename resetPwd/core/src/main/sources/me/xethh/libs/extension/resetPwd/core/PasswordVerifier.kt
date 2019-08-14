package me.xethh.libs.extension.resetPwd.core.utils

enum class PasswordType(val value:Int){
    UPPER_CASE(2), LOWER_CASE(4), DIGITS(1), SYMBOL(8), OTHER(16)
}
class PasswordVerifier {
    companion object{
        fun verify(pwd:String):Set<PasswordType>{

            val set = HashSet<PasswordType>()
            pwd.forEach { ch->
                when(ch.toInt()){
                    //Number
                    in 48..57-> set.add(PasswordType.DIGITS)
                    //Upper case
                    in 65..90-> set.add(PasswordType.UPPER_CASE)
                    //Lower case
                    in 97..122->set.add(PasswordType.LOWER_CASE)
                    in 33..47->set.add(PasswordType.SYMBOL)
                    in 58..64->set.add(PasswordType.SYMBOL)
                    in 91..96->set.add(PasswordType.SYMBOL)
                    in 123..126->set.add(PasswordType.SYMBOL)
                    else -> set.add(PasswordType.OTHER)
                }
            }

            return set
        }
    }
}