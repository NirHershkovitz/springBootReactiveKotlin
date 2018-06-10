package blog.codejunkie.demo

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ConnectorTests: StringSpec({
    "ip-api return correct hosting details" {
        IpApiConnector().invoke("codejunkie.blog").block() shouldBe
                HostingDetails(isp = "GoDaddy.com, LLC", country = "United States")
    }
})

