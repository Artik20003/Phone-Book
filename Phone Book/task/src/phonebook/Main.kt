package phonebook
import java.io.File
import kotlin.math.*
import java.util.*
const val MAX_LOAD_FACTOR = 0.7
class ValueTimePair<T> (val value: T, val millisTimeTaken:Long)
class PhoneBookRecord (val name: String, val phone: Int)
class TimeExceededException(val millisTimeTaken:Long, m: String = ""): Exception(m)
class CustomHashMap<T: List<List<PhoneBookRecord?>>> (val hashMap: T, val hashKey: Int)

fun  quickSort (list: MutableList<PhoneBookRecord>, low: Int = 0, high: Int = list.lastIndex): ValueTimePair<MutableList<PhoneBookRecord>> {
    val startTime = System.currentTimeMillis()
    if(low < high) {
        val pivot = list[high]
        var i = low - 1

        // making partition
        for (j in low.. (high - 1)) {
            if(list[j].name < pivot.name) {
                i++
                Collections.swap(list, i, j)

            }
        }
        Collections.swap(list, i+1, high)
        quickSort(list, low, i)
        quickSort(list, i+2, high)
    }

    return ValueTimePair(list, (System.currentTimeMillis() - startTime))
}

fun <T: Iterable<String>, S: List<PhoneBookRecord>> binarySearch(search: T, phoneBook: S): ValueTimePair<Int> {
    var findings = 0
    val startTime = System.currentTimeMillis()
    for (searchName in search) {
        var left = 0
        var right = phoneBook.lastIndex
        var middle = (right + left) / 2

        while (left < right) {
            if(phoneBook[middle].name == searchName) {
                findings++
                break
            }
            if(searchName < phoneBook[middle].name) {
                right = middle - 1
            } else {
                left = middle + 1
            }
            middle = (right + left) / 2
        }
    }
    return ValueTimePair(findings, (System.currentTimeMillis() - startTime))
}

fun bubbleSort (list: MutableList<PhoneBookRecord>, stopTimeMillis:Long): ValueTimePair<MutableList<PhoneBookRecord>> {
    val startTime = System.currentTimeMillis()
    var isSorted = false
    while (!isSorted) {
        isSorted = true

        for (i in 0..(list.lastIndex - 1) ) {
            if(list[i].name > list[i+1].name) {

                val cur = list[i]
                list[i] = list[i+1]
                list[i+1] = cur
                isSorted = false
                if(System.currentTimeMillis() - startTime > stopTimeMillis) {
                    throw TimeExceededException(System.currentTimeMillis() - startTime)

                }
            }
        }

    }
    return ValueTimePair(list, (System.currentTimeMillis() - startTime))
}

fun <T: Iterable<String>, S: List<PhoneBookRecord>> jumpSearch(search: T, phoneBook: S): ValueTimePair<Int> {
    val jumpSize = floor(sqrt(phoneBook.size.toDouble())).toInt()
    val startTime = System.currentTimeMillis()

    var count = 0

    Loop@for(searchName in search) {
        var i = minOf(jumpSize,phoneBook.lastIndex)
        if(searchName < phoneBook[0].name || searchName > phoneBook.last().name) continue
        while (i < phoneBook.lastIndex) {
            if(searchName <= phoneBook[i].name ) {
                for (j in i downTo maxOf(0,(i-jumpSize))) {
                    if(phoneBook[j].name == searchName) {
                        count++
                        continue@Loop
                    }
                }
            }
            else {
                i = minOf(i + jumpSize,phoneBook.lastIndex)
            }
        }
    }
    return ValueTimePair(count, (System.currentTimeMillis() - startTime))
}
fun <T: Iterable<String>, S: List<PhoneBookRecord>> linearSearch(search: T, phoneBook: S): ValueTimePair<Int> {
    val startTime = System.currentTimeMillis()
    var i = 0
    for(searchName in search) {
        var bFound = false
        for (record in phoneBook) {
            if (record.name == searchName) {
                if (!bFound) i++
                bFound = true
            }
        }
        if(!bFound) print(searchName)
    }

    return ValueTimePair(i, (System.currentTimeMillis() - startTime))
}

fun Long.toMinutesString(): String  {
    val minutes = this / 1000 / 60
    val seconds = this / 1000 % 60
    val millis = this % 1000
    return "$minutes min. $seconds sec. $millis ms."
}

fun hashFunction (s: String, p: Int):Int {
    var prevHash = 1L
    var hashSum = 0L
    for (letter in s) {
        hashSum = (letter.code + prevHash * 115249L) % p
        prevHash = hashSum
    }
    return (hashSum).toInt()
}
fun getHashMap(list: MutableList<PhoneBookRecord>): ValueTimePair<CustomHashMap<List<List<PhoneBookRecord?>>>> {
    val startTime = System.currentTimeMillis()
    val hashKey = (list.size * MAX_LOAD_FACTOR).toInt()
    val hashMap = MutableList<MutableList<PhoneBookRecord?>>(hashKey) { mutableListOf() }
    for (item in list) {
        var hash  = hashFunction(item.name, hashKey)
        hashMap[hash].add(item)
    }
    return ValueTimePair(CustomHashMap(hashMap, hashKey), (System.currentTimeMillis() - startTime))
}

fun  <T: Iterable<String>, S: CustomHashMap<List<List<PhoneBookRecord?>>>> hashMapSearch(search: T, hashMap:  S): ValueTimePair<Int> {
    val startTime = System.currentTimeMillis()
    var i = 0
    for (searchName in search) {
        for (item in hashMap.hashMap[hashFunction(searchName, hashMap.hashKey)]) {
            if(item?.name == searchName) {
                i++
                break
            }
        }

    }
    return ValueTimePair(i, (System.currentTimeMillis() - startTime))
}


fun main() {
    val search = File("/Users/artem/Downloads/find.txt").readText().trim().split("\r\n")

    val phoneBook = File("/Users/artem/Downloads/directory.txt").readLines().map {
        val name = it.split(" ").toMutableList().filterIndexed({ i, s -> i != 0 }).joinToString(" ")
        PhoneBookRecord(name, it.split(" ").first().toInt())

    }.toMutableList()

    println("Start searching (linear search)...")
    val linearRes = linearSearch(search, phoneBook)
    println("Found ${linearRes.value} / ${search.size} entries. Time taken: ${linearRes.millisTimeTaken.toMinutesString()}")

    println("\nStart searching (bubble sort + jump search)...")
    try {
        val sortRes = bubbleSort(phoneBook, linearRes.millisTimeTaken * 10)
        val jumpRes = jumpSearch(search, sortRes.value)
        println("Found ${jumpRes.value} / ${search.size} entries. Time taken: ${(jumpRes.millisTimeTaken + sortRes.millisTimeTaken).toMinutesString()}")
        println("Sorting time: ${sortRes.millisTimeTaken.toMinutesString()}")
        println("Searching time: ${jumpRes.millisTimeTaken.toMinutesString()}")
    } catch (e: TimeExceededException) {
        println("Found ${linearRes.value} / ${search.size} entries. Time taken: ${(linearRes.millisTimeTaken + e.millisTimeTaken).toMinutesString()}")
        println("Sorting time: ${e.millisTimeTaken.toMinutesString()} - STOPPED, moved to linear search")
        println("Searching time: ${linearRes.millisTimeTaken.toMinutesString()}")

    }



    println("\nStart searching (quick sort + binary search)...")
    val quickSortRes = quickSort(phoneBook)
    val binarySearch = binarySearch(search, quickSortRes.value)

    println("Found ${linearRes.value} / ${search.size} entries. Time taken: ${(quickSortRes.millisTimeTaken + binarySearch.millisTimeTaken).toMinutesString()}")
    println("Sorting time: ${quickSortRes.millisTimeTaken.toMinutesString()}")
    println("Searching time: ${binarySearch.millisTimeTaken.toMinutesString()}")

    //val fileName = "/Users/artem/Downloads/output1.txt"
    //File(fileName).writeText(File("/Users/artem/Downloads/find.txt").readText())

    println("\nStart searching (hash table)...")
    val hashMapRes = getHashMap(phoneBook)
    val hashMapSearch = hashMapSearch(search, hashMapRes.value)
    println("Found ${hashMapSearch.value} / ${search.size} entries. Time taken: ${(hashMapSearch.millisTimeTaken + hashMapRes.millisTimeTaken).toMinutesString()}")
    println("Creating time: ${hashMapRes.millisTimeTaken.toMinutesString()}")
    println("Searching time: ${hashMapSearch.millisTimeTaken.toMinutesString()}")


}
