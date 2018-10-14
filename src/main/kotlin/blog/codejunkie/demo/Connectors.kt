package blog.codejunkie.demo

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class IpApiConnector constructor(baseUrl : String = "http://ip-api.com") {
    private val client = WebClient.create("$baseUrl/json/")

    fun invoke(domain: String) = client.get().uri(domain).retrieve().bodyToMono(IpApiResponse::class.java)
}

data class IpApiResponse(val isp : String = "", val country : String = "", var status : String = "", val message: String = "")