import { dateParams, serviceParams } from '../utils/filter-query.js';

export function ImpactFilters() {
  let form;
  const checked = name => [...form.querySelectorAll(`input[name="${name}"]:checked`)].map(input => input.value);
  const navigate = params => { window.location.href = `?${params.toString()}`; };
  function base() {
    const params = dateParams(form);
    const environment = checked('environment')[0];
    if (environment) params.set('environment', environment);
    return params;
  }
  return {
    mount(element) { form = element; },
    submit() {
      navigate(serviceParams(base(), checked('assets'), checked('containers'), form.querySelectorAll('[name="containers"]').length));
    },
    assetsChanged() { navigate(serviceParams(base(), checked('assets'), [], 0)); },
    environmentChanged() { navigate(base()); },
    reset() { window.location.href = window.location.pathname; }
  };
}

export function DateFilter() {
  let form;
  return {
    mount(element) { form = element; },
    submit() { window.location.href = `?${dateParams(form).toString()}`; }
  };
}
