package de.goork.mapflip.parser

import java.io.IOException
import org.junit.Assert.*
import org.junit.Test

class MapyLinkResolverTest {
    private val short = "https://mapy.cz/s/abcdef"

    @Test fun `follows old to new domain and relative redirects`() {
        var requests = 0
        val result = MapyLinkResolver.resolve(short) {
            requests++
            if (requests == 1) 302 to "https://mapy.com/s/abcdef"
            else 302 to "/cs/?source=coor&id=14.4%2C50.1"
        }
        assertEquals(ParsedLocation.Coordinates(50.1, 14.4), result)
        assertEquals(2, requests)
    }

    @Test fun `rejects external and credential redirects before requesting them`() {
        for (target in listOf("https://evil.test/", "https://mapy.com.evil.test/",
            "https://user@mapy.com/", "file:///etc/passwd", "https://mapy.com:1234/")) {
            var requests = 0
            assertEquals(ParsedLocation.WebFallback(short), MapyLinkResolver.resolve(short) {
                requests++
                302 to target
            })
            assertEquals(1, requests)
        }
    }

    @Test fun `loop timeout and expired links retain original`() {
        assertEquals(ParsedLocation.WebFallback(short), MapyLinkResolver.resolve(short) { 302 to short })
        assertEquals(ParsedLocation.WebFallback(short), MapyLinkResolver.resolve(short) { throw IOException("timeout") })
        assertEquals(ParsedLocation.WebFallback(short), MapyLinkResolver.resolve(short) { 404 to null })
        var requests = 0
        assertEquals(ParsedLocation.WebFallback(short), MapyLinkResolver.resolve(short) {
            requests++
            302 to "/s/link$requests"
        })
        assertEquals(6, requests)
    }

    @Test fun `direct URLs avoid network and http short links upgrade to https`() {
        assertEquals(ParsedLocation.Coordinates(50.1, 14.4),
            MapyLinkResolver.resolve("https://mapy.com/?x=14.4&y=50.1") { error("Unexpected request") })
        MapyLinkResolver.resolve("http://mapy.cz/s/abcdef") {
            assertEquals("https", it.scheme)
            404 to null
        }
    }
}
