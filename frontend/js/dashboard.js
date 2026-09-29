document.addEventListener('DOMContentLoaded', () => {
    fetchDashboardData();
});

async function fetchDashboardData() {
    try {
        const [playersRes, teamsRes, matchesRes, eventsRes] = await Promise.all([
            fetch('/api/players'),
            fetch('/api/teams'),
            fetch('/api/matches'),
            fetch('/api/events')
        ]);
        
        if (!playersRes.ok || !teamsRes.ok || !matchesRes.ok || !eventsRes.ok) {
            throw new Error('Failed to fetch API data');
        }

        const players = await playersRes.json();
        const teams = await teamsRes.json();
        const matches = await matchesRes.json();
        
        let events = [];
        try {
            events = await eventsRes.json();
        } catch(e) {
            console.warn('Could not parse events JSON', e);
        }
        
        // Update KPIs
        animateValue('players-count', 0, players.length, 1000);
        animateValue('teams-count', 0, teams.length, 1000);
        animateValue('matches-count', 0, matches.length, 1000);
        animateValue('events-count', 0, events.length, 1000);

        // Render Performance Chart
        renderPerformanceChart(players);

        // Render Top Performers
        renderTopPerformers(players, teams);

        // Render Recent Matches
        renderRecentMatches(matches, teams);
        
        // Render Match Events
        renderMatchEvents(events, players);

    } catch (error) {
        console.error('Error fetching dashboard data:', error);
        
        document.getElementById('players-count').innerHTML = '<span class="text-negative">Error</span>';
        document.getElementById('teams-count').innerHTML = '<span class="text-negative">Error</span>';
        document.getElementById('matches-count').innerHTML = '<span class="text-negative">Error</span>';
        document.getElementById('events-count').innerHTML = '<span class="text-negative">Error</span>';
        
        const errorStateHtml = `
            <div class="empty-state">
                <i data-lucide="alert-circle" style="color: var(--status-negative);"></i>
                <p>Unable to load data. The SportsPulse server may be unavailable.</p>
                <button class="btn btn-outline" style="margin-top: 1rem;" onclick="fetchDashboardData()">Retry</button>
            </div>
        `;
        document.getElementById('recent-matches-container').innerHTML = errorStateHtml;
        document.getElementById('top-performers-container').innerHTML = errorStateHtml;
        const matchEventsContainer = document.getElementById('match-events-container');
        if (matchEventsContainer) matchEventsContainer.innerHTML = errorStateHtml;
        
        if (window.lucide) {
            lucide.createIcons();
        }
    }
}

function animateValue(id, start, end, duration) {
    const obj = document.getElementById(id);
    if (!obj) return;
    
    if (obj.querySelector('.skeleton')) {
        obj.innerHTML = '0';
    }
    
    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        obj.innerHTML = Math.floor(progress * (end - start) + start);
        if (progress < 1) {
            window.requestAnimationFrame(step);
        } else {
            obj.innerHTML = end;
        }
    };
    window.requestAnimationFrame(step);
}

function renderPerformanceChart(players) {
    const chartContainer = document.getElementById('performance-chart');
    if (!chartContainer) return;
    
    if (!players || players.length === 0) {
        chartContainer.innerHTML = `
            <div style="color: var(--text-secondary); width: 100%; text-align: center; padding: 2rem 0;">
                <p>No performance data available.</p>
            </div>
        `;
        return;
    }

    const buckets = [0, 0, 0, 0, 0];
    const labels = ['0–20', '21–40', '41–60', '61–80', '81–100'];
    
    players.forEach(p => {
        const score = p.performanceScore || 0;
        if (score <= 20) buckets[0]++;
        else if (score <= 40) buckets[1]++;
        else if (score <= 60) buckets[2]++;
        else if (score <= 80) buckets[3]++;
        else buckets[4]++;
    });

    const maxVal = Math.max(...buckets, 1);
    
    chartContainer.innerHTML = '';
    
    buckets.forEach((count, i) => {
        const heightPct = (count / maxVal) * 100;
        const bar = document.createElement('div');
        bar.className = 'css-bar';
        bar.style.height = '0%';
        bar.style.flex = '1';
        bar.style.margin = '0 5%';
        bar.style.background = 'var(--accent-primary)';
        bar.style.borderRadius = '4px 4px 0 0';
        bar.style.position = 'relative';
        bar.style.transition = 'height 1s ease-out';
        
        const label = document.createElement('span');
        label.textContent = count;
        label.style.position = 'absolute';
        label.style.top = '-20px';
        label.style.left = '50%';
        label.style.transform = 'translateX(-50%)';
        label.style.fontSize = '0.8rem';
        label.style.fontWeight = '600';
        label.style.color = 'var(--text-primary)';
        bar.appendChild(label);
        
        const xAxisLabel = document.createElement('div');
        xAxisLabel.style.position = 'absolute';
        xAxisLabel.style.bottom = '-25px';
        xAxisLabel.style.left = '50%';
        xAxisLabel.style.transform = 'translateX(-50%)';
        xAxisLabel.style.fontSize = '0.75rem';
        xAxisLabel.style.color = 'var(--text-secondary)';
        xAxisLabel.style.whiteSpace = 'nowrap';
        xAxisLabel.textContent = labels[i];
        bar.appendChild(xAxisLabel);
        
        chartContainer.appendChild(bar);
        
        setTimeout(() => {
            bar.style.height = \`\${Math.max(2, heightPct)}%\`;
        }, 50 + (i * 100));
    });
}

function renderTopPerformers(players, teams) {
    const container = document.getElementById('top-performers-container');
    if (!container) return;
    
    if (!players || players.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="padding: 1.5rem;">
                <p>Unable to load players.</p>
                <button class="btn btn-outline" style="margin-top: 1rem;" onclick="fetchDashboardData()">Retry</button>
            </div>
        `;
        return;
    }

    const validPlayers = players.filter(p => p.performanceScore > 0);
    
    if (!validPlayers || validPlayers.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="padding: 1.5rem;">
                <p>Performance data unavailable</p>
            </div>
        `;
        return;
    }

    const sorted = [...validPlayers].sort((a, b) => {
        const scoreA = a.performanceScore || 0;
        const scoreB = b.performanceScore || 0;
        if (scoreB !== scoreA) return scoreB - scoreA;
        return (b.goals || 0) - (a.goals || 0); 
    }).slice(0, 4);

    let html = '<div style="display: flex; flex-direction: column; gap: 1rem;">';
    
    sorted.forEach((p, index) => {
        const team = teams.find(t => t.id === p.teamId) || { name: 'Unknown Team' };
        const score = p.performanceScore.toFixed(1);
        const rankColor = index === 0 ? 'var(--status-warning)' : 'var(--text-secondary)';
        const displayIndex = String(index + 1).padStart(2, '0');
        
        html += `
            <div style="display: flex; align-items: center; justify-content: space-between; padding: 1rem; background: rgba(0,0,0,0.2); border-radius: 8px; border: 1px solid rgba(255,255,255,0.05);">
                <div style="display: flex; align-items: center; gap: 1rem;">
                    <div style="width: 30px; height: 30px; border-radius: 50%; background: rgba(255,255,255,0.05); display: flex; align-items: center; justify-content: center; color: ${rankColor}; font-weight: 700; font-family: monospace; font-size: 0.9rem;">
                        ${displayIndex}
                    </div>
                    <div>
                        <div style="font-weight: 600; color: var(--text-primary); font-size: 1rem;">${p.name}</div>
                        <div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 0.2rem;">${team.name}</div>
                    </div>
                </div>
                <div style="text-align: right;">
                    <div style="font-weight: 700; color: var(--accent-primary); font-size: 1.1rem;">${score}</div>
                    <div style="font-size: 0.75rem; color: var(--text-secondary);">Score</div>
                </div>
            </div>
        `;
    });
    
    html += '</div>';
    container.innerHTML = html;
}

function renderRecentMatches(matches, teams) {
    const container = document.getElementById('recent-matches-container');
    if (!container) return;
    
    if (!matches || matches.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="padding: 1.5rem;">
                <p>No matches available</p>
            </div>
        `;
        return;
    }

    // Sort matches by date if possible, otherwise use as-is
    const sorted = [...matches].sort((a, b) => {
        if (!a.date) return 1;
        if (!b.date) return -1;
        return new Date(b.date) - new Date(a.date);
    });

    const recent = sorted.slice(0, 3);
    
    let html = '<div style="display: flex; flex-direction: column; gap: 1rem;">';
    
    recent.forEach(m => {
        const teamA = teams.find(t => t.id === m.teamAId) || { name: m.teamAId || 'TBA' };
        const teamB = teams.find(t => t.id === m.teamBId) || { name: m.teamBId || 'TBA' };
        
        html += `
            <div style="border: 1px solid var(--border-color); border-radius: var(--border-radius-sm); padding: 1.25rem; background: rgba(0,0,0,0.2);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; border-bottom: 1px solid rgba(255,255,255,0.05); padding-bottom: 0.5rem;">
                    <span style="font-size: 0.75rem; color: var(--text-secondary); font-weight: 600; letter-spacing: 1px; text-transform: uppercase;">
                        ${m.status === 'COMPLETED' ? 'FINAL' : m.status || 'SCHEDULED'}
                    </span>
                    <span style="font-size: 0.75rem; color: var(--text-secondary);">
                        ${m.date || 'TBD'}
                    </span>
                </div>
                
                <div class="match-score">
                    <div style="flex: 1; text-align: right; color: var(--text-primary); font-family: 'Inter', sans-serif; font-size: 1rem;">
                        ${teamA.name}
                    </div>
                    <div style="padding: 0 1rem; color: var(--accent-primary); letter-spacing: 2px;">
                        ${m.score || 'vs'}
                    </div>
                    <div style="flex: 1; text-align: left; color: var(--text-primary); font-family: 'Inter', sans-serif; font-size: 1rem;">
                        ${teamB.name}
                    </div>
                </div>
            </div>
        `;
    });
    
    html += '</div>';
    container.innerHTML = html;
}

function renderMatchEvents(events, players) {
    const container = document.getElementById('match-events-container');
    if (!container) return;
    
    if (!events || events.length === 0) {
        container.innerHTML = `
            <div style="padding: 1.5rem; background: rgba(0,0,0,0.2); border-radius: 8px; border: 1px dashed rgba(255,255,255,0.1); text-align: center;">
                <p style="color: var(--text-secondary); font-size: 0.9rem;">No match events recorded.</p>
            </div>
        `;
        return;
    }
    
    const recentEvents = events.slice(-5).reverse();
    let html = '<div style="display: flex; flex-direction: column; gap: 0.75rem;">';
    
    recentEvents.forEach(e => {
        const player = players.find(p => p.id === e.playerId) || { name: e.playerId || 'Unknown' };
        html += `
            <div style="display: flex; align-items: flex-start; gap: 1rem; padding: 0.75rem; background: rgba(0,0,0,0.2); border-radius: 6px; border-left: 3px solid var(--accent-primary);">
                <div style="min-width: 45px; font-family: monospace; font-size: 0.8rem; color: var(--text-secondary); font-weight: 600;">
                    ${e.timestamp || '00:00'}
                </div>
                <div style="flex: 1; text-align: left;">
                    <div style="font-size: 0.75rem; color: var(--accent-primary); font-weight: 700; letter-spacing: 1px; text-transform: uppercase;">
                        ● ${e.eventType || 'EVENT'}
                    </div>
                    <div style="font-weight: 600; font-size: 0.9rem; margin-top: 0.2rem;">${player.name}</div>
                    ${e.description ? \`<div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 0.1rem;">\${e.description}</div>\` : ''}
                </div>
            </div>
        `;
    });
    
    html += '</div>';
    container.innerHTML = html;
}
