import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default

fun main(args: Array<String>) {
    val parser = ArgParser("mastermind")

    val sequenceLength by parser.option(ArgType.Int, shortName = "l", description = "Sequence length").default(4)
    val maxAttempts by parser.option(ArgType.Int, shortName = "a", description = "Maximum attempts").default(10)
    val colors by parser.option(ArgType.String, shortName = "c", description = "Colors list").default("A B C D E F")
    parser.parse(args)

    val colorsList = colors.uppercase().split(" ").map(String::trim)
    val settings = Settings(sequenceLength = sequenceLength, maxAttempts = maxAttempts, colorsList = colorsList)
    val game = Game(settings)

    game.start()
}
