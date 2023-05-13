package newstracker.translation.domain

final case class Localization(
    language: LocalizationLanguage,
    content: LocalizationContent
)
