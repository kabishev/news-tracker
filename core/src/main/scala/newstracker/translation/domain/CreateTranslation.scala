package newstracker.translation.domain

final case class CreateLocalization(
    id: TranslationId,
    language: LocalizationLanguage
)
