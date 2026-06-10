const facts = [
    ["Total Accidents (2025) - 10,667", "Total Fatalities (2025) - 223", "Head On Collisions (2025) - 349", "Accidents in the Dark (2025) - 2,458"],
    ["Head On Collisions (2025) - 349", "Accidents Per Day (2025) - 29.2", "Accidents in the Dark (2025) - 2,458", "Accidents on Freeways (2025) - 917"],
    ["Accidents in the Dark (2025) - 2,458", "Total Accidents (2025) - 10,667", "Total Fatalities (2025) - 223", "Head On Collisions (2025) - 349"],
    ["Accidents on Highways (2025) - 1,783", "Accidents in the Dark (2025) - 2,458", "Head On Collisions (2025) - 349", "Daily Car Related Deaths (2025) - 0.6"]
];

let index = 0;

function updateFacts() {
    const fact1Element = document.getElementById("fact1");

    if (fact1Element) {
        fact1Element.innerText = facts[0][index];
        document.getElementById("fact2").innerText = facts[1][index];
        document.getElementById("fact3").innerText = facts[2][index];
        document.getElementById("fact4").innerText = facts[3][index];

        index = (index + 1) % 4;
    }
}

updateFacts();
setInterval(updateFacts, 5000);

const darkModeBtn = document.getElementById("darkModeBtn");
if (localStorage.getItem("theme") === "dark") {
    document.body.classList.add("dark-mode");
}

if (darkModeBtn) {
    darkModeBtn.addEventListener("click", () => {
        document.body.classList.toggle("dark-mode");

        // Save the preference to local storage
        if (document.body.classList.contains("dark-mode")) {
            localStorage.setItem("theme", "dark");
        } else {
            localStorage.setItem("theme", "light");
        }
    });
}
const rawData = [
    { weather: 'Clear', road: 'Dry', crashes: 13500 }, { weather: 'Clear', road: 'Wet', crashes: 910 },
    { weather: 'Raining', road: 'Wet', crashes: 3550 }, { weather: 'Raining', road: 'Dry', crashes: 295 },
    { weather: 'Fog', road: 'Dry', crashes: 155 }, { weather: 'Fog', road: 'Wet', crashes: 290 },
    { weather: 'Snowing', road: 'Snowy', crashes: 530 }, { weather: 'Snowing', road: 'Icy', crashes: 660 },
    { weather: 'Strong winds', road: 'Dry', crashes: 250 }, { weather: 'Dust', road: 'Dry', crashes: 75 },
    { weather: 'Smoke', road: 'Dry', crashes: 45 }, { weather: 'Not known', road: 'Unk.', crashes: 260 },
    { weather: 'Not known', road: 'Muddy', crashes: 60 }
];

const weatherTypes = ['Clear', 'Raining', 'Snowing', 'Fog', 'Smoke', 'Dust', 'Strong winds', 'Not known'];
const roadTypes = ['Dry', 'Wet', 'Muddy', 'Snowy', 'Icy', 'Unk.'];

// UI Setup
document.getElementById('filter-toggle').addEventListener('click', () => {
    const panel = document.getElementById('filter-panel');
    panel.style.display = panel.style.display === 'none' ? 'block' : 'none';
});

function initFilters() {
    weatherTypes.forEach(w => document.getElementById('weather-filters').innerHTML += `<label><input type="checkbox" value="${w}" checked class="w-filter"> ${w}</label><br>`);
    roadTypes.forEach(r => document.getElementById('road-filters').innerHTML += `<label><input type="checkbox" value="${r}" checked class="r-filter"> ${r}</label><br>`);
    document.querySelectorAll('input').forEach(i => i.addEventListener('change', updateDashboard));
}

function updateDashboard() {
    const activeW = Array.from(document.querySelectorAll('.w-filter:checked')).map(i => i.value);
    const activeR = Array.from(document.querySelectorAll('.r-filter:checked')).map(i => i.value);


    const filtered = rawData.filter(d => activeW.includes(d.weather) && activeR.includes(d.road));
    const grandTotal = filtered.reduce((sum, d) => sum + d.crashes, 0);


    const tbody = document.getElementById('table-body');
    tbody.innerHTML = `<tr style="background:#ddd; font-weight:bold;"><td>ALL</td><td>${grandTotal}</td></tr>`;

    activeW.forEach(w => {
        const wTotal = filtered.filter(d => d.weather === w).reduce((sum, d) => sum + d.crashes, 0);
        tbody.innerHTML += `<tr style="background:#eee;"><td><strong>${w}</strong></td><td>${wTotal}</td></tr>`;
        activeR.forEach(r => {
            const val = filtered.find(d => d.weather === w && d.road === r);
            tbody.innerHTML += `<tr><td>&nbsp;&nbsp;&nbsp;${r}</td><td>${val ? val.crashes : 0}</td></tr>`;
        });
    });


}

initFilters();
updateDashboard();