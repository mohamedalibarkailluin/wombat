import { defaults, detailConfig } from '../utils/chart-options.js';

export function ServiceRow(service) {
  let root;
  let charts = [];
  let observer;
  let disposed = false;
  function render() {
    defaults();
    charts.forEach(chart => chart.destroy());
    charts = ['gwp', 'pe', 'adp'].map((metric, index) =>
      new Chart(root.querySelectorAll('canvas')[index], detailConfig(service, metric)));
  }
  return {
    expanded: false,
    mount(element) {
      root = element;
      observer = new MutationObserver(() => { if (charts.length) render(); });
      observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });
    },
    toggle() {
      this.expanded = !this.expanded;
      if (!this.expanded) return;
      queueMicrotask(() => {
        if (disposed) return;
        if (!charts.length) render();
        else charts.forEach(chart => { chart.resize(); chart.reset(); chart.update(); });
      });
    },
    unmount() {
      disposed = true;
      observer?.disconnect();
      charts.forEach(chart => chart.destroy());
    }
  };
}
