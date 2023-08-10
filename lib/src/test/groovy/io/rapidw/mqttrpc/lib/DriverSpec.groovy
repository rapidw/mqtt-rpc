package io.rapidw.mqttrpc.lib

import io.rapidw.mqttrpc.driver.spec.Canteen
import spock.lang.Specification


class DriverSpec extends Specification {

    def "canteen"() {
        when:
        def driverService = new DriverService('localhost', 8080)
        def canteen = driverService.newInstance(Canteen)

        then:
        canteen.hello("world").get() == 'canteen world'
    }

}