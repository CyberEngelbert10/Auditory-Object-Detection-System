import os
import re
import csv

raw_dir = 'app/src/main/res/raw/bemba'
map_file = 'bemba_audio_mapping.csv'
translations_file = 'translations.csv'

files = os.listdir(raw_dir)
valid = [f for f in files if re.match(r'^[a-z0-9_]+_bemba\\.m4a$', f)]
invalid = [f for f in files if f not in valid]

print(f'Total files in {raw_dir}: {len(files)}')
print(f'Valid *_bemba.m4a files: {len(valid)}')
print('\nFirst 25 valid:')
print('\n'.join(valid[:25]))

if invalid:
    print('\nInvalid filenames:')
    print('\n'.join(invalid))

# count mapping rows
try:
    with open(map_file, encoding='utf-8', newline='') as f:
        reader = csv.reader(f)
        rows = list(reader)
    print(f'\nMapping rows (excluding header): {len(rows) - 1}')
except FileNotFoundError:
    print(f'\nMapping file {map_file} not found')

# Check translations.csv rows
try:
    with open(translations_file, encoding='utf-8', newline='') as f:
        reader = csv.DictReader(f)
        rows = list(reader)
    print(f'Translations rows: {len(rows)}')
    bemba_populated = sum(1 for r in rows if r.get('Bemba') and r.get('Bemba').strip())
    print(f'Rows with Bemba translation (placeholder or real): {bemba_populated}')
except FileNotFoundError:
    print(f'Translations file {translations_file} not found')
