'use strict';
(() => {
  const menu = document.querySelector('.menu-button');
  menu?.addEventListener('click', () => {
    const open = document.querySelector('.sidebar').classList.toggle('open');
    menu.setAttribute('aria-expanded', String(open));
  });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
      document.querySelector('.sidebar')?.classList.remove('open');
      menu?.setAttribute('aria-expanded', 'false');
    }
  });
  document.querySelectorAll('form[data-confirm]').forEach(form => {
    form.addEventListener('submit', event => {
      if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
  });
  try {
    const theme = document.body.dataset.profileTheme?.toLowerCase() || localStorage.getItem('campus-theme') || 'light';
    document.body.dataset.theme = theme;
    localStorage.setItem('campus-theme', theme);
  } catch { document.body.dataset.theme = document.body.dataset.profileTheme?.toLowerCase() || 'light'; }
  document.querySelectorAll('time[data-local-time]').forEach(time => {
    const date = new Date(time.dateTime);
    if (!Number.isNaN(date.valueOf())) time.textContent = date.toLocaleString('es-EC', {timeZone: 'America/Guayaquil', dateStyle: 'short', timeStyle: 'short'});
  });
  document.getElementById('refresh-page')?.addEventListener('click', () => location.reload());
  document.getElementById('print-report')?.addEventListener('click', () => window.print());
  document.getElementById('go-back')?.addEventListener('click', () => history.length > 1 ? history.back() : location.assign('/'));
  const mapPanel = document.getElementById('tracking-map');
  if (!mapPanel) return;
  const svg = document.getElementById('route-map');
  const ns = 'http://www.w3.org/2000/svg';
  let snapshot;
  try { snapshot = JSON.parse(document.getElementById('tracking-data').textContent); }
  catch { document.getElementById('connection-status').textContent = 'No se pudo cargar el esquema'; return; }
  let selectedRoute = mapPanel.dataset.routeId || '';
  let zoom = 1;
  let focus = null;
  const routeFilter = document.getElementById('map-route');
  routeFilter.value = selectedRoute;
  const palette = ['#862b40', '#42887b', '#5678a8', '#b58a45'];
  function element(tag, attrs = {}, text = '') {
    const node = document.createElementNS(ns, tag);
    for (const [key, value] of Object.entries(attrs)) node.setAttribute(key, value);
    if (text) node.textContent = text;
    return node;
  }
  function html(tag, className, text = '') {
    const node = document.createElement(tag);
    node.className = className;
    node.textContent = text;
    return node;
  }
  function routeName(id) { return snapshot.routes.find(route => route.id === id)?.name || `Ruta #${id}`; }
  function busName(id) { return snapshot.buses.find(bus => bus.id === id)?.plate || `Unidad #${id}`; }
  function tooltip(text) {
    const target = document.getElementById('map-tooltip');
    target.textContent = text;
    target.hidden = false;
  }
  function interactive(node, label, action) {
    node.setAttribute('tabindex', '0');
    node.setAttribute('role', 'button');
    node.setAttribute('aria-label', label);
    node.classList.add('map-node');
    node.append(element('title', {}, label));
    node.addEventListener('click', action);
    node.addEventListener('keydown', event => {
      if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); action(); }
    });
  }
  function drawMap() {
    svg.replaceChildren(element('title', {}, 'Mapa esquemático de rutas y posiciones simuladas'));
    const defs = element('defs');
    const pattern = element('pattern', {id: 'grid', width: 52, height: 52, patternUnits: 'userSpaceOnUse', patternTransform: 'rotate(-16)'});
    pattern.append(element('path', {d: 'M 52 0 L 0 0 0 52', fill: 'none', stroke: '#dde3da', 'stroke-width': 1}));
    defs.append(pattern); svg.append(defs);
    svg.append(element('rect', {width: 900, height: 510, fill: '#f0f2ed'}));
    svg.append(element('path', {d: 'M0 370Q130 280 245 340T470 290T720 270T900 140L900 230Q720 350 630 340T400 390T190 420T0 450Z', fill: '#e1e9db'}));
    svg.append(element('rect', {width: 900, height: 510, fill: 'url(#grid)'}));
    const routes = snapshot.routes.filter(route => !selectedRoute || String(route.id) === selectedRoute);
    const buses = snapshot.buses.filter(bus => !selectedRoute || String(bus.routeId) === selectedRoute);
    const coordinates = routes.flatMap(route => route.stops.map(stop => stop.coordinate));
    coordinates.push(...buses.map(bus => bus.position.coordenada));
    if (!coordinates.length) {
      svg.append(element('text', {x:450,y:255,'text-anchor':'middle',fill:'#788373','font-size':17}, 'Agrega paradas para visualizar el recorrido'));
      return;
    }
    const latitudes = coordinates.map(point => point.latitud);
    const longitudes = coordinates.map(point => point.longitud);
    const minLat = Math.min(...latitudes), maxLat = Math.max(...latitudes);
    const minLon = Math.min(...longitudes), maxLon = Math.max(...longitudes);
    const midLat = (minLat + maxLat) / 2, midLon = (minLon + maxLon) / 2;
    const latSpan = Math.max(.006, maxLat - minLat), lonSpan = Math.max(.006, maxLon - minLon);
    const scale = Math.min(650 / (lonSpan * Math.cos(midLat * Math.PI / 180)), 350 / latSpan);
    const project = coordinate => [450 + (coordinate.longitud - midLon) * Math.cos(midLat * Math.PI / 180) * scale,
      255 - (coordinate.latitud - midLat) * scale];
    const center = focus ? project(focus) : [450,255];
    const group = element('g', {transform:`translate(450 255) scale(${zoom}) translate(${-center[0]} ${-center[1]})`});
    svg.append(group);
    routes.forEach(route => {
      const routeIndex = snapshot.routes.findIndex(item => item.id === route.id);
      const color = palette[routeIndex % palette.length];
      const points = route.stops.map(stop => project(stop.coordinate));
      if (points.length > 1) {
        const path = points.map((point, index) => `${index ? 'L' : 'M'}${point.join(' ')}`).join(' ') + ' Z';
        group.append(element('path', {d:path,fill:'none',stroke:'white','stroke-width':9,'stroke-linejoin':'round'}));
        group.append(element('path', {d:path,fill:'none',stroke:color,'stroke-width':4,'stroke-linejoin':'round','stroke-dasharray':routeIndex % 2 ? '9 4' : 'none'}));
      }
      route.stops.forEach(stop => {
        const [x,y] = project(stop.coordinate);
        const marker = element('g');
        marker.append(element('circle',{cx:x,cy:y,r:9,fill:'white',stroke:color,'stroke-width':3}));
        marker.append(element('text',{x,y:y+3,'text-anchor':'middle',fill:color,'font-size':8,'font-weight':700}, String(stop.order)));
        marker.append(element('text',{x:x+17,y:y-14,fill:'#52614d','font-size':11,'font-weight':600,'paint-order':'stroke',stroke:'#f0f2ed','stroke-width':4,'stroke-linejoin':'round'},stop.name.replace(' · Demo','')));
        interactive(marker, `${stop.name}, parada ${stop.order}`, () => tooltip(`${route.code} · ${stop.name} · Parada ${stop.order}`));
        group.append(marker);
      });
    });
    buses.forEach(bus => {
      const [x,y] = project(bus.position.coordenada);
      const active = bus.status === 'ACTIVE';
      const marker = element('g', {transform:`translate(${x} ${y})`});
      marker.append(element('circle',{cx:0,cy:0,r:22,fill:active?'#862b401a':'#77777715'}));
      marker.append(element('rect',{x:-15,y:-11,width:30,height:23,rx:7,fill:active?'#862b40':'#8b9095',stroke:'white','stroke-width':3}));
      marker.append(element('rect',{x:-8,y:-6,width:16,height:8,rx:2,fill:'white'}));
      marker.append(element('circle',{cx:-7,cy:6,r:2,fill:'white'}));
      marker.append(element('circle',{cx:7,cy:6,r:2,fill:'white'}));
      marker.append(element('text',{x:0,y:33,'text-anchor':'middle',fill:active?'#862b40':'#70767b','font-size':9,'font-weight':700,'paint-order':'stroke',stroke:'#f0f2ed','stroke-width':4},bus.plate));
      interactive(marker, `${bus.plate}, ${active ? 'activo' : 'no activo'}, ${Math.round(bus.position.speedKmh)} kilómetros por hora`, () => {
        tooltip(`${bus.plate} · ${bus.journey.state}${bus.journey.arrival ? ' · Siguiente: ' + bus.journey.nextStopName + ' · ' + bus.journey.arrival.minutes + ' min aprox.' : ''}`);
      });
      group.append(marker);
    });
  }
  function renderCards() {
    const container = document.getElementById('live-buses');
    if (!container) return;
    container.replaceChildren();
    const buses = snapshot.buses.filter(bus => !selectedRoute || String(bus.routeId) === selectedRoute);
    if (!buses.length) container.append(html('p','empty','No hay unidades asignadas a este recorrido.'));
    for (const bus of buses) {
      const active = bus.status === 'ACTIVE';
      const card = html('article','bus-card');
      const top = html('div','bus-card-top');
      top.append(html('strong','',bus.plate),html('span',`badge ${active ? 'green' : 'neutral'}`,active ? 'Activo' : bus.status === 'MAINTENANCE' ? 'Mantenimiento' : 'Fuera de servicio'));
      const bottom = html('div','bus-card-bottom');
      bottom.append(html('span','',bus.manualControl ? 'Control manual' : active ? `${Math.round(bus.position.speedKmh)} km/h · Simulado` : 'Sin circulación'));
      const link = html('a','', 'Ver recorrido ↗'); link.href = `/routes/${bus.routeId}`; bottom.append(link);
      const journey = html('div', 'bus-journey');
      journey.append(html('p', '', bus.journey.state));
      if (bus.manualControl) journey.append(html('small', '', 'Control manual · Ubicación declarada'));
      if (bus.journey.arrival) {
        const eta = bus.journey.arrival;
        journey.append(html('strong', '', `Siguiente: ${bus.journey.nextStopName}`),
          html('p', '', `${eta.minutes} min aprox. · ${eta.meters} m${eta.estimatedSpeed ? ' · Velocidad de referencia' : ''}`));
      }
      card.append(top, journey, bottom);
      container.append(card);
    }
  }
  function renderEta() {
    const container = document.getElementById('eta-list');
    if (!container) return;
    container.replaceChildren();
    if (!snapshot.arrivals.length) container.append(html('p','empty','No hay unidades activas con llegada estimada para esta parada.'));
    for (const eta of snapshot.arrivals) {
      const card = html('div','eta-card');
      card.append(html('strong','',busName(eta.busId)),html('span','badge green',eta.state));
      const time = html('div','eta-time');
      time.append(html('strong','',String(eta.minutes)),document.createTextNode(' min '),html('span','',`· ${eta.meters} m${eta.estimatedSpeed ? ' · Velocidad de referencia' : ''}`));
      card.append(time); container.append(card);
    }
  }
  function render() {
    drawMap(); renderCards(); renderEta();
    const stats = {active:snapshot.buses.filter(bus => bus.status === 'ACTIVE').length,
      routes:snapshot.routes.length,stops:snapshot.routes.reduce((sum,route) => sum + route.stops.length,0),
      alerts:snapshot.alerts.filter(alert => !alert.resolvedAt).length};
    document.querySelectorAll('[data-stat]').forEach(node => { node.textContent = stats[node.dataset.stat]; });
    document.getElementById('last-updated').textContent = `Actualizado ${new Date(snapshot.updatedAt).toLocaleTimeString('es-EC', {hour:'2-digit',minute:'2-digit',second:'2-digit'})}`;
    const badge = document.getElementById('connection-status');
    badge.textContent = snapshot.simulationEnabled ? 'Simulación activa' : 'Simulación pausada';
    badge.className = `badge ${snapshot.simulationEnabled ? 'green' : 'neutral'}`;
  }
  routeFilter.addEventListener('change', () => { selectedRoute = routeFilter.value; focus = null; zoom = 1; drawMap(); renderCards(); });
  document.getElementById('zoom-in').addEventListener('click', () => { zoom = Math.min(3,zoom+.25); drawMap(); });
  document.getElementById('zoom-out').addEventListener('click', () => { zoom = Math.max(.75,zoom-.25); drawMap(); });
  document.getElementById('zoom-fit').addEventListener('click', () => { zoom = 1; focus = null; document.getElementById('map-tooltip').hidden = true; drawMap(); });
  render();
  let stopped = false;
  async function poll() {
    if (stopped) return;
    try {
      if (!document.hidden) {
        const query = new URLSearchParams();
        if (mapPanel.dataset.routeId) query.set('routeId',mapPanel.dataset.routeId);
        if (mapPanel.dataset.stopId) query.set('stopId',mapPanel.dataset.stopId);
        const response = await fetch(`/tracking?${query}`, {headers:{Accept:'application/json'},cache:'no-store',signal:AbortSignal.timeout(8000)});
        if (!response.ok || !response.headers.get('content-type')?.includes('application/json')) throw new Error('Respuesta de seguimiento no disponible');
        snapshot = await response.json();
        render();
      }
    } catch {
      const badge = document.getElementById('connection-status');
      badge.textContent = 'Sin conexión · Reintentando'; badge.className = 'badge amber';
    } finally { if (!stopped) window.setTimeout(poll,4000); }
  }
  window.addEventListener('pagehide', () => { stopped = true; });
  window.addEventListener('pageshow', event => { if (event.persisted) { stopped = false; poll(); } });
  window.setTimeout(poll,4000);
})();
