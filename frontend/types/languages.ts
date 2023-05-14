export type Language = {
  code: string;
  name: string;
};

export const availableLanguages: Language[] = [
  { code: 'en', name: 'English' },
  { code: 'fr', name: 'French' },
  { code: 'de', name: 'German' },
  { code: 'ru', name: 'Russian' }
];