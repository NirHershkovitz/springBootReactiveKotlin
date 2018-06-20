package blog.codejunkie.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class IspController(val connector : IpApiConnector) {

    @GetMapping("/isp")
    fun ispDetails(@RequestParam() domain: String) = connector.invoke(domain)

}