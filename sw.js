const CACHE_NAME = 'rock-qr-suite-v1';
const ASSETS_TO_CACHE = [
  './index.html',
  './share/index.html',
  './manifest.json',
  'https://cdn.tailwindcss.com',
  'https://unpkg.com/lucide@latest',
  'https://cdn.jsdelivr.net/npm/qrcode@1.5.3/build/qrcode.min.js',
  'https://unpkg.com/html5-qrcode@2.3.8/html5-qrcode.min.js'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(ASSETS_TO_CACHE);
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) {
            return caches.delete(key);
          }
        })
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request).then((cachedResponse) => {
      if (cachedResponse) {
        return cachedResponse;
      }
      return fetch(event.request).then((response) => {
        // Option to cache newly fetched resources dynamically if from matching domains
        if (event.request.url.includes('google-analyzer') || event.request.url.includes('firebase')) {
          return response;
        }
        return response;
      });
    }).catch(() => {
      // Fallback offline responses
      if (event.request.mode === 'navigate') {
        return caches.match('./index.html');
      }
    })
  );
});
