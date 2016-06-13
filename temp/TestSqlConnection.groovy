@GrabConfig(systemClassLoader=true)
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7')
@Grab(group='net.sourceforge.jtds', module='jtds', version='1.3.1')

Package RateCodeSync
import groovyx.net.http.HTTPBuilder 
import static groovyx.net.http.Method.POST 
import groovyx.net.http.ContentType 
import groovy.sql.Sql
import java.util.logging.Logger
import groovy.time.TimeCategory
import groovy.xml.MarkupBuilder
import java.io.*



class RateCode {
    String rate_code
    String room_type_code
    String resorts
    String season_code
    Date begin_date
    Date end_date
    int monday_yn
    int tuesday_yn
    int wednesday_yn
    int thursday_yn
    int friday_yn
    int saturday_yn
    int sunday_yn
    String currency_code
    BigDecimal adult1
    BigDecimal adult2
    BigDecimal adult3
    BigDecimal adult4
    BigDecimal adult5
    BigDecimal adult_add
    BigDecimal children_add
    BigDecimal active_yn
    Date inactive_date
    Date insert_date
    Date update_date
    BigDecimal weekendAdult1
    BigDecimal weekendAdult2
    BigDecimal weekendAdult3
    BigDecimal weekendAdult4
    BigDecimal weekendAdult5
    BigDecimal weekendAdult_add
    String Packages
    BigDecimal weekendChildren_add
    
}

def logger = Logger.getLogger("SqlTrace")
def timeFormat = "yyyy-MM-dd HH:mm:ss"
def intevalMinute = -219600

/**
 * 给一个日期增加减少分钟
 * @param  d 日期
 * @param  m 分钟
 * @return   日期
 */
def IncreaseMinute(Date d, int m) {
    use(TimeCategory) {
        return d + m.minute
    }
}

/**
 * 获取增量房价码
 * @param  db         数据库连接实例
 * @param  lastUpdate 上次更新的时间，格式为数据库兼容格式
 * @return            返回增量房价集合
 */

 
def GetRateCode(db, String lastUpdate) {
    def rateCodes = []
    def writer=new StringWriter()
    def xml = new MarkupBuilder(writer)
     
    xml.OTA_HotelRateAmountNotifRQ(
           xmlns:'http://www.opentravel.org/OTA/2003/05',
           TimeStamp:'2016-06-02T17:42:37',
           Target:'Test',
           Version:'1.000',
           PrimaryLangID:'en'
     ){
        xml.RateAmountMessages(HotelCode:'2003365'){
    
            db.eachRow(
                "select " +
                        "rate_code," +
                        "room_type_code," +
                        "resorts," +
                        "season_code," +
                        "begin_date," +
                        "end_date," +
                        "monday_yn," +
                        "tuesday_yn," +
                        "wednesday_yn," +
                        "thursday_yn," +
                        "friday_yn," +
                        "saturday_yn," +
                        "sunday_yn," +
                        "currency_code," +
                        "adult1," +
                        "adult2," +
                        "adult3," +
                        "adult4," +
                        "adult5," +
                        "adult_add," +
                        "children_add," +
                        "active_yn," +
                        "inactive_date," +
                        "insert_date," +
                        "update_date," +
                        "weekendAdult1," +
                        "weekendAdult2," +
                        "weekendAdult3," +
                        "weekendAdult4," +
                        "weekendAdult5," +
                        "weekendAdult_add," +
                        "Packages," +
                        "weekendChildren_add " +
                        "from RateCode with (nolock) where insert_date > '2016-01-01 00:00:00'"
                ) {row->     
                        
                            RateAmountMessage(LocatorID:new Date().format('yyyyMMddHHmmss')){
                               StatusApplicationControl(Start:row.begin_date.format('yyyy-MM-dd'),End:row.end_date.format('yyyy-MM-dd'),IsRoom:'true',InvTypeCode:row.room_type_code,RatePlanCode:row.rate_code)
                                Rates(){
                                     Rate(){
                                       BaseByGuestAmts(){
                                         BaseByGuestAmt(NumberOfGuests:'1',CurrencyCode:'CNY',AmountBeforeTax:row.adult1)
                                         BaseByGuestAmt(NumberOfGuests:'2',CurrencyCode:'CNY',AmountBeforeTax:row.adult2)
                                       }
                                       MealsIncluded('14')
                                    }
                                }
                            }
                         
                  rateCodes << new RateCode(row.toRowResult())
                }
        }
      }
    def http = new HTTPBuilder( 'http://10.1.249.106:8090/api/push/RoomPriceDataPush' ) 
    http.auth.basic('Test','OTA_Push') 
    http.request(POST) { req -> 
        body= writer.toString()
      
        requestContentType=ContentType.Text 
        response.success = { resp, reader -> 
            print "Response status is: ${ resp.statusLine }" 
               System.out << reader 
        } 
        response.'404' = { resp ->  
            print 'Not Found' 
        } 
     } 
     
      def outFile=new File("e:/groovy.xml")
      def printWriter=outFile.newPrintWriter()
      printWriter.println(writer.toString())
      printWriter.flush()
      printWriter.close()
      //println writer.toString()
      
    return rateCodes
}

    
     
 
def db = Sql.newInstance (
    url: "jdbc:jtds:sqlserver://10.1.249.89;databaseName=HPMS_ADPDB",
    user: 'uws_hhub',
    password: '123321a',
    driverClassName: "net.sourceforge.jtds.jdbc.Driver"   
    )


def lastUpdate = IncreaseMinute(new Date(), intevalMinute).format(timeFormat)

logger.info(lastUpdate);

def codes = GetRateCode(db, lastUpdate)

codes.each() {
    println it.rate_code

}

db.close()