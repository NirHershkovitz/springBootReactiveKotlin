package blog.codejunkie.demo

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
class IspController(val connector : IpApiConnector) {

    @GetMapping("/isp")
    fun ispDetails(@RequestParam() domain: String) = connector.invoke(domain)

    @PostMapping("/isp")
    fun multipleIspsDetails(@RequestBody() multipleDomainsRequest: MultipleDomainsRequest) =
            Flux.concat(multipleDomainsRequest.domains.map({ connector.invoke(it) }))

    data class MultipleDomainsRequest(val domains: List<String>)
}