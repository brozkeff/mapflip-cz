package de.goork.mapflip.parser

/** Offline build: direct URLs only. */
object MapyLinkResolver {
    suspend fun parse(url: String): ParsedLocation = UniversalMapParser.parse(url)
}
