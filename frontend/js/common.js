document.addEventListener('DOMContentLoaded', () => {
    // Menu toggle logic for responsive layout
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', (e) => {
            e.stopPropagation();
            sidebar.classList.toggle('open');
        });
        
        // Close sidebar on click outside
        document.addEventListener('click', (e) => {
            if (window.innerWidth <= 768 && sidebar.classList.contains('open')) {
                if (!sidebar.contains(e.target)) {
                    sidebar.classList.remove('open');
                }
            }
        });
    }

    // Global Search Logic
    const globalSearchInput = document.getElementById('global-search-input');
    const globalSearchResults = document.getElementById('global-search-results');
    
    if (globalSearchInput && globalSearchResults) {
        let debounceTimer;
        
        // Support ESC to close
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                globalSearchResults.style.display = 'none';
                globalSearchInput.blur();
            }
        });

        globalSearchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            const query = e.target.value.trim();
            
            if (!query) {
                globalSearchResults.style.display = 'none';
                return;
            }

            debounceTimer = setTimeout(async () => {
                try {
                    const response = await fetch('/api/algorithms/search?q=' + encodeURIComponent(query));
                    if (!response.ok) throw new Error('Search failed');
                    const results = await response.json();
                    
                    globalSearchResults.innerHTML = '';
                    
                    const players = results.players || [];
                    const teams = results.teams || [];
                    const matches = results.matches || [];
                    
                    if (players.length === 0 && teams.length === 0 && matches.length === 0) {
                        globalSearchResults.innerHTML = '<div style="padding: 1rem; color: var(--text-muted); text-align: center;"><i data-lucide="info" style="width: 16px; margin-right: 8px;"></i>No results found<br><span style="font-size: 0.8rem; margin-top: 4px; display: block;">Try another search term.</span></div>';
                    } else {
                        if (players.length > 0) {
                            const pSection = document.createElement('div');
                            pSection.innerHTML = '<div style="padding: 0.5rem 1rem; font-size: 0.75rem; font-weight: 600; color: var(--text-muted); letter-spacing: 0.05em; text-transform: uppercase;">Players</div>';
                            players.slice(0, 3).forEach(p => {
                                const item = document.createElement('a');
                                item.href = '/players.html'; 
                                item.className = 'search-result-item';
                                item.innerHTML = `
                                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                                        <div style="background: rgba(255,255,255,0.05); padding: 0.5rem; border-radius: 50%; color: var(--accent-primary);">
                                            <i data-lucide="user" style="width: 16px; height: 16px;"></i>
                                        </div>
                                        <div>
                                            <div style="font-weight: 600; color: var(--text-primary); font-size: 0.9rem;">${p.name}</div>
                                            <div style="font-size: 0.75rem; color: var(--text-muted);">${p.teamId}</div>
                                        </div>
                                    </div>
                                `;
                                pSection.appendChild(item);
                            });
                            globalSearchResults.appendChild(pSection);
                        }
                        
                        if (teams.length > 0) {
                            const tSection = document.createElement('div');
                            tSection.innerHTML = '<div style="padding: 0.5rem 1rem; font-size: 0.75rem; font-weight: 600; color: var(--text-muted); letter-spacing: 0.05em; text-transform: uppercase; border-top: 1px solid rgba(255,255,255,0.05);">Teams</div>';
                            teams.slice(0, 3).forEach(t => {
                                const item = document.createElement('a');
                                item.href = '/teams.html'; 
                                item.className = 'search-result-item';
                                item.innerHTML = `
                                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                                        <div style="background: rgba(255,255,255,0.05); padding: 0.5rem; border-radius: 50%; color: var(--accent-primary);">
                                            <i data-lucide="shield" style="width: 16px; height: 16px;"></i>
                                        </div>
                                        <div>
                                            <div style="font-weight: 600; color: var(--text-primary); font-size: 0.9rem;">${t.name}</div>
                                            <div style="font-size: 0.75rem; color: var(--text-muted);">${t.coach}</div>
                                        </div>
                                    </div>
                                `;
                                tSection.appendChild(item);
                            });
                            globalSearchResults.appendChild(tSection);
                        }
                        
                        if (matches.length > 0) {
                            const mSection = document.createElement('div');
                            mSection.innerHTML = '<div style="padding: 0.5rem 1rem; font-size: 0.75rem; font-weight: 600; color: var(--text-muted); letter-spacing: 0.05em; text-transform: uppercase; border-top: 1px solid rgba(255,255,255,0.05);">Matches</div>';
                            matches.slice(0, 3).forEach(m => {
                                const item = document.createElement('a');
                                item.href = '/matches.html'; 
                                item.className = 'search-result-item';
                                item.innerHTML = `
                                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                                        <div style="background: rgba(255,255,255,0.05); padding: 0.5rem; border-radius: 50%; color: var(--accent-primary);">
                                            <i data-lucide="swords" style="width: 16px; height: 16px;"></i>
                                        </div>
                                        <div>
                                            <div style="font-weight: 600; color: var(--text-primary); font-size: 0.9rem;">${m.teamAId} vs ${m.teamBId}</div>
                                            <div style="font-size: 0.75rem; color: var(--text-muted);">${m.venue}</div>
                                        </div>
                                    </div>
                                `;
                                mSection.appendChild(item);
                            });
                            globalSearchResults.appendChild(mSection);
                        }
                        
                        const viewAll = document.createElement('a');
                        viewAll.href = '/algorithms.html';
                        viewAll.className = 'search-result-view-all';
                        viewAll.innerHTML = `View all results <i data-lucide="arrow-right" style="width: 14px;"></i>`;
                        globalSearchResults.appendChild(viewAll);
                    }
                    globalSearchResults.style.display = 'block';
                    lucide.createIcons();
                } catch (err) {
                    console.error('Global search error', err);
                }
            }, 300);
        });

        // Close search results when clicking outside
        document.addEventListener('click', (e) => {
            if (!globalSearchInput.contains(e.target) && !globalSearchResults.contains(e.target)) {
                globalSearchResults.style.display = 'none';
            }
        });
    }
});
