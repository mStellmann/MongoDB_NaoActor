package messages


// communication between MongoDBActor and "DataActors" (e.g. DBAccessFile)
package internalMessages {

case class Save(collection: String, naoID: String, timestamp: Long, content: Map[String, List[String]])

case class SaveFile(collction: String, naoID: String, timestamp: Long, filename: String, filetyp: String, file: Array[Byte], content: Map[String, List[String]])

case class GetDataBetweenTimestamps(collection: String, naoID: String, timestampStart: Long, timestampEnd: Long, content: Option[Map[String, List[String]]])

case class GetFileBetweenTimestamps(collection: String, naoID: String, timestampStart: Long, timestampEnd: Long, filetyp: Option[String], content: Option[Map[String, List[String]]])

}

// communication between "DataActors" and user
package userMessages {
      // TODO
}
