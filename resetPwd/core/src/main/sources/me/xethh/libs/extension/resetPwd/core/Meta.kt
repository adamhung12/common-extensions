package me.xethh.libs.extension.resetPwd.core

open class SystemMeta(val systemCode:String, val systemName:String, val description : String){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SystemMeta

        if (systemCode != other.systemCode) return false
        if (systemName != other.systemName) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = systemCode.hashCode()
        result = 31 * result + systemName.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }
}
open class ModuleMeta(val moudleCode:String, val moduleName:String, val description : String , val systemMeta: SystemMeta){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModuleMeta

        if (moudleCode != other.moudleCode) return false
        if (moduleName != other.moduleName) return false
        if (description != other.description) return false
        if (systemMeta != other.systemMeta) return false

        return true
    }

    override fun hashCode(): Int {
        var result = moudleCode.hashCode()
        result = 31 * result + moduleName.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + systemMeta.hashCode()
        return result
    }
}
