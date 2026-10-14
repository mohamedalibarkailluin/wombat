import {
  clampStartDay, dateToUtcDay, formatUtcDayRange, maxSpanDaysOf,
  pickerDateToUtcDay, shiftUtcDay, shiftUtcMonth, utcDayDiff,
  utcDayToPickerDate, utcIsoToEndDay, utcIsoToStartDay
} from '../utils/utc-date.js';

export function RangeLabel() {
  return { mount(element) { element.textContent = formatUtcDayRange(element.dataset.start, element.dataset.end); } };
}

export function DateRange() {
  let instance;
  let timer;
  return {
    mount(element) {
      const picker = element.querySelector('#dateRangePicker');
      const from = element.querySelector('[name="from"]');
      const to = element.querySelector('[name="to"]');
      from.value = utcIsoToStartDay(picker.dataset.fromUtc);
      to.value = utcIsoToEndDay(picker.dataset.toUtc);
      if (!window.easepick) {
        console.warn('easepick is unavailable; using native date controls');
        picker.style.display = 'none';
        [from, to].forEach(input => {
          input.type = 'date';
          input.classList.add('date-range-native');
        });
        return;
      }
      const maximum = maxSpanDaysOf(picker);
      const today = dateToUtcDay(new Date());
      const preset = start => [utcDayToPickerDate(start), utcDayToPickerDate(today)];
      const months = count => preset(shiftUtcMonth(shiftUtcDay(today, 1), -count));
      let clamping = false;
      instance = new window.easepick.create({
        element: picker,
        css: ['/vendor/easepick.css', '/public/easepick-theme.css'],
        zIndex: 10,
        grid: window.matchMedia('(max-width: 720px)').matches ? 1 : 2,
        calendars: window.matchMedia('(max-width: 720px)').matches ? 1 : 2,
        format: 'YYYY-MM-DD',
        plugins: ['RangePlugin', 'PresetPlugin', 'LockPlugin'],
        RangePlugin: {
          tooltip: true, repick: true, delimiter: ' \u2192 ',
          startDate: from.value ? utcDayToPickerDate(from.value) : undefined,
          endDate: to.value ? utcDayToPickerDate(to.value) : undefined
        },
        PresetPlugin: {
          position: 'bottom',
          customPreset: {
            Today: preset(today), 'Last 7 days': preset(shiftUtcDay(today, -6)),
            'Last 3 months': months(3), 'Last 6 months': months(6), 'Last year': months(12)
          }
        },
        LockPlugin: {
          filter(date, picked) {
            return !!maximum && picked?.length === 1 &&
              Math.abs(utcDayDiff(pickerDateToUtcDay(picked[0].toJSDate()), pickerDateToUtcDay(date.toJSDate()))) > maximum - 1;
          }
        },
        setup(control) {
          control.on('select', event => {
            const { start, end } = event.detail;
            const endDay = end ? pickerDateToUtcDay(end) : '';
            const startDay = clampStartDay(start ? pickerDateToUtcDay(start) : '', endDay, maximum);
            if (startDay) from.value = startDay;
            if (endDay) to.value = endDay;
            if (clamping) { clamping = false; return; }
            if (start && startDay && startDay !== pickerDateToUtcDay(start)) {
              clamping = true;
              timer = setTimeout(() => instance.setDateRange(utcDayToPickerDate(startDay), utcDayToPickerDate(endDay)), 0);
            }
          });
        }
      });
    },
    unmount() {
      clearTimeout(timer);
      instance?.destroy();
    }
  };
}
