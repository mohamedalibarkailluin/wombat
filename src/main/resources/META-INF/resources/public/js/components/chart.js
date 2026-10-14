import { chartColors, defaults, horizontalConfig, sharesConfig, wrapLabel } from '../utils/chart-options.js';

export function ChartView(metric, services) {
  let chart;
  let observer;
  return {
    services,
    colors: chartColors(),
    mount(element) {
      if (!window.Chart) throw new Error('Chart.js failed to load');
      if (metric !== 'shares') {
        const lines = services.reduce((maximum, service) => Math.max(maximum, wrapLabel(service.name).length), 1);
        const height = Math.max(130, services.length * (6 + lines * 12) + 48);
        element.querySelector('.hbar-inner').style.height = `${height}px`;
        element.querySelector('.chart-wrap').style.height = `${Math.min(height, 320)}px`;
      }
      const render = () => {
        defaults();
        this.colors = chartColors();
        chart?.destroy();
        chart = new Chart(element.querySelector('canvas'), metric === 'shares' ? sharesConfig(services) : horizontalConfig(services, metric));
      };
      render();
      observer = new MutationObserver(render);
      observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });
    },
    unmount() {
      observer?.disconnect();
      chart?.destroy();
    }
  };
}

export function Overview() {
  let observer;
  return {
    mount(element) {
      const shares = element.querySelector('.chart-card');
      const panel = element.querySelector('.kpi-card');
      observer = new ResizeObserver(() => { panel.style.height = `${shares.offsetHeight}px`; });
      observer.observe(shares);
    },
    unmount() { observer?.disconnect(); }
  };
}
