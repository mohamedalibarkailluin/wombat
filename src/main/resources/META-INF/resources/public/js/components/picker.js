export function Picker() {
  let root;
  let outside;
  let escape;
  return {
    expanded: false,
    query: '',
    revision: 0,
    get label() {
      this.revision;
      if (!root) return '';
      const inputs = [...root.querySelectorAll('.container-list input')];
      const selected = inputs.filter(input => input.checked);
      return inputs[0]?.type === 'radio'
        ? selected[0]?.dataset.label || selected[0]?.value || ''
        : `${selected.length} / ${inputs.length} ${root.dataset.kind}`;
    },
    mount(element) {
      root = element;
      this.revision++;
      outside = event => { if (!root.contains(event.target)) this.expanded = false; };
      escape = event => { if (event.key === 'Escape') this.expanded = false; };
      document.addEventListener('click', outside);
      document.addEventListener('keydown', escape);
    },
    toggle() {
      this.expanded = !this.expanded;
      if (this.expanded) {
        this.query = '';
        queueMicrotask(() => root.querySelector('.container-search')?.focus());
      }
    },
    visible(label) { return label.toLowerCase().includes(this.query.toLowerCase()); },
    select(checked) {
      root.querySelectorAll('.container-item').forEach(item => {
        if (item.style.display !== 'none') item.querySelector('input').checked = checked;
      });
      this.revision++;
    },
    changed() { this.revision++; },
    unmount() {
      document.removeEventListener('click', outside);
      document.removeEventListener('keydown', escape);
    }
  };
}
