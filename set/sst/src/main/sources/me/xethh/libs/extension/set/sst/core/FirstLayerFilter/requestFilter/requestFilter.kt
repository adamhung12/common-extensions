package me.xethh.libs.extension.set.sst.core.FirstLayerFilter.requestFilter

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest

class VerifyingRequest{
    lateinit var method:String
    lateinit var uri:String
    lateinit var url:String
    lateinit var sourceIp:String
    lateinit var sourceHost:String
    var sourcePort:Int = -1
    lateinit var localIp:String
    lateinit var localHost:String
    var localPort:Int = -1

    constructor(){}
    constructor(httpServletRequest: HttpServletRequest){
        method = httpServletRequest.method
        url = httpServletRequest.requestURL.toString()
        uri = httpServletRequest.requestURI
        sourceIp = httpServletRequest.remoteAddr
        sourceHost = httpServletRequest.remoteHost
        sourcePort = httpServletRequest.remotePort
        localIp = httpServletRequest.localAddr
        localHost = httpServletRequest.localName
        localPort = httpServletRequest.localPort

    }

    override fun toString(): String {
        return "VerifyingRequest(method='$method', uri='$uri', url='$url', sourceIp='$sourceIp', sourceHost='$sourceHost', sourcePort=$sourcePort, localIp='$localIp', localHost='$localHost', localPort=$localPort)"
    }


}

class SSTRequestURLVerifier(list:List<SSTRequestURLFilter>){
    val head:SSTRequestURLFilter = object : SSTRequestURLFilter() {
        override fun isSupport(request: VerifyingRequest): Boolean {
            return false
        }
    }
    var tail:SSTRequestURLFilter = head

    init {
        list.forEach{ tail.next = it; tail = it}
    }
    fun verify(request: VerifyingRequest):Boolean {
        return head.isSupportChain(request)
    }
}

abstract class SSTRequestURLFilter{
    var next:SSTRequestURLFilter? = null

    fun isSupportChain(request: VerifyingRequest):Boolean{
        if(isSupport(request))
            return true
        else {
            if(next==null)
                return false
            else return next!!.isSupportChain(request)
        }
    }

    abstract fun isSupport(request:VerifyingRequest):Boolean
}

class RegSSTRequestURLFilter(val rule:String): SSTRequestURLFilter() {
    val pattern = Pattern.compile(rule)
    override fun isSupport(request: VerifyingRequest): Boolean {
        return pattern.matcher(request.uri).matches()
    }
}

class StaticSSTRequestURLFilter(val rule:String) : SSTRequestURLFilter() {
    override fun isSupport(request: VerifyingRequest): Boolean {
        return rule==request.uri
    }
}