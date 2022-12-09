package advent

object Day7 : AdventDay {
    override fun part1(input: List<String>): Any {
        val root = parseDirectories(input)
        return root.getAllSubdirectories()
            .map { it.contentSize }
            .filter { it <= 100000 }
            .sum()
    }

    override fun part2(input: List<String>): Any {
        val root = parseDirectories(input)
        val freeSpace = 70000000 - root.contentSize
        return root.getAllSubdirectories()
            .map { it.contentSize }
            .filter { freeSpace + it > 30000000 }
            .min()
    }

    sealed interface Content {
        val contentSize: Int
    }

    class Directory(val parent: Directory?) : Content, MutableMap<String, Content> by mutableMapOf() {
        override val contentSize: Int by lazy { this.values.sumOf { it.contentSize } }
    }

    class File(override val contentSize: Int) : Content

    fun Directory.getAllSubdirectories(): Sequence<Directory> = sequence {
        val children = this@getAllSubdirectories.values.filterIsInstance<Directory>()
        for (child in children) {
            yield(child)
            yieldAll(child.getAllSubdirectories())
        }
    }

    fun parseDirectories(input: List<String>): Directory {
        val rootDirectory = Directory(null)
        var currentDirectory = rootDirectory
        input.forEach { line ->
            when {
                line == "$ cd /" -> currentDirectory = rootDirectory
                line == "$ cd .." -> currentDirectory = currentDirectory.parent!!
                line.startsWith("$ cd") -> {
                    currentDirectory = currentDirectory[line.substringAfterLast(' ')] as Directory
                }
                line.startsWith("$ ls") -> { /* Drop */ }
                line.startsWith("dir") -> currentDirectory[line.substringAfter("dir ")] = Directory(currentDirectory)
                else -> {
                    val (size, name) = line.split(' ')
                    currentDirectory[name] = File(size.toInt())
                }
            }
        }
        return rootDirectory
    }
}