#!/usr/bin/env python3
"""Reproducible ORIGINAL game-ready transparent pixel-art sprites.
Uses Pillow only when generating assets; no runtime Python dependency.
Each sprite uses a fixed 72x128 cell with a bottom-centered isometric footprint.
"""
from PIL import Image, ImageDraw
from pathlib import Path
import random

CW, CH, COLS = 72, 128, 8
sprites=[]

def rgba(c):return tuple(c) if isinstance(c,tuple) else c

def p(draw, pts, fill, outline=None):
    draw.polygon(pts, fill=fill)
    if outline:draw.line(pts+[pts[0]], fill=outline, width=1, joint='curve')

def diamond(d,cx,cy,hw,hh,fill,border=None):
    p(d,[(cx,cy-hh),(cx+hw,cy),(cx,cy+hh),(cx-hw,cy)],fill,border)

def base(kind, seed):
    im=Image.new('RGBA',(CW,CH));d=ImageDraw.Draw(im)
    rng=random.Random(seed*1517+12)
    grass=[('#8cab69','#628d57'),('#86a765','#608750'),('#91ae71','#6a9459')][seed%3]
    if kind=='grass':
        diamond(d,36,112,32,16,grass[0],grass[1])
        for i in range(15):
            x=rng.randrange(13,59);y=rng.randrange(103,122)
            if abs(x-36)*0.5+abs(y-112)>14:continue
            c=rng.choice(['#769c5a','#9db979','#bdd08d','#628f58'])
            d.rectangle((x,y,x+1,y+1),fill=c)
        for i in range(2):
            x=rng.randrange(20,52);y=rng.randrange(108,118)
            if abs(x-36)*.5+abs(y-112)>12:continue
            d.line([(x,y),(x+1,y-2)],fill='#c4cf84',width=1)
    elif kind=='water':
        diamond(d,36,112,32,16,['#3f92b0','#478da7'][seed%2],'#356a8b')
        for i in range(5):
            x=rng.randrange(17,55);y=rng.randrange(105,118)
            if abs(x-36)*.5+abs(y-112)>11:continue
            d.line((x,y,x+5,y-2),fill=['#82c1c9','#76b8c7','#a5d7d5'][i%3],width=1)
    elif kind=='zone':
        colors=[('#668d6a','#53775a','#d9cd92'),('#709bb0','#4c758b','#b3e8f0'),('#c8a173','#977750','#dcd29b')]
        top,edge,accent=colors[seed]
        diamond(d,36,112,32,16,top,edge)
        # Fine survey stakes, subtle without obscuring the planned zone.
        for x,y in [(23,104),(49,104),(23,120),(49,120)]:
            d.rectangle((x,y,x+2,y+2),fill=accent)
        d.line([(23,112),(36,104),(49,112)], fill=accent,width=1)
    elif kind=='road':
        mask=seed
        diamond(d,36,112,32,16,'#555a59','#3d4746')
        d.line([(8,113),(36,99),(64,113)],fill='#67716c',width=1)
        # neighbour bits map to four diamond diagonal radii.
        dirs=[(16,8),(-16,-8),(-16,8),(16,-8)]
        if mask==0:
            diamond(d,36,112,5,3,'#bea96b')
        for bit,(x,y) in enumerate(dirs):
            if mask&(1<<bit):
                d.line((36+int(.34*x),112+int(.34*y),36+int(.72*x),112+int(.72*y)), fill='#e0c47c',width=2)
        d.rectangle((35,111,36,112),fill='#a3aba3')
    return im

def add(im): sprites.append(im)
for i in range(3):add(base('grass',i))                  # 0-2
for i in range(2):add(base('water',i))                  # 3-4
for i in range(3):add(base('zone',i))                   # 5-7
for i in range(16):add(base('road',i))                  # 8-23

def tree(variant):
    im=base('grass',variant);d=ImageDraw.Draw(im)
    dx=[-3,1,2][variant];x=36+dx
    d.rectangle((x-3,75,x+2,111),fill='#705a3e')
    d.rectangle((x-1,82,x,105),fill='#94714c')
    c=[('#2f6b54','#478b61','#81b46c'),('#375f55','#548963','#82a96c'),('#315b47','#447d54','#70a765')][variant]
    # intentionally sharp, pixel stepped leafy crowns
    p(d,[(x,53),(x+8,61),(x+7,64),(x+16,74),(x+12,79),(x+20,88),(x+15,96),(x-18,96),(x-21,88),(x-13,79),(x-15,73),(x-8,63)],c[0],'#244d46')
    p(d,[(x,55),(x+7,63),(x+10,75),(x+6,80),(x+13,89),(x+8,89),(x+2,80),(x-6,80),(x-8,72)],c[1])
    for xx,yy in [(-6,70),(4,68),(-11,86),(7,84),(-2,59)]:
        d.rectangle((x+xx,yy,x+xx+3,yy+2),fill=c[2])
    return im
for i in range(3):add(tree(i))                           # 24-26

def building(style,level,variant):
    im=base('zone',style);d=ImageDraw.Draw(im)
    x=36; bottom=109
    if style==0: # housing: cream/warm wood, slate or russet roof
        h=[32,48,63][level-1]
        lt,rt,roof,edge=(('#ecd3a0','#c1a778','#bd655f','#855b53') if variant==0 else ('#b9c9a4','#8aa186','#a47058','#6a5246'))
    elif style==1: # commerce: cool glass with bright awning and signage
        h=[26,43,58][level-1]
        lt,rt,roof,edge=(('#d2e7df','#86b7c6','#4c9bad','#436e7b') if variant==0 else ('#d9dac0','#86aaa6','#618e86','#4c6e70'))
    else: # industry: broad low factory and brick smokestacks
        h=[25,38,49][level-1]
        lt,rt,roof,edge=(('#c2a385','#987c68','#667d7c','#586360') if variant==0 else ('#b49b86','#8b7d77','#727479','#5c5d65'))
    top=bottom-h
    # Side walls follow square/isometric axes.
    p(d,[(14,top+11),(36,top+21),(36,bottom+1),(14,bottom-11)],lt,edge)
    p(d,[(36,top+21),(58,top+11),(58,bottom-11),(36,bottom+1)],rt,edge)
    diamond(d,36,top+11,22,11,roof,edge)
    d.line([(16,top+11),(36,top+2),(56,top+11)],fill='#e2bd9d' if style==0 else '#a9c4c4',width=2)
    if style==0:
        if level==1: # cottage roof/ridge and door; clearly different from tall shops
            d.rectangle((23,bottom-19,28,bottom-12),fill='#6f654d')
            d.rectangle((43,bottom-21,49,bottom-16),fill='#89b3bd')
        else:
            for y in range(top+30,bottom-9,12):
                d.rectangle((21,y,26,y+5),fill='#6d8994')
                d.rectangle((43,y,48,y+5),fill='#7f9aa2')
            d.rectangle((30,bottom-12,34,bottom-3),fill='#775d51')
        if variant==1: d.rectangle((52,top+15,54,top+22),fill='#e4d49d')
    elif style==1:
        # storefront: broad window + neon awning, gold signage
        d.polygon([(18,bottom-22),(36,bottom-15),(36,bottom-12),(18,bottom-19)],fill='#dbb976')
        d.polygon([(36,bottom-15),(55,bottom-22),(55,bottom-19),(36,bottom-12)],fill='#caa45c')
        d.rectangle((22,bottom-15,29,bottom-9),fill='#527f97')
        d.rectangle((42,bottom-16,50,bottom-10),fill='#5b899a')
        if level>1:
            for y in range(top+25,bottom-27,12):
                d.rectangle((23,y,28,y+5),fill='#5e91a5')
                d.rectangle((44,y,49,y+5),fill='#6392a0')
        d.rectangle((31,top+12,35,top+14),fill='#edf0ba')
    else:
        # Industrial vents, striped entrance and rooftop stack.
        d.rectangle((22,bottom-17,33,bottom-5),fill='#5e6460')
        for j in range(3):d.line((23,bottom-15+4*j,32,bottom-15+4*j),fill='#969f97',width=1)
        d.rectangle((45,bottom-17,51,bottom-10),fill='#83918b')
        chimney_x=46 if variant==0 else 26
        chimney_y=top+10
        d.rectangle((chimney_x-3,chimney_y-19,chimney_x+3,chimney_y+5),fill='#a16f60')
        d.rectangle((chimney_x-4,chimney_y-20,chimney_x+4,chimney_y-17),fill='#5b5e5b')
        d.rectangle((chimney_x-2,chimney_y-12,chimney_x+2,chimney_y-10),fill='#d0b498')
    return im
for style in range(3):
    for level in range(1,4):
        for variant in range(2):add(building(style,level,variant)) # 27..44

# Alpha 3: 16 shoreline transitions, 6 tree silhouettes, 4 ground accents,
# and 9 new architectural variants. Existing indices 0-44 remain unchanged.

def shore_water(mask):
    im=Image.new('RGBA',(CW,CH));d=ImageDraw.Draw(im)
    diamond(d,36,112,32,16,'#438da2')
    # All edges are contiguous. Only edges *without* water neighbours form a shore.
    # Bit order SE(1), NW(2), SW(4), NE(8), matching City.waterMask.
    edges=[[(67,112),(36,127)],[(5,112),(36,97)],
           [(5,112),(36,127)],[(36,97),(67,112)]]
    inside=[[(62,111),(36,124)],[(10,111),(36,100)],
            [(10,113),(36,124)],[(36,100),(62,113)]]
    for bit,edge in enumerate(edges):
        if not (mask & (1<<bit)):
            d.line(edge,fill='#b2c4a7',width=3)
            d.line(inside[bit],fill='#83bec0',width=2)
    rng=random.Random(200+mask)
    for i in range(8):
        x=rng.randrange(14,57);y=rng.randrange(104,119)
        if abs(x-36)*.5+abs(y-112)>12:continue
        d.line((x,y,x+rng.randrange(2,6),y-1),fill=rng.choice(['#63aabb','#79bec8','#a1ccd0']),width=1)
    return im
for mask in range(16):add(shore_water(mask))          # 45-60

def leafy_tree(v):
    im=base('grass',v+1);d=ImageDraw.Draw(im)
    rng=random.Random(801+v)
    x=34+(v%3-1)*3
    if v==3 or v==4: # lighter birch silhouette
        d.rectangle((x-2,73,x+2,108),fill='#d4c9a4')
        for yy in range(76,105,9):d.line((x-1,yy,x+1,yy+1),fill='#6f6f62')
    else:
        d.rectangle((x-3,77,x+3,110),fill='#785a3c')
    if v==5: # rounded autumn tree
        canopy=['#a2824a','#d4ac59','#ad784b']
    elif v==3 or v==4:
        canopy=['#527d50','#88a45b','#a1b872']
    else:
        canopy=['#3f7458','#598a55','#81ac69']
    # Multiple overlapping stepped circles produce an irregular broad crown.
    for xx,yy,rad in [(x-13,69,12),(x+11,69,12),(x,58,16),
                       (x-5,80,11),(x+6,77,12)]:
        col=canopy[rng.randrange(3)]
        d.rectangle((xx-rad//2,yy-rad//2,xx+rad//2,yy+rad//2),fill=col)
        d.ellipse((xx-rad,yy-rad,xx+rad,yy+rad),fill=col)
    for i in range(17):
        xx=x+rng.randrange(-19,20);yy=rng.randrange(52,83)
        if (xx-x)**2/400+(yy-67)**2/340<1.1:
            d.rectangle((xx,yy,xx+2,yy+2),fill=rng.choice(canopy[1:]))
    return im
for v in range(6):add(leafy_tree(v))                    # 61-66

def meadow(v):
    im=base('grass',v+5);d=ImageDraw.Draw(im)
    rng=random.Random(413+v)
    for i in range(10):
        x=rng.randrange(15,57);y=rng.randrange(102,124)
        if abs(x-36)*.5+abs(y-112)>12:continue
        if v==0: # golden wildflowers
            d.rectangle((x,y-2,x,y),fill='#5f8b52')
            d.point((x,y-3),fill='#e1c77a')
        elif v==1: # stones
            d.polygon([(x-2,y),(x+1,y-2),(x+4,y),(x+1,y+2)],fill='#859685')
            d.line((x-2,y,x+1,y-2),fill='#c2c8a5')
        elif v==2: # scrub
            d.rectangle((x-2,y-1,x+3,y+1),fill='#568852')
            d.rectangle((x,y-4,x+1,y-1),fill='#8fab5f')
        else: # dry speckles
            d.line((x-2,y,x+2,y+1),fill='#b6b27a')
    return im
for v in range(4):add(meadow(v))                        # 67-70

def landmark_variant(style,level):
    im=building(style,level,2);d=ImageDraw.Draw(im)
    h=([32,48,63] if style==0 else [26,43,58] if style==1 else [25,38,49])[level-1]
    top=109-h
    if style==0: # porched houses with alternate teal roof and chimney
        diamond(d,36,top+11,22,11,'#537a78','#3d5c62')
        d.polygon([(17,top+11),(36,top+3),(55,top+11)],fill='#77989a')
        d.rectangle((42,top-5,47,top+8),fill='#b78d6d')
        d.rectangle((41,top-7,48,top-4),fill='#63585c')
        d.rectangle((20,109-19,29,109-15),fill='#d8b67b')
        d.line((18,109-20,33,109-20),fill='#f1dab1',width=2)
    elif style==1: # market with large sign, canvas awning and roof skylight
        diamond(d,36,top+11,22,11,'#8d7a66','#657b73')
        d.rectangle((32,top+5,39,top+8),fill='#bbd8cf')
        d.rectangle((17,109-27,32,109-22),fill='#e4b978')
        d.rectangle((41,109-26,55,109-22),fill='#e4b978')
        for i in (21,27,44,51):d.line((i,109-22,i,109-19),fill='#b56460')
    else: # tall silo and skylight differentiate industrial footprint
        diamond(d,36,top+11,22,11,'#697784','#52636d')
        d.rectangle((20,top-16,28,top+6),fill='#c1b2a0')
        d.rectangle((19,top-17,29,top-14),fill='#6a696b')
        d.rectangle((36,top+7,44,top+10),fill='#91a8b0')
        d.rectangle((47,top+13,51,top+21),fill='#9a6f60')
    return im
for style in range(3):
    for level in range(1,4):add(landmark_variant(style,level)) # 71-79
# Alpha 2: four intentionally distinct architectural silhouettes and palettes per
# sector and level, maintaining the old 80 sprite IDs for backwards compatibility.
def neighborhood_variant(style,level,variation):
    im=building(style,level,variation%2)
    data=im.load()
    base_colors=(
        [('#ecd3a0','#c1a778','#bd655f','#855b53'),('#b9c9a4','#8aa186','#a47058','#6a5246')],
        [('#d2e7df','#86b7c6','#4c9bad','#436e7b'),('#d9dac0','#86aaa6','#618e86','#4c6e70')],
        [('#c2a385','#987c68','#667d7c','#586360'),('#b49b86','#8b7d77','#727479','#5c5d65')]
    )
    colorways=(
        [('#d6e6dc','#8ea9a7','#5484b5','#4a6582'),('#ebc3a5','#c38b80','#d99449','#96673f'),('#ded3eb','#aaa2ca','#8b6697','#604f77'),('#d4dcdb','#899ca5','#465365','#303a48')],
        [('#eef0d7','#adc39d','#4c986c','#38684f'),('#f3d4b4','#d09b8e','#cf6a57','#a0454a'),('#dad6ed','#9f97c5','#9a70ae','#705684'),('#cedce6','#7e9bb6','#426b9b','#375879')],
        [('#d0b49a','#92715d','#a75e40','#7e4337'),('#b8d4db','#7b9fab','#457d9c','#376076'),('#d5cda1','#9a9878','#c5a455','#947d43'),('#b6bfc4','#77888e','#515f70','#39444f')]
    )
    from PIL import ImageColor
    old=base_colors[style][variation%2]
    new=colorways[style][variation]
    palette={ImageColor.getrgb(src)+(255,):ImageColor.getrgb(dst)+(255,) for src,dst in zip(old,new)}
    for py in range(CH):
        for px in range(CW):
            color=data[px,py]
            if color in palette:data[px,py]=palette[color]
    d=ImageDraw.Draw(im)
    heights=([32,48,63] if style==0 else [26,43,58] if style==1 else [25,38,49])
    top=109-heights[level-1]
    if style==0:
        if variation==0: # tiled blue townhouses with a roof dormer
            d.rectangle((31,top-2,40,top+7),fill='#d2e9ea')
            p(d,[(29,top-2),(41,top-2),(35,top-8)],'#336080')
            d.rectangle((20,97,24,104),fill='#43875e')
            d.rectangle((52,94,55,101),fill='#43875e')
        elif variation==1: # ochre apartments with balconies
            for floor in range(level):
                yy=top+31+floor*12
                d.line((19,yy,33,yy+4),fill='#704f46',width=3)
                d.line((41,yy+4,55,yy),fill='#704f46',width=3)
            d.rectangle((44,top-1,49,top+9),fill='#ac694c')
        elif variation==2: # pink roof and window boxes
            for yy in range(top+29,104,11):
                d.rectangle((21,yy,27,yy+2),fill='#477e66')
                d.rectangle((45,yy,51,yy+2),fill='#a9d883')
            d.polygon([(27,top+6),(36,top-4),(45,top+6)],fill='#e1b5d7')
        else: # slate corner flat, tall vertical window ribbon
            d.rectangle((28,top+24,32,101),fill='#b6d4e3')
            d.rectangle((45,top+23,49,max(top+24,96)),fill='#8fbcd1')
            d.rectangle((16,94,23,100),fill='#57877a')
    elif style==1:
        if variation==0: # green produce market
            d.rectangle((17,88,35,94),fill='#b2d98b')
            d.rectangle((38,87,55,93),fill='#72b47c')
            for i in range(4):d.rectangle((20+i*9,96,23+i*9,98),fill='#e6d78f')
        elif variation==1: # warm cafe, patio lamps
            d.rectangle((18,84,54,89),fill='#e5b779')
            for xx in (19,27,35,43,51):d.point((xx,84),fill='#fbecab')
            d.rectangle((40,94,47,101),fill='#6e9b9a')
        elif variation==2: # striped diner canopy
            for i in range(7):d.rectangle((18+i*5,89,20+i*5,95),fill='#f0e7d7' if i%2 else '#bf738f')
            d.rectangle((26,top+5,46,top+8),fill='#eecad1')
        else: # blue office and rooftop antenna
            for xx in (19,28,42,51):
                for yy in range(top+25,88,11):d.rectangle((xx,yy,xx+3,yy+5),fill='#b5e7ef')
            d.line((36,top-12,36,top+1),fill='#c9d5d4',width=2)
            d.rectangle((33,top-14,39,top-12),fill='#f6b66d')
    else:
        if variation==0: # red brick chimney
            d.rectangle((45,top-20,53,top+12),fill='#ab604d')
            for yy in range(top-18,top+10,6):d.line((45,yy,53,yy),fill='#e1b696')
        elif variation==1: # two blue cooling pipes
            for xx in (20,47):
                d.rectangle((xx,top-15,xx+5,top+12),fill='#5a8b9e')
                d.rectangle((xx-1,top-18,xx+6,top-15),fill='#b4cfce')
        elif variation==2: # cargo warehouse, yellow roof lamps
            for xx in (22,32,42,52):d.rectangle((xx,top+7,xx+3,top+10),fill='#efda9a')
            d.rectangle((20,95,33,104),fill='#6a7773')
            d.rectangle((20,99,33,101),fill='#c5a865')
        else: # steel works, tall water tower
            d.rectangle((20,top-15,30,top+14),fill='#8397a1')
            d.rectangle((18,top-18,32,top-14),fill='#c8d2d2')
            d.line((23,top+12,23,100),fill='#637985',width=2)
    return im
for style in range(3):
    for level in range(1,4):
        for variation in range(4):add(neighborhood_variant(style,level,variation)) # 80-115
assert len(sprites)==116,len(sprites)


rows=(len(sprites)+COLS-1)//COLS
atlas=Image.new('RGBA',(CW*COLS,CH*rows),(0,0,0,0))
for i,im in enumerate(sprites):atlas.paste(im,((i%COLS)*CW,(i//COLS)*CH))
output=Path(__file__).resolve().parents[1]/'android/assets/city/atlas.png'
output.parent.mkdir(parents=True, exist_ok=True)
atlas.save(output,optimize=True)
print(f'{len(sprites)} original isometric sprites => {atlas.size}, {output.stat().st_size} bytes: {output}')
