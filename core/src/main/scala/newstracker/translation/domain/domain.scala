package newstracker.translation

import io.estatico.newtype.macros.newtype

package object domain {
  @newtype case class TranslationId(value: String)
  @newtype case class LocalizationLanguage(value: String)
  @newtype case class LocalizationContent(value: String)
}
