document.addEventListener('DOMContentLoaded', () => {
    fetchDashboardData();
});

async function fetchJsonSafe(url) {
    try {
        const response = await fetch(url, { cache: 'no-store' });
        if (!response.ok) {
            throw new Error(`${url} returned ${response.status}`);
        }
        const data = await response.json();
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error(`Failed to load ${url}:`, error);
        return [];
    }
}

async function fetchDashboardData() {
    const [players, teams, matches, events] = await Promise.all([
        fetchJsonSafe('/api/players'),
        fetchJsonSafe('/api/teams'),
        fetchJsonSafe('/api/matches'),
        fetchJsonSafe('/api/events')
    ]);

    animateValue('players-count', 0, players.length, 700);
    animateValue('teams-count', 0, teams.length, 700);
    animateValue('matches-count', 0, matches.length, 700);
    animateValue('events-count', 0, events.length, 700);

    renderPerformanceChart(players);
    renderTopPerformers(players, teams);
    renderRecentMatches(matches, teams);
    renderMatchEvents(events, players);

    const loadedAnything = players.length || teams.length || matches.length || events.length;
    if (!loadedAnything) {
        showDashboardWarning();
    }

    if (window.lucide) lucide.createIcons();
}

function showDashboardWarning() {
    const targets = ['recent-matches-container', 'top-performers-container', 'match-events-container'];
    const message = `
        <div class="empty-state" style="padding: 1.5rem;">
            <i data-lucide="alert-circle"></i>
            <p>No data was returned by the SportsPulse APIs.</p>
            <button class="btn btn-outline" style="margin-top: 1rem;" onclick="fetchDashboardData()">Retry</button>
        </div>`;

    targets.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.innerHTML = message;
    });
}

function animateValue(id, start, end, duration) {
    const obj = document.getElementById(id);
    if (!obj) return;

    obj.textContent = String(start);
    let startTimestamp = null;

    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        obj.textContent = String(Math.floor(progress * (end - start) + start));
        if (progress < 1) {
            window.requestAnimationFrame(step);
        } else {
            obj.textContent = String(end);
        }
    };

    window.requestAnimationFrame(step);
}

function renderPerformanceChart(players) {
    const chartContainer = document.getElementById('performance-chart');
    if (!chartContainer) return;

    if (!players.length) {
        chartContainer.innerHTML = '<div style="color: var(--text-secondary); width:100%; text-align:center; padding:2rem 0;">No performance data available.</div>';
        return;
    }

    const buckets = [0, 0, 0, 0, 0];
    const labels = ['0–20', '21–40', '41–60', '61–80', '81–100'];

    players.forEach(player => {
        const score = Number(player.performanceScore) || 0;
        if (score <= 20) buckets[0]++;
        else if (score <= 40) buckets[1]++;
        else if (score <= 60) buckets[2]++;
        else if (score <= 80) buckets[3]++;
        else buckets[4]++;
    });

    const maxVal = Math.max(...buckets, 1);
    chartContainer.innerHTML = '';

    buckets.forEach((count, index) => {
        const bar = document.createElement('div');
        bar.className = 'css-bar';
        bar.style.height = '0%';
        bar.style.flex = '1';
        bar.style.margin = '0 5%';
        bar.style.background = 'var(--accent-primary)';
        bar.style.borderRadius = '4px 4px 0 0';
        bar.style.position = 'relative';
        bar.style.transition = 'height 0.8s ease-out';

        bar.innerHTML = `
            <span style="position:absolute; top:-20px; left:50%; transform:translateX(-50%); font-size:.8rem; font-weight:600;">${count}</span>
            <div style="position:absolute; bottom:-25px; left:50%; transform:translateX(-50%); font-size:.75rem; color:var(--text-secondary); white-space:nowrap;">${labels[index]}</div>`;

        chartContainer.appendChild(bar);
        setTimeout(() => {
            bar.style.height = `${Math.max(2, (count / maxVal) * 100)}%`;
        }, 60 + (index * 80));
    });
}

function renderTopPerformers(players, teams) {
    const container = document.getElementById('top-performers-container');
    if (!container) return;

    if (!players.length) {
        container.innerHTML = '<div class="empty-state" style="padding:1.5rem;"><p>No players available.</p></div>';
        return;
    }

    const sorted = [...players]
        .sort((a, b) => (Number(b.performanceScore) || 0) - (Number(a.performanceScore) || 0))
        .slice(0, 4);

    container.innerHTML = `
        <div style="display:flex; flex-direction:column; gap:1rem;">
            ${sorted.map((player, index) => {
                const team = teams.find(t => t.id === player.teamId);
                const score = Number(player.performanceScore) || 0;
                return `
                    <div style="display:flex; align-items:center; justify-content:space-between; padding:1rem; background:rgba(0,0,0,.2); border-radius:8px; border:1px solid rgba(255,255,255,.05);">
                        <div style="display:flex; align-items:center; gap:1rem; min-width:0;">
                            <div style="width:30px; height:30px; border-radius:50%; background:rgba(255,255,255,.05); display:flex; align-items:center; justify-content:center; font-weight:700; font-family:monospace;">${String(index + 1).padStart(2, '0')}</div>
                            <div style="min-width:0;">
                                <div style="font-weight:600; color:var(--text-primary);">${escapeHtml(player.name || player.id || 'Unknown player')}</div>
                                <div style="font-size:.8rem; color:var(--text-secondary); margin-top:.2rem;">${escapeHtml(team?.name || player.teamId || 'No team')}</div>
                            </div>
                        </div>
                        <div style="text-align:right;">
                            <div style="font-weight:700; color:var(--accent-primary); font-size:1.1rem;">${score.toFixed(1)}</div>
                            <div style="font-size:.75rem; color:var(--text-secondary);">Score</div>
                        </div>
                    </div>`;
            }).join('')}
        </div>`;
}

function renderRecentMatches(matches, teams) {
    const container = document.getElementById('recent-matches-container');
    if (!container) return;

    if (!matches.length) {
        container.innerHTML = '<div class="empty-state" style="padding:1.5rem;"><p>No matches available.</p></div>';
        return;
    }

    const recent = [...matches]
        .sort((a, b) => String(b.date || '').localeCompare(String(a.date || '')))
        .slice(0, 4);

    container.innerHTML = `
        <div style="display:flex; flex-direction:column; gap:1rem;">
            ${recent.map(match => {
                const teamA = teams.find(t => t.id === match.teamAId);
                const teamB = teams.find(t => t.id === match.teamBId);
                return `
                    <div style="border:1px solid var(--border-color); border-radius:var(--border-radius-sm); padding:1.25rem; background:rgba(0,0,0,.2);">
                        <div style="display:flex; justify-content:space-between; gap:1rem; margin-bottom:1rem; border-bottom:1px solid rgba(255,255,255,.05); padding-bottom:.5rem;">
                            <span style="font-size:.75rem; color:var(--text-secondary); font-weight:600; letter-spacing:1px; text-transform:uppercase;">${escapeHtml(match.status || 'SCHEDULED')}</span>
                            <span style="font-size:.75rem; color:var(--text-secondary);">${escapeHtml(match.date || 'TBD')}</span>
                        </div>
                        <div class="match-score">
                            <div style="flex:1; text-align:right;">${escapeHtml(teamA?.name || match.teamAId || 'TBA')}</div>
                            <div style="padding:0 1rem; color:var(--accent-primary); letter-spacing:2px;">${escapeHtml(match.score || 'vs')}</div>
                            <div style="flex:1; text-align:left;">${escapeHtml(teamB?.name || match.teamBId || 'TBA')}</div>
                        </div>
                        <div style="margin-top:.75rem; text-align:center; font-size:.8rem; color:var(--text-secondary);">${escapeHtml(match.venue || 'Venue not set')}</div>
                    </div>`;
            }).join('')}
        </div>`;
}

function renderMatchEvents(events, players) {
    const container = document.getElementById('match-events-container');
    if (!container) return;

    if (!events.length) {
        container.innerHTML = '<div style="padding:1.5rem; text-align:center;"><p style="color:var(--text-secondary); font-size:.9rem;">No match events recorded.</p></div>';
        return;
    }

    const recentEvents = events.slice(-5).reverse();
    container.innerHTML = `
        <div style="display:flex; flex-direction:column; gap:.75rem;">
            ${recentEvents.map(event => {
                const player = players.find(p => p.id === event.playerId);
                return `
                    <div style="display:flex; align-items:flex-start; gap:1rem; padding:.75rem; background:rgba(0,0,0,.2); border-radius:6px; border-left:3px solid var(--accent-primary);">
                        <div style="min-width:45px; font-family:monospace; font-size:.8rem; color:var(--text-secondary); font-weight:600;">${escapeHtml(event.timestamp || '--')}</div>
                        <div style="flex:1; text-align:left;">
                            <div style="font-size:.75rem; color:var(--accent-primary); font-weight:700; letter-spacing:1px; text-transform:uppercase;">● ${escapeHtml(event.eventType || 'EVENT')}</div>
                            <div style="font-weight:600; font-size:.9rem; margin-top:.2rem;">${escapeHtml(player?.name || event.playerId || 'Unknown')}</div>
                            ${event.description ? `<div style="font-size:.8rem; color:var(--text-secondary); margin-top:.1rem;">${escapeHtml(event.description)}</div>` : ''}
                        </div>
                    </div>`;
            }).join('')}
        </div>`;
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
