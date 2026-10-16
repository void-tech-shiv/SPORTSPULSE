const moduleConfig = {
    m1: {
        title: 'M1 · String Algorithms',
        description: 'KMP and Rabin–Karp string matching on the same commentary sample used by the CLI.',
        fields: [
            { id: 'algo-pattern', label: 'Search pattern', value: 'Mbappe', placeholder: 'Mbappe' }
        ]
    },
    m2: {
        title: 'M2 · Suffix Structures',
        description: 'Builds a suffix array, LCP array, suffix-array search and longest repeated substring.',
        fields: [
            { id: 'algo-pattern', label: 'Report search pattern', value: 'goal', placeholder: 'goal' }
        ]
    },
    m3: {
        title: 'M3 · Dynamic Programming',
        description: 'Runs LIS, LCS and the fantasy-team 0/1 Knapsack demonstration from the CLI.',
        fields: [
            { id: 'algo-budget', label: 'Fantasy budget', value: '50', placeholder: '50', type: 'number' }
        ]
    },
    m4: {
        title: 'M4 · Network Flow',
        description: 'Runs Dinic maximum flow and player-position bipartite matching.'
    },
    m5: {
        title: 'M5 · NP-Completeness',
        description: 'Runs the CLI 3-SAT solver plus exact and approximate Vertex Cover.'
    },
    m6: {
        title: 'M6 · Randomized / Parallel / Heuristics',
        description: 'Runs the project’s Simulated Annealing roster optimizer and Genetic Algorithm scheduler.'
    }
};

window.currentModule = '';

function openModule(moduleId) {
    const config = moduleConfig[moduleId];
    if (!config) return;

    window.currentModule = moduleId;
    const dashboard = document.getElementById('lab-dashboard');
    const detail = document.getElementById('module-detail');
    if (dashboard) dashboard.style.display = 'none';
    if (detail) detail.classList.add('active');

    document.getElementById('module-title').textContent = config.title;
    document.getElementById('module-description').textContent = config.description;

    const inputs = document.getElementById('module-inputs');
    inputs.innerHTML = '';
    (config.fields || []).forEach(field => {
        const wrapper = document.createElement('label');
        wrapper.style.display = 'block';
        wrapper.style.marginBottom = '1rem';
        wrapper.innerHTML = `
            <span style="display:block;margin-bottom:.45rem;color:var(--text-secondary);font-size:.82rem;font-weight:600;">${field.label}</span>
            <input id="${field.id}" type="${field.type || 'text'}" value="${field.value || ''}" placeholder="${field.placeholder || ''}" class="form-control" style="width:100%;">
        `;
        inputs.appendChild(wrapper);
    });

    const results = document.getElementById('algorithm-results');
    results.style.display = 'none';
    document.getElementById('algorithm-output').textContent = '';

    if (window.lucide) lucide.createIcons();
}

function closeModules() {
    window.currentModule = '';
    const dashboard = document.getElementById('lab-dashboard');
    const detail = document.getElementById('module-detail');
    if (detail) detail.classList.remove('active');
    if (dashboard) dashboard.style.display = 'block';
}

async function executeCurrentModule() {
    if (!window.currentModule) return;

    const runButton = document.getElementById('run-module-button');
    const results = document.getElementById('algorithm-results');
    const output = document.getElementById('algorithm-output');

    const params = new URLSearchParams({ module: window.currentModule });
    const pattern = document.getElementById('algo-pattern');
    const budget = document.getElementById('algo-budget');
    if (pattern) params.set('pattern', pattern.value.trim() || (window.currentModule === 'm1' ? 'Mbappe' : 'goal'));
    if (budget) params.set('budget', budget.value || '50');

    results.style.display = 'block';
    output.textContent = '[SYSTEM] Executing Java backend...';
    if (runButton) runButton.disabled = true;

    try {
        const response = await fetch('/api/algorithms/run?' + params.toString(), { cache: 'no-store' });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || ('Server returned ' + response.status));

        output.textContent = [
            '[SPORTSPULSE ALGORITHM LAB]',
            data.title || window.currentModule.toUpperCase(),
            '',
            data.logs || 'No output returned.',
            '',
            `[SYSTEM] Java execution time: ${data.timeMs ?? 0} ms`
        ].join('\n');
    } catch (error) {
        console.error('Algorithm execution error:', error);
        output.textContent = '[ERROR] ' + error.message + '\n\nMake sure the Java web server is running from the updated branch.';
    } finally {
        if (runButton) runButton.disabled = false;
    }
}

async function executeDatasetSearch() {
    const input = document.getElementById('dataset-search-input');
    const container = document.getElementById('dataset-search-results');
    if (!input || !container) return;

    const query = input.value.trim();
    if (!query) {
        container.innerHTML = '<div class="empty-state"><p>Enter a player, team, venue, date or status.</p></div>';
        return;
    }

    container.innerHTML = '<div class="empty-state"><p>Searching with backend KMP...</p></div>';
    try {
        const response = await fetch('/api/algorithms/search?q=' + encodeURIComponent(query), { cache: 'no-store' });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || 'Search failed');

        const players = data.players || [];
        const teams = data.teams || [];
        const matches = data.matches || [];

        if (!players.length && !teams.length && !matches.length) {
            container.innerHTML = '<div class="empty-state"><p>No KMP matches found.</p></div>';
            return;
        }

        const rows = [];
        players.forEach(p => rows.push(`<div class="search-result-item"><strong>Player:</strong>&nbsp; ${p.name} · ${p.position} · ${p.teamId}</div>`));
        teams.forEach(t => rows.push(`<div class="search-result-item"><strong>Team:</strong>&nbsp; ${t.name} · ${t.sport} · Coach ${t.coach}</div>`));
        matches.forEach(m => rows.push(`<div class="search-result-item"><strong>Match:</strong>&nbsp; ${m.id} · ${m.teamAId} vs ${m.teamBId} · ${m.date} · ${m.venue}</div>`));
        container.innerHTML = rows.join('');
    } catch (error) {
        container.innerHTML = `<div class="empty-state"><p style="color:var(--status-negative);">${error.message}</p></div>`;
    }
}
