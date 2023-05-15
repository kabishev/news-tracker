package newstracker.translation.db

import mongo4cats.bson.ObjectId

import newstracker.translation._

final case class TranslationEntity(
    _id: ObjectId,
    localizations: List[Localization]
) {

  def toDomain: domain.Translation =
    domain.Translation(
      id = domain.TranslationId(_id.toHexString),
      localizations = localizations.map(_.toDomain)
    )
}

object TranslationEntity {

  def from(translation: domain.Translation): TranslationEntity =
    TranslationEntity(
      _id = ObjectId(translation.id.value),
      localizations = translation.localizations.map(Localization.from)
    )
}
