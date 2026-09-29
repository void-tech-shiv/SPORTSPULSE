async function executeKMP() {
    const query = document.getElementById('search-input').value;
    if (!query) {
        alert('Please enter a search term');
        return;
    }

    try {
        const response = await fetch('/api/algorithms/search?q=' + encodeURIComponent(query));
        const results = await response.json();
        
        const container = document.getElementById('results-container');
        const list = document.getElementById('search-results');
        
        container.style.display = 'block';
        list.innerHTML = '';
        
        const players = results.players || [];
        const teams = results.teams || [];
        const matches = results.matches || [];
        
        if (players.length === 0 && teams.length === 0 && matches.length === 0) {
            list.innerHTML = `
                <li style="padding: 1.5rem; background: rgba(255,255,255,0.02); border-radius: 8px; justify-content: center; color: var(--text-muted);">
                    <i data-lucide="info" style="margin-right: 0.5rem; width: 18px;"></i> No results found matching your query.
                </li>
            `;
            lucide.createIcons();
            return;
        }
        
        players.forEach(p => {
            const li = document.createElement('li');
            li.innerHTML = `
                <div style="display: flex; align-items: center; gap: 1rem; width: 100%;">
                    <div style="width: 40px; height: 40px; border-radius: 50%; background: var(--bg-hover); display: flex; align-items: center; justify-content: center; color: var(--accent-primary);">
                        <i data-lucide="user"></i>
                    </div>
                    <div style="flex: 1;">
                        <h4 style="margin: 0; font-size: 1rem; color: var(--text-primary);">${p.name}</h4>
                        <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">
                            Position: ${p.position} &nbsp;&bull;&nbsp; Team ID: ${p.teamId}
                        </div>
                    </div>
                    <div>
                        <span class="algo-badge" style="background: rgba(220, 255, 7, 0.1); color: var(--accent-primary); border-color: rgba(220, 255, 7, 0.2);">
                            Match Found
                        </span>
                    </div>
                </div>
            `;
            list.appendChild(li);
        });
        
        teams.forEach(t => {
            const li = document.createElement('li');
            li.innerHTML = `
                <div style="display: flex; align-items: center; gap: 1rem; width: 100%;">
                    <div style="width: 40px; height: 40px; border-radius: 50%; background: var(--bg-hover); display: flex; align-items: center; justify-content: center; color: var(--accent-primary);">
                        <i data-lucide="shield"></i>
                    </div>
                    <div style="flex: 1;">
                        <h4 style="margin: 0; font-size: 1rem; color: var(--text-primary);">${t.name}</h4>
                        <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">
                            Coach: ${t.coach} &nbsp;&bull;&nbsp; Team ID: ${t.id}
                        </div>
                    </div>
                    <div>
                        <span class="algo-badge" style="background: rgba(220, 255, 7, 0.1); color: var(--accent-primary); border-color: rgba(220, 255, 7, 0.2);">
                            Match Found
                        </span>
                    </div>
                </div>
            `;
            list.appendChild(li);
        });

        matches.forEach(m => {
            const li = document.createElement('li');
            li.innerHTML = `
                <div style="display: flex; align-items: center; gap: 1rem; width: 100%;">
                    <div style="width: 40px; height: 40px; border-radius: 50%; background: var(--bg-hover); display: flex; align-items: center; justify-content: center; color: var(--accent-primary);">
                        <i data-lucide="swords"></i>
                    </div>
                    <div style="flex: 1;">
                        <h4 style="margin: 0; font-size: 1rem; color: var(--text-primary);">${m.teamAId} vs ${m.teamBId}</h4>
                        <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">
                            Venue: ${m.venue} &nbsp;&bull;&nbsp; Match ID: ${m.id}
                        </div>
                    </div>
                    <div>
                        <span class="algo-badge" style="background: rgba(220, 255, 7, 0.1); color: var(--accent-primary); border-color: rgba(220, 255, 7, 0.2);">
                            Match Found
                        </span>
                    </div>
                </div>
            `;
            list.appendChild(li);
        });
        
        lucide.createIcons();
    } catch (error) {
        console.error('Error executing search:', error);
        alert('Error performing search. Check console.');
    }
}

async function executeGenericAlgo() {
    if (!window.currentModule) return;
    
    document.getElementById('generic-results').style.display = 'block';
    const output = document.getElementById('generic-output');
    output.innerHTML = 'Initializing simulation...\n';
    
    try {
        const response = await fetch('/api/algorithms/simulate?module=' + window.currentModule);
        if (!response.ok) {
            throw new Error('Server returned ' + response.status);
        }
        const data = await response.json();
        
        let logs = `[SYSTEM] Loaded module: ${window.currentModule}\n`;
        logs += `[SYSTEM] Execution started at ${new Date().toISOString()}\n\n`;
        logs += data.logs || 'No output received from backend.';
        logs += `\n\n[SYSTEM] Execution finished in ${data.timeMs || 0}ms.\n`;
        
        output.innerHTML = logs;
    } catch (error) {
        output.innerHTML += `\n[ERROR] Execution failed: ${error.message}`;
    }
}
