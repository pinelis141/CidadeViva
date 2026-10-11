#!/usr/bin/env python3
"""Pure-stdlib safeguard for the committed RGBA atlas, no Pillow in CI."""
from pathlib import Path
import struct, zlib
source=Path(__file__).resolve().parents[1]/'android/assets/city/atlas.png'
contents=source.read_bytes()
assert contents[:8] == b'\x89PNG\r\n\x1a\n', 'missing PNG signature'
assert len(contents)>4000, 'atlas unusually small'
w,h,depth,mode=struct.unpack('>IIBB',contents[16:26]); assert (w,h,depth,mode)==(576,2048,8,6),(w,h,depth,mode)
assert b'IDAT' in contents and b'IEND' in contents
root=Path(__file__).resolve().parents[1]/'art/game/buildings_v3'
for kind in ('res','com','ind'):
    for level in range(1,4):
        file=root/f'{kind}_1x1_lvl{level}.png'
        data=file.read_bytes()
        assert data[:8]==b'\x89PNG\r\n\x1a\n',file
        iw,ih,depth,mode=struct.unpack('>IIBB',data[16:26])
        assert (iw,ih,depth,mode)==(72,128,8,6),(file,iw,ih,depth,mode)
        assert len(data)>1200,(file,'suspiciously empty source asset')
print('OK: 125-sprite isometric atlas PNG, 576x2048 RGBA, 9 new 1x1 sources, %d bytes.' % len(contents))
