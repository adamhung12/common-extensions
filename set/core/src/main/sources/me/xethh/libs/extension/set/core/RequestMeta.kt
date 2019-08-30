package me.xethh.libs.extension.set.core

import me.xethh.libs.toolkits.webDto.core.MetaEntity

interface RequestMeta {
    val id: String
    val metaEntity: MetaEntity
}
open class RequestMetaImpl(override var id:String, override var metaEntity: MetaEntity) : RequestMeta