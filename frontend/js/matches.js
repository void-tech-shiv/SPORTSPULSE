let allMatches = [];
let allTeams = [];
let allEvents = [];

document.addEventListener('DOMContentLoaded', () => {
    loadData();

    const form = document.getElementById('add-match-form');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await addMatch();
        });
    }

    const searchInput = document.getElementById('match-search');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            const filtered = allMatches.filter(m => {
                const teamA = getTeamName(m.teamAId).toLowerCase();
                const teamB = getTeamName(m.teamBId).toLowerCase();
                return (m.id || '').toLowerCase().includes(query)
                    || teamA.includes(query)
                    || teamB.includes(query)
                    || (m.teamAId || '').toLowerCase().includes(query)
                    || (m.teamBId || '').toLowerCase().includes(query)
                    || (m.date || '').toLowerCase().includes(query)
                    || (m.venue || '').toLowerCase().includes(query)
                    || (m.status || '').toLowerCase().includes(query)
                    || (m.competitionId || '').toLowerCase().includes(query);
            });
            renderMatches(filtered);
        });
    }
});

async function loadData() {
    try {
        const [matchesRes, teamsRes, eventsRes] = await Promise.all([
            fetch('/api/matches', { cache: 'no-store' }),
            fetch('/api/teams', { cache: 'no-store' }),
            fetch('/api/events', { cache: 'no-store' })
        ]);

        if (!matchesRes.ok) throw new Error('Failed to load matches');
        allMatches = await matchesRes.json();
        allTeams = teamsRes.ok ? await teamsRes.json() : [];
        allEvents = eventsRes.ok ? await eventsRes.json() : [];
        renderMatches(allMatches);
    } catch (error) {
        console.error('Error fetching data:', error);
        const container = document.getElementById('matches-list');
        if (container) {
            container.innerHTML = `
                <div style="grid-column:1/-1;text-align:center;padding:3rem;color:var(--status-negative);background:var(--bg-card);border-radius:8px;">
                    <i data-lucide="alert-circle" style="width:48px;height:48px;margin-bottom:1rem;"></i>
                    <h3>Failed to load matches</h3>
                    <p>Ensure the SportsPulse server is running.</p>
                </div>`;
        }
        if (typeof lucide !== 'undefined') lucide.createIcons();
    }
}

function getTeamName(teamId) {
    const team = allTeams.find(t => t.id === teamId);
    return team ? team.name : (teamId || 'TBA');
}

function renderMatches(matches) {
    const container = document.getElementById('matches-list');
    if (!container) return;
    container.innerHTML = '';

    if (!matches.length) {
        container.innerHTML = '<div style="grid-column:1/-1;text-align:center;padding:3rem;color:var(--text-secondary);background:var(--bg-card);border-radius:8px;"><h3>No matches found</h3></div>';
        return;
    }

    matches.forEach(m => {
        const teamAName = getTeamName(m.teamAId);
        const teamBName = getTeamName(m.teamBId);
        const statusLower = (m.status || '').toLowerCase();
        let statusClass = 'status-scheduled';
        if (statusLower === 'completed') statusClass = 'status-completed';
        else if (statusLower === 'ongoing') statusClass = 'status-ongoing';

        const matchEvents = allEvents.filter(e => e.matchId === m.id);
        const eventsSummary = matchEvents.length
            ? `<div style="font-size:.8rem;color:var(--accent-primary);margin-top:.5rem;text-align:center;">${matchEvents.length} Recorded Events</div>`
            : '<div style="font-size:.8rem;color:var(--text-secondary);margin-top:.5rem;text-align:center;">No events recorded</div>';

        const card = document.createElement('div');
        card.className = 'match-card';
        card.innerHTML = `
            <div class="match-header">
                <span style="font-family:monospace;opacity:.7;">${m.date || 'TBD'} • ${m.id || '-'}</span>
                <span class="match-status ${statusClass}">${m.status || 'Scheduled'}</span>
            </div>
            <div class="match-teams">
                <div class="team"><div class="team-logo-small">${teamAName.substring(0, 2).toUpperCase()}</div><div class="team-name">${teamAName}</div></div>
                <div class="match-score">${m.score || 'VS'}</div>
                <div class="team"><div class="team-logo-small">${teamBName.substring(0, 2).toUpperCase()}</div><div class="team-name">${teamBName}</div></div>
            </div>
            ${eventsSummary}
            <div style="text-align:center;font-size:.75rem;color:var(--text-secondary);margin-top:.35rem;">Competition: ${m.competitionId || 'N/A'}</div>
            <div class="match-footer">
                <div class="match-venue"><i data-lucide="map-pin" style="width:14px;height:14px;"></i>${m.venue || 'TBA'}</div>
                <div style="display:flex;gap:.4rem;align-items:center;">
                    <button class="btn btn-outline" style="padding:.3rem .55rem;font-size:.8rem;" onclick="editMatch('${m.id}')">Edit</button>
                    <button class="delete-match" onclick="deleteMatch('${m.id}')" title="Delete Match"><i data-lucide="trash-2" style="width:16px;height:16px;"></i></button>
                </div>
            </div>`;
        container.appendChild(card);
    });

    if (typeof lucide !== 'undefined') lucide.createIcons();
}

async function addMatch() {
    const data = {
        id: document.getElementById('m-id').value.trim(),
        competitionId: document.getElementById('m-comp').value.trim(),
        teamAId: document.getElementById('m-team-a').value.trim(),
        teamBId: document.getElementById('m-team-b').value.trim(),
        date: document.getElementById('m-date').value.trim(),
        venue: document.getElementById('m-venue').value.trim(),
        score: document.getElementById('m-score').value.trim(),
        status: document.getElementById('m-status').value.trim()
    };

    try {
        const res = await fetch('/api/matches', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!res.ok) throw new Error('Add failed');
        closeModals();
        document.getElementById('add-match-form').reset();
        await loadData();
    } catch (error) {
        console.error('Error adding match:', error);
        alert('Failed to schedule match. Check the ID and values.');
    }
}

async function editMatch(id) {
    const match = allMatches.find(m => m.id === id);
    if (!match) return;

    const competitionId = prompt('Competition ID:', match.competitionId || '');
    if (competitionId === null) return;
    const teamAId = prompt('Team A ID:', match.teamAId || '');
    if (teamAId === null) return;
    const teamBId = prompt('Team B ID:', match.teamBId || '');
    if (teamBId === null) return;
    const date = prompt('Date (YYYY-MM-DD):', match.date || '');
    if (date === null) return;
    const venue = prompt('Venue:', match.venue || '');
    if (venue === null) return;
    const score = prompt('Score:', match.score || '0-0');
    if (score === null) return;
    const status = prompt('Status:', match.status || 'Scheduled');
    if (status === null) return;

    try {
        const res = await fetch('/api/matches/' + encodeURIComponent(id), {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ competitionId, teamAId, teamBId, date, venue, score, status })
        });
        if (!res.ok) throw new Error('Update failed');
        await loadData();
    } catch (error) {
        console.error('Error updating match:', error);
        alert('Failed to update match.');
    }
}

async function deleteMatch(id) {
    if (!confirm('Are you sure you want to delete match ' + id + '?')) return;
    try {
        const res = await fetch('/api/matches/' + encodeURIComponent(id), { method: 'DELETE' });
        if (!res.ok) throw new Error('Delete failed');
        await loadData();
    } catch (error) {
        console.error('Error deleting match:', error);
        alert('Failed to delete match.');
    }
}

function openAddMatchModal() {
    const modal = document.getElementById('add-match-modal');
    if (modal) modal.classList.add('active');
}

function closeModals() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
}
