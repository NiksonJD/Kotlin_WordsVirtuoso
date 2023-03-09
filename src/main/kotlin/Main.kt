package wordsvirtuoso

import java.io.File

fun input(prompt: String) = println(prompt).run { readln() }
fun check(f: Set<String>) = f.filter { it.length != 5 || !it.matches(Regex("[a-z]{5}")) || it.toSet().size != 5 }

class Statistics() {
    val start = System.currentTimeMillis()
    val badChar = mutableSetOf<String>()
    val variants = mutableListOf<MutableMap<String, Color>>()
    var count = 0
    fun addMap(wordMap: MutableMap<String, Color>) = variants.add(wordMap)
    fun addChar(bad: String) = badChar.add(bad)
    fun addWord(word: String) =
        word.asSequence().map { it.toString() to Color.GREEN }.toMap().toMutableMap().also { addMap(it) }

    fun printV() = variants.forEach { ml -> ml.forEach { (key, value) -> print(value.dye(key)) }.run { println() } }
}

enum class Color(private val value: String) {
    GREEN("\u001B[48:5:10m"),
    YELLOW("\u001B[48:5:11m"),
    GREY("\u001B[48:5:7m"),
    AZURE("\u001B[48:5:14m");

    fun dye(string: String) = "$value$string\u001B[0m"
}

fun output(input: String, x: String, statistics: Statistics) {
    val m = mutableMapOf<String, Color>()
    input.mapIndexed { i, c ->
        if (c == x[i]) m.put(c.toString(), Color.GREEN) else if (x.contains(c)) m.put(c.toString(), Color.YELLOW) else {
            statistics.addChar(c.toString()); m.put(c.toString(), Color.GREY)
        }
    }
    statistics.addMap(m).also { println() }.run { statistics.printV() }
    println("\n" + statistics.badChar.sorted().joinToString("").let { Color.AZURE.dye(it) })
}

fun game(candidateWords: Set<String>, allWords: Set<String>) {
    val x = candidateWords.random().uppercase()
    val statistics = Statistics()
    while (true) {
        val i = input("Input a 5-letter word:").uppercase().also { statistics.count++ }
        if (i.equals("exit", true)) {
            println("The game is over."); break
        } else if (i == x) {
            if (statistics.count == 1) {
                x.forEach { print(Color.GREEN.dye(it.toString())) }
                println("\n\nCorrect!\nAmazing luck! The solution was found at once.")
            } else {
                statistics.addWord(x).also { println() }.also { statistics.printV() }
                val seconds = (System.currentTimeMillis() - statistics.start) / 1000
                println("\nCorrect!\nThe solution was found after ${statistics.count} tries in $seconds seconds.")
            }; break
        } else if (i.length != 5) println("The input isn't a 5-letter word.")
        else if (!i.matches(Regex("[A-Z]{5}"))) println("One or more letters of the input aren't valid.")
        else if (i.toSet().size != 5) println("The input has duplicate letters.")
        else if (!allWords.contains(i.lowercase())) println("The input word isn't included in my words list.")
        else output(i, x, statistics)
    }
}

fun start(args: Array<String>) {
    if (args.size != 2) println("Error: Wrong number of arguments.").also { return }
    val (w, c) = arrayOf(File(args[0]), File(args[1]))
    if (!w.exists()) println("Error: The words file $w doesn't exist.").also { return }
    if (!c.exists()) println("Error: The candidate words file $c doesn't exist.").also { return }
    val (a, d) = arrayOf(w.readLines().map { it.lowercase() }.toSet(), c.readLines().map { it.lowercase() }.toSet())
    if (!check(a).isEmpty()) println("Error: ${check(a).size} invalid words were found in the $w file.").also { return }
    if (!check(d).isEmpty()) println("Error: ${check(d).size} invalid words were found in the $c file.").also { return }
    val r = d.toMutableSet(); r.removeAll(a)
    if (r.isNotEmpty()) println("Error: ${r.size} candidate words are not included in the $w file.").also { return }
    println("Words Virtuoso").also { game(d, a) }
}

fun main(args: Array<String>) = start(args)