let allTeams = [];
let playersData = [];

document.addEventListener('DOMContentLoaded', () => {
    loadData();

    document.getElementById('add-team-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        await addTeam();
    });

    const searchInput = document.getElementById('team-search');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase();
            const filtered = allTeams.filter(t => 
                t.name.toLowerCase().includes(query) || 
                (t.sport && t.sport.toLowerCase().includes(query))
            );
            renderTeams(filtered);
        });
    }
});

async function loadData() {
    try {
        // Fetch both teams and players to calculate stats (e.g. squad size)
        const [teamsRes, playersRes] = await Promise.all([
            fetch('/api/teams'),
            fetch('/api/players')
        ]);
        
        allTeams = await teamsRes.json();
        playersData = await playersRes.json();
        
        renderTeams(allTeams);
    } catch (error) {
        console.error('Error fetching data:', error);
        document.getElementById('teams-list').innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: var(--status-negative); background: var(--bg-card); border-radius: 8px;">
                <i data-lucide="alert-circle" style="width: 48px; height: 48px; margin-bottom: 1rem;"></i>
                <h3>Failed to load teams</h3>
                <p>Ensure the server is running and the database is accessible.</p>
            </div>
        `;
        if (typeof lucide !== 'undefined') lucide.createIcons();
    }
}

function renderTeams(teams) {
    const container = document.getElementById('teams-list');
    container.innerHTML = '';
    
    if (teams.length === 0) {
        container.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: var(--text-secondary); background: var(--bg-card); border-radius: 8px;">
                <i data-lucide="shield-off" style="width: 48px; height: 48px; margin-bottom: 1rem; opacity: 0.5;"></i>
                <h3>No teams found</h3>
            </div>
        `;
        if (typeof lucide !== 'undefined') lucide.createIcons();
        return;
    }

    teams.forEach(t => {
        // Calculate stats
        const teamPlayers = playersData.filter(p => p.teamId === t.id);
        const squadSize = teamPlayers.length;
        const totalMatches = teamPlayers.reduce((sum, p) => sum + (p.matchesPlayed || 0), 0);
        const avgAge = squadSize > 0 
            ? (teamPlayers.reduce((sum, p) => sum + (p.age || 0), 0) / squadSize).toFixed(1) 
            : 0;
            
        // Calculate avg performance
        const scoredPlayers = teamPlayers.filter(p => p.performanceScore > 0);
        const avgPerf = scoredPlayers.length > 0 
            ? (scoredPlayers.reduce((sum, p) => sum + p.performanceScore, 0) / scoredPlayers.length).toFixed(1)
            : '-';

        const card = document.createElement('div');
        card.className = 'team-card';
        card.innerHTML = `
            <div class="team-header">
                <div class="team-logo">
                    ${t.name.substring(0, 2).toUpperCase()}
                </div>
                <div class="team-info">
                    <h3>${t.name}</h3>
                    <p>${t.sport || 'General Sport'} • ID: ${t.id}</p>
                </div>
            </div>
            
            <div class="team-stats">
                <div class="stat-item">
                    <span class="stat-label">Squad Size</span>
                    <span class="stat-value">${squadSize}</span>
                </div>
                <div class="stat-item">
                    <span class="stat-label">Avg Age</span>
                    <span class="stat-value">${avgAge}</span>
                </div>
                <div class="stat-item">
                    <span class="stat-label">Avg Perf</span>
                    <span class="stat-value">${avgPerf}</span>
                </div>
                <div class="stat-item">
                    <span class="stat-label">Total Matches</span>
                    <span class="stat-value">${totalMatches}</span>
                </div>
            </div>
            
            <div class="team-actions">
                <button class="btn btn-outline" style="padding: 0.4rem 0.75rem; font-size: 0.85rem;" onclick="viewRoster('${t.id}', '${t.name}')">View Roster</button>
                <button class="delete-team" onclick="deleteTeam('${t.id}')" title="Delete Team">
                    <i data-lucide="trash-2" style="width: 18px; height: 18px;"></i>
                </button>
            </div>
        `;
        container.appendChild(card);
    });

    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
}

function viewRoster(teamId, teamName) {
    const teamPlayers = playersData.filter(p => p.teamId === teamId);
    document.getElementById('roster-team-name').textContent = teamName + ' Roster';
    
    const tbody = document.getElementById('roster-list');
    tbody.innerHTML = '';
    
    if (teamPlayers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; padding: 2rem; color: var(--text-secondary);">No players assigned to this team</td></tr>';
    } else {
        teamPlayers.forEach(p => {
            const perfScore = p.performanceScore ? p.performanceScore.toFixed(1) : '-';
            let perfColor = 'var(--status-warning)'; 
            if (p.performanceScore >= 80) perfColor = 'var(--status-positive)';
            else if (p.performanceScore < 50 && p.performanceScore > 0) perfColor = 'var(--status-negative)';
            else if (!p.performanceScore) perfColor = 'var(--text-secondary)';
            
            tbody.innerHTML += `
                <tr>
                    <td>
                        <div style="font-weight: 600;">${p.name}</div>
                        <div style="font-size: 0.75rem; color: var(--text-secondary);">${p.id}</div>
                    </td>
                    <td>${p.position || '-'}</td>
                    <td>${p.age || '-'}</td>
                    <td>
                        <span style="display: inline-flex; align-items: center; gap: 0.5rem;">
                            <span class="status-indicator" style="background-color: ${perfColor}; box-shadow: 0 0 8px ${perfColor};"></span>
                            ${perfScore}
                        </span>
                    </td>
                </tr>
            `;
        });
    }
    
    document.getElementById('view-roster-modal').classList.add('active');
}

async function addTeam() {
    const data = {
        id: document.getElementById('t-id').value,
        name: document.getElementById('t-name').value,
        sport: document.getElementById('t-sport').value,
        coach: document.getElementById('t-coach').value
    };

    try {
        const res = await fetch('/api/teams', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (res.ok) {
            closeModals();
            document.getElementById('add-team-form').reset();
            loadData();
        } else {
            alert('Failed to add team.');
        }
    } catch (error) {
        console.error('Error adding team:', error);
    }
}

async function deleteTeam(id) {
    if (confirm('Are you sure you want to delete team ' + id + '? This may affect players assigned to this team.')) {
        try {
            await fetch('/api/teams/' + id, { method: 'DELETE' });
            loadData();
        } catch (error) {
            console.error('Error deleting team:', error);
        }
    }
}

function openAddTeamModal() {
    document.getElementById('add-team-modal').classList.add('active');
}

function closeModals() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
}
