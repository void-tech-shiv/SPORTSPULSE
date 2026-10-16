let allPlayers = [];
let teamsMap = {};

document.addEventListener('DOMContentLoaded', () => {
    loadPlayers();

    const form = document.getElementById('add-player-form');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await addPlayer();
        });
    }

    const searchInput = document.getElementById('player-search');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            const filtered = allPlayers.filter(p => {
                const teamName = teamsMap[p.teamId] || '';
                return (p.id || '').toLowerCase().includes(query)
                    || (p.name || '').toLowerCase().includes(query)
                    || (p.position || '').toLowerCase().includes(query)
                    || (p.teamId || '').toLowerCase().includes(query)
                    || teamName.toLowerCase().includes(query);
            });
            renderPlayers(filtered);
        });
    }
});

async function loadPlayers() {
    try {
        const [playersRes, teamsRes] = await Promise.all([
            fetch('/api/players', { cache: 'no-store' }),
            fetch('/api/teams', { cache: 'no-store' })
        ]);

        if (!playersRes.ok) throw new Error('Failed to load players');

        allPlayers = await playersRes.json();
        teamsMap = {};
        if (teamsRes.ok) {
            const teams = await teamsRes.json();
            teams.forEach(t => { teamsMap[t.id] = t.name; });
        }

        renderPlayers(allPlayers);
    } catch (error) {
        console.error('Error fetching players:', error);
        const list = document.getElementById('players-list');
        if (list) {
            list.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align:center;padding:2rem;color:var(--status-negative);">
                        Failed to load players. Make sure the SportsPulse server is running.
                    </td>
                </tr>`;
        }
    }
}

function renderPlayers(players) {
    const tbody = document.getElementById('players-list');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!players.length) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;padding:2rem;color:var(--text-secondary);">No players found.</td></tr>';
        return;
    }

    players.forEach(p => {
        const perf = Number(p.performanceScore) || 0;
        const perfScore = perf > 0 ? perf.toFixed(1) : 'Data unavailable';
        let perfColor = 'var(--status-warning)';
        if (perf >= 80) perfColor = 'var(--status-positive)';
        else if (perf > 0 && perf < 50) perfColor = 'var(--status-negative)';
        else if (!perf) perfColor = 'var(--text-secondary)';

        const teamDisplay = p.teamId ? (teamsMap[p.teamId] || p.teamId) : '-';
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <div style="display:flex;align-items:center;gap:.75rem;">
                    <div style="width:32px;height:32px;border-radius:50%;background:var(--bg-card);display:flex;align-items:center;justify-content:center;font-weight:bold;color:var(--accent-primary);">
                        ${(p.name || '?').charAt(0)}
                    </div>
                    <div>
                        <div style="font-weight:600;">${p.name || '-'}</div>
                        <div style="font-size:.75rem;color:var(--text-secondary);">${p.id || '-'}</div>
                    </div>
                </div>
            </td>
            <td>${p.teamId ? `<span style="background:rgba(255,255,255,.1);padding:.25rem .5rem;border-radius:4px;font-size:.8rem;">${teamDisplay}</span>` : '-'}</td>
            <td>${p.position || '-'}</td>
            <td>${p.age || '-'}</td>
            <td>${p.matchesPlayed || 0}</td>
            <td>${p.goals || 0} / ${p.runs || 0}</td>
            <td>
                <span style="display:inline-flex;align-items:center;gap:.5rem;">
                    <span class="status-indicator" style="background-color:${perfColor};box-shadow:0 0 8px ${perfColor};"></span>
                    ${perfScore} ${perf ? 'pts' : ''}
                </span>
            </td>
            <td style="text-align:right;">
                <div style="display:flex;gap:.4rem;justify-content:flex-end;">
                    <button class="btn btn-outline" style="padding:.25rem .55rem;" onclick="editPlayer('${p.id}')">Edit</button>
                    <button class="btn btn-outline" style="padding:.25rem .55rem;border-color:var(--status-negative);color:var(--status-negative);" onclick="deletePlayer('${p.id}')">Delete</button>
                </div>
            </td>`;
        tbody.appendChild(tr);
    });
}

async function addPlayer() {
    const data = {
        id: document.getElementById('p-id').value.trim(),
        name: document.getElementById('p-name').value.trim(),
        teamId: document.getElementById('p-team').value.trim(),
        position: document.getElementById('p-position').value.trim(),
        age: parseInt(document.getElementById('p-age').value, 10) || 0,
        matchesPlayed: 0,
        goals: 0,
        assists: 0,
        wickets: 0,
        runs: 0,
        fitnessScore: 0,
        performanceScore: 0
    };

    try {
        const res = await fetch('/api/players', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!res.ok) throw new Error('Failed to add player');

        closeModals();
        document.getElementById('add-player-form').reset();
        await loadPlayers();
    } catch (error) {
        console.error('Error adding player:', error);
        alert('Failed to add player. Check the ID and input values.');
    }
}

async function editPlayer(id) {
    const player = allPlayers.find(p => p.id === id);
    if (!player) return;

    const name = prompt('Player name:', player.name || '');
    if (name === null) return;
    const teamId = prompt('Team ID:', player.teamId || '');
    if (teamId === null) return;
    const position = prompt('Position:', player.position || '');
    if (position === null) return;
    const age = promptNumber('Age:', player.age);
    if (age === null) return;
    const matchesPlayed = promptNumber('Matches played:', player.matchesPlayed);
    if (matchesPlayed === null) return;
    const goals = promptNumber('Goals:', player.goals);
    if (goals === null) return;
    const assists = promptNumber('Assists:', player.assists);
    if (assists === null) return;
    const wickets = promptNumber('Wickets:', player.wickets);
    if (wickets === null) return;
    const runs = promptNumber('Runs:', player.runs);
    if (runs === null) return;
    const fitnessScore = promptNumber('Fitness score:', player.fitnessScore, true);
    if (fitnessScore === null) return;
    const performanceScore = promptNumber('Performance score:', player.performanceScore, true);
    if (performanceScore === null) return;

    const payload = { name, teamId, position, age, matchesPlayed, goals, assists, wickets, runs, fitnessScore, performanceScore };

    try {
        const res = await fetch('/api/players/' + encodeURIComponent(id), {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error('Update failed');
        await loadPlayers();
    } catch (error) {
        console.error('Error updating player:', error);
        alert('Failed to update player.');
    }
}

function promptNumber(label, currentValue, allowDecimal = false) {
    const value = prompt(label, currentValue ?? 0);
    if (value === null) return null;
    const parsed = allowDecimal ? parseFloat(value) : parseInt(value, 10);
    return Number.isFinite(parsed) ? parsed : 0;
}

async function deletePlayer(id) {
    if (!confirm('Are you sure you want to delete player ' + id + '?')) return;
    try {
        const res = await fetch('/api/players/' + encodeURIComponent(id), { method: 'DELETE' });
        if (!res.ok) throw new Error('Delete failed');
        await loadPlayers();
    } catch (error) {
        console.error('Error deleting player:', error);
        alert('Failed to delete player.');
    }
}

function openAddPlayerModal() {
    const modal = document.getElementById('add-player-modal');
    if (modal) modal.classList.add('active');
}

function closeModals() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
}
