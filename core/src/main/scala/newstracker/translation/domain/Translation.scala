package newstracker.translation.domain

final case class Translation(
    id: TranslationId,
    localizations: List[Localization]
)
