import os
import re
import csv

translations_csv = 'translations.csv'
labelmap_txt = 'app/src/main/assets/labelmap.txt'
mapping_csv = 'bemba_audio_mapping.csv'

# Load labelmap
with open(labelmap_txt, 'r', encoding='utf-8') as f:
    labels = [line.strip() for line in f if line.strip()]

# Load translations.csv
rows = []
with open(translations_csv, 'r', encoding='utf-8', newline='') as f:
    reader = csv.DictReader(f)
    fieldnames = reader.fieldnames
    for row in reader:
        rows.append(row)

# Map English label -> row index
english_to_row = {r['English'].strip(): i for i, r in enumerate(rows)}

# Build mapping.csv and update translations (Bemba column) if empty
with open(mapping_csv, 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f)
    writer.writerow(['English','Bemba_translation_placeholder','filename_noext','resource_name'])

    for label in labels:
        # normalize filename
        base = label.lower().replace('-', '_').replace(' ', '_')
        base = re.sub(r'[^a-z0-9_]', '', base)
        filename = f"{base}_bemba.m4a"
        resource_name = base + '_bemba'

        # Update translations.csv Bemba column if empty
        if label in english_to_row:
            idx = english_to_row[label]
            if 'Bemba' in rows[idx] and (rows[idx]['Bemba'] is None or rows[idx]['Bemba'].strip() == ''):
                rows[idx]['Bemba'] = base  # placeholder

        writer.writerow([label, base, filename, resource_name])

# Write updated translations back
with open(translations_csv, 'w', encoding='utf-8', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=fieldnames)
    writer.writeheader()
    writer.writerows(rows)

print(f"Done. Updated {translations_csv} (Bemba placeholders) and wrote {mapping_csv}.")
