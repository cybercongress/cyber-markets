fun main(args: Array<String>) {

    val list = mutableListOf<String>()

    list.sortWith(java.util.Comparator { o1, o2 ->
        return@Comparator -1
    })

}