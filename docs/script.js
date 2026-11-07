(() => {
  'use strict';
  const gallery = document.getElementById('screenshot-gallery');
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
  const moveGallery = direction => {
    if (!gallery) return;
    const card = gallery.querySelector('.screen-card');
    const distance = card ? card.getBoundingClientRect().width + 24 : 270;
    gallery.scrollBy({left: distance * direction, behavior: reducedMotion.matches ? 'auto' : 'smooth'});
  };
  document.getElementById('gallery-prev')?.addEventListener('click', () => moveGallery(-1));
  document.getElementById('gallery-next')?.addEventListener('click', () => moveGallery(1));
  gallery?.addEventListener('keydown', event => {
    if (event.key === 'ArrowRight' || event.key === 'ArrowLeft') {
      event.preventDefault();
      moveGallery(event.key === 'ArrowRight' ? 1 : -1);
    }
  });
})();
