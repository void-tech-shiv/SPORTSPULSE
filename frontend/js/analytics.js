document.addEventListener('DOMContentLoaded', () => {
    loadTopScorers();

    const refreshButton = document.getElementById('refresh-reports');
    if (refreshButton) {
        refreshButton.addEventListener('click', loadTopScorers);
    }
});

async function loadTopScorers() {
    const container = document.getElementById('top-scorers-container');
    if (!container) return;

    container.innerHTML = '<div class="empty-state"><p>Loading CLI analytics...</p></div>';

    try {
        const response = await fetch('/api/analytics/top-scorers', { cache: 'no-store' });
        const players = await response.json();
        if (!response.ok) throw new Error(players.error || 'Failed to load analytics');

        if (!Array.isArray(players) || !players.length) {
            container.innerHTML = '<div class="empty-state"><p>No scorer data available.</p></div>';
            return;
        }

        container.innerHTML = players.map((player, index) => `
            <div style="display:flex;align-items:center;justify-content:space-between;gap:1rem;padding:.85rem 1rem;border:1px solid rgba(255,255,255,.06);border-radius:9px;background:rgba(255,255,255,.02);margin-bottom:.6rem;">
                <div style="display:flex;align-items:center;gap:.85rem;">
                    <div style="width:30px;height:30px;border-radius:50%;display:flex;align-items:center;justify-content:center;background:rgba(255,255,255,.05);color:var(--accent-primary);font-weight:800;">${index + 1}</div>
                    <div>
                        <div style="font-weight:700;">${player.name}</div>
                        <div style="font-size:.76rem;color:var(--text-secondary);">${player.teamId || 'No team'} · ${player.position || 'No position'}</div>
                    </div>
                </div>
                <div style="text-align:right;">
                    <div style="font-size:1.1rem;font-weight:800;color:var(--accent-primary);">${player.goals || 0}</div>
                    <div style="font-size:.7rem;color:var(--text-secondary);text-transform:uppercase;">Goals</div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Top scorers analytics error:', error);
        container.innerHTML = `<div class="empty-state"><p style="color:var(--status-negative);">${error.message}</p></div>`;
    }
}
