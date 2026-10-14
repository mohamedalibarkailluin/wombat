import { createApp } from '/vendor/petite-vue.es.js';
import { ThemeToggle } from '../components/theme-toggle.js';
import { RangeLabel } from '../components/date-range.js';

export function mountPage(components) {
  const app = createApp({ $delimiters: ['[[', ']]'], ...components }).mount('#page-root');
  const header = createApp({ $delimiters: ['[[', ']]'], ThemeToggle, RangeLabel }).mount('#header-root');
  const dispose = () => { app.unmount(); header.unmount(); };
  const hide = event => {
    if (!event.persisted) {
      dispose();
      window.removeEventListener('pagehide', hide);
    }
  };
  window.addEventListener('pagehide', hide);
}
