package wordsvirtuoso

import java.io.File
import java.io.FileNotFoundException
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val wordsFileAddress: String
    val candidatesFileAddress: String

    if (args.size == 2) {
        wordsFileAddress = args[0]
        candidatesFileAddress = args[1]
    } else {
        println("Error: Wrong number of arguments.")
        exitProcess(-1)
    }

    val wordsFile = File(wordsFileAddress)
    val candidatesFile = File(candidatesFileAddress)
    val listTypes = listOf("words", "candidate words")

    val wordsLines = readFile(wordsFile, listTypes[0])
    val candidatesLines = readFile(candidatesFile, listTypes[1])

    checkWords(wordsFile, wordsLines)
    checkWords(candidatesFile, candidatesLines)

    listsComparison(wordsLines,candidatesLines, wordsFile)

    val selectedWord = selectCandidatesWord(candidatesLines)
    gameEngine(wordsLines, selectedWord)
}

fun readFile(file: File, listType: String): List<String> {
    val fileContent : List<String>

    try {
        fileContent = file.readLines()
    } catch (e: FileNotFoundException) {
        println("Error: The $listType file $file doesn't exist.")
        exitProcess(-1)
    }
    return fileContent
}

fun checkWords(file:File, content: List<String>) {
    var invalidWordCounter = 0
    for (line in content) {
        if(!line.contains("^(?:([A-Za-z])(?!.*\\1)){5}\$".toRegex())) invalidWordCounter++
    }
    if (invalidWordCounter > 0) {
        println("Error: $invalidWordCounter invalid words were found in the $file file.")
        exitProcess(-1)
    }
}

fun listsComparison(list1: List<String>, list2: List<String>, file: File) {
    var absentWordsCounter = 0
    for (line in list2) {
        if (!list1.any { it.equals(line, ignoreCase = true) }) absentWordsCounter++
    }
    if (absentWordsCounter > 0) {
        println("Error: $absentWordsCounter candidate words are not included in the $file file.")
        exitProcess(-1)
    } else println("Words Virtuoso")
}

fun selectCandidatesWord(line: List<String>): String {
    return line.random()
}

fun getUserInput(): String {
    return readln().lowercase()
}

// it will be part of a service alike class
fun isInputValid(input: String, wordsLines: List<String>): Boolean {
    var validation = false
    if (input == "exit") {
        validation = true
    } else if (input.length != 5) {
        println("The input isn't a 5-letter word.")
    } else if (input.contains("[^A-Za-z]".toRegex())) {
        println("One or more letters of the input aren't valid.")
    } else if (input.contains("([A-Za-z])\\1".toRegex())) {
        println("The input has duplicate letters.")
    } else if (input !in wordsLines) {
        println("The input word isn't included in my words list.")
    } else {
        validation = true
    }
    return validation
}

// part of a future monitor class
fun guessColourise(input: String, selectedWord: String): String {
    var encodedGuess = ""

    for (i in input.indices) {
        if (input[i] == selectedWord[i]) {
            encodedGuess += "\u001B[48:5:10:38:5:0m${input[i].uppercase()}\u001B[0m"
        } else if (selectedWord.contains(input[i])) {
            encodedGuess += "\u001B[48:5:11:38:5:0m${input[i].uppercase()}\u001B[0m"
        } else {
            encodedGuess += "\u001B[48:5:7:38:5:0m${input[i].uppercase()}\u001B[0m"
        }
    }
    return encodedGuess
}

// another monitor function, it will probably be moved to monitor class
fun printGuessList(input: String, selectedWord: String, guessSurvey: MutableList<String>) {
    val guessEncoded = guessColourise(input, selectedWord)
    guessSurvey.add(guessEncoded)
    for (element in guessSurvey) println(element)
}

// a monitor function block which I can turn into a monitor class later
fun showResponsesToUser(input: String, selectedWord: String, guessCounter: Int, guessSurvey: MutableList<String>, startTime: Long) {
    when (input) {
        "exit" -> {
            println("The game is over.")
            exitProcess(-1)
        }
        selectedWord -> {

            if (guessCounter == 1) {
                printGuessList(input, selectedWord, guessSurvey)
                println()
                println("Correct!")
                println("Amazing luck! The solution was found at once.")
                exitProcess(-1)
            } else {
                printGuessList(input, selectedWord, guessSurvey)
                println()
                val timeLapsed = (getTime() - startTime) / 1000
                println("Correct!")
                println("The solution was found after $guessCounter tries in $timeLapsed seconds.")
                exitProcess(-1)
            }
        } else -> {
        printGuessList(input, selectedWord, guessSurvey)
    }
    }
}

fun getTime(): Long {
    return System.currentTimeMillis()
}

fun gameEngine(wordsLines: List<String>, selectedWord: String) {
    val guessSurvey: MutableList<String> = mutableListOf()
    var wrongLetters: Set<Char> = setOf()
    var wrongLettersText = ""
    var guessCounter = 0
    var isFirstTurn = true
    var startTime = 0L

    while(true) {
        println("Input a 5-letter word:")
        if (isFirstTurn) {
            startTime = getTime()
            isFirstTurn = false
        }

        val input = getUserInput()
        val isInputValid = isInputValid(input, wordsLines)
        
        if (isInputValid) {
            guessCounter++
            showResponsesToUser(input, selectedWord, guessCounter, guessSurvey, startTime)
            println()
            val missedChars = input.toCharArray().filter{ !selectedWord.contains(it)}
            wrongLetters = wrongLetters.plus(missedChars)
            println("\u001B[48:5:14:38:5:0m${wrongLetters.sorted().joinToString("").uppercase()}\u001B[0m")
            println()
        }
    }
}
