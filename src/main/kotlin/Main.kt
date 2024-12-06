import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import androidx.compose.ui.window.application
import androidx.compose.ui.window.Window

fun main(args: Array<String>) {
    val parser = ArgParser("mastermind")

    val gui by parser.option(ArgType.Boolean, shortName = "g", description = "Run in GUI mode").default(false)
    val sequenceLength by parser.option(ArgType.Int, shortName = "l", description = "Sequence length").default(4)
    val maxAttempts by parser.option(ArgType.Int, shortName = "a", description = "Maximum attempts").default(10)
    val colors by parser.option(ArgType.String, shortName = "c", description = "Colors list").default("A,B,C,D,E,F")
    val manualCode by parser.option(ArgType.String, shortName = "m", description = "Manual code").default("")
    parser.parse(args)

    if (gui) {
        application {
            Window(onCloseRequest = ::exitApplication, title = "Mastermind - Game") {
                app()
            }
        }
    } else {
        val colorsList = colors.split(",").map(String::trim)
        val settings = Settings(sequenceLength = sequenceLength, maxAttempts = maxAttempts, colorsList = colorsList)
        val game = Game(settings, manualCode)
        game.start()
    }
}