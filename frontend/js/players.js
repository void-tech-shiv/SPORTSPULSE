let allPlayers = [];

document.addEventListener('DOMContentLoaded', () => {
    loadPlayers();

    document.getElementById('add-player-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        await addPlayer();
    });

    const searchInput = document.getElementById('player-search');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase();
            const filtered = allPlayers.filter(p => 
                p.name.toLowerCase().includes(query) || 
                (p.position && p.position.toLowerCase().includes(query))
            );
            renderPlayers(filtered);
        });
    }
});

let teamsMap = {};

async function loadPlayers() {
    try {
        const [playersRes, teamsRes] = await Promise.all([
            fetch('/api/players'),
            fetch('/api/teams')
        ]);
        
        if (!playersRes.ok) throw new Error('Failed to load players');
        
        allPlayers = await playersRes.json();
        if (teamsRes.ok) {
            const teams = await teamsRes.json();
            teams.forEach(t => { teamsMap[t.id] = t.name; });
        }
        
        renderPlayers(allPlayers);
    } catch (error) {
        console.error('Error fetching players:', error);
        document.getElementById('players-list').innerHTML = `
            <tr>
                <td colspan="8" style="text-align: center; padding: 2rem; color: var(--status-negative);">
                    Failed to load players. Make sure the server is running.
                </td>
            </tr>
        `;
    }
}

function renderPlayers(players) {
    const tbody = document.getElementById('players-list');
    tbody.innerHTML = '';
    
    if (players.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                    No players found.
                </td>
            </tr>
        `;
        return;
    }

    players.forEach(p => {
        const perfScore = p.performanceScore ? p.performanceScore.toFixed(1) : 'Data unavailable';
        
        let perfColor = 'var(--status-warning)'; 
        if (p.performanceScore >= 80) perfColor = 'var(--status-positive)';
        else if (p.performanceScore < 50 && p.performanceScore > 0) perfColor = 'var(--status-negative)';
        else if (!p.performanceScore) perfColor = 'var(--text-secondary)';

        const teamDisplay = p.teamId ? (teamsMap[p.teamId] || p.teamId) : '-';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <div style="display: flex; align-items: center; gap: 0.75rem;">
                    <div style="width: 32px; height: 32px; border-radius: 50%; background: var(--bg-card); display: flex; align-items: center; justify-content: center; font-weight: bold; color: var(--accent-primary);">
                        ${p.name.charAt(0)}
                    </div>
                    <div>
                        <div style="font-weight: 600;">${p.name}</div>
                        <div style="font-size: 0.75rem; color: var(--text-secondary);">${p.id}</div>
                    </div>
                </div>
            </td>
            <td>${p.teamId ? `<span style="background: rgba(255,255,255,0.1); padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.8rem;">${teamDisplay}</span>` : '-'}</td>
            <td>${p.position || '-'}</td>
            <td>${p.age || '-'}</td>
            <td>${p.matchesPlayed || 0}</td>
            <td>${p.goals || 0} / ${p.runs || 0}</td>
            <td>
                <span style="display: inline-flex; align-items: center; gap: 0.5rem;">
                    <span class="status-indicator" style="background-color: ${perfColor}; box-shadow: 0 0 8px ${perfColor};"></span>
                    ${perfScore} ${p.performanceScore ? 'pts' : ''}
                </span>
            </td>
            <td style="text-align: right;">
                <button class="btn btn-outline" style="padding: 0.25rem 0.5rem; border-color: var(--status-negative); color: var(--status-negative);" onclick="deletePlayer('${p.id}')">
                    Delete
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function addPlayer() {
    const data = {
        id: document.getElementById('p-id').value,
        name: document.getElementById('p-name').value,
        teamId: document.getElementById('p-team').value,
        position: document.getElementById('p-position').value,
        age: parseInt(document.getElementById('p-age').value) || 0,
        matchesPlayed: 0,
        goals: 0,
        runs: 0
    };

    try {
        const res = await fetch('/api/players', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (res.ok) {
            closeModals();
            document.getElementById('add-player-form').reset();
            loadPlayers();
        } else {
            alert('Failed to add player.');
        }
    } catch (error) {
        console.error('Error adding player:', error);
    }
}

async function deletePlayer(id) {
    if (confirm('Are you sure you want to delete player ' + id + '?')) {
        try {
            await fetch('/api/players/' + id, { method: 'DELETE' });
            loadPlayers();
        } catch (error) {
            console.error('Error deleting player:', error);
        }
    }
}

function openAddPlayerModal() {
    document.getElementById('add-player-modal').classList.add('active');
}

function closeModals() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
}
