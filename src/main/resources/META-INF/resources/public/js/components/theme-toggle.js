import { writePreference } from '../utils/storage.js';

export function ThemeToggle() {
  return {
    dark: document.documentElement.getAttribute('data-theme') === 'dark',
    toggle() {
      this.dark = !this.dark;
      const theme = this.dark ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', theme);
      writePreference('wombat.theme', theme);
    }
  };
}
