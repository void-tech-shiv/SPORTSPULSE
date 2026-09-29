let allMatches = [];
let allTeams = [];
let allEvents = [];

document.addEventListener('DOMContentLoaded', () => {
    loadData();

    document.getElementById('add-match-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        await addMatch();
    });

    const searchInput = document.getElementById('match-search');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase();
            const filtered = allMatches.filter(m => {
                const teamA = getTeamName(m.teamAId).toLowerCase();
                const teamB = getTeamName(m.teamBId).toLowerCase();
                return teamA.includes(query) || 
                       teamB.includes(query) || 
                       (m.venue && m.venue.toLowerCase().includes(query)) ||
                       (m.status && m.status.toLowerCase().includes(query));
            });
            renderMatches(filtered);
        });
    }
});

async function loadData() {
    try {
        const [matchesRes, teamsRes, eventsRes] = await Promise.all([
            fetch('/api/matches'),
            fetch('/api/teams'),
            fetch('/api/events')
        ]);
        
        allMatches = await matchesRes.json();
        allTeams = await teamsRes.json();
        if (eventsRes.ok) {
            allEvents = await eventsRes.json();
        }
        
        renderMatches(allMatches);
    } catch (error) {
        console.error('Error fetching data:', error);
        document.getElementById('matches-list').innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: var(--status-negative); background: var(--bg-card); border-radius: 8px;">
                <i data-lucide="alert-circle" style="width: 48px; height: 48px; margin-bottom: 1rem;"></i>
                <h3>Failed to load matches</h3>
                <p>Ensure the server is running and the database is accessible.</p>
            </div>
        `;
        if (typeof lucide !== 'undefined') lucide.createIcons();
    }
}

function getTeamName(teamId) {
    const team = allTeams.find(t => t.id === teamId);
    return team ? team.name : teamId;
}

function renderMatches(matches) {
    const container = document.getElementById('matches-list');
    container.innerHTML = '';
    
    if (matches.length === 0) {
        container.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: var(--text-secondary); background: var(--bg-card); border-radius: 8px;">
                <i data-lucide="calendar-x" style="width: 48px; height: 48px; margin-bottom: 1rem; opacity: 0.5;"></i>
                <h3>No matches found</h3>
            </div>
        `;
        if (typeof lucide !== 'undefined') lucide.createIcons();
        return;
    }

    matches.forEach(m => {
        const teamAName = getTeamName(m.teamAId);
        const teamBName = getTeamName(m.teamBId);
        
        let statusClass = 'status-scheduled';
        const statusLower = (m.status || '').toLowerCase();
        if (statusLower === 'completed') statusClass = 'status-completed';
        else if (statusLower === 'ongoing') statusClass = 'status-ongoing';
        
        const matchEvents = allEvents.filter(e => e.matchId === m.id);
        const eventsSummary = matchEvents.length > 0 
            ? `<div style="font-size: 0.8rem; color: var(--accent-primary); margin-top: 0.5rem; text-align: center;">${matchEvents.length} Recorded Events</div>` 
            : `<div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 0.5rem; text-align: center;">No events recorded</div>`;

        const card = document.createElement('div');
        card.className = 'match-card';
        card.innerHTML = `
            <div class="match-header">
                <span style="font-family: monospace; opacity: 0.7;">${m.date || 'TBD'} • ${m.id}</span>
                <span class="match-status ${statusClass}">${m.status || 'Scheduled'}</span>
            </div>
            
            <div class="match-teams">
                <div class="team">
                    <div class="team-logo-small">${teamAName.substring(0, 2).toUpperCase()}</div>
                    <div class="team-name">${teamAName}</div>
                </div>
                
                <div class="match-score">
                    ${m.score ? m.score : 'VS'}
                </div>
                
                <div class="team">
                    <div class="team-logo-small">${teamBName.substring(0, 2).toUpperCase()}</div>
                    <div class="team-name">${teamBName}</div>
                </div>
            </div>
            
            ${eventsSummary}
            
            <div class="match-footer">
                <div class="match-venue">
                    <i data-lucide="map-pin" style="width: 14px; height: 14px;"></i>
                    ${m.venue || 'TBA'}
                </div>
                <button class="delete-match" onclick="deleteMatch('${m.id}')" title="Delete Match">
                    <i data-lucide="trash-2" style="width: 16px; height: 16px;"></i>
                </button>
            </div>
        `;
        container.appendChild(card);
    });

    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
}

async function addMatch() {
    const data = {
        id: document.getElementById('m-id').value,
        competitionId: document.getElementById('m-comp').value,
        teamAId: document.getElementById('m-team-a').value,
        teamBId: document.getElementById('m-team-b').value,
        date: document.getElementById('m-date').value,
        venue: document.getElementById('m-venue').value,
        score: document.getElementById('m-score').value,
        status: document.getElementById('m-status').value
    };

    try {
        const res = await fetch('/api/matches', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (res.ok) {
            closeModals();
            document.getElementById('add-match-form').reset();
            loadData();
        } else {
            alert('Failed to add match.');
        }
    } catch (error) {
        console.error('Error adding match:', error);
    }
}

async function deleteMatch(id) {
    if (confirm('Are you sure you want to delete match ' + id + '?')) {
        try {
            await fetch('/api/matches/' + id, { method: 'DELETE' });
            loadData();
        } catch (error) {
            console.error('Error deleting match:', error);
        }
    }
}

function openAddMatchModal() {
    document.getElementById('add-match-modal').classList.add('active');
}

function closeModals() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
}
