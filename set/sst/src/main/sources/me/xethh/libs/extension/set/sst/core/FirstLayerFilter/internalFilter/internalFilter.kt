package me.xethh.libs.extension.set.sst.core.FirstLayerFilter.internalFilter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class InternalFilterChain(listOfFilters:List<InternalFilter>){
    val head:InternalFilter = DefaultInternalFilter()
    var last:InternalFilter = head

    init{
        listOfFilters.forEach { last.next = it; last = it }
    }

    fun filter(request: HttpServletRequest, response: HttpServletResponse):Pair<HttpServletRequest, HttpServletResponse>{
        var r = Pair(request, response)
        var h = head
        while (h.next!=null){
            h = h.next!!
            r = h.doFilter(r.first, r.second)
        }
        return r

    }
}

abstract class InternalFilter{
    var next:InternalFilter? = null
    abstract fun doFilter(request:HttpServletRequest, response: HttpServletResponse) : Pair<HttpServletRequest, HttpServletResponse>
}

class DefaultInternalFilter() : InternalFilter(){
    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse): Pair<HttpServletRequest, HttpServletResponse> {
        return Pair(request, response)
    }
}