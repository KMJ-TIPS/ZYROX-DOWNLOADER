#!/usr/bin/env python3
"""Original, synthesized ZYROX interface sounds; PCM mono WAV, no sampled content."""
import math
import struct
import wave
from pathlib import Path

RATE = 44100
ROOT = Path(__file__).resolve().parents[1] / 'app/src/main/res/raw'


def write(name, seconds, sample):
    values = [max(-0.95, min(0.95, sample(i / RATE))) for i in range(round(seconds * RATE))]
    with wave.open(str(ROOT / name), 'wb') as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(RATE)
        out.writeframes(b''.join(struct.pack('<h', round(v * 32767)) for v in values))
    peak = max(abs(v) for v in values)
    print(f'{name}: {seconds:.3f}s, {RATE}Hz mono PCM16, peak={peak:.3f}, frames={len(values)}')


def tap(t):
    envelope = min(1, t / .0015) * math.exp(-78 * t) * min(1, max(0, (.065 - t) / .009))
    phase = 2 * math.pi * (1650 * t - 2600 * t * t)
    return .28 * envelope * (math.sin(phase) + .13 * math.sin(2.01 * phase))


def complete(t):
    total = 0
    for start, freq, amp in [(0, 523.251, .22), (.115, 659.255, .21), (.230, 783.991, .23)]:
        x = t - start
        if x < 0:
            continue
        envelope = min(1, x / .006) * math.exp(-8.8 * x) * min(1, max(0, (.63 - t) / .08))
        total += amp * envelope * (math.sin(2 * math.pi * freq * x) + .12 * math.sin(4 * math.pi * freq * x))
    return total


ROOT.mkdir(parents=True, exist_ok=True)
write('zyrox_tap.wav', .065, tap)
write('zyrox_complete.wav', .63, complete)
