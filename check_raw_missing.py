import os, re
labels = [line.strip() for line in open('app/src/main/assets/labelmap.txt', 'r', encoding='utf-8') if line.strip()]
raw = 'app/src/main/res/raw'
missing = []
for label in labels:
    base = label.lower().replace('-', '_').replace(' ', '_')
    base = re.sub(r'[^a-z0-9_]', '', base)
    fname = f"{base}_bemba.m4a"
    if not os.path.exists(os.path.join(raw, fname)):
        missing.append(fname)
print('Missing in raw:', missing)
print('Count missing:', len(missing))
