import { reactive } from '/vendor/petite-vue.es.js';
import { mountPage } from './common.js';
import { DateRange } from '../components/date-range.js';
import { Picker } from '../components/picker.js';
import { ImpactFilters } from '../components/impact-filters.js';
import { Modal } from '../components/modal.js';
import { ChartView, Overview } from '../components/chart.js';
import { TimelineChart } from '../components/timeline-chart.js';
import { ServiceRow } from '../components/service-row.js';

const data = JSON.parse(document.getElementById('impact-data').textContent);
mountPage({
  DateRange, Picker, ImpactFilters, Modal, Overview,
  warning: reactive(Modal()),
  ChartView: metric => ChartView(metric, data.services),
  TimelineChart: () => TimelineChart(data.timeline),
  ServiceRow: index => ServiceRow(data.services[index])
});
