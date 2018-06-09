package blog.codejunkie.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IspController {

    @GetMapping("/isp")
    fun ispDetails() = mapOf(
        "country" to "United States",
        "isp" to "GoDaddy.com, LLC"
    )

}