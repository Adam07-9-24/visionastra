package com.tecsup.visionastra.mobile.core.network

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.math.BigDecimal

class BigDecimalJsonAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): BigDecimal? =
        when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.NUMBER,
            JsonReader.Token.STRING -> reader.nextString().toBigDecimalOrNull()
                ?: throw JsonDataException("Presupuesto invalido")
            else -> throw JsonDataException("Presupuesto invalido")
        }

    @ToJson
    fun toJson(writer: JsonWriter, value: BigDecimal?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(value)
        }
    }
}
