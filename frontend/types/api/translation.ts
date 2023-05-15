export interface Localization {
  language: string;
  content: string;
}

export interface Translation {
  id: string;
  localizations: Localization[];
}