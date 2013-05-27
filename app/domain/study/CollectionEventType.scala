package domain.study

class CollectionEventType private[study] (
  collectionEventTypeId: CollectionEventTypeId,
  name: String,
  description: String,
  recurring: Boolean) {

}

object CollectionEventType {

  // TODO: not sure yet if this is the right place for this method
  def nextIdentity: CollectionEventTypeId =
    new CollectionEventTypeId(java.util.UUID.randomUUID.toString.toUpperCase)

}