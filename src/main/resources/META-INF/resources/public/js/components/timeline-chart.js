import { defaults, METRICS, timelineConfig } from '../utils/chart-options.js';
import { readPreference, writePreference } from '../utils/storage.js';

export function TimelineChart(timeline) {
  let chart;
  let observer;
  let root;
  const stored = readPreference('wombat.timeline.mode');
  return {
    metric: timeline.series[0]?.key || 'gwp',
    mode: stored === 'bar' ? 'bar' : 'line',
    get accent() { return METRICS[this.metric].accent; },
    mount(element) {
      root = element;
      observer = new MutationObserver(() => this.render());
      observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });
      this.render();
    },
    render() {
      defaults();
      const series = timeline.series.find(series => series.key === this.metric);
      chart?.destroy();
      chart = new Chart(root.querySelector('canvas'), timelineConfig(timeline, series, this.mode));
      root.querySelectorAll('.chart-pill').forEach(pill => {
        pill.classList.remove('green', 'blue', 'yellow');
        pill.classList.add(METRICS[pill.dataset.series || this.metric].accent);
      });
    },
    select(metric) {
      if (this.metric === metric) return;
      this.metric = metric;
      this.render();
    },
    shape(mode) {
      if (this.mode === mode) return;
      this.mode = mode;
      writePreference('wombat.timeline.mode', mode);
      this.render();
    },
    unmount() {
      observer?.disconnect();
      chart?.destroy();
    }
  };
}
