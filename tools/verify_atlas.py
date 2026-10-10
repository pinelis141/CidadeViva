#!/usr/bin/env python3
"""Pure-stdlib safeguard for the committed RGBA atlas, no Pillow in CI."""
from pathlib import Path
import struct, zlib
source=Path(__file__).resolve().parents[1]/'android/assets/city/atlas.png'
contents=source.read_bytes()
assert contents[:8] == b'\x89PNG\r\n\x1a\n', 'missing PNG signature'
assert len(contents)>4000, 'atlas unusually small'
w,h,depth,mode=struct.unpack('>IIBB',contents[16:26]); assert (w,h,depth,mode)==(576,768,8,6),(w,h,depth,mode)
assert b'IDAT' in contents and b'IEND' in contents
print('OK: 45-sprite original atlas PNG, 576x768 RGBA, %d bytes.' % len(contents))
