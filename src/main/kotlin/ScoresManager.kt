import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object ScoresManager {
    private var connection: Connection? = null

    fun connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:mastermind.db")
        } catch (e: SQLException) {
            println(e.message)
        }
    }

    fun createScoresTable() {
        val createScores = """
        CREATE TABLE IF NOT EXISTS scores (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            sequenceLength INTEGER,
            maxAttempts INTEGER,
            colorsNumber INTEGER,
            time REAL
        );
        """

        try {
            val statement = connection?.createStatement()
            statement?.execute(createScores)
        } catch (e: SQLException) {
            println(e.message)
        }
    }

    fun insertScore(sequenceLength: Int, maxAttempts: Int, colorsNumber: Int, time: Long) {
        val record = "INSERT INTO scores(sequenceLength, maxAttempts, colorsNumber, time)" +
                "VALUES(?, ?, ?, ?)"
        try {
            val preparedStatement = connection?.prepareStatement(record)
            preparedStatement?.setInt(1, sequenceLength)
            preparedStatement?.setInt(2, maxAttempts)
            preparedStatement?.setInt(3, colorsNumber)
            preparedStatement?.setDouble (4, time.toDouble()/1000.0)
            preparedStatement?.executeUpdate()
        } catch (e: SQLException) {
            println(e.message)
        }
    }

    fun getFilteredScores(sequenceLength: Int, maxAttempts: Int, colorsNumber: Int): List<Double> {
        val scores = mutableListOf<Double>()
        val query = """
        SELECT * FROM scores 
        WHERE sequenceLength = ? AND maxAttempts = ? AND colorsNumber = ?
        ORDER BY time ASC
        """
        try {
            val preparedStatement = connection?.prepareStatement(query)
            preparedStatement?.setInt(1, sequenceLength)
            preparedStatement?.setInt(2, maxAttempts)
            preparedStatement?.setInt(3, colorsNumber)
            val resultSet = preparedStatement?.executeQuery()
            while (resultSet?.next() == true) {
                val score = resultSet.getDouble("time")
                scores.add(score)
            }
        } catch (e: SQLException) {
            println(e.message)
        }
        return scores
    }
}