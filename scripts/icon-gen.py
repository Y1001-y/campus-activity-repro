#!/usr/bin/env python3
"""S01 图标生成脚本：纯标准库生成一张真实的 PNG（无第三方依赖、无 mock）。

用法：python scripts/icon-gen.py [输出路径]
默认输出：scripts/output/app-icon.png
生成内容：128x128 蓝底圆角方块 + 白色字母 C（校园活动 Campus 首字母），逐像素写入 PNG。
"""
import os
import struct
import sys
import zlib

SIZE = 128


def png_chunk(tag: bytes, data: bytes) -> bytes:
    return (struct.pack('>I', len(data)) + tag + data
            + struct.pack('>I', zlib.crc32(tag + data) & 0xFFFFFFFF))


def make_png(path: str) -> str:
    # 圆角半径
    r = 24
    # 字母 C 的近似：外圆环去掉右侧开口
    cx, cy = 64, 64
    rows = bytearray()
    for y in range(SIZE):
        rows.append(0)  # PNG filter type 0
        for x in range(SIZE):
            # 圆角判断
            corner = False
            for ox, oy in ((r, r), (SIZE - 1 - r, r), (r, SIZE - 1 - r), (SIZE - 1 - r, SIZE - 1 - r)):
                if ((x < r and ox == r) or (x > SIZE - 1 - r and ox != r)) and \
                   ((y < r and oy == r) or (y > SIZE - 1 - r and oy != r)):
                    if (x - ox) ** 2 + (y - oy) ** 2 > r * r:
                        corner = True
            if corner:
                rows.extend((245, 246, 250, 255))  # 背景灰
                continue
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            # C：半径 25~40 的环，且去掉右侧 (x>cx 且 |y-cy|<14) 的开口
            if 25 <= d <= 40 and not (x > cx and abs(y - cy) < 14):
                rows.extend((255, 255, 255, 255))
            else:
                rows.extend((59, 108, 246, 255))  # #3b6cf6
    ihdr = struct.pack('>IIBBBBB', SIZE, SIZE, 8, 6, 0, 0, 0)  # RGBA
    data = (b'\x89PNG\r\n\x1a\n'
            + png_chunk(b'IHDR', ihdr)
            + png_chunk(b'IDAT', zlib.compress(bytes(rows), 9))
            + png_chunk(b'IEND', b''))
    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(data)
    return path


if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.join('scripts', 'output', 'app-icon.png')
    p = make_png(out)
    print(f'generated: {p} ({os.path.getsize(p)} bytes)')
