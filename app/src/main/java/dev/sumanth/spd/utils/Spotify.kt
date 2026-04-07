package dev.sumanth.spd.utils

 object SpotifyManager {

    val jsScript = """
        (async () => {
            const delay = ms => new Promise(res => setTimeout(res, ms));
            await delay(4000);
            console.log("JS: Starting scraper");
            
            const rows = [];
            const set = new Set();

            // Function to detect current page type and extract track info
            const detectPageType = () => {
                const url = window.location.href;
                if (url.includes('/track/')) {
                    return 'track';
                } else if (url.includes('/album/')) {
                    return 'album';
                } else if (url.includes('/playlist/')) {
                    return 'playlist';
                }
                return 'unknown';
            };

            // Function to extract single track (for track pages)
            const extractSingleTrack = () => {
                try {
                    // Try multiple selectors for track info on track pages
                    const titleElement = document.querySelector('h1[data-encore-id="heading"]') || 
                                        document.querySelector('h1');
                    const artistElement = document.querySelectorAll('a[href*="/artist/"]')[0];
                    
                    if (titleElement && artistElement) {
                        return {
                            title: titleElement.innerText.trim(),
                            artist: artistElement.innerText.trim()
                        };
                    }
                } catch (e) {
                    console.log("JS: Error extracting single track - " + e.message);
                }
                return null;
            };

            // Function to handle playlist/album pages
            const handleRows = () => {
                const tracks = [...document.querySelectorAll('[data-testid="track-row"]')]
                  .map(row => {
                    try {
                        const title = row.querySelector('[data-encore-id="listRowTitle"]')?.innerText;
                        const artist = row.querySelector('.encore-text-body-small')?.innerText;
                    
                        return { title, artist };
                    } catch (e) {
                        return null;
                    }
                  })
                  .filter(t => t && t.title && t.artist);

                tracks.forEach(track => {
                    const key = track.title + '-' + track.artist;
                    if (!set.has(key)) {
                        set.add(key);
                        rows.push(track);
                    }
                });
                console.log("JS: Found " + rows.length + " rows");
            };

            const pageType = detectPageType();
            console.log("JS: Detected page type: " + pageType);

            if (pageType === 'track') {
                // Handle single track
                const track = extractSingleTrack();
                if (track) {
                    rows.push(track);
                    console.log("JS: Extracted single track - " + track.title + " by " + track.artist);
                } else {
                    console.log("JS: ERROR Failed to extract track information");
                }
                console.log('FINAL_ROWS: ' + JSON.stringify(rows));
            } else if (pageType === 'album' || pageType === 'playlist') {
                // Handle album/playlist with scrolling
                let lastScroll = -1;
                let scrollCount = 0;
                let noScrollCount = 0;

                const interval = setInterval(() => {
                  window.scrollBy(0, 400);
                  handleRows();
                  scrollCount++;
                  console.log("JS: Scrolling... (" + scrollCount + ") Found: " + rows.length);

                  const currentScroll = window.scrollY;
                  if (currentScroll === lastScroll && lastScroll !== -1) {
                    noScrollCount++;
                    if (noScrollCount > 2) {
                        console.log("JS: Reached bottom");
                        console.log('FINAL_ROWS: ' + JSON.stringify(rows));
                        clearInterval(interval);
                        return;
                    }
                  } else {
                    noScrollCount = 0;
                  }
                  lastScroll = currentScroll;
                }, 1500);

                // Failsafe timeout after 60 seconds
                setTimeout(() => {
                    clearInterval(interval);
                    console.log("JS: Timeout reached, stopping scrape");
                    console.log('FINAL_ROWS: ' + JSON.stringify(rows));
                }, 60000);
            } else {
                console.log("JS: ERROR Unknown page type");
            }
        })();
    """.trimIndent()

}
