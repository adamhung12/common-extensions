package me.xethh.libs.extension.set.sst.core.FirstLayerFilter.requestFilter

import org.junit.Assert.assertTrue
import org.junit.Test

class TestSuit{
    @Test
    fun testStaticSSTRequestURLFilter(){
        var filter:SSTRequestURLFilter = StaticSSTRequestURLFilter("/abcd")
        var request = VerifyingRequest()
        request.uri="/abcd"

        assertTrue("static request uri",filter.isSupportChain(request))

        filter = RegSSTRequestURLFilter("/cd(/.*$|$)")
        request.uri="/cd/sdjakldj/djkfadj"
        assertTrue("regex request uri",filter.isSupportChain(request))
        request.uri="/cd"
        assertTrue("regex request uri",filter.isSupportChain(request))


    }
}