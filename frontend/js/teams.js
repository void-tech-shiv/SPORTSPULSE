let allTeams = [];
let playersData = [];

document.addEventListener('DOMContentLoaded', () => {
    loadData();

    const form = document.getElementById('add-team-form');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await addTeam();
        });
    }

    const searchInput = document.getElementById('team-search');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            const filtered = allTeams.filter(t =>
                (t.id || '').toLowerCase().includes(query)
                || (t.name || '').toLowerCase().includes(query)
                || (t.sport || '').toLowerCase().includes(query)
                || (t.coach || '').toLowerCase().includes(query)
            );
            renderTeams(filtered);
        });
    }
});

async function loadData() {
    try {
        const [teamsRes, playersRes] = await Promise.all([
            fetch('/api/teams', { cache: 'no-store' }),
            fetch('/api/players', { cache: 'no-store' })
        ]);

        if (!teamsRes.ok) throw new Error('Failed to load teams');
        allTeams = await teamsRes.json();
        playersData = playersRes.ok ? await playersRes.json() : [];
        renderTeams(allTeams);
    } catch (error) {
        console.error('Error fetching data:', error);
        const container = document.getElementById('teams-list');
        if (container) {
            container.innerHTML = `
                <div style="grid-column:1/-1;text-align:center;padding:3rem;color:var(--status-negative);background:var(--bg-card);border-radius:8px;">
                    <i data-lucide="alert-circle" style="width:48px;height:48px;margin-bottom:1rem;"></i>
                    <h3>Failed to load teams</h3>
                    <p>Ensure the SportsPulse server is running.</p>
                </div>`;
        }
        if (typeof lucide !== 'undefined') lucide.createIcons();
    }
}

function renderTeams(teams) {
    const container = document.getElementById('teams-list');
    if (!container) return;
    container.innerHTML = '';

    if (!teams.length) {
        container.innerHTML = '<div style="grid-column:1/-1;text-align:center;padding:3rem;color:var(--text-secondary);background:var(--bg-card);border-radius:8px;"><h3>No teams found</h3></div>';
        return;
    }

    teams.forEach(t => {
        const teamPlayers = playersData.filter(p => p.teamId === t.id);
        const squadSize = teamPlayers.length;
        const totalMatches = teamPlayers.reduce((sum, p) => sum + (Number(p.matchesPlayed) || 0), 0);
        const avgAge = squadSize
            ? (teamPlayers.reduce((sum, p) => sum + (Number(p.age) || 0), 0) / squadSize).toFixed(1)
            : '0.0';
        const scoredPlayers = teamPlayers.filter(p => Number(p.performanceScore) > 0);
        const avgPerf = scoredPlayers.length
            ? (scoredPlayers.reduce((sum, p) => sum + Number(p.performanceScore), 0) / scoredPlayers.length).toFixed(1)
            : '-';

        const card = document.createElement('div');
        card.className = 'team-card';
        card.innerHTML = `
            <div class="team-header">
                <div class="team-logo">${(t.name || 'TM').substring(0, 2).toUpperCase()}</div>
                <div class="team-info">
                    <h3>${t.name || '-'}</h3>
                    <p>${t.sport || 'General Sport'} • ID: ${t.id || '-'}</p>
                    <p style="margin-top:.25rem;">Coach: ${t.coach || '-'}</p>
                </div>
            </div>
            <div class="team-stats">
                <div class="stat-item"><span class="stat-label">Squad Size</span><span class="stat-value">${squadSize}</span></div>
                <div class="stat-item"><span class="stat-label">Avg Age</span><span class="stat-value">${avgAge}</span></div>
                <div class="stat-item"><span class="stat-label">Avg Perf</span><span class="stat-value">${avgPerf}</span></div>
                <div class="stat-item"><span class="stat-label">Total Matches</span><span class="stat-value">${totalMatches}</span></div>
            </div>
            <div class="team-actions" style="display:flex;gap:.5rem;align-items:center;">
                <button class="btn btn-outline" style="padding:.4rem .75rem;font-size:.85rem;" onclick="viewRoster('${t.id}', '${String(t.name || '').replaceAll("'", "\\'")}')">View Roster</button>
                <button class="btn btn-outline" style="padding:.4rem .75rem;font-size:.85rem;" onclick="editTeam('${t.id}')">Edit</button>
                <button class="delete-team" onclick="deleteTeam('${t.id}')" title="Delete Team"><i data-lucide="trash-2" style="width:18px;height:18px;"></i></button>
            </div>`;
        container.appendChild(card);
    });

    if (typeof lucide !== 'undefined') lucide.createIcons();
}

function viewRoster(teamId, teamName) {
    const teamPlayers = playersData.filter(p => p.teamId === teamId);
    const title = document.getElementById('roster-team-name');
    if (title) title.textContent = teamName + ' Roster';

    const tbody = document.getElementById('roster-list');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!teamPlayers.length) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:2rem;color:var(--text-secondary);">No players assigned to this team</td></tr>';
    } else {
        teamPlayers.forEach(p => {
            const perf = Number(p.performanceScore) || 0;
            tbody.innerHTML += `
                <tr>
                    <td><div style="font-weight:600;">${p.name}</div><div style="font-size:.75rem;color:var(--text-secondary);">${p.id}</div></td>
                    <td>${p.position || '-'}</td>
                    <td>${p.age || '-'}</td>
                    <td>${perf ? perf.toFixed(1) : '-'}</td>
                </tr>`;
        });
    }

    const modal = document.getElementById('view-roster-modal');
    if (modal) modal.classList.add('active');
}

async function addTeam() {
    const data = {
        id: document.getElementById('t-id').value.trim(),
        name: document.getElementById('t-name').value.trim(),
        sport: document.getElementById('t-sport').value.trim(),
        coach: document.getElementById('t-coach').value.trim()
    };

    try {
        const res = await fetch('/api/teams', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!res.ok) throw new Error('Add failed');
        closeModals();
        document.getElementById('add-team-form').reset();
        await loadData();
    } catch (error) {
        console.error('Error adding team:', error);
        alert('Failed to add team. Check the ID and values.');
    }
}

async function editTeam(id) {
    const team = allTeams.find(t => t.id === id);
    if (!team) return;

    const name = prompt('Team name:', team.name || '');
    if (name === null) return;
    const sport = prompt('Sport:', team.sport || '');
    if (sport === null) return;
    const coach = prompt('Coach:', team.coach || '');
    if (coach === null) return;

    try {
        const res = await fetch('/api/teams/' + encodeURIComponent(id), {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, sport, coach })
        });
        if (!res.ok) throw new Error('Update failed');
        await loadData();
    } catch (error) {
        console.error('Error updating team:', error);
        alert('Failed to update team.');
    }
}

async function deleteTeam(id) {
    if (!confirm('Are you sure you want to delete team ' + id + '? This may affect players assigned to this team.')) return;
    try {
        const res = await fetch('/api/teams/' + encodeURIComponent(id), { method: 'DELETE' });
        if (!res.ok) throw new Error('Delete failed');
        await loadData();
    } catch (error) {
        console.error('Error deleting team:', error);
        alert('Failed to delete team.');
    }
}

function openAddTeamModal() {
    const modal = document.getElementById('add-team-modal');
    if (modal) modal.classList.add('active');
}

function closeModals() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
}
