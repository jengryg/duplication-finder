fun main(args: Array<String>) {
    val kArgs = KeyArgs(args)

    setLoggingLevel(kArgs.getOrNull("log")?.uppercase() ?: "INFO")

    Program(kArgs).main()
}