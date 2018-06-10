package blog.codejunkie.demo

import org.springframework.web.reactive.function.client.WebClient

class IpApiConnector {
    private val client = WebClient.create("http://ip-api.com/json/")

    fun invoke(domain: String) = client.get().uri(domain).retrieve().bodyToMono(HostingDetails::class.java)
}

data class HostingDetails(val isp : String, val country : String)