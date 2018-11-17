package blog.codejunkie.demo

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
class IspController(val connector : IpApiConnector) {

    @GetMapping("/isp")
    fun ispDetails(@RequestParam() domain: String) = connector.invoke(domain)

    @PostMapping("/isp")
    fun multipleIspsDetails(@RequestBody() request: MultipledomainsRequest) =
            Flux.
                    merge(request.domains.map { domain -> connector.invoke(domain).map { isp -> mapOf(domain to isp) } }).
                    reduce({ a, b -> a.plus(b) })

    data class MultipledomainsRequest(val domains: List<String>)
}