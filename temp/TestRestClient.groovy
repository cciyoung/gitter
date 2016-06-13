@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7')

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.POST
import groovyx.net.http.ContentType
import groovy.util.XmlSlurper
import groovy.xml.MarkupBuilder
import java.io.*


def http = new HTTPBuilder( 'http://10.1.249.106:8090/api/push/RoomPriceDataPush' )
http.auth.basic('Test','OTA_Push')
http.request(POST) { req -> 
    //uri.path='/'
    def text =new XmlSlurper().parse(new File('E:/groovy.xml'))
    
    body={
       text
    }
     headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
     headers.Accept = 'application/xml'
     
    requestContentType=ContentType.XML
    response.success = { resp, reader ->
        print "Response status is: ${ resp.statusLine }"
        System.out << reader
    }

    response.'404' = { resp -> 
        print 'Not Found'
    }
}