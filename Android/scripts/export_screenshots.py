#!/usr/bin/env python3
"""Export the final, unmodified frame of the recorded production-UI APNGs."""
import argparse
import hashlib
import json
from pathlib import Path
try:
    from PIL import Image
except ImportError:
    raise SystemExit('Install Pillow first: python3 -m pip install Pillow')

project = Path(__file__).resolve().parent.parent
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--snapshot-dir', type=Path, default=project/'app/src/test/snapshots/videos')
parser.add_argument('--output', type=Path, default=project/'docs/screenshots')
args = parser.parse_args()
args.output.mkdir(parents=True, exist_ok=True)
methods = {
    'homeDark': ('home-dark', 36),
    'homeLight': ('home-light', 36),
    'libraryDark': ('library-dark', 36),
    'musicDark': ('music-dark', 36),
    'settingsDark': ('settings-dark', 29),
    'splash': ('splash', 36),
}
images = []
for method, (name, api) in methods.items():
    matches = list(args.snapshot_dir.glob(f'*_ProductionScreenshots_{method}_*.png'))
    if len(matches) != 1:
        raise SystemExit(f'Expected exactly one recorded APNG for {method}, found {len(matches)}')
    source = matches[0]
    target = args.output/f'{name}.png'
    with Image.open(source) as image:
        frames = image.n_frames
        image.seek(frames-1)
        # Selecting a recorded frame preserves the UI pixels. No mockup, redraw,
        # compositing, resizing, cropping, or screenshot content replacement.
        image.copy().save(target)
        size = list(image.size)
    images.append({
        'file': target.name, 'pixels': size, 'api': api, 'framesRecorded': frames,
        'selectedFrame': frames-1, 'selectedTimeMillis': 1500,
        'sha256': hashlib.sha256(target.read_bytes()).hexdigest(),
        'sourceRecording': source.name,
        'sourceSha256': hashlib.sha256(source.read_bytes()).hexdigest(),
    })
metadata = {
    'kind': 'App UI renders, not device captures',
    'renderer': 'Paparazzi 2.0.0-alpha02 / Android layoutlib',
    'deviceConfiguration': 'Pixel 5, 1080 x 2340, system chrome hidden',
    'productionEntryPoints': ['AppRoot', 'ZyroxSplashView'],
    'state': 'Empty first-use library; no native engine, downloads, account or network initialized',
    'settingsApiReason': 'Supported Android 10 scoped-storage path; layoutlib has no real StorageManager/package service',
    'images': images,
}
(args.output/'provenance.json').write_text(json.dumps(metadata, indent=2)+'\n')
print(json.dumps(metadata, indent=2))
