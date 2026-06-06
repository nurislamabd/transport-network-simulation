const form = document.querySelector('#simulationForm');
const runButton = document.querySelector('#runButton');
const message = document.querySelector('#message');
const playButton = document.querySelector('#playButton');
const resetButton = document.querySelector('#resetButton');
const hourSlider = document.querySelector('#hourSlider');
const speedSlider = document.querySelector('#speedSlider');
const hourLabel = document.querySelector('#hourLabel');
const speedLabel = document.querySelector('#speedLabel');
const canvas = document.querySelector('#mapCanvas');
const ctx = canvas.getContext('2d');
const metrics = document.querySelector('#metrics');
const downloads = document.querySelector('#downloads');

let replay = null;
let currentHour = 0;
let timer = null;
let playing = false;

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  stopPlayback();
  runButton.disabled = true;
  setMessage('Running Java backend simulation...');

  const body = {
    random: document.querySelector('#randomRun').checked,
    mapX: numberValue('#mapX'),
    mapY: numberValue('#mapY'),
    trucks: numberValue('#trucks'),
    warehouses: numberValue('#warehouses'),
    shipments: numberValue('#shipments')
  };

  try {
    const response = await fetch('/api/simulations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const payload = await response.json();
    if (!response.ok) throw new Error(payload.error || 'Simulation failed.');
    loadReplay(payload);
    setMessage(`Simulation ${payload.runId} completed with ${payload.completedHours} replay hours.`);
  } catch (error) {
    setMessage(error.message, true);
  } finally {
    runButton.disabled = false;
  }
});

playButton.addEventListener('click', () => {
  if (!replay) return;
  playing ? stopPlayback() : startPlayback();
});

resetButton.addEventListener('click', () => {
  stopPlayback();
  setHour(0);
});

hourSlider.addEventListener('input', () => setHour(Number(hourSlider.value)));
speedSlider.addEventListener('input', () => {
  speedLabel.textContent = `${Number(speedSlider.value).toFixed(2).replace(/\.00$/, '')}x`;
  if (playing) {
    stopPlayback();
    startPlayback();
  }
});

function loadReplay(payload) {
  const warehouses = latestById(payload.warehousesData, 'WarehouseID');
  const trucksByHour = groupBy(payload.trucksData, 'Hour');
  const deliveredByHour = deliveredShipmentCounts(payload.shipmentsData);
  replay = { ...payload, warehouses, trucksByHour, deliveredByHour };
  currentHour = 0;

  hourSlider.max = payload.completedHours;
  hourSlider.disabled = false;
  playButton.disabled = false;
  resetButton.disabled = false;

  metrics.innerHTML = `
    <div><strong>Run</strong><span>${escapeHtml(payload.runId.slice(0, 13))}</span></div>
    <div><strong>Trucks</strong><span>${payload.trucks}</span></div>
    <div><strong>Warehouses</strong><span>${payload.warehouses}</span></div>
    <div><strong>Shipments</strong><span>${payload.shipments}</span></div>
  `;
  downloads.innerHTML = `
    <a href="${payload.csv.trucks}" target="_blank" rel="noreferrer">Download truck CSV</a>
    <a href="${payload.csv.warehouses}" target="_blank" rel="noreferrer">Download warehouse CSV</a>
    <a href="${payload.csv.shipments}" target="_blank" rel="noreferrer">Download shipment CSV</a>
  `;

  setHour(0);
}

function startPlayback() {
  if (!replay) return;
  playing = true;
  playButton.textContent = 'Pause';
  const intervalMs = Math.max(80, 650 / Number(speedSlider.value));
  timer = setInterval(() => {
    if (currentHour >= replay.completedHours) {
      stopPlayback();
      return;
    }
    setHour(currentHour + 1);
  }, intervalMs);
}

function stopPlayback() {
  playing = false;
  playButton.textContent = 'Play';
  if (timer) clearInterval(timer);
  timer = null;
}

function setHour(hour) {
  if (!replay) return;
  currentHour = Math.max(0, Math.min(replay.completedHours, hour));
  hourSlider.value = currentHour;
  hourLabel.textContent = `${currentHour} / ${replay.completedHours}`;
  drawReplay();
}

function drawReplay() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  drawGrid();
  if (!replay) {
    ctx.fillStyle = '#9dafc9';
    ctx.font = '24px sans-serif';
    ctx.fillText('Run a simulation to see trucks move across the network.', 48, 72);
    return;
  }

  const scaleX = (x) => 42 + (Number(x) / replay.mapX) * (canvas.width - 84);
  const scaleY = (y) => 42 + (Number(y) / replay.mapY) * (canvas.height - 84);

  for (const warehouse of replay.warehouses) {
    const x = scaleX(warehouse.PosX);
    const y = scaleY(warehouse.PosY);
    ctx.fillStyle = '#8fffba';
    ctx.strokeStyle = '#d8ffe8';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.rect(x - 9, y - 9, 18, 18);
    ctx.fill();
    ctx.stroke();
    ctx.fillStyle = '#e8f0ff';
    ctx.font = '12px sans-serif';
    ctx.fillText(`W${warehouse.WarehouseID}`, x + 12, y - 12);
  }

  const trucks = replay.trucksByHour.get(String(currentHour)) || [];
  for (const truck of trucks) {
    const x = scaleX(truck.PosX);
    const y = scaleY(truck.PosY);
    ctx.fillStyle = truck.Status === 'Done' ? '#9dafc9' : '#69e2ff';
    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.arc(x, y, 8, 0, Math.PI * 2);
    ctx.fill();
    ctx.stroke();
    ctx.fillStyle = '#e8f0ff';
    ctx.font = '12px sans-serif';
    ctx.fillText(`T${truck.TruckID}`, x + 10, y + 4);
  }

  const delivered = replay.deliveredByHour.get(String(currentHour)) || 0;
  ctx.fillStyle = 'rgba(8, 17, 31, .78)';
  ctx.fillRect(22, 20, 270, 78);
  ctx.fillStyle = '#e8f0ff';
  ctx.font = '18px sans-serif';
  ctx.fillText(`Hour ${currentHour}`, 42, 50);
  ctx.font = '14px sans-serif';
  ctx.fillStyle = '#9dafc9';
  ctx.fillText(`${delivered}/${replay.shipments} shipments delivered`, 42, 76);
}

function drawGrid() {
  ctx.fillStyle = '#07101d';
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  ctx.strokeStyle = 'rgba(255,255,255,.06)';
  ctx.lineWidth = 1;
  for (let x = 0; x < canvas.width; x += 55) {
    ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, canvas.height); ctx.stroke();
  }
  for (let y = 0; y < canvas.height; y += 55) {
    ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(canvas.width, y); ctx.stroke();
  }
}

function latestById(rows, idField) {
  const map = new Map();
  for (const row of rows) map.set(String(row[idField]), row);
  return [...map.values()];
}

function groupBy(rows, field) {
  const map = new Map();
  for (const row of rows) {
    const key = String(row[field]);
    if (!map.has(key)) map.set(key, []);
    map.get(key).push(row);
  }
  return map;
}

function deliveredShipmentCounts(rows) {
  const grouped = groupBy(rows, 'Hour');
  const counts = new Map();
  for (const [hour, hourRows] of grouped) {
    counts.set(hour, hourRows.filter((row) => row.Status === 'Delivered').length);
  }
  return counts;
}

function numberValue(selector) { return Number(document.querySelector(selector).value); }
function setMessage(text, isError = false) { message.textContent = text; message.classList.toggle('error', isError); }
function escapeHtml(value) { return String(value).replace(/[&<>'"]/g, (char) => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[char])); }

drawReplay();
