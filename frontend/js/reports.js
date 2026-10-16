let reportState = {
    players: [],
    teams: [],
    matches: [],
    events: []
};

document.addEventListener('DOMContentLoaded', () => {
    loadReports();

    const refreshButton = document.getElementById('refresh-reports');
    if (refreshButton) refreshButton.addEventListener('click', loadReports);

    const searchInput = document.getElementById('report-search');
    if (searchInput) {
        searchInput.addEventListener('input', () => renderReports(searchInput.value));
    }
});

async function fetchArray(url) {
    try {
        const response = await fetch(url, { cache: 'no-store' });
        if (!response.ok) throw new Error(`${url} returned ${response.status}`);
        const data = await response.json();
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error(`Report fetch failed for ${url}:`, error);
        return [];
    }
}

async function loadReports() {
    setLoadingState();

    const [players, teams, matches, events] = await Promise.all([
        fetchArray('/api/players'),
        fetchArray('/api/teams'),
        fetchArray('/api/matches'),
        fetchArray('/api/events')
    ]);

    reportState = { players, teams, matches, events };

    setText('report-player-count', players.length);
    setText('report-team-count', teams.length);
    setText('report-match-count', matches.length);
    setText('report-event-count', events.length);

    renderPerformanceSummary();
    const searchInput = document.getElementById('report-search');
    renderReports(searchInput?.value || '');

    if (window.lucide) lucide.createIcons();
}

function setLoadingState() {
    const container = document.getElementById('reports-container');
    if (container) {
        container.innerHTML = '<div class="empty-state"><i data-lucide="activity"></i><p>Loading reports...</p></div>';
    }
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = String(value);
}

function renderPerformanceSummary() {
    const container = document.getElementById('performance-summary');
    if (!container) return;

    const { players, matches, events } = reportState;
    const completedMatches = matches.filter(m => String(m.status || '').toUpperCase() === 'COMPLETED').length;
    const avgPerformance = players.length
        ? players.reduce((sum, p) => sum + (Number(p.performanceScore) || 0), 0) / players.length
        : 0;
    const avgFitness = players.length
        ? players.reduce((sum, p) => sum + (Number(p.fitnessScore) || 0), 0) / players.length
        : 0;

    const topPlayer = [...players].sort((a, b) =>
        (Number(b.performanceScore) || 0) - (Number(a.performanceScore) || 0)
    )[0];

    container.innerHTML = `
        <div style="display:grid; grid-template-columns:repeat(auto-fit,minmax(180px,1fr)); gap:1rem;">
            ${summaryBox('Completed Matches', completedMatches, 'check-circle')}
            ${summaryBox('Avg Performance', avgPerformance.toFixed(1), 'trending-up')}
            ${summaryBox('Avg Fitness', avgFitness.toFixed(1), 'heart-pulse')}
            ${summaryBox('Recorded Events', events.length, 'radio')}
        </div>
        <div style="margin-top:1rem; padding:1rem 1.1rem; border:1px solid var(--border-color); border-radius:10px; background:rgba(0,0,0,.18);">
            <div style="font-size:.75rem; text-transform:uppercase; letter-spacing:.08em; color:var(--text-secondary); margin-bottom:.4rem;">Top Performer</div>
            <div style="font-size:1.05rem; font-weight:700; color:var(--text-primary);">${escapeHtml(topPlayer?.name || 'No player data')}</div>
            ${topPlayer ? `<div style="margin-top:.25rem; color:var(--text-secondary); font-size:.85rem;">Performance ${Number(topPlayer.performanceScore || 0).toFixed(1)} · Fitness ${Number(topPlayer.fitnessScore || 0).toFixed(1)} · ${Number(topPlayer.goals || 0)} goals · ${Number(topPlayer.assists || 0)} assists</div>` : ''}
        </div>`;
}

function summaryBox(label, value, icon) {
    return `
        <div style="padding:1rem; border:1px solid var(--border-color); border-radius:10px; background:rgba(0,0,0,.18);">
            <div style="display:flex; align-items:center; gap:.5rem; color:var(--text-secondary); font-size:.78rem; text-transform:uppercase; letter-spacing:.06em;">
                <i data-lucide="${icon}" style="width:15px; height:15px;"></i>${label}
            </div>
            <div style="font-size:1.5rem; font-weight:800; margin-top:.6rem; color:var(--accent-primary);">${escapeHtml(value)}</div>
        </div>`;
}

function renderReports(searchTerm = '') {
    const container = document.getElementById('reports-container');
    if (!container) return;

    const { players, teams, matches, events } = reportState;
    const query = String(searchTerm || '').trim().toLowerCase();

    if (!matches.length) {
        container.innerHTML = `
            <div class="empty-state" style="padding:2rem;">
                <i data-lucide="file-x"></i>
                <p>No match data is available, so detailed reports cannot be generated yet.</p>
                <a href="/matches.html" class="btn btn-outline" style="margin-top:1rem;">Open Matches</a>
            </div>`;
        if (window.lucide) lucide.createIcons();
        return;
    }

    const reports = matches.map(match => buildMatchReport(match, teams, players, events));
    const filtered = reports.filter(report => !query || report.searchText.includes(query));

    if (!filtered.length) {
        container.innerHTML = '<div class="empty-state" style="padding:2rem;"><i data-lucide="search-x"></i><p>No report matches your search.</p></div>';
        if (window.lucide) lucide.createIcons();
        return;
    }

    container.innerHTML = `<div style="display:grid; grid-template-columns:repeat(auto-fit,minmax(300px,1fr)); gap:1rem;">${filtered.map(reportCard).join('')}</div>`;
    if (window.lucide) lucide.createIcons();
}

function buildMatchReport(match, teams, players, events) {
    const teamA = teams.find(t => t.id === match.teamAId);
    const teamB = teams.find(t => t.id === match.teamBId);
    const matchEvents = events.filter(e => e.matchId === match.id);
    const involvedPlayerIds = new Set(matchEvents.map(e => e.playerId));
    const involvedPlayers = players.filter(p => involvedPlayerIds.has(p.id));

    const topEventPlayer = involvedPlayers
        .map(player => ({
            player,
            eventCount: matchEvents.filter(e => e.playerId === player.id).length
        }))
        .sort((a, b) => b.eventCount - a.eventCount)[0];

    const teamAName = teamA?.name || match.teamAId || 'Team A';
    const teamBName = teamB?.name || match.teamBId || 'Team B';
    const playerNames = involvedPlayers.map(p => p.name).join(' ');

    return {
        match,
        teamAName,
        teamBName,
        matchEvents,
        involvedPlayers,
        topEventPlayer,
        searchText: [
            match.id,
            teamAName,
            teamBName,
            match.venue,
            match.date,
            match.status,
            match.score,
            playerNames
        ].join(' ').toLowerCase()
    };
}

function reportCard(report) {
    const { match, teamAName, teamBName, matchEvents, involvedPlayers, topEventPlayer } = report;
    const status = String(match.status || 'SCHEDULED').toUpperCase();

    return `
        <article style="border:1px solid var(--border-color); border-radius:12px; background:rgba(0,0,0,.2); overflow:hidden;">
            <div style="padding:1rem 1.1rem; border-bottom:1px solid rgba(255,255,255,.06); display:flex; justify-content:space-between; gap:1rem; align-items:flex-start;">
                <div>
                    <div style="font-size:.75rem; color:var(--text-secondary); text-transform:uppercase; letter-spacing:.08em;">Match ${escapeHtml(match.id || 'N/A')}</div>
                    <div style="font-size:1.05rem; font-weight:700; margin-top:.35rem;">${escapeHtml(teamAName)} <span style="color:var(--accent-primary);">vs</span> ${escapeHtml(teamBName)}</div>
                </div>
                <span style="font-size:.72rem; font-weight:700; padding:.35rem .55rem; border-radius:999px; border:1px solid var(--border-color); color:var(--text-secondary);">${escapeHtml(status)}</span>
            </div>

            <div style="padding:1.1rem;">
                <div style="text-align:center; padding:.9rem; border-radius:10px; background:rgba(255,255,255,.03); margin-bottom:1rem;">
                    <div style="font-size:1.55rem; font-weight:800; color:var(--accent-primary);">${escapeHtml(match.score || 'vs')}</div>
                    <div style="font-size:.8rem; color:var(--text-secondary); margin-top:.25rem;">${escapeHtml(match.date || 'Date not set')} · ${escapeHtml(match.venue || 'Venue not set')}</div>
                </div>

                <div style="display:grid; grid-template-columns:1fr 1fr; gap:.75rem; font-size:.85rem;">
                    ${detailBox('Events', matchEvents.length)}
                    ${detailBox('Players Involved', involvedPlayers.length)}
                    ${detailBox('Competition', match.competitionId || 'N/A')}
                    ${detailBox('Most Active', topEventPlayer?.player?.name || 'N/A')}
                </div>

                <div style="margin-top:1rem;">
                    <div style="font-size:.76rem; font-weight:700; color:var(--text-secondary); text-transform:uppercase; letter-spacing:.07em; margin-bottom:.55rem;">Event Details</div>
                    ${renderEventList(matchEvents, involvedPlayers)}
                </div>
            </div>
        </article>`;
}

function detailBox(label, value) {
    return `
        <div style="padding:.75rem; border:1px solid rgba(255,255,255,.06); border-radius:8px; background:rgba(255,255,255,.02);">
            <div style="font-size:.68rem; color:var(--text-secondary); text-transform:uppercase; letter-spacing:.06em;">${label}</div>
            <div style="font-weight:700; margin-top:.25rem; color:var(--text-primary);">${escapeHtml(value)}</div>
        </div>`;
}

function renderEventList(events, involvedPlayers) {
    if (!events.length) {
        return '<div style="color:var(--text-secondary); font-size:.84rem; padding:.8rem; border:1px dashed var(--border-color); border-radius:8px;">No events recorded for this match.</div>';
    }

    return `<div style="display:flex; flex-direction:column; gap:.5rem;">${events.slice(0, 6).map(event => {
        const player = involvedPlayers.find(p => p.id === event.playerId);
        return `
            <div style="display:flex; gap:.75rem; align-items:flex-start; padding:.65rem .7rem; background:rgba(255,255,255,.025); border-radius:7px;">
                <span style="min-width:42px; font-family:monospace; color:var(--accent-primary); font-weight:700;">${escapeHtml(event.timestamp || '--')}</span>
                <div>
                    <div style="font-size:.8rem; font-weight:700;">${escapeHtml(event.eventType || 'EVENT')} · ${escapeHtml(player?.name || event.playerId || 'Unknown')}</div>
                    ${event.description ? `<div style="font-size:.76rem; color:var(--text-secondary); margin-top:.15rem;">${escapeHtml(event.description)}</div>` : ''}
                </div>
            </div>`;
    }).join('')}</div>`;
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
