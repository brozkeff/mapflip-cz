package de.goork.mapflip.parser

import java.net.HttpURLConnection
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MapyLinkResolver {
    /** Only the online source set contains networking. Never follow redirects to other hosts. */
    suspend fun parse(url: String): ParsedLocation = withContext(Dispatchers.IO) {
        val extracted = UniversalMapParser.extractMapUrl(url) ?: url
        val local = UniversalMapParser.parse(extracted)
        if (!MapyMapsParser.canParse(extracted) || local !is ParsedLocation.WebFallback) {
            return@withContext local
        }
        resolve(extracted)
    }

    internal fun resolve(original: String, request: (URI) -> Pair<Int, String?> = ::request): ParsedLocation {
        val visited = mutableSetOf<URI>()
        try {
            var current = URI(original.replace(" ", "%20"))
            repeat(6) {
                // Upgrade old HTTP links before making any request.
                if (current.scheme.equals("http", ignoreCase = true)) {
                    current = URI("https" + current.toString().substring(4))
                }
                if (!MapyMapsParser.canParse(current.toString()) ||
                    current.port !in listOf(-1, 443) || !visited.add(current)) {
                    return ParsedLocation.WebFallback(original)
                }
                val local = MapyMapsParser.parse(current.toString())
                if (local !is ParsedLocation.WebFallback) return local
                val (status, location) = request(current)
                if (status !in listOf(301, 302, 303, 307, 308) || location.isNullOrBlank()) {
                    return ParsedLocation.WebFallback(original)
                }
                current = current.resolve(location)
            }
        } catch (_: Exception) {
            // Expired short links, timeouts and unsupported objects keep their original URL.
        }
        return ParsedLocation.WebFallback(original)
    }

    private fun request(uri: URI): Pair<Int, String?> {
        val connection = uri.toURL().openConnection() as HttpURLConnection
        return try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            // Use browser-style GET headers; Mapy short links can return 404 for HEAD requests.
            connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Linux; Android 16; Pixel 7) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/140.0.0.0 Mobile Safari/537.36")
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml")
            connection.responseCode to connection.getHeaderField("Location")
        } finally {
            connection.disconnect()
        }
    }
}
