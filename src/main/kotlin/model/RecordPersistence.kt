package model

import Logging
import logger
import utils.FileIo
import utils.JsonParser

object RecordPersistence : Logging {
    val log = logger()

    /**
     * Load the given [jsonFile] from disk and deserialize it as record of type [T].
     *
     * @param T the type of the [IRecord] this method should load and deserialize
     * @param jsonFile the file containing the json to load
     *
     * @return the record loaded from the [jsonFile] deserialized into [T]
     */
    inline fun <reified T : IRecord> load(jsonFile: String): T {
        return JsonParser.deserialize<T>(FileIo.readFile(jsonFile)).also {
            log.atInfo()
                .setMessage("Loaded and deserialized json.")
                .addKeyValue("record") { T::class.simpleName }
                .addKeyValue("jsonFile") { jsonFile }
                .addKeyValue("recordRoot") { it.path }
                .log()
        }
    }

    /**
     * Save the given [record] of type [T] as json into [jsonFile].
     *
     * @param T the type of the [IRecord] this method should serialize and save
     * @param jsonFile the file to save the json in
     * @param record the record of type [T] to serialize and save
     */
    inline fun <reified T : IRecord> save(jsonFile: String, record: T) {
        FileIo.writeFile(jsonFile, JsonParser.serialize(record))

        log.atInfo()
            .setMessage("Serialized and saved json.")
            .addKeyValue("record") { T::class.simpleName }
            .addKeyValue("jsonFile") { jsonFile }
            .addKeyValue("recordRoot") { record.path }
            .log()
    }

    /**
     * Load the given [jsonFile] from disk and deserialize it as a list of records of type [T].
     *
     * @param T the type of the [IRecord] this method should load and deserialize
     * @param jsonFile the file containing the json to load
     *
     * @return a list containing all records loaded from the [jsonFile] deserialized into [T]
     */
    inline fun <reified T : IRecord> loadList(jsonFile: String): List<T> {
        return JsonParser.deserialize<List<T>>(FileIo.readFile(jsonFile)).also {
            log.atInfo()
                .setMessage("Loaded and deserialized json list.")
                .addKeyValue("record") { T::class.simpleName }
                .addKeyValue("jsonFile") { jsonFile }
                .addKeyValue("elements#") { it.size }
                .log()
        }
    }

    /**
     * Save the given [recordList] containing records of type [T] as json into [jsonFile].
     *
     * @param T the type of the [IRecord] this method should serialize and save
     * @param jsonFile the file to save the json in
     * @param recordList the list of records of type [T] to serialize and save
     */
    inline fun <reified T : IRecord> save(jsonFile: String, recordList: List<T>) {
        FileIo.writeFile(jsonFile, JsonParser.serialize(recordList))
        log.atInfo()
            .setMessage("Serialized and saved json list.")
            .addKeyValue("record") { T::class.simpleName }
            .addKeyValue("jsonFile") { jsonFile }
            .addKeyValue("elements#") { recordList.size }
            .log()
    }
}