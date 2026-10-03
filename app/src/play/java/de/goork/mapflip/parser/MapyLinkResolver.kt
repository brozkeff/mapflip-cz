package de.goork.mapflip.parser

/** Online resolution is reserved for the separate online build target. */
object MapyLinkResolver {
    suspend fun parse(url: String): ParsedLocation = UniversalMapParser.parse(url)
}
