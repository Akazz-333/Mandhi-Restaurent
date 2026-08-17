// creative.js — Premium Interactions & Animations for Mandhi House

(function() {
  // 3D perspective card tilt effect
  function initCardTilt() {
    const cards = document.querySelectorAll('#mh-menu-grid .card, .contact-card');
    cards.forEach(card => {
      if (card.dataset.tiltInit) return;
      card.dataset.tiltInit = '1';

      card.style.transition = 'transform 0.15s ease-out, border-color 0.22s ease, box-shadow 0.22s ease';

      card.addEventListener('mousemove', (e) => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left; // x relative to card
        const y = e.clientY - rect.top;  // y relative to card
        
        const w = rect.width;
        const h = rect.height;
        
        // Normalize coordinates to -1 to 1 range
        const dx = (x - w / 2) / (w / 2);
        const dy = (y - h / 2) / (h / 2);
        
        // Max tilt of 8 degrees
        const tiltX = -dy * 8;
        const tiltY = dx * 8;
        
        card.style.transform = `perspective(1000px) rotateX(${tiltX.toFixed(1)}deg) rotateY(${tiltY.toFixed(1)}deg) translateY(-6px) scale(1.025)`;
        card.style.boxShadow = `0 18px 40px rgba(0, 0, 0, 0.45), 0 0 20px rgba(201, 151, 43, ${(Math.abs(dx) + Math.abs(dy)) * 0.12})`;
      });

      card.addEventListener('mouseleave', () => {
        card.style.transition = 'transform 0.5s cubic-bezier(0.16, 1, 0.3, 1), border-color 0.22s ease, box-shadow 0.22s ease';
        card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) translateY(0) scale(1)';
        card.style.boxShadow = '';
      });
    });
  }

  // Golden spark explosion particles on clicks/confirmations
  function createSparkExplosion(x, y) {
    const container = document.createElement('div');
    container.style.position = 'fixed';
    container.style.left = '0';
    container.style.top = '0';
    container.style.width = '100vw';
    container.style.height = '100vh';
    container.style.pointerEvents = 'none';
    container.style.zIndex = '999999';
    document.body.appendChild(container);

    const count = 18;
    for (let i = 0; i < count; i++) {
      const spark = document.createElement('div');
      spark.style.position = 'absolute';
      spark.style.left = `${x}px`;
      spark.style.top = `${y}px`;
      spark.style.width = `${Math.random() * 6 + 3}px`;
      spark.style.height = spark.style.width;
      spark.style.borderRadius = '50%';
      
      // Warm golden/amber theme HSL colors
      const hue = 32 + Math.random() * 22; // Gold to Orange range
      spark.style.backgroundColor = `hsl(${hue}, 100%, ${55 + Math.random() * 25}%)`;
      spark.style.boxShadow = '0 0 8px rgba(255, 190, 40, 0.9)';
      
      const angle = Math.random() * Math.PI * 2;
      const speed = Math.random() * 160 + 60; // speed in pixels
      const vx = Math.cos(angle) * speed;
      const vy = Math.sin(angle) * speed - 40; // slight upward motion bias
 
      container.appendChild(spark);
 
      let startTime = null;
      function animate(timestamp) {
        if (!startTime) startTime = timestamp;
        const elapsed = timestamp - startTime;
        const progress = elapsed / 850; // 850ms duration
 
        if (progress < 1) {
          const px = x + vx * progress;
          // Apply a gentle gravity pull
          const py = y + vy * progress + 0.5 * 250 * progress * progress;
          spark.style.left = `${px}px`;
          spark.style.top = `${py}px`;
          spark.style.opacity = (1 - progress).toString();
          spark.style.transform = `scale(${1 - progress * 0.4})`;
          requestAnimationFrame(animate);
        } else {
          spark.remove();
        }
      }
      requestAnimationFrame(animate);
    }
 
    // Clean up container
    setTimeout(() => {
      container.remove();
    }, 1000);
  }
 
  // Smooth scroll reveals for headings and sections
  function initScrollReveal() {
    const observerOptions = {
      root: null,
      threshold: 0.1,
      rootMargin: '0px 0px -50px 0px'
    };
 
    const revealObserver = new IntersectionObserver((entries, observer) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('reveal-active');
          observer.unobserve(entry.target); // Trigger only once
        }
      });
    }, observerOptions);
 
    // Track targets: menu-section-group title, sections, etc.
    const revealTargets = document.querySelectorAll('.menu-section-group, .hero .tagline, .hero .divider-wrap, .mh-section.reveal-hidden');
    revealTargets.forEach(target => {
      revealObserver.observe(target);
    });
  }

  // 3D Stacking Cards Scroll Animation (inspired by Phenomenon Studio's "The Nocturne")
  function initCardStacking() {
    const sections = document.querySelectorAll('.mh-section');
    window.addEventListener('scroll', () => {
      const viewportHeight = window.innerHeight;
      sections.forEach((sec, idx) => {
        const rect = sec.getBoundingClientRect();
        
        // If the section is stuck at top: 0 and is currently being covered by the next one
        if (rect.top <= 4 && rect.bottom > 0) {
          // Progress goes from 0 (not covered) to 1 (completely covered)
          const progress = Math.max(0, Math.min(1, 1 - (rect.bottom / viewportHeight)));
          
          // Apply hardware-accelerated scale-down, opacity-fade, and slight translation
          const scale = 1 - progress * 0.08;      // scale down to 0.92
          const opacity = 1 - progress * 0.55;    // fade opacity to 0.45
          const translateY = -progress * 50;      // translate up by 50px
          
          sec.style.transform = `scale(${scale}) translateY(${translateY}px)`;
          sec.style.opacity = opacity;
        } else if (rect.top > 4) {
          // Reset styles when scrolling back up
          sec.style.transform = '';
          sec.style.opacity = '';
        }
      });
    });
  }

  // Export functions globally
  window.MH_Creative = {
    initCardTilt,
    createSparkExplosion,
    initScrollReveal,
    initCardStacking
  };

  // Run auto-init on DOMContentLoaded
  window.addEventListener('DOMContentLoaded', () => {
    initScrollReveal();
    // initCardStacking(); // Disabled to allow natural document flow and scroll-unlock
  });
})();
