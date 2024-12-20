class KeyArgs(args: Array<String>) {
    val arguments = args.map { it.trim() }.map {
        it.split("=").also {
            if (it.size != 2) {
                throw IllegalArgumentException("Invalid argument structure detected. Each argument must be of the form key=value. The value can not contain another = (equal sign).")
            }
        }
    }.associate { (key, value) ->
        key.trim().lowercase() to value.trim()
    }

    fun getOrThrow(name: String): String {
        return arguments.getOrElse(name.lowercase()) {
            throw IllegalArgumentException("Argument with name ${name.lowercase()} not found.")
        }
    }

    fun getOrNull(name: String): String? {
        return return arguments[name.lowercase()]
    }
}