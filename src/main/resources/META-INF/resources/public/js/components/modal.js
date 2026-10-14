import { reactive } from '/vendor/petite-vue.es.js';

export function Modal() {
  const visibility = reactive({ opened: false });
  let root;
  let origin;
  let escape;
  return {
    get opened() { return visibility.opened; },
    mount(element) {
      root = element;
      escape = event => { if (event.key === 'Escape' && visibility.opened) this.close(); };
      document.addEventListener('keydown', escape);
    },
    show(trigger) {
      origin = trigger;
      visibility.opened = true;
      document.body.classList.add('modal-open');
      queueMicrotask(() => root.querySelector('.asset-modal-close').focus());
    },
    close() {
      visibility.opened = false;
      queueMicrotask(() => {
        if (![...document.querySelectorAll('.asset-modal-overlay')].some(element => element.style.display !== 'none')) {
          document.body.classList.remove('modal-open');
        }
        origin?.focus();
      });
    },
    trapFocus(event) {
      if (event.key !== 'Tab') return;
      const controls = [...root.querySelector('.asset-modal').querySelectorAll('button, a[href], input, [tabindex="0"]')];
      const first = controls[0];
      const last = controls.at(-1);
      if (event.shiftKey && event.target === first) { event.preventDefault(); last.focus(); }
      else if (!event.shiftKey && event.target === last) { event.preventDefault(); first.focus(); }
    },
    unmount() {
      document.removeEventListener('keydown', escape);
      if (visibility.opened) this.close();
    }
  };
}
