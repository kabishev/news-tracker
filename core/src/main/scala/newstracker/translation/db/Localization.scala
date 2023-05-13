package newstracker.translation.db

import newstracker.translation._

final case class Localization(
    language: String,
    content: String
) {

  def toDomain: domain.Localization =
    domain.Localization(
      language = domain.LocalizationLanguage(language),
      content = domain.LocalizationContent(content)
    )
}

object Localization {

  def from(localization: domain.Localization): Localization =
    Localization(
      language = localization.language.value,
      content = localization.content.value
    )
}
